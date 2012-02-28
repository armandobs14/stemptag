package placerefs;

import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import com.aliasi.chunk.Chunk;
import com.aliasi.chunk.ChunkAndCharSeq;
import com.aliasi.chunk.Chunking;
import com.aliasi.chunk.ChunkingEvaluation;
import com.aliasi.chunk.ChunkingImpl;
import com.aliasi.classify.PrecisionRecallEvaluation;
import com.aliasi.util.Strings;

public class NormalizedPlaceChunkingEvaluation extends ChunkingEvaluation{
	
	private final Set<Chunking[]> mCases = new HashSet<Chunking[]>();

    private final Set<ChunkAndCharSeq> mTruePositiveSet
        = new HashSet<ChunkAndCharSeq>();
    private final Set<ChunkAndCharSeq> mFalsePositiveSet
        = new HashSet<ChunkAndCharSeq>();
    private final Set<ChunkAndCharSeq> mFalseNegativeSet
        = new HashSet<ChunkAndCharSeq>();
	
	String mLastCase = null;
	
	public NormalizedPlaceChunkingEvaluation() {
        /* do nothing */
    }
	
	 public Set<Chunking[]> cases() {
	        return Collections.<Chunking[]>unmodifiableSet(mCases);
	    }
	
	public NormalizedPlaceChunkingEvaluation perTypeEvaluation(String chunkType) {
		NormalizedPlaceChunkingEvaluation evaluation = new NormalizedPlaceChunkingEvaluation();
        for (Chunking[] testCase : cases()) {
            Chunking referenceChunking = testCase[0];
            Chunking responseChunking = testCase[1];
            Chunking referenceChunkingRestricted
                = restrictTo(referenceChunking,chunkType);
            Chunking responseChunkingRestricted
                = restrictTo(responseChunking,chunkType);
            evaluation.addCase(referenceChunkingRestricted,
                               responseChunkingRestricted);
        }
        return evaluation;
    }

    static Chunking restrictTo(Chunking chunking, String type) {
        CharSequence cs = chunking.charSequence();
        ChunkingImpl chunkingOut = new ChunkingImpl(cs);
        for (Chunk chunk : chunking.chunkSet())
            if (chunk.type().equals(type))
                chunkingOut.add(chunk);
        return chunkingOut;
    }
	
	static String formatChunks(Chunking chunking) {
        StringBuilder sb = new StringBuilder();
        int pos = 0;
        for (Chunk chunk : chunking.chunkSet()) {
            int start = chunk.start();
            int padLength = start-pos;
            for (int j = 0; j < padLength; ++j)
                sb.append(" ");
            int end = chunk.end();
            int chunkLength = end-start;
            char marker = chunk.type().length() > 0
                ? chunk.type().charAt(0)
                : '!';
            if (chunkLength > 0) sb.append(marker);
            for (int j = 1; j < chunkLength; ++j)
                sb.append(".");
            pos = end;
        }
        sb.append("\n");
        return sb.toString();
    }
	
	static String formatHeader(int indent, Chunking chunking) {
        String cs = chunking.charSequence().toString();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < indent; ++i) sb.append(" ");
        sb.append("CHUNKS= ");
        for (Chunk chunk : chunking.chunkSet()) {
            sb.append("(" + chunk.start()
                      + "," + chunk.end()
                      + "):" + chunk.type() + "   ");
        }

        if (sb.charAt(sb.length()-1) != '\n') sb.append("\n");
        for (int i = 0; i < indent; ++i) sb.append(" ");
        sb.append(cs);
        sb.append("\n");
        int length = cs.length();
        printMods(1,length, sb,indent);
        printMods(10,length, sb,indent);
        printMods(100,length, sb,indent);
        if (sb.charAt(sb.length()-1) != '\n') sb.append("\n");
        return sb.toString();
    }

    static void printMods(int base, int length, StringBuilder sb, int indent) {
        if (length <= base) return;
        for (int i = 0; i < indent; ++i) sb.append(" ");
        for (int i = 0; i < length; ++i) {
            if (base == 1 || (i >= base && i % 10 == 0))
                sb.append(Integer.toString((i/base)%10));
            else
                sb.append(" ");
        }
        sb.append("\n");
    }
	
	public void addCase(Chunking referenceChunking,
	            		Chunking responseChunking) {
		StringBuilder sb = new StringBuilder();

		CharSequence cSeq = referenceChunking.charSequence();
		if (!Strings.equalCharSequence(cSeq,
		                           responseChunking.charSequence())) {
		String msg = "Char sequences must be same."
		    + " Reference char seq=" + cSeq
		    + " Response char seq=" + responseChunking.charSequence();
		throw new IllegalArgumentException(msg);
		}
		sb.append("\n");
		sb.append(formatHeader(5,referenceChunking)); // 5 is indent for " REF " and "RESP "
		sb.append("\n REF ");
		sb.append(formatChunks(referenceChunking));
		sb.append("RESP ");
		sb.append(formatChunks(responseChunking));
		sb.append("\n");
		mLastCase = sb.toString();
		
		mCases.add(new Chunking[] { referenceChunking, responseChunking });
		// need mutable sets, so wrap
		Set<NormalizedPLACEChunk> refSet = unscoredNormalizedChunkSet(referenceChunking);
		Set<NormalizedPLACEChunk> respSet = unscoredNormalizedChunkSet(responseChunking);

		for (NormalizedPLACEChunk respChunk : respSet) {
		
			Iterator<NormalizedPLACEChunk> it = refSet.iterator();
			ChunkAndCharSeq ccs = new ChunkAndCharSeq(respChunk,cSeq);
			boolean flag = false;
			while(it.hasNext()){
				NormalizedPLACEChunk nChunk = it.next();
				if (nChunk.equals(respChunk)){
					mTruePositiveSet.add(ccs);
					it.remove();
					flag = true;
					break;
				}	
			}
			if (!flag)
				mFalsePositiveSet.add(ccs);
		}

		for (NormalizedPLACEChunk refChunk : refSet) {
			mFalseNegativeSet.add(new ChunkAndCharSeq(refChunk,cSeq));
		}
	}

	static Set<NormalizedPLACEChunk> unscoredNormalizedChunkSet(Chunking chunking) {
        Set<NormalizedPLACEChunk> result = new HashSet<NormalizedPLACEChunk>();
        for (Chunk chunk : chunking.chunkSet())
            result.add((NormalizedPLACEChunk)chunk);
        return result;
    }
	
	public Set<ChunkAndCharSeq> truePositiveSet() {
        return Collections.<ChunkAndCharSeq>unmodifiableSet(mTruePositiveSet);
    }
	
	public Set<ChunkAndCharSeq> falsePositiveSet() {
        return Collections.<ChunkAndCharSeq>unmodifiableSet(mFalsePositiveSet);
    }
	
	public Set<ChunkAndCharSeq> falseNegativeSet() {
        return Collections.<ChunkAndCharSeq>unmodifiableSet(mFalseNegativeSet);
    }
	
	public PrecisionRecallEvaluation precisionRecallEvaluation() {
        int tp = truePositiveSet().size();
        int fn = falseNegativeSet().size();
        int fp = falsePositiveSet().size();
        return new PrecisionRecallEvaluation(tp,fn,fp,0);
    }

    @Override
    public String toString() {
        return precisionRecallEvaluation().toString();
    }
}
