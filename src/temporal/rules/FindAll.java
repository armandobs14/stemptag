package temporal.rules;

import java.util.regex.*;
import java.util.*;

public class FindAll {
	
    public GlobalVariables globalVariables;
    
    public FindAll(GlobalVariables globals){
        globalVariables = globals;
    }
    
    public boolean printAllMatchResult( Pattern pattern, String text, boolean bExactMatch) {
        Matcher m = pattern.matcher(text);
        if (!m.matches()) return false;
        boolean bMatched = false;
        if (text.length() > 0) {
            List<MatchResult> results = findAll(pattern, text);
            for (MatchResult r : results) {
                if (globalVariables.bPrintOutTheResults) {
                    System.out.printf("*Found '%s' at (%d,%d)%n", r.group(), r.start(), r.end());
                }
                globalVariables.tempVariables.matchResult = r;
                bMatched = true;
                return bMatched;
            }
        } else {
            globalVariables.tempVariables.matchResult = null; 
            bMatched  =true;
        }
        if(!bMatched) {
            globalVariables.tempVariables.matchResult = null; 
            globalVariables.tempVariables.sCurrentMatch = "";
        }
        return bMatched;
    }
    
    public List<MatchResult> findAll(Pattern pattern, String text) {
        List<MatchResult> results = new ArrayList<MatchResult>();
        Matcher m = pattern.matcher(text);
        while (m.find()) results.add(m.toMatchResult());
        return results;
    }

}