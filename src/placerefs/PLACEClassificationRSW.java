package placerefs;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import temporal.CRFFeatureExtractor;

import com.aliasi.chunk.BioTagChunkCodec;
import com.aliasi.chunk.ChunkFactory;
import com.aliasi.chunk.TagChunkCodec;
import com.aliasi.corpus.Parser;
import com.aliasi.corpus.XValidatingObjectCorpus;
import com.aliasi.crf.ChainCrfFeatureExtractor;
import com.aliasi.crf.ChainCrfFeatures;

import com.aliasi.tag.Tagging;
import com.aliasi.tokenizer.IndoEuropeanTokenizerFactory;
import com.aliasi.tokenizer.Tokenizer;
import com.aliasi.tokenizer.TokenizerFactory;

public class PLACEClassificationRSW {

	private static String outputPR = "outputPlaceReferences";
	
	public static void createChunkingsFile(File corpus_train) throws IOException{
		
		File chunkingsFile = new File(outputPR+"/chunkings.xml");
		chunkingsFile.delete();
		
		BufferedWriter out = new BufferedWriter(new FileWriter(outputPR+"/chunkings.xml", true));
		out.append("<output>\n");
		out.flush();
		
		
		Parser parser = new PLACEChunkParser();
		
		
		XValidatingObjectCorpus corpus = new XValidatingObjectCorpus(0);
		parser.setHandler(corpus);
		parser.parse(corpus_train);
		
		out.append("</output>");
		out.close();
	}
	
	public static LinkedHashMap<String, Integer> getMostCommonFeatures(File chunkingsFile, int minTimesOccuredFeat) throws ParserConfigurationException, XPathExpressionException, SAXException, IOException{
		HashMap<String, Integer> listaFeaturesNames = new HashMap<String, Integer>();
		//HashMap<String, Integer> mapFeatureId = new HashMap<String, Integer>();
		LinkedHashMap<String, Integer> mapFeatureId = new LinkedHashMap<String, Integer>();
		
		TokenizerFactory factory = new IndoEuropeanTokenizerFactory();
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db = dbf.newDocumentBuilder();
		InputSource is = new InputSource();
        is.setCharacterStream(new FileReader(new File(outputPR+"/chunkings.xml")));
        Document doc = db.parse(is);
		XPath xpath = XPathFactory.newInstance().newXPath();
		NodeList chunkings = (NodeList) xpath.compile("//chunking").evaluate(doc, XPathConstants.NODESET);
		
		//For each chunking
		for (int n = 0; n < chunkings.getLength(); n++) {
			String mBuf = ((NodeList) xpath.compile("./mBuf").evaluate(chunkings.item(n), XPathConstants.NODESET)).item(0).getTextContent();
			NormalizedPlaceChunking chunking = new NormalizedPlaceChunking(mBuf);
			NodeList chunksInChunkings = (NodeList) xpath.compile("./chunks/chunk").evaluate(chunkings.item(n), XPathConstants.NODESET);
			
			//For each chunk in chunking
			for (int m = 0; m < chunksInChunkings.getLength(); m++) {
				Node node = chunksInChunkings.item(m);
				
				NormalizedPLACEChunk chunk = new NormalizedPLACEChunk(ChunkFactory.createChunk(
						Integer.parseInt((String)xpath.compile("./start").evaluate(node, XPathConstants.STRING)), 
						Integer.parseInt((String)xpath.compile("./end").evaluate(node, XPathConstants.STRING)), 
						((String) xpath.compile("./type").evaluate(node, XPathConstants.STRING)),
						0));
				chunk.setPlaceType((String) xpath.compile("./placeType").evaluate(node, XPathConstants.STRING));
				
				Double lat = (Double)xpath.compile("./latitude").evaluate(node, XPathConstants.NUMBER);
				if(!lat.isNaN())
					chunk.setLatitude(lat);
					
				Double lon = (Double)xpath.compile("./longitude").evaluate(node, XPathConstants.NUMBER);
				if(!lon.isNaN())
					chunk.setLongitude(lon);
				
				/*
				System.out.println("#################################");
				System.out.println("chunk start: " + chunk.start());
				System.out.println("chunk end: " + chunk.end());
				System.out.println("chunk type: " + chunk.type());
				System.out.println("chunk placeType: " + chunk.getPlaceType());
				
				try{
					System.out.println("chunk lat: " + chunk.getLatitude());
				}
				catch(NullPointerException npe){
					System.out.println("chunk lat: " + "null");
				}
				
				try{
					System.out.println("chunk lon: " + chunk.getLongitude());
				}
				catch(NullPointerException npe){
					System.out.println("chunk lon: " + "null");
				}
				
				System.out.println("chunk score: " + chunk.score());
				System.out.println("#################################");
				*/
				
				chunking.add(chunk);
			}
			
			TagChunkCodec tagCC = new BioTagChunkCodec(factory, true);
			System.out.println("########################################");
			//System.out.println("toTagging: " + tagCC.toTagging(chunking));
			System.out.println("phrase: " + chunking.toString());
			
			
//				HashSet<String> hs = new HashSet<String>();
//				hs.add("NAM");
//				hs.add("NOM");
//				
//				System.out.println("tagSet: " + tagCC.tagSet(hs));
			
			ChainCrfFeatureExtractor<String> featureExtractor = new CRFFeatureExtractor("models/POS/pos-en-general-brown.HiddenMarkovModel");
			ChainCrfFeatures<String> ccf = featureExtractor.extract(tagCC.toStringTagging(chunking).tokens(), tagCC.toStringTagging(chunking).tags());
			
			System.out.println("ccf nTag: " + ccf.numTags());
			System.out.println("ccf nTok: " + ccf.numTokens());
			
			for(int token = 0;token<ccf.numTokens();token++){
				Set<String> nodeFeaturesNames = ccf.nodeFeatures(token).keySet();
				System.out.println("token id: " + token);
				System.out.println("token: " + ccf.token(token));
				//System.out.println("ccf nF: " + nodeFeaturesNames);
				
				for (String string : nodeFeaturesNames){
					try{
						Integer nVezes = listaFeaturesNames.get(string);
						listaFeaturesNames.remove(string);
						//System.out.println("Vou meter: " + string + " com " + (nVezes+1) + " vezes.");
						listaFeaturesNames.put(string, nVezes+1);
					}
					catch(NullPointerException npe){
						//System.out.println("Vou meter: " + string + " com " + 1 + " vez.");
						listaFeaturesNames.put(string, 1);
					}
				}
			}
		}
			
		/*for (String string : listaFeaturesNames.keySet()) {
			if(listaFeaturesNames.get(string)>1)
				System.out.println(string + " " + listaFeaturesNames.get(string));
		}*/
		
		
		//atribui ids as features encontradas
		int n = 2;
		for (String string : listaFeaturesNames.keySet()) {
			if(listaFeaturesNames.get(string) > minTimesOccuredFeat){
				n++;
				System.out.println("is this already in the map? : " + mapFeatureId.containsKey(string));
				mapFeatureId.put(string, n);
				//System.out.println("So para ter a certeza: " + listaFeaturesNames.get(string));
			}
		}
		
		return mapFeatureId;
	}
	
	public static void createARFF(File rsw, File corpus_train, LinkedHashMap<String, Integer> featsId) throws IOException, ParserConfigurationException, XPathExpressionException, SAXException{
		
		for(String s : featsId.keySet())
			System.out.println("s: " + s + " id: " + featsId.get(s));
		
		BufferedWriter out = new BufferedWriter(new FileWriter(rsw, true));
		out.append("@relation RSWClassificationData\n");
		out.append("@attribute sequence_number numeric\n");
		out.append("@attribute element_number numeric\n");
		for(String string : featsId.keySet()){
			out.append("@attribute "+string+" numeric\n");
			out.flush();
		}
		out.append("@attribute class {O, B_NAM, I_NAM, B_NOM, I_NOM}\n");
		out.append("\n");
		out.append("@data\n");
		out.flush();
		
		int phraseId = 0;
		int wordId = 0;
		
		
		TokenizerFactory factory = new IndoEuropeanTokenizerFactory();
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db = dbf.newDocumentBuilder();
		InputSource is = new InputSource();
        is.setCharacterStream(new FileReader(new File(outputPR+"/chunkings.xml")));
        Document doc = db.parse(is);
		XPath xpath = XPathFactory.newInstance().newXPath();
		NodeList chunkings = (NodeList) xpath.compile("//chunking").evaluate(doc, XPathConstants.NODESET);
		
		//For each chunking
		for (int n = 0; n < chunkings.getLength(); n++) {
			phraseId++;
			wordId = 0;
			
			
			String mBuf = ((NodeList) xpath.compile("./mBuf").evaluate(chunkings.item(n), XPathConstants.NODESET)).item(0).getTextContent();
			NormalizedPlaceChunking chunking = new NormalizedPlaceChunking(mBuf);
			NodeList chunksInChunkings = (NodeList) xpath.compile("./chunks/chunk").evaluate(chunkings.item(n), XPathConstants.NODESET);
			
			//For each chunk in chunking
			for (int m = 0; m < chunksInChunkings.getLength(); m++) {
				Node node = chunksInChunkings.item(m);
				
				NormalizedPLACEChunk chunk = new NormalizedPLACEChunk(ChunkFactory.createChunk(
						Integer.parseInt((String)xpath.compile("./start").evaluate(node, XPathConstants.STRING)), 
						Integer.parseInt((String)xpath.compile("./end").evaluate(node, XPathConstants.STRING)), 
						((String) xpath.compile("./type").evaluate(node, XPathConstants.STRING)),
						0));
				chunk.setPlaceType((String) xpath.compile("./placeType").evaluate(node, XPathConstants.STRING));
				
				Double lat = (Double)xpath.compile("./latitude").evaluate(node, XPathConstants.NUMBER);
				if(!lat.isNaN())
					chunk.setLatitude(lat);
					
				Double lon = (Double)xpath.compile("./longitude").evaluate(node, XPathConstants.NUMBER);
				if(!lon.isNaN())
					chunk.setLongitude(lon);
				
				/*
				System.out.println("#################################");
				System.out.println("chunk start: " + chunk.start());
				System.out.println("chunk end: " + chunk.end());
				System.out.println("chunk type: " + chunk.type());
				System.out.println("chunk placeType: " + chunk.getPlaceType());
				
				try{
					System.out.println("chunk lat: " + chunk.getLatitude());
				}
				catch(NullPointerException npe){
					System.out.println("chunk lat: " + "null");
				}
				
				try{
					System.out.println("chunk lon: " + chunk.getLongitude());
				}
				catch(NullPointerException npe){
					System.out.println("chunk lon: " + "null");
				}
				
				System.out.println("chunk score: " + chunk.score());
				System.out.println("#################################");
				*/
				
				chunking.add(chunk);
			}
			
			TagChunkCodec tagCC = new BioTagChunkCodec(factory, true);
			//System.out.println("########################################");
			//System.out.println("toTagging: " + tagCC.toTagging(chunking));
			//System.out.println("phrase: " + chunking.toString());
			
			
			ChainCrfFeatureExtractor<String> featureExtractor = new CRFFeatureExtractor("models/POS/pos-en-general-brown.HiddenMarkovModel");
			ChainCrfFeatures<String> ccf = featureExtractor.extract(tagCC.toStringTagging(chunking).tokens(), tagCC.toStringTagging(chunking).tags());
			
			for(int token = 0;token<ccf.numTokens();token++){
				ArrayList<Integer> featsIdScore = new ArrayList<Integer>();
				wordId++;
				
				Set<String> nodeFeaturesNames = ccf.nodeFeatures(token).keySet();
				//System.out.println("token id: " + token);
				//System.out.println("token: " + ccf.token(token));
				//System.out.println("ccf nF: " + nodeFeaturesNames);
				
				for (String string : nodeFeaturesNames){
					//System.out.println("phrase: " + phraseId + " word: " + wordId + " nodeFeature: " + string);
					
					Integer featID = featsId.get(string);
					//System.out.println("featID: " + featID);
					if(featID!=null)
						featsIdScore.add(featID);
					
				}
				
				Collections.sort(featsIdScore);
				
				String newLineData = "{1 " + phraseId + ", 2 " + wordId + ", ";
				Iterator<Integer> it = featsIdScore.iterator();
				
				while(it.hasNext()){
					newLineData += it.next() + " 1.0, ";
				}
				
				newLineData += (featsId.keySet().size()+3) + " " + tagCC.toTagging(chunking).tag(token) + "}";
				
				System.out.println("vou escrever o token: " + ccf.token(token) + " com a tag: " + tagCC.toTagging(chunking).tag(token) + " nesta linha: " + newLineData);
				out.append(newLineData+"\n");
				out.flush();
			}
		}
		out.close();
	}
	
	public static void main ( String args[] ) throws Exception {
		File rsw = new File(outputPR+"/ClassificationRSW.arff");
		File in = new File(args[0]);
		
		if(!rsw.exists()){
			PLACEClassificationRSW.createChunkingsFile(in);
			LinkedHashMap<String, Integer> featsId = PLACEClassificationRSW.getMostCommonFeatures(new File(outputPR+"/chunkings.xml"), 30);
			PLACEClassificationRSW.createARFF(rsw, in, featsId);
		}
	}
	
}
