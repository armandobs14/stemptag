package temporal;

import com.aliasi.chunk.Chunker;
import com.aliasi.chunk.ChunkerEvaluator;
import com.aliasi.chunk.Chunking;
import com.aliasi.chunk.ChunkingEvaluation;
import com.aliasi.chunk.ChunkingImpl;

public class NormalizedChunkerEvaluator extends ChunkerEvaluator {
	
	private Chunker mChunker;

    private boolean mVerbose = false;

    private final NormalizedChunkingEvaluation mChunkingEvaluation;

	public NormalizedChunkerEvaluator(Chunker chunker) {
		super(chunker);
		mChunkingEvaluation = new NormalizedChunkingEvaluation();
		mChunker = chunker;
	}
	
	
	public void handle(Chunking referenceChunking) {
        CharSequence cSeq = referenceChunking.charSequence();


        // first-best
        Chunking firstBestChunking  = mChunker.chunk(cSeq);
        if (firstBestChunking == null)
            firstBestChunking = new ChunkingImpl(cSeq);
        mChunkingEvaluation.addCase(referenceChunking,firstBestChunking);
        report();
	}
	
	void report() {
        if (!mVerbose) return;
        
        System.out.println(mChunkingEvaluation.mLastCase);

    }
	
	public ChunkingEvaluation evaluation() {
        return mChunkingEvaluation;
    }

}
