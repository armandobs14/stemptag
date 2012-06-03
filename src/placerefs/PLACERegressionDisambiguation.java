package placerefs;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;

/*import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.Query;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;*/

import org.apache.lucene.analysis.standard.StandardAnalyzer;
//import org.apache.lucene.document.Document;
import org.apache.lucene.index.*;
import org.apache.lucene.queryParser.*;
import org.apache.lucene.search.*;
import org.apache.lucene.store.*;
import org.apache.lucene.util.Version;


import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import placerefs.gazetteer.CandidateGenerator;
import placerefs.gazetteer.GazetteerEntry;
import placerefs.gazetteer.VincentyDistanceCalculator;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.PrecisionModel;
import weka.classifiers.Classifier;
import weka.core.Instance;
import weka.classifiers.functions.SVMreg;

public class PLACERegressionDisambiguation {
		
	private static String outputPR = "outputPlaceReferences";
	
	private static HashMap<GazetteerEntry, Double> sortHashMap(HashMap<GazetteerEntry, Double> input){
	    Map<GazetteerEntry, Double> tempMap = new HashMap<GazetteerEntry, Double>();
	    for (GazetteerEntry wsState : input.keySet()){
	        tempMap.put(wsState,input.get(wsState));
	    }

	    List<GazetteerEntry> mapKeys = new ArrayList<GazetteerEntry>(tempMap.keySet());
	    List<Double> mapValues = new ArrayList<Double>(tempMap.values());
	    HashMap<GazetteerEntry, Double> sortedMap = new LinkedHashMap<GazetteerEntry, Double>();
	    TreeSet<Double> sortedSet = new TreeSet<Double>(mapValues);
	    Object[] sortedArray = sortedSet.toArray();
	    int size = sortedArray.length;
	    for (int i=0; i<size; i++){
	        sortedMap.put(mapKeys.get(mapValues.indexOf(sortedArray[i])), 
	                      (Double)sortedArray[i]);
	    }
	    return sortedMap;
	}
	
	
	public static Classifier readModel ( String path ) throws Exception {
		Classifier regression = (Classifier) weka.core.SerializationHelper.read(path);
		return regression;
	}

	public static void writeModel ( File file, Classifier model ) throws Exception {
		weka.core.SerializationHelper.write(file.getAbsolutePath(),model);
	}
	
	@SuppressWarnings("deprecation")
	public static void trainModel ( File in, PrintWriter o) {
		try {
			double CorrectlatAttribute;
	        double CorrectlonAttribute;
	        int size;
			List<GazetteerEntry> candidatos = new ArrayList<GazetteerEntry>();
			ArrayList<GazetteerEntry> candidatosDestePlace = new ArrayList<GazetteerEntry>();
			
			//variaveis para restringir o numero de candidatos deste place a usar nas features de places futuros
			HashMap<GazetteerEntry, Double> topCandidatosDestePlace = new HashMap<GazetteerEntry, Double>();
			
			
			ArrayList<String> features;
			ArrayList<String> data = new ArrayList<String>();
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
	        DocumentBuilder db = dbf.newDocumentBuilder();
	        InputSource is = new InputSource();
	        is.setCharacterStream(new FileReader(in));
	        Document doc = db.parse(is);
			XPath xpath = XPathFactory.newInstance().newXPath();
			NodeList docs = (NodeList) xpath.compile("//doc").evaluate(doc,	XPathConstants.NODESET);
			
			
			//For each document
			for (int n = 0; n < docs.getLength(); n++) {
				
				PLACEConstants.candidatesPlaceSameDoc.clear();
				PLACEConstants.bestCandidatePreviousPlaceSameDoc.clear();
				
				
				System.out.println("doc########################################################");
		    	PLACEConstants.isFirstPlace = true;
		    	NodeList elemList = (NodeList) xpath.compile("./p").evaluate(docs.item(n), XPathConstants.NODESET);
	    		
	    		//TODO: TESTE LUIS
		    	/*
	    		//For every phrase in the document
		    	for ( int i = 0; i < elemList.getLength(); i++ ) try {
		    		NodeList placeList = (NodeList) xpath.compile("./PLACE").evaluate(elemList.item(i), XPathConstants.NODESET);
		    		String phraseText = xpath.compile("./text()").evaluate(elemList.item(i));

		    		//For every PLACE tag in the phrase
		    		for ( int j = 0; j < placeList.getLength(); j++ ){
		    			String latLong = xpath.compile("./@latLong").evaluate(placeList.item(j));
		    			String placeText = xpath.compile("./text()").evaluate(placeList.item(j));
					
		    			if (latLong == ""){
		    				continue;
		    			}
					
		    			size = latLong.replaceAll("(°)|�", "").split(" ")[0].length();
		    			CorrectlatAttribute = Double.parseDouble(latLong.replaceAll("(°)|�", "").split(" ")[0].substring(0, size-2));
		            	if(latLong.split(" ")[0].toLowerCase().contains("s"))
		            		CorrectlatAttribute = CorrectlatAttribute*(-1);
		            	
		            	size = latLong.replaceAll("(°)|�", "").split(" ")[1].length();
		            	CorrectlonAttribute = Double.parseDouble(latLong.replaceAll("(°)|�", "").split(" ")[1].substring(0, size-2));
		            	if(latLong.split(" ")[1].toLowerCase().contains("w"))
		            		CorrectlonAttribute = CorrectlonAttribute*(-1);
		            	
		            	//Computes each candidate of the place expression
			            CandidateGenerator cg = new CandidateGenerator();
			            candidatos = cg.getCandidates(placeText);
			            Iterator<GazetteerEntry> it = candidatos.iterator();
			            GazetteerEntry nextPlace;
					
			            while(it.hasNext()) {
			            	PLACEConstants.candidatesPlaceSameDoc.add(it.next());
			            }
		    		}
		    	}catch(Exception e){
				e.printStackTrace();
			}
			*/
				
				
			//For every phrase in the document
			for ( int i = 0; i < elemList.getLength(); i++ ) try {
				NodeList placeList = (NodeList) xpath.compile("./PLACE").evaluate(elemList.item(i), XPathConstants.NODESET);
				
				//String phraseText = xpath.compile("data(.)").evaluate(elemList.item(i));
				//System.out.println("USANDO DATA: " + phraseText);
				
				String phraseText = ((NodeList) xpath.compile(".").evaluate(elemList.item(i), XPathConstants.NODESET)).item(0).getTextContent();
				
				//#############################################################################################
				//##################################### Feito Por Mim #########################################
				//#############################################################################################
				
//				System.out.println("count: " + xpath.compile("count(./text())").evaluate(elemList.item(i)));
//				System.out.println("countPLACE: " + placeList.getLength());
				
//				int countText = Integer.parseInt(xpath.compile("count(./text())").evaluate(elemList.item(i)));
//				int countPlace = placeList.getLength();
//				String pWholeText = "";
//				
//				if(countText>1){
//					int j = 0, k = 0;
//					for(; ((j <= countText) && (k < placeList.getLength())); j++, k++){
//						//System.out.println("text"+(j+1)+": "+xpath.compile("./text()["+(j+1)+"]").evaluate(elemList.item(i)));
//						//System.out.println("place"+k+": "+xpath.compile("./text()").evaluate(placeList.item(k)));
//						pWholeText = pWholeText.concat(xpath.compile("./text()["+(j+1)+"]").evaluate(elemList.item(i)) + xpath.compile("./text()").evaluate(placeList.item(k)));
//					}
//					
//					if(countText > countPlace){
//						pWholeText = pWholeText.concat(xpath.compile("./text()["+countText+"]").evaluate(elemList.item(i)));
//						//System.out.println("text"+countText+": "+xpath.compile("./text()["+countText+"]").evaluate(elemList.item(i)));
//					}
//					else if(countText < countPlace){
//						pWholeText = pWholeText.concat(xpath.compile("./text()").evaluate(placeList.item(countPlace-1)));
//						//System.out.println("place"+(countPlace-1)+": "+xpath.compile("./text()").evaluate(placeList.item(countPlace-1)));
//					}
//					
//				}
//				else
//					pWholeText = xpath.compile("./text()").evaluate(elemList.item(i));
//				//System.out.println("pWholeText: " + pWholeText);
//				
//				//#############################################################################################
//				//#############################################################################################
//				//#############################################################################################
//
//				
//				String phraseText = pWholeText;
//				System.out.println("phraseText: " + phraseText);
//				phraseText = pWholeText;

				//For every PLACE tag in the phrase
				for ( int j = 0; j < placeList.getLength(); j++ ){
					//Array para calcular os melhores candidatos deste PLACE para calculo de features futuras
					topCandidatosDestePlace.clear();
					
					String latLong = xpath.compile("./@latLong").evaluate(placeList.item(j));
					String placeText = xpath.compile("./text()").evaluate(placeList.item(j));
					
//					System.out.println("placeText: " + placeText);
					
					if (latLong == ""){
						continue;
					}
					
					size = latLong.replaceAll("(°)|�", "").split(" ")[0].length();
					CorrectlatAttribute = Double.parseDouble(latLong.replaceAll("(°)|�", "").split(" ")[0].substring(0, size-2));
	            	if (latLong.split(" ")[0].toLowerCase().contains("s"))
	            		CorrectlatAttribute = CorrectlatAttribute*(-1);
	            	
	            	size = latLong.replaceAll("(°)|�", "").split(" ")[1].length();
	            	CorrectlonAttribute = Double.parseDouble(latLong.replaceAll("(°)|�", "").split(" ")[1].substring(0, size-2));
	            	if (latLong.split(" ")[1].toLowerCase().contains("w"))
	            		CorrectlonAttribute = CorrectlonAttribute*(-1);
					
					//Computes each candidate of the place expression
	            	CandidateGenerator cg = new CandidateGenerator();
	            	candidatos = cg.getCandidates(placeText);
	                Iterator<GazetteerEntry> it = candidatos.iterator();
					GazetteerEntry nextPlace;
					
					
					System.out.println("N candidatos deste place: " + candidatos.size());
					
					//Descobre qual o melhor candidato e adiciona-o a lista para feature10
					double scoreBestCandidate = Double.MAX_VALUE;
					int indexBestCandidate = 0;
					int iteratorCount = 0;
					
					//Computes for each candidate its features
					while(it.hasNext()) {
						iteratorCount++;
						
						String dataToPrint = "";
						nextPlace = it.next();
						
						System.out.println("----------------------------------");
						System.out.println("Candidato: " + nextPlace.name);
						System.out.println("Candidatos nos no same doc: " + PLACEConstants.candidatesPlaceSameDoc.size());
						
						candidatosDestePlace.add(nextPlace);
						features = PLACEFeatureGenerator.featuresGenerator(nextPlace, phraseText, placeText);

	 	            	data.add(features.get(0));
	 	            	data.add(features.get(1));
	 	            	data.add(features.get(2));
	 	            	data.add(features.get(3));
	 	            	data.add(features.get(4));
	 	            	data.add(features.get(5));
	 	            	data.add(features.get(6));
	 	            	data.add(features.get(7));
	 	            	data.add(features.get(8));
	 	            	data.add(features.get(9));
	 	            	
	 	            	//Prints data to the temporary file
	 	            	for (int m = 0; m < data.size(); m++)
	 	            		dataToPrint = dataToPrint+data.get(m)+",";
	 	            	
	 	            	Point p1 = new Point(new Coordinate(CorrectlonAttribute,CorrectlatAttribute), new PrecisionModel(),4326);
	 	            	Point p2 = new Point(new Coordinate(Double.parseDouble(nextPlace.coordinates.split(" ")[1].replaceAll("N|E|S|W", "")),Double.parseDouble(nextPlace.coordinates.split(" ")[0].replaceAll("N|E|S|W", ""))), new PrecisionModel(),4326);
	 	            	Double vc = VincentyDistanceCalculator.getDistance(p1,p2);
	 	            	
	 	            	System.out.println("Point p1: " + p1);
	 	            	System.out.println("Point p2: " + p2);
	 	            	System.out.println("Vincenty Dist: " + vc);
	 	            	System.out.println("----------------------------------");
	 	            	
	 	            	dataToPrint = dataToPrint+vc;	
	 	            	o.println(dataToPrint);
	 	            	data.clear();
	 	            	
	 	            	
	 	            	if(vc<scoreBestCandidate){
	 	            		scoreBestCandidate = vc;
	 	            		indexBestCandidate = iteratorCount;
	 	            	}
	 	            	
	 	            	//Cria lista dos x melhores candidatos deste place
	 	            	if(topCandidatosDestePlace.size()==placerefs.gazetteer.Configurator.LIM_MAX_CANDIDATES_FEATURES){
	 	            		
//	 	            		for (Double d : topCandidatosDestePlace.values()) {
//								System.out.println("Value: " + d);
//							}
	 	            		
	 	            		HashMap<GazetteerEntry, Double> sortedHash = sortHashMap(topCandidatosDestePlace);
//	 	            		System.out.println("----SEPARADOR-----");
//	 	            		for (Double d : sortedHash.values()) {
//								System.out.println("Value: " + d);
//							}
	 	            		
	 	            		Double maiorDist = (Double)sortedHash.values().toArray()[sortedHash.values().size()-1];
	 	            		if(vc<maiorDist){
	 	            			topCandidatosDestePlace.remove(sortedHash.keySet().toArray()[sortedHash.keySet().size()-1]);
	 	            			topCandidatosDestePlace.put(nextPlace, vc);
	 	            		}
	 	            		
//	 	            		System.out.println("----SEPARADOR2-----");
//	 	            		for (Double d : topCandidatosDestePlace.values()) {
//								System.out.println("Value: " + d);
//							}
	 	            	}
	 	            	else{
	 	            		topCandidatosDestePlace.put(nextPlace, vc);
	 	            	}
	 	            	
	 	            }
					
					//Actualiza a lista de candidatos anteriores com os melhores candidatos gerados pelo corrente place
					for (GazetteerEntry e : topCandidatosDestePlace.keySet()){
						PLACEConstants.candidatesPlaceSameDoc.add(e); 
					}
					topCandidatosDestePlace.clear();
					
					//Acrescenta o melhor candidato dos PLACE anteriores do mesmo doc
					it = candidatos.iterator();
					iteratorCount = 0;
					while(it.hasNext()){
						iteratorCount++;
						nextPlace = it.next();
						if(iteratorCount==indexBestCandidate)
							PLACEConstants.bestCandidatePreviousPlaceSameDoc.add(nextPlace);
					}
					
					
					candidatosDestePlace.clear();
					cg.close();
				}

				
			} catch ( Exception e ) { 
				e.printStackTrace();
			}
			}
			
		} catch ( Exception e ) { e.printStackTrace(); }
	}
	
	@SuppressWarnings("unchecked")
	public static GazetteerEntry disambiguate ( String place, String phraseText, List<GazetteerEntry> candidates , Classifier model ) {
		double maxScore = Double.MAX_VALUE;
		GazetteerEntry bestCandidate = null;
		ArrayList<String> features;	
        GazetteerEntry candidatePlace;
        ArrayList<GazetteerEntry> candidatosDestePlace = new ArrayList<GazetteerEntry>();
        
        //variaveis para restringir o numero de candidatos deste place a usar nas features de places futuros
		HashMap<GazetteerEntry, Double> topCandidatosDestePlace = new HashMap<GazetteerEntry, Double>();
        
        System.out.println("#######################");
        System.out.println("Disambiguate!");
        
        //Descobre qual o melhor candidato e adiciona-o a lista para feature10
		int indexBestCandidate = 0;
		int iteratorCount = 0;
        
        try {
	
			Iterator<GazetteerEntry> it = candidates.iterator();
			
			System.out.println("PhraseText:" + phraseText);
			System.out.println("Place:" + place);
			
			
			
			// repeat steps below for each candidate disambiguation
	        while(it.hasNext()) {
	            	iteratorCount++;
	            	
	            	candidatePlace = it.next();
	            	candidatosDestePlace.add(candidatePlace);
	            	/*
	            	for (GazetteerEntry e : candidatosDestePlace){
	            		PLACEConstants.candidatesPlaceSameDoc.add(e);
					}
	            	*/
	            	
	            	features = PLACEFeatureGenerator.featuresGenerator(candidatePlace, phraseText, place);
	        
	            	System.out.println("------------------------");
	            	System.out.println("CandidatePlace:" + candidatePlace.name);
	            	
	    			double feature1 = Double.parseDouble(features.get(0));
	    			double feature2 = Double.parseDouble(features.get(1).replaceAll(" ", ""));
	    			double feature3 = Double.parseDouble(features.get(2));
	    			double feature4 = Double.parseDouble(features.get(3));
	    			double feature5 = Double.parseDouble(features.get(4));
	    			double feature6 = Double.parseDouble(features.get(5));
	    			double feature7 = Double.parseDouble(features.get(6));
	    			double feature8 = Double.parseDouble(features.get(7));
	    			double feature9 = Double.parseDouble(features.get(8));
	    			double feature10 = Double.parseDouble(features.get(9));
	    			
	    			System.out.println("Valor feature 1:" + feature1);
 	            	System.out.println("Valor feature 2:" + feature2);
 	            	System.out.println("Valor feature 3:" + feature3);
 	            	System.out.println("Valor feature 4:" + feature4);
 	            	System.out.println("Valor feature 5:" + feature5);
 	            	System.out.println("Valor feature 6:" + feature6);
 	            	System.out.println("Valor feature 7:" + feature7);
 	            	System.out.println("Valor feature 8:" + feature8);
 	            	System.out.println("Valor feature 9:" + feature9);
 	            	System.out.println("Valor feature 10:" + feature10);
	    			
	    			Instance instance = new Instance(10);
	    			instance.modifyValue(0, feature1);
	    			instance.modifyValue(1, feature2);
	    			instance.modifyValue(2, feature3);
	    			instance.modifyValue(3, feature4);
	    			instance.modifyValue(4, feature5);
	    			instance.modifyValue(5, feature6);
	    			instance.modifyValue(6, feature7);
	    			instance.modifyValue(7, feature8);
	    			instance.modifyValue(8, feature9);
	    			instance.modifyValue(9, feature10);
	    				    			
	    			double score = model.classifyInstance(instance);
	    			
	    			System.out.println("Score: " + score);
	    		
    				if ( score < maxScore ) {
    					maxScore = score;
    					bestCandidate = candidatePlace;
    					indexBestCandidate = iteratorCount;
    				}
	    		
    				
	    			System.out.println("MaxScore: " + maxScore);
	    			System.out.println("BestCandidate: " + bestCandidate.name);
	    			System.out.println("------------------------");
	    			
	    			//Cria lista dos x melhores candidatos deste place
 	            	if(topCandidatosDestePlace.size()==placerefs.gazetteer.Configurator.LIM_MAX_CANDIDATES_FEATURES){
 	            		
 	            		HashMap<GazetteerEntry, Double> sortedHash = sortHashMap(topCandidatosDestePlace);

 	            		
 	            		Double maiorDist = (Double)sortedHash.values().toArray()[sortedHash.values().size()-1];
 	            		if(score<maiorDist){
 	            			topCandidatosDestePlace.remove(sortedHash.keySet().toArray()[sortedHash.keySet().size()-1]);
 	            			topCandidatosDestePlace.put(candidatePlace, score);
 	            		}
 	            		
// 	            		System.out.println("----SEPARADOR2-----");
// 	            		for (Double d : topCandidatosDestePlace.values()) {
//							System.out.println("Value: " + d);
//						}
 	            	}
 	            	else{
 	            		topCandidatosDestePlace.put(candidatePlace, score);
 	            	}
	    			
	            }
	            
				for (GazetteerEntry e : topCandidatosDestePlace.keySet()){
					PLACEConstants.candidatesPlaceSameDoc.add(e); 
				}
				topCandidatosDestePlace.clear();
	            
	            //Acrescenta o melhor candidato dos PLACE anteriores do mesmo doc
				it = candidates.iterator();
				iteratorCount = 0;
				while(it.hasNext()){
					iteratorCount++;
					candidatePlace = it.next();
					if(iteratorCount==indexBestCandidate)
						PLACEConstants.bestCandidatePreviousPlaceSameDoc.add(candidatePlace);
				}
	            
	            
				candidatosDestePlace.clear();
	            	            
		} catch ( Exception e ) {
			e.printStackTrace();
		}
		
		return bestCandidate;
		

		
	}
	
	
	
	public static void main ( String args[] ) throws Exception {
		File svm = new File(outputPR+"/RegressionPlaceModel.svm");
		if(!svm.exists()){
			PLACEConstants.init();
			PLACEConstants.candidatesPlaceSameDoc.clear();
			
			File fileData = new File(outputPR+"/ranking-data.arff");
			
			//File fileData = File.createTempFile("ranking-data", ".arff");
			
			if(!fileData.exists()){
				PrintWriter o = new PrintWriter(new FileWriter(fileData.getAbsolutePath()));
			        o.println("@RELATION  place-learn-to-rank");
			        o.println("@ATTRIBUTE feature1      NUMERIC");
	        		o.println("@ATTRIBUTE feature2      NUMERIC");
	        		o.println("@ATTRIBUTE feature3      NUMERIC");
	        		o.println("@ATTRIBUTE feature4      NUMERIC");
	        		o.println("@ATTRIBUTE feature5      NUMERIC");
	        		o.println("@ATTRIBUTE feature6      NUMERIC");
		       		o.println("@ATTRIBUTE feature7      NUMERIC");
			       	o.println("@ATTRIBUTE feature8      NUMERIC");
			        o.println("@ATTRIBUTE feature9      NUMERIC");
			        o.println("@ATTRIBUTE feature10     NUMERIC");
			        o.println("@ATTRIBUTE distance      NUMERIC");
			        o.println("@DATA");

			        trainModel( new File(outputPR+"/place-train.xml"), o);
				o.close();
			}
	        //File fileModel = File.createTempFile("ranking-model", "tmp");
			File fileModel = new File(outputPR+"/ranking-model.tmp");
	        
	        //fileData.deleteOnExit();
	        //fileModel.deleteOnExit();

		System.out.println("fileModel path: "+fileModel.getAbsolutePath());
	        
		//String params2[] = { "-i", "-no-cv" , "-split-percentage", "100", "-K", "weka.classifiers.functions.supportVector.RBFKernel", "-t" , fileData.getAbsolutePath(), "-d" , fileModel.getAbsolutePath() };
		String params2[] = { "-i", "-no-cv" , "-K", "weka.classifiers.functions.supportVector.RBFKernel", "-t", fileData.getAbsolutePath(), "-d", fileModel.getAbsolutePath() };
	        SVMreg.main(params2);
		
		if(fileModel.exists()){
		Classifier model = readModel(fileModel.getAbsolutePath());
	        writeModel ( new File(outputPR+"/RegressionPlaceModel.svm"), model );
		}
		else{
		System.out.println("NAO CRIEI O MODELO!");
		}
	        //fileData.delete();
	        //fileModel.delete();
		}
	}

}
