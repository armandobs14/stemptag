package placerefs;

import com.aliasi.chunk.Chunking;
import com.aliasi.chunk.ChunkFactory;
import com.aliasi.chunk.ChunkingImpl;
import com.aliasi.corpus.ObjectHandler;
import com.aliasi.corpus.XMLParser;
import com.aliasi.tokenizer.TokenizerFactory;
import com.aliasi.xml.DelegatingHandler;


import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import org.xml.sax.Attributes;
import org.xml.sax.helpers.DefaultHandler;

public class PLACEChunkParser extends XMLParser<ObjectHandler> {

	private static final long serialVersionUID = 6281374154299530460L;
	  
	private TokenizerFactory tokenizer;
	private static int contador = 0;
	
	String mSentenceTag = "p";
    
    public PLACEChunkParser() {
        super();
    }
    
    public PLACEChunkParser(TokenizerFactory factory) {
        super();
        tokenizer = factory;
    }

    public PLACEChunkParser(ObjectHandler handler) {
        super(handler);
    }

    @Override
    protected DefaultHandler getXMLHandler() {
        return new MucHandler(getHandler());
    }

    public void setSentenceTag(String tag) {
        mSentenceTag = tag;
    }

    class MucHandler extends DelegatingHandler {
        ObjectHandler mChunkHandler;
        SentenceHandler mSentHandler;
        MucHandler(ObjectHandler chunkHandler) {
            mChunkHandler = chunkHandler;
            mSentHandler = new SentenceHandler();
            setDelegate(mSentenceTag,mSentHandler);
        }
        @Override
        public void finishDelegate(String qName, DefaultHandler handler) {
            Chunking chunking = mSentHandler.getChunking();
            mChunkHandler.handle(chunking);
        }
    }

    static class SentenceHandler extends DefaultHandler {
        StringBuilder mBuf;
        String mType;
        String typeAttribute;
        String placeTypeAttribute;
        Double latAttribute;
        Double lonAttribute;
        int mStart;
        int mEnd;
        final List<NormalizedPLACEChunk> mChunkList = new ArrayList<NormalizedPLACEChunk>();
        SentenceHandler() { }
        
        @Override
        public void startDocument() {
            mBuf = new StringBuilder();
            mChunkList.clear();
        }
        @Override
        public void startElement(String uri, String localName, String qName, Attributes attributes) {
        	
        	if (attributes.getValue(0) != null && attributes.getValue(0).equals("true")){
        		//limpa no inicio de cada documento
        		System.out.println("candidatesPlaceSameDoc: " + PLACEConstants.candidatesPlaceSameDoc.size());
        		System.out.println("bestCandidatePreviousPlaceSameDoc: " + PLACEConstants.bestCandidatePreviousPlaceSameDoc.size());
        		
        		PLACEConstants.candidatesPlaceSameDoc.clear();
        		PLACEConstants.bestCandidatePreviousPlaceSameDoc.clear();
        		
        	}
        	
        	//TODO: VER COMO ITERAR PRIMEIRO SOBRE TODOS OS PLACES DO DOC ANTES DO PARSER
        	System.out.println("getValue(0): " + attributes.getValue(0));
    		System.out.println("form: " + attributes.getValue("form"));
    		System.out.println("type: " + attributes.getValue("type"));
        	
        	
            if (!"PLACE".equals(qName)) return;
            int size;
            if (attributes.getValue("form") != null && attributes.getValue("form").equals("NAM")){
            	mType = "NAM";//TODO: attributes.getValue("TYPE");
            	PLACEChunkParser.contador++;
            	System.out.println("contador: "+PLACEChunkParser.contador);
            }
            else{
            	mType = "NOM";
            }
            	
            mStart = mBuf.length();
            typeAttribute = attributes.getValue("type");
            if (attributes.getValue("latLong") != null){
            	size = attributes.getValue("latLong").replaceAll("�", "").replaceAll("�", "").split(" ")[0].length();
            	latAttribute = Double.parseDouble(attributes.getValue("latLong").replaceAll("�", "").replaceAll("�", "").split(" ")[0].substring(0, size-2));
            	if (attributes.getValue("latLong").replaceAll("�", "").split(" ")[0].toLowerCase().contains("s"))
            		latAttribute = latAttribute*(-1);
            	System.out.println("latitude: "+latAttribute);
            	
            	size = attributes.getValue("latLong").replaceAll("�", "").replaceAll("�", "").split(" ")[1].length();
            	lonAttribute = Double.parseDouble(attributes.getValue("latLong").replaceAll("�", "").replaceAll("�", "").split(" ")[1].substring(0, size-2));
            	if (attributes.getValue("latLong").replaceAll("�", "").split(" ")[1].toLowerCase().contains("w"))
            		lonAttribute = lonAttribute*(-1);
            	System.out.println("longitude: "+lonAttribute);
            } else {
            	latAttribute = null;
            	lonAttribute = null;
            }
            
            placeTypeAttribute = attributes.getValue("type");

        }
        @Override
        public void endElement(String uri, String localName, String qName) {
            if (!"PLACE".equals(qName)) return;
            mEnd = mBuf.length();
            NormalizedPLACEChunk chunk = new NormalizedPLACEChunk(ChunkFactory.createChunk(mStart,mEnd,mType,0));
            if (latAttribute!=null && lonAttribute!=null){
	            chunk.setLatitude(latAttribute);
	            chunk.setLongitude(lonAttribute);
            }
            if (placeTypeAttribute!=null)
            	chunk.setPlaceType(placeTypeAttribute);
            
            mChunkList.add(chunk);
        }
        @Override
        public void characters(char[] cs, int start, int length) {
            mBuf.append(cs,start,length);
        }
        
        public Chunking getChunking() {
        	NormalizedPlaceChunking chunking = new NormalizedPlaceChunking(mBuf);
        	
        	
        	
        	//TODO: ver cm fazer isto de maneira inteligente
        	FileWriter fstream;
			try {
				fstream = new FileWriter("outputPlaceReferences/chunkings.xml", true);
				BufferedWriter out = new BufferedWriter(fstream);
	        	out.append("<chunking>\n");
	        	out.append("\t<mBuf>");
	        	out.append("<![CDATA["+mBuf+"]]>");
	        	out.append("</mBuf>\n");
	        	if(mChunkList.size()>0){
		        	out.append("\t<chunks>\n");
		        	for (NormalizedPLACEChunk chunk : mChunkList){
		        		chunking.add(chunk);
		        		out.append("\t\t<chunk>\n");
		        		out.append("\t\t\t<end>"+chunk.end()+"</end>\n");
		        		out.append("\t\t\t<start>"+chunk.start()+"</start>\n");
		        		
		        		out.append("\t\t\t<latitude>");
		        		try{
		        			out.append(Double.toString(chunk.getLatitude()));
		        		}catch(NullPointerException npe){}
		        		out.append("</latitude>\n");
		        		
		        		out.append("\t\t\t<longitude>");
		        		try{
		        			out.append(Double.toString(chunk.getLongitude()));
		        		}catch(NullPointerException npe){}
		        		out.append("</longitude>\n");
		        		
		        		
		        		out.append("\t\t\t<placeType>"+chunk.getPlaceType()+"</placeType>\n");
		        		out.append("\t\t\t<type>"+chunk.type()+"</type>\n");
		        		out.append("\t\t\t<score>"+chunk.score()+"</score>\n");
		        		out.append("\t\t</chunk>\n");		
		        	}
		        	out.append("\t</chunks>\n");
	        	}
	        	out.append("</chunking>\n");
	        	out.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
        	
        	
        	
        	
            
            		
            System.out.println("chunking: " + chunking.charSequence());
            		
            return chunking;
        }
    }
    
}
