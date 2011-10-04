package cintil;

import java.io.File;
import java.io.PrintStream;
import com.aliasi.chunk.BioTagChunkCodec;
import com.aliasi.chunk.CharLmHmmChunker;
import com.aliasi.chunk.Chunker;
import com.aliasi.chunk.ChunkerEvaluator;
import com.aliasi.chunk.TagChunkCodec;
import com.aliasi.corpus.Parser;
import com.aliasi.corpus.XValidatingObjectCorpus;
import com.aliasi.crf.ChainCrfChunker;
import com.aliasi.crf.ChainCrfFeatureExtractor;
import com.aliasi.hmm.HmmCharLmEstimator;
import com.aliasi.io.LogLevel;
import com.aliasi.io.Reporter;
import com.aliasi.io.Reporters;
import com.aliasi.stats.AnnealingSchedule;
import com.aliasi.stats.RegressionPrior;
import com.aliasi.tokenizer.IndoEuropeanTokenizerFactory;
import com.aliasi.tokenizer.TokenizerFactory;
import com.aliasi.util.AbstractExternalizable;

public class CINTILExperiment {

	private static String path = ".";

	public static void trainResolver(File in, File out) throws Exception {
		boolean crf = out.getAbsolutePath().endsWith(".crf");
		Parser parser = new CINTILChunkParser();
		if (crf) {
			TokenizerFactory factory = new IndoEuropeanTokenizerFactory();
			boolean enforceConsistency = true, cacheFeatures = true, addIntercept = true, uninformativeIntercept = true;
			int minFeatureCount = 1, priorBlockSize = 3, minEpochs = 10, maxEpochs = 5000;
			double priorVariance = 4.0, initialLearningRate = 0.05, learningRateDecay = 0.995, minImprovement = 0.00001;
			TagChunkCodec tagChunkCodec = new BioTagChunkCodec(factory,	enforceConsistency);
			ChainCrfFeatureExtractor<String> featureExtractor = new CRFFeatureExtractor(in.getParentFile().getAbsolutePath()+ "/pos-en-general-brown.HiddenMarkovModel", true, true);
			RegressionPrior prior = RegressionPrior.gaussian(priorVariance,	uninformativeIntercept);
			AnnealingSchedule annealingSchedule = AnnealingSchedule.exponential(initialLearningRate, learningRateDecay);
			Reporter reporter = Reporters.stdOut().setLevel(LogLevel.ERROR);
			XValidatingObjectCorpus corpus = new XValidatingObjectCorpus(0);
			parser.setHandler(corpus);
			parser.parse(in);
			ChainCrfChunker chunker = ChainCrfChunker.estimate(corpus,
					tagChunkCodec, factory, featureExtractor, addIntercept,
					minFeatureCount, cacheFeatures, prior, priorBlockSize,
					annealingSchedule, minImprovement, minEpochs, maxEpochs,
					reporter);
			AbstractExternalizable.serializeTo(chunker, out);
		} else {
			int MAX_N_GRAM = 8, NUM_CHARS = 256;
			double LM_INTERPOLATION = MAX_N_GRAM;
			TokenizerFactory factory = new IndoEuropeanTokenizerFactory();
			HmmCharLmEstimator hmmEstimator = new HmmCharLmEstimator(MAX_N_GRAM, NUM_CHARS, LM_INTERPOLATION, true);
			CharLmHmmChunker chunkerEstimator = new CharLmHmmChunker(factory, hmmEstimator);
			parser.setHandler(chunkerEstimator);
			parser.parse(in);
			AbstractExternalizable.compileTo(chunkerEstimator, out);
		}
	}

	public static void testResolver(File model, File data, PrintStream eval, PrintStream out) throws Exception {
		Chunker chunker = null;
		chunker = (Chunker) AbstractExternalizable.readObject(model);
		testResolver(chunker, data, eval, out);
	}

	public static void testResolver(Chunker chunker, File data,	PrintStream eval, PrintStream out) throws Exception {
		ChunkerEvaluator evaluator = new ChunkerEvaluator(chunker);
		evaluator.setVerbose(true);
		Parser parser = new CINTILChunkParser();
		parser.setHandler(evaluator);
		parser.parse(data);
		eval.println(evaluator.evaluation().precisionRecallEvaluation().toString());		
	}

	public static void main(String args[]) throws Exception {
		if (args.length > 0) path = args[0];
		trainResolver(new File(path+"/../cintil-train.xml"),new File(path+"/../cintil.model.crf"));
		String main[] = { path+"/../timex-train.xml" };
		testResolver(new File(path+"/../../cintil.model.crf"), new File(path+"/../cintil-train.xml"), System.out, System.out); 
	}

}