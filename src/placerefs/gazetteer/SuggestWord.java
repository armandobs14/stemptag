package placerefs.gazetteer;

public final class SuggestWord implements Comparable<SuggestWord> {

    public String eid;

    public float score;

    public String string;

    public final int compareTo(SuggestWord a) {
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
    public int hashCode() {
        int result = 17;        
        long l = Double.doubleToLongBits(this.score);
        int c = (int)(l ^ (l >>> 32));        
        result = 31 * result + c;
        result = 31 * result + this.eid.hashCode();
        return result;
    }

}