package temporal;

import java.util.ArrayList;
import org.joda.time.Interval;

import com.aliasi.chunk.Chunk;

public class NormalizedChunk implements Chunk {

	private ArrayList<Interval> normalized;
	
	private String normalizedString;
	
	private int type;

	private Chunk c;

	private int start;
	
	private int end;
	
	public NormalizedChunk ( Chunk c ) {
		this.normalized = new ArrayList<Interval>();
		this.c=c;
		this.start = c.start();
		this.end = c.end();
		this.normalizedString = "";
		this.type = 0;
	}
	
	public void setEnd(int end) { this.end = end; }
	
    public void setStart(int start) { this.start = start; }

	public int end() { return end; }
	
    public int start() { return start; }
    
	public double score() { return c.score(); }
    
    public String type() { return c.type(); }
	
    public void setNormalized ( ArrayList<Interval> s ) { this.normalized = s; }
    
    public void setNormalized ( String s ) { this.normalizedString = s; }
    
    public String getNormalized ( ) { return normalizedString; }
	
	public ArrayList<Interval> getNormalizedSet ( ) { return normalized; }
	
	public int getType() { return type;	}

	public void setType(int type) {	this.type = type; }
	
	@Override
	public boolean equals ( Object that ) {
		if (that instanceof NormalizedChunk){
			if (((NormalizedChunk) that).getNormalized() == null)
				return false;
			if (this.normalizedString == null)
				return false;
			return ((NormalizedChunk) that).start() == this.start() && 
			       ((NormalizedChunk) that).end() == this.end() && 
			       normalizedString.equals(((NormalizedChunk)that).getNormalized());
		}
		else 
			return that.equals(c);
	}
	
}
