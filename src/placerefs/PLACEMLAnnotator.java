package placerefs;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import placerefs.gazetteer.CandidateGenerator;
import placerefs.gazetteer.GazetteerEntry;
import temporal.CandidateCreation;
import weka.classifiers.Classifier;
import com.aliasi.chunk.Chunk;
import com.aliasi.chunk.Chunker;
import com.aliasi.chunk.Chunking;
import com.aliasi.chunk.ChunkingImpl;
import com.vividsolutions.jts.geom.Point;

public class PLACEMLAnnotator implements Chunker{
	
	private Chunker chunker;  
	private Classifier regressionModel; 
	
	public PLACEMLAnnotator ( Chunker chunker, Classifier regModel ) {
    	this.chunker = chunker;
    	this.regressionModel = regModel;
    }

	@Override
	public Chunking chunk(CharSequence cSeq) {
		return chunk(cSeq.toString().toCharArray(),0,cSeq.length()); 
	}

	@Override
	public Chunking chunk(char[] cs, int start, int end) {
		System.out.println("ENTREI NO METODO CHUNK!!");
		
		String s = new String(cs,start,end);
		List<GazetteerEntry> candidatos = new ArrayList<GazetteerEntry>();
		GazetteerEntry bestCandidate;
		ChunkingImpl chunks = new ChunkingImpl(s);
 	    Chunking chunking = chunker.chunk(cs,start,end);
 	    for ( Chunk chunk : chunking.chunkSet()) {
 	    	
 	    	System.out.println("CHUNK TYPE: " + chunk.type());
 	    	System.out.println("IS CHUNK NOT A NORMALIZEDPLACE? : " + !(chunk instanceof NormalizedPLACEChunk));
 	    	
 	    	if (chunk.type().equals(placerefs.Configurator.chunkTypeForEvaluation) && !(chunk instanceof NormalizedPLACEChunk)) {
 	    		try {
	 	    		String chunkText = s.substring(chunk.start(),chunk.end());
	 	    		System.out.println("PLACE: "+chunkText);
	 	    		NormalizedPLACEChunk place = new NormalizedPLACEChunk(chunk);
	 	    		//Get candidates
	 	    		CandidateGenerator cg = new CandidateGenerator();               
					candidatos = cg.getCandidates(chunkText);
					//Compute best candidate
					System.out.println("ANTES DO DISAMBIGUATE");
					bestCandidate = PLACERegressionDisambiguation.disambiguate(chunkText, s, candidatos, regressionModel);
					System.out.println("DEPOIS DO DISAMBIGUATE");
					
					place.setLatitude(Double.parseDouble(bestCandidate.coordinates.split(" ")[0].replaceAll("N|E|S|W", "")));
					place.setLongitude(Double.parseDouble(bestCandidate.coordinates.split(" ")[1].replaceAll("N|E|S|W", "")));
					
					chunks.add(place);
					cg.close();
					
 	    		} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
 	    	} 
 	    	else chunks.add(chunk);
 	    }
 		return chunks;
	}

}
