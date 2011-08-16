package placerefs;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import placerefs.gazetteer.CandidateGenerator;
import placerefs.gazetteer.KbEntity;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.PrecisionModel;
import weka.classifiers.Classifier;
import weka.core.Instance;
import weka.classifiers.functions.SVMreg;

public class PLACERegressionDisambiguation {
		
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
			List<KbEntity> candidatos = new ArrayList<KbEntity>();
			ArrayList<KbEntity> candidatosDestePlace = new ArrayList<KbEntity>();
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
				
				PLACEConstants.candidatesPlaceSameDoc.clear();
				System.out.println("doc########################################################");
	    		PLACEConstants.isFirstPlace = true;
	    		NodeList elemList = (NodeList) xpath.compile("./p").evaluate(docs.item(n), XPathConstants.NODESET);
	    		
			//For every phrase in the document
			for ( int i = 0; i < elemList.getLength(); i++ ) try {
				NodeList placeList = (NodeList) xpath.compile("./PLACE").evaluate(elemList.item(i), XPathConstants.NODESET);
				String phraseText = xpath.compile("./text()").evaluate(elemList.item(i));

				//For every PLACE tag in the phrase
				for ( int j = 0; j < placeList.getLength(); j++ ){
					String latLong = xpath.compile("./@latLong").evaluate(placeList.item(j));
					String placeText = xpath.compile("./text()").evaluate(placeList.item(j));
					System.out.println(placeText);
					
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
					Iterator<KbEntity> it = candidatos.iterator();
					KbEntity nextPlace;
					
					//Computes for each candidate its features
					while(it.hasNext()) {
						String dataToPrint = "";
						nextPlace = it.next();
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
	 	            	
	 	            	//Prints data to the temporary file
	 	            	for (int m = 0; m < data.size(); m++)
	 	            		dataToPrint = dataToPrint+data.get(m)+",";
	 	            	
	 	            	Point p1 = new Point(new Coordinate(CorrectlonAttribute,CorrectlatAttribute), new PrecisionModel(),4326);
	 	            	Point p2 = new Point(new Coordinate(Double.parseDouble(nextPlace.coordinates.split(" ")[1].replaceAll("N|E|S|W", "")),Double.parseDouble(nextPlace.coordinates.split(" ")[0].replaceAll("N|E|S|W", ""))), new PrecisionModel(),4326);

	 	            	dataToPrint = dataToPrint+p1.distance(p2); 	            	
	 	            	o.println(dataToPrint);
	 	            	data.clear();
	 	            	
	 	            }
					
					//Actualiza a lista de candidatos anteriores com os candidatos gerados pelo corrente place
					for (KbEntity e : candidatosDestePlace){
						PLACEConstants.candidatesPlaceSameDoc.add(e); 
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
	public static KbEntity disambiguate ( String place, String phraseText, List<KbEntity> candidates , Classifier model ) {
		double maxScore = Double.MAX_VALUE;
		KbEntity bestCandidate = null;
		ArrayList<String> features;	
        KbEntity candidatePlace;
        ArrayList<KbEntity> candidatosDestePlace = new ArrayList<KbEntity>();
        
        try {
	
			Iterator<KbEntity> it = candidates.iterator();

			// repeat steps below for each candidate disambiguation
	            while(it.hasNext()) {
	            	candidatePlace = it.next();
	            	candidatosDestePlace.add(candidatePlace);
	            	features = PLACEFeatureGenerator.featuresGenerator(candidatePlace, phraseText, place);
	        
	    			double feature1 = Double.parseDouble(features.get(0));
	    			double feature2 = Double.parseDouble(features.get(1).replaceAll(" ", ""));
	    			double feature3 = Double.parseDouble(features.get(2));
	    			double feature4 = Double.parseDouble(features.get(3));
	    			double feature5 = Double.parseDouble(features.get(4));
	    			double feature6 = Double.parseDouble(features.get(5));
	    			double feature7 = Double.parseDouble(features.get(6));
	    			double feature8 = Double.parseDouble(features.get(7));
	    			double feature9 = Double.parseDouble(features.get(8));
	    			Instance instance = new Instance(9);
	    			instance.modifyValue(0, feature1);
	    			instance.modifyValue(1, feature2);
	    			instance.modifyValue(2, feature3);
	    			instance.modifyValue(3, feature4);
	    			instance.modifyValue(4, feature5);
	    			instance.modifyValue(5, feature6);
	    			instance.modifyValue(6, feature7);
	    			instance.modifyValue(7, feature8);
	    			instance.modifyValue(8, feature9);
	    				    			
	    			double score = model.classifyInstance(instance);
	    			
	    			if ( score < maxScore ) {
	    				maxScore = score;
	    				bestCandidate = candidatePlace;
	    			}
	            	
	            }
	            
	            for (KbEntity e : candidatosDestePlace){
					PLACEConstants.candidatesPlaceSameDoc.add(e); 
				}
				
				candidatosDestePlace.clear();
	            	            
		} catch ( Exception e ) {
			e.printStackTrace();
		}
		
		return bestCandidate;
		

		
	}
	
	
	
	public static void main ( String args[] ) throws Exception {
		PLACEConstants.init();
		PLACEConstants.candidatesPlaceSameDoc.clear();
		File fileData = File.createTempFile("ranking-data", ".arff");
		System.out.println(fileData.getAbsolutePath());
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
        o.println("@ATTRIBUTE distance      NUMERIC");
        o.println("@DATA");
        
        trainModel( new File("/Users/vitorloureiro/Desktop/Geo-Temporal/place-train.xml"), o);
		o.close();
		
        File fileModel = File.createTempFile("ranking-model", "tmp");
        fileData.deleteOnExit();
        fileModel.deleteOnExit();
        String params2[] = { "-i", "-no-cv" , "-split-percentage", "99", "-K", "weka.classifiers.functions.supportVector.RBFKernel", "-t" , fileData.getAbsolutePath(), "-d" , fileModel.getAbsolutePath() };
        SVMreg.main(params2);
        Classifier model = readModel(fileModel.getAbsolutePath());
        writeModel ( new File("/Users/vitorloureiro/Desktop/Geo-Temporal/geoModels/RegressionPlaceModel.svm"), model );
        fileData.delete();
        fileModel.delete();
	}

}
