package com.aliasi.crf;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import com.aliasi.chunk.BioTagChunkCodec;
import com.aliasi.chunk.ChunkerEvaluator;
import com.aliasi.chunk.TagChunkCodec;
import com.aliasi.corpus.Parser;
import com.aliasi.corpus.XValidatingObjectCorpus;
import com.aliasi.io.LogLevel;
import com.aliasi.io.Reporter;
import com.aliasi.io.Reporters;
import com.aliasi.stats.AnnealingSchedule;
import com.aliasi.stats.RegressionPrior;
import com.aliasi.tokenizer.IndoEuropeanTokenCategorizer;
import com.aliasi.tokenizer.IndoEuropeanTokenizerFactory;
import com.aliasi.tokenizer.TokenizerFactory;
import com.aliasi.util.ObjectToDoubleMap;
import temporal.TIMEXChunkParser;

public class TestHighOrderCrf {
	
	public static void main ( String args[] ) throws Exception {
		Parser parser = new TIMEXChunkParser();
		TokenizerFactory factory = new IndoEuropeanTokenizerFactory();
		boolean enforceConsistency = true, cacheFeatures = true, addIntercept = true, uninformativeIntercept = true;
		int minFeatureCount = 1, priorBlockSize = 3, minEpochs = 10, maxEpochs = 5000;
		double priorVariance = 4.0, initialLearningRate = 0.05, learningRateDecay = 0.995, minImprovement = 0.00001;
		TagChunkCodec tagChunkCodec = new BioTagChunkCodec(factory,enforceConsistency);
		HighOrderCrfFeatureExtractor<String> featureExtractor = new TestHighOrderCRFFeatureExtractor();
		RegressionPrior prior = RegressionPrior.gaussian( priorVariance, uninformativeIntercept );
		AnnealingSchedule annealingSchedule = AnnealingSchedule.exponential(initialLearningRate, learningRateDecay);
        Reporter reporter = Reporters.stdOut().setLevel(LogLevel.ERROR);
        XValidatingObjectCorpus corpus = new XValidatingObjectCorpus(0);
		parser.setHandler(corpus);
	    parser.parse("aux");
		HighOrderCrfChunker chunker = HighOrderCrfChunker.estimate(corpus, tagChunkCodec, factory, featureExtractor, addIntercept, minFeatureCount, cacheFeatures, prior, priorBlockSize, annealingSchedule, minImprovement, minEpochs, maxEpochs, reporter);
		ChunkerEvaluator evaluator = new ChunkerEvaluator(chunker);
		evaluator.setVerbose(true);
		parser = new TIMEXChunkParser();
		parser.setHandler(evaluator);
        parser.parse("aux");
        evaluator.evaluation().perTypeEvaluation("TIMEX2").precisionRecallEvaluation().toString();
	}

}

class TestHighOrderCRFFeatureExtractor implements com.aliasi.crf.HighOrderCrfFeatureExtractor<String>, Serializable {
    
	static final long serialVersionUID = 123L;
	
    public TestHighOrderCRFFeatureExtractor() throws ClassNotFoundException, IOException { }
    	
    public HighOrderCrfFeatures<String> extract(List<String> tokens, List<String> tags) { return new HighOrderChunkerFeatures(tokens,tags); }
    
    Object writeReplace() { return this; }
    
    class HighOrderChunkerFeatures extends HighOrderCrfFeatures<String> {
                
        public HighOrderChunkerFeatures(List<String> tokens, List<String> tags) {
            super(tokens,tags);
        }
        
        public Map<String,? extends Number> nodeFeatures(int n) {
            ObjectToDoubleMap<String> feats = new ObjectToDoubleMap<String>();
            boolean bos = n == 0;
            boolean eos = (n + 1) >= numTokens();
            String tokenCat = tokenCat(n);
            String prevTokenCat = bos ? null : tokenCat(n-1);
            String nextTokenCat = eos ? null : tokenCat(n+1);
            String token = normedToken(n);
            String prevToken = bos ? null : normedToken(n-1);
            String nextToken = eos ? null : normedToken(n+1);
            if (bos) feats.set("BOS",1.0);
            if (eos) feats.set("EOS",1.0);
            if (!bos && !eos) feats.set("!BOS!EOS",1.0);
            feats.set("TOK_" + token, 1.0);
            if (!bos) feats.set("TOK_PREV_" + prevToken,1.0);
            if (!eos) feats.set("TOK_NEXT_" + nextToken,1.0);
            feats.set("TOK_CAT_" + tokenCat, 1.0);
            if (!bos) feats.set("TOK_CAT_PREV_" + prevTokenCat, 1.0);
            if (!eos) feats.set("TOK_CAT_NEXT_" + nextTokenCat, 1.0);
			for (String suffix : suffixes(token)) feats.set("SUFF_" + suffix,1.0);
            if (!bos) for (String suffix : suffixes(prevToken)) feats.set("SUFF_PREV_" + suffix,1.0);
            if (!eos) for (String suffix : suffixes(nextToken)) feats.set("SUFF_NEXT_" + suffix,1.0);
            for (String prefix : prefixes(token)) feats.set("PREF_" + prefix,1.0);
            if (!bos) for (String prefix : prefixes(prevToken)) feats.set("PREF_PREV_" + prefix,1.0);
            if (!eos) for (String prefix : prefixes(nextToken)) feats.set("PREF_NEXT_" + prefix,1.0);
            return feats;
        }
        
        public Map<String,? extends Number> edgeFeatures(int n, int k[] ) {
            ObjectToDoubleMap<String> feats = new ObjectToDoubleMap<String>();
            feats.set("PREV_TAG_" + tag(k[0]), 1.0);
            feats.set("PREV_TAG_TOKEN_CAT_"  + tag(k[0]) + "_" + tokenCat(n-1), 1.0);
            return feats;
        }
        
        public String normedToken(int n) { return token(n).replaceAll("\\d+","*$0*").replaceAll("\\d","D"); }
        
        public String tokenCat(int n) { return IndoEuropeanTokenCategorizer.CATEGORIZER.categorize(token(n)); }
    }
    
    int MAX_PREFIX_LENGTH = 4;
    
    List<String> prefixes(String s) {
        int numPrefixes = java.lang.Math.min(MAX_PREFIX_LENGTH,s.length());
        if (numPrefixes == 0) return java.util.Collections.emptyList();
        if (numPrefixes == 1) return java.util.Collections.singletonList(s);
        List<String> result = new ArrayList<String>(numPrefixes);
        for (int i = 1; i <= java.lang.Math.min(MAX_PREFIX_LENGTH,s.length()); ++i) result.add(s.substring(0,i));
        return result;
    }
    
    int MAX_SUFFIX_LENGTH = 4;
    
    List<String> suffixes(String s) {
        int numSuffixes = java.lang.Math.min(s.length(), MAX_SUFFIX_LENGTH);
        if (numSuffixes <= 0) return java.util.Collections.emptyList();
        if (numSuffixes == 1) return java.util.Collections.singletonList(s);
        List<String> result = new ArrayList<String>(numSuffixes);
        for (int i = s.length() - numSuffixes; i < s.length(); ++i) result.add(s.substring(i));
        return result;
    }
        
}