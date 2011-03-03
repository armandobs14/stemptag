package temporal;

import com.aliasi.chunk.Chunk;

public class NormalizedChunk implements Chunk {

	private String normalized;
	
	private Chunk c;

	private int start;
	
	private int end;
	
	public NormalizedChunk ( Chunk c ) {
		this.normalized = "";
		this.c=c;
		this.start = c.start();
		this.end = c.end();
	}

	public NormalizedChunk ( Chunk c, String s ) {
		this.normalized = s;
		this.c=c;
		this.start = c.start();
		this.end = c.end();
	}
	
	public void setEnd(int end) { this.end = end; }
	
    public void setStart(int start) { this.start = start; }

	public int end() { return end; }
	
    public int start() { return start; }
    
	public double score() { return c.score(); }
    
    public String type() { return c.type(); }
	
    public void setNormalized ( String s ) { 
    	this.normalized = s;
    }
	
	public String getNormalized ( ) { return normalized; }
	
	public boolean equals ( Chunk chunk ) {
		if (chunk instanceof NormalizedChunk) return chunk.equals(c) && normalized.equals(((NormalizedChunk)chunk).getNormalized());
		else return chunk.equals(c);
	}
	
}
