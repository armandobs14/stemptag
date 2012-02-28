package temporal.rules;

public class MixedAll_TE {
    
    private TIMEXRuleAnnotator pAnnotator;
    
    public MixedAll_TE(TIMEXRuleAnnotator pAnnot){
        pAnnotator = pAnnot;
    }
    
    public GlobalVariables Variables(){
        return pAnnotator.globalVariables;
    }
    
    public FindAll Find(){
        return pAnnotator.findAll;
    }

    public boolean capture_DAY_TIME_TIMEZONE_Case1(String sParagraph, String sRegExPrefix, String sRegExSuffix, int[] iPrevGroupNumber, boolean bMayLeave) {
        Variables().startCapture();
        TempResultVariables tempRes = new TempResultVariables(Variables());
        tempRes.setInitialValues(iPrevGroupNumber);
        if (pAnnotator.dateTE.capture_DMY(sParagraph, sRegExPrefix, sRegExSuffix, iPrevGroupNumber, bMayLeave)){
            tempRes.setBestResult(iPrevGroupNumber[0]);
        }
        if (!tempRes.bAtLeastOneResult){
            tempRes.resetInitialValues(iPrevGroupNumber);
            if (pAnnotator.dateTE.capture_DAY_DMY(sParagraph, sRegExPrefix, sRegExSuffix, iPrevGroupNumber, bMayLeave)){
                tempRes.setBestResult(iPrevGroupNumber[0]);
            }
        }
        if (!tempRes.bAtLeastOneResult){
            tempRes.resetInitialValues(iPrevGroupNumber);
            if (pAnnotator.dateTE.capture_DAY_DM(sParagraph, sRegExPrefix, sRegExSuffix, iPrevGroupNumber, bMayLeave)){
                tempRes.setBestResult(iPrevGroupNumber[0]);
            }
        }
        if (!tempRes.bAtLeastOneResult){
            tempRes.resetInitialValues(iPrevGroupNumber);
            if (pAnnotator.dateTE.capture_DAY_NAME(sParagraph, sRegExPrefix, sRegExSuffix, iPrevGroupNumber, bMayLeave)){
                tempRes.setBestResult(iPrevGroupNumber[0]);
            }
        }
        tempRes.prepareReturn(iPrevGroupNumber);
        return Variables().prepareReturn(tempRes.bAtLeastOneResult);   
    }
    
    public boolean capture_DAY_TIME_TIMEZONE(String sParagraph, String sRegExPrefix, String sRegExSuffix, int[] iPrevGroupNumber, boolean bMayLeave) {
        Variables().startCapture();
        String sNewSuf = ConstRegEx.REST_ANY + sRegExSuffix;
        if (capture_DAY_TIME_TIMEZONE_Case1(sParagraph, 
                sRegExPrefix, sNewSuf, iPrevGroupNumber, bMayLeave)) {
            try {
                TempSaveCurrentMatch tmpMatch = new TempSaveCurrentMatch(Variables());
                sParagraph = tmpMatch.saveMatchAndMoveOverAndReset(sParagraph);
                String sNewPref = ConstRegEx.newPrefix(iPrevGroupNumber);
                sNewSuf = ConstRegEx.REST_ANY + sRegExSuffix;
                boolean bRes = 
                        pAnnotator.comPrefSuf.capture_WS_or_WS_op_COMMA_SEMICOL_WS_op
                        (sParagraph, sNewPref, sNewSuf, iPrevGroupNumber, bMayLeave);
                if (bRes) {
                    sParagraph = tmpMatch.saveMatchAndMoveOverAndReset(sParagraph);
                    sNewPref = ConstRegEx.newPrefix(iPrevGroupNumber);
                    
                    bRes = pAnnotator.timeTE.capture_SIMPLE_TIME_ALL(sParagraph, sNewPref,
                            sNewSuf, iPrevGroupNumber, bMayLeave);
                    if (bRes) {
                        tmpMatch.restoreCurrentMatch();
                    }
                }
                return Variables().prepareReturn(bRes);
            }catch(Exception e){
                    if (!bMayLeave){ Const.writeSpecificError(e, sParagraph);}
            }
        }
        return Variables().prepareReturn(false);
    }    
    
    public boolean capture_DAYS(String sParagraph, String sRegExPrefix, String sRegExSuffix, int[] iPrevGroupNumber, boolean bMayLeave) {
        Variables().startCapture();
        Variables().tempVariables.stempLN = "";
        String sNewSuf = ConstRegEx.REST_ANY + sRegExSuffix;
        boolean bRes = pAnnotator.comPrefSuf.capture_THE_WS_op(sParagraph, 
                sRegExPrefix, sNewSuf, iPrevGroupNumber, bMayLeave);
        if (bRes){
            TempSaveCurrentMatch tmpMatch = new TempSaveCurrentMatch(Variables());
            sParagraph = tmpMatch.saveMatchAndMoveOverAndReset(sParagraph);
            String sNewPrefix = ConstRegEx.newPrefix(iPrevGroupNumber);
            
            bRes = pAnnotator.capture_LAST_NEXT_WORD_WS_op(sParagraph, 
                    sNewPrefix, sNewSuf, iPrevGroupNumber, bMayLeave);
            if (bRes){
                sParagraph = tmpMatch.saveMatchAndMoveOverAndReset(sParagraph);
                sNewPrefix = ConstRegEx.newPrefix(iPrevGroupNumber);
                
                TempResultVariables tempRes = new TempResultVariables(Variables());
                tempRes.setInitialValues(iPrevGroupNumber);

                if (pAnnotator.dateTE.capture_DAY_NAME(sParagraph,
                        sNewPrefix, sRegExSuffix, iPrevGroupNumber, bMayLeave)) {
                    tempRes.setBestResult(iPrevGroupNumber[0]);
                }
                if (!tempRes.bAtLeastOneResult) {
                    tempRes.resetInitialValues(iPrevGroupNumber);
                    if (pAnnotator.dateTE.capture_WEEKEND(sParagraph,
                            sNewPrefix, sRegExSuffix, iPrevGroupNumber, bMayLeave)) {
                        tempRes.setBestResult(iPrevGroupNumber[0]);
                    }
                }
                
                if (tempRes.bAtLeastOneResult) {
                    try {
                        /*(?{$tempLN?
                        $crtTimePoint->function("$tempLN(DOW=".$crtTimePoint->dayOfWeek().")")
                        :
                        $crtTimePoint->function("DOW=".$crtTimePoint->dayOfWeek());
                        $crtTimePoint->dayOfWeek('');})                
                         */
                        if (Variables().tempVariables.stempLN.length() > 0) {
                            Variables().tempVariables.crtTimePoint.set("function",
                                    Variables().tempVariables.stempLN +
                                    "(DOW=" +
                                    Variables().tempVariables.crtTimePoint.get("dayOfWeek") +
                                    ")");
                        } else {
                            Variables().tempVariables.crtTimePoint.set("function",
                                    "DOW=" +
                                    Variables().tempVariables.crtTimePoint.get("dayOfWeek"));
                        }
                        Variables().tempVariables.crtTimePoint.set("dayOfWeek", "");

                        tmpMatch.restoreCurrentMatch();
                    } catch (Exception e) {
                        if (!bMayLeave) {
                            Const.writeSpecificError(e, sParagraph);
                        }
                    }
                    return Variables().prepareReturn(true);
                }

                //a new case
                tempRes.resetInitialValues(iPrevGroupNumber);
                if (pAnnotator.dateTE.capture_TODAY_DEICTIC(sParagraph,
                        sNewPrefix, sRegExSuffix, iPrevGroupNumber, bMayLeave)) {
                    //without tmpMatch.restoreCurrentMatch();
                    tempRes.setBestResult(iPrevGroupNumber[0]);
                    return Variables().prepareReturn(true);
                }
                tempRes.prepareReturn(iPrevGroupNumber);
                return Variables().prepareReturn(tempRes.bAtLeastOneResult);
                
            }
        }
        return Variables().prepareReturn(bRes);
    }
    
    public boolean capture_DAY_AND_DAY_PART(String sParagraph, String sRegExPrefix, String sRegExSuffix, int[] iPrevGroupNumber,boolean bMayLeave ) {
        Variables().startCapture();
        int[] iTempGr = new int[1]; iTempGr[0] =0;
        String sNewSuffix = ConstRegEx.groupBrackets(ConstRegEx.WS, iTempGr) + ConstRegEx.REST_ANY + sRegExSuffix;
        boolean bRes=capture_DAYS(sParagraph, sRegExPrefix, sNewSuffix, iPrevGroupNumber, bMayLeave) ;
        if (bRes) {
            Variables().concatenateSuffix(iPrevGroupNumber[0] + 1);
            TempSaveCurrentMatch tmpMatch = new TempSaveCurrentMatch(Variables());
            sParagraph = tmpMatch.saveMatchAndMoveOverAndReset(sParagraph);
            String sNewPref = ConstRegEx.newPrefix(iPrevGroupNumber);
            bRes = pAnnotator.capture_DAY_PART(sParagraph, sNewPref, sRegExSuffix, iPrevGroupNumber, bMayLeave);
            if (bRes){
                try {
                    Variables().tempVariables.crtTimePoint.set("hour",
                            Variables().tempVariables.stempTU);
                    tmpMatch.restoreCurrentMatch();
                } catch (Exception e) {
                    if (!bMayLeave){ Const.writeSpecificError(e, sParagraph);}                
                }
            }
        }
        return Variables().prepareReturn(bRes);
    }    
    
    public boolean capture_MIXED_ALL(String sParagraph, String sRegExPrefix, String sRegExSuffix, int[] iPrevGroupNumber,boolean bMayLeave ) {
        Variables().startCapture();
        TempResultVariables tempRes = new TempResultVariables(Variables());
        Variables().tempVariables.crtTimePoint.clear();
        Variables().tempVariables.rangeTimePoint.clear();
        tempRes.setInitialValues(iPrevGroupNumber);
        if (capture_DAY_TIME_TIMEZONE(sParagraph, sRegExPrefix, sRegExSuffix, iPrevGroupNumber, bMayLeave)) {
            this.pAnnotator.writeToLogFile("DAY_TIME_TIMEZONE\n");
            tempRes.setBestResult(iPrevGroupNumber[0]);
            return Variables().prepareReturn(true);
        }
        tempRes.prepareReturn(iPrevGroupNumber);
        if (capture_DAY_AND_DAY_PART(sParagraph, sRegExPrefix, sRegExSuffix, iPrevGroupNumber, bMayLeave)){
            this.pAnnotator.writeToLogFile("DAY_AND_DAY_PART\n");
            tempRes.setBestResult( iPrevGroupNumber[0]);       
            return Variables().prepareReturn(true);
        }
        tempRes.prepareReturn(iPrevGroupNumber);
        return Variables().prepareReturn(tempRes.bAtLeastOneResult);
    }

}
