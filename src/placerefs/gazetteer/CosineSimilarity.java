package placerefs.gazetteer;

import java.io.File;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.tokenattributes.TermAttribute;
import org.apache.lucene.index.GAEIndexReaderPool;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;

public final class CosineSimilarity {

    private IndexReader reader;
    
    private Integer numDocs;
    
    public static final CosineSimilarity INSTANCE = new CosineSimilarity();

    private CosineSimilarity() {
        try {
        	if ( Configurator.APP_ENGINE ) {
        		GAEIndexReaderPool readerPool = GAEIndexReaderPool.getInstance();
        		reader = readerPool.borrowReader(Configurator.LUCENE_KB_COMPLETE);
        	} else {
        		reader = IndexReader.open(FSDirectory.open(new File(Configurator.LUCENE_KB_COMPLETE)), true);
        	}
            numDocs = reader.maxDoc();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private Double idf(String t) {
        try {
            Integer df = reader.docFreq(new Term("text", t));
            return 1+Math.log(numDocs/(df+1));
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return 0.0;
    }
     
    public Double getSimilarity(String q, String d) {
        Double sim = 0.0;
        Map<String, Integer> wordsQ = tokenizeDocument(q, new StandardAnalyzer(Version.LUCENE_29));
        Map<String, Integer> wordsD = tokenizeDocument(d, new StandardAnalyzer(Version.LUCENE_29));
        Double normQ = 0.0;
        Double normD = 0.0;
        for (Map.Entry<String, Integer> e : wordsQ.entrySet()) {
            String t = e.getKey();
            Integer tfd = wordsD.get(t);
            Double idf = idf(t);
            if (tfd != null) {
                sim += e.getValue()*tfd*Math.pow(idf,2);
                normD += Math.pow(tfd*idf,2);
            }
            normQ += Math.pow(e.getValue()*idf, 2);
        }
        for (Map.Entry<String, Integer> e : wordsD.entrySet()) {            
            if (!wordsQ.containsKey(e.getKey())) {
                normD += Math.pow(e.getValue()*idf(e.getKey()),2);
            }            
        }
        sim = sim / (Math.sqrt(normQ)*Math.sqrt(normD));
        return sim;
    }
    
    private static Map<String, Integer> tokenizeDocument(String documentContent, Analyzer a){
        Map<String, Integer> documentTokenization = new HashMap<String, Integer>();
        TokenStream ts = null;
        ts = a.tokenStream("", new StringReader(documentContent));
        TermAttribute termAtt = (TermAttribute)(ts.addAttribute(TermAttribute.class));
        try {
            ts.reset();
            while (ts.incrementToken()) {
                Integer count = documentTokenization.get(termAtt.term());
                if (count == null) {
                    count = 1;
                } else {
                    count++;
                }
                documentTokenization.put(termAtt.term(), count);
            }
            ts.end();
            ts.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return documentTokenization;
    }

}