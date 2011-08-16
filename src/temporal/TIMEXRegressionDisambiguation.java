package temporal;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;
import java.util.HashMap;
import java.util.TreeMap;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import org.joda.time.DateTime;
import org.joda.time.Interval;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import weka.classifiers.Classifier;
import weka.core.Instance;
import weka.classifiers.functions.SVMreg;

public class TIMEXRegressionDisambiguation {
	
	public static Map<Integer,String> wordsList = new TreeMap<Integer,String>();
	private static Map<String,Interval> trainingData = new HashMap<String,Interval>();
	private static boolean flag;
	
	public static void init(String path){
		TIMEXRegressionDisambiguation.makeWordList(path);
	}
		
	public static Classifier readModel ( String path ) throws Exception {
		Classifier regression = (Classifier) weka.core.SerializationHelper.read(path);
		return regression;
	}

	public static void writeModel ( File file, Classifier model ) throws Exception {
		weka.core.SerializationHelper.write(file.getAbsolutePath(),model);
	}
	
	public static void trainModel ( File in, PrintWriter o) {
		try {
			ArrayList<Interval> candidatos = new ArrayList<Interval>();
			ArrayList<String> features;
			ArrayList<String> data = new ArrayList<String>();;
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
	        DocumentBuilder db = dbf.newDocumentBuilder();
	        InputSource is = new InputSource();
	        is.setCharacterStream(new FileReader(in));
	        Document doc = db.parse(is);
			XPath xpath = XPathFactory.newInstance().newXPath();
			NodeList docs = (NodeList) xpath.compile("//doc").evaluate(doc,	XPathConstants.NODESET);
			
			//For each document
			for (int n = 0; n < docs.getLength(); n++) {
				
				CandidateCreation.candidatesMillisecondsSameDoc.clear();
	    		CandidateCreation.candidatesIntervalsSameDoc.clear();
	    		CandidateCreation.isFirstTimex = true;
	    		NodeList elemList = (NodeList) xpath.compile("./p").evaluate(docs.item(n), XPathConstants.NODESET);
	    		
			
			//For every phrase in the document
			for ( int i = 0; i < elemList.getLength(); i++ ) try {
				//Tira o Timestamp do Documento
				if(elemList.item(i).hasAttributes()){
					CandidateCreation.docCreationTime = new DateTime(new String(elemList.item(i).getAttributes().item(0).getTextContent()));
					System.out.println("Data cria��o documento: "+CandidateCreation.docCreationTime);
				}
					
				//Limpa os arrays com os candidatos da mesma frase.
				CandidateCreation.candidatesMillisecondsSameSentence.clear();
			    CandidateCreation.candidatesIntervalsSameSentence.clear();
				System.out.println("p");
				Element timex = (Element) elemList.item(i);
				ArrayList<Integer> wordFeatureList;
			    NodeList timex2List = timex.getElementsByTagName("TIMEX2");
			    //For every TIMEX2 tag in the phrase
				for ( int j = 0; j < timex2List.getLength(); j++ ){
					String val = xpath.compile("./@val").evaluate(timex2List.item(j));
					Interval Correctdate = CandidateCreation.normalizeTimex(val, new DateTime());
					if (Correctdate == null){
						/*//Limpa os arrays com os candidatos da mesma frase.
						CandidateCreation.candidatesMillisecondsSameSentence.clear();
					    CandidateCreation.candidatesIntervalsSameSentence.clear();*/
						CandidateCreation.numberCandidatesTimex = 0;
						continue;
					}
					
					//Computes the word feature
					String TimexText = xpath.compile("./text()").evaluate(timex2List.item(j));
					wordFeatureList = wordFeature(TimexText);
							
					//Computes each candidate of the time expression
					candidatos = TIMEXRuleDisambiguation.createCanonicalForm(timex2List.item(j).getTextContent());
					if (candidatos == null || CandidateCreation.granularityDuration > 0)
						continue;
					System.out.println("Numero candidatos: "+candidatos.size());
					Iterator<Interval> it = candidatos.iterator();
					Interval auxIt;
					double overlap;
					//Computes for each candidate its features
					while(it.hasNext()) {
						String dataToPrint = "";
	 	            	auxIt = it.next();
	 	            	features = TIMEXRuleDisambiguation.featuresGenerator(auxIt);
	 	            	System.out.println("Interval: "+auxIt.toString());
	 	            	System.out.println("Overlap: "+TIMEXRuleDisambiguation.normalizedOverlap(auxIt, Correctdate));
	 	            	data.add(features.get(0));
	 	            	data.add(features.get(1));
	 	            	data.add(features.get(2));
	 	            	data.add(features.get(3));
	 	            	data.add(features.get(4));
	 	            	data.add(features.get(5));
	 	            	data.add(features.get(6));
	 	            	data.add(features.get(7));
	 	            	data.add(features.get(8));
	 	            	for (int m = 0; m < wordFeatureList.size(); m++ )
	 	            		data.add(wordFeatureList.get(m).toString());
	 	            	overlap = TIMEXRuleDisambiguation.normalizedOverlap(auxIt, Correctdate);
	 	            	System.out.println("tamanho: "+data.size());
	 	            	
	 	            	//Prints data to the temporary file
	 	            	for (int m = 0; m < data.size(); m++)
	 	            		dataToPrint = dataToPrint+data.get(m)+",";
	 	            	dataToPrint = dataToPrint+overlap; 	            	
	 	            	o.println(dataToPrint);
	 	            	System.out.println(dataToPrint);
	 	            	data.clear();
	 	            	
	 	            }
	 	            
					CandidateCreation.numberCandidatesTimex = 0;
				}
				
			} catch ( Exception e ) { }
			}
			
			TIMEXRegressionDisambiguation.flag = true;
		} catch ( Exception e ) { e.printStackTrace(); }
	}
	
	@SuppressWarnings("unchecked")
	public static String disambiguate ( String timex, ArrayList<Interval> candidates , Classifier model ) {
		//double maxScore = Double.MIN_VALUE;
		double maxScore = -1000;
		Interval bestCandidate = null;
		ArrayList<String> features;
		ArrayList<Integer> wordFeatures = new ArrayList<Integer>();	
        Interval candidateInterval;
        String featureGranularity = "";
        
        try {
        //get List of Words from file
        FileInputStream fi=new FileInputStream("/Users/vitorloureiro/Desktop/Teste3/models/WordsList.lex");  
        ObjectInputStream oi=new ObjectInputStream(fi); 
        wordsList= (TreeMap<Integer,String>)oi.readObject();    
        oi.close();
        
		
			Iterator<Interval> it = candidates.iterator();

			//Get word features
			wordFeatures = wordFeature(timex);
			// repeat steps below for each candidate disambiguation
	            while(it.hasNext()) {
	            	candidateInterval = it.next();
	            	features = TIMEXRuleDisambiguation.featuresGenerator(candidateInterval);
	            	System.out.println(features.get(0));
	            	System.out.println(features.get(1));
	            	System.out.println(features.get(2));
	            	System.out.println(features.get(3));
	            	System.out.println(features.get(4));
	            	System.out.println(features.get(5));
	            	System.out.println(features.get(6));
	            	System.out.println(features.get(7));
	            	System.out.println(features.get(8));
	        
	    			double feature1 = Double.parseDouble(features.get(0));
	    			double feature2 = Double.parseDouble(features.get(1));
	    			double feature3 = Double.parseDouble(features.get(2));
	    			double feature4 = Double.parseDouble(features.get(3));
	    			double feature5 = Double.parseDouble(features.get(4));
	    			double feature6 = Double.parseDouble(features.get(5));
	    			double feature7 = Double.parseDouble(features.get(6));
	    			double feature8 = Double.parseDouble(features.get(7));
	    			double feature9 = Double.parseDouble(features.get(8));
	    			Instance instance = new Instance(9+wordFeatures.size());
	    			instance.modifyValue(0, feature1);
	    			instance.modifyValue(1, feature2);
	    			instance.modifyValue(2, feature3);
	    			instance.modifyValue(3, feature4);
	    			instance.modifyValue(4, feature5);
	    			instance.modifyValue(5, feature6);
	    			instance.modifyValue(6, feature7);
	    			instance.modifyValue(7, feature8);
	    			instance.modifyValue(8, feature9);
	    			
	    			for (int i = 0; i < wordFeatures.size(); i++)
	    				instance.modifyValue(i+9, wordFeatures.get(i));
	    				    			
	    			double score = model.classifyInstance(instance);
	    			System.out.println("Score candidato "+candidateInterval.toString()+": "+score);
	    			System.out.println("MaxScore: "+maxScore);
	    			if ( score > maxScore ) {
	    				maxScore = score;
	    				bestCandidate = candidateInterval;
	    				featureGranularity = features.get(5);
	    			}
	            	
	            }
	            System.out.println("Granularity: "+featureGranularity);
	            
	         // Return date associated to the best candidate
	            
	            //if is duration timex
	            if (CandidateCreation.granularityDuration > 0){
	            	if (CandidateCreation.granularityDuration == 1){
	            		return new String ("PT"+bestCandidate.getStart().getMillisOfSecond()+"S");
	            	}
	            	else if (CandidateCreation.granularityDuration == 2){
	            		return new String ("PT"+bestCandidate.getStart().getMillisOfSecond()+"M");
	            	}
	            	else if (CandidateCreation.granularityDuration == 3){
	            		return new String ("PT"+bestCandidate.getStart().getMillisOfSecond()+"H");
	            	}
	            	else if (CandidateCreation.granularityDuration == 4){
	            		return new String ("P"+bestCandidate.getStart().getMillisOfSecond()+"D");
	            	}
	            	else if (CandidateCreation.granularityDuration == 5){
	            		return new String ("P"+bestCandidate.getStart().getMillisOfSecond()+"W");
	            	}
	            	else if (CandidateCreation.granularityDuration == 6){
	            		return new String ("P"+bestCandidate.getStart().getMillisOfSecond()+"M");
	            	}
	            	else if (CandidateCreation.granularityDuration == 7){
	            		return new String ("P"+bestCandidate.getStart().getMillisOfSecond()+"Y");
	            	}
	            	else if (CandidateCreation.granularityDuration == 8){
	            		return new String ("P"+bestCandidate.getStart().getMillisOfSecond()+"DE");
	            	}
	            	else if (CandidateCreation.granularityDuration == 10){
	            		return new String ("P"+bestCandidate.getStart().getMillisOfSecond()+"CE");
	            	}
	            	else if (CandidateCreation.granularityDuration == 91){
	            		return new String ("PTXXS");
	            	}
	            	else if (CandidateCreation.granularityDuration == 92){
	            		return new String ("PTXXM");
	            	}
	            	else if (CandidateCreation.granularityDuration == 93){
	            		return new String ("PTXXH");
	            	}
	            	else if (CandidateCreation.granularityDuration == 94){
	            		return new String ("PXXD");
	            	}
	            	else if (CandidateCreation.granularityDuration == 95){
	            		return new String ("PXXW");
	            	}
	            	else if (CandidateCreation.granularityDuration == 96){
	            		return new String ("PXXM");
	            	}
	            	else if (CandidateCreation.granularityDuration == 97){
	            		return new String ("PXXY");
	            	}
	            	else if (CandidateCreation.granularityDuration == 98){
	            		return new String ("PXXDE");
	            	}
	            	else if (CandidateCreation.granularityDuration == 100){
	            		return new String ("PXXCE");
	            	}
	            	else if (CandidateCreation.granularityDuration == 101){
	            		return new String ("PXXCE");
	            	}
	            }
	            
	            //if is point timex
	            if (featureGranularity.equals("1")){
	            	return bestCandidate.getStart().toString("YYYY-MM-dd'T'"+bestCandidate.getStart().getHourOfDay()+":mm");
	            }
	            else if (featureGranularity.equals("2")){
	            	return bestCandidate.getStart().toString("YYYY-MM-dd'T'"+bestCandidate.getStart().getHourOfDay());
	            }
	            else if (featureGranularity.equals("3")){
	            	return bestCandidate.getStart().toString("YYYY-MM-dd");
	            }
	            else if (featureGranularity.equals("4")){
	            	return bestCandidate.getStart().toString("YYYY-MM-dd");
	            }
	            else if (featureGranularity.equals("5")){
	            	return bestCandidate.getStart().toString("YYYY-MM");
	            }
	            else if (featureGranularity.equals("6")){
	            	return bestCandidate.getStart().toString("YYYY");
	            }
	            else if (featureGranularity.equals("7")){
	            	return bestCandidate.getStart().toString("YYYY");
	            }
	            else
	            	return null;
	            
		} catch ( Exception e ) {
			e.printStackTrace();
		}
		return bestCandidate.getStart().toString();
		

		
	}
	
	public static ArrayList<Integer> wordFeature(String timex){
		ArrayList<Integer> list = new ArrayList<Integer>();
		for (int i = 0; i < wordsList.size(); i++){
			list.add(0);
		}
		
		System.out.println("timex: "+timex);
		String split[] = timex.split(" ");
		int l;
		for (String s : split){
			Iterator<String> it2 = wordsList.values().iterator();
			l = 0;
			if (wordsList.containsValue(s.replaceAll(",", "").toLowerCase()))
				while (it2.hasNext()){
					if (it2.next().equals(s.replaceAll(",", "").toLowerCase())){
						list.set(l, 1);
						System.out.println("Posi��o da palavra "+s.replaceAll(",", "")+": "+l);
						break;
					}
					l++;	
				}
		}
		return list;
		
	}
	
	public static void makeWordList(String path){
		try {
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db = dbf.newDocumentBuilder();
        ArrayList<String> stopwords = new ArrayList<String>();
        FileInputStream fi=new FileInputStream("/Users/vitorloureiro/Desktop/Teste3/models/StopWords.lex");  
        ObjectInputStream oi=new ObjectInputStream(fi); 
        stopwords = (ArrayList<String>)oi.readObject();    
        oi.close();
        
        int k=0;
	        InputSource is = new InputSource();
			is.setCharacterStream(new FileReader(path));
	        Document doc = db.parse(is);
			XPath xpath = XPathFactory.newInstance().newXPath();
			XPathExpression expr = xpath.compile("//TIMEX2");
			NodeList elemList = (NodeList) expr.evaluate(doc, XPathConstants.NODESET);
			
			for ( int j = 0; j < elemList.getLength(); j++ ) {
				String text = xpath.compile("./text()").evaluate(elemList.item(j));
				
				String split[] = text.split(" ");
				
				for (String s : split){
					if (!wordsList.containsValue(s.toLowerCase()) && !stopwords.contains(s.toLowerCase())){
						wordsList.put(k, s.replaceAll(",", "").toLowerCase());
						k++;
					}
				}
			}

        //Serialize List to a file, to be used later
        FileOutputStream fo = new FileOutputStream("/Users/vitorloureiro/Desktop/Teste3/models/WordsList.lex");  
        ObjectOutputStream oo=new ObjectOutputStream(fo);          
        oo.writeObject(wordsList);   
        oo.close();
        
        } catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SAXException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (XPathExpressionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	public static void main ( String args[] ) throws Exception {
	/*	Classifier model = trainModel( new File("/Users/vitorloureiro/Desktop/Teste3/teino_regressao.xml") );
		long result = disambiguate("10th july 1944", model);
		String resultStr = new SimpleDateFormat ("yyyy-MM-dd").format(result);
		System.out.println(resultStr);*/
	//	TIMEXExperiment.prepareCorpus("/Users/vitorloureiro/Desktop/Teste3/WikiWars_regression", 100);
		CandidateCreation.init();
		CandidateCreation.isXMLflag = true;
		CandidateCreation.candidatesMillisecondsSameDoc.clear();
		CandidateCreation.candidatesIntervalsSameDoc.clear();
	//	TIMEXRegressionDisambiguation.makeWordList(args[0]);
	//	TIMEXRegressionDisambiguation.makeWordList("/Users/vitorloureiro/Desktop/Teste3/WikiWars_regression/01_WW2.key.xml");
		TIMEXRegressionDisambiguation.makeWordList("/Users/vitorloureiro/Desktop/Teste3/timex-train.xml");
		File fileData = File.createTempFile("ranking-data", ".arff");
		System.out.println(fileData.getAbsolutePath());
		PrintWriter o = new PrintWriter(new FileWriter(fileData.getAbsolutePath()));
        o.println("@RELATION  timex-learn-to-rank");
        o.println("@ATTRIBUTE feature1      NUMERIC");
        o.println("@ATTRIBUTE feature2      NUMERIC");
        o.println("@ATTRIBUTE feature3      NUMERIC");
        o.println("@ATTRIBUTE feature4      NUMERIC");
        o.println("@ATTRIBUTE feature5      NUMERIC");
        o.println("@ATTRIBUTE feature6      NUMERIC");
        o.println("@ATTRIBUTE feature7      NUMERIC");
        o.println("@ATTRIBUTE feature8      NUMERIC");
        o.println("@ATTRIBUTE feature9      NUMERIC");
        for (int i = 0; i < wordsList.size(); i++)
        	o.println("@ATTRIBUTE feature"+(i+10)+"      NUMERIC");
        o.println("@ATTRIBUTE overlap       NUMERIC");
        o.println("@DATA");
        
        //trainModel( new File(args[0]), o);
        trainModel( new File("/Users/vitorloureiro/Desktop/Teste3/timex-train.xml"), o);
        
		TIMEXRegressionDisambiguation.flag = false;
		
		o.close();
        File fileModel = File.createTempFile("ranking-model", "tmp");
        fileData.deleteOnExit();
        fileModel.deleteOnExit();
        String params2[] = { "-i", "-no-cv" , "-split-percentage", "99", "-K", "weka.classifiers.functions.supportVector.RBFKernel", "-t" , fileData.getAbsolutePath(), "-d" , fileModel.getAbsolutePath() };
        SVMreg.main(params2);
        Classifier model = readModel(fileModel.getAbsolutePath());
        writeModel ( new File("/Users/vitorloureiro/Desktop/Teste3/models/RegressionModel.svm"), model );
        fileData.delete();
        fileModel.delete();
        wordsList.clear();
	}

}