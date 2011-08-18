package placerefs.gazetteer;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.spell.Dictionary;
import java.util.Iterator;

public class LuceneDictionary implements Dictionary {

  private IndexReader reader;

  public LuceneDictionary(IndexReader reader) {
    this.reader = reader;
  }

  public final Iterator<Document> getWordsIterator() {
    return new LuceneIterator();
  }

  final class LuceneIterator implements Iterator<Document> {

	private boolean hasNextCalled = false;
    
	private int i = -1;
    
	private int max;

    LuceneIterator() {
        max = reader.maxDoc();
        System.out.println("Found " + max + " indexed documents");
    }

    public Document next() {        
        if (hasNext()) {
            hasNextCalled = false;
            try {
                return reader.document(i);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } 
        return null;
    }

    public boolean hasNext() {
        if (!hasNextCalled) {
            hasNextCalled = true;
            do {
                i++;
            } while(i < max && reader.isDeleted(i));
        }
        return i < max;
    }

    public void remove() {
     throw new UnsupportedOperationException();
    }
  }
  
}