package placerefs;

import com.aliasi.chunk.Chunker;
import com.aliasi.chunk.Chunking;
import com.aliasi.corpus.ObjectHandler;

public class NormalizedPlaceChunkerEvaluator implements ObjectHandler<Chunking> { 
	
	private Chunker mChunker;

    private boolean mVerbose = false;

    private final NormalizedPlaceChunkingEvaluation mChunkingEvaluation;

	public NormalizedPlaceChunkerEvaluator(Chunker chunker) {
		mChunkingEvaluation = new NormalizedPlaceChunkingEvaluation();
		mChunker = chunker;
	}
	
	
	public void handle(Chunking referenceChunking) {
        CharSequence cSeq = referenceChunking.charSequence();
        

        // first-best
        Chunking firstBestChunking  = mChunker.chunk(cSeq);
        if (firstBestChunking == null)
            firstBestChunking = new NormalizedPlaceChunking(cSeq);
        mChunkingEvaluation.addCase(referenceChunking,firstBestChunking);
        report();
	}
	
	void report() {
        if (!mVerbose) return;
        
        System.out.println(mChunkingEvaluation.mLastCase);

    }
	
	public void setVerbose(boolean isVerbose) {
        mVerbose = isVerbose;
    }
	
	
	public NormalizedPlaceChunkingEvaluation evaluation() {
        return mChunkingEvaluation;
    }

}
