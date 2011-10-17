package temporal;

import com.aliasi.chunk.Chunk;
import com.aliasi.chunk.Chunking;
import com.aliasi.chunk.ChunkFactory;
import com.aliasi.chunk.ChunkingImpl;
import com.aliasi.corpus.ObjectHandler;
import com.aliasi.corpus.XMLParser;
import com.aliasi.tokenizer.TokenizerFactory;
import com.aliasi.xml.DelegatingHandler;
import java.util.ArrayList;
import java.util.List;

import org.joda.time.DateTime;
import org.xml.sax.Attributes;
import org.xml.sax.helpers.DefaultHandler;

public class TIMEXChunkParser extends XMLParser<ObjectHandler> {

	private static final long serialVersionUID = 6281374154299530460L;
	  
	private TokenizerFactory tokenizer;
	
	String mSentenceTag = "p";
    
    public TIMEXChunkParser() {
        super();
    }
    
    public TIMEXChunkParser(TokenizerFactory factory) {
        super();
        tokenizer = factory;
    }

    public TIMEXChunkParser(ObjectHandler handler) {
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
        String valAttribute;
        String setAttribute;
        int mStart;
        int mEnd;
        final List<NormalizedChunk> mChunkList = new ArrayList<NormalizedChunk>();
        SentenceHandler() { }
        @Override
        public void startDocument() {
            mBuf = new StringBuilder();
            mChunkList.clear();
        }
        @Override
        public void startElement(String uri, String localName, String qName, Attributes attributes) {
        	if (attributes.getLength() > 0)
        		if (attributes.getQName(0).equals("DOCCREATIONTIME")){
        			CandidateCreation.docCreationTime = new DateTime(new String(attributes.getValue(0)));
        			CandidateCreation.candidatesMillisecondsSameDoc.clear();
            		CandidateCreation.candidatesIntervalsSameDoc.clear();
            		CandidateCreation.isFirstTimex = true;
        		}
        	if (qName.equals("p")){
        		CandidateCreation.candidatesMillisecondsSameSentence.clear();
			    CandidateCreation.candidatesIntervalsSameSentence.clear();
        	}
            if (!"TIMEX2".equals(qName)) return;
            mType = qName;//TODO: attributes.getValue("TYPE");
            mStart = mBuf.length();
            valAttribute = attributes.getValue("val");
            setAttribute = attributes.getValue("set");
        }
        @Override
        public void endElement(String uri, String localName, String qName) {
            if (!"TIMEX2".equals(qName)) return;
            mEnd = mBuf.length();
            NormalizedChunk chunk = new NormalizedChunk(ChunkFactory.createChunk(mStart,mEnd,mType,0));
            chunk.setNormalized(valAttribute);
            if (valAttribute == null){
            	chunk.setType(7);
            }
            else if (valAttribute.equals("")){
            	chunk.setType(7);
            }
            else if (setAttribute != null && setAttribute.toLowerCase().equals("yes")){
            	chunk.setType(1);
            }
            else if (valAttribute.toLowerCase().equals("past_ref")){
            	chunk.setType(4);
            }
            else if (valAttribute.toLowerCase().equals("present_ref")){
            	chunk.setType(5);
            }
            else if (valAttribute.toLowerCase().equals("future_ref")){
            	chunk.setType(6);
            }
            else if (valAttribute.toLowerCase().contains("p") || valAttribute.toLowerCase().contains("pt")){
            	chunk.setType(2);
            }
            else{
            	chunk.setType(3);
            }
            
            mChunkList.add(chunk);
        }
        @Override
        public void characters(char[] cs, int start, int length) {
            mBuf.append(cs,start,length);
        }
        public Chunking getChunking() {
        	NormalizedChunking chunking = new NormalizedChunking(mBuf);
            for (NormalizedChunk chunk : mChunkList){
            		chunking.add(chunk);
            }
            return chunking;
        }
    }
    
}