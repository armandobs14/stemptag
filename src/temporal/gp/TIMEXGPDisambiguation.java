package temporal.gp;

import java.io.File;
import java.io.FileReader;
import java.text.SimpleDateFormat;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

public class TIMEXGPDisambiguation {
		
	private static Program trainModel ( Map<String,Long> dates ) {
		ArrayList<DataPoint> data = new ArrayList<DataPoint>();
		for ( String str : dates.keySet() ) data.add( new DataPoint(str, new Long(dates.get(str))) );
		GeneticPrograming model = new GeneticPrograming( data.toArray(new DataPoint[0]) );
		ExecutorService executor = Executors.newFixedThreadPool(1);
		executor.execute(model);
		executor.shutdown();
		while (!executor.isTerminated()) { }
		Individual best = model.bestOfRunIndividual;
		return best.program;
	}
	
	public static Program trainModel ( File in ) {
		Map<String,Long> trainingData = new HashMap<String,Long>();
		try {
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
	        DocumentBuilder db = dbf.newDocumentBuilder();
	        InputSource is = new InputSource();
	        is.setCharacterStream(new FileReader(in));
	        Document doc = db.parse(is);
			XPath xpath = XPathFactory.newInstance().newXPath();
			XPathExpression expr = xpath.compile("//TIMEX2");
			NodeList elemList = (NodeList) expr.evaluate(doc, XPathConstants.NODESET);
			for ( int i = 0; i < elemList.getLength(); i++ ) try {
				String text = xpath.compile("./text()").evaluate(elemList.item(i));
				String val = xpath.compile("./@val").evaluate(elemList.item(i));
				trainingData.put(text , new SimpleDateFormat ("yyyy-MM-dd").parse(val).getTime());
			} catch ( Exception e ) { }
			Program program = trainModel(trainingData);
			return program;
		} catch ( Exception e ) { return null; }
	}
	
	public static long disambiguate ( String timex , Program i ) {
		long values[] = new DataPoint ( timex , 0).data;
		long result = (long)(i.eval(values));
		return result;
	}
	
	public static void main ( String args[] ) throws Exception {
		Program model = trainModel( new File("/Users/bmartins/Downloads/wikiwars/keyinline/01_WW2.key.xml") );
		long result = disambiguate("10th july 1944", model);
		String resultStr = new SimpleDateFormat ("yyyy-MM-dd").format(result);
		System.out.println(resultStr);
	}

}