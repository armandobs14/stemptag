package temporal.web;

import com.aliasi.chunk.Chunk;
import com.aliasi.chunk.Chunking;
import com.aliasi.sentences.SentenceChunker;
import com.aliasi.sentences.SentenceModel;
import com.aliasi.tokenizer.TokenizerFactory;
import com.aliasi.util.Streams;
import com.aliasi.xml.GroupCharactersFilter;
import com.aliasi.xml.RemoveElementsFilter;
import com.aliasi.xml.SAXFilterHandler;
import com.aliasi.xml.SAXWriter;
import com.aliasi.xml.TextContentFilter;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import org.cyberneko.html.parsers.SAXParser;
import org.joda.time.DateTime;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.helpers.XMLReaderFactory;

import temporal.CandidateCreation;

/**
 * The <code>SentenceDemo</code> provides sentence detection relative
 * to a sentence model and tokenizer factory.  All of the work is
 * done by the parent class.
 */
public class SentenceDemo implements StreamDemo {

    protected TokenizerFactory mTokenizerFactory;
    protected SentenceModel mSentenceModel;
    protected SentenceChunker mSentenceChunker;
    
    private final Map<String,String[]> mPropertyDeclarations = new HashMap<String,String[]>();

    private final Map<String,String> mPropertyToolTips = new HashMap<String,String>();

    private final Map<String,String> mModelToResource = new HashMap<String,String>();
    private final Map<String,String> mTutorialToUrl = new HashMap<String,String>();

    private final Properties mDefaultProperties
        = new Properties();

    private final String mTitle;
    private final String mDescription;
    private boolean isPlainText;

    /**
     * Construct an abstract text demo with the specified title and description.
     * The title is returned by {@link #title()} and the description
     * by {@link #description()}.

     * @param title Title of the demo.
     * @param description Text description of the demo.
     */
    public SentenceDemo(String title, String description) {
        mTitle = title;
        mDescription = description;
        declareProperty(Constants.INPUT_CHAR_ENCODING_PARAM,
                        Constants.AVAILABLE_CHARSETS,
                        IN_CHARSET_TIP);
        declareProperty(Constants.OUTPUT_CHAR_ENCODING_PARAM,
                        Constants.AVAILABLE_CHARSETS,
                        OUT_CHARSET_TIP);
        declareProperty(Constants.CONTENT_TYPE_PARAM,
                        Constants.AVAILABLE_CONTENT_TYPES,
                        IN_CONTENT_TYPE_TIP);
        declareProperty(Constants.INCLUDE_ELTS_PARAM,
                        new String[0],
                        INCLUDE_ELTS_TIP);
        declareProperty(Constants.REMOVE_ELTS_PARAM,
                        new String[0],
                        REMOVE_ELTS_TIP);
    }

    /**
     * Declares a property for this demo with the specified key, legal
     * values and tool tip.  If the values are given as
     * <code>null</code>, any possible value is allowed.
     *
     * @parma key Name of the property.
     * @param values Array of legal property values.
     * @param tooltip Tool tip for this property.
     */
    public void declareProperty(String key, String[] values, 
                                String tooltip) {
        mPropertyToolTips.put(key,tooltip);
        mPropertyDeclarations.put(key,values);
        if (values != null && values.length > 0) {
            String defaultValue = values[0];
            mDefaultProperties.setProperty(key,defaultValue);
        }
    }

    /**
     * Adds the specified tutorial at the specified URL.
     *
     * @param tutorial Name of tutorial.
     * @param url URL of tutorial.
     */
    public void addTutorial(String tutorial, String url) {
        mTutorialToUrl.put(tutorial,url);
    }

    /**
     * Adds a the specified resource for the specified model name.
     *
     * @param model Model name.
     * @param resourcePath Path to resource from the class.
     */
    public void addModel(String model, String resourcePath) {
        mModelToResource.put(model,resourcePath);
    }

    /**
     * Returns the mapping of model names to resources.  The mapping
     * is constructed from calls to {@link #addModel(String,String)}.
     *
     * @return The mapping from model names to resource paths.
     */
    public Map<String,String> modelToResource() {
        return mModelToResource;
    }

    /**
     * Returns the mapping of tutorial names to URLs.  The mapping
     * is constructed from calls to {@link #addTutorial(String,String)}.
     *
     * @return The mapping from tutorial names to URLs.
     */
    public Map<String,String> tutorialToUrl() {
        return mTutorialToUrl;
    }

    /**
     * Do-nothing implementation.
     */
    public void init() { 
    }

    /**
     * Returns the default properties.  These are established for
     * general text demos, plus whatever was set by {@link
     * #declareProperty(String,String[],String)}.
     *
     * @return The default mapping from properties to values.
     */
    public Properties defaultProperties() {
        return mDefaultProperties;
    }

    /**
     * Returns the mapping from properties to legal values.  The value
     * will be <code>null</code> if any input is allowed.  These
     * values derive from the underlying text demo properties plus
     * whatever was set by {@link
     * #declareProperty(String,String[],String)}.
     *
     * @return The property declarations for this demo.
     */
    public Map<String,String[]> propertyDeclarations() {
        return mPropertyDeclarations;
    }

    /**
     * Returns the mapping from properties to tool tips.  These values
     * derive from the underlying text demo tool tips plus whatever
     * was set by {@link #declareProperty(String,String[],String)}.
     *
     * @return The property declarations for this demo.
     */
    public Map<String,String> propertyToolTips() {
        return mPropertyToolTips;
    }

    /**
     * Returns the title specified in the constructor.
     *
     * @return The title specified in the constructor.
     */
    public String title() {
        return mTitle;
    }

    /**
     * Returns the description specified in the constructor.
     *
     * @return The description specified in the constructor.
     */
    public String description() {
        return mDescription;
    }

    /**
     * Returns the XML content type, <code>text/xml</code>.
     */
    public String responseType() {
        return "text/xml";
    }

    /**
     * This method implements the basic stream demo process method.
     * It handles all input/output character sets and all aspects of
     * the XML/HTML handling.  The actual demo processing is done by
     * calling the abstract method {@link
     * #process(char[],int,int,SAXWriter,Properties)}.
     */
    public void process(InputStream in, OutputStream out, 
                        Properties properties) 
        throws IOException {

        try {
        	isPlainText = false;
        	
            String outCharset 
                = properties.getProperty(Constants.OUTPUT_CHAR_ENCODING_PARAM);
            SAXWriter saxWriter = new SAXWriter(out,outCharset,!XHTML_MODE); // don't need XML
            
            String inCharset 
                = properties.getProperty(Constants.INPUT_CHAR_ENCODING_PARAM);

            String inType = properties.getProperty(Constants.CONTENT_TYPE_PARAM);
            if (inType.startsWith(Constants.TEXT_PLAIN)) {
                char[] cs = Streams.toCharArray(in,inCharset);
                saxWriter.startDocument();
                saxWriter.startSimpleElement("output");
                isPlainText = true;
                CandidateCreation.isFirstTimex = true;
                process(cs,0,cs.length,saxWriter,properties);
                CandidateCreation.candidatesMillisecondsSameDoc.clear();
                CandidateCreation.candidatesMillisecondsSameSentence.clear();
                CandidateCreation.candidatesIntervalsSameDoc.clear();
                CandidateCreation.candidatesIntervalsSameSentence.clear();
                saxWriter.endSimpleElement("output");
                saxWriter.endDocument();
                return;
            }

            // annotator
            DefaultHandler handler
                = new ProcessHandler(saxWriter,properties);
            InputSource inSource = new InputSource(in);
            inSource.setEncoding(inCharset);
            XMLReader xmlReader = null;
            if (inType.startsWith(Constants.TEXT_XML)) {
                xmlReader = XMLReaderFactory.createXMLReader();
            } else if (inType.startsWith(Constants.TEXT_HTML)) {
                xmlReader = new SAXParser();
            } else {
                String msg = "Unexpected input content type=" + inType;
                throw new SAXException(msg);
            }

            // restrict to included elements
            String eltsToAnnotateCSV 
                = properties.getProperty(Constants.INCLUDE_ELTS_PARAM);
            if (nonEmpty(eltsToAnnotateCSV)) {
                handler = new IncludeElementHandler(eltsToAnnotateCSV,
                                                    saxWriter,
                                                    handler);
            }
            
            handler = new GroupCharactersFilter(handler);
                
            // remove elements
            String eltsToRemoveCSV 
                = properties.getProperty(Constants.REMOVE_ELTS_PARAM);
            if (nonEmpty(eltsToRemoveCSV)) {
                String[] eltsToRemove = eltsToRemoveCSV.split(",");
                RemoveElementsFilter filter 
                    = new RemoveElementsFilter(handler);
                for (int i = 0; i < eltsToRemove.length; ++i)
                    filter.removeElement(eltsToRemove[i]);
                handler = filter;
            }

            // parse and handle
            xmlReader.setContentHandler(handler);
            xmlReader.parse(inSource);
        } catch (SAXException e) {
            throw new IOException("SAXException=" + e);
        }
    }

    private class ProcessHandler extends SAXFilterHandler {
        final Properties mProperties;
        final SAXWriter mWriter;
        boolean flag = false;
        
        ProcessHandler(SAXWriter writer, Properties properties) {
            super(writer);
            mWriter = writer;
            mProperties = properties;
        }
        
        @Override
        public void startDocument() {
        	mWriter.startDocument();
        	try {
				mWriter.startSimpleElement("output");
			} catch (SAXException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
        }
        
        @Override
        public void endDocument() {
        	try {
				mWriter.endSimpleElement("output");
			} catch (SAXException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
        	mWriter.endDocument();
        	CandidateCreation.candidatesMillisecondsSameDoc.clear();
    		CandidateCreation.candidatesIntervalsSameDoc.clear();
    		CandidateCreation.candidatesMillisecondsSameSentence.clear();
			CandidateCreation.candidatesIntervalsSameSentence.clear();
        }
        
        
        @Override
        public void startElement(String uri, String localName, String qName, Attributes attributes) {
        	if (qName.equals("s")){
	        	if (attributes.getLength() > 0)
	        		if (attributes.getQName(0).equals("DOCCREATIONTIME")){
	        			flag = true;
	        			CandidateCreation.docCreationTime = new DateTime(new String(attributes.getValue(0)));
	        			System.out.println("DocCreationTime: "+CandidateCreation.docCreationTime);
	        			CandidateCreation.candidatesMillisecondsSameDoc.clear();
	            		CandidateCreation.candidatesIntervalsSameDoc.clear();
	            		CandidateCreation.isFirstTimex = true;
	        		}
	        		else{
	        			CandidateCreation.isFirstTimex = false;
	        		}

	        	CandidateCreation.candidatesMillisecondsSameSentence.clear();
				CandidateCreation.candidatesIntervalsSameSentence.clear();

        	}
        }
        
        @Override
        public void endElement(String uri, String localName, String qName) {

        }
        
        public void characters(char[] cs, int start, int length) 
            throws SAXException {
        	
        	if (!flag)
        		process(cs,start,start+length,mWriter,mProperties);
        	flag = false;
        }
    }

    /**
     * Reads the resource of the specified name relative
     * to this class and returns it as an object.  The
     * input stream for reading is created using:
     *
     * <pre>in = this.getClass().getResourceAsStream(resourceName);</pre>
     *
     * <p>This method returns <code>null</code> if there is an error
     * reading the resource or if it is not found.
     *
     * @param resourceName Name of resource.
     * @return The value of the resource.
     */
    protected Object readResource(String resourceName) {
        InputStream in = null;
        BufferedInputStream bufIn = null;
        ObjectInputStream objIn = null;
        try {
            in = this.getClass().getResourceAsStream(resourceName);
            if (in == null) {
                String msg = "Could not open stream for resource="
                    + resourceName;
                throw new IOException(msg);
            }
            bufIn = new BufferedInputStream(in);
            objIn = new ObjectInputStream(bufIn);
            return objIn.readObject();
        } catch (IOException e) {
            throw new IllegalArgumentException(e.toString());
        } catch (ClassNotFoundException e) {
            throw new IllegalArgumentException(e.toString());
        } finally {
            Streams.closeQuietly(objIn);
            Streams.closeQuietly(bufIn);
            Streams.closeQuietly(in);
        }
    }


    final static String INCLUDE_ELTS_TIP
        = "Comma-separated array of elements whose text content will be annotated.";

    final static String REMOVE_ELTS_TIP
        = "Comma-separated array of elements whose tags will be removed from the output.";

    final static String IN_CHARSET_TIP 
        = "Character encoding of input. All encodings shown, default first.";

    final static String OUT_CHARSET_TIP 
        = "Char encoding of output.  All encodings shown, default first.";

    final static String IN_CONTENT_TYPE_TIP 
        = "Set to the content type of the input text.";

    static final boolean XHTML_MODE = true;

    static boolean nonEmpty(String s) {
        return s != null
            && s.length() > 0;
    }

    static class IncludeElementHandler extends TextContentFilter {
        DefaultHandler mIncludedEltHandler;
        IncludeElementHandler(String eltsToAnnotateCSV,
                              SAXWriter writer,
                              DefaultHandler includedEltHandler) {
            super(writer);
            mIncludedEltHandler = includedEltHandler;
            String[] eltsToAnnotate = eltsToAnnotateCSV.split(",");
            for (int i = 0; i < eltsToAnnotate.length; ++i)
                filterElement(eltsToAnnotate[i]);
        }
        public void filteredCharacters(char[] cs, int start, int length) 
            throws SAXException {

            mIncludedEltHandler.characters(cs,start,length);
        }
    }

    /**
     * Construct a sentence demo using the specified tokenizer
     * factory and model.  The factory and model are reconstituted
     * using reflection using zero-argument constructors.
     *
     * @param tokenizerFactoryClassName Name of tokenizer factory class.
     * @param sentenceModelClassName Name of sentence model class.
     * @param demoName Name of the demo.
     * @param demoDescription Plain text description of the demo.
     */
    public SentenceDemo(String tokenizerFactoryClassName,
				String sentenceModelClassName,
				String demoName,
				String demoDescription) {
    	this(demoName,demoDescription);
        try {
            mTokenizerFactory = (TokenizerFactory) Class.forName(tokenizerFactoryClassName).getConstructor(new Class<?>[0]).newInstance(new Object[0]);
        } catch (ClassNotFoundException e) {
            throw new IllegalArgumentException(e);
        } catch (InstantiationException e) {
            throw new IllegalArgumentException(e);
        } catch (NoSuchMethodException e) {
            throw new IllegalArgumentException(e);
        } catch (IllegalAccessException e) {
            throw new IllegalArgumentException(e);
        } catch (InvocationTargetException e) {
            throw new IllegalArgumentException(e);
        }
	try { 
            mSentenceModel = (SentenceModel) Class.forName(sentenceModelClassName).getConstructor(new Class<?>[0]).newInstance(new Object[0]);
        } catch (ClassNotFoundException e) {
            throw new IllegalArgumentException(e);
        } catch (InstantiationException e) {
            throw new IllegalArgumentException(e);
        } catch (NoSuchMethodException e) {
            throw new IllegalArgumentException(e);
        } catch (IllegalAccessException e) {
            throw new IllegalArgumentException(e);
        } catch (InvocationTargetException e) {
            throw new IllegalArgumentException(e);
        }
	mSentenceChunker = new SentenceChunker(mTokenizerFactory,mSentenceModel);

    }

    /**
     * Extract sentences from the specified character slice,
     * wrapping them in XML sentence elements and deferring
     * their text to <code>processSentence</code> for further
     * processing.
     *
     * @param cs Underlying characters.
     * @param start Index of the first character of slice.
     * @param end Index of one past the last character of the slice.
     * @param writer SAXWriter to which output is written.
     * @param properties Properties for the processing.
     * @throws SAXException If there is an error during processing.
     */
    public void process(char[] cs, int start, int end,
			SAXWriter writer,
			Properties properties) 
	throws SAXException {


	String text = new String(cs,start,end-start);
	
	Chunking sentenceChunking
	    = mSentenceChunker.chunk(cs,start,end);
	Iterator<Chunk> sentenceIt
	    = sentenceChunking.chunkSet().iterator();
	int pos = 0;
	for (int i = 0; sentenceIt.hasNext(); ++i) {
	    Chunk sentenceChunk = sentenceIt.next();
	    int sentStart = sentenceChunk.start();
	    int sentEnd = sentenceChunk.end();
	    String sentenceText = text.substring(sentStart,sentEnd);
	    if (isPlainText){
	    	sentenceText = sentenceText.replaceAll("\\.", "");
	    	System.out.println(sentenceText);
	    	CandidateCreation.docCreationTime = new DateTime(sentenceText);
	    	isPlainText= false;
	    }
	    else{
		    writer.characters(text.substring(pos,sentStart));
		    writer.startSimpleElement("s","i",Integer.toString(i));
		    processSentence(sentenceText,writer,properties,i);
		    writer.endSimpleElement("s");
		    CandidateCreation.isFirstTimex = false;
	    }
	    pos = sentEnd;
	}
	writer.characters(text.substring(pos));
    }

    /**
     * Construct a sentence demo from a model of the specified
     * name, a tokenizer factory of the given name.  The genre
     * specificationis merely used in the title and description; it
     * will not affect behavior in any other way.
     *
     * @param tokenizerFactoryClassName Name of tokenizer factory's class.
     * @param sentenceModelName Name of sentence model's class.
     * @param genreTip A description of the genre for title and description.
     */
    public SentenceDemo(String tokenizerFactoryClassName,
			String sentenceModelName,
			String genreTip) {
	
	 this(tokenizerFactoryClassName,
	      sentenceModelName,
	      "Sentence Demo: " + genreTip,
	      "This is the sentence demo." 
	      + " It is intended to run over text of genre " + genreTip + ".");
    }

    /** 
     * Simply writes the text of the sentence to the writer.
     * 
     * @param sentenceText Text of sentence to process.
     * @param writer SAX writer to which to write results.
     * @param properities Properties of the request
     */
    public void processSentence(String sentenceText,
				SAXWriter writer,
				Properties properties,
				int sentenceNum)
	 throws SAXException {
	 
	 writer.characters(sentenceText);
     }
    
}