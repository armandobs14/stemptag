package temporal;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPathConstants;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import temporal.rules.TIMEXRuleAnnotator;
import com.aliasi.chunk.BioTagChunkCodec;
import com.aliasi.chunk.CharLmHmmChunker;
import com.aliasi.chunk.Chunk;
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

public class ModelTraining {
	
	private static String path = ".";
	private static String pathModel = ".";

	public static void trainResolver ( File in, File out ) throws Exception {
		boolean crf = out.getAbsolutePath().endsWith(".crf");
		Parser parser = new TIMEXChunkParser();
		if (crf) {
			TokenizerFactory factory = new IndoEuropeanTokenizerFactory();
			boolean enforceConsistency = true, cacheFeatures = true, addIntercept = true, uninformativeIntercept = true;
			int minFeatureCount = 1, priorBlockSize = 3, minEpochs = 10, maxEpochs = 5000;
			double priorVariance = 4.0, initialLearningRate = 0.05, learningRateDecay = 0.995, minImprovement = 0.00001;
			TagChunkCodec tagChunkCodec = new BioTagChunkCodec(factory,enforceConsistency);
			ChainCrfFeatureExtractor<String> featureExtractor = new CRFFeatureExtractor(in.getParentFile().getParentFile().getAbsolutePath()+ "/../pos-en-general-brown.HiddenMarkovModel", true, true);
			RegressionPrior prior = RegressionPrior.gaussian( priorVariance, uninformativeIntercept );
			AnnealingSchedule annealingSchedule = AnnealingSchedule.exponential(initialLearningRate, learningRateDecay);
	        Reporter reporter = Reporters.stdOut().setLevel(LogLevel.ERROR);
	        XValidatingObjectCorpus corpus = new XValidatingObjectCorpus(0);
			parser.setHandler(corpus);
		    parser.parse(in);
			ChainCrfChunker chunker = ChainCrfChunker.estimate(corpus, tagChunkCodec, factory, featureExtractor, addIntercept, minFeatureCount, cacheFeatures, prior, priorBlockSize, annealingSchedule, minImprovement, minEpochs, maxEpochs, reporter);
			AbstractExternalizable.serializeTo(chunker,out);
		} else {
		    	int MAX_N_GRAM = 8, NUM_CHARS = 256;
		    	double LM_INTERPOLATION = MAX_N_GRAM;
		    	TokenizerFactory factory = new IndoEuropeanTokenizerFactory();
		    	HmmCharLmEstimator hmmEstimator = new HmmCharLmEstimator(MAX_N_GRAM,NUM_CHARS,LM_INTERPOLATION,true);
		    	CharLmHmmChunker chunkerEstimator = new CharLmHmmChunker(factory,hmmEstimator);
		    	parser.setHandler(chunkerEstimator);
		    	parser.parse(in);
		    	AbstractExternalizable.compileTo(chunkerEstimator,out);
		}
	}
	
	public static void annotateData ( File data, PrintStream out, Chunker chunker2 ) throws Exception {
		Chunker chunker = new TIMEXRuleDisambiguation(chunker2);
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder loader = factory.newDocumentBuilder();
		Document doc = loader.parse( new FileInputStream(data) );
    	javax.xml.xpath.XPath xpath = javax.xml.xpath.XPathFactory.newInstance().newXPath();
    	NodeList docs = (NodeList) xpath.compile("//doc").evaluate( doc, XPathConstants.NODESET );
		out.println("<corpus>");
    	for (int i = 0; i < docs.getLength(); i++ ) {
			out.println("<doc>");
			NodeList paragraphs = (NodeList) xpath.compile("./p").evaluate( docs.item(i), XPathConstants.NODESET );
			for (int j = 0; j < paragraphs.getLength(); j++ ) {
				out.print("<p>");
				String txt = xpath.compile(".").evaluate( paragraphs.item(j) );
				Chunk[] chunking = chunker.chunk(txt).chunkSet().toArray(new Chunk[0]);
				int lastPos = 0;
				for (int k = 0; k < chunking.length; k++ ) {
					int start = chunking[k].start();
		        	int end = chunking[k].end();
		        	out.print(txt.substring(lastPos,start));
		        	out.print("<TIMEX2");
		        	if (chunking[k] instanceof NormalizedChunk) {
		        		String normalized = ((NormalizedChunk)(chunking[k])).getNormalized();
		        		if(normalized.length()>0) {
		        		   if (!normalized.contains("=")) out.print(" VAL=\""); else out.print(" ");
		        		   out.print(normalized);
		        		   if (!normalized.contains("=")) out.print("\">"); else out.print(">");
		        		} else out.print(">");
		        	} else out.print(">");
		        	out.print(txt.substring(start,end));
		        	out.print("</TIMEX2>");
		        	lastPos = end;
		    	}
				out.print(txt.substring(lastPos));
				out.println("</p>");
			}
			out.println("</doc>");
		}
		out.println("</corpus>");
	}
	
	public static void prepareCorpus ( String path, int percent ) throws IOException {
	//	PrintWriter test = new PrintWriter(new FileWriter(path+"/../../timex-test.xml"));
		PrintWriter train = new PrintWriter(new FileWriter(path+"/../timex-train.xml"));
		File files[] = new File(path).listFiles();
		int split = (int)((double)files.length * ((double)percent / 100.0));
		System.out.println(split);
	//	System.out.println(split);
	//	test.println("<corpus>");
		train.println("<corpus>");
		//See which corpus to use
		if(path.contains("ACE")){
			System.out.println("ACE_Corpus");
			
			}
		else if(path.contains("aquaint_timeml")){
			System.out.println("AQUAINT_TimeML_Corpus");
			
			for (int pos = 1 ; pos < split ; pos++ ) {
				PrintWriter out =  train;
				BufferedReader input = new BufferedReader(new FileReader(files[pos]));
				String aux = null; String aux2 = " "; String _split = null; String _split2 = null; String[] str=null;
				out.println("<doc>");
				Boolean _flag=false;
				Pattern _patern = null;	Pattern _patern2 = null;
				
				//Vê qual o tipo de ficheiro para ser correctamente processado
				if(files[pos].getName().contains("APW")){
					_patern = Pattern.compile("^\t.*");
					_patern2 = Pattern.compile("   [A-Z].*");
					_split = "\t";
					_split2= "   ";
					_flag=false;
				}
				else if ((files[pos].getName().contains("NYT"))){
					_patern = Pattern.compile("   [A-Z].*");
					_patern2 = Pattern.compile("   [A-Z].*");
					_split = "   ";
					_flag=false;
				}
				else if ((files[pos].getName().contains("XIE"))){
					_patern = Pattern.compile("^[A-Z]+.*");
					_patern2 = Pattern.compile("^[A-Z]+.*");
					_split = "§§";
					_flag=true;
				}
				while ( (aux=input.readLine()) != null) { 
					if(_patern.matcher(aux).matches() | _patern2.matcher(aux).matches()) {
					aux = aux.replaceAll("<EVENT[^>]*>","");aux = aux.replaceAll("</EVENT>","");
					aux = aux.replaceAll("<SIGNAL[^>]*>","");aux = aux.replaceAll("</SIGNAL>","");
					aux = aux.replaceAll("</TIMEX3>","</TIMEX2>");
					aux = aux.replaceAll("<TIMEX3 [^v]* value=(^>)*", "<TIMEX2 val=$1");
					aux = aux.replaceAll("<TIMEX2 val=([^ |>]*)[^>]*", "<TIMEX2 val=$1");
					aux2= aux; break;} }
				while ( (aux=input.readLine()) != null ) {
					
					if(aux.trim().contains("</TimeML>")) {
						aux2 = aux2.replaceAll("\\(PROFILE.*","");
						str = aux2.split(_split);						
						
						if(str.length!=1){
							for (int j=0;j<str.length;j++){
								if(str[j].contains(".")){
									out.println("<p>"+str[j].trim()+"</p>");
								}
							}
						}
						else{
							str = aux2.split(_split2);
							for (int j=0;j<str.length;j++){
								if(str[j].contains("."))
									out.println("<p>"+str[j].trim()+"</p>");
							}
						}
					aux2 = " ";
					break;
					}
					
					aux = substituteAndReplaceTags(aux);
								
					if (!aux.equals(""))
						aux2 = aux2+" "+aux;
					else if (_flag)
						aux2 = aux2+"§§";
					
				}
			aux2=" ";
			out.println("</doc>");
		}
		}

		else if(path.contains("TimeBank")){
			System.out.println("TimeBank_Corpus");
			
				for (int pos = 0 ; pos < split ; pos++ ) {
					PrintWriter out = train;
					BufferedReader input = new BufferedReader(new FileReader(files[pos]));
					String aux = null;
					String aux2 = " ";
					String[] str=null;
					out.println("<doc>");
					while ( (aux=input.readLine()) != null && !aux.trim().equals("<TEXT>")) { }
					while ( (aux=input.readLine()) != null ) {
												
						if(aux.trim().equals("</TEXT>")) {
							str = aux2.split("</p> <p>");
							//Substitute TIME3 tags with TIMEX2
							for (int i=0;i<str.length;i++){
								if(str[i].contains("<p>") && str[i].contains("TIMEX3"))
									out.println(str[i].trim().replaceAll("<TIMEX3 [^v]* value=(^>)*", "<TIMEX2 val=$1")+"</p>");
								else if(str[i].contains("</p>") && str[i].contains("TIMEX3"))
									out.println("<p>"+str[i].trim().replaceAll("<TIMEX3 [^v]* value=(^>)*", "<TIMEX2 val=$1"));
								else if(str[i].contains("TIMEX3"))
									out.println("<p>"+str[i].trim().replaceAll("<TIMEX3 [^v]* value=(^>)*", "<TIMEX2 val=$1")+"</p>");
								else if(str[i].contains("<p>") && str[i].contains("</p>"))
									out.println(str[i].trim());
								else if (str[i].contains("</p>"))
									out.println("<p>"+str[i].trim());
								else if (str[i].contains("<p>"))
									out.println(str[i].trim()+"</p>");
							}
						aux2 = " ";
						break;
						}
						if(aux.trim().contains("<turn")) {continue;}
						if(aux.trim().contains("</turn>")) {continue;}
						//Substitute tags that are not present in the scheme proposed
						aux = substituteAndReplaceTags(aux);
						
						aux2 = aux2.trim()+" "+aux.trim();
							
				}
					aux2=" ";
					out.println("</doc>");
				}
		}
		else if (path.contains("WikiWars")){
			System.out.println("WikiWars_Corpus");
			for (int pos = 0 ; pos < split ; pos++ ) {
				PrintWriter out = train;
				BufferedReader input = new BufferedReader(new FileReader(files[pos]));
				String aux = null;
				out.println("<doc>");
				while ( (aux=input.readLine()) != null && !aux.trim().equals("<TEXT>")) { }
				while ( (aux=input.readLine()) != null ) {
					if(aux.trim().equals("</TEXT>")) break;
					aux = aux.replaceFirst("(<TIMEX2[^>]*>[^<]*)<TIMEX2[^>]*>([^<]*)</TIMEX2>([^<]*)","$1$2$3");
					out.println("<p>" + aux + "</p>");
				}
				out.println("</doc>");
			}
		}
	
		train.println("</corpus>");
		train.close();
	}
	
	public static String substituteAndReplaceTags ( String sentence ){
		
		sentence = sentence.replaceAll("<MAKEINSTANCE[^>]*/>","");
		sentence = sentence.replaceAll("<TLINK[^>]*/>","");
		sentence = sentence.replaceAll("<ALINK[^>]*/>","");
		sentence = sentence.replaceAll("<SLINK[^>]*/>","");
		sentence = sentence.replaceAll("<DOC>","");sentence = sentence.replaceAll("</DOC>","");
		sentence = sentence.replaceAll("<DOCNO>","");sentence = sentence.replaceAll("</DOCNO>","");
		sentence = sentence.replaceAll("<DATE_TIME>","");sentence = sentence.replaceAll("</DATE_TIME>","");
		sentence = sentence.replaceAll("<BODY>","");sentence = sentence.replaceAll("</BODY>","");
		sentence = sentence.replaceAll("<TEXT>","");sentence = sentence.replaceAll("</TEXT>","");
		sentence = sentence.replaceAll("<P>","");sentence = sentence.replaceAll("</P>","");
		sentence = sentence.replaceAll("<ANNOTATION[^>]*>","");sentence = sentence.replaceAll("</ANNOTATION>","");
		sentence = sentence.replaceAll("<TRAILER[^>]*>","");sentence = sentence.replaceAll("</TRAILER>","");
		sentence = sentence.replaceAll(".*&amp;QL;.*","");
		sentence = sentence.replaceAll(".*&amp;UR;.*","");
		sentence = sentence.replaceAll("&amp;LR;","");
		sentence = sentence.replaceAll("<TIMEX3 [^v]* value=(^>)*", "<TIMEX2 val=$1");
		sentence = sentence.replaceAll("<TIMEX2 val=([^ |>]*)[^>]*", "<TIMEX2 val=$1");
		
		sentence = sentence.replaceAll("</TIMEX3>","</TIMEX2>");
		sentence = sentence.replaceAll(" anchorTimeID=[^(>|v)]*"," ");
		sentence = sentence.replaceAll("quant=\"every\""," ");
		sentence = sentence.replaceAll("<TIMEX [^>]*>","");sentence = sentence.replaceAll("</TIMEX>","");
		sentence = sentence.replaceAll("<ENAMEX[^>]*>","");sentence = sentence.replaceAll("</ENAMEX>","");
		sentence = sentence.replaceAll("<EVENT[^>]*>","");sentence = sentence.replaceAll("</EVENT>","");
		sentence = sentence.replaceAll("<NUMEX[^>]*>","");sentence = sentence.replaceAll("</NUMEX>","");
		sentence = sentence.replaceAll("<SIGNAL[^>]*>","");sentence = sentence.replaceAll("</SIGNAL>","");
		sentence = sentence.replaceAll("<CARDINAL[^>]*>","");sentence = sentence.replaceAll("</CARDINAL>","");
		sentence = sentence.replaceAll("<HEAD>","");sentence = sentence.replaceAll("</HEAD>","");
		sentence = sentence.replaceAll("<NG[^>]*>","");sentence = sentence.replaceAll("</NG>","");
		sentence = sentence.replaceAll("<VG>","");sentence = sentence.replaceAll("</VG>","");
		sentence = sentence.replaceAll("<PG>","");sentence = sentence.replaceAll("</PG>","");
		sentence = sentence.replaceAll("<VG-INF>","");sentence = sentence.replaceAll("</VG-INF>","");
		sentence = sentence.replaceAll("<VG-VBG>","");sentence = sentence.replaceAll("</VG-VBG>","");
		sentence = sentence.replaceAll("<RG>","");sentence = sentence.replaceAll("</RG>","");
		sentence = sentence.replaceAll("<VG-VBN>","");sentence = sentence.replaceAll("</VG-VBN>","");
		sentence = sentence.replaceAll("<JG>","");sentence = sentence.replaceAll("</JG>","");
		sentence = sentence.replaceAll("<IN-MW>","");sentence = sentence.replaceAll("</IN-MW>","");
		sentence = sentence.replaceAll("([^( |>)])<TIMEX3 ([^>]*)> ([^<]*)</TIMEX2>", "$1 <TIMEX3 $2>$3</TIMEX2>");
		sentence = sentence.replace("<s>", "<p>");
		sentence = sentence.replace("</s>", "</p>");
		sentence = sentence.replace("</p>.", ".</p>");
		
		return sentence;	
	}
		
	public static void main ( String args[] ) throws Exception {	
		if(args.length>0){
			String[] arg = args[0].split(",");
			path = arg[0];
			pathModel = arg[1];
		}
		System.out.println(path);
		prepareCorpus(path,100);
		trainResolver(new File(path+"/../timex-train.xml"),new File(pathModel+"/timex.model.crf"));

	}

}
