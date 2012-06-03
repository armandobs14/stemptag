package placerefs;

public final class Configurator {
	
	private Configurator() { }
	
	/** A boolean indicating if a classification validation should be done*/
    public static final boolean CLASSIFICATION = false;
    
    /** A boolean indicating if a disambiguation validation should be done*/
    public static final boolean DISAMBIGUATION = true;
    
    /** Classification features*/
    public static final boolean fBosEos = true;
    public static final boolean fPOS = true;
    public static final boolean fDictionaries = true;
    public static final boolean fTokens = true;
    public static final boolean fTokenCat = true;
    public static final boolean fPref = true;
    public static final boolean fSuf = true;
    
    /** Classification Model*/
    public static final String clasModel = "CRF";
    
    /** Name of type of reference to evaluate*/
    public static final String chunkTypeForEvaluation = "NAM";
    
    
}
