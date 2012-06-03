package cintil;

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
import org.xml.sax.Attributes;
import org.xml.sax.helpers.DefaultHandler;

public class CINTILChunkParser extends XMLParser<ObjectHandler> {

    private static final long serialVersionUID = 6281374154299530460L;
	  
    private TokenizerFactory tokenizer;
	
    String mSentenceTag = "s";
    
    public CINTILChunkParser() {
           super();
    }
    
    public CINTILChunkParser(TokenizerFactory factory) {
        super();
        tokenizer = factory;
    }

    public CINTILChunkParser(ObjectHandler handler) {
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
        int mStart;
        int mEnd;
        final List<Chunk> mChunkList = new ArrayList<Chunk>();

        SentenceHandler() { }

        @Override
        public void startDocument() {
            mBuf = new StringBuilder();
            mChunkList.clear();
        }

        @Override
        public void startElement(String uri, String localName, String qName, Attributes attributes) {
        	return;
        }

        @Override
        public void endElement(String uri, String localName, String qName) {
            return;            
        }

        @Override
        public void characters(char[] cs, int start, int length) {
        	String tokens[] = new String(cs,start,length).split(" ");
        	for ( int i=0; i<tokens.length; i++ ) {
        		String token = tokens[i];
        		String text = token.substring(0,token.indexOf("/"));
        		String bio = token.substring(token.indexOf("[")+1,token.indexOf("]"));
        		if (bio.startsWith("B-")) {
        			mType = bio.substring(2);
        			mStart = mBuf.length();
        		} else if (!bio.startsWith("I-") && mType != null) {
        			mChunkList.add(ChunkFactory.createChunk(mStart,mEnd,mType,0));
        			mType = null;
        		}
        		mBuf.append(text);
        		mEnd = mBuf.length();
        		if (i < tokens.length ) mBuf.append(" ");
        	}
        }

        public Chunking getChunking() {
        	ChunkingImpl chunking = new ChunkingImpl(mBuf);
            for (Chunk chunk : mChunkList ) chunking.add(chunk);
            return chunking;
        }

    }
    
}
