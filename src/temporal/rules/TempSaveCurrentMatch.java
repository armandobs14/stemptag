package temporal.rules;

public class TempSaveCurrentMatch {
    
	GlobalVariables pGlobalVariables;
    
    public String sMatch;
    
    public StringBuffer sParag; 

    public int iNrOfExpression; 
    /*ABC 
     * for 'A'  we have iNrOfExpression = 0
     * for 'B'  we have iNrOfExpression = 1
     * for 'C'  we have iNrOfExpression = 2
     */
    
    public TempSaveCurrentMatch(GlobalVariables glb) {
        pGlobalVariables = glb;
        sParag = new StringBuffer();
        sMatch = "";
        iNrOfExpression = 0;
    }
    
    public String saveMatchAndMoveOverAndReset(String sParagraph){
        sParag = new StringBuffer(sParagraph);
        if (pGlobalVariables.tempVariables.sCurrentMatch.length() > 0) {
            if (iNrOfExpression == 0){
                sMatch = pGlobalVariables.tempVariables.sCurrentMatch;
            } else {
                sMatch += pGlobalVariables.tempVariables.sCurrentMatch;
            }
            sParag.delete(0, pGlobalVariables.tempVariables.sCurrentMatch.length());
            iNrOfExpression++;
        }
        pGlobalVariables.tempVariables.sCurrentMatch = "";//reset
        return sParag.toString();
    }
    
    public void restoreCurrentMatch(){
        sMatch += pGlobalVariables.tempVariables.sCurrentMatch;
        pGlobalVariables.tempVariables.sCurrentMatch = sMatch;
   }

}
