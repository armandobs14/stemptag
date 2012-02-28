package temporal.rules;

public class Undefinite_TE {
	
    private TIMEXRuleAnnotator pAnnotator;
    
    public Undefinite_TE(TIMEXRuleAnnotator pAnnot){
        pAnnotator = pAnnot;
    }
    
    public GlobalVariables Variables(){
        return pAnnotator.globalVariables;
    }
    
    public FindAll Find(){
        return pAnnotator.findAll;
    }
    
    public boolean capture_DAY_PARTS_UNDEF(String sParagraph, String sRegExPrefix, String sRegExSuffix, int[] iPrevGroupNumber,boolean bMayLeave) {
        Variables().startCapture();
        boolean bRes = pAnnotator.capture_DAY_PART(sParagraph, sRegExPrefix, sRegExSuffix, iPrevGroupNumber, bMayLeave);
        if (bRes) try {
                Variables().tempVariables.crtTimePoint.set("hour", Variables().tempVariables.stempTU);
        } catch(Exception e) {
                if (!bMayLeave){ Const.writeSpecificError(e, sParagraph);}
        }
        return Variables().prepareReturn(bRes);
    }
    
    public boolean capture_UNDEFINITE_ALL(String sParagraph, String sRegExPrefix, String sRegExSuffix, int[] iPrevGroupNumber,boolean bMayLeave) {
        Variables().startCapture();        
        TempResultVariables tempRes = new TempResultVariables(Variables());
        Variables().tempVariables.crtTimePoint.clear();
        Variables().tempVariables.rangeTimePoint.clear();
        tempRes.setInitialValues(iPrevGroupNumber);
        if (capture_DAY_PARTS_UNDEF(sParagraph, sRegExPrefix, sRegExSuffix, iPrevGroupNumber, bMayLeave)){
            this.pAnnotator.writeToLogFile("DAY_PARTS_UNDEF\n");
            tempRes.setBestResult( iPrevGroupNumber[0]);       
            return Variables().prepareReturn(true);
        }
        tempRes.prepareReturn(iPrevGroupNumber);
        return Variables().prepareReturn(tempRes.bAtLeastOneResult);
    } 

}
