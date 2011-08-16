package temporal;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import com.aliasi.corpus.Parser;
import com.aliasi.crf.ChainCrfFeatures;
import com.aliasi.hmm.HiddenMarkovModel;
import com.aliasi.hmm.HmmCharLmEstimator;
import com.aliasi.hmm.HmmDecoder;
import com.aliasi.io.FileExtensionFilter;
import com.aliasi.tokenizer.IndoEuropeanTokenCategorizer;
import com.aliasi.util.FastCache;
import com.aliasi.util.ObjectToDoubleMap;
import com.aliasi.util.Streams;

public class CRFFeatureExtractor implements com.aliasi.crf.ChainCrfFeatureExtractor<String>, Serializable {
    
	static final long serialVersionUID = 123L;
	
	String hmmPOSModelFile;
	
	HashSet<String> timeNames;
	HashSet<String> placeNames;
	HashSet<String> personNames;
	HashSet<String> facilityNames;
	HashSet<String> companyNames;
	HashSet<String> festivalNames;
	HashSet<String> titleNames;
	HashSet<String> governmentNames;
	HashSet<String> ambiguousTimeNames;
	
    public CRFFeatureExtractor() throws ClassNotFoundException, IOException { hmmPOSModelFile = null; }

    public CRFFeatureExtractor(String hmmPos) { 
    	this(hmmPos,true,true);
    }
    
	public CRFFeatureExtractor(String hmmPos, boolean dictionaries, boolean pos) { 
		if(pos && hmmPos!=null) this.hmmPOSModelFile = hmmPos; 
		if(hmmPos!=null && dictionaries) try {
			//timeNames = readDictionary(new File(hmmPos + File.separator + "timeNames.lst"));
			//timeNames = readDictionary(new File("/Users/vitorloureiro/Desktop/Geo-Temporal/timeNames.lst"));
			  placeNames = readDictionary(new File("/Users/vitorloureiro/Desktop/Geo-Temporal/PlaceLexicon/locationNames.lst"));
			  personNames = readDictionary(new File("/Users/vitorloureiro/Desktop/Geo-Temporal/PlaceLexicon/person.lst")); 
			  facilityNames = readDictionary(new File("/Users/vitorloureiro/Desktop/Geo-Temporal/PlaceLexicon/facility.lst"));
			  companyNames = readDictionary(new File("/Users/vitorloureiro/Desktop/Geo-Temporal/PlaceLexicon/company.lst"));
			  festivalNames = readDictionary(new File("/Users/vitorloureiro/Desktop/Geo-Temporal/PlaceLexicon/festival.lst"));
			  titleNames = readDictionary(new File("/Users/vitorloureiro/Desktop/Geo-Temporal/PlaceLexicon/title.lst"));
			  governmentNames = readDictionary(new File("/Users/vitorloureiro/Desktop/Geo-Temporal/PlaceLexicon/government.lst"));
              ambiguousTimeNames = readDictionary(new File("/Users/vitorloureiro/Desktop/Geo-Temporal/PlaceLexicon/ambiguousTimeNames.lst"));
			  System.out.println("TamanhoPlaceNames: "+placeNames.size());
			  System.out.println("TamanhoPlaceNames: "+personNames.size());
			  System.out.println("TamanhoPlaceNames: "+facilityNames.size());
			  System.out.println("TamanhoPlaceNames: "+companyNames.size());
			  System.out.println("TamanhoPlaceNames: "+festivalNames.size());
			  System.out.println("TamanhoPlaceNames: "+titleNames.size());
			  System.out.println("TamanhoPlaceNames: "+governmentNames.size());
			if (placeNames == null)
				throw new Exception();
		} catch ( Exception e ) { e.printStackTrace(); }
	}
	
    public ChainCrfFeatures<String> extract(List<String> tokens, List<String> tags) { return new ChunkerFeatures(tokens,tags); }
    
    Object writeReplace() { return this; }
    
    public static HashSet<String> readDictionary ( File data ) throws IOException {
    	if (!data.exists()) return null;
    	HashSet<String> set = new HashSet<String>();
    	BufferedReader reader = new BufferedReader(new FileReader(data));
    	String aux;
    	while ((aux=reader.readLine())!=null) 
    		if(aux.trim().length() > 0){ 
    			set.add(aux.toLowerCase().trim());
    		}
    	return set;
    }
    
    public static HiddenMarkovModel trainPOStagger ( File inputPath, File output ) throws Exception {
        int N_GRAM = 8;
        int NUM_CHARS = 256;
        double LAMBDA_FACTOR = 8.0;
        HmmCharLmEstimator estimator = new HmmCharLmEstimator(N_GRAM,NUM_CHARS,LAMBDA_FACTOR);
        Parser parser = new GeniaPosParser();
        parser.setHandler(estimator);
        File[] files = inputPath.listFiles(new FileExtensionFilter("txt"));
        for (int i = 0; i < files.length; ++i) {
          System.out.println("POS Training file=" + files[i]);
          File aux = File.createTempFile(files[i].getName(), ".tmp");
          aux.deleteOnExit();
          BufferedReader in = new BufferedReader(new FileReader(files[i]));
          PrintWriter out = new PrintWriter(new FileWriter(aux));
          String line = null;
          while((line=in.readLine())!=null) if(!line.startsWith("<") && !line.startsWith("#")) out.println(line.replace(' ','\n'));
          in.close();
          out.close();
          parser.parse(aux);
          aux.delete();
        }
        HiddenMarkovModel hmmPos = estimator;
        if (output!=null) {
            FileOutputStream fileOut = new FileOutputStream(output);
            ObjectOutputStream objOut = new ObjectOutputStream(fileOut);
            estimator.compileTo(objOut);
            Streams.closeOutputStream(objOut);
        }
        return hmmPos;
    }
    
    class ChunkerFeatures extends ChainCrfFeatures<String> {
        
    	com.aliasi.tag.Tagging<String> mPosTagging = null;
        
    	public ChunkerFeatures(List<String> tokens, List<String> tags) {
            super(tokens,tags);
         /*   InputStream in = null;
            BufferedInputStream bufIn = null;
            ObjectInputStream objIn = null;
            String resourceName = "/Users/vitorloureiro/Desktop/Teste/models/pos-en-general-brown.HiddenMarkovModel";
            if ( hmmPOSModelFile!=null )try {
                in = this.getClass().getResourceAsStream(resourceName);
                if (in == null) {
                    String msg = "Could not open stream for resource="
                        + resourceName;
                    throw new IOException(msg);
                }
                bufIn = new BufferedInputStream(in);
                objIn = new ObjectInputStream(bufIn);
                HiddenMarkovModel hmmPos = ((HiddenMarkovModel)(objIn.readObject()));
                objIn.close();
                bufIn.close();
                in.close();
            	mPosTagging = new HmmDecoder(hmmPos,null,new FastCache<String,double[]>(100000)).tag(tokens);
             
        	}catch ( Exception e ) { e.printStackTrace(); }*/
            
            if ( hmmPOSModelFile!=null ) try {
            	ObjectInputStream obj = new ObjectInputStream(new FileInputStream(new File(hmmPOSModelFile)));
            	HiddenMarkovModel hmmPos = ((HiddenMarkovModel)(obj.readObject()));
            	obj.close();
            	mPosTagging = new HmmDecoder(hmmPos,null,new FastCache<String,double[]>(100000)).tag(tokens);
            } catch ( Exception e ) { e.printStackTrace(); }
            else mPosTagging = null;
        }
        
        public Map<String,? extends Number> nodeFeatures(int n) {
            ObjectToDoubleMap<String> feats = new ObjectToDoubleMap<String>();
            boolean bos = n == 0;
            boolean eos = (n + 1) >= numTokens();
            String tokenCat = tokenCat(n);
            String prevTokenCat = bos ? null : tokenCat(n-1);
            String nextTokenCat = eos ? null : tokenCat(n+1);
            String token = normedToken(n);
            String prevToken = bos ? null : normedToken(n-1);
            String nextToken = eos ? null : normedToken(n+1);
            if (bos) feats.set("BOS",1.0);
            if (eos) feats.set("EOS",1.0);
            if (!bos && !eos) feats.set("!BOS!EOS",1.0);
            feats.set("TOK_" + token, 1.0);
            if (!bos) feats.set("TOK_PREV_" + prevToken,1.0);
            if (!eos) feats.set("TOK_NEXT_" + nextToken,1.0);
            feats.set("TOK_CAT_" + tokenCat, 1.0);
            if (!bos) feats.set("TOK_CAT_PREV_" + prevTokenCat, 1.0);
            if (!eos) feats.set("TOK_CAT_NEXT_" + nextTokenCat, 1.0);
			if ( mPosTagging != null ) {
            	String posTag = mPosTagging.tag(n);
            	String prevPosTag = bos ? null : mPosTagging.tag(n-1);
            	String nextPosTag = eos ? null : mPosTagging.tag(n+1);
            	feats.set("POS_" + posTag,1.0);
            	if (!bos) feats.set("POS_PREV_" + prevPosTag,1.0);
            	if (!eos) feats.set("POS_NEXT_" + nextPosTag,1.0);
			}
			if ( placeNames!=null ) {
            	if (placeNames.contains(token.toLowerCase())){
            		feats.set("PNAMES",1.0);
            	}
            	if (!bos && placeNames.contains(prevToken.toLowerCase())){
            		feats.set("PNAMES_PREV",1.0);
            	}
            	if (!eos && placeNames.contains(nextToken.toLowerCase())){
            		feats.set("PNAMES_NEXT",1.0);
            	}
			}
			if ( personNames!=null ) {
            	if (personNames.contains(token.toLowerCase())){
            		feats.set("PRNAMES",1.0);
            	}
            	if (!bos && personNames.contains(prevToken.toLowerCase())){
            		feats.set("PRNAMES_PREV",1.0);
            	}
            	if (!eos && personNames.contains(nextToken.toLowerCase())){
            		feats.set("PRNAMES_NEXT",1.0);
            	}
			}
			if ( facilityNames!=null ) {
            	if (facilityNames.contains(token.toLowerCase())){
            		feats.set("FNAMES",1.0);
            	}
            	if (!bos && facilityNames.contains(prevToken.toLowerCase())){
            		feats.set("FNAMES_PREV",1.0);
            	}
            	if (!eos && facilityNames.contains(nextToken.toLowerCase())){
            		feats.set("FNAMES_NEXT",1.0);
            	}
			}
			if ( companyNames!=null ) {
            	if (companyNames.contains(token.toLowerCase())){
            		feats.set("CNAMES",1.0);
            	}
            	if (!bos && companyNames.contains(prevToken.toLowerCase())){
            		feats.set("CNAMES_PREV",1.0);
            	}
            	if (!eos && companyNames.contains(nextToken.toLowerCase())){
            		feats.set("CNAMES_NEXT",1.0);
            	}
			}
			if ( festivalNames!=null ) {
            	if (festivalNames.contains(token.toLowerCase())){
            		feats.set("FSNAMES",1.0);
            	}
            	if (!bos && festivalNames.contains(prevToken.toLowerCase())){
            		feats.set("FSNAMES_PREV",1.0);
            	}
            	if (!eos && festivalNames.contains(nextToken.toLowerCase())){
            		feats.set("FSNAMES_NEXT",1.0);
            	}
			}
			if ( titleNames!=null ) {
            	if (titleNames.contains(token.toLowerCase())){
            		feats.set("TTNAMES",1.0);
            	}
            	if (!bos && titleNames.contains(prevToken.toLowerCase())){
            		feats.set("TTNAMES_PREV",1.0);
            	}
            	if (!eos && titleNames.contains(nextToken.toLowerCase())){
            		feats.set("TTNAMES_NEXT",1.0);
            	}
			}
			if ( governmentNames!=null ) {
            	if (governmentNames.contains(token.toLowerCase())){
            		feats.set("GNAMES",1.0);
            	}
            	if (!bos && governmentNames.contains(prevToken.toLowerCase())){
            		feats.set("GNAMES_PREV",1.0);
            	}
            	if (!eos && governmentNames.contains(nextToken.toLowerCase())){
            		feats.set("GNAMES_NEXT",1.0);
            	}
			}
			for (String suffix : suffixes(token)) feats.set("SUFF_" + suffix,1.0);
            if (!bos) for (String suffix : suffixes(prevToken)) feats.set("SUFF_PREV_" + suffix,1.0);
            if (!eos) for (String suffix : suffixes(nextToken)) feats.set("SUFF_NEXT_" + suffix,1.0);
            for (String prefix : prefixes(token)) feats.set("PREF_" + prefix,1.0);
            if (!bos) for (String prefix : prefixes(prevToken)) feats.set("PREF_PREV_" + prefix,1.0);
            if (!eos) for (String prefix : prefixes(nextToken)) feats.set("PREF_NEXT_" + prefix,1.0);
            return feats;
        }
        
        public Map<String,? extends Number> edgeFeatures(int n, int k) {
            boolean bos = n == 0;
            boolean eos = (n + 1) >= numTokens();
        	ObjectToDoubleMap<String> feats = new ObjectToDoubleMap<String>();
            String prevToken = bos ? null : normedToken(n-1);
            feats.set("PREV_TAG_" + tag(k), 1.0);
            feats.set("PREV_TAG_TOKEN_CAT_"  + tag(k) + "_" + tokenCat(n-1), 1.0);
            if (governmentNames.contains(prevToken.toLowerCase())) feats.set("PREV_TAG_TOKEN_CAT_"  + tag(k) + "_GNAMES", 1.0);
            return feats;
        }
        
        public String normedToken(int n) { return token(n).replaceAll("\\d+","*$0*").replaceAll("\\d","D"); }
        
        public String tokenCat(int n) { return IndoEuropeanTokenCategorizer.CATEGORIZER.categorize(token(n)); }
    }
    
    int MAX_PREFIX_LENGTH = 4;
    
    List<String> prefixes(String s) {
        int numPrefixes = java.lang.Math.min(MAX_PREFIX_LENGTH,s.length());
        if (numPrefixes == 0) return java.util.Collections.emptyList();
        if (numPrefixes == 1) return java.util.Collections.singletonList(s);
        List<String> result = new ArrayList<String>(numPrefixes);
        for (int i = 1; i <= java.lang.Math.min(MAX_PREFIX_LENGTH,s.length()); ++i) result.add(s.substring(0,i));
        return result;
    }
    
    int MAX_SUFFIX_LENGTH = 4;
    
    List<String> suffixes(String s) {
        int numSuffixes = java.lang.Math.min(s.length(), MAX_SUFFIX_LENGTH);
        if (numSuffixes <= 0) return java.util.Collections.emptyList();
        if (numSuffixes == 1) return java.util.Collections.singletonList(s);
        List<String> result = new ArrayList<String>(numSuffixes);
        for (int i = s.length() - numSuffixes; i < s.length(); ++i) result.add(s.substring(i));
        return result;
    }
        
}