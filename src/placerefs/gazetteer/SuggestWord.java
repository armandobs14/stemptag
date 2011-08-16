package placerefs.gazetteer;

public final class SuggestWord implements Comparable<SuggestWord> {

	/**
     * the external id for this word
     */
    public String eid;
    /**
     * the score of the word
     */
    public float score;

    /**
     * the suggested word
     */
    public String string;

    public final int compareTo(SuggestWord a) {
        // first criteria: the edit distance
        if (score > a.score) {
            return 1;
        }
        if (score < a.score) {
            return -1;
        }
        
        
        return this.eid.compareTo(a.eid);
    }


    @Override
    public boolean equals(Object obj) {
        if ( this == obj ) return true;
        if ( !(obj instanceof SuggestWord) ) return false;
              
        return this.eid.equals(((SuggestWord) obj).eid) && this.score == ((SuggestWord) obj).score;
    }
    
    @Override
    public int hashCode() { // as seen in Effective Java 2nd ed.
        int result = 17;
        
        long l = Double.doubleToLongBits(this.score);
        int c = (int)(l ^ (l >>> 32));
        
        result = 31 * result + c;
        result = 31 * result + this.eid.hashCode();
        
        return result;
    }
}