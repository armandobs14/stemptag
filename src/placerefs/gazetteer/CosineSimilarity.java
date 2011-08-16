package placerefs.gazetteer;

import java.io.File;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.tokenattributes.TermAttribute;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;


/**
 * Implements the vector space model for computing the similarity between two documents.
 * It assumes the existence of a document collection indexed by Lucene, from where it gets the 
 * document frequency for each term. 
 */
public final class CosineSimilarity {

    private IndexReader reader;
    private Integer numDocs;
    
    /** Singleton. **/
    public static final CosineSimilarity INSTANCE = new CosineSimilarity();
    private CosineSimilarity() {
        try {
            reader = IndexReader.open(
                    FSDirectory.open(new File(Configurator.LUCENE_KB_COMPLETE)), true);

            numDocs = reader.maxDoc();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /**
     * Inverse Document Frequency, as computed in Lucene.
     * 
     * @param t The term.
     * @return The idf score.
     */
    private Double idf(String t) {
        try {
            Integer df = reader.docFreq(new Term("text", t));
            return 1+Math.log(numDocs/(df+1));
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return 0.0;
    }
     

    /**
     * 
     * Get the vector space model score, according to the cosine similarity and tf-idf weights.
     * Texts are pre-processed with Lucene's StandardAnalyzer.
     * 
     * @param q Documtent's text content.
     * @param d Documtent's text content.
     * @return The similarity value (>= 0.0).
     */
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
    
    /**
     * Lucene text tokenizer.
     * 
     * @param documentContent A string corresponding to a document's text content.
     * @param a The Lucene analyzer to use.
     * @return Returns all the words considered and processed by the analyzer, 
     * as well as a count for the number of occurrences of that word in the string.
     */
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
