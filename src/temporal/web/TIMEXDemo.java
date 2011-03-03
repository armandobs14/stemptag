package temporal.web;

import com.aliasi.chunk.Chunker;
import com.aliasi.chunk.Chunk;
import com.aliasi.chunk.Chunking;
import com.aliasi.chunk.NBestChunker;
import com.aliasi.chunk.ConfidenceChunker;
import com.aliasi.xml.SAXWriter;
import com.aliasi.xml.SimpleElementHandler;
import com.aliasi.util.ScoredObject;
import java.util.Iterator;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import temporal.NormalizedChunk;
import temporal.TIMEXRuleDisambiguation;
import temporal.rules.TIMEXRuleAnnotator;

public class TIMEXDemo extends SentenceDemo {

    private Chunker mEntityChunker;

    public TIMEXDemo(String tokenizerFactoryClassName,
                           String sentenceModelClassName,
                           String chunkerResourceName,
                           String genre) {
        super(tokenizerFactoryClassName,sentenceModelClassName,
              "TIMEX Demo",
              "TIMEX Demo for " + genre);
        if (chunkerResourceName.equals("rules.TIMEXRuleAnnotator")) {
        	try {
        		mEntityChunker = new TIMEXRuleAnnotator();
        	} catch ( Exception e ) {
        		e.printStackTrace();
        	}
    	} else mEntityChunker = (Chunker) readResource(chunkerResourceName);

        declareProperty(Constants.RESULT_TYPE_PARAM,
        				Constants.RESULT_TYPE_VALS,
        				Constants.RESULT_TYPE_TOOL_TIP);
    }

    public void processSentence(String sentenceText, SAXWriter writer,
                                Properties properties,
                                int sentId) throws SAXException {
        String resultType = properties.getProperty("resultType");
        int pos = 0;
        if (resultType.equals(Constants.FIRST_BEST_RESULT_TYPE)) {
            Chunking mentionChunking = mEntityChunker.chunk(sentenceText);
            writeMentionChunking(writer,mentionChunking);
        } else if (resultType.equals(Constants.N_BEST_RESULT_TYPE)) {
            if (!(mEntityChunker instanceof NBestChunker)) {
                writer.characters("THIS NE MODEL DOES NOT SUPPORT N-BEST");
                return;
            }
            NBestChunker nBestChunker = (NBestChunker) mEntityChunker;
            char[] cs = sentenceText.toCharArray();
            Iterator<ScoredObject<Chunking>> chunkingIt = nBestChunker.nBest(cs,0,cs.length, Constants.MAX_N_BEST);
            for (int i = 0; i < Constants.MAX_N_BEST && chunkingIt.hasNext(); ++i) {
                ScoredObject<Chunking> so = chunkingIt.next();
                double log2P = so.score();
                Chunking chunking = so.getObject();
                writer.startSimpleElement("analysis",
                                          "rank",Integer.toString(i),
                                          "jointLog2P",Double.toString(log2P));
                writeMentionChunking(writer,chunking);
                writer.endSimpleElement("analysis");
            }
        } else if (resultType.equals(Constants.CONF_RESULT_TYPE)) {
            if (!(mEntityChunker instanceof ConfidenceChunker)) {
                writer.characters("THIS NE MODEL DOES NOT SUPPORT CONFIDENCE CHUNKING");
                return;
            }
            writer.startSimpleElement("nBestEntities");
            writer.startSimpleElement("s");
            writer.characters(sentenceText);
            writer.endSimpleElement("s");
            writer.startSimpleElement("confidence");
            ConfidenceChunker confChunker = (ConfidenceChunker) mEntityChunker;
            char[] cs = sentenceText.toCharArray();
            Iterator<Chunk> it = confChunker.nBestChunks(cs,0,cs.length, Constants.MAX_CONF);
            for (int i = 0; i < Constants.MAX_CONF && it.hasNext(); ++i) {
                Chunk chunk = it.next();
                int start = chunk.start();
                int end = chunk.end();
                String type = chunk.type();
                String mentionText = sentenceText.substring(start,end);
                double score = chunk.score();
                double condProb = java.lang.Math.pow(2.0,score);
        		String normalized = ((NormalizedChunk)(chunk)).getNormalized();
         		if (normalized.contains("=")) normalized = normalized.substring(normalized.indexOf('"')+1,normalized.lastIndexOf('"'));
                Attributes atts = SimpleElementHandler
                    .createAttributes("VAL",normalized,
                                      "START",Integer.toString(start),
                                      "END",Integer.toString(end),
                                      "condProb",Double.toString(condProb),
                                      "TEXT",mentionText,
                                      "RANK",Integer.toString(i));
                writer.startSimpleElement("TIMEX",atts);
                writer.characters(mentionText);
                writer.endSimpleElement("TIMEX");
            }
            writer.endSimpleElement("confidence");
        }
    }

    void writeMentionChunking(SAXWriter writer, Chunking mentionChunking) throws SAXException {
        Set<Chunk> chunkSet = new TreeSet<Chunk>(Chunk.TEXT_ORDER_COMPARATOR);
        chunkSet.addAll(mentionChunking.chunkSet());
   //     Iterator<Chunk> it = chunkSet.iterator();
        String text = mentionChunking.charSequence().toString();
        System.out.println(text+"!!!");
        int pos = 0;
        for ( Chunk chunk : mentionChunking.chunkSet()) {
 	    		NormalizedChunk timex = new NormalizedChunk(chunk);
 	    		timex.setNormalized(TIMEXRuleDisambiguation.createCanonicalForm(text.substring(chunk.start(),chunk.end())));
 	    		int start = timex.start();
 	            int end = timex.end();
 	            String chunkText = text.substring(start,end);
 	            System.out.println(chunkText+"#");
 	            String whitespace = text.substring(pos,start);
 	    		String normalized = ((NormalizedChunk)(timex)).getNormalized();
 	     		if (normalized.contains("=")) normalized = normalized.substring(normalized.indexOf('"')+1,normalized.lastIndexOf('"'));
 	            Attributes atts = SimpleElementHandler.createAttributes("VAL",normalized);
 	            writer.characters(whitespace);
 	            writer.startSimpleElement("TIMEX",atts);
 	            writer.characters(chunkText);
 	            writer.endSimpleElement("TIMEX");
 	            pos = end;
        }
     /*   while (it.hasNext()) {
            Chunk neChunk = it.next();
            int start = neChunk.start();
            int end = neChunk.end();
            String type = neChunk.type();
            String chunkText = text.substring(start,end);
            System.out.println(chunkText+"#");
            String whitespace = text.substring(pos,start);
    		String normalized = ((NormalizedChunk)(neChunk)).getNormalized();
     		if (normalized.contains("=")) normalized = normalized.substring(normalized.indexOf('"')+1,normalized.lastIndexOf('"'));
            Attributes atts = SimpleElementHandler.createAttributes("VAL",normalized);
            writer.characters(whitespace);
            writer.startSimpleElement("TIMEX");
            writer.characters(chunkText);
            writer.endSimpleElement("TIMEX");
            pos = end;
        }*/
        String whitespace = text.substring(pos);
        writer.characters(whitespace);
    }
            
}
