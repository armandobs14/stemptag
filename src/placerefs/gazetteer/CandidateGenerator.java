package placerefs.gazetteer;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


import org.apache.lucene.analysis.KeywordAnalyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
//import org.apache.lucene.index.GAEIndexReader;
//import org.apache.lucene.index.GAEIndexReaderPool;
//import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.queryParser.*;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.spell.SpellChecker;
import org.apache.lucene.search.spell.SuggestWord;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.SimpleFSDirectory;
import org.apache.lucene.util.Version;

public class CandidateGenerator {

    private SpellChecker spellChecker;

    private IndexSearcher spellSearcher;
    
    private IndexSearcher completeSearcher;
    
    private QueryParser completeQueryParser;

    public CandidateGenerator() {
        try {
        	if ( Configurator.APP_ENGINE ) {
        		/*GAEIndexReaderPool readerPool = GAEIndexReaderPool.getInstance();
        		GAEIndexReader indexReaderComplete = readerPool.borrowReader(Configurator.LUCENE_KB_COMPLETE);
        		completeSearcher = new IndexSearcher(indexReaderComplete);
        		GAEIndexReaderPool readerPool2 = GAEIndexReaderPool.getInstance();
        		GAEIndexReader indexReaderSpell = readerPool.borrowReader(Configurator.LUCENE_KB_SPELLCHECK);
        		spellSearcher = new IndexSearcher(indexReaderSpell);*/
        	} else {
        		spellSearcher = new IndexSearcher(FSDirectory.open(new File(Configurator.LUCENE_KB_SPELLCHECK)), true);
        		
        		//Indice Velho
//        		completeSearcher = new IndexSearcher(FSDirectory.open(new File(Configurator.LUCENE_KB_COMPLETE)), true);
        		
        		//Novo Indice
        		completeSearcher = new IndexSearcher(SimpleFSDirectory.open(new File(Configurator.LUCENE_KB_COMPLETE)), true);

        	}
        	//Novo Indice
    		spellChecker = new SpellChecker(FSDirectory.open(new File(Configurator.LUCENE_KB_SPELLCHECK)), "name", "eid", false);
    		completeQueryParser = new QueryParser(Version.LUCENE_35, "eid", new KeywordAnalyzer());
    		
        	//Indice Velho
//    		spellChecker = new SpellChecker(spellSearcher, "name", "eid");
//    		completeQueryParser = new QueryParser(Version.LUCENE_30, "eid", new KeywordAnalyzer());

    		
    		
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public List<GazetteerEntry> getCandidates(String toponym) throws Exception {
    	System.out.println("toponym candidate generation: " + toponym);
    	
    	//Novo Indice    	
		Set<Integer> candidates = spellChecker.suggestSimilar(QueryParser.escape(toponym), Configurator.MAX_NAME_SUGGESTIONS);
		
//		System.out.println("candidates n: " + candidates.size());
//        for(int i = 1; i < candidates.size(); i++){
//    		System.out.println("candidate " + i + ": " + candidates.get(i-0).eid);
//        }
		
    	//Indice Velho
//    	List<SuggestWord> candidates = spellChecker.suggestSimilar(QueryParser.escape(toponym), Configurator.MAX_NAME_SUGGESTIONS);
		
        Set<Integer> antiDuplicates = new HashSet<Integer>();
        List<GazetteerEntry> result = new ArrayList<GazetteerEntry>();
        
        //Indice Velho
//        for (SuggestWord candidate : candidates) {
        //Indice Novo
        for (Integer candidate : candidates) {
            if (antiDuplicates.contains(candidate)) {
                continue;
            } else {
            	//Indice Novo
                antiDuplicates.add(candidate);
                //Indice Velho
//                antiDuplicates.add(Integer.getInteger(candidate.eid));
            }
            
            //System.out.println("CEID: " + candidate.eid + " Name:" + candidate.string);
            
            Query query = completeQueryParser.parse(candidate.toString());
            
//            Document d = completeSearcher.getIndexReader().document(new Integer(candidate.eid));
            
            ScoreDoc[] hits = completeSearcher.search(query, 1).scoreDocs;
            Document d = completeSearcher.doc(hits[0].doc);
            GazetteerEntry c = new GazetteerEntry();
            
            
            
            c.id = d.get("eid");
            c.name = d.get("name");
            /*c.wiki_title = d.get("wiki_title");
            
            System.out.println(c.wiki_title);*/
            
            
            c.wiki_text = d.get("text");
            
            c.altNames = new String[]{};

//            d.getValues("altname");
            c.coordinates = d.get("coord");
            
            c.area = null;
            c.population = null;
//            c.area = d.get("area");
            
//            c.population = d.get("pop");
            
            result.add(c);
        }
        return result;
    }

    public void close() {
        try {            
            spellChecker.close();
            completeSearcher.getIndexReader().close();
            completeSearcher.close();
        } catch (IOException e) {
            e.printStackTrace();
        }        
    }

}
