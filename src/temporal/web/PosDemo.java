package temporal.web;

import com.aliasi.classify.ConditionalClassification;
import com.aliasi.hmm.HmmDecoder;
import com.aliasi.hmm.HiddenMarkovModel;
import com.aliasi.tag.Tagging;
import com.aliasi.tag.TagLattice;
import com.aliasi.tag.ScoredTagging;
import com.aliasi.tokenizer.Tokenizer;
import com.aliasi.xml.SAXWriter;
import com.aliasi.util.FastCache;
import com.aliasi.util.Strings;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

public class PosDemo extends SentenceDemo {

    private final HmmDecoder mDecoder;

    public PosDemo(String tokenizerFactoryClassName,
		   String sentenceModelClassName,
		   String hmmResourceName,
		   String cacheSize,
		   String logCacheSize,
		   String genre) {

	super(tokenizerFactoryClassName,sentenceModelClassName, DEMO_NAME,DEMO_DESCRIPTION+genre);
	HiddenMarkovModel hmm = (HiddenMarkovModel) readResource(hmmResourceName);
	int cacheSizeInt = Integer.valueOf(cacheSize);
	FastCache<String,double[]> cache 
	    = cacheSizeInt > 0
	    ? new FastCache<String,double[]>(cacheSizeInt)
	    : null;
	int logCacheSizeInt = Integer.valueOf(logCacheSize);
	FastCache<String,double[]> logCache 
	    = logCacheSizeInt > 0
	    ? new FastCache<String,double[]>(logCacheSizeInt)
	    : null;
	mDecoder = new HmmDecoder(hmm,cache,logCache);
	addModel("Model for this demo",hmmResourceName);
	addTutorial("Part-of-Speech Tutorial",
		    "http://alias-i.com/lingpipe/demos/tutorial/posTags/read-me.html");
	addTutorial("Sentence Tutorial",
		    "http://alias-i.com/lingpipe/demos/tutorial/sentences/read-me.html");
	declareProperty(Constants.RESULT_TYPE_PARAM,
			Constants.RESULT_TYPE_VALS,
			Constants.RESULT_TYPE_TOOL_TIP);
    }

    static String DEMO_NAME = "Part-of-Speech Demo";

    static String DEMO_DESCRIPTION = "This is a part-of-speech tagging demo for text of type: ";

    public void processSentence(String sentenceText,
				SAXWriter writer,
				Properties properties,
				int sentenceNum) 
	throws SAXException {

	char[] cs = sentenceText.toCharArray();

	Tokenizer tokenizer  = mTokenizerFactory.tokenizer(cs,0,cs.length);

	List<String> tokenList = new ArrayList<String>();
	List<String> whitespaceList = new ArrayList<String>();
	tokenizer.tokenize(tokenList,whitespaceList);
	String[] tokens 
	    = tokenList.toArray(new String[0]);
	String[] whitespaces 
	    = whitespaceList.toArray(new String[0]);

	String resultType = properties.getProperty("resultType");
	if (resultType.equals(Constants.FIRST_BEST_RESULT_TYPE)) {
	    // first-best inline
            Tagging<String> tagging = mDecoder.tag(tokenList);
            List<String> tagList = tagging.tags();
            String[] tags = tagList.toArray(Strings.EMPTY_STRING_ARRAY);
	    writeInside(writer,tokens,whitespaces,tags);
	} else if (resultType.equals("nBest")) {
	    Iterator<ScoredTagging<String>> it = mDecoder.tagNBest(tokenList,Constants.MAX_N_BEST);
	    for (int i = 0; it.hasNext(); ++i) {
                ScoredTagging<String> st = it.next();
		double log2P = st.score();
                List<String> tagList = st.tags();
		String[] tags = tagList.toArray(Strings.EMPTY_STRING_ARRAY);
		write(writer,tokens,whitespaces,tags,i,log2P);
	    }
	} else if (resultType.equals(Constants.CONF_RESULT_TYPE)) {
	    TagLattice<String> lattice = mDecoder.tagMarginal(Arrays.asList(tokens));
	    for (int i = 0; i < tokens.length; ++i) {
		writer.characters(whitespaces[i]);
		writer.startSimpleElement("nBestTags");
		writer.characters(tokens[i]);
                ConditionalClassification tokenClassification
                    = lattice.tokenClassification(i);
		// List<ScoredObject<String>> tags = lattice.log2ConditionalTagList(i);
		for (int j = 0; j < Constants.MAX_CONF && j < tokenClassification.size(); ++j) {
		    double condProb = tokenClassification.score(j);
                    double log2P = com.aliasi.util.Math.log2(condProb);
		    if (j > 0 && log2P < Constants.MIN_CONF_LOG2_P) break;
		    String type = tokenClassification.category(j);
		    Attributes atts 
			= SAXWriter
			.createAttributes("token",Integer.toString(i),
					  "sentence",Integer.toString(sentenceNum),
					  "type",type,
					  "condProb",Double.toString(condProb),
					  "phrase",tokens[i],
					  "rank",Integer.toString(j));
		    writer.startSimpleElement("tag",atts);
		    writer.endSimpleElement("tag");
		}
		writer.endSimpleElement("nBestTags");
	    }
	}
    }

    void write(SAXWriter writer, 
	       String[] tokens, String[] whitespaces, String[] tags, 
	       int rank, double log2P) throws SAXException {
	writer.startSimpleElement("analysis",
				  "rank",Integer.toString(rank),
				  "jointLog2Prob",Double.toString(log2P));
	writeInside(writer,tokens,whitespaces,tags);
	writer.endSimpleElement("analysis");
    }


    void writeInside(SAXWriter writer, String[] tokens, String[] whitespaces, String[] tags) throws SAXException {
	for (int i = 0; i < tags.length; ++i) {
	    writer.characters(whitespaces[i]);
	    writer.startSimpleElement("token","pos",tags[i]);
	    writer.characters(tokens[i]);
	    writer.endSimpleElement("token");
	}
	writer.characters(whitespaces[whitespaces.length-1]);
    }

}