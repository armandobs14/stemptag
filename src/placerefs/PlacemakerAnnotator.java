package placerefs;

import java.io.*;
import java.util.*;
import java.net.*;
import javax.xml.parsers.*;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.*;
import org.w3c.dom.*;
import org.xml.sax.*;
import com.aliasi.chunk.Chunk;
import com.aliasi.chunk.ChunkFactory;
import com.aliasi.chunk.Chunker;
import com.aliasi.chunk.Chunking;
import com.aliasi.chunk.ChunkingImpl;
import com.aliasi.sentences.IndoEuropeanSentenceModel;
import com.aliasi.sentences.SentenceChunker;
import com.aliasi.tokenizer.IndoEuropeanTokenizerFactory;

/**
 * Java wrapper over the Yahoo! Placemaker Web service
 */
public class PlacemakerAnnotator implements Chunker {

   private static String appid = "AVNVvo3V34EOqIaAO7Uo.CrlQeGg8Ss43EhQfPm0HMZjqnkSUtA2MkhAiTkQ6T3XE6FWGg--";
	
   public Chunking chunk(CharSequence cSeq) { 
	   return chunk(cSeq.toString().toCharArray(),0,cSeq.length());
   }
   
   public Chunking chunk(char[] cs, int start, int end) { 
	    String s = new String(cs,start,end);
	    ChunkingImpl chunks = new ChunkingImpl(s);
    	try {
    		List<NormalizedPLACEChunk> refs = callPlacemaker( s );
    		for ( NormalizedPLACEChunk ref : refs ) chunks.add(ref);
    	} catch ( Exception ex ) {
    		ex.printStackTrace();
    	}
    	return chunks;
   }

   private List<NormalizedPLACEChunk> callPlacemaker ( String str ) throws Exception {
	   List<NormalizedPLACEChunk> lst = new ArrayList<NormalizedPLACEChunk>();
       Document doc = httpPost("http://wherein.yahooapis.com/v1/document" , 
                      new String[]{ "documentContent", str, "documentType", "text/plain", "appid", appid } ); 
       XPath xpath = XPathFactory.newInstance().newXPath();
       NodeList elemList = (NodeList) xpath.compile("//*[name()='reference']").evaluate(doc, XPathConstants.NODESET);
       for ( int i = 0; i < elemList.getLength(); i++ ) {
	   String id = xpath.compile("./*[name()='woeIds']").evaluate(elemList.item(i));
       String sText = xpath.compile("./*[name()='text']").evaluate(elemList.item(i));
       Integer iStart = new Integer(xpath.compile("./*[name()='start']").evaluate(elemList.item(i)));
       String type = xpath.compile("//*[name()='place' and .//*[name()='woeId']='" + id + "']//*[name()='type']").evaluate(doc);
       Double lat = new Double(xpath.compile("//*[name()='place' and .//*[name()='woeId']='" + id + "']//*[name()='centroid']/*[name()='latitude']").evaluate(doc));
	   Double lon = new Double(xpath.compile("//*[name()='place' and .//*[name()='woeId']='" + id + "']//*[name()='centroid']/*[name()='longitude']").evaluate(doc));
       NormalizedPLACEChunk chunk = new NormalizedPLACEChunk( 
                                                ChunkFactory.createChunk(iStart, iStart + sText.length(), "PLACE" ) 
                                    );
	   chunk.setLatitude(lat);
	   chunk.setLongitude(lon);
       chunk.setType(type);
	   lst.add( chunk );
       }
       return lst;
   }

   private static Document httpPost ( String location, String[] params ) {
		try {
			String data = "";
			for ( int i = 0; i < params.length; i++) {
				if ( data.length() > 0 ) data += "&";
				data += URLEncoder.encode(params[i], "UTF-8") + "=" + URLEncoder.encode(params[++i], "UTF-8");
			}		
			URL url = new URL(location);
			URLConnection conn = url.openConnection ();
		    	conn.setDoOutput(true);
		    	OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream());
		    	wr.write(data);
		    	wr.flush();
			BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
			StringBuffer sb = new StringBuffer();
			while ( ( data = rd.readLine() ) != null ) sb.append(data);
			rd.close();
			if ( Class.forName("org.apache.xerces.parsers.StandardParserConfiguration") != null) {
		    		System.setProperty("org.apache.xerces.xni.parser.XMLParserConfiguration", "org.apache.xerces.parsers.StandardParserConfiguration");
 	        }
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
	        	DocumentBuilder db = dbf.newDocumentBuilder();
	        	InputSource is = new InputSource();
	        	is.setCharacterStream(new StringReader(sb.toString()));
	        	Document doc = db.parse(is);
			return doc;
		} catch ( Exception e ) { e.printStackTrace(); return null; }
   }

}