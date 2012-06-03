package temporal;

import java.util.ArrayList;

import org.joda.time.Interval;

import weka.classifiers.Classifier;

import com.aliasi.chunk.Chunk;
import com.aliasi.chunk.Chunker;
import com.aliasi.chunk.Chunking;
import com.aliasi.chunk.ChunkingImpl;

public class TIMEXMLAnnotator implements Chunker {
	
	private Chunker chunker;
	private Classifier regressionModel;
	
	public TIMEXMLAnnotator ( Chunker chunker, Classifier regModel ) {
    	this.chunker = chunker;
    	this.regressionModel = regModel;
    }

	@Override
	public Chunking chunk(CharSequence cSeq) {
		return chunk(cSeq.toString().toCharArray(),0,cSeq.length()); 
	}

	@Override
	public Chunking chunk(char[] cs, int start, int end) {
		String s = new String(cs,start,end);
		ArrayList<Interval> normalized;
		ChunkingImpl chunks = new ChunkingImpl(s);
 	    Chunking chunking = chunker.chunk(cs,start,end);
 	    for ( Chunk chunk : chunking.chunkSet()) {
 	    	if (chunk.type().equals("TIMEX2") && !(chunk instanceof NormalizedChunk)) {
 	    		String chunkText = s.substring(chunk.start(),chunk.end());
 	    		NormalizedChunk timex = new NormalizedChunk(chunk);
 	    		timex.setNormalized(TIMEXRuleDisambiguation.createCanonicalForm(chunkText));
 	    		normalized = timex.getNormalizedSet();
 	    		if (normalized != null){
 	    			String bestCandidate = TIMEXRegressionDisambiguation.disambiguate(chunkText, normalized, regressionModel);
 	            	timex.setNormalized(bestCandidate);
 	            	CandidateCreation.numberCandidatesTimex = 0;
 	    		}
 	    		else if (CandidateCreation.past_future_present_null_ref == 2){
 	    			timex.setNormalized("FUTURE_REF");
 	    			CandidateCreation.past_future_present_null_ref = 0;
 	    		}
 	    		else if (CandidateCreation.past_future_present_null_ref == 3){
 	    			timex.setNormalized("PRESENT_REF");
 	    			CandidateCreation.past_future_present_null_ref = 0;
 	    		}
 	    		else if (CandidateCreation.past_future_present_null_ref == 4){
 	    			timex.setNormalized("PAST_REF");
 	    			CandidateCreation.past_future_present_null_ref = 0;
 	    		}
 	    		
 	    		CandidateCreation.granularityDuration = 0;
 	    		chunks.add(timex);
 	    	} else chunks.add(chunk);
 	    }
 		return chunks;
	}

}
