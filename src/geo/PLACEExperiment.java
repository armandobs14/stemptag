package geo;

import geo.PLACEChunkParser;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Locale;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;

import org.joda.time.DateTime;
import org.joda.time.Interval;
import org.joda.time.Period;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import temporal.rules.TIMEXRuleAnnotator;
import temporal.CRFFeatureExtractor;
import temporal.TIMEXMLAnnotator;
import com.aliasi.chunk.BioTagChunkCodec;
import com.aliasi.chunk.CharLmHmmChunker;
import com.aliasi.chunk.Chunk;
import com.aliasi.chunk.ChunkAndCharSeq;
import com.aliasi.chunk.ChunkFactory;
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
import com.aliasi.xml.SimpleElementHandler;

import weka.classifiers.Classifier;
import weka.core.Instance;
import weka.classifiers.functions.SVMreg;

import dmir.gis.CosineSimilarity;
import dmir.gis.Main;

public class PLACEExperiment {

	private static String path = ".";

	public static void trainResolver(File in, File out) throws Exception {
		boolean crf = out.getAbsolutePath().endsWith(".crf");
		Parser parser = new PLACEChunkParser();
		if (crf) {
			TokenizerFactory factory = new IndoEuropeanTokenizerFactory();
			boolean enforceConsistency = true, cacheFeatures = true, addIntercept = true, uninformativeIntercept = true;
			int minFeatureCount = 1, priorBlockSize = 3, minEpochs = 10, maxEpochs = 5000;
			double priorVariance = 4.0, initialLearningRate = 0.05, learningRateDecay = 0.995, minImprovement = 0.00001;
			TagChunkCodec tagChunkCodec = new BioTagChunkCodec(factory,	enforceConsistency);
			ChainCrfFeatureExtractor<String> featureExtractor = new CRFFeatureExtractor(in.getParentFile().getAbsolutePath()+ "/pos-en-general-brown.HiddenMarkovModel", false, false);
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

	public static void testResolver(File model, String regressionModelFilePath, File data, PrintStream eval, PrintStream out) throws Exception {
		Chunker chunker = null;
		chunker = (Chunker) AbstractExternalizable.readObject(model);
		Classifier regressionModel = PLACERegressionDisambiguation.readModel(regressionModelFilePath);
		testResolver(chunker, regressionModel, data, eval, out);
	}

	public static void testResolver(Chunker chunker, Classifier regressionModel, File data,	PrintStream eval, PrintStream out) throws Exception {
		//Evaluate CRF model
		ChunkerEvaluator evaluator = new ChunkerEvaluator(chunker);
		evaluator.setVerbose(false);
		Parser parser = new PLACEChunkParser();
		parser.setHandler(evaluator);
		parser.parse(data);
		if (chunker instanceof TIMEXRuleAnnotator)
			eval.print("Rule-based model - ");
		else if (chunker instanceof ChainCrfChunker)
			eval.print("CRF model - ");
		else
			eval.print("HMM model - ");
		eval.println();
		eval.println(evaluator.evaluation().perTypeEvaluation("NOM").precisionRecallEvaluation().toString());
			
		//Evaluate Regression model
	/*	NormalizedPlaceChunkerEvaluator evaluatorRegression = new NormalizedPlaceChunkerEvaluator(new PLACEMLAnnotator(chunker,regressionModel));
		evaluatorRegression.setVerbose(false);
		Parser parserRegression = new PLACEChunkParser();
		parserRegression.setHandler(evaluatorRegression);
		parserRegression.parse(data);
		if (chunker instanceof TIMEXRuleAnnotator)
			eval.print("Rule-based model - ");
		else if (chunker instanceof ChainCrfChunker)
			eval.print("CRF model - ");
		else
			eval.print("HMM model - ");
		eval.println();
		eval.println(evaluatorRegression.evaluation().perTypeEvaluation("PLACE").precisionRecallEvaluation().toString());*/
		
	//	if (out != null)
	//		annotateData(data, regressionModel, out, chunker);
	}

	public static void annotateData(File data, Classifier regressionModel, PrintStream out, Chunker chunker2)
	throws Exception {
		Chunker chunker = new PLACEMLAnnotator(chunker2, regressionModel);
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder loader = factory.newDocumentBuilder();
		Document doc = loader.parse(new FileInputStream(data));
		javax.xml.xpath.XPath xpath = javax.xml.xpath.XPathFactory.newInstance().newXPath();
		 
		NodeList docs = (NodeList) xpath.compile("//doc").evaluate(doc,	XPathConstants.NODESET);
		out.println("<corpus>");
		//For each document
		for (int i = 1; i < docs.getLength(); i++) {
			PLACEConstants.candidatesPlaceSameDoc.clear();
			PLACEConstants.isFirstPlace = true;
			
			out.println("<doc>");
			NodeList paragraphs = (NodeList) xpath.compile("./p").evaluate(docs.item(i), XPathConstants.NODESET);
			//For each sentence
			for (int j = 0; j < paragraphs.getLength(); j++) {
				out.print("<p>");
				String txt = xpath.compile(".").evaluate(paragraphs.item(j));
				System.out.println("Texto:"+txt);
				Chunk[] chunking = chunker.chunk(txt).chunkSet().toArray(new Chunk[0]);
				int lastPos = 0;
				//For each PLACE
				for (int k = 0; k < chunking.length; k++) {
					NormalizedPLACEChunk place = (NormalizedPLACEChunk)chunking[k];
					int start = place.start();
					int end = place.end();
					String chunkText = txt.substring(start,end);
					System.out.println("PlaceText: "+chunkText);
	 	    	//	timex.setNormalized(TIMEXRuleDisambiguation.createCanonicalForm(chunkText));
	 	            System.out.println(chunkText+"#");
	 	        //    ArrayList<Interval> normalized = timex.getNormalizedSet();
	 	            out.print(txt.substring(lastPos, start));
					out.print("<PLACE");
					
	 	      //      if (normalized == null){
	 	        	    out.print(">");
						out.print(chunkText);
						out.print("</PLACE>");
	 	 	            lastPos = end;
	 	     /*       }
	 	            else{
	 	            	System.out.println("Numero candidatos: "+normalized.size());
	 	            	String bestCandidate = TIMEXRegressionDisambiguation.disambiguate(chunkText, normalized, regressionModel);
	 	            	timex.setNormalized(bestCandidate);
	 	            	CandidateCreation.granularityDuration = 0;
	 	            	System.out.println(bestCandidate+"!!!!!!!!!!!");
	 	            	CandidateCreation.numberCandidatesTimex = 0;
	 	            	out.print(" val=\"");
	 	            	out.print(bestCandidate+"\"");
	 	            	out.print(">");
						out.print(chunkText);
						out.print("</TIMEX2>");
						lastPos = end;
	 	            }*/
				}
				out.print(txt.substring(lastPos));
				out.println("</p>");
			}
			out.println("</doc>");
		}
		out.println("</corpus>");
	}

	public static void prepareCorpus ( String path, int percent ) throws IOException {
		PrintWriter test = new PrintWriter(new FileWriter(path+"/../place-test.xml"));
		PrintWriter train = new PrintWriter(new FileWriter(path+"/../place-train.xml"));
		File files[] = new File(path).listFiles();
		int split = (int)((double)files.length * ((double)percent / 100.0));
		System.out.println(split);
		 
		test.println("<corpus>");
		train.println("<corpus>");
		//See which corpus to use
		if (path.contains("mitre_spatialml")){
			System.out.println("mitre_spatialml_Corpus");
			
			for (int pos = 1 ; pos < files.length ; pos++ ) {
				PrintWriter out = ( pos < split ) ? train : test;
				BufferedReader input = new BufferedReader(new FileReader(files[pos]));
				String aux = null;
				//flag to put space between concatenated lines
				boolean flag = true;
				boolean isFirstSentence = true;
				String concat = "";
				out.println("<doc>");
					
				while ( (aux=input.readLine()) != null && !aux.trim().contains("<TEXT>")) { }
				while ( (aux=input.readLine()) != null){
					if(aux.trim().equals("</TEXT>")) break;
					
					if (aux.equals(""))
						continue;
					
					if (!aux.endsWith(".")){
						if (flag){
							concat = concat.trim() + aux.replaceAll("&quot;", "");
							flag = false;
						}
						else 
							concat = concat.trim() + " " + aux.replaceAll("&quot;", "");
					}
					else{
						if (isFirstSentence){
							out.println("<p isFirst=\"true\">" + concat.trim() + " " + aux.replaceAll("&quot;", "") + "</p>");
							concat = "";
							flag = true;
							isFirstSentence = false;
						}
						else{
							out.println("<p>" + concat.trim() + " " + aux.replaceAll("&quot;", "") + "</p>");
							concat = "";
							flag = true;
						}
							
					}
				}
				
				out.println("</doc>");
			}
		}
		
		test.println("</corpus>");
		train.println("</corpus>");
		test.close();
		train.close();
	}

	public static void main(String args[]) throws Exception {
		if (args.length > 0)
			path = args[0];
	 	  prepareCorpus(path, 0);
		  
		  PrintStream evaluationCRF = new PrintStream(new FileOutputStream(new File(path+"/../Place-NOM2-evaluation-results-crf.txt"))); 
		  PrintStream annotationCRF = new PrintStream(new FileOutputStream(new File(path+"/../Place_CRF-Recognition-annotation-results-crf.txt")));
		  PLACEConstants.init();
		  PLACEConstants.candidatesPlaceSameDoc.clear();
	//	  trainResolver(new File(path+"/../place-train.xml"),new File(path+"/../place.model.crf"));
		  String main[] = {path+"/../place-train.xml"};
	//	  System.out.println("A entrar na desambiguação");
	//	  PLACERegressionDisambiguation.main(main);
		  testResolver(new File(path+"/../place.model.crf"), "/Users/vitorloureiro/Desktop/Geo-Temporal/geoModels/RegressionPlaceModel.svm", new File(path+"/../place-test.xml"), evaluationCRF, annotationCRF);
		  evaluationCRF.close(); 
		  annotationCRF.close();
		
	/*	HashSet<String> placeNames = new HashSet<String>();
		File files[] = new File(path).listFiles();
		System.out.println(files.length);
		
		for (int pos = 1 ; pos < files.length ; pos++ ) {
	    	BufferedReader reader = new BufferedReader(new FileReader(files[pos]));
	    	String aux;
	    	while ((aux=reader.readLine())!=null) 
	    		if(aux.trim().length() > 0){ 
	    			placeNames.add(aux.toLowerCase().trim());
	    		}
		}
		
		
		FileOutputStream fo = new FileOutputStream("/Users/vitorloureiro/Desktop/Geo-Temporal/PlaceWordsList.lex");  
        ObjectOutputStream oo=new ObjectOutputStream(fo);          
        oo.writeObject(placeNames);   
        oo.close();*/
		
		  
	
	}

}
