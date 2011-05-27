package temporal.rules;

import java.util.regex.*;

public class Time_TE {
    
	private TIMEXRuleAnnotator pAnnotator;
    
    public Time_TE(TIMEXRuleAnnotator pAnnot){
        pAnnotator = pAnnot;
    }
    
    public GlobalVariables Variables(){
        return pAnnotator.globalVariables;
    }
    
    public FindAll Find(){
        return pAnnotator.findAll;
    }
         
    public boolean capture_NUM_PLURAL_TIME_Case1(String sParagraph, String sRegExPrefix, String sRegExSuffix, int[] iPrevGroupNumber,boolean bMayLeave){
        Variables().startCapture();
        String sNewSuf = ConstRegEx.REST_ANY + sRegExSuffix;
        boolean bRes = pAnnotator.comPrefSuf.capture_THE_WS_op(sParagraph, sRegExPrefix, sNewSuf, iPrevGroupNumber, bMayLeave);
        if (bRes) {
            TempSaveCurrentMatch tmpMatch = new TempSaveCurrentMatch(Variables());
            sParagraph = tmpMatch.saveMatchAndMoveOverAndReset(sParagraph);
            String sNewPref = ConstRegEx.newPrefix(iPrevGroupNumber);
            TempResultVariables tempRes = new TempResultVariables(Variables());
            tempRes.setInitialValues(iPrevGroupNumber);
            if (pAnnotator.capture_QUANTT(sParagraph,sNewPref, sRegExSuffix, iPrevGroupNumber, bMayLeave)) {
                tempRes.setBestResult(iPrevGroupNumber[0]);
            }
            if (!tempRes.bAtLeastOneResult) {
                tempRes.resetInitialValues(iPrevGroupNumber);
                if (pAnnotator.capture_TO_99_MIX(sParagraph, sNewPref, sNewSuf, iPrevGroupNumber, bMayLeave)) {
                    tempRes.setBestResult(iPrevGroupNumber[0]);
                }
            }
            tempRes.prepareReturn(iPrevGroupNumber);
            if (tempRes.bAtLeastOneResult) {
                try {
                    Variables().tempVariables.stempNo = Variables().tempVariables.ordWords.get_val().toString();
                    Variables().tempVariables.stempPart = "";
                    sNewPref = ConstRegEx.newPrefix(iPrevGroupNumber);
                    sParagraph = tmpMatch.saveMatchAndMoveOverAndReset(sParagraph);
                    bRes = pAnnotator.comPrefSuf.capture_WS_op_DASH_WS_op_or_WS(sParagraph, sNewPref, sRegExSuffix, iPrevGroupNumber, bMayLeave);
                    if (bRes) tmpMatch.restoreCurrentMatch();
                    return bRes;
                } catch (Exception e) {
                    if (!bMayLeave) Const.writeSpecificError(e, sParagraph);
                }
            }
            return Variables().prepareReturn(tempRes.bAtLeastOneResult);
        }
        return Variables().prepareReturn(bRes);
    }    

    public boolean capture_NUM_PLURAL_TIME_Case2(String sParagraph, String sRegExPrefix, String sRegExSuffix, int[] iPrevGroupNumber,boolean bMayLeave){
        Variables().startCapture();
        TempResultVariables tempRes = new TempResultVariables(Variables());
        tempRes.setInitialValues(iPrevGroupNumber);
        if (pAnnotator.capture_PLURAL_TIME_UNIT_LEX(sParagraph, sRegExPrefix, sRegExSuffix, iPrevGroupNumber, bMayLeave)) {
            tempRes.setBestResult(iPrevGroupNumber[0]);
        }
        if (!tempRes.bAtLeastOneResult) {
            tempRes.resetInitialValues(iPrevGroupNumber);
            if (pAnnotator.capture_TIME_UNIT_LEX(sParagraph, sRegExPrefix, sRegExSuffix, iPrevGroupNumber, bMayLeave)) {
                tempRes.setBestResult(iPrevGroupNumber[0]);
            }
        }
        if (!tempRes.bAtLeastOneResult) {
            tempRes.resetInitialValues(iPrevGroupNumber);
            if (pAnnotator.capture_PLURAL_DAY_PART(sParagraph, sRegExPrefix, sRegExSuffix, iPrevGroupNumber, bMayLeave)) {
                tempRes.setBestResult(iPrevGroupNumber[0]);
            }
        }
        tempRes.prepareReturn(iPrevGroupNumber);
        if (tempRes.bAtLeastOneResult){
            try{
                TempSaveCurrentMatch tmpMatch = new TempSaveCurrentMatch(Variables());
                sParagraph = tmpMatch.saveMatchAndMoveOverAndReset(sParagraph);
                String sNewPref = ConstRegEx.newPrefix(iPrevGroupNumber);
                boolean bRes = pAnnotator.capture_WS_AND_PART_op(sParagraph, sNewPref, sRegExSuffix, iPrevGroupNumber, true);
                if (bRes) { tmpMatch.restoreCurrentMatch(); }
            } catch(Exception e) {
                    if (!bMayLeave){ Const.writeSpecificError(e, sParagraph);}
            }
        }
        return Variables().prepareReturn(tempRes.bAtLeastOneResult);
    }    
    
   public boolean capture_NUM_PLURAL_TIME_Case3(String sParagraph, String sRegExPrefix, String sRegExSuffix, int[] iPrevGroupNumber,boolean bMayLeave){
       Variables().startCapture();
       String sNewSuf = ConstRegEx.REST_ANY + sRegExSuffix;
       boolean bRes = pAnnotator.capture_AND_PARTS(sParagraph, sRegExPrefix, sNewSuf, iPrevGroupNumber, bMayLeave);
       if (bRes) {
           TempSaveCurrentMatch tmpMatch = new TempSaveCurrentMatch(Variables());
           sParagraph = tmpMatch.saveMatchAndMoveOverAndReset(sParagraph);
           String sNewPref = ConstRegEx.newPrefix(iPrevGroupNumber);
           bRes = pAnnotator.comPrefSuf.capture_WS(sParagraph, sNewPref, sNewSuf, iPrevGroupNumber, bMayLeave);
           if (bRes) {
               sParagraph = tmpMatch.saveMatchAndMoveOverAndReset(sParagraph);
               sNewPref = ConstRegEx.newPrefix(iPrevGroupNumber);
               TempResultVariables tempRes = new TempResultVariables(Variables());
               tempRes.setInitialValues(iPrevGroupNumber);
               if (pAnnotator.capture_PLURAL_TIME_UNIT_LEX(sParagraph, sNewPref, sRegExSuffix, iPrevGroupNumber, bMayLeave)) {
                   tempRes.setBestResult(iPrevGroupNumber[0]);
               }
               if (!tempRes.bAtLeastOneResult) {
                   tempRes.resetInitialValues(iPrevGroupNumber);
                   if (pAnnotator.capture_TIME_UNIT_LEX(sParagraph, sNewPref, sRegExSuffix, iPrevGroupNumber, bMayLeave)) {
                       tempRes.setBestResult(iPrevGroupNumber[0]);
                   }
               }
               tempRes.prepareReturn(iPrevGroupNumber);
               if (tempRes.bAtLeastOneResult) { tmpMatch.restoreCurrentMatch(); }
               return Variables().prepareReturn(tempRes.bAtLeastOneResult);
           }
           return bRes;
       }
       return Variables().prepareReturn(bRes);
   }
   
   public boolean capture_NUM_PLURAL_TIME(String sParagraph, String sRegExPrefix, String sRegExSuffix, int[] iPrevGroupNumber,boolean bMayLeave){
        Variables().startCapture();
        String sNewSuf = ConstRegEx.REST_ANY + sRegExSuffix;
        boolean bRes = capture_NUM_PLURAL_TIME_Case1(sParagraph, sRegExPrefix, sNewSuf, iPrevGroupNumber, bMayLeave);
        if (bRes) {
            TempSaveCurrentMatch tmpMatch = new TempSaveCurrentMatch(Variables());
            sParagraph = tmpMatch.saveMatchAndMoveOverAndReset(sParagraph);
            String sNewPref = ConstRegEx.newPrefix(iPrevGroupNumber);
            TempResultVariables tempRes = new TempResultVariables(Variables());
            tempRes.setInitialValues(iPrevGroupNumber);
            if (capture_NUM_PLURAL_TIME_Case2(sParagraph, sNewPref, sRegExSuffix, iPrevGroupNumber, bMayLeave)) {
                tempRes.setBestResult(iPrevGroupNumber[0]);
            }
            if (!tempRes.bAtLeastOneResult) {
                tempRes.resetInitialValues(iPrevGroupNumber);
                if (capture_NUM_PLURAL_TIME_Case3(sParagraph, sNewPref, sRegExSuffix, iPrevGroupNumber, bMayLeave)) {
                    tempRes.setBestResult(iPrevGroupNumber[0]);
                }
            }
            tempRes.prepareReturn(iPrevGroupNumber);
            if (tempRes.bAtLeastOneResult) {
                try {
                    Variables().tempVariables.stempNumPluralTime = Variables().tempVariables.stempNo + Variables().tempVariables.stempPart + Variables().tempVariables.stempTU;                    
                    tmpMatch.restoreCurrentMatch();
                } catch(Exception e) {
                    if (!bMayLeave){ Const.writeSpecificError(e, sParagraph);}
                }
            }
            return Variables().prepareReturn(tempRes.bAtLeastOneResult);
        }
        return Variables().prepareReturn(bRes);
    }    

    public boolean capture_AGO_WORDS(String sParagraph, String sRegExPrefix, String sRegExSuffix, int[] iPrevGroupNumber,boolean bMayLeave) {
        Variables().startCapture();
        String sPattern = ConstRegEx.groupBrackets(Variables().hashtbLexValues.get("LER"), iPrevGroupNumber);
        Pattern patternLex = Pattern.compile(
                ConstRegEx.prefixWordSuffix(sPattern, sRegExPrefix, sRegExSuffix),
                Pattern.CASE_INSENSITIVE);
        boolean bResult = Find().printAllMatchResult(patternLex, sParagraph, false);
        if (bResult) {
            try {
                Variables().tempVariables.stempLER = Variables().getMapHashValue(Variables().getResult_LowerCase(iPrevGroupNumber[0])) + "_LER";
                Variables().saveCurrentMatch(iPrevGroupNumber[0]);
            } catch(Exception e){
                    if (!bMayLeave) { Const.writeSpecificError(e, sParagraph); }
                    Variables().tempVariables.sCurrentMatch = "";
            }
        }        
        return Variables().prepareReturn(bResult);
    }    

    public boolean capture_NUM_PLURAL_TIME_AGO(String sParagraph, String sRegExPrefix, String sRegExSuffix, int[] iPrevGroupNumber,boolean bMayLeave) {
        Variables().startCapture();
        @SuppressWarnings("unused")
        String sNewPrefix = sRegExPrefix +
                ConstRegEx.groupBrackets(
                    ConstRegEx.groupBrackets(Variables().hashtbLexValues.get("POINT_MOD"), iPrevGroupNumber)+
                    ConstRegEx.groupBrackets(ConstRegEx.WS, iPrevGroupNumber), iPrevGroupNumber) + "?";
       @SuppressWarnings("unused")
       int iPointMode = iPrevGroupNumber[0] -1;
       @SuppressWarnings("unused")
       String sNewSuffix = ConstRegEx.REST_ANY + sRegExSuffix;
       return Variables().prepareReturn(false);
    }

    public boolean capture_AGO_BEFORE_TIME(String sParagraph, String sRegExPrefix, String sRegExSuffix, int[] iPrevGroupNumber,boolean bMayLeave) {
       Variables().startCapture();
       return Variables().prepareReturn(false);    
    }
    
    public boolean capture_24TIME(String sParagraph, String sRegExPrefix, String sRegExSuffix, int[] iPrevGroupNumber,boolean bMayLeave) {
        Variables().startCapture();
        String sNewPref = sRegExPrefix + "(";
        String sNewSuf = ")" + sRegExSuffix;
        iPrevGroupNumber[0]++;
        int iResult_Gr = iPrevGroupNumber[0];        
        String sPattern = ConstRegEx.groupBrackets("1[3-9]|2[0-3]|0?[0-9]", iPrevGroupNumber);
        int iHour = iPrevGroupNumber[0];
        sPattern += ConstRegEx.groupBrackets(ConstRegEx.WS, iPrevGroupNumber) + "?[:]" +
                ConstRegEx.groupBrackets(ConstRegEx.WS, iPrevGroupNumber) + "?" +
                ConstRegEx.groupBrackets("[0-5][0-9]", iPrevGroupNumber);
        int iMin = iPrevGroupNumber[0];
        sPattern += ConstRegEx.groupBrackets(
                ConstRegEx.groupBrackets(ConstRegEx.WS, iPrevGroupNumber) + "?[:]" +
                ConstRegEx.groupBrackets(ConstRegEx.WS, iPrevGroupNumber) + "?" +
                ConstRegEx.groupBrackets("[0-5][0-9]", iPrevGroupNumber), iPrevGroupNumber)+
                "?";
        int iSec = iPrevGroupNumber[0];
        sPattern += ConstRegEx.groupBrackets(
                ConstRegEx.groupBrackets(ConstRegEx.WS, iPrevGroupNumber) + "?[.]" +
                ConstRegEx.groupBrackets(ConstRegEx.WS, iPrevGroupNumber) + "?" +
                ConstRegEx.groupBrackets("\\d\\d", iPrevGroupNumber)
                , iPrevGroupNumber)+"?";
        int iMiliSec = iPrevGroupNumber[0];
        Pattern patternLex = Pattern.compile(ConstRegEx.prefixWordSuffix(sPattern, sNewPref, sNewSuf));
        boolean bResult = Find().printAllMatchResult(patternLex, sParagraph, false);
        if (bResult){
            try {
                Variables().saveCurrentMatch(iResult_Gr);
                Variables().tempVariables.stempHour = Variables().tempVariables.matchResult.group(iHour);
                Variables().tempVariables.stempMin = Variables().tempVariables.matchResult.group(iMin);
                try {
                    Variables().tempVariables.stempSec = Variables().tempVariables.matchResult.group(iSec);
                    Variables().tempVariables.stempMiliSec = Variables().tempVariables.matchResult.group(iMiliSec);
                } catch (Exception e) {
                    Variables().tempVariables.stempSec = "";
                    Variables().tempVariables.stempMiliSec ="";
                }
            } catch(Exception e) { if (!bMayLeave){ Const.writeSpecificError(e, sParagraph);} }
        }
        return Variables().prepareReturn(bResult);
    }    
    
    public boolean capture_12TIME(String sParagraph, String sRegExPrefix, String sRegExSuffix, int[] iPrevGroupNumber,boolean bMayLeave) {
        Variables().startCapture();
        String sNewPref = sRegExPrefix + "(";
        String sNewSuf = ")" + sRegExSuffix;
        iPrevGroupNumber[0]++; //beacause of the brackets
        int iResult_Gr = iPrevGroupNumber[0];
        String sPattern = ConstRegEx.groupBrackets("1[012]|0?[1-9]", iPrevGroupNumber);
        int iHour = iPrevGroupNumber[0];
        sPattern += ConstRegEx.groupBrackets(ConstRegEx.WS, iPrevGroupNumber) + "?[:]" +
                ConstRegEx.groupBrackets(ConstRegEx.WS, iPrevGroupNumber) + "?" +
                ConstRegEx.groupBrackets("[0-5][0-9]", iPrevGroupNumber);
        int iMin = iPrevGroupNumber[0];
        sPattern += ConstRegEx.groupBrackets(
                ConstRegEx.groupBrackets(ConstRegEx.WS, iPrevGroupNumber) + "?[:]" +
                ConstRegEx.groupBrackets(ConstRegEx.WS, iPrevGroupNumber) + "?" +
                ConstRegEx.groupBrackets("[0-5][0-9]", iPrevGroupNumber), iPrevGroupNumber)+
                "?";
        int iSec = iPrevGroupNumber[0];
        sPattern += ConstRegEx.groupBrackets(
                ConstRegEx.groupBrackets(ConstRegEx.WS, iPrevGroupNumber) + "?[.]" +
                ConstRegEx.groupBrackets(ConstRegEx.WS, iPrevGroupNumber) + "?" +
                ConstRegEx.groupBrackets("\\d\\d", iPrevGroupNumber)
                , iPrevGroupNumber) + "?";
        int iMiliSec = iPrevGroupNumber[0];
        Pattern patternLex = Pattern.compile(
                ConstRegEx.prefixWordSuffix(sPattern, sNewPref, sNewSuf)
                );
        boolean bResult = Find().printAllMatchResult(patternLex, sParagraph, false);
        if (bResult) {
            try {
                Variables().saveCurrentMatch(iResult_Gr);

                Variables().tempVariables.stempHour =
                        Variables().tempVariables.matchResult.group(iHour);
                Variables().tempVariables.stempMin =
                        Variables().tempVariables.matchResult.group(iMin);
                //because of ?
                try {
                    Variables().tempVariables.stempSec =
                            Variables().tempVariables.matchResult.group(iSec);
                    Variables().tempVariables.stempMiliSec =
                            Variables().tempVariables.matchResult.group(iMiliSec);
                } catch (Exception e) {
                    Variables().tempVariables.stempSec = "";
                    Variables().tempVariables.stempMiliSec ="";
                }
            }catch(Exception e){
                    if (!bMayLeave){ Const.writeSpecificError(e, sParagraph);}
            }
        }
        return Variables().prepareReturn(bResult);
    }    
    
    public boolean capture_12TIME_2(String sParagraph,
            String sRegExPrefix, String sRegExSuffix, int[] iPrevGroupNumber,boolean bMayLeave) {
/*
$_12TIME= 
 qr/((0?[1-9]|1[012])(?{$tempHour = $^N;}))
 $WS?([:]|$WS)$WS?
 (([0-5][0-9])(?{$tempMin = $^N;}))
 /; #8 32 
 */        
        Variables().startCapture();
        String sNewPref = sRegExPrefix + "(";
        String sNewSuf = ")" + sRegExSuffix;
        iPrevGroupNumber[0]++; //beacause of the brackets
        int iResult_Gr = iPrevGroupNumber[0];
        
        String sPattern = ConstRegEx.groupBrackets("1[012]|0?[1-9]", iPrevGroupNumber);
        int iHour = iPrevGroupNumber[0];
        sPattern += ConstRegEx.groupBrackets(ConstRegEx.WS, iPrevGroupNumber) + "?[:\\s]" +
                ConstRegEx.groupBrackets(ConstRegEx.WS, iPrevGroupNumber) + "?" +
                ConstRegEx.groupBrackets("[0-5][0-9]", iPrevGroupNumber);
        int iMin = iPrevGroupNumber[0];
        Pattern patternLex = Pattern.compile(
                ConstRegEx.prefixWordSuffix(sPattern, sNewPref, sNewSuf)
                );
        boolean bResult = Find().printAllMatchResult(patternLex, sParagraph, false);
        if (bResult) {
            try {
                Variables().saveCurrentMatch(iResult_Gr);

                Variables().tempVariables.stempHour =
                        Variables().tempVariables.matchResult.group(iHour);
                Variables().tempVariables.stempMin =
                        Variables().tempVariables.matchResult.group(iMin);
            }catch(Exception e){
                    if (!bMayLeave){ Const.writeSpecificError(e, sParagraph);}
            }
        }
        return Variables().prepareReturn(bResult);
    }    
        
    public boolean capture_ONE_TO_TWELVE(String sParagraph,
            String sRegExPrefix, String sRegExSuffix, int[] iPrevGroupNumber,boolean bMayLeave) {
/*
$ONE_TO_TWELVE= 
qr/$_12TIME|
 $_24TIME|
 (([1-9]|[1][012])(?{$tempHour = $^N;$tempMin = '';}))|
 (
 (?{$ordWords = new OrdinalNumber();})
 ($UNIT|$TEEN)(?{$tempHour = $ordWords->val();$tempMin = '';})
 )/i; 
#1, 2 ... 12, one, two ... twelve. Notice that cardinal words aren't constrained to be in the range one to twelve or 
 */        
        Variables().startCapture();
        TempResultVariables tempRes = new TempResultVariables(Variables());
        tempRes.setInitialValues(iPrevGroupNumber);
        
        if (capture_12TIME(sParagraph, sRegExPrefix, sRegExSuffix, iPrevGroupNumber, bMayLeave)){
            tempRes.setBestResult( iPrevGroupNumber[0]);    
            return Variables().prepareReturn(true);
        }
        
        tempRes.resetInitialValues(iPrevGroupNumber);
        if (capture_24TIME(sParagraph, sRegExPrefix, sRegExSuffix, iPrevGroupNumber, bMayLeave)) {
            tempRes.setBestResult( iPrevGroupNumber[0]);    
            return Variables().prepareReturn(true);
        }
        
        tempRes.resetInitialValues(iPrevGroupNumber);
        
        //(([1-9]|[1][012])(?{$tempHour = $^N;$tempMin = '';}))
        //String sPattern = ConstRegEx.groupBrackets("[1-9]|[1][012]", iPrevGroupNumber);
        String sPattern = ConstRegEx.groupBrackets("[1][012]|[1-9]", iPrevGroupNumber);
        Pattern patternLex = Pattern.compile(
                ConstRegEx.prefixWordSuffix(sPattern, sRegExPrefix, sRegExSuffix),
                Pattern.CASE_INSENSITIVE
                );
        boolean bResult = Find().printAllMatchResult(patternLex, sParagraph, false);
        if (bResult){
            try {
                Variables().tempVariables.stempHour =
                        Variables().tempVariables.matchResult.group(iPrevGroupNumber[0]);
                Variables().tempVariables.stempMin = "";

                Variables().saveCurrentMatch(iPrevGroupNumber[0]);
                tempRes.setBestResult(iPrevGroupNumber[0]);
            } catch(Exception e){
                    if (!bMayLeave){ Const.writeSpecificError(e, sParagraph);}
            }
            return Variables().prepareReturn(true);
        }
        
        tempRes.resetInitialValues(iPrevGroupNumber);
        Variables().tempVariables.ordWords = new OrdinalNumber();
        
        TempResultVariables tempRes2 = new TempResultVariables(Variables());
        tempRes2.setInitialValues(iPrevGroupNumber);
        
        if (pAnnotator.capture_UNIT(sParagraph, sRegExPrefix, sRegExSuffix, iPrevGroupNumber, bMayLeave)){
            try {
                Variables().tempVariables.stempHour =
                        Variables().tempVariables.ordWords.get_val().toString();
                Variables().tempVariables.stempMin = "";

                tempRes2.setBestResult(iPrevGroupNumber[0]);
            }catch(Exception e){
                    if (!bMayLeave){ Const.writeSpecificError(e, sParagraph);}
            }
            return Variables().prepareReturn(true);
        }
        
        tempRes2.resetInitialValues(iPrevGroupNumber);
        if (pAnnotator.capture_TEEN(sParagraph, sRegExPrefix, sRegExSuffix, iPrevGroupNumber, bMayLeave)){
            try {
                Variables().tempVariables.stempHour =
                        Variables().tempVariables.ordWords.get_val().toString();
                Variables().tempVariables.stempMin = "";

                tempRes2.setBestResult(iPrevGroupNumber[0]);
            } catch (Exception e) {
                if (!bMayLeave) {Const.writeSpecificError(e, sParagraph);}
            }
            return Variables().prepareReturn(true);
        }
        tempRes2.prepareReturn(iPrevGroupNumber);
        if (tempRes2.bAtLeastOneResult){
            tempRes.setBestResult( iPrevGroupNumber[0]);   
            return Variables().prepareReturn(true);
        }
        
        tempRes.prepareReturn(iPrevGroupNumber);
        return Variables().prepareReturn(tempRes.bAtLeastOneResult);
    }    
    public boolean capture_NUM_OCLOCK(String sParagraph,
            String sRegExPrefix, String sRegExSuffix, int[] iPrevGroupNumber,boolean bMayLeave) {
/*
$NUM_OCLOCK= qr/$ONE_TO_TWELVE$WS?(o\'clock|O\'CLOCK)/i;  
				#six o'clock, 6 o'clock 
 */        
        Variables().startCapture();
        String sNewSuffix = ConstRegEx.REST_ANY+
                sRegExSuffix;
        boolean bRes = capture_ONE_TO_TWELVE(sParagraph, 
                sRegExPrefix, sNewSuffix, iPrevGroupNumber, bMayLeave);
        if (bRes){
            TempSaveCurrentMatch tmpMatch = new TempSaveCurrentMatch(Variables());
            sParagraph = tmpMatch.saveMatchAndMoveOverAndReset(sParagraph);
            String sNewPref = ConstRegEx.newPrefix(iPrevGroupNumber);
            bRes = pAnnotator.comPrefSuf.capture_WS_op_OCLOCK(sParagraph, 
                    sNewPref, sRegExSuffix, iPrevGroupNumber, bMayLeave);
            if (bRes){
                tmpMatch.restoreCurrentMatch();
            }
        }
        return Variables().prepareReturn(bRes);
    }    
    public boolean capture_PRE_AMPM(String sParagraph,
            String sRegExPrefix, String sRegExSuffix, int[] iPrevGroupNumber,boolean bMayLeave) {
/*
 $PRE_AMPM= 
 qr/($NUM_OCLOCK|$ONE_TO_TWELVE|$_12TIME_2|
 (([2][0-4]|[1][0-9]|[1-9]))(?{$tempHour = $^N;$tempMin = '';}))/;
				#6:30, six o'clock, 6 o'clock, 6, six 
 */  
        Variables().startCapture();
        TempResultVariables tempRes = new TempResultVariables(Variables());
        tempRes.setInitialValues(iPrevGroupNumber);
    
        if (capture_12TIME_2(sParagraph, sRegExPrefix, sRegExSuffix, iPrevGroupNumber, bMayLeave)){
            tempRes.setBestResult( iPrevGroupNumber[0]);    
            return Variables().prepareReturn(true);
        }
        
        tempRes.resetInitialValues(iPrevGroupNumber);
        if (capture_NUM_OCLOCK(sParagraph, sRegExPrefix, sRegExSuffix, iPrevGroupNumber, bMayLeave)){
             tempRes.setBestResult( iPrevGroupNumber[0]);    
             return Variables().prepareReturn(true);
        }
        
        tempRes.resetInitialValues(iPrevGroupNumber);
        if (capture_ONE_TO_TWELVE(sParagraph, sRegExPrefix, sRegExSuffix, iPrevGroupNumber, bMayLeave)){
            tempRes.setBestResult( iPrevGroupNumber[0]);    
            return Variables().prepareReturn(true);
        }
                
        tempRes.resetInitialValues(iPrevGroupNumber);
        int iResult_Group = iPrevGroupNumber[0]+1;
         //(([2][0-4]|[1][0-9]|[1-9]))(?{$tempHour = $^N;$tempMin = '';}))/
        String sPattern = ConstRegEx.groupBrackets("[2][0-4]|[1][0-9]|[1-9]", iPrevGroupNumber);
        Pattern patternLex = Pattern.compile(
                ConstRegEx.prefixWordSuffix(sPattern, sRegExPrefix, sRegExSuffix)
                );
        boolean bResult = Find().printAllMatchResult(patternLex, sParagraph, false);
        if (bResult){
            try {
                Variables().tempVariables.stempHour =
                        Variables().tempVariables.matchResult.group(iResult_Group);
                Variables().tempVariables.stempMin = "";

                Variables().saveCurrentMatch(iPrevGroupNumber[0]);
                tempRes.setBestResult(iPrevGroupNumber[0]);
            }catch(Exception e){
                    if (!bMayLeave){ Const.writeSpecificError(e, sParagraph);}
            }
            return Variables().prepareReturn(true);
        }
        
        tempRes.prepareReturn(iPrevGroupNumber);
        return Variables().prepareReturn(tempRes.bAtLeastOneResult);
    }    
    public boolean capture_TIME_AMPM(String sParagraph,
            String sRegExPrefix, String sRegExSuffix, int[] iPrevGroupNumber,boolean bMayLeave) {
/*
$TIME_AMPM= 
 qr/$PRE_AMPM$WS?$AMPM(?{$tempHour=($tempAP eq "p")?($tempHour != 12 ? $tempHour+12 : 12):($tempHour == 12 ? 24 : $tempHour);})/;
				#6:30 a.m., six o'clock a.m., 6 o'clock a.m., 6 a.m., six a.m 
$AMPM				= qr/([ap])\.?m\.?(?{$tempAP = lc($^N);})/i; 
 */        
        Variables().startCapture();
        String sNewSuffix = ConstRegEx.REST_ANY+sRegExSuffix;
        boolean bRes =capture_PRE_AMPM(sParagraph, sRegExPrefix, sNewSuffix, iPrevGroupNumber, bMayLeave);
        if (bRes){
            try {
              TempSaveCurrentMatch tmpMatch = new TempSaveCurrentMatch(Variables());
              sParagraph = tmpMatch.saveMatchAndMoveOverAndReset(sParagraph);
              String sNewPref = ConstRegEx.newPrefix(iPrevGroupNumber)  ;
              bRes = pAnnotator.comPrefSuf.capture_WS_op_AMPM(sParagraph, 
                      sNewPref, sRegExSuffix, iPrevGroupNumber, bMayLeave);
                if (bRes) {
                    Variables().tempVariables.stempAP =
                            Variables().tempVariables.matchResult.group(iPrevGroupNumber[0]).toLowerCase();

                    int iTempHour = Integer.parseInt(Variables().tempVariables.stempHour);
                    if (Variables().tempVariables.stempAP.compareTo("p") == 0) {
                        if (iTempHour != 12) {
                            iTempHour += 12;
                        } else {
                            iTempHour = 12;
                        }
                    } else {
                        if (iTempHour == 12) {
                            iTempHour = 24;
                        }
                    }
                    Variables().tempVariables.stempHour = Integer.toString(iTempHour);
                    
                    tmpMatch.restoreCurrentMatch();
                }
            } catch (Exception e) {
                if (!bMayLeave){ Const.writeSpecificError(e, sParagraph);}            
            }   
        }
        return Variables().prepareReturn(bRes);
    }
    public boolean capture_PRE_TZONE(String sParagraph,
            String sRegExPrefix, String sRegExSuffix, int[] iPrevGroupNumber, boolean bMayLeave) {
/*
$PRE_TZONE= qr/$TIME_AMPM|
 $PRE_AMPM|
 $_24TIME|
 $TIME_NAME|
 ((0[1-9]|1[0-9]|2[0-3])
 (?{$tempHour = $^N;}))[:]?(([0-5][0-9])(?{$tempMin = $^N;}))/;
				#6:30/6/six (a.m.), 18:30 
*/        
        Variables().startCapture();
        TempResultVariables tempRes = new TempResultVariables(Variables());
        tempRes.setInitialValues(iPrevGroupNumber);        
        
        if (capture_TIME_AMPM(sParagraph,
                sRegExPrefix, sRegExSuffix, iPrevGroupNumber, bMayLeave)){
            tempRes.setBestResult(iPrevGroupNumber[0]);   
            return Variables().prepareReturn(true);
        }
        
        tempRes.resetInitialValues(iPrevGroupNumber);
        if (capture_PRE_AMPM(sParagraph,
                sRegExPrefix, sRegExSuffix, iPrevGroupNumber, bMayLeave)){
            tempRes.setBestResult(iPrevGroupNumber[0]);   
            return Variables().prepareReturn(true);
        }
        
        tempRes.resetInitialValues(iPrevGroupNumber);
        if (capture_24TIME(sParagraph,
                sRegExPrefix, sRegExSuffix, iPrevGroupNumber, bMayLeave)){
            tempRes.setBestResult(iPrevGroupNumber[0]);   
            return Variables().prepareReturn(true);
        }        
        
        tempRes.resetInitialValues(iPrevGroupNumber);
        if (pAnnotator.capture_TIME_NAME(sParagraph,
                sRegExPrefix, sRegExSuffix, iPrevGroupNumber, bMayLeave)){
            tempRes.setBestResult(iPrevGroupNumber[0]);   
            return Variables().prepareReturn(true);
        }        
        
        tempRes.resetInitialValues(iPrevGroupNumber);
/*
(   
 (0[1-9]|1[0-9]|2[0-3])
 (?{$tempHour = $^N;}))
 [:]?(([0-5][0-9])(?{$tempMin = $^N;}))/;
 */
        int iRes = iPrevGroupNumber[0] + 1;
        int iHour = iRes + 1;
        int iMin = iRes + 2;
        String sPattern = ConstRegEx.groupBrackets(
                    ConstRegEx.groupBrackets("0[1-9]|1[0-9]|2[0-3]"
                    , iPrevGroupNumber) + 
                    "[:]?" +
                    ConstRegEx.groupBrackets("[0-5][0-9]"
                    , iPrevGroupNumber) 
                , iPrevGroupNumber);
        Pattern patternLex = Pattern.compile(
                ConstRegEx.prefixWordSuffix(sPattern, sRegExPrefix, sRegExSuffix)
                );
        boolean bResult = Find().printAllMatchResult(patternLex, sParagraph, false);
        if (bResult){
            try{
                Variables().saveCurrentMatch(iRes);
/*
 (?{$tempHour = $^N;}))
 [:]?(([0-5][0-9])(?{$tempMin = $^N;}))/;
 */                
                Variables().tempVariables.stempHour = 
                        Variables().tempVariables.matchResult.group(iHour);

                Variables().tempVariables.stempMin = 
                        Variables().tempVariables.matchResult.group(iMin);
                
                tempRes.setBestResult(iPrevGroupNumber[0]);
                return Variables().prepareReturn(true);
            }catch(Exception e){
                    if (!bMayLeave){ Const.writeSpecificError(e, sParagraph);}
                    Variables().tempVariables.sCurrentMatch = "";
            }
        }
        
        tempRes.prepareReturn(iPrevGroupNumber);
        return Variables().prepareReturn(tempRes.bAtLeastOneResult);
    }    
    public boolean capture_TZONEFULL(String sParagraph,
            String sRegExPrefix, String sRegExSuffix, int[] iPrevGroupNumber, boolean bMayLeave){
/*
$TZONEFULL= qr/
 ($TIMEZONEFULL)(?{$tempTZone = $map_val{lc($^N)."_TIMEZONEFULL"}})/i;
				# Greenwich Mean Time
 */     
        Variables().startCapture();
        String sPattern = ConstRegEx.groupBrackets(
                Variables().hashtbLexValues.get("TIMEZONEFULL"), iPrevGroupNumber);
        Pattern patternLex = Pattern.compile(
                ConstRegEx.prefixWordSuffix(sPattern, sRegExPrefix, sRegExSuffix),
                Pattern.CASE_INSENSITIVE
                );
        boolean bResult = Find().printAllMatchResult(patternLex, sParagraph, false);
        if (bResult){
            try {
                Variables().tempVariables.stempTZone = 
                        Variables().getMapHashValue(
                            Variables().getResult_LowerCase(iPrevGroupNumber[0])+
                            "_TIMEZONEFULL"
                        ) ;
                Variables().saveCurrentMatch(iPrevGroupNumber[0]);
            }catch(Exception e){
                    if (!bMayLeave){ Const.writeSpecificError(e, sParagraph);}
                    Variables().tempVariables.sCurrentMatch = "";
            }
        }
        return Variables().prepareReturn(bResult);
    }
    public boolean capture_TZONEABBR(String sParagraph,
            String sRegExPrefix, String sRegExSuffix, int[] iPrevGroupNumber, boolean bMayLeave){
/*
$TZONEABBR= qr/
 ($TIMEZONE)(?{$tempTZone = $map_val{$^N}; print "MATCH<$^N>";})/;
				# GMT, EST
 */        
        Variables().startCapture();
        String sPattern = ConstRegEx.groupBrackets(
                Variables().hashtbLexValues.get("TIMEZONE"), iPrevGroupNumber);
        Pattern patternLex = Pattern.compile(
                ConstRegEx.prefixWordSuffix(sPattern, sRegExPrefix, sRegExSuffix)
                );
        boolean bResult = Find().printAllMatchResult(patternLex, sParagraph, false);
        if (bResult){
            try {
                Variables().tempVariables.stempTZone = 
                        Variables().getMapHashValue(
                            Variables().tempVariables.matchResult.group(
                                iPrevGroupNumber[0]
                            )
                        );
                Variables().saveCurrentMatch(iPrevGroupNumber[0]);
            }catch(Exception e){
                    if (!bMayLeave){ Const.writeSpecificError(e, sParagraph);}
                    Variables().tempVariables.sCurrentMatch = "";
            }
        }
        return Variables().prepareReturn(bResult);
        
    }
    
    public boolean capture_TZONE(String sParagraph,
            String sRegExPrefix, String sRegExSuffix, int[] iPrevGroupNumber, boolean bMayLeave){
/*
$TZONE= qr/(?{$tempTZone = '';})
 ($TZONEFULL|
 $TZONEABBR|
 ([A-Z][A-Z]?[A-Z]?T)|
 ([Ll]ocal|LOCAL)$WS([Tt]ime|TIME)|
 ([A-Z][a-z]+$WS)+([Tt]ime|TIME))/;  
#GMT, BST, EST, local time, South African time 
 */       
        Variables().startCapture();
        Variables().tempVariables.stempTZone = "";
        
        TempResultVariables tempRes = new TempResultVariables(Variables());
        tempRes.setInitialValues(iPrevGroupNumber);
        
        if (capture_TZONEFULL(sParagraph,
                sRegExPrefix, sRegExSuffix, iPrevGroupNumber, bMayLeave)){
            tempRes.setBestResult(iPrevGroupNumber[0]);
            return Variables().prepareReturn(true);
        }
        
        tempRes.resetInitialValues(iPrevGroupNumber);
        if (capture_TZONEABBR(sParagraph,
                sRegExPrefix, sRegExSuffix, iPrevGroupNumber, bMayLeave)){
            tempRes.setBestResult(iPrevGroupNumber[0]);
            return Variables().prepareReturn(true);
        }

        tempRes.resetInitialValues(iPrevGroupNumber);
/*
 ([A-Z][A-Z]?[A-Z]?T)|
 ([Ll]ocal|LOCAL)$WS([Tt]ime|TIME)|
 ([A-Z][a-z]+$WS)+([Tt]ime|TIME))/;  
 */  
        int iRes = iPrevGroupNumber[0] + 1;
        String sPattern = ConstRegEx.groupBrackets(
                ConstRegEx.groupBrackets("[A-Z][A-Z]?[A-Z]?T", iPrevGroupNumber)
                + "|"+
                ConstRegEx.groupBrackets("[Ll]ocal|LOCAL", iPrevGroupNumber) +
                ConstRegEx.groupBrackets(ConstRegEx.WS, iPrevGroupNumber)+
                ConstRegEx.groupBrackets("[Tt]ime|TIME", iPrevGroupNumber) 
                + "|"+
                ConstRegEx.groupBrackets(
                    ConstRegEx.groupBrackets("[A-Z][a-z]+", iPrevGroupNumber) +
                    ConstRegEx.groupBrackets(ConstRegEx.WS, iPrevGroupNumber)
                , iPrevGroupNumber)+ "+"+
                ConstRegEx.groupBrackets("[Tt]ime|TIME", iPrevGroupNumber) 
                , iPrevGroupNumber);
        
        Pattern patternLex = Pattern.compile(
                ConstRegEx.prefixWordSuffix(sPattern, sRegExPrefix, sRegExSuffix));
        boolean bResult = Find().printAllMatchResult(patternLex, sParagraph, false);
        if (bResult){
            Variables().saveCurrentMatch(iRes);
            tempRes.setBestResult(iPrevGroupNumber[0]);
            return Variables().prepareReturn(true);
        }
        
        tempRes.prepareReturn(iPrevGroupNumber);
        return Variables().prepareReturn(tempRes.bAtLeastOneResult);        
    }    
    public boolean capture_TIME_TIMEZONE(String sParagraph,
            String sRegExPrefix, String sRegExSuffix, int[] iPrevGroupNumber, boolean bMayLeave) {
/*
$TIME_TIMEZONE			= qr/$PRE_TZONE$WS?,?$WS?$TZONE/;
				#6:30/6/six (a.m.)/18:30 local time/BST 
 */        
       Variables().startCapture();
       String sNewSuf = ConstRegEx.REST_ANY + sRegExSuffix;
       boolean bRes = capture_PRE_TZONE(sParagraph,
               sRegExPrefix, sNewSuf, iPrevGroupNumber, bMayLeave);
       if (bRes){
           TempSaveCurrentMatch tmpMatch = new TempSaveCurrentMatch(Variables());
           sParagraph = tmpMatch.saveMatchAndMoveOverAndReset(sParagraph);
           String sNewPrefix = ConstRegEx.newPrefix(iPrevGroupNumber);

           bRes = pAnnotator.comPrefSuf.capture_WS_op_COMMA_op_WS_op(sParagraph, 
                   sNewPrefix, sNewSuf, iPrevGroupNumber, bMayLeave);
            if (bRes) {
                sParagraph = tmpMatch.saveMatchAndMoveOverAndReset(sParagraph);
                sNewPrefix = ConstRegEx.newPrefix(iPrevGroupNumber);
                
                bRes = capture_TZONE(sParagraph, sNewPrefix, sRegExSuffix, iPrevGroupNumber, bMayLeave);
                if (bRes) {
                    tmpMatch.restoreCurrentMatch();
                }
            }
        }
       return Variables().prepareReturn(bRes); 
    }
    
    public boolean capture_TIME_IN_WORDS_TO_Case1(String sParagraph,
            String sRegExPrefix, String sRegExSuffix, int[] iPrevGroupNumber, boolean bMayLeave) {
        /*
        $TIME_IN_WORDS_TO		= qr/
        ( ($QUANTT|$TO_99_MIX)(?{$tempNo = $ordWords->val();})
        (
        ($WS(minutes?))|
        (??{($tempNo%5==0&&$tempNo<=30)?'':'WRONG';})
        )|
        ($AANONE$WS)?(quarter)(?{$tempNo = 15;})
        )
        /i;
        # ten minutes to three
         */
        Variables().startCapture();
        String sNewSuf = ConstRegEx.REST_ANY + sRegExSuffix;

        TempResultVariables tempRes = new TempResultVariables(Variables());
        tempRes.setInitialValues(iPrevGroupNumber);
        int iResult_Group = iPrevGroupNumber[0];
//2008.10.23 capture_QUANTT contains WS
        if (pAnnotator.capture_QUANTT(sParagraph, sRegExPrefix, sRegExSuffix, iPrevGroupNumber, bMayLeave)) {
            tempRes.setBestResult( iPrevGroupNumber[0]);
        }
        if (!tempRes.bAtLeastOneResult) {
            tempRes.resetInitialValues(iPrevGroupNumber);
            if (pAnnotator.capture_TO_99_MIX(sParagraph, sRegExPrefix, sNewSuf, iPrevGroupNumber, bMayLeave)) {
                tempRes.setBestResult( iPrevGroupNumber[0]);
            }
        }
        tempRes.prepareReturn(iPrevGroupNumber);
        if (tempRes.bAtLeastOneResult) {
            try {
                TempSaveCurrentMatch tmpMatch = new TempSaveCurrentMatch(Variables());
                sParagraph = tmpMatch.saveMatchAndMoveOverAndReset(sParagraph);

                iPrevGroupNumber[0] = iResult_Group;
                Variables().tempVariables.stempNo =
                        Variables().tempVariables.ordWords.get_val().toString();

                String sNewPref = ConstRegEx.newPrefix(iPrevGroupNumber);

                TempResultVariables tempRes2 = new TempResultVariables(Variables());
                tempRes2.setInitialValues(iPrevGroupNumber);
                int iResult_Group2 = iPrevGroupNumber[0] + 1;

                //($WS(minutes?))
                String sPattern = ConstRegEx.groupBrackets(
                        ConstRegEx.groupBrackets(ConstRegEx.WS, iPrevGroupNumber) +
                        ConstRegEx.groupBrackets("minutes?", iPrevGroupNumber),
                        iPrevGroupNumber);
                Pattern patternLex = Pattern.compile(
                        ConstRegEx.prefixWordSuffix(sPattern, sNewPref, sRegExSuffix));
                boolean bResult =
                        Find().printAllMatchResult(patternLex, sParagraph, false);
                if (bResult) {
                    Variables().saveCurrentMatch(iResult_Group2);
                    tempRes2.setBestResult(iPrevGroupNumber[0]);
                }

                if (!tempRes2.bAtLeastOneResult) {
                    tempRes2.resetInitialValues(iPrevGroupNumber);
                    //(??{($tempNo%5==0&&$tempNo<=30)?'':'WRONG';})
                    int iTempNo = Integer.parseInt(Variables().tempVariables.stempNo);
                    if ((iTempNo % 5 == 0) && (iTempNo <= 30)) {
                        //tempRes2.resetInitialValues(iPrevGroupNumber);
                        Variables().tempVariables.sCurrentMatch = "";
                        tempRes2.bAtLeastOneResult = true;
                    }
                }
                if (!tempRes2.bAtLeastOneResult) {
                    tempRes2.resetInitialValues(iPrevGroupNumber);
                    //($AANONE$WS)?(quarter)(?{$tempNo = 15;})
                    sPattern = ConstRegEx.groupBrackets(
                            ConstRegEx.groupBrackets(
                            ConstRegEx.groupBrackets(
                            Variables().hashtbLexValues.get("AANONE"), iPrevGroupNumber) +
                            ConstRegEx.groupBrackets(ConstRegEx.WS, iPrevGroupNumber),
                            iPrevGroupNumber) + "?" +
                            ConstRegEx.groupBrackets("quarter", iPrevGroupNumber)
                            , iPrevGroupNumber);
                    patternLex = Pattern.compile(
                            ConstRegEx.prefixWordSuffix(sPattern, sNewPref, sRegExSuffix));
                    bResult =
                            Find().printAllMatchResult(patternLex, sParagraph, false);
                    if (bResult) {
                        Variables().tempVariables.stempNo = "15";
                        Variables().saveCurrentMatch(iResult_Group2);
                        tempRes2.setBestResult(iPrevGroupNumber[0]);
                    }
                }
                tempRes2.prepareReturn(iPrevGroupNumber);
                tmpMatch.restoreCurrentMatch();

                return Variables().prepareReturn( tempRes2.bAtLeastOneResult);
            }catch(Exception e){
                    if (!bMayLeave){ Const.writeSpecificError(e, sParagraph);}
            }
            return Variables().prepareReturn(tempRes.bAtLeastOneResult);            
        } else {
            return Variables().prepareReturn(false);
        }
    }
    public boolean capture_TIME_IN_WORDS_TO_Case2(String sParagraph,
            String sRegExPrefix, String sRegExSuffix, int[] iPrevGroupNumber,boolean bMayLeave) {
/*
 (
    ($TIME_AMPM|$PRE_AMPM)(?{$crtTimePoint->minute(60-$tempNo);crtTimePoint->hour($tempHour-1);})
    |
    $THE$WS(hour)(?{$crtTimePoint->minute(60-$tempNo);$crtTimePoint->hour("XX");})
 )
 */        
        Variables().startCapture();
        TempResultVariables tempRes2 = new TempResultVariables(Variables());
        tempRes2.setInitialValues(iPrevGroupNumber);

        if (capture_TIME_AMPM(sParagraph, sRegExPrefix, sRegExSuffix, iPrevGroupNumber, bMayLeave)) {
            try {
                int iNo = Integer.parseInt(Variables().tempVariables.stempNo);
                Variables().tempVariables.crtTimePoint.set("minute", Integer.toString(60 - iNo));
                int iHour = Integer.parseInt(Variables().tempVariables.stempHour);
                Variables().tempVariables.crtTimePoint.set("hour", Integer.toString(iHour - 1));

                tempRes2.setBestResult(iPrevGroupNumber[0]);
            }catch(Exception e){
                    if (!bMayLeave){ Const.writeSpecificError(e, sParagraph);}
            }
            return Variables().prepareReturn(true);
        }

        tempRes2.resetInitialValues(iPrevGroupNumber);
        if (capture_PRE_AMPM(sParagraph, sRegExPrefix, sRegExSuffix, iPrevGroupNumber, bMayLeave)) {
            try {
                int iNo = Integer.parseInt(Variables().tempVariables.stempNo);
                Variables().tempVariables.crtTimePoint.set("minute", Integer.toString(60 - iNo));
                int iHour = Integer.parseInt(Variables().tempVariables.stempHour);
                Variables().tempVariables.crtTimePoint.set("hour", Integer.toString(iHour - 1));

                tempRes2.setBestResult(iPrevGroupNumber[0]);
            } catch(Exception e){
                    if (!bMayLeave){ Const.writeSpecificError(e, sParagraph);}
            }
            return Variables().prepareReturn(true);
        }

        tempRes2.resetInitialValues(iPrevGroupNumber);
        //$THE$WS(hour)(?{$crtTimePoint->minute(60-$tempNo);$crtTimePoint->hour("XX");})
        int iRes = iPrevGroupNumber[0]+1;
        String sPattern = ConstRegEx.groupBrackets(
                ConstRegEx.groupBrackets(
                Variables().hashtbLexValues.get("THE"), iPrevGroupNumber) +
                ConstRegEx.groupBrackets(ConstRegEx.WS, iPrevGroupNumber) +
                ConstRegEx.groupBrackets("hour", iPrevGroupNumber)
                , iPrevGroupNumber);
        Pattern patternLex = Pattern.compile(
                ConstRegEx.prefixWordSuffix(sPattern, sRegExPrefix, sRegExSuffix));
        boolean bResult =
                Find().printAllMatchResult(patternLex, sParagraph, false);
        if (bResult) {
            try {
                int iNo = Integer.parseInt(Variables().tempVariables.stempNo);
                Variables().tempVariables.crtTimePoint.set("minute", Integer.toString(60 - iNo));
                Variables().tempVariables.crtTimePoint.set("hour", "XX");

                Variables().saveCurrentMatch(iRes);
                tempRes2.setBestResult(iPrevGroupNumber[0]);
            } catch(Exception e){
                    if (!bMayLeave){ Const.writeSpecificError(e, sParagraph);}
            }
            return Variables().prepareReturn(true);
        }

        tempRes2.prepareReturn(iPrevGroupNumber);
        return Variables().prepareReturn( tempRes2.bAtLeastOneResult);
    }
    
  
    public boolean capture_TIME_IN_WORDS_TO(String sParagraph,
            String sRegExPrefix, String sRegExSuffix, int[] iPrevGroupNumber, boolean bMayLeave) {
        /*
        $TIME_IN_WORDS_TO		= qr/
        ( ($QUANTT|$TO_99_MIX)(?{$tempNo = $ordWords->val();})
        (
        ($WS(minutes?))|
        (??{($tempNo%5==0&&$tempNo<=30)?'':'WRONG';})
        )|
        ($AANONE$WS)?(quarter)(?{$tempNo = 15;})
        )
        $WS$TO$WS
        (
        ($TIME_AMPM|$PRE_AMPM)(?{$crtTimePoint->minute(60-$tempNo);crtTimePoint->hour($tempHour-1);})
        |
        $THE$WS(hour)(?{$crtTimePoint->minute(60-$tempNo);$crtTimePoint->hour("XX");})
        )
        ($WS$TZONE(?{$crtTimePoint->timeZone($tempTZone);}))?/i;
        # ten minutes to three
         */
        Variables().startCapture();
        String sNewSuf = ConstRegEx.REST_ANY + sRegExSuffix;

        TempResultVariables tempRes = new TempResultVariables(Variables());
        tempRes.setInitialValues(iPrevGroupNumber);

        if (capture_TIME_IN_WORDS_TO_Case1(sParagraph, 
                sRegExPrefix, sNewSuf, iPrevGroupNumber, bMayLeave)) {
            try {
                TempSaveCurrentMatch tmpMatch = new TempSaveCurrentMatch(Variables());
                sParagraph = tmpMatch.saveMatchAndMoveOverAndReset(sParagraph);
                String sNewPref = ConstRegEx.newPrefix(iPrevGroupNumber);
                sNewSuf = ConstRegEx.REST_ANY + sRegExSuffix;
                boolean bRes = 
                        pAnnotator.comPrefSuf.capture_WS_TO_WS
                        (sParagraph, sNewPref, sNewSuf, iPrevGroupNumber, bMayLeave);
                if (bRes) {
                    sParagraph = tmpMatch.saveMatchAndMoveOverAndReset(sParagraph);
                    sNewPref = ConstRegEx.newPrefix(iPrevGroupNumber);
                    
                    if (capture_TIME_IN_WORDS_TO_Case2(sParagraph, sNewPref,
                            sNewSuf, iPrevGroupNumber, bMayLeave)) {
                        //tempRes.setBestResult(iResult_Group2, iPrevGroupNumber[0]);    
                        tempRes.setBestResult(iPrevGroupNumber[0]);
                    }

                    tempRes.prepareReturn(iPrevGroupNumber);
                    if (tempRes.bAtLeastOneResult) {
                        tmpMatch.restoreCurrentMatch();
                    }
                    return Variables().prepareReturn(tempRes.bAtLeastOneResult);
                }
                return Variables().prepareReturn(bRes);
            }catch(Exception e){
                    if (!bMayLeave){ Const.writeSpecificError(e, sParagraph);}
            }
        }
        return Variables().prepareReturn(false);
    }

    public boolean capture_TIME_IN_WORDS_PAST_Case1(String sParagraph,
            String sRegExPrefix, String sRegExSuffix, int[] iPrevGroupNumber, boolean bMayLeave) {
        /*
        $TIME_IN_WORDS_PAST= qr/
        (($QUANTT|$TO_99_MIX)(?{$tempNo = $ordWords->val();})
        (($WS(minutes?))|(??{($tempNo%5==0&&$tempNo<=59)?'':'WRONG';}))|
        ($AANONE$WS)?(quarter)(?{$tempNo = 15;})|half(?{$tempNo = 30;}))
        /i;
        # ten minutes to three
         */
        //String sNewPref = sRegExPrefix + "(";
        Variables().startCapture();
        String sNewSuf = ConstRegEx.REST_ANY + sRegExSuffix;

        TempResultVariables tempRes = new TempResultVariables(Variables());
        tempRes.setInitialValues(iPrevGroupNumber);
//2008.10.23  capture_QUANTT contains WS
        if (pAnnotator.capture_QUANTT(sParagraph, sRegExPrefix, sRegExSuffix, iPrevGroupNumber, bMayLeave)) {
            tempRes.setBestResult( iPrevGroupNumber[0]);
        }

        if (!tempRes.bAtLeastOneResult) {
            tempRes.resetInitialValues(iPrevGroupNumber);
            if (pAnnotator.capture_TO_99_MIX(sParagraph, sRegExPrefix, sNewSuf,
                    iPrevGroupNumber, bMayLeave)) {
                tempRes.setBestResult( iPrevGroupNumber[0]);
            }
        }
        tempRes.prepareReturn(iPrevGroupNumber);
        if (tempRes.bAtLeastOneResult) {
            try {
                TempSaveCurrentMatch tmpMatch = new TempSaveCurrentMatch(Variables());
                sParagraph = tmpMatch.saveMatchAndMoveOverAndReset(sParagraph);

                Variables().tempVariables.stempNo =
                        Variables().tempVariables.ordWords.get_val().toString();

                String sNewPref = ConstRegEx.newPrefix(iPrevGroupNumber);

                TempResultVariables tempRes2 = new TempResultVariables(Variables());
                tempRes2.setInitialValues(iPrevGroupNumber);
                int iResult_Group2 = iPrevGroupNumber[0] + 1;

                //($WS(minutes?))
                String sPattern = ConstRegEx.groupBrackets(
                        ConstRegEx.groupBrackets(ConstRegEx.WS, iPrevGroupNumber) +
                        ConstRegEx.groupBrackets("minutes?", iPrevGroupNumber),
                        iPrevGroupNumber);
                Pattern patternLex = Pattern.compile(
                        ConstRegEx.prefixWordSuffix(sPattern, sNewPref, sRegExSuffix));
                boolean bResult =
                        Find().printAllMatchResult(patternLex, sParagraph, false);
                if (bResult) {
                    Variables().saveCurrentMatch(iResult_Group2);
                    tempRes2.setBestResult(iPrevGroupNumber[0]);
                }
                if (!tempRes2.bAtLeastOneResult) {
                    tempRes2.resetInitialValues(iPrevGroupNumber);
                    //(??{($tempNo%5==0&&$tempNo<=30)?'':'WRONG';})
                    int iTempNo = Integer.parseInt(Variables().tempVariables.stempNo);
                    if ((iTempNo % 5 == 0) && (iTempNo <= 59)) {
                        //tempRes2.resetInitialValues(iPrevGroupNumber);
                        Variables().tempVariables.sCurrentMatch = "";
                        tempRes2.bAtLeastOneResult=true;
                    }
                }
                if (!tempRes2.bAtLeastOneResult) {
                    tempRes2.resetInitialValues(iPrevGroupNumber);
                    //($AANONE$WS)?(quarter)(?{$tempNo = 15;})
                    sPattern = ConstRegEx.groupBrackets(
                            ConstRegEx.groupBrackets(
                            ConstRegEx.groupBrackets(
                            Variables().hashtbLexValues.get("AANONE"), iPrevGroupNumber) +
                            ConstRegEx.groupBrackets(ConstRegEx.WS, iPrevGroupNumber),
                            iPrevGroupNumber) + "?" +
                            ConstRegEx.groupBrackets("quarter", iPrevGroupNumber)
                            , iPrevGroupNumber);
                    patternLex = Pattern.compile(
                            ConstRegEx.prefixWordSuffix(sPattern, sNewPref, sRegExSuffix));
                    bResult =
                            Find().printAllMatchResult(patternLex, sParagraph, false);
                    if (bResult) {
                        Variables().tempVariables.stempNo = "15";

                        Variables().saveCurrentMatch(iResult_Group2);
                        tempRes2.setBestResult(iPrevGroupNumber[0]);

                    }
                }
                if (!tempRes2.bAtLeastOneResult) {
                    tempRes2.resetInitialValues(iPrevGroupNumber);
                    //half(?{$tempNo = 30;}))
                    sPattern = ConstRegEx.groupBrackets("half", iPrevGroupNumber);
                    patternLex = Pattern.compile(
                            ConstRegEx.prefixWordSuffix(sPattern, sNewPref, sRegExSuffix));
                    bResult =
                            Find().printAllMatchResult(patternLex, sParagraph, false);
                    if (bResult) {
                        Variables().tempVariables.stempNo = "30";

                        Variables().saveCurrentMatch(iResult_Group2);
                        tempRes2.setBestResult(iPrevGroupNumber[0]);
                    }
                }
                tempRes2.prepareReturn(iPrevGroupNumber);
                tmpMatch.restoreCurrentMatch();
                return Variables().prepareReturn( tempRes2.bAtLeastOneResult);
            }catch(Exception e){
               if (!bMayLeave){ Const.writeSpecificError(e, sParagraph);}
            }
            return Variables().prepareReturn(tempRes.bAtLeastOneResult);
        } else {
            return Variables().prepareReturn(false);
        }
    }
    
       public boolean capture_TIME_IN_WORDS_PAST_Case2(String sParagraph,
            String sRegExPrefix, String sRegExSuffix, int[] iPrevGroupNumber,boolean bMayLeave) {
/*
 (($TIME_AMPM|$PRE_AMPM)(?{$crtTimePoint->minute($tempNo);$crtTimePoint->hour($tempHour);})|
 $THE$WS(hour)(?{$crtTimePoint->minute($tempNo);$crtTimePoint->hour("XX");}))
 
 */        
        Variables().startCapture();
        int iResult_Group2 = iPrevGroupNumber[0];
        TempResultVariables tempRes2 = new TempResultVariables(Variables());
        tempRes2.setInitialValues(iPrevGroupNumber);

        if (capture_TIME_AMPM(sParagraph, sRegExPrefix, sRegExSuffix, iPrevGroupNumber, bMayLeave)) {
            try {
                int iNo = Integer.parseInt(Variables().tempVariables.stempNo);
                Variables().tempVariables.crtTimePoint.set("minute", Integer.toString(iNo));
                int iHour = Integer.parseInt(Variables().tempVariables.stempHour);
                Variables().tempVariables.crtTimePoint.set("hour", Integer.toString(iHour));

                tempRes2.setBestResult(iPrevGroupNumber[0]);
            }catch(Exception e){
                    if (!bMayLeave){ Const.writeSpecificError(e, sParagraph);}
            }
            return Variables().prepareReturn(true);
        }

        tempRes2.resetInitialValues(iPrevGroupNumber);
        if (capture_PRE_AMPM(sParagraph, sRegExPrefix, sRegExSuffix, iPrevGroupNumber, bMayLeave)) {
            try {
                int iNo = Integer.parseInt(Variables().tempVariables.stempNo);
                Variables().tempVariables.crtTimePoint.set("minute", Integer.toString(iNo));
                int iHour = Integer.parseInt(Variables().tempVariables.stempHour);
                Variables().tempVariables.crtTimePoint.set("hour", Integer.toString(iHour));

                tempRes2.setBestResult(iPrevGroupNumber[0]);
            }catch(Exception e){
                    if (!bMayLeave){ Const.writeSpecificError(e, sParagraph);}
            }
            return Variables().prepareReturn(true);
        }

        tempRes2.resetInitialValues(iPrevGroupNumber);
        //$THE$WS(hour)(?{$crtTimePoint->minute(60-$tempNo);$crtTimePoint->hour("XX");})
        String sPattern = ConstRegEx.groupBrackets(
                Variables().hashtbLexValues.get("THE"), iPrevGroupNumber) +
                ConstRegEx.groupBrackets(ConstRegEx.WS, iPrevGroupNumber) +
                ConstRegEx.groupBrackets("hour", iPrevGroupNumber);
        Pattern patternLex = Pattern.compile(
                ConstRegEx.prefixWordSuffix(sPattern, sRegExPrefix, sRegExSuffix));
        boolean bResult =
                Find().printAllMatchResult(patternLex, sParagraph, false);
        if (bResult) {
            try {
                int iNo = Integer.parseInt(Variables().tempVariables.stempNo);
                Variables().tempVariables.crtTimePoint.set("minute", Integer.toString(iNo));
                Variables().tempVariables.crtTimePoint.set("hour", "XX");

                Variables().saveCurrentMatch(iResult_Group2);
                tempRes2.setBestResult(iPrevGroupNumber[0]);
            }catch(Exception e){
                    if (!bMayLeave){ Const.writeSpecificError(e, sParagraph);}
            }
            return Variables().prepareReturn(true);
        }

        tempRes2.prepareReturn(iPrevGroupNumber);
        return Variables().prepareReturn( tempRes2.bAtLeastOneResult);
    }
       
    public boolean capture_TIME_IN_WORDS_PAST(String sParagraph,
            String sRegExPrefix, String sRegExSuffix, int[] iPrevGroupNumber,boolean bMayLeave){
/*
$TIME_IN_WORDS_PAST= qr/
 (($QUANTT|$TO_99_MIX)(?{$tempNo = $ordWords->val();})
 (($WS(minutes?))|(??{($tempNo%5==0&&$tempNo<=59)?'':'WRONG';}))|
    ($AANONE$WS)?(quarter)(?{$tempNo = 15;})|half(?{$tempNo = 30;}))
 $WS$PAST$WS
 (($TIME_AMPM|$PRE_AMPM)(?{$crtTimePoint->minute($tempNo);$crtTimePoint->hour($tempHour);})|
 $THE$WS(hour)(?{$crtTimePoint->minute($tempNo);$crtTimePoint->hour("XX");}))
 ($WS$TZONE(?{$crtTimePoint->timeZone($tempTZone);}))?/i;
# half past four
 */    
        Variables().startCapture();
        String sNewSuf = ConstRegEx.REST_ANY + sRegExSuffix;

        TempResultVariables tempRes = new TempResultVariables(Variables());
        tempRes.setInitialValues(iPrevGroupNumber);
      
        if (capture_TIME_IN_WORDS_PAST_Case1(sParagraph, sRegExPrefix, sNewSuf, 
                iPrevGroupNumber, bMayLeave)){
            try {
                TempSaveCurrentMatch tmpMatch = new TempSaveCurrentMatch(Variables());
                sParagraph = tmpMatch.saveMatchAndMoveOverAndReset(sParagraph);

                String sNewPref = ConstRegEx.newPrefix(iPrevGroupNumber);
                sNewSuf = ConstRegEx.REST_ANY + sRegExSuffix;
                boolean bRes = pAnnotator.comPrefSuf.capture_WS_PAST_WS(
                        sParagraph, sNewPref, sNewSuf, iPrevGroupNumber, bMayLeave);
                if (bRes) {
                    sParagraph = tmpMatch.saveMatchAndMoveOverAndReset(sParagraph);    
                    sNewPref = ConstRegEx.newPrefix(iPrevGroupNumber);
                    
                    if (capture_TIME_IN_WORDS_PAST_Case2(sParagraph, sNewPref,
                            sNewSuf, iPrevGroupNumber, bMayLeave)) {
                        tempRes.setBestResult(iPrevGroupNumber[0]);
                    }

                    tempRes.prepareReturn(iPrevGroupNumber);
                    if (tempRes.bAtLeastOneResult) {
                        tmpMatch.restoreCurrentMatch();
                    }
                    return Variables().prepareReturn(tempRes.bAtLeastOneResult);
                }
                return Variables().prepareReturn(bRes);
            }catch(Exception e){
                    if (!bMayLeave){ Const.writeSpecificError(e, sParagraph);}
            }
        }
        return Variables().prepareReturn(false);       
    }

 public boolean capture_SIMPLE_TIMES(String sParagraph,
            String sRegExPrefix, String sRegExSuffix, int[] iPrevGroupNumber,boolean bMayLeave){
/*
$SIMPLE_TIMES= 
 qr/($TIME_TIMEZONE|$TIME_AMPM|$_12TIME|$NUM_OCLOCK|$_24TIME|$TIME_NAME)
 (?{$crtTimePoint->hour($tempHour); 
 $crtTimePoint->minute($tempMin); 
 $crtTimePoint->second($tempSec); 
 $crtTimePoint->miliSec($tempMiliSec);
 $crtTimePoint->timeZone($tempTZone);$tempMin='';$tempSec='';$tempMiliSec =''})/;
 */     
     Variables().startCapture();
        TempResultVariables tempRes = new TempResultVariables(Variables());
        tempRes.setInitialValues(iPrevGroupNumber);

        if (capture_TIME_TIMEZONE(sParagraph,
                sRegExPrefix, sRegExSuffix, iPrevGroupNumber, bMayLeave)){
            tempRes.setBestResult(iPrevGroupNumber[0]);
        }
        
        if (!tempRes.bAtLeastOneResult){
            tempRes.resetInitialValues(iPrevGroupNumber);
            if (capture_TIME_AMPM(sParagraph, 
                    sRegExPrefix, sRegExSuffix, iPrevGroupNumber, bMayLeave)){
                tempRes.setBestResult(iPrevGroupNumber[0]);
            }
        }
        
        if (!tempRes.bAtLeastOneResult){
            tempRes.resetInitialValues(iPrevGroupNumber);
            if (capture_12TIME(sParagraph, 
                    sRegExPrefix, sRegExSuffix, iPrevGroupNumber, bMayLeave)){
                tempRes.setBestResult(iPrevGroupNumber[0]);
            }
        }
        
        if (!tempRes.bAtLeastOneResult){
            tempRes.resetInitialValues(iPrevGroupNumber);
            if (capture_NUM_OCLOCK(sParagraph, 
                    sRegExPrefix, sRegExSuffix, iPrevGroupNumber, bMayLeave)){
                tempRes.setBestResult(iPrevGroupNumber[0]);
            }
        }
        
        if (!tempRes.bAtLeastOneResult){
            tempRes.resetInitialValues(iPrevGroupNumber);
            if (capture_24TIME(sParagraph, 
                    sRegExPrefix, sRegExSuffix, iPrevGroupNumber, bMayLeave)){
                tempRes.setBestResult(iPrevGroupNumber[0]);
            }
        }

        if (!tempRes.bAtLeastOneResult){
            tempRes.resetInitialValues(iPrevGroupNumber);
            if (pAnnotator.capture_TIME_NAME(sParagraph, 
                    sRegExPrefix, sRegExSuffix, iPrevGroupNumber, bMayLeave)){
                tempRes.setBestResult(iPrevGroupNumber[0]);
            }
        }
        tempRes.prepareReturn(iPrevGroupNumber);
        if (tempRes.bAtLeastOneResult){
            try{
                Variables().tempVariables.crtTimePoint.set("hour", 
                        Variables().tempVariables.stempHour);
                
                Variables().tempVariables.crtTimePoint.set("minute", 
                        Variables().tempVariables.stempMin);                
                
                Variables().tempVariables.crtTimePoint.set("second", 
                        Variables().tempVariables.stempSec);                                
                
                Variables().tempVariables.crtTimePoint.set("miliSec", 
                        Variables().tempVariables.stempMiliSec);   
             
                Variables().tempVariables.crtTimePoint.set("timeZone",
                        Variables().tempVariables.stempTZone);
                
                Variables().tempVariables.stempMin = "";
                Variables().tempVariables.stempSec = "";
                Variables().tempVariables.stempMiliSec = "";
                
            }catch(Exception e){
                    //if (!bMayLeave){ Const.writeSpecificError(e, sParagraph);}
            }
        }
        return Variables().prepareReturn(tempRes.bAtLeastOneResult);
    }
    public boolean capture_SIMPLE_TIME_ALL(String sParagraph,
            String sRegExPrefix, String sRegExSuffix, int[] iPrevGroupNumber,
            boolean bMayLeave) {
/*
$SIMPLE_TIME_ALL= qr/
 $TIME_IN_WORDS_TO|
 $TIME_IN_WORDS_PAST|
 $SIMPLE_TIMES/; # deleted |(($PRE_AMPM)(?{$crtTimePoint->hour($tempHour);}))
 */        
        Variables().startCapture();
        TempResultVariables tempRes = new TempResultVariables(Variables());
        tempRes.setInitialValues(iPrevGroupNumber);

        if (capture_TIME_IN_WORDS_TO(sParagraph,
                sRegExPrefix, sRegExSuffix, iPrevGroupNumber, bMayLeave)){
            tempRes.setBestResult(iPrevGroupNumber[0]);
            return Variables().prepareReturn(true);
        }

        tempRes.resetInitialValues(iPrevGroupNumber);
        if (capture_TIME_IN_WORDS_PAST(sParagraph, sRegExPrefix, sRegExSuffix, iPrevGroupNumber, bMayLeave)) {
            tempRes.setBestResult(iPrevGroupNumber[0]);
            return Variables().prepareReturn(true);
        }

        tempRes.resetInitialValues(iPrevGroupNumber);
        if (capture_SIMPLE_TIMES(sParagraph, sRegExPrefix, sRegExSuffix, iPrevGroupNumber, bMayLeave)) {
            tempRes.setBestResult(iPrevGroupNumber[0]);
            return Variables().prepareReturn(true);
        }
        
        tempRes.prepareReturn(iPrevGroupNumber);
        return Variables().prepareReturn(tempRes.bAtLeastOneResult);
    } 
//#==================== COMPLEX TIMES =====================    
    public boolean capture_TIME_DAY_DAY_PART_Case1(String sParagraph,
            String sRegExPrefix, String sRegExSuffix, int[] iPrevGroupNumber,
            boolean bMayLeave) {
/*
 * ($DAY_NAME|in$WS$THE)/i
 */      
        Variables().startCapture();
        TempResultVariables tempRes = new TempResultVariables(Variables());
        tempRes.setInitialValues(iPrevGroupNumber);

        if (pAnnotator.dateTE.capture_DAY_NAME(sParagraph, 
                sRegExPrefix, sRegExSuffix, iPrevGroupNumber, bMayLeave)){
            tempRes.setBestResult(iPrevGroupNumber[0]);
            return Variables().prepareReturn(true);
        }
        
        tempRes.resetInitialValues(iPrevGroupNumber);
        //in$WS$THE
        int iRes = iPrevGroupNumber[0] + 1;
        String sPattern = ConstRegEx.groupBrackets(
                    "in"+
                    ConstRegEx.groupBrackets(ConstRegEx.WS, iPrevGroupNumber)+
                    ConstRegEx.groupBrackets(
                        Variables().hashtbLexValues.get("THE"), iPrevGroupNumber)
                , iPrevGroupNumber);
        Pattern patternLex = Pattern.compile(
                ConstRegEx.prefixWordSuffix(sPattern, sRegExPrefix, sRegExSuffix),
                Pattern.CASE_INSENSITIVE);
        boolean bResult =
                Find().printAllMatchResult(patternLex, sParagraph, false);
        if (bResult){
            Variables().saveCurrentMatch(iRes);
            tempRes.setBestResult(iPrevGroupNumber[0]);
            return Variables().prepareReturn(true);
        }
        
        tempRes.prepareReturn(iPrevGroupNumber);
        return Variables().prepareReturn(tempRes.bAtLeastOneResult);
    }    
    public boolean capture_TIME_DAY_DAY_PART(String sParagraph,
            String sRegExPrefix, String sRegExSuffix, int[] iPrevGroupNumber,
            boolean bMayLeave) {
/*
$TIME_DAY_DAY_PART=qr/
 $SIMPLE_TIME_ALL$WS
 ($DAY_NAME|in$WS$THE)
 $WS$DAY_PART
 (?{$crtTimePoint->hour(int($tempHour)+12)
    if ((index("AFEVNI",$tempTU) != -1) && (int($tempHour)<12));
 $crtTimePoint->hour("00") if ((index("EVNI",$tempTU) != -1) && (int($tempHour)==12));})/i;
#2 o'clock EST Wednesday afternoon 
 */        
        Variables().startCapture();
        String sNewSuf = ConstRegEx.REST_ANY + sRegExSuffix;
        boolean bRes = capture_SIMPLE_TIME_ALL(sParagraph,
                sRegExPrefix, sNewSuf, iPrevGroupNumber, bMayLeave);
        if (bRes){
            try{
                TempSaveCurrentMatch tmpMatch = new TempSaveCurrentMatch(Variables());
                sParagraph = tmpMatch.saveMatchAndMoveOverAndReset(sParagraph);
                String sNewPref = ConstRegEx.newPrefix(iPrevGroupNumber);
                
                bRes = pAnnotator.comPrefSuf.capture_WS(sParagraph,
                        sNewPref, sNewSuf, iPrevGroupNumber, bMayLeave);
                if (bRes) {
                    sParagraph = tmpMatch.saveMatchAndMoveOverAndReset(sParagraph);
                    sNewPref = ConstRegEx.newPrefix(iPrevGroupNumber);

                    bRes = capture_TIME_DAY_DAY_PART_Case1(sParagraph,
                            sNewPref, sNewSuf, iPrevGroupNumber, bMayLeave);
                    if (bRes) {
                        try {
                            sParagraph = tmpMatch.saveMatchAndMoveOverAndReset(sParagraph);
                            sNewPref = ConstRegEx.newPrefix(iPrevGroupNumber);

                            bRes = pAnnotator.comPrefSuf.capture_WS(sParagraph,
                                    sNewPref, sNewSuf, iPrevGroupNumber, bMayLeave);
                            if (bRes) {
                                sParagraph = tmpMatch.saveMatchAndMoveOverAndReset(sParagraph);
                                sNewPref = ConstRegEx.newPrefix(iPrevGroupNumber);

                                bRes = pAnnotator.capture_DAY_PART(sParagraph,
                                        sNewPref, sRegExSuffix, iPrevGroupNumber, bMayLeave);
                                if (bRes) {
                                    try {
                                        /*
                                        (?{$crtTimePoint->hour(int($tempHour)+12)
                                        if ((index("AFEVNI",$tempTU) != -1) && (int($tempHour)<12));
                                        $crtTimePoint->hour("00") if ((index("EVNI",$tempTU) != -1) && (int($tempHour)==12));})
                                         */
                                        String sIndex = "AFEVNI";
                                        int iHour = Integer.parseInt(Variables().tempVariables.stempHour);
                                        if ((sIndex.indexOf(Variables().tempVariables.stempTU) != -1) &&
                                                (iHour < 12)) {
                                            Variables().tempVariables.crtTimePoint.set("hour",
                                                    Integer.toString(iHour + 12));
                                        }
                                        sIndex = "EVNI";
                                        if ((sIndex.indexOf(Variables().tempVariables.stempTU) != -1) &&
                                                (iHour == 12)) {
                                            Variables().tempVariables.crtTimePoint.set("hour",
                                                    "00");
                                        }

                                        tmpMatch.restoreCurrentMatch();
                                    } catch (Exception e) {
                                        if (!bMayLeave) {
                                            Const.writeSpecificError(e, sParagraph);
                                        }
                                    }
                                }
                            }
                        } catch (Exception e) {
                            if (!bMayLeave) {
                                Const.writeSpecificError(e, sParagraph);
                            }
                        }
                    }
                }
                
            }catch(Exception e){
                    if (!bMayLeave){ Const.writeSpecificError(e, sParagraph);}
            }
        }
        return Variables().prepareReturn(bRes);
    } 
    
//#============= DAY-PARTS IN CONTEXT ====================== 
    public boolean capture_PARTS_TIME_UNIT_Suf(String sParagraph,String sRegExPrefix, String sRegExSuffix, 
             int[] iPrevGroupNumber,boolean bMayLeave){
        Variables().startCapture();
        int iPref = iPrevGroupNumber[0] + 1;
        
        String sPattern = ConstRegEx.groupBrackets(
                ConstRegEx.groupBrackets(ConstRegEx.WS, iPrevGroupNumber)+
                ConstRegEx.groupBrackets(
                    ConstRegEx.groupBrackets(Variables().hashtbLexValues.get("OF")
                    , iPrevGroupNumber)+
                    ConstRegEx.groupBrackets(ConstRegEx.WS, iPrevGroupNumber)
                , iPrevGroupNumber) + "?"+
                ConstRegEx.groupBrackets(
                    ConstRegEx.groupBrackets(Variables().hashtbLexValues.get("AANONE")
                    , iPrevGroupNumber)+
                    ConstRegEx.groupBrackets(ConstRegEx.WS, iPrevGroupNumber)
                , iPrevGroupNumber) + "?" 
                , iPrevGroupNumber);
        Pattern patternLex = Pattern.compile(
                ConstRegEx.prefixWordSuffix(sPattern, sRegExPrefix, sRegExSuffix),
                Pattern.CASE_INSENSITIVE
                );
        boolean bResult = Find().printAllMatchResult(patternLex, sParagraph, false);
        if (bResult) 
        {
            Variables().saveCurrentMatch(iPref);
        }
        return Variables().prepareReturn(bResult);
        
    }    
    public boolean capture_PARTS_TIME_UNIT(String sParagraph,String sRegExPrefix, String sRegExSuffix, 
             int[] iPrevGroupNumber,boolean bMayLeave){
/*
$PARTS_TIME_UNIT		= 
 qr/($PARTS)$WS($OF$WS)?($AANONE$WS)?$TIME_UNIT_LEX(?{$crtTimePoint->val("$tempPart$tempTU");})/;  
 */     
        Variables().startCapture();
        String sNewSuffix = ConstRegEx.REST_ANY + sRegExSuffix;

        if (pAnnotator.capture_PARTS(sParagraph, sRegExPrefix, sNewSuffix, iPrevGroupNumber, bMayLeave)){
            try {
                TempSaveCurrentMatch tmpMatch = new TempSaveCurrentMatch(Variables());
                sParagraph = tmpMatch.saveMatchAndMoveOverAndReset(sParagraph);

                String sNewPrefix = ConstRegEx.newPrefix(iPrevGroupNumber);
                boolean bResult;
                bResult = capture_PARTS_TIME_UNIT_Suf(sParagraph,
                        sNewPrefix, sNewSuffix, iPrevGroupNumber, bMayLeave);
                if (bResult) {
                    sParagraph = tmpMatch.saveMatchAndMoveOverAndReset(sParagraph);
                    sNewPrefix = ConstRegEx.newPrefix(iPrevGroupNumber);

                    bResult = pAnnotator.capture_TIME_UNIT_LEX(sParagraph,
                            sNewPrefix, sRegExSuffix, iPrevGroupNumber, bMayLeave);
                    if (bResult) {
                        try {
                            Variables().tempVariables.crtTimePoint.set("val",
                                    Variables().tempVariables.stempPart +
                                    Variables().tempVariables.stempTU);

                            tmpMatch.restoreCurrentMatch();
                        } catch (Exception e) {
                            if (!bMayLeave) {
                                System.out.println(e.toString());
                            }
                        }
                    }
                }
                return Variables().prepareReturn(bResult);
            }catch(Exception e){
                if (!bMayLeave){ Const.writeSpecificError(e, sParagraph);}
            }   
        }
        return Variables().prepareReturn(false);
        
    }
    
    //#=================== DURATIONS ==========================   
    public boolean capture_NUM_SINGULAR_TIME_Case1(String sParagraph,
            String sRegExPrefix, String sRegExSuffix, int[] iPrevGroupNumber,
            boolean bMayLeave) {
/*
$NUM_SINGULAR_TIME= 
 qr/
 (?{$tempPart = '';$tempSingularTime = '';})
 $AANONE?$WS?($TUN)(?{$tempTU=$map_val{lc($^N)."_TUN"};})
 ($WS$AND_PARTS)?(?{$tempSingularTime = "1$tempPart$tempTU";})
 * /i
 */      
        Variables().startCapture();
        Variables().tempVariables.stempPart = "";
        Variables().tempVariables.stempSingularTime = "";

        int iRes = iPrevGroupNumber[0] + 1;
        String sPattern = ConstRegEx.groupBrackets(
                //(AANONE WS)?
                    ConstRegEx.groupBrackets(
                        ConstRegEx.groupBrackets(
                            Variables().hashtbLexValues.get("AANONE"), iPrevGroupNumber)+
                        ConstRegEx.groupBrackets(ConstRegEx.WS, iPrevGroupNumber) 
                    , iPrevGroupNumber)+ "?"+
                    ConstRegEx.groupBrackets(
                        Variables().hashtbLexValues.get("TUN"), iPrevGroupNumber)
                , iPrevGroupNumber);
        Pattern patternLex = Pattern.compile(
                ConstRegEx.prefixWordSuffix(sPattern, sRegExPrefix, sRegExSuffix),
                Pattern.CASE_INSENSITIVE);
        boolean bResult =
                Find().printAllMatchResult(patternLex, sParagraph, false);
        if (bResult){
            try{
                Variables().saveCurrentMatch(iRes);
                
                Variables().tempVariables.stempTU =
                        Variables().getMapHashValue(
                            Variables().getResult_LowerCase(iPrevGroupNumber[0]) +
                            "_TUN"
                        );
                
                TempSaveCurrentMatch tmpMatch = new TempSaveCurrentMatch(Variables());
                sParagraph = tmpMatch.saveMatchAndMoveOverAndReset(sParagraph);
                String sNewPref = ConstRegEx.newPrefix(iPrevGroupNumber);
                
                boolean bResult2 = pAnnotator.capture_WS_AND_PART_op(sParagraph,
                        sNewPref, sRegExSuffix, iPrevGroupNumber, bMayLeave);      
                if (bResult2)
                {
                    try {
                        //(?{$tempSingularTime = "1$tempPart$tempTU";})
                        Variables().tempVariables.stempSingularTime =
                                "1" + Variables().tempVariables.stempPart+
                                Variables().tempVariables.stempTU;
                        
                        tmpMatch.restoreCurrentMatch();
                    } catch (Exception e) {
                        if (!bMayLeave) {Const.writeSpecificError(e, sParagraph);}
                    }
                }
                
            }catch(Exception e){
                    if (!bMayLeave){ Const.writeSpecificError(e, sParagraph);}
            }
        }
        return Variables().prepareReturn(bResult);
    }    


   public boolean capture_NUM_SINGULAR_TIME_Case2(String sParagraph,
            String sRegExPrefix, String sRegExSuffix, int[] iPrevGroupNumber,
            boolean bMayLeave) {
/*
$NUM_SINGULAR_TIME= 
 $AANONE$WS$AND_PARTS$WS
 ($PLURAL_TIME_UNIT_LEX|$TIME_UNIT_LEX|$PLURAL_DAY_PART)
 (?{$tempSingularTime="1$tempPart$tempTU"})/i;* 
 */       
        Variables().startCapture();
        String sNewSuf = ConstRegEx.REST_ANY + sRegExSuffix;
        boolean bRes = pAnnotator.comPrefSuf.capture_AANONE_WS(sParagraph, 
                sRegExPrefix, sNewSuf, iPrevGroupNumber, bMayLeave);
       if (bRes) {
           TempSaveCurrentMatch tmpMatch = new TempSaveCurrentMatch(Variables());
           sParagraph = tmpMatch.saveMatchAndMoveOverAndReset(sParagraph);
           
           String sNewPref = ConstRegEx.newPrefix(iPrevGroupNumber);  
           bRes = pAnnotator.capture_AND_PARTS(sParagraph,
                   sNewPref, sNewSuf, iPrevGroupNumber, bMayLeave);
           if (bRes) {
               try {
                   sParagraph = tmpMatch.saveMatchAndMoveOverAndReset(sParagraph);

                   sNewPref = ConstRegEx.newPrefix(iPrevGroupNumber);
                   bRes = pAnnotator.comPrefSuf.capture_WS(sParagraph, 
                           sNewPref, sNewSuf, iPrevGroupNumber, bMayLeave);
                   if (bRes) {
                       sParagraph = tmpMatch.saveMatchAndMoveOverAndReset(sParagraph);
                       sNewPref = ConstRegEx.newPrefix(iPrevGroupNumber);

                       TempResultVariables tempRes = new TempResultVariables(Variables());
                       tempRes.setInitialValues(iPrevGroupNumber);

                       if (pAnnotator.capture_PLURAL_TIME_UNIT_LEX(sParagraph,
                               sNewPref, sRegExSuffix, iPrevGroupNumber, bMayLeave)) {
                           tempRes.setBestResult(iPrevGroupNumber[0]);
                       }

                       if (!tempRes.bAtLeastOneResult) {
                           tempRes.resetInitialValues(iPrevGroupNumber);
                           if (pAnnotator.capture_TIME_UNIT_LEX(sParagraph,
                                   sNewPref, sRegExSuffix, iPrevGroupNumber, bMayLeave)) {
                               tempRes.setBestResult(iPrevGroupNumber[0]);
                           }
                       }

                       if (!tempRes.bAtLeastOneResult) {
                           tempRes.resetInitialValues(iPrevGroupNumber);
                           if (pAnnotator.capture_PLURAL_DAY_PART(sParagraph,
                                   sNewPref, sRegExSuffix, iPrevGroupNumber, bMayLeave)) {
                               tempRes.setBestResult(iPrevGroupNumber[0]);
                           }
                       }
                       if (tempRes.bAtLeastOneResult) {
                           try {
                               //(?{$tempSingularTime="1$tempPart$tempTU"})/i;* 
                               Variables().tempVariables.stempSingularTime =
                                       "1" + Variables().tempVariables.stempPart +
                                       Variables().tempVariables.stempTU;

                               tmpMatch.restoreCurrentMatch();
                           } catch (Exception e) {
                               if (!bMayLeave) {
                                   Const.writeSpecificError(e, sParagraph);
                               }
                           }
                       }
                       return Variables().prepareReturn(tempRes.bAtLeastOneResult);
                   }
               } catch (Exception e) {
                   if (!bMayLeave) {
                       Const.writeSpecificError(e, sParagraph);
                   }
               }
           }
       }
       return Variables().prepareReturn(bRes);
   }    
    public boolean capture_NUM_SINGULAR_TIME(String sParagraph,
            String sRegExPrefix, String sRegExSuffix, int[] iPrevGroupNumber,
            boolean bMayLeave) {
/*
$NUM_SINGULAR_TIME= 
 qr/
 (?{$tempPart = '';$tempSingularTime = '';})
 $AANONE?$WS?($TUN)(?{$tempTU=$map_val{lc($^N)."_TUN"};})
 ($WS$AND_PARTS)?(?{$tempSingularTime = "1$tempPart$tempTU";})
 |$AANONE$WS$AND_PARTS$WS
 ($PLURAL_TIME_UNIT_LEX|$TIME_UNIT_LEX|$PLURAL_DAY_PART)
 (?{$tempSingularTime="1$tempPart$tempTU"})/i;
#"an hour, one and a quarter hours"
 */        
        Variables().startCapture();
        TempResultVariables tempRes = new TempResultVariables(Variables());
        tempRes.setInitialValues(iPrevGroupNumber);

        if (capture_NUM_SINGULAR_TIME_Case1(sParagraph,
                sRegExPrefix, sRegExSuffix, iPrevGroupNumber, bMayLeave)){
            tempRes.setBestResult(iPrevGroupNumber[0]);
            return Variables().prepareReturn(true);
        }
        
        tempRes.resetInitialValues(iPrevGroupNumber);
        if (capture_NUM_SINGULAR_TIME_Case2(sParagraph, 
                sRegExPrefix, sRegExSuffix, iPrevGroupNumber, bMayLeave)){
            tempRes.setBestResult(iPrevGroupNumber[0]);
            return Variables().prepareReturn(true);
        }
        
        tempRes.prepareReturn(iPrevGroupNumber);
        return Variables().prepareReturn(tempRes.bAtLeastOneResult);
    }    
  
    public boolean capture_RECURSIVE_TIME(String sParagraph,
            String sRegExPrefix, String sRegExSuffix, int[] iPrevGroupNumber,
            boolean bMayLeave) {
/*
$RECURSIVE_TIME= 
qr/(?{local $tRT2='';})
 (
    $NUM_PLURAL_TIME(?{$tRT1=$tempNumPluralTime;})|
    $NUM_SINGULAR_TIME(?{$tRT1=$tempSingularTime;})
 )
 (
 ($WS($AND|$PUNCT)?$WS?)
 ($NUM_PLURAL_TIME(?{local $tRT2=$tRT2.$tempNumPluralTime;})|
 $N`UM_SINGULAR_TIME(?{local $tRT2=$tRT2.$tempSingularTime;}))
 )*
 (?{$tempRecursiveTime=$tRT1.$tRT2;})/;
#"two hours, three minutes and ten seconds"
 */        
        Variables().startCapture();
        String[] stRT2 =new String[1]; stRT2[0] = "";
        //String sNewSuffix =  ConstRegEx.REST_ANY + sRegExSuffix;//2008.10.22
       
        TempResultVariables tempRes = new TempResultVariables(Variables());
        tempRes.setInitialValues(iPrevGroupNumber);
        
        @SuppressWarnings("unused")
        boolean bPluralDate = false;
        boolean[] bSingularTime = new boolean[1]; bSingularTime[0] = false;
        
        if (capture_NUM_PLURAL_TIME(sParagraph,
                sRegExPrefix, sRegExSuffix, iPrevGroupNumber, bMayLeave)){
            bPluralDate = tempRes.setBestResult( iPrevGroupNumber[0]);    
        }
        
        if (!tempRes.bAtLeastOneResult) {
            tempRes.resetInitialValues(iPrevGroupNumber);
            if (capture_NUM_SINGULAR_TIME(sParagraph, 
                    sRegExPrefix, sRegExSuffix, iPrevGroupNumber, bMayLeave)) {
                bSingularTime[0] = tempRes.setBestResult(iPrevGroupNumber[0]);
            }
        }
        tempRes.prepareReturn(iPrevGroupNumber);
        if (tempRes.bAtLeastOneResult) {
            try {
                if (bSingularTime[0]) {
                    Variables().tempVariables.stRT1 =
                            Variables().tempVariables.stempSingularTime;
                } else {
                    Variables().tempVariables.stRT1 =
                            Variables().tempVariables.stempNumPluralTime;
                }
                TempSaveCurrentMatch tmpMatch = new TempSaveCurrentMatch(Variables());
                sParagraph = tmpMatch.saveMatchAndMoveOverAndReset(sParagraph);
                String sNewPrefix = ConstRegEx.newPrefix(iPrevGroupNumber);
                
                boolean bRes = pAnnotator.capture_Recursive_TIME_Suffix(sParagraph, 
                        sNewPrefix, sRegExSuffix, iPrevGroupNumber, bMayLeave, bSingularTime, stRT2);

                if (bRes) 
                {
                        try {
// (?{$tempRecursiveTime=$tRT1.$tRT2;})/;                        
                            Variables().tempVariables.stempRecursiveTime =
                                    Variables().tempVariables.stRT1 + stRT2[0];

                                tmpMatch.restoreCurrentMatch();
                        } catch (Exception e) {
                            if (!bMayLeave) {
                                System.out.println(e.toString());
                            }
                        }
                }
                return Variables().prepareReturn(bRes);
            }catch(Exception e){
                if (!bMayLeave){ Const.writeSpecificError(e, sParagraph);}
            }
        }
        return Variables().prepareReturn(tempRes.bAtLeastOneResult);
        
    }    
 
    public boolean capture_DURATION_TIME_Suffix1(String sParagraph,
            String sRegExPrefix, String sRegExSuffix, int[] iPrevGroupNumber,
            boolean bMayLeave) {
/*
 * 
 */        
            Variables().startCapture();
            int iSuf = iPrevGroupNumber[0] + 1;
            int[] iSuffixGroupNumber = new int[1];iSuffixGroupNumber[0] = 0;        
            String sPattern  = 
                    ConstRegEx.groupBrackets(
                        ConstRegEx.groupBrackets(ConstRegEx.WS
                        , iSuffixGroupNumber) + "?" +
                        ConstRegEx.groupBrackets(
                        Variables().hashtbLexValues.get("LONG"),
                        iSuffixGroupNumber) + "?" +
                        ConstRegEx.groupBrackets(
                        ConstRegEx.groupBrackets(
                        ConstRegEx.WS,
                        iSuffixGroupNumber) +
                        ConstRegEx.groupBrackets(
                        Variables().hashtbLexValues.get("POST_DUR_MOD"),
                        iSuffixGroupNumber), iSuffixGroupNumber) + "?"
                    , iSuffixGroupNumber);        
        Pattern patternLex = Pattern.compile(
                ConstRegEx.prefixWordSuffix(sPattern, sRegExPrefix, sRegExSuffix),
                Pattern.CASE_INSENSITIVE);
        boolean bResult = Find().printAllMatchResult(patternLex, sParagraph, false);
        if (bResult) 
        {
            Variables().saveCurrentMatch(iSuf);
        }
        return Variables().prepareReturn(true);
           
    }    
   public boolean capture_DUR_MOD_WS_op(String sParagraph,
            String sRegExPrefix, String sRegExSuffix, int[] iPrevGroupNumber,
            boolean bMayLeave) {
/*
 $DURATION_TIME= 
  (( ($DUR_MOD)(?{$tempMod = $map_val{lc($^N)."_DUR_MOD"};})  )$WS)?
 * /i;
 */   
        Variables().startCapture();
        int iPref = iPrevGroupNumber[0] + 1;
        int iDur_Mod_Number = iPrevGroupNumber[0] + 3;
        String sPattern = 
                ConstRegEx.groupBrackets(
                    ConstRegEx.groupBrackets(
                        ConstRegEx.groupBrackets(Variables().hashtbLexValues.get("DUR_MOD")
                        , iPrevGroupNumber) +
                        ConstRegEx.groupBrackets(ConstRegEx.WS
                            , iPrevGroupNumber)
                    , iPrevGroupNumber) + "?"
                , iPrevGroupNumber);
        Pattern patternLex = Pattern.compile(
                ConstRegEx.prefixWordSuffix(sPattern, sRegExPrefix, sRegExSuffix),
                Pattern.CASE_INSENSITIVE
                );
        @SuppressWarnings("unused")
        boolean bResult = Find().printAllMatchResult(patternLex, sParagraph, false);
        //if (bResult) //?
        {
            try {
                Variables().saveCurrentMatch(iPref);

                if (Variables().tempVariables.matchResult.start(iDur_Mod_Number) != -1) {
                    String sCapturedStr =
                            Variables().getResult_LowerCase(iDur_Mod_Number);
                    Variables().tempVariables.stempMod =
                            Variables().getMapHashValue(sCapturedStr + "_DUR_MOD");
                }
            }catch(Exception e){
                if (!bMayLeave){ Const.writeSpecificError(e, sParagraph);}
            } 
        }
        return Variables().prepareReturn(true);

    }         
    public boolean capture_DURATION_TIME(String sParagraph,
            String sRegExPrefix, String sRegExSuffix, int[] iPrevGroupNumber,
            boolean bMayLeave) {
/*
$DURATION_TIME= 
 qr/
 (?{$tempMod = '';})
 (( ($DUR_MOD)(?{$tempMod = $map_val{lc($^N)."_DUR_MOD"};})  )$WS)?
 (
 $RECURSIVE_TIME(?{$crtTimePoint->val($tempRecursiveTime);})|
 $PARTS_TIME_UNIT
 )
 $WS?$LONG?(?{$crtTimePoint->mod($tempMod);})
 
 ($WS$POST_DUR_MOD)?/i;
				#"nearly two hours long"
 */        
        Variables().startCapture();
        Variables().tempVariables.stempMod = "";
        String sNewSuffix =  ConstRegEx.REST_ANY+ sRegExSuffix;
        boolean bRes = capture_DUR_MOD_WS_op(sParagraph, 
                sRegExPrefix, sNewSuffix, iPrevGroupNumber, bMayLeave);
        
        if (bRes) {
            TempSaveCurrentMatch tmpMatch = new TempSaveCurrentMatch(Variables());
            sParagraph = tmpMatch.saveMatchAndMoveOverAndReset(sParagraph);
            String sNewPrefix = ConstRegEx.newPrefix(iPrevGroupNumber);
            
            TempResultVariables tempRes = new TempResultVariables(Variables());
            tempRes.setInitialValues(iPrevGroupNumber);
            
            
            @SuppressWarnings("unused")
            boolean bRecTime = false;
            @SuppressWarnings("unused")
            boolean bPartsTime = false;
            if (capture_RECURSIVE_TIME(sParagraph,
                    sNewPrefix, sRegExSuffix, iPrevGroupNumber, bMayLeave)) {
                Variables().tempVariables.crtTimePoint.set("val",
                        Variables().tempVariables.stempRecursiveTime);
                bRecTime = tempRes.setBestResult(iPrevGroupNumber[0]);
            }

            if (!tempRes.bAtLeastOneResult) {
                tempRes.resetInitialValues(iPrevGroupNumber);
                if (capture_PARTS_TIME_UNIT(sParagraph,
                        sNewPrefix, sRegExSuffix, iPrevGroupNumber, bMayLeave)) {
                    bPartsTime = tempRes.setBestResult(iPrevGroupNumber[0]);
                }
            }
            tempRes.prepareReturn(iPrevGroupNumber);
            if (tempRes.bAtLeastOneResult) {
                try {
                    sParagraph = tmpMatch.saveMatchAndMoveOverAndReset(sParagraph);
                    sNewPrefix = ConstRegEx.newPrefix(iPrevGroupNumber);
                    
                    bRes = capture_DURATION_TIME_Suffix1(sParagraph, 
                            sNewPrefix, sRegExSuffix, iPrevGroupNumber, bMayLeave);
                    
                    Variables().tempVariables.crtTimePoint.set("mod", Variables().tempVariables.stempMod);
                    tmpMatch.restoreCurrentMatch();
                    
                    return Variables().prepareReturn(bRes);
                    //iPrevGroupNumber[0] += iSuffixGroupNumber[0];
                } catch (Exception e) {
                    if (!bMayLeave) {
                        Const.writeSpecificError(e, sParagraph);
                    }
                }
            }
            return Variables().prepareReturn(tempRes.bAtLeastOneResult);
        }
        return Variables().prepareReturn(bRes);
    }    
//#============== RANGE ============     
    public boolean capture_RANGE_PRE_AMPM_TIME_AMPM(String sParagraph,
            String sRegExPrefix, String sRegExSuffix, int[] iPrevGroupNumber,
            boolean bMayLeave) {
/*
 * RANGE_PRE_AMPM_TIME_AMPM
 * 
 */        
        Variables().startCapture();
        String sNewSuf = ConstRegEx.REST_ANY +sRegExSuffix;
        boolean bRes = capture_PRE_AMPM(sParagraph, 
                sRegExPrefix, sNewSuf, iPrevGroupNumber, bMayLeave);
        if (bRes){
            TempSaveCurrentMatch tmpMatch = new TempSaveCurrentMatch(Variables());
            sParagraph = tmpMatch.saveMatchAndMoveOverAndReset(sParagraph);
            String sNewPref = ConstRegEx.newPrefix(iPrevGroupNumber);
            
            String sTime = tmpMatch.sMatch;
                    
            bRes = pAnnotator.comPrefSuf.capture_WS_RANGE_WS_or_Comma_WS(sParagraph, sNewPref, sNewSuf, 
                    iPrevGroupNumber, bMayLeave);
            if (bRes){
                //2008.11.30
                TimePoint tpRange = new TimePoint();
                tpRange.copy(
                        Variables().tempVariables.crtTimePoint);
                tpRange.set("range_Text",
                        Variables().tempVariables.sCurrentMatch);
                tpRange.set("text",
                        sTime);                                
                tpRange.set(
                        "hour",Variables().tempVariables.stempHour);
                tpRange.set(
                        "minute",Variables().tempVariables.stempMin);                
                tpRange.set(
                        "second",Variables().tempVariables.stempSec);                                
                tpRange.set(
                        "miliSec", Variables().tempVariables.stempMiliSec);
                Variables().tempVariables.rangeTimePoint.add(tpRange);
                
                sParagraph = tmpMatch.saveMatchAndMoveOverAndReset(sParagraph);
                sNewPref = ConstRegEx.newPrefix(iPrevGroupNumber);

                
                bRes = capture_TIME_AMPM(sParagraph,
                        sNewPref, sRegExSuffix, iPrevGroupNumber, bMayLeave);
                if (bRes) {
                    try {
                        Variables().tempVariables.crtTimePoint.set("text",
                                Variables().tempVariables.sCurrentMatch);
                        Variables().tempVariables.crtTimePoint.set(
                                "hour", Variables().tempVariables.stempHour);
                        Variables().tempVariables.crtTimePoint.set(
                                "minute", Variables().tempVariables.stempMin);
                        Variables().tempVariables.crtTimePoint.set(
                                "second", Variables().tempVariables.stempSec);
                        Variables().tempVariables.crtTimePoint.set(
                                "miliSec", Variables().tempVariables.stempMiliSec);
                        
                        //rangeTimePoint
                        int iTempHour = Integer.parseInt(
                                tpRange.get("hour"));
                        if (Variables().tempVariables.stempAP.compareTo("p") == 0) {
                            if (iTempHour != 12) {
                                iTempHour += 12;
                            } else {
                                iTempHour = 12;
                            }
                        } else {
                            if (iTempHour == 12) {
                                iTempHour = 24;
                            }
                        }
                        tpRange.set("hour", 
                                Integer.toString(iTempHour));
                        int iLastRangeTp = Variables().tempVariables.rangeTimePoint.size()
                                -1;
                        Variables().tempVariables.rangeTimePoint.set(iLastRangeTp, tpRange);
                        tmpMatch.restoreCurrentMatch();
                    } catch (Exception e) {
                        if (!bMayLeave) {
                            Const.writeSpecificError(e, sParagraph);
                        }
                    }
                }
            }
        }
        return Variables().prepareReturn(bRes);
    }    
    
    public boolean capture_TIME_ALL(String sParagraph, String sRegExPrefix, String sRegExSuffix, int[] iPrevGroupNumber, boolean bMayLeave) { 
        Variables().startCapture();
        TempResultVariables tempRes = new TempResultVariables(Variables());
        Variables().tempVariables.crtTimePoint.clear();
        Variables().tempVariables.rangeTimePoint.clear();
        tempRes.setInitialValues(iPrevGroupNumber);
        if (capture_TIME_IN_WORDS_TO(sParagraph, sRegExPrefix, sRegExSuffix, iPrevGroupNumber, bMayLeave)){
            this.pAnnotator.writeToLogFile("TIME_IN_WORDS_TO\n");
            tempRes.setBestResult( iPrevGroupNumber[0]);       
            return Variables().prepareReturn(true);
        }
        tempRes.resetInitialValues(iPrevGroupNumber);        
        if (capture_RANGE_PRE_AMPM_TIME_AMPM(sParagraph, sRegExPrefix, sRegExSuffix, iPrevGroupNumber, bMayLeave)){
            this.pAnnotator.writeToLogFile("RANGE_PRE_AMPM_TIME_AMPM\n");
            tempRes.setBestResult( iPrevGroupNumber[0]);       
            return Variables().prepareReturn(true);
        }
        tempRes.resetInitialValues(iPrevGroupNumber);
        if (capture_TIME_IN_WORDS_PAST(sParagraph, sRegExPrefix, sRegExSuffix, iPrevGroupNumber, bMayLeave)){
            this.pAnnotator.writeToLogFile("TIME_IN_WORDS_PAST\n");
            tempRes.setBestResult( iPrevGroupNumber[0]);       
            return Variables().prepareReturn(true);
        }
        tempRes.resetInitialValues(iPrevGroupNumber);
        if (capture_TIME_DAY_DAY_PART(sParagraph, sRegExPrefix, sRegExSuffix, iPrevGroupNumber, bMayLeave)) {
            this.pAnnotator.writeToLogFile("TIME_DAY_DAY_PART\n");
            tempRes.setBestResult(iPrevGroupNumber[0]);
            return Variables().prepareReturn(true);
        }
        tempRes.resetInitialValues(iPrevGroupNumber);
        if (capture_SIMPLE_TIMES(sParagraph, sRegExPrefix, sRegExSuffix, iPrevGroupNumber, bMayLeave)) {
            this.pAnnotator.writeToLogFile("SIMPLE_TIMES\n");
            tempRes.setBestResult(iPrevGroupNumber[0]);
            return Variables().prepareReturn(true);
        }
        tempRes.resetInitialValues(iPrevGroupNumber);
        if (capture_DURATION_TIME(sParagraph, sRegExPrefix, sRegExSuffix, iPrevGroupNumber, bMayLeave)) {
            this.pAnnotator.writeToLogFile("DURATION_TIME\n");
            tempRes.setBestResult(iPrevGroupNumber[0]);
            return Variables().prepareReturn(true);
        }
        tempRes.prepareReturn(iPrevGroupNumber);
        return Variables().prepareReturn(tempRes.bAtLeastOneResult);
    }  
}
