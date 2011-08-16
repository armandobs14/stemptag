package placerefs.gazetteer;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.apache.lucene.analysis.KeywordAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;

public class CandidateGenerator {

    private Directory luceneSpellDirectory;
    
    private SpellChecker spellChecker;

    private Directory luceneCompleteDirectory;
    
    private IndexSearcher completeSearcher;
    
    private QueryParser completeQueryParser;

    public CandidateGenerator() {
        try {
            luceneSpellDirectory = FSDirectory.open(new File(Configurator.LUCENE_KB_SPELLCHECK));
            spellChecker = new SpellChecker(luceneSpellDirectory, "name", "eid");
            luceneCompleteDirectory = FSDirectory.open(new File(Configurator.LUCENE_KB_COMPLETE));
            completeSearcher = new IndexSearcher(luceneCompleteDirectory, true);
            completeQueryParser = new QueryParser(Version.LUCENE_29, "eid", new KeywordAnalyzer());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public List<KbEntity> getCandidates(String toponym) throws Exception {

        
        List<SuggestWord> candidates = spellChecker.suggestSimilar(QueryParser.escape(toponym), Configurator.MAX_NAME_SUGGESTIONS);
        
        Set<String> antiDuplicates = new HashSet<String>();
        List<KbEntity> result = new ArrayList<KbEntity>();
        for (SuggestWord candidate : candidates) {

            if (antiDuplicates.contains(candidate.eid)) {
                continue;
            } else {
                antiDuplicates.add(candidate.eid);
            }
            
            Query query = completeQueryParser.parse(candidate.eid);                         
            ScoreDoc[] hits = completeSearcher.search(query, 1).scoreDocs;
            Document d = completeSearcher.doc(hits[0].doc);

            KbEntity c = new KbEntity();
            c.id = d.get("eid");
            c.name = d.get("name");
            c.wiki_title = d.get("wiki_title");
            c.wiki_text = d.get("text");
            c.altNames = d.getValues("altname");
            c.coordinates = d.get("coord");
            c.area = d.get("area");
            c.population = d.get("pop");
            
            result.add(c);
        }
        
        return result;
    }


    public void close() {
        try {            
            spellChecker.close();            
            completeSearcher.close();
            luceneCompleteDirectory.close();
        } catch (IOException e) {
            e.printStackTrace();
        }        
    }
}
