package placerefs;

import java.util.ArrayList;
import placerefs.gazetteer.ConcaveHullBuilder;
import placerefs.gazetteer.CosineSimilarity;
import placerefs.gazetteer.KbEntity;
import placerefs.gazetteer.VincentyDistanceCalculator;
import com.aliasi.spell.EditDistance;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.PrecisionModel;

public class PLACEFeatureGenerator {
	
	
	public static double getConcaveHull(ArrayList<Point> points) throws Exception{
		
		return ConcaveHullBuilder.getConcaveHullGeo(points.toArray(new Point[0]),50d).getArea();	
	}
	
	public static double getConvexHull(ArrayList<Point> points) throws Exception{
	
		return new GeometryFactory().createMultiPoint(points.toArray(new Point[0])).convexHull().getArea();	
	}
	
	public static double getDistanceBetweenPoints(Point point1, Point point2) throws Exception {
		return VincentyDistanceCalculator.getDistance(point1,point2);
	}
	
	@SuppressWarnings("deprecation")
	public static ArrayList<String> featuresGenerator(KbEntity candidate, String phraseText, String placeText) throws Exception{
		
		ArrayList<String> arrayFeatures = new ArrayList<String>();
		double maxValue = Double.MAX_VALUE;
		double maxValuePhrase = Double.MAX_VALUE;
		double somaDistancias = 0;
		ArrayList<Point> list = new ArrayList<Point>();
		double latCandidate = Double.parseDouble(candidate.coordinates.split(" ")[0].replaceAll("N|E|S|W", ""));
		double longCandidate = Double.parseDouble(candidate.coordinates.split(" ")[1].replaceAll("N|E|S|W", "")); 
		
		//Feature 1
		if (candidate.population == null)
			arrayFeatures.add(new String("-1"));
		else
			arrayFeatures.add(candidate.population);
		
		//Feature 2
		if (candidate.area == null)
			arrayFeatures.add(new String("-1"));
		else{
			String[] split = (candidate.area).split("\\.");
			String str = "";
			
			if (split.length > 2){
				for (String st : split)
					str = str+st;
				arrayFeatures.add(str);
			}
			else
				arrayFeatures.add(candidate.area);
		}
		
		//Feature 3
		arrayFeatures.add(String.valueOf(candidate.wiki_text.split(" ").length));
		
		//Feature 4
		for (KbEntity e : PLACEConstants.candidatesPlaceSameDoc){
			Point p1 = new Point(new Coordinate(longCandidate,latCandidate), new PrecisionModel(),4326);
			Point p2 = new Point(new Coordinate(Double.parseDouble(e.coordinates.split(" ")[1].replaceAll("N|E|S|W", "")),Double.parseDouble(e.coordinates.split(" ")[0].replaceAll("N|E|S|W", ""))), new PrecisionModel(),4326);
			if (p1.distance(p2) < maxValue)
				maxValue = p1.distance(p2);
		}
		arrayFeatures.add(String.valueOf(maxValue));
		
		//Feature 5
		for (KbEntity e : PLACEConstants.candidatesPlaceSameDoc){
			Point p1 = new Point(new Coordinate(longCandidate,latCandidate), new PrecisionModel(),4326);
			Point p2 = new Point(new Coordinate(Double.parseDouble(e.coordinates.split(" ")[1].replaceAll("N|E|S|W", "")),Double.parseDouble(e.coordinates.split(" ")[0].replaceAll("N|E|S|W", ""))), new PrecisionModel(),4326);
			somaDistancias = somaDistancias + p1.distance(p2);
		}
		if (PLACEConstants.candidatesPlaceSameDoc.size() == 0)
			arrayFeatures.add(new String("-1"));
		else
			arrayFeatures.add(String.valueOf(somaDistancias/PLACEConstants.candidatesPlaceSameDoc.size()));
		
		//Feature 6
		for (KbEntity e : PLACEConstants.candidatesPlaceSameDoc){
			list.add(new Point(new Coordinate(Double.parseDouble(e.coordinates.split(" ")[1].replaceAll("N|E|S|W", "")),Double.parseDouble(e.coordinates.split(" ")[0].replaceAll("N|E|S|W", ""))), new PrecisionModel(),4326));
		}
		arrayFeatures.add(String.valueOf(new GeometryFactory().createMultiPoint(list.toArray(new	Point[0])).convexHull().getArea()));
		
		//Feature 7
		arrayFeatures.add(String.valueOf(ConcaveHullBuilder.getConcaveHullGeo(list.toArray(new Point[0]),50d).getArea()));
		
		//Feature 8
		if (candidate.wiki_text.equals("") || phraseText.equals(""))
			arrayFeatures.add(new String("-1"));
		else
			arrayFeatures.add(String.valueOf(CosineSimilarity.INSTANCE.getSimilarity(candidate.wiki_text, phraseText)));
		
		//Feature 9
		for (String s : candidate.altNames){
			if ((new EditDistance(true).distance(s, placeText)) < maxValuePhrase)
				maxValuePhrase = new EditDistance(true).distance(s, placeText);				
		}
		if ((new EditDistance(true).distance(candidate.name, placeText)) < maxValuePhrase)
			maxValuePhrase = new EditDistance(true).distance(candidate.name, placeText);
		if (candidate.altNames.length == 0)
			arrayFeatures.add(new String("-1"));
		else
			arrayFeatures.add(String.valueOf(maxValuePhrase));
			
		return arrayFeatures;
		
		
	}

}
