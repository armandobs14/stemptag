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
            if (!"TIMEX2".equals(qName)) return;
            mType = qName;//TODO: attributes.getValue("TYPE");
            mStart = mBuf.length();
        }
        @Override
        public void endElement(String uri, String localName, String qName) {
            if (!"TIMEX2".equals(qName)) return;
            mEnd = mBuf.length();
            Chunk chunk = ChunkFactory.createChunk(mStart,mEnd,mType,0);
            mChunkList.add(chunk);
        }
        @Override
        public void characters(char[] cs, int start, int length) {
            mBuf.append(cs,start,length);
        }
        public Chunking getChunking() {
            ChunkingImpl chunking = new ChunkingImpl(mBuf);
            for (Chunk chunk : mChunkList) chunking.add(chunk);
            return chunking;
        }
    }
    
}