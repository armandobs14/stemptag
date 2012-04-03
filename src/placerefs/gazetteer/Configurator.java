package placerefs.gazetteer;

public final class Configurator {

    private Configurator() { }
    
    /** A Boolean indicating if the indexes are stored over Google App Engine */
    //estava true
    public static final Boolean APP_ENGINE = false;
    
    /** DBPedia file containing redirect information. **/
    public static final String DBPEDIA_REDIRECTS = "/Volumes/300Extra/dbpedia/redirects_en.nt";
    
    /** DBPedia file containing disambiguation information. **/
    public static final String DBPEDIA_DISAMBIGUATIONS = "/Volumes/300Extra/dbpedia/disambiguations_en.nt";

    /** The path to the knowledge base. **/
    public static final String KB_FOLDER = "/Volumes/300Extra/tac-kbp-2011/kb/data/";
    
    /** The path to the lucene base path. **/
    //public static final String LUCENE_KB = "/media/sda3/IndiceLucene400k/";
    public static final String LUCENE_KB = "ConfigurationFiles/IndiceLucene40k/";
    
    /** The path to the lucene index for the KB entities content. **/
    public static final String LUCENE_KB_COMPLETE = LUCENE_KB + "kb-content";
     
    /** The path to the lucene index for the KB entities names. **/
    public static final String LUCENE_KB_NAMES = LUCENE_KB + "kb-names";
    
    /** The path to the lucene index for the KB entities names with spell check. **/
    public static final String LUCENE_KB_SPELLCHECK = LUCENE_KB + "kb-spell";

    
    
    
    public static final int MAX_NAME_SUGGESTIONS = 10;

    public static final boolean INDEX = false;

}
