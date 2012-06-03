package placerefs;

import java.util.ArrayList;
import placerefs.gazetteer.ConcaveHullBuilder;
import placerefs.gazetteer.CosineSimilarity;
import placerefs.gazetteer.GazetteerEntry;
import placerefs.gazetteer.VincentyDistanceCalculator;
import com.aliasi.spell.EditDistance;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.PrecisionModel;

public class PLACEFeatureGenerator {
	
	private static boolean debug = false;
	
	public static double getConcaveHull(ArrayList<Point> points) throws Exception{
		
		return ConcaveHullBuilder.getConcaveHullGeo(points.toArray(new Point[0]),50d).getArea();	
	}
	
	public static double getConvexHull(ArrayList<Point> points) throws Exception{
	
		return new GeometryFactory().createMultiPoint(points.toArray(new Point[0])).convexHull().getArea();	
	}
	
	public static double getDistanceBetweenPoints(Point point1, Point point2) throws Exception {
		return VincentyDistanceCalculator.getDistance(point1,point2);
	}
	
	public static void getFeaturePopulation(GazetteerEntry candidate, ArrayList<String> arrayFeatures){
		
		if(debug){
			System.out.println("++++++++++++++");
			System.out.println("Feature 1!");
		}
		//Feature 1
		if (candidate.population == null)
			arrayFeatures.add(new String("-1"));
		else
			arrayFeatures.add(candidate.population);
			
		if(debug){	
			System.out.println("CANDIDATE Population: " + arrayFeatures.get(0));
			System.out.println("++++++++++++++");
		}
	}
	
	public static void getFeatureArea(GazetteerEntry candidate, ArrayList<String> arrayFeatures){
		if(debug){
			System.out.println("++++++++++++++");
			System.out.println("Feature 2!");
		}
		if (candidate.area == null)
			arrayFeatures.add(new String("-1"));
		else{
			String[] split = (candidate.area).split("\\.");
			String str = "";
			
			if(debug)
				System.out.println("SPLIT AREA: " + candidate.area);
			
			if (split.length > 2){
				for (String st : split)
					str = str+st;
				
				if(debug)
					System.out.println("str dentro if: " + str);
						
				arrayFeatures.add(str);
			}
			else
				arrayFeatures.add(candidate.area);
		}
		if(debug){
			System.out.println("CANDIDATE Area: " + arrayFeatures.get(1));
			System.out.println("++++++++++++++");
		}
	}
	
	public static void getFeatureNumWordsText(GazetteerEntry candidate, ArrayList<String> arrayFeatures){
		if(debug){
			System.out.println("++++++++++++++");
			System.out.println("Feature 3!");
		}
		
		arrayFeatures.add(String.valueOf(candidate.wiki_text.split(" ").length));
		
		if(debug){
			System.out.println("CANDIDATE Wiki_Text Length: " + arrayFeatures.get(2));
			System.out.println("++++++++++++++");
		}
	}
	
	public static void getFeatureVCDCandidatesSameDoc(GazetteerEntry candidate, ArrayList<String> arrayFeatures, double latCandidate, double longCandidate){
		double maxValue = Double.MAX_VALUE;
		
		if(debug){
			System.out.println("++++++++++++++");
			System.out.println("Feature 4!");
			System.out.println("SIZE CANDIDATESPLACESAMEDOC: "+PLACEConstants.candidatesPlaceSameDoc.size());
		}
		
		int i = 1;
		for (GazetteerEntry e : PLACEConstants.candidatesPlaceSameDoc){
			if(debug)
				System.out.println("CANDIDATO NR " + i);
			
			i++;
			
			Point p1 = new Point(new Coordinate(longCandidate,latCandidate), new PrecisionModel(),4326);
			Point p2 = new Point(new Coordinate(Double.parseDouble(e.coordinates.split(" ")[1].replaceAll("N|E|S|W", "")),Double.parseDouble(e.coordinates.split(" ")[0].replaceAll("N|E|S|W", ""))), new PrecisionModel(),4326);
			
			if(debug){
				System.out.println("POINT P1 : " + p1.toString());
				System.out.println("POINT P2 : " + p2.toString());
				System.out.println("VDC DISTANCE: " + VincentyDistanceCalculator.getDistance(p1, p2));
				System.out.println("MAXVALUE: " + maxValue);
			}
			/*
			if (p1.distance(p2) < maxValue)
				maxValue = p1.distance(p2);
				*/
			
			
			
			if(VincentyDistanceCalculator.getDistance(p1, p2)< maxValue)
				maxValue = VincentyDistanceCalculator.getDistance(p1, p2);
			
			if(debug)
				System.out.println("NOVO MAXVALUE: " + maxValue);
			
		}
		if(maxValue==Double.MAX_VALUE)
			arrayFeatures.add(new String("-1"));
		else
			arrayFeatures.add(String.valueOf(maxValue));
		
		if(debug){
			System.out.println("CANDIDATE MAX VALUE VCD PARA OUTROS CANDIDATOS SAME DOC: " + arrayFeatures.get(3));
			System.out.println("++++++++++++++");
		}
	}
	
	public static void getFeatureDistanceAvg(GazetteerEntry candidate, ArrayList<String> arrayFeatures, double latCandidate, double longCandidate){
		double somaDistancias = 0;
		
		if(debug){
			System.out.println("++++++++++++++");
			System.out.println("Feature 5!");
			System.out.println("SIZE CANDIDATESPLACESAMEDOC: "+PLACEConstants.candidatesPlaceSameDoc.size());
		}
		
		int i = 1;
		for (GazetteerEntry e : PLACEConstants.candidatesPlaceSameDoc){
			if(debug)
				System.out.println("CANDIDATO NR " + i);
			
			i++;
			
			Point p1 = new Point(new Coordinate(longCandidate,latCandidate), new PrecisionModel(),4326);
			Point p2 = new Point(new Coordinate(Double.parseDouble(e.coordinates.split(" ")[1].replaceAll("N|E|S|W", "")),Double.parseDouble(e.coordinates.split(" ")[0].replaceAll("N|E|S|W", ""))), new PrecisionModel(),4326);
			
			if(debug){
				System.out.println("POINT P1 : " + p1.toString());
				System.out.println("POINT P2 : " + p2.toString());
			}
			
			//somaDistancias = somaDistancias + p1.distance(p2);
			somaDistancias = somaDistancias + VincentyDistanceCalculator.getDistance(p1, p2);
			if(debug)
				System.out.println("SOMA DISTANCIAS ATE AGR (VCD) : " + p2.toString());
		}
		if (PLACEConstants.candidatesPlaceSameDoc.size() == 0)
			arrayFeatures.add(new String("-1"));
		else
			arrayFeatures.add(String.valueOf(somaDistancias/PLACEConstants.candidatesPlaceSameDoc.size()));
		
		if(debug){
			System.out.println("CANDIDATE somaDist/sizePLACESameDoc: " + arrayFeatures.get(4));
			System.out.println("++++++++++++++");
		}
	}
	
	public static void getFeatureConvexHull(GazetteerEntry candidate, ArrayList<String> arrayFeatures, ArrayList<Point> list){
		if(debug){
			System.out.println("++++++++++++++");
			System.out.println("Feature 6!");
			System.out.println("SIZE CANDIDATESPLACESAMEDOC: "+PLACEConstants.candidatesPlaceSameDoc.size());
		}
		
		for (GazetteerEntry e : PLACEConstants.candidatesPlaceSameDoc){
			list.add(new Point(new Coordinate(Double.parseDouble(e.coordinates.split(" ")[1].replaceAll("N|E|S|W", "")),Double.parseDouble(e.coordinates.split(" ")[0].replaceAll("N|E|S|W", ""))), new PrecisionModel(),4326));
		}
		arrayFeatures.add(String.valueOf(new GeometryFactory().createMultiPoint(list.toArray(new Point[0])).convexHull().getArea()));
		
		if(debug){
			System.out.println("CANDIDATE Valor Area CONVEXA: " + arrayFeatures.get(5));
			System.out.println("++++++++++++++");
		}
	}
	
	public static void getFeatureConcaveHull(GazetteerEntry candidate, ArrayList<String> arrayFeatures, ArrayList<Point> list) throws Exception{
		if(debug){
			System.out.println("++++++++++++++");
			System.out.println("Feature 7!");
		}
		arrayFeatures.add(String.valueOf(ConcaveHullBuilder.getConcaveHullGeo(list.toArray(new Point[0]),50d).getArea()));
		if(debug){
			System.out.println("CANDIDATE Valor Area CONCAVA: " + arrayFeatures.get(6));
			System.out.println("++++++++++++++");
		}
	}
	
	public static void getFeatureCosineSimilarityTextPhrase(GazetteerEntry candidate, ArrayList<String> arrayFeatures, String phraseText){
	
		if(debug){
			System.out.println("++++++++++++++");
			System.out.println("Feature 8!");
		}
		if (candidate.wiki_text.equals("") || phraseText.equals(""))
			arrayFeatures.add(new String("-1"));
		else{
			Double x = CosineSimilarity.INSTANCE.getSimilarity(candidate.wiki_text, phraseText);
			arrayFeatures.add(String.valueOf(x));
		}
		
		if(debug){
			System.out.println("CANDIDATE COSINE SIMILARITY entre wiki_text candidato e phraseText: " + arrayFeatures.get(7));
			System.out.println("++++++++++++++");
		}
		
	}
	
	public static void getFeatureEditDistance(GazetteerEntry candidate, ArrayList<String> arrayFeatures, String placeText){
		double maxValuePhrase = Double.MAX_VALUE;
		
		if(debug){
			System.out.println("++++++++++++++");
			System.out.println("Feature 9!");
			
			System.out.println("SIZE ALTNAMES DO CANDIDATO: " + candidate.altNames.length);
			System.out.println("MAXVALUEPHRASE: " + maxValuePhrase);
		}
		for (String s : candidate.altNames){
			if(debug)
				System.out.println("DISTANCIA ENTRE ALTNAME " + s + " e PLACETEXT: " + new EditDistance(true).distance(s, placeText));
			
			if ((new EditDistance(true).distance(s, placeText)) < maxValuePhrase)
				maxValuePhrase = new EditDistance(true).distance(s, placeText);				
		}
		
		if(debug){
			System.out.println("MIDDLE MAXVALUEPHRASE: " + maxValuePhrase);
			System.out.println("DISTANCIA ENTRE CANDIDATE NAME " + candidate.name + " e PLACETEXT: " + new EditDistance(true).distance(candidate.name, placeText));
		}
		
		if ((new EditDistance(true).distance(candidate.name, placeText)) < maxValuePhrase)
			maxValuePhrase = new EditDistance(true).distance(candidate.name, placeText);
		if (candidate.altNames.length == 0)
			arrayFeatures.add(new String("-1"));
		else
			arrayFeatures.add(String.valueOf(maxValuePhrase));
			
		if(debug){
			System.out.println("MENOR EDITDISTANCE ENTRE NOMES: " + arrayFeatures.get(8));
			System.out.println("++++++++++++++");
		}
	}
	
	public static void getFeatureVCDBestCandidatesPreviousPlaceSameDoc(GazetteerEntry candidate, ArrayList<String> arrayFeatures, double latCandidate, double longCandidate){
		int i = 1;
		Double maxValue = Double.MAX_VALUE;
		
		if(debug){
			System.out.println("++++++++++++++");
			System.out.println("Feature 10!");
			System.out.println("Size bestCandidates: " + PLACEConstants.bestCandidatePreviousPlaceSameDoc.size());
		}
		
		for (GazetteerEntry e : PLACEConstants.bestCandidatePreviousPlaceSameDoc){
			if(debug)
				System.out.println("CANDIDATO NR " + i);
			i++;
			
			Point p1 = new Point(new Coordinate(longCandidate,latCandidate), new PrecisionModel(),4326);
			Point p2 = new Point(new Coordinate(Double.parseDouble(e.coordinates.split(" ")[1].replaceAll("N|E|S|W", "")),Double.parseDouble(e.coordinates.split(" ")[0].replaceAll("N|E|S|W", ""))), new PrecisionModel(),4326);
			
			if(debug){
				System.out.println("POINT P1 : " + p1.toString());
				System.out.println("POINT P2 : " + p2.toString());
				System.out.println("VDC DISTANCE: " + VincentyDistanceCalculator.getDistance(p1, p2));
				System.out.println("MAXVALUE: " + maxValue);
			}
			
			if(VincentyDistanceCalculator.getDistance(p1, p2)< maxValue)
				maxValue = VincentyDistanceCalculator.getDistance(p1, p2);
			
			if(debug)
				System.out.println("NOVO MAXVALUE: " + maxValue);
			
		}
		if(maxValue==Double.MAX_VALUE)
			arrayFeatures.add(new String("0"));
		else
			arrayFeatures.add(String.valueOf(maxValue));
		
		if(debug){
			System.out.println("CANDIDATE MAX VALUE VCD PARA MELHORES CANDIDATOS SAME DOC: " + arrayFeatures.get(9));
			System.out.println("++++++++++++++");
		}
	}
	
	@SuppressWarnings("deprecation")
	public static ArrayList<String> featuresGenerator(GazetteerEntry candidate, String phraseText, String placeText) throws Exception{
		
		//System.out.println("phrase_text: "+ phraseText);
		ArrayList<String> arrayFeatures = new ArrayList<String>();
		ArrayList<Point> list = new ArrayList<Point>();
		double latCandidate = Double.parseDouble(candidate.coordinates.split(" ")[0].replaceAll("N|E|S|W", ""));
		double longCandidate = Double.parseDouble(candidate.coordinates.split(" ")[1].replaceAll("N|E|S|W", ""));
		
		
		getFeaturePopulation(candidate, arrayFeatures);
		getFeatureArea(candidate, arrayFeatures);
		getFeatureNumWordsText(candidate, arrayFeatures);
		getFeatureVCDCandidatesSameDoc(candidate, arrayFeatures, latCandidate, longCandidate);
		getFeatureDistanceAvg(candidate, arrayFeatures, latCandidate, longCandidate);
		getFeatureConvexHull(candidate, arrayFeatures, list);
		getFeatureConcaveHull(candidate, arrayFeatures, list);
		getFeatureCosineSimilarityTextPhrase(candidate, arrayFeatures, phraseText);
		getFeatureEditDistance(candidate, arrayFeatures, placeText);
		getFeatureVCDBestCandidatesPreviousPlaceSameDoc(candidate, arrayFeatures, latCandidate, longCandidate);
		
		
		
		return arrayFeatures;
	}

}
