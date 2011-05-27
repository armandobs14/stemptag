package temporal.web;

import java.io.ByteArrayInputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.nio.charset.Charset;
import java.util.Map;
import java.util.Set;

/**
 * The <code>Constants</code> class simply provides constants
 * used in other classes.
 */
public class Constants {

    static final int MAX_N_BEST = 16;
    
    static final int MAX_CONF = 8;
    
    static final double MIN_CONF_LOG2_P = -10;

    static final String RESULT_TYPE_PARAM = "resultType";
    
    static final String FIRST_BEST_RESULT_TYPE = "firstBest";
    
    static final String N_BEST_RESULT_TYPE = "nBest";
    
    static final String CONF_RESULT_TYPE = "conf";
    
    static final String[] RESULT_TYPE_VALS = new String[] { FIRST_BEST_RESULT_TYPE, N_BEST_RESULT_TYPE, CONF_RESULT_TYPE };

    static final String RESULT_TYPE_TOOL_TIP = "Select first-best, n-best or confidence-ranked results.";
    
    public final static String INPUT_FILE_PARAM
	= "inFile";

    public final static String OUTPUT_FILE_PARAM
	= "outFile";

    public final static String INPUT_DIR_PARAM
	= "inDir";

    public final static String OUTPUT_DIR_PARAM
	= "outDir";

    public final static String DEMO_CONSTRUCTOR_PARAM
	= "demoConstructor";

    public final static String DEMO_CONSTRUCTOR_ARGS_PARAM
	= "demoConstructorArgs";

    public final static String OUTPUT_CHAR_ENCODING_PARAM
	= "outCharset";

    public final static String INPUT_CHAR_ENCODING_PARAM
	= "inCharset";

    public final static String CONTENT_TYPE_PARAM 
	= "contentType";

    public final static String REMOVE_ELTS_PARAM
	= "removeElts";

    public final static String INCLUDE_ELTS_PARAM
	= "includeElts";

    public final static String EXCLUDE_ELTS_PARAM
	= "excludeElts";

    public final static String TEXT_PLAIN
	= "text/plain";

    public final static String TEXT_XML
	= "text/xml";

    public final static String TEXT_HTML
	= "text/html";

    public final static String[] AVAILABLE_CONTENT_TYPES
	= new String[] { TEXT_PLAIN,
			 TEXT_XML,
			 TEXT_HTML };

    public static String[] AVAILABLE_CHARSETS;
    static {
	Map<String,Charset> availableCharsetMap = Charset.availableCharsets();
	Set<String> availableCharsetSet = availableCharsetMap.keySet();	
	String[] allCharsets
	    = availableCharsetSet.toArray(new String[0]);
	String defaultCharset = getDefaultCharset();

	AVAILABLE_CHARSETS = new String[allCharsets.length+1];
	System.arraycopy(allCharsets,0,AVAILABLE_CHARSETS,1,
			 allCharsets.length);
	AVAILABLE_CHARSETS[0] = defaultCharset;
    }

    public static String getDefaultCharset() {
	// in 1.5: return Charset.defaultCharset().name();
	ByteArrayInputStream bytesIn = new ByteArrayInputStream(new byte[0]);
	InputStreamReader reader = new InputStreamReader(bytesIn);
	String charsetName = reader.getEncoding();
	Charset charset = Charset.forName(charsetName);
	return charset.name();
    }

    public static StreamDemo constructDemo(String demoClassName,
					   String demoConstructorArgsParam) 
	throws ClassCastException {

	if (demoClassName == null) {
	    String msg = "Require init parameter=demoConstructor"
		+ " with value set to name of instance of StreamDemo"
		+ " with implementation on the classpath.";
	    throw new IllegalArgumentException(msg);
	}

	Object[] demoConstructorArgs 
	    = (demoConstructorArgsParam == null)
	    ? new String[0]
	    : demoConstructorArgsParam.split(",");
        
        @SuppressWarnings({"unchecked","rawtypes"})
        Class<?>[] argClasses = (Class<?>[]) new Class[demoConstructorArgs.length];
	java.util.Arrays.fill(argClasses,String.class);

        try {
            Class<?> consClass = Class.forName(demoClassName);
            Constructor<?> cons = consClass.getConstructor(argClasses);
	        StreamDemo demo = (StreamDemo) cons.newInstance(demoConstructorArgs);
	        demo.init();
	        return demo;
        } catch (IllegalAccessException e) {
	    printDebug(e,demoClassName,demoConstructorArgs);
        } catch (InstantiationException e) {
	    printDebug(e,demoClassName,demoConstructorArgs);
        } catch (InvocationTargetException e) {
	    printDebug(e,demoClassName,demoConstructorArgs);
        } catch (ExceptionInInitializerError e) {
	    printDebug(e,demoClassName,demoConstructorArgs);
        } catch (ClassNotFoundException e) {
	    printDebug(e,demoClassName,demoConstructorArgs);
        } catch (NoSuchMethodException e) {
	    printDebug(e,demoClassName,demoConstructorArgs);
        }
	return null;
    }
  
    static void printDebug(Throwable e,
			   String demoClassName,
			   Object[] demoConstructorArgs) {
	System.out.println("Exception in constructor=" + e);
	System.out.println("  demoClassName=|" + demoClassName + "|");
	System.out.println("  demoConstructorArgs=");
	for (int i = 0; i < demoConstructorArgs.length; ++i)
	    System.out.println("    " + i + "=|" + demoConstructorArgs[i] + "|");
	System.out.println("  stack trace=");
	e.printStackTrace(System.out);
	throw new IllegalArgumentException("Exception="+ e.getCause());
    }

}