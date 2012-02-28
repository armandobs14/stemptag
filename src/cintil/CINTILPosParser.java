package cintil;

import com.aliasi.corpus.ObjectHandler;
import com.aliasi.corpus.XMLParser;
import com.aliasi.tag.Tagging;
import com.aliasi.tokenizer.TokenizerFactory;
import com.aliasi.xml.DelegatingHandler;
import java.util.ArrayList;
import java.util.List;
import org.xml.sax.Attributes;
import org.xml.sax.helpers.DefaultHandler;

public class CINTILPosParser extends XMLParser<ObjectHandler<Tagging<String>>> {
	  	
    String mSentenceTag = "s";
    
    public CINTILPosParser() {
           super();
    }
    
    public CINTILPosParser(TokenizerFactory factory) {
        super();
    }

    public CINTILPosParser(ObjectHandler handler) {
        super(handler);
    }

    @Override
    protected DefaultHandler getXMLHandler() { return new MucHandler(getHandler()); }

    public void setSentenceTag(String tag) { mSentenceTag = tag; }

    class MucHandler extends DelegatingHandler {
        ObjectHandler mHandler;
        SentenceHandler mSentHandler;

        MucHandler(ObjectHandler chunkHandler) {
            mHandler = chunkHandler;
            mSentHandler = new SentenceHandler();
            setDelegate(mSentenceTag,mSentHandler);
        }

        @Override
        public void finishDelegate(String qName, DefaultHandler handler) {
        	Tagging<String> tagging = mSentHandler.getTagging();
            mHandler.handle(tagging);
        }
    }

    static class SentenceHandler extends DefaultHandler {

    	List<String> tokList; 
    	List<String> tagList;
        
    	SentenceHandler() { }

        @Override
        public void startDocument() {
        	tokList = new ArrayList<String>();
        	tagList = new ArrayList<String>();
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
        	for ( String token : tokens ) {
        		String text = token.substring(0,token.indexOf("/"));
        		String pos = token.substring(token.indexOf("/")+1,token.indexOf("["));
        		tokList.add(text);
        		tagList.add(pos);
        	}
        }
        
        private Tagging<String> getTagging (  ) {
            Tagging<String> tagging = new Tagging<String>(tokList,tagList);
            return tagging;
        }

    }

}