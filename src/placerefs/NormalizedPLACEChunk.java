package placerefs;

import java.util.ArrayList;

import org.joda.time.Interval;

import com.aliasi.chunk.Chunk;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.PrecisionModel;

public class NormalizedPLACEChunk implements Chunk {
	
	private String type;
	
	private String placeType;

	private Chunk c;

	private int start;
	
	private int end;
	
	private Double latitude;
	
	private Double longitude;
	
	public NormalizedPLACEChunk ( Chunk c ) {
		this.c=c;
		this.start = c.start();
		this.end = c.end();
		this.type = "";
	}


	public void setPlaceType(String placeType) { this.placeType = placeType; }
	
	public void setEnd(int end) { this.end = end; }
	
    public void setStart(int start) { this.start = start; }
    
    public void setType(String type) {	this.type = type; }
    
	public void setLongitude(double longitude) { this.longitude = longitude;	}
	
	public void setLatitude(double latitude) { this.latitude = latitude;	}

	public int end() { return end; }
	
    public int start() { return start; }
    
	public double score() { return c.score(); }
    
    public String type() { return c.type(); }
	       
    public double getLatitude() { return latitude; }

	public double getLongitude() { return longitude; }
	
	public String getType() { return type;	}
	
	public String getPlaceType() { return placeType; }
	
	@SuppressWarnings("deprecation")
	@Override
	public boolean equals ( Object that ) {
		if (that instanceof NormalizedPLACEChunk){
			if (this.latitude != null && this.longitude != null){
				Point p1 = new Point(new Coordinate(longitude,latitude), new PrecisionModel(),4326);
				Point p2 = new Point(new Coordinate(((NormalizedPLACEChunk)that).getLongitude(),((NormalizedPLACEChunk)that).getLatitude()), new PrecisionModel(),4326);
				if (this.placeType != null && (this.placeType.toLowerCase().equals("civil") || 
											   this.placeType.toLowerCase().equals("continent") || 
											   this.placeType.toLowerCase().equals("country") ||
											   this.placeType.toLowerCase().equals("state"))){
					return ((p1.distance(p2) < 5) && ((NormalizedPLACEChunk) that).start() == this.start() && ((NormalizedPLACEChunk) that).end() == this.end());
				}
				else
					return ((p1.distance(p2) < 1) && ((NormalizedPLACEChunk) that).start() == this.start() && ((NormalizedPLACEChunk) that).end() == this.end());
			}
			else
				return false;
		}
		else 
			return that.equals(c);
	}
	
}
