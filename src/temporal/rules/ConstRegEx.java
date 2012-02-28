package temporal.rules;

public class ConstRegEx {
	
    public static String WS = "\\s+|\\-";
    
    public static String DASH = "\\-";

    public static String PUNCT = "(?:[,.;:?!\\(\\)]|''|``|')";  //?: means not numbered
    
    public static String REST_ANY = "(?:.*?)";
    
    public static String onlyWholeWord(String sRegExp ) { return "\\b"+sRegExp+"\\b"; }
    
    public static String conditioned_findAnywhere(String sRegExp) {
        return REST_ANY + sRegExp + REST_ANY;
    }
    
    public static String findAnywhere(String sRegExp){
        return sRegExp + REST_ANY;
    }

    public static String prefixWordSuffix(String sRegExp, String sPref, String sSuff){
        return findAnywhere(sPref + sRegExp + sSuff);
    }
    
    public static String conditioned_prefixWordSuffix(String sRegExp, String sPref, String sSuff){
        return conditioned_findAnywhere(sPref + sRegExp + sSuff);
    }
    
    public static String groupBrackets(String sRegExp, int[] iGroupIndex){
        iGroupIndex[0]++;
        return "("+sRegExp+")";
    }
    
    public static void resetIntArray(int iNewValue, int[] iGroupIndex){
        iGroupIndex[0] = iNewValue;
    }
    
    public static void resetTimePoint(TimePoint newTP, TimePoint lastTP){
        lastTP.copy(newTP);
    }
    
    public static String newPrefix(int[] iGroupIndex) {
        iGroupIndex[0] = 1;
        return "(";
    }

    public ConstRegEx() { }
    
}
