package placerefs.gazetteer;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.spell.Dictionary;
import java.util.Iterator;

/**
 * Lucene Dictionary: terms taken from the given field of a Lucene index.
 *
 * When using IndexReader.terms(Term) the code must not call next() on TermEnum
 * as the first call to TermEnum, see: http://issues.apache.org/jira/browse/LUCENE-6
 */
public class LuceneDictionary implements Dictionary {

  private IndexReader reader;

  public LuceneDictionary(IndexReader reader) {
    this.reader = reader;
  }

  public final Iterator<Document> getWordsIterator() {
    return new LuceneIterator();
  }


  /**
   * 
   * Returns an iterator for all documents in a lucene index.
   * 
   * @author ivo
   *
   */
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