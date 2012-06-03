package temporal.rules;

import java.util.regex.*;

public class Date_TE {

    private TIMEXRuleAnnotator pAnnotator;
    
    public Date_TE(TIMEXRuleAnnotator pAnnot){
        pAnnotator = pAnnot;
    }
    
    public GlobalVariables Variables(){
        return pAnnotator.globalVariables;
    }
    
    public FindAll Find(){
        return pAnnotator.findAll;
    }

    public boolean capture_DAY_NAME_FULL_LEX(String sParagraph, String sRegExPrefix, String sRegExSuffix, int[] iPrevGroupNumber,boolean bMayLeave) {
        Variables().startCapture();
        String sPattern = ConstRegEx.groupBrackets(
                Variables().hashtbLexValues.get("DOW").toString(),
                iPrevGroupNumber
                );
        Pattern patternLex = Pattern.compile(
                ConstRegEx.prefixWordSuffix(sPattern, sRegExPrefix, sRegExSuffix), 
                Pattern.CASE_INSENSITIVE);
        boolean bResult = Find().printAllMatchResult(patternLex, sParagraph, false);
        if (bResult) try {
                Variables().saveMatchValue_FromMapHash("dayOfWeek", iPrevGroupNumber[0]);
                Variables().saveCurrentMatch(iPrevGroupNumber[0]);
        } catch(Exception e) {
                if (!bMayLeave){ Const.writeSpecificError(e, sParagraph);}
                Variables().tempVariables.sCurrentMatch = "";
        }            
        return Variables().prepareReturn(bResult);
    }

    public boolean capture_DAY_NAME_ABBREV_LEX(String sParagraph, String sRegExPrefix, String sRegExSuffix, int[] iPrevGroupNumber,boolean bMayLeave) {
        Variables().startCapture();
        int iRes = iPrevGroupNumber[0] + 1;
        String sPattern = ConstRegEx.groupBrackets(
                ConstRegEx.groupBrackets( 
                Variables().hashtbLexValues.get("DOWA").toString(),
                iPrevGroupNumber
                )+"\\.?"
                ,iPrevGroupNumber);
        Pattern patternLex = Pattern.compile(
                ConstRegEx.prefixWordSuffix(sPattern, sRegExPrefix, sRegExSuffix), 
                Pattern.CASE_INSENSITIVE);
        boolean bResult = Find().printAllMatchResult(patternLex, sParagraph, false);
        if (bResult) try {
                Variables().saveMatchValue_FromMapHash("dayOfWeek", iPrevGroupNumber[0]);
                Variables().saveCurrentMatch(iRes);
        } catch(Exception e) {
                if (!bMayLeave){ Const.writeSpecificError(e, sParagraph);}
                Variables().tempVariables.sCurrentMatch = "";
        }
        return Variables().prepareReturn(bResult);        
    }

    public boolean capture_DAY_NAME(String sParagraph, String sRegExPrefix, String sRegExSuffix, int[] iPrevGroupNumber,boolean bMayLeave) {
        Variables().startCapture();
        TempResultVariables tempRes = new TempResultVariables(Variables());
        tempRes.setInitialValues(iPrevGroupNumber);
        if (capture_DAY_NAME_FULL_LEX(sParagraph, sRegExPrefix, sRegExSuffix, iPrevGroupNumber, bMayLeave)) {
           tempRes.setBestResult( iPrevGroupNumber[0]);    
           return Variables().prepareReturn(true);
        }
        tempRes.resetInitialValues(iPrevGroupNumber);        
        if (capture_DAY_NAME_ABBREV_LEX(sParagraph, sRegExPrefix, sRegExSuffix, iPrevGroupNumber, bMayLeave)){
           tempRes.setBestResult( iPrevGroupNumber[0]);                   
           return Variables().prepareReturn(true);
        }
        tempRes.prepareReturn( iPrevGroupNumber ); 
        return Variables().prepareReturn(tempRes.bAtLeastOneResult);
    }    
    
    public boolean capture_STANDALONE_MONTHS1_Case1(String sParagraph, String sRegExPrefix, String sRegExSuffix, int[] iPrevGroupNumber, boolean bMayLeave){
        Variables().startCapture();
        String sPattern = ConstRegEx.groupBrackets( 
                Variables().hashtbLexValues.get("MS").toString(),
                iPrevGroupNumber);
        Pattern patternLex = Pattern.compile(
                ConstRegEx.prefixWordSuffix(sPattern, sRegExPrefix, sRegExSuffix),
                Pattern.CASE_INSENSITIVE);
        boolean bResult = Find().printAllMatchResult(patternLex, sParagraph, false);
        if (bResult){
            try {
                Variables().saveMatchValue_FromMapHash("month", iPrevGroupNumber[0]);
                Variables().saveCurrentMatch(iPrevGroupNumber[0]);
            }
            catch(Exception e){
                if (!bMayLeave){ Const.writeSpecificError(e, sParagraph);}
                Variables().tempVariables.sCurrentMatch = "";
            }   

        }
        return Variables().prepareReturn(bResult);        
    }
    
    public boolean capture_STANDALONE_MONTHS1_Case2(String sParagraph, String sRegExPrefix, String sRegExSuffix,
            int[] iPrevGroupNumber,boolean bMayLeave){
    /*
$STANDALONE_MONTHS1		
 = ($MONA)(?{$crtTimePoint->month($map_val{lc($^N)});})\.?/i 
*/
        Variables().startCapture();
        int iRes = iPrevGroupNumber[0] + 1;
        String sPattern =ConstRegEx.groupBrackets(
                ConstRegEx.groupBrackets(
                Variables().hashtbLexValues.get("MONA").toString(),
                iPrevGroupNumber)+"\\.?"
                ,iPrevGroupNumber);
        Pattern patternLex = Pattern.compile(
                ConstRegEx.prefixWordSuffix(sPattern, sRegExPrefix, sRegExSuffix),
                Pattern.CASE_INSENSITIVE);
        boolean bResult = Find().printAllMatchResult(patternLex, sParagraph, false);
        if (bResult){
            try {
                Variables().saveMatchValue_FromMapHash("month", iPrevGroupNumber[0]);
                Variables().saveCurrentMatch(iRes);
            }
            catch(Exception e){
                if (!bMayLeave){ Const.writeSpecificError(e, sParagraph);}
                Variables().tempVariables.sCurrentMatch = "";
            }   

        }
        return Variables().prepareReturn(bResult);        
    } 
    
    public boolean capture_STANDALONE_MONTHS1(String sParagraph, String sRegExPrefix, 
            String sRegExSuffix, int[] iPrevGroupNumber,boolean bMayLeave){
        /*
$STANDALONE_MONTHS1		
 = qr/($MS)(?{$crtTimePoint->month($map_val{lc($^N)});})|
    //($MONA)(?{$crtTimePoint->month($map_val{lc($^N)});})\.?/i 
         if qr/$MS/i;		
#January, February, July, September, October, November, December, plus abbreviations of month names. These are month names which are not ambiguous, so can be recognised on their own regardless of context 
*/
        
        //conditioned: if qr/$MS/i;
        Variables().startCapture();
        String sPattern = Variables().hashtbLexValues.get("MS").toString() ;
                
        Pattern patternLex = Pattern.compile(
                ConstRegEx.conditioned_prefixWordSuffix(sPattern, sRegExPrefix, sRegExSuffix),
                Pattern.CASE_INSENSITIVE);
        if (Find().printAllMatchResult(patternLex, sParagraph, false)){
            TempResultVariables tempRes = new TempResultVariables(Variables());
            tempRes.setInitialValues(iPrevGroupNumber);
            if (capture_STANDALONE_MONTHS1_Case1(sParagraph, sRegExPrefix, sRegExSuffix, iPrevGroupNumber, bMayLeave)){
                tempRes.setBestResult(iPrevGroupNumber[0]);  
                return Variables().prepareReturn(true);
            }
            tempRes.resetInitialValues(iPrevGroupNumber);
            if (capture_STANDALONE_MONTHS1_Case2(sParagraph, sRegExPrefix, sRegExSuffix, iPrevGroupNumber, bMayLeave)){
                tempRes.setBestResult(iPrevGroupNumber[0]);   
                return Variables().prepareReturn(true);
            }
            
            tempRes.prepareReturn( iPrevGroupNumber );
            return Variables().prepareReturn(tempRes.bAtLeastOneResult);
        }else{
            return Variables().prepareReturn(false);
        }
    }    
    
    public boolean capture_AMBIG_MONTHS_LEX(String sParagraph, String sRegExPrefix, String sRegExSuffix,
            int[] iPrevGroupNumber,boolean bMayLeave) {
/*
$AMBIG_MONTHS_LEX		= qr/$MAMB/i;		
				#March, April, May, June, August. Also Jan (no fullstop) -- These are month names which are potentially ambiguous and context must be taken into account 
 * $STANDALONE_MONTHS2		=
         qr/($AMBIG_MONTHS_LEX)(?{$crtTimePoint->month($map_val{lc($^N)});})/i;		
 */
        Variables().startCapture();
        String sPattern = ConstRegEx.groupBrackets(
                Variables().hashtbLexValues.get("MAMB").toString()
                , iPrevGroupNumber);
        Pattern patternLex = Pattern.compile(
                ConstRegEx.prefixWordSuffix(sPattern, sRegExPrefix, sRegExSuffix),
                Pattern.CASE_INSENSITIVE);
        boolean bRes = Find().printAllMatchResult(patternLex, sParagraph, false);
        if (bRes){
            try {
                Variables().saveCurrentMatch(iPrevGroupNumber[0]);
            } catch (Exception e) {
                if (!bMayLeave){ Const.writeSpecificError(e, sParagraph);}
                Variables().tempVariables.sCurrentMatch = "";
            }

        }
        return Variables().prepareReturn(bRes);
    }
    
    public boolean capture_STANDALONE_MONTHS2(String sParagraph, String sRegExPrefix, String sRegExSuffix, int[] iPrevGroupNumber,boolean bMayLeave) {
        /*
$STANDALONE_MONTHS2		=
         qr/($AMBIG_MONTHS_LEX)(?{$crtTimePoint->month($map_val{lc($^N)});})/i;		
#ambiguous months recognised after certain prepositions ("until March") or after determiners ("their April meeting").  
         */
        Variables().startCapture();
        //Remark: "{$crtTimePoint->month($map_val{lc($^N)})" moved to capture_AMBIG_MONTHS_LEX
        if (capture_AMBIG_MONTHS_LEX(sParagraph, sRegExPrefix, sRegExSuffix, iPrevGroupNumber, bMayLeave)){
            try {
                Variables().saveMatchValue_FromMapHash("month", iPrevGroupNumber[0]);
            } catch (Exception e) {
                if (!bMayLeave){ Const.writeSpecificError(e, sParagraph);}
            }

            return Variables().prepareReturn(true);
        }
        return Variables().prepareReturn(false);
    }
    
    //#================= DATE NAMES ====================== 
    public boolean capture_SPECIAL_DATE_LEX_Case1(String sParagraph, String sRegExPrefix, String sRegExSuffix, 
            int[] iPrevGroupNumber,boolean bMayLeave){
/*
 $SPECIAL_DATE_LEX		= qr/(($THE|$AANONE)$WS)?$HOL/;				
#Names of special holidays 
 $SPECIAL_DATE_NAME		= 
 qr/(($SPECIAL_DATE_GUESS|$SPECIAL_DATE_LEX)(?{$crtTimePoint->special(1);}))($WS$HOLIDAY)?/;
				#Special dates from the lexicon or guessed 
 *
 */        
            Variables().startCapture();
            int iResGr = iPrevGroupNumber[0] + 1;
            String sPattern_Case1 =
                    ConstRegEx.groupBrackets(
                        ConstRegEx.groupBrackets(
                            ConstRegEx.groupBrackets(
                                Variables().hashtbLexValues.get("THE").toString() + "|" +
                                Variables().hashtbLexValues.get("AANONE").toString(),
                            iPrevGroupNumber) + 
                            ConstRegEx.groupBrackets(ConstRegEx.WS, iPrevGroupNumber),
                        iPrevGroupNumber) + "?" +
                        ConstRegEx.groupBrackets(
                            Variables().hashtbLexValues.get("HOL").toString()
                        ,iPrevGroupNumber)
                    , iPrevGroupNumber);
            int[] iTempGrNr = new int[1]; iTempGrNr[0] = 0;
            String sNewSuffix = 
                        ConstRegEx.groupBrackets(
                         ConstRegEx.groupBrackets(
                              ConstRegEx.groupBrackets(ConstRegEx.WS, iTempGrNr) + 
                            ConstRegEx.groupBrackets(
                                Variables().hashtbLexValues.get("HOLIDAY").toString(), iTempGrNr)
                                ,iTempGrNr
                            )  + "?" 
                        , iTempGrNr)+
                        sRegExSuffix;

            Pattern patternLex = Pattern.compile(
                    ConstRegEx.prefixWordSuffix(sPattern_Case1, sRegExPrefix, sNewSuffix),
                    Pattern.CASE_INSENSITIVE
                    );
            boolean bResult = Find().printAllMatchResult(patternLex, sParagraph, true);
            if ( bResult ){
                try{
                    Variables().tempVariables.crtTimePoint.set("special", "1");
                    Variables().saveCurrentMatch(iResGr);
                    Variables().concatenateSuffix(iPrevGroupNumber[0] + 1);
                    iPrevGroupNumber[0] += iTempGrNr[0];
                } catch(Exception e){
                    if (!bMayLeave){ Const.writeSpecificError(e, sParagraph);}
                    Variables().tempVariables.sCurrentMatch = "";
                }   

            }
            return Variables().prepareReturn(bResult);
    }
    
    public boolean capture_SPECIAL_DATE_GUESS_Case2(String sParagraph, String sRegExPrefix, String sRegExSuffix, int[] iPrevGroupNumber,boolean bMayLeave){
/*
 $SPECIAL_DATE_GUESS		
 = qr/[A-Z][\.`\'A-z\-]+$WS((([A-Z][\.`\'A-z\-]+)|([`\']s?))$WS)*(Day|DAY|Eve|EVE|Night|NIGHT)/;		
#Guess at special dates that aren't in the lexicon 
 * 
 $SPECIAL_DATE_NAME		= 
 qr/(($SPECIAL_DATE_GUESS|$SPECIAL_DATE_LEX)(?{$crtTimePoint->special(1);}))($WS$HOLIDAY)?/;
				#Special dates from the lexicon or guessed 
 */        
            Variables().startCapture();
            int iRes = iPrevGroupNumber[0]+1;
            String sPattern_Case2 =
                    ConstRegEx.groupBrackets(
                    "[A-Z][\\.`\\'A-z\\-]+" +
                    ConstRegEx.groupBrackets(ConstRegEx.WS, iPrevGroupNumber) +
                    ConstRegEx.groupBrackets(
                    ConstRegEx.groupBrackets(
                    ConstRegEx.groupBrackets("[A-Z][\\.`\\'A-z\\-]+", iPrevGroupNumber) +
                    "|" +
                    ConstRegEx.groupBrackets(
                    "[`\\']s?", iPrevGroupNumber),
                    iPrevGroupNumber) +
                    ConstRegEx.groupBrackets(ConstRegEx.WS, iPrevGroupNumber),
                    iPrevGroupNumber) +
                    "*" +
                    ConstRegEx.groupBrackets(
                    "Day|DAY|Eve|EVE|Night|NIGHT",
                    iPrevGroupNumber)
                    , iPrevGroupNumber);
            //!!int[] iTempGrNr is not necessary
            int[] iTempGR = new int[1]; iTempGR[0] = 0;
            String sNewSuffix = 
                    ConstRegEx.groupBrackets(
                        ConstRegEx.groupBrackets(
                            ConstRegEx.groupBrackets(ConstRegEx.WS, iTempGR) +
                            Variables().hashtbLexValues.get("HOLIDAY").toString(),
                            iTempGR
                        )+"?"
                    ,iTempGR)+
                    sRegExSuffix;
            Pattern patternLex = Pattern.compile(
                    ConstRegEx.prefixWordSuffix(sPattern_Case2, sRegExPrefix, sNewSuffix)
                    );
            boolean bResult= Find().printAllMatchResult(patternLex, sParagraph, true);
            if (bResult){
                try {
                    Variables().tempVariables.crtTimePoint.set("special", "1");
                    Variables().saveCurrentMatch(iRes);
                    Variables().concatenateSuffix(iPrevGroupNumber[0] + 1);
                    iPrevGroupNumber[0] += iTempGR[0];
                } catch(Exception e){
                    if (!bMayLeave){ Const.writeSpecificError(e, sParagraph);}
                    Variables().tempVariables.sCurrentMatch = "";
                }   
            }
            
            return Variables().prepareReturn(bResult);
    }
    
    public boolean capture_SPECIAL_DATE_NAME(String sParagraph, String sRegExPrefix, String sRegExSuffix, int[] iPrevGroupNumber,boolean bMayLeave){
/*$SPECIAL_DATE_NAME		= 
 qr/(($SPECIAL_DATE_GUESS|$SPECIAL_DATE_LEX)(?{$crtTimePoint->special(1);}))($WS$HOLIDAY)?/;
				#Special dates from the lexicon or guessed 
 */     
        Variables().startCapture();
        TempResultVariables tempRes = new TempResultVariables(Variables());
        tempRes.setInitialValues(iPrevGroupNumber);

        if (capture_SPECIAL_DATE_GUESS_Case2(
                sParagraph, sRegExPrefix, sRegExSuffix, iPrevGroupNumber, bMayLeave)){
            tempRes.setBestResult( iPrevGroupNumber[0]);       
            return Variables().prepareReturn(true);
        }
        tempRes.resetInitialValues(iPrevGroupNumber);
        if (capture_SPECIAL_DATE_LEX_Case1(sParagraph, 
                sRegExPrefix, sRegExSuffix, iPrevGroupNumber, bMayLeave)){
            tempRes.setBestResult( iPrevGroupNumber[0]);       
            return Variables().prepareReturn(true);
        }
        
        tempRes.prepareReturn(iPrevGroupNumber);
        return Variables().prepareReturn(tempRes.bAtLeastOneResult);
        //return Variables().prepareReturn(true);
    }
    
    public boolean capture_TODAY_DEICTIC(String sParagraph, String sRegExPrefix, String sRegExSuffix,
            int[] iPrevGroupNumber,boolean bMayLeave){
/*
 $TODAY_DEICTIC			=
 qr/(($DD)(?{$crtTimePoint->function($map_val{lc($^N)});}))/i;
				#today, yesterday, tomorrow 
 */        
        Variables().startCapture();
        String sPattern = ConstRegEx.groupBrackets(
                Variables().hashtbLexValues.get("DD").toString(),
                iPrevGroupNumber
                );
        Pattern patternLex = Pattern.compile(
                ConstRegEx.prefixWordSuffix(sPattern, sRegExPrefix, sRegExSuffix),
                Pattern.CASE_INSENSITIVE);
        boolean bResult = Find().printAllMatchResult(patternLex, sParagraph, false);
        if (bResult){
            try {
                Variables().saveMatchValue_FromMapHash("function", iPrevGroupNumber[0]);
                Variables().saveCurrentMatch(iPrevGroupNumber[0]);
            } catch(Exception e){
                if (!bMayLeave){ Const.writeSpecificError(e, sParagraph);}
                Variables().tempVariables.sCurrentMatch = "";
            }   
        }
        return Variables().prepareReturn(bResult);        
    }    
    
    public boolean capture_WEEKEND(String sParagraph, String sRegExPrefix, String sRegExSuffix, int[] iPrevGroupNumber,boolean bMayLeave) {
/*
 $WEEKEND			= 
 qr/($THE$WS)?(($DN)(?{$crtTimePoint->dayOfWeek($map_val{lc($^N)});}))s?/i;
 */        
        Variables().startCapture();
        int iRes = iPrevGroupNumber[0] + 1;
        String sPattern = 
            ConstRegEx.groupBrackets(                
                ConstRegEx.groupBrackets(
                    Variables().hashtbLexValues.get("THE").toString() +
                    ConstRegEx.groupBrackets(ConstRegEx.WS, iPrevGroupNumber),
                    iPrevGroupNumber) + "?" +
                ConstRegEx.groupBrackets(
                    Variables().hashtbLexValues.get("DN").toString(),
                    iPrevGroupNumber
                )+ "s?"
           ,iPrevGroupNumber );
        Pattern patternLex = Pattern.compile(
                ConstRegEx.prefixWordSuffix(sPattern, sRegExPrefix, sRegExSuffix),
                Pattern.CASE_INSENSITIVE);
        boolean bResult = Find().printAllMatchResult(patternLex, sParagraph, false);
        if (bResult){
            try {
                Variables().saveMatchValue_FromMapHash("dayOfWeek", iPrevGroupNumber[0]);
                Variables().saveCurrentMatch(iRes);
            } catch(Exception e){
                if (!bMayLeave){ Const.writeSpecificError(e, sParagraph);}
                Variables().tempVariables.sCurrentMatch = "";
            }   
        }
        return Variables().prepareReturn(bResult);        
    }    
    
// #=================== SEASONS =======================    
    public boolean capture_STANDALONE_SEASONS(String sParagraph, String sRegExPrefix, String sRegExSuffix, 
            int[] iPrevGroupNumber,boolean bMayLeave) {
/**
 $STANDALONE_SEASONS		= 
 * qr/($THE$WS)?(($SEAS)(?{$crtTimePoint->month($map_val{lc($^N)});}))/;
#non-ambiguous seasons: summer, winter, autumn (not spring, fall) 
*/
            Variables().startCapture();
            int iRes = iPrevGroupNumber[0] + 1;
            String sPattern = 
                 ConstRegEx.groupBrackets(
                    ConstRegEx.groupBrackets(
                    Variables().hashtbLexValues.get("THE").toString() +
                    ConstRegEx.groupBrackets(ConstRegEx.WS, iPrevGroupNumber), 
                    iPrevGroupNumber) + "?" +
                    ConstRegEx.groupBrackets(
                        Variables().hashtbLexValues.get("SEAS").toString(),
                        iPrevGroupNumber 
                    )
                    ,iPrevGroupNumber);
            Pattern patternLex = Pattern.compile(
                    ConstRegEx.prefixWordSuffix(sPattern, sRegExPrefix, sRegExSuffix));
            boolean bResult = Find().printAllMatchResult(patternLex, sParagraph, false);
            if (bResult) {
                try {
                    Variables().saveMatchValue_FromMapHash("month", iPrevGroupNumber[0]);
                    Variables().saveCurrentMatch(iRes);
                } catch(Exception e){
                    if (!bMayLeave){ Const.writeSpecificError(e, sParagraph);}
                    Variables().tempVariables.sCurrentMatch = "";
                }   
            }
            return Variables().prepareReturn(bResult);
    }    
//#=============== YEARS =============================     
    public boolean capture_YEAR2000(String sParagraph, String sRegExPrefix, String sRegExSuffix, int[] iPrevGroupNumber,boolean bMayLeave){
/*
 $YEAR2000			= 
 qr/($THE$WS)?$YEAR$WS(([1-9][0-9][0-9][0-9])(?{$crtTimePoint->year($^N);}))/i;	
				#the year 2000, the year 1947 
 */        
        Variables().startCapture();
            int iRes = iPrevGroupNumber[0]+ 1;
            String sPattern =
            ConstRegEx.groupBrackets(                    
                    ConstRegEx.groupBrackets(
                    Variables().hashtbLexValues.get("THE").toString() +
                    ConstRegEx.groupBrackets(ConstRegEx.WS, iPrevGroupNumber),
                    iPrevGroupNumber) + "?" +
                    Variables().hashtbLexValues.get("YEAR").toString() +
                    ConstRegEx.groupBrackets(ConstRegEx.WS, iPrevGroupNumber) +
                    ConstRegEx.groupBrackets(
                    "[1-9][0-9][0-9][0-9]",
                    iPrevGroupNumber)
                  , iPrevGroupNumber);
            Pattern patternLex = Pattern.compile(
                    ConstRegEx.prefixWordSuffix(sPattern, sRegExPrefix, sRegExSuffix),
                    Pattern.CASE_INSENSITIVE);
            boolean bResult = Find().printAllMatchResult(patternLex, sParagraph, false);
            if (bResult) {
                try {
                    Variables().saveMatchValue_FromMatchResult("year", iPrevGroupNumber[0]);
                    Variables().saveCurrentMatch(iRes);
                } catch(Exception e){
                    if (!bMayLeave){ Const.writeSpecificError(e, sParagraph);}
                    Variables().tempVariables.sCurrentMatch = "";
                }   
            }
            return Variables().prepareReturn(bResult);
    }    
    
    public boolean capture_ADBC_YEAR_AD_BC_Case1(String sParagraph, String sRegExPrefix, String sRegExSuffix, int[] iPrevGroupNumber,boolean bMayLeave){
/*
 * $ADBC				= 
 qr/((A\.D\.?|AD|a\.d\.?|ad)(?{$crtTimePoint->yearType("AD");}))
/;
 */        
/*
 $YEAR_AD_BC			= 
 qr/([0-9]+)$WS?$ADBC(?{$crtTimePoint->year($^N);})/;		
#700 A.D.
 */    
        Variables().startCapture();
        String sPattern_ADBC = ConstRegEx.groupBrackets("A\\.D\\.?|AD|a\\.d\\.?|ad", iPrevGroupNumber);
        Pattern patternLex = Pattern.compile(
                ConstRegEx.prefixWordSuffix(sPattern_ADBC, sRegExPrefix, sRegExSuffix));
        boolean bResult = Find().printAllMatchResult(patternLex, sParagraph, false);
        if (bResult) {
            try {
                Variables().tempVariables.crtTimePoint.set("yearType", "AD");
                Variables().saveCurrentMatch(iPrevGroupNumber[0]);
            }   catch(Exception e){
                if (!bMayLeave){ Const.writeSpecificError(e, sParagraph);}
                Variables().tempVariables.sCurrentMatch = "";
            }   
        }
        return Variables().prepareReturn(bResult);
    }
    
    public boolean capture_ADBC_YEAR_AD_BC_Case2(String sParagraph, String sRegExPrefix, String sRegExSuffix, int[] iPrevGroupNumber,boolean bMayLeave){
/*
 * $ADBC				= 
 ((B\.C\.?|BC|b\.c\.?|bc)(?{$crtTimePoint->yearType("BC");}))/;
/;
 */        
            
/*
 $YEAR_AD_BC			= qr/([0-9]+)$WS?$ADBC(?{$crtTimePoint->year($^N);})/;		
				#700 A.D
 */           
        Variables().startCapture();
        String sPattern_ADBC = ConstRegEx.groupBrackets("B\\.C\\.?|BC|b\\.c\\.?|bc", iPrevGroupNumber);
        Pattern patternLex = Pattern.compile(
                ConstRegEx.prefixWordSuffix(sPattern_ADBC, sRegExPrefix, sRegExSuffix)
                );
        boolean bResult = Find().printAllMatchResult(patternLex, sParagraph, false);
        if (bResult) {
            try {
                Variables().tempVariables.crtTimePoint.set("yearType", "BC");
                Variables().saveCurrentMatch(iPrevGroupNumber[0]);
            } catch(Exception e){
                if (!bMayLeave){ Const.writeSpecificError(e, sParagraph);}
                Variables().tempVariables.sCurrentMatch = "";
            }   
        }
        return Variables().prepareReturn(bResult);

    }
    public boolean capture_ADBC(String sParagraph, String sRegExPrefix, String sRegExSuffix, 
            int[] iPrevGroupNumber,boolean bMayLeave){
/*$ADBC				= 
 qr/((A\.D\.?|AD|a\.d\.?|ad)(?{$crtTimePoint->yearType("AD");}))|
 ((B\.C\.?|BC|b\.c\.?|bc)(?{$crtTimePoint->yearType("BC");}))/;        
 * */
        Variables().startCapture();
        TempResultVariables tempRes = new TempResultVariables(Variables());
        tempRes.setInitialValues(iPrevGroupNumber);
        
        if (capture_ADBC_YEAR_AD_BC_Case1(sParagraph, 
                sRegExPrefix, sRegExSuffix, iPrevGroupNumber, bMayLeave)) {
            tempRes.setBestResult( iPrevGroupNumber[0]);       
            return Variables().prepareReturn(true);
        }
        
        tempRes.resetInitialValues(iPrevGroupNumber);
        if (capture_ADBC_YEAR_AD_BC_Case2(sParagraph, 
                sRegExPrefix, sRegExSuffix, iPrevGroupNumber, bMayLeave)){
            tempRes.setBestResult( iPrevGroupNumber[0]);       
            return Variables().prepareReturn(true);
        }
        
        tempRes.prepareReturn(iPrevGroupNumber);
        return Variables().prepareReturn(tempRes.bAtLeastOneResult);
    }
    
    public boolean capture_YEAR_AD_BC_Pref(String sParagraph,
            String sRegExPrefix, String sRegExSuffix, int[] iPrevGroupNumber,
            boolean bMayLeave) {
/*
$YEAR_AD_BC			= qr/([0-9]+)$WS?$ADBC(?{$crtTimePoint->year($^N);})/;		
 */        
       Variables().startCapture();
       int iPref = iPrevGroupNumber[0] + 1;
       int iYear= iPref + 1;
       String sPattern = 
                ConstRegEx.groupBrackets(
                    ConstRegEx.groupBrackets("[0-9]+", 
                    iPrevGroupNumber) +
                    ConstRegEx.groupBrackets(ConstRegEx.WS, iPrevGroupNumber) + "?"
                , iPrevGroupNumber);

       Pattern patternLex = Pattern.compile(
               ConstRegEx.prefixWordSuffix(sPattern, sRegExPrefix, sRegExSuffix),
               Pattern.CASE_INSENSITIVE);
       boolean bResult = Find().printAllMatchResult(patternLex, sParagraph, false);
       if (bResult) {
           try {
               Variables().saveMatchValue_FromMatchResult("year", iYear);
               Variables().concatenatePrefix(iPref);
           } catch (Exception e) {
               if (!bMayLeave) {
                   Const.writeSpecificError(e, sParagraph);
               }
           }           
       }
       return Variables().prepareReturn(bResult);
    }        
    public boolean capture_YEAR_AD_BC(String sParagraph, String sRegExPrefix, String sRegExSuffix, int[] iPrevGroupNumber,boolean bMayLeave){
/*
 $ADBC				= 
 qr/((A\.D\.?|AD|a\.d\.?|ad)(?{$crtTimePoint->yearType("AD");}))|
 ((B\.C\.?|BC|b\.c\.?|bc)(?{$crtTimePoint->yearType("BC");}))/;
 * 
 $YEAR_AD_BC			= qr/([0-9]+)$WS?$ADBC(?{$crtTimePoint->year($^N);})/;		
				#700 A.D. 
 */        
        Variables().startCapture();
        
        TempSaveCurrentMatch tmpMatch = new TempSaveCurrentMatch(Variables());
        String sNewSuf = ConstRegEx.REST_ANY + sRegExSuffix;
        
        boolean bRes = capture_YEAR_AD_BC_Pref(sParagraph, 
                sRegExPrefix, sNewSuf, iPrevGroupNumber, bMayLeave);
        if (bRes){
            sParagraph = tmpMatch.saveMatchAndMoveOverAndReset(sParagraph);
            String sNewPrefix = ConstRegEx.newPrefix(iPrevGroupNumber);
            
            bRes = capture_ADBC(sParagraph, sNewPrefix, 
                    sRegExSuffix, iPrevGroupNumber, bMayLeave);
            if (bRes){
                tmpMatch.restoreCurrentMatch();
            }
        }
        return Variables().prepareReturn(bRes);
    }
    
    public boolean capture_COPYRIGHT_YEAR(String sParagraph, String sRegExPrefix, String sRegExSuffix, int[] iPrevGroupNumber,boolean bMayLeave){
/*
 $COPYRIGHT_YEAR			= 
 qr/(?<=c\.)$WS?(19[0-9][0-9](?{$crtTimePoint->year($^N); $crtTimePoint->yearType("c.");}))/; 		
#c.1996 	
 */     
        Variables().startCapture();
        int iRes = iPrevGroupNumber[0] + 1;
        String sPattern = ConstRegEx.groupBrackets(
                ConstRegEx.groupBrackets(
                    "?<=c\\.", 
                    iPrevGroupNumber)+
                    ConstRegEx.groupBrackets(ConstRegEx.WS, iPrevGroupNumber) + "?" +
                    ConstRegEx.groupBrackets(
                        "19[0-9][0-9]",
                        iPrevGroupNumber
                    )
                , iPrevGroupNumber);
        Pattern patternLex = Pattern.compile(
                ConstRegEx.prefixWordSuffix(sPattern, sRegExPrefix, sRegExSuffix)
                );
        boolean bResult = Find().printAllMatchResult(patternLex, sParagraph, false);
        if (bResult){
            try {
                Variables().saveMatchValue_FromMatchResult("year", iPrevGroupNumber[0]);
                Variables().tempVariables.crtTimePoint.set("yearType", "c.");
                Variables().saveCurrentMatch(iRes);
            } catch(Exception e){
                if (!bMayLeave){ Const.writeSpecificError(e, sParagraph);}
                Variables().tempVariables.sCurrentMatch = "";
            }   
        }
        return Variables().prepareReturn(bResult);
    }
    
    public boolean capture_APO_YEAR(String sParagraph, String sRegExPrefix, String sRegExSuffix, int[] iPrevGroupNumber,boolean bMayLeave){
/*
 $APO_YEAR			= qr/[\'`]([0-9][0-9])(?{$tempY = "XX$^N";})/;				
				#year abbreviation: '68 
 */        
        Variables().startCapture();
        int iRes = iPrevGroupNumber[0] + 1;
        String sPattern = 
                ConstRegEx.groupBrackets(
                    "[\\'`]"+
                    ConstRegEx.groupBrackets("[0-9][0-9]", iPrevGroupNumber)
                , iPrevGroupNumber);
        Pattern patternLex = Pattern.compile(
                ConstRegEx.prefixWordSuffix(sPattern, sRegExPrefix, sRegExSuffix)
                );
        boolean bResult = Find().printAllMatchResult(patternLex, sParagraph, true);        
        if (bResult){
            try {
                Variables().tempVariables.stempY = "XX" + 
                        Variables().tempVariables.matchResult.group(iPrevGroupNumber[0]);
                Variables().saveCurrentMatch(iRes);
            } catch(Exception e){
                if (!bMayLeave){ Const.writeSpecificError(e, sParagraph);}
                Variables().tempVariables.sCurrentMatch = "";
            }   
        }
        return Variables().prepareReturn(bResult);
    }  
    public boolean capture_YEAR_LONG_Case1(String sParagraph, String sRegExPrefix, String sRegExSuffix, 
            int[] iPrevGroupNumber,boolean bMayLeave){
/*
$YEAR_LONG			= 
//qr/((1[8-9][0-9][0-9])(?{$tempY = $^N;}))
#Four digits that could be a year. Range is from 1000-2999. For use in a larger date 
 */        
       Variables().startCapture();
       String sPattern = ConstRegEx.groupBrackets(
                "1[8-9][0-9][0-9]", 
                iPrevGroupNumber);
        Pattern patternLex = Pattern.compile(
                ConstRegEx.prefixWordSuffix(sPattern, sRegExPrefix, sRegExSuffix)
                );
        boolean bResult = Find().printAllMatchResult(patternLex, sParagraph, false);
        if (bResult){
            try{
            Variables().tempVariables.stempY = 
                    Variables().tempVariables.matchResult.group(iPrevGroupNumber[0]);
            Variables().saveCurrentMatch(iPrevGroupNumber[0]);
            } catch(Exception e){
                if (!bMayLeave){ Const.writeSpecificError(e, sParagraph);}
                Variables().tempVariables.sCurrentMatch = "";
            }   
        }
        return Variables().prepareReturn(bResult);
    }
    public boolean capture_YEAR_LONG_Case2(String sParagraph, String sRegExPrefix, String sRegExSuffix, 
            int[] iPrevGroupNumber,boolean bMayLeave){
/*
$YEAR_LONG			= 
//qr/((20[0-9][0-9])(?{$tempY = $^N;}))
#Four digits that could be a year. Range is from 1000-2999. For use in a larger date 
 */        
       Variables().startCapture();
       String sPattern = ConstRegEx.groupBrackets(
                "20[0-9][0-9]", 
                iPrevGroupNumber);
        Pattern patternLex = Pattern.compile(
                ConstRegEx.prefixWordSuffix(sPattern, sRegExPrefix, sRegExSuffix)
                );
        boolean bResult = Find().printAllMatchResult(patternLex, sParagraph, false);
        if (bResult){
            try{
//            System.out.println(ConstRegEx.prefixWordSuffix(sPattern, sRegExPrefix, sRegExSuffix));
                Variables().tempVariables.stempY =
                        Variables().tempVariables.matchResult.group(iPrevGroupNumber[0]);
                Variables().saveCurrentMatch(iPrevGroupNumber[0]);
            } catch(Exception e){
                if (!bMayLeave){ Const.writeSpecificError(e, sParagraph);}
                Variables().tempVariables.sCurrentMatch = "";
            }   
        }
        return Variables().prepareReturn(bResult);
    }
    
    public boolean capture_YEAR_LONG(String sParagraph, String sRegExPrefix, String sRegExSuffix, 
            int[] iPrevGroupNumber,boolean bMayLeave){
/*
$YEAR_LONG			= 
//qr/((1[8-9][0-9][0-9])(?{$tempY = $^N;}))|((20[0-9][0-9])(?{$tempY = $^N;}))|
 (($YEAR_ORD)(?{$tempY = $ordWords->val();}))/;  			
#Four digits that could be a year. Range is from 1000-2999. For use in a larger date 
 */        
        Variables().startCapture();
        TempResultVariables tempRes = new TempResultVariables(Variables());
        tempRes.setInitialValues(iPrevGroupNumber);
        if (capture_YEAR_LONG_Case1(sParagraph, sRegExPrefix, sRegExSuffix, iPrevGroupNumber, bMayLeave)){
            tempRes.setBestResult( iPrevGroupNumber[0]);
            return Variables().prepareReturn(true);
        }
        
        tempRes.resetInitialValues(iPrevGroupNumber);
        if (capture_YEAR_LONG_Case2(sParagraph, sRegExPrefix, sRegExSuffix, iPrevGroupNumber, bMayLeave)){
            tempRes.setBestResult( iPrevGroupNumber[0]);
            return Variables().prepareReturn(true);
        }
        
        tempRes.resetInitialValues(iPrevGroupNumber);
        if (pAnnotator.capture_YEAR_ORD(sRegExSuffix, sRegExPrefix, sRegExSuffix, iPrevGroupNumber, bMayLeave)){
            Variables().tempVariables.stempY = Variables().tempVariables.ordWords.get_val().toString();
            tempRes.setBestResult( iPrevGroupNumber[0]);
            return Variables().prepareReturn(true);
        }
        tempRes.prepareReturn( iPrevGroupNumber ); 
        return Variables().prepareReturn(tempRes.bAtLeastOneResult);
    }
    
    public boolean capure_YEAR_LONG_STAND_ALONE1(String sParagraph, String sRegExPrefix, String sRegExSuffix, 
            int[] iPrevGroupNumber,boolean bMayLeave){
/*
$YEAR_LONG_STAND_ALONE1		= qr/
 ($SOME_PREPS|
 $SOME_CONJS|
 $FINANCIAL|
 $PUNCT)$WS$YEAR_LONG/i;  	#([a-z][a-z]+)|$SOME_DETS
#Four digits that could be a year. Range is from 1000-2999. The rule constrains the preceding word to be a preposition, determiner or conjunction in either lower or upper case or the  string FY, or failing that, it must be lower case alphabetic. This will rule out egs like "Corporate Communications Dept., CIMS 1820, PO Box" but it will get egs like "the fatal 1994 crash" 

 */            
        Variables().startCapture();
        String sNewSuffix = ConstRegEx.REST_ANY + sRegExSuffix;

        TempResultVariables tempRes = new TempResultVariables(Variables());
        tempRes.setInitialValues(iPrevGroupNumber);
        
        if (this.pAnnotator.capture_SOME_PREPS(sParagraph, 
                sRegExPrefix, sNewSuffix, iPrevGroupNumber, bMayLeave)){
              tempRes.setBestResult( iPrevGroupNumber[0]);       
        }
        if (!tempRes.bAtLeastOneResult) {
            tempRes.resetInitialValues(iPrevGroupNumber);
            if (this.pAnnotator.capture_SOME_CONJS(sParagraph,
                    sRegExPrefix, sNewSuffix, iPrevGroupNumber, bMayLeave)) {
                tempRes.setBestResult(iPrevGroupNumber[0]);
            }
        }
        if (!tempRes.bAtLeastOneResult) {
            tempRes.resetInitialValues(iPrevGroupNumber);
            if (this.pAnnotator.capture_FINANCIAL(sParagraph,
                    sRegExPrefix, sNewSuffix, iPrevGroupNumber, bMayLeave)) {
                tempRes.setBestResult(iPrevGroupNumber[0]);
            }
        }
        
        if (!tempRes.bAtLeastOneResult) {
            tempRes.resetInitialValues(iPrevGroupNumber);
            if (this.pAnnotator.capture_PUNCT(sParagraph,
                    sRegExPrefix, sNewSuffix, iPrevGroupNumber, bMayLeave)) {
                tempRes.setBestResult(iPrevGroupNumber[0]);
            }
        }
        
        tempRes.prepareReturn(iPrevGroupNumber);
        if (tempRes.bAtLeastOneResult) {
             try {
                TempSaveCurrentMatch tmpMatch = new TempSaveCurrentMatch(Variables());
                sParagraph = tmpMatch.saveMatchAndMoveOverAndReset(sParagraph);
                String sNewPreffix = ConstRegEx.newPrefix(iPrevGroupNumber);
//$WS$YEAR_LONG/i;  	
                boolean bRes = pAnnotator.comPrefSuf.capture_WS(sParagraph, 
                        sNewPreffix, sNewSuffix, iPrevGroupNumber, bMayLeave);
                 if (bRes) {
                     sParagraph = tmpMatch.saveMatchAndMoveOverAndReset(sParagraph);
                     sNewPreffix = ConstRegEx.newPrefix(iPrevGroupNumber);
                     
                     bRes = capture_YEAR_LONG(sParagraph, 
                             sNewPreffix, sRegExSuffix, iPrevGroupNumber, bMayLeave);
                     if (bRes) {
                         try {
                             tmpMatch.restoreCurrentMatch();
                         } catch (Exception e) {
                             if (!bMayLeave) {Const.writeSpecificError(e, sParagraph);}
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
    public boolean capture_YEAR_LONG_STAND_ALONE2(String sParagraph, String sRegExPrefix, String sRegExSuffix, 
            int[] iPrevGroupNumber,boolean bMayLeave){
/*
$YEAR_LONG_STAND_ALONE2		= qr/
 ([1][9][0-9][0-9]|200[0-9])(?{$tempY = $^N;})/ if qr/(?=[A-Z][a-z]+$WS)?(19[0-9]|200)[0-9]/;	
				#same as above but attempt to get just some of the cases where the preceding word starts with a capital. Also restricts the range to just 1900's.
 */         
        Variables().startCapture();
        String sCondition = "(?=[A-Z][a-z]+"+
                "("+ConstRegEx.WS+")"+
                ")?(19[0-9]|200)[0-9]";

        Pattern patternLex = Pattern.compile(
                ConstRegEx.conditioned_prefixWordSuffix(sCondition, sRegExPrefix, sRegExSuffix));
        if (Find().printAllMatchResult(patternLex, sParagraph, false)){
                String sPattern = ConstRegEx.groupBrackets("[1][9][0-9][0-9]|200[0-9]",
                        iPrevGroupNumber);
                Pattern patternLex2 = Pattern.compile(
                        ConstRegEx.prefixWordSuffix(sPattern, sRegExPrefix, sRegExSuffix));
                boolean bResult = Find().printAllMatchResult(patternLex2, sParagraph, false);
                if (bResult) {
                    try {
                        Variables().tempVariables.stempY =
                                Variables().tempVariables.matchResult.group(iPrevGroupNumber[0]);
                        Variables().saveCurrentMatch(iPrevGroupNumber[0]);
                    }catch(Exception e){
                        if (!bMayLeave){ Const.writeSpecificError(e, sParagraph);}
                        Variables().tempVariables.sCurrentMatch = "";
                    } 
                }
                return Variables().prepareReturn(bResult);
        }
        return Variables().prepareReturn(false);
    }
    public boolean capture_YEAR_LONG_STAND_ALONE3(String sParagraph, String sRegExPrefix, String sRegExSuffix, 
            int[] iPrevGroupNumber,boolean bMayLeave){
/*
$YEAR_LONG_STAND_ALONE3		= qr/
 ([1][9][0-9][0-9]|200[0-9])(?{$tempY = $^N;})/ if qr/(?=[A-Z][a-z]+$WS)(19[0-9]|200)[0-9]/;	   
 */      
        Variables().startCapture();
        String sCondition = "(?=[A-Z][a-z]+"+
                "("+ConstRegEx.WS+")"+
                ")(19[0-9]|200)[0-9]";
        Pattern patternLex = Pattern.compile(
                ConstRegEx.conditioned_prefixWordSuffix(sCondition, sRegExPrefix, sRegExSuffix));
        if (Find().printAllMatchResult(patternLex, sParagraph, false)) {
            String sPattern = ConstRegEx.groupBrackets("[1][9][0-9][0-9]|200[0-9]", 
                    iPrevGroupNumber);
            Pattern patternLex2 = Pattern.compile(
                ConstRegEx.prefixWordSuffix(sPattern, sRegExPrefix, sRegExSuffix));
            boolean bResult = Find().printAllMatchResult(patternLex2, sParagraph, false);
            if (bResult){
                try {
                    Variables().tempVariables.stempY = 
                            Variables().tempVariables.matchResult.group(iPrevGroupNumber[0]);
                    Variables().saveCurrentMatch(iPrevGroupNumber[0]);
                } catch(Exception e){
                    if (!bMayLeave){ Const.writeSpecificError(e, sParagraph);}
                    Variables().tempVariables.sCurrentMatch = "";
                }   
            }        
            return Variables().prepareReturn(bResult);
        }
        return Variables().prepareReturn(false);
    }
    public boolean capure_YEAR_STAND_ALONE(String sParagraph, String sRegExPrefix, String sRegExSuffix, 
            int[] iPrevGroupNumber,boolean bMayLeave){
/*
$YEAR_STAND_ALONE		= qr/
 ($YEAR_LONG_STAND_ALONE1|
 $YEAR_LONG_STAND_ALONE2|
 $YEAR_LONG_STAND_ALONE3|
 $APO_YEAR)(?{$crtTimePoint->year($tempY);})/;		
 */     
       Variables().startCapture();
       TempResultVariables tempRes = new TempResultVariables(Variables());
       tempRes.setInitialValues(iPrevGroupNumber);

       if (capure_YEAR_LONG_STAND_ALONE1(sParagraph, 
               sRegExPrefix, sRegExSuffix, iPrevGroupNumber, bMayLeave)){
           tempRes.setBestResult( iPrevGroupNumber[0]);       
       }
       
        if (!tempRes.bAtLeastOneResult) {
            tempRes.resetInitialValues(iPrevGroupNumber);
            if (capture_YEAR_LONG_STAND_ALONE2(sParagraph,
                    sRegExPrefix, sRegExSuffix, iPrevGroupNumber, bMayLeave)) {
                tempRes.setBestResult(iPrevGroupNumber[0]);
            }
        }
        if (!tempRes.bAtLeastOneResult) {
            tempRes.resetInitialValues(iPrevGroupNumber);
            if (capture_YEAR_LONG_STAND_ALONE3(sParagraph,
                    sRegExPrefix, sRegExSuffix, iPrevGroupNumber, bMayLeave)) {
                tempRes.setBestResult(iPrevGroupNumber[0]);
            }
        }
        if (!tempRes.bAtLeastOneResult) {
            tempRes.resetInitialValues(iPrevGroupNumber);
            if (capture_APO_YEAR(sParagraph, sRegExPrefix, sRegExSuffix, iPrevGroupNumber, bMayLeave)) {
                tempRes.setBestResult(iPrevGroupNumber[0]);
            }
        }
       tempRes.prepareReturn(iPrevGroupNumber);
       if (tempRes.bAtLeastOneResult) {
           try {
               Variables().tempVariables.crtTimePoint.set("year", 
                       Variables().tempVariables.stempY);
           } catch(Exception e){
                if (!bMayLeave){ Const.writeSpecificError(e, sParagraph);}
           }   
       }
       return Variables().prepareReturn(tempRes.bAtLeastOneResult);
    }
    public boolean capture_YEAR_IN_CONTEXT(String sParagraph, String sRegExPrefix, String sRegExSuffix, 
            int[] iPrevGroupNumber,boolean bMayLeave){
/*
$YEAR_IN_CONTEXT		= 
 //qr/(($YEAR_LONG|$APO_YEAR)(?{$crtTimePoint->year($tempY);}))/;				
				#year in a larger date: 1968, '68 
 */      
       Variables().startCapture();
       TempResultVariables tempRes = new TempResultVariables(Variables());
       tempRes.setInitialValues(iPrevGroupNumber);

        if (capture_YEAR_LONG(sParagraph, 
                sRegExPrefix, sRegExSuffix, iPrevGroupNumber, bMayLeave)){
            tempRes.setBestResult( iPrevGroupNumber[0]);  
        }
       
        if (!tempRes.bAtLeastOneResult) {
            tempRes.resetInitialValues(iPrevGroupNumber);
            if (capture_APO_YEAR(sParagraph, sRegExPrefix, sRegExSuffix, iPrevGroupNumber, bMayLeave)) {
                tempRes.setBestResult(iPrevGroupNumber[0]);
            }
        }     
      
        tempRes.prepareReturn(iPrevGroupNumber);
        if (tempRes.bAtLeastOneResult) {
            try{
                Variables().tempVariables.crtTimePoint.set("year", Variables().tempVariables.stempY);
            } catch(Exception e){
                if (!bMayLeave){ Const.writeSpecificError(e, sParagraph);}
            }   
        }
        return Variables().prepareReturn(tempRes.bAtLeastOneResult);
    }
    public boolean capture_YEAR_IN_DATE(String sParagraph, String sRegExPrefix, String sRegExSuffix,
            int[] iPrevGroupNumber,boolean bMayLeave){
/*
$YEAR_IN_DATE= qr/$YEAR_IN_CONTEXT|
 (([1-9][0-9])(?{$crtTimePoint->year("XX$^N");}))/;		
				#year as part of a date: 1968, '68, 68
 */       
        Variables().startCapture();
        TempResultVariables tempRes = new TempResultVariables(Variables());
        tempRes.setInitialValues(iPrevGroupNumber);
        
        if (capture_YEAR_IN_CONTEXT(sParagraph, 
                sRegExPrefix, sRegExSuffix, iPrevGroupNumber, bMayLeave)){
                tempRes.setBestResult(iPrevGroupNumber[0]);
                return Variables().prepareReturn(true);
        }
        //ELIMINATED BECAUSE OF capture_RANGE_DATE_DATE_MONTH 2008.12.06
        /*tempRes.resetInitialValues(iPrevGroupNumber);
        //(([1-9][0-9])(?{$crtTimePoint->year("XX$^N");}))/;		
        String sPattern = ConstRegEx.groupBrackets("[1-9][0-9]", iPrevGroupNumber);
        Pattern patternLex = Pattern.compile(
                ConstRegEx.prefixWordSuffix(sPattern, sRegExPrefix, sRegExSuffix));
        boolean bRes = Find().printAllMatchResult(patternLex, sParagraph, false);
        if (bRes){
            try{
                String sRes= Variables().tempVariables.matchResult.group(iPrevGroupNumber[0]);
                Variables().tempVariables.crtTimePoint.set("year", "XX"+sRes);
                
                Variables().saveCurrentMatch(iPrevGroupNumber[0]);
            }catch(Exception e){
              if (!bMayLeave){ Const.writeSpecificError(e, sParagraph);}
              Variables().tempVariables.sCurrentMatch = "";
            }   
            tempRes.setBestResult(iPrevGroupNumber[0]);
            return Variables().prepareReturn(true);
        }*/
        tempRes.prepareReturn(iPrevGroupNumber);
        return Variables().prepareReturn(tempRes.bAtLeastOneResult);
    }
    public boolean capture_FY_YEAR_Case1(String sParagraph, String sRegExPrefix, String sRegExSuffix,
            int[] iPrevGroupNumber,boolean bMayLeave){
/*
$FY_YEAR			= 
//qr/
 ($FINANCIAL($WS$YEAR)?$WS?$YEAR_IN_CONTEXT
/i;    
 */    
        Variables().startCapture();
        String sNewSuf = ConstRegEx.REST_ANY + sRegExSuffix;
        boolean bRes = pAnnotator.comPrefSuf.capture_FINANCIAL_WS_YEAR_op_WS_op(sParagraph, 
                sRegExPrefix, sNewSuf, iPrevGroupNumber, bMayLeave);
        if (bRes){
            TempSaveCurrentMatch tmpMatch = new TempSaveCurrentMatch(Variables());
            sParagraph = tmpMatch.saveMatchAndMoveOverAndReset(sParagraph);
            String sNewPref = ConstRegEx.newPrefix(iPrevGroupNumber);
            
            bRes = capture_YEAR_IN_CONTEXT(sParagraph, sNewPref, 
                    sRegExSuffix, iPrevGroupNumber, bMayLeave);
            if (bRes){
                tmpMatch.restoreCurrentMatch();
            }
        }
        return Variables().prepareReturn(bRes);   
    }    
    
    public boolean capture_FY_YEAR_Case2(String sParagraph, String sRegExPrefix, String sRegExSuffix,
            int[] iPrevGroupNumber,boolean bMayLeave){
/*
$FY_YEAR			= 
//qr/
 ($FINANCIAL($WS$YEAR)?$WS?$YEAR_IN_CONTEXT
/i;    
 */    
        Variables().startCapture();
        String sNewSuf = ConstRegEx.REST_ANY + sRegExSuffix;
        boolean bRes = capture_YEAR_IN_CONTEXT(sParagraph, sRegExPrefix, 
                    sNewSuf, iPrevGroupNumber, bMayLeave);
        if (bRes){
            TempSaveCurrentMatch tmpMatch = new TempSaveCurrentMatch(Variables());
            sParagraph = tmpMatch.saveMatchAndMoveOverAndReset(sParagraph);
            String sNewPref = ConstRegEx.newPrefix(iPrevGroupNumber);
            
            bRes = pAnnotator.comPrefSuf.capture_WS_FINANCIAL_WS_YEAR_op(sParagraph, sNewPref, 
                    sRegExSuffix, iPrevGroupNumber, bMayLeave);
            if (bRes){
                tmpMatch.restoreCurrentMatch();
            }
        }
        return Variables().prepareReturn(bRes);   
    }    
    
    public boolean capture_FY_YEAR(String sParagraph, String sRegExPrefix, String sRegExSuffix,
            int[] iPrevGroupNumber,boolean bMayLeave){
/*
$FY_YEAR			= 
//qr/($THE$WS)?
 ($FINANCIAL($WS$YEAR)?$WS?$YEAR_IN_CONTEXT
 |
 $YEAR_IN_CONTEXT$WS$FINANCIAL($WS$YEAR)?)
 (?{$crtTimePoint->yearType("FY");})/i;    
 */        
        Variables().startCapture();
        String sNewSuf = ConstRegEx.REST_ANY + sRegExSuffix;
        boolean bRes = pAnnotator.comPrefSuf.capture_THE_WS_op(sParagraph, 
                sRegExPrefix, sNewSuf, iPrevGroupNumber, bMayLeave);
        if (bRes){
            TempSaveCurrentMatch tmpMatch = new TempSaveCurrentMatch(Variables());
            sParagraph = tmpMatch.saveMatchAndMoveOverAndReset(sParagraph);
            String sNewPref = ConstRegEx.newPrefix(iPrevGroupNumber);

            TempResultVariables tempRes = new TempResultVariables(Variables());
            tempRes.setInitialValues(iPrevGroupNumber);
            
            if (capture_FY_YEAR_Case1(sParagraph, 
                    sNewPref, sRegExSuffix, iPrevGroupNumber, bMayLeave)){
                tempRes.setBestResult(iPrevGroupNumber[0]);
            }
            
            if (!tempRes.bAtLeastOneResult){
                tempRes.resetInitialValues(iPrevGroupNumber);
                if (capture_FY_YEAR_Case2(sParagraph,
                        sNewPref, sRegExSuffix, iPrevGroupNumber, bMayLeave)){
                    tempRes.setBestResult(iPrevGroupNumber[0]);
                }
            }
            
            tempRes.prepareReturn(iPrevGroupNumber);
            if (tempRes.bAtLeastOneResult) {
                try {
                    Variables().tempVariables.crtTimePoint.set("yearType", "FY");
                    
                    tmpMatch.restoreCurrentMatch();
                } catch (Exception e) {
                    if (!bMayLeave) {Const.writeSpecificError(e, sParagraph);}
                }
            }
            return Variables().prepareReturn(tempRes.bAtLeastOneResult);
        }
        return Variables().prepareReturn(bRes);
    }
    
//#=================== SIMPLE DATES ====================         
    public boolean capture_POSS_DATE(String sParagraph, String sRegExPrefix, String sRegExSuffix,
            int[] iPrevGroupNumber, boolean bMayLeave){
/*
$POSS_DATE			= qr/0?[1-9]|[12][0-9]|3[01]/;
				#a number that could be a date (up to 31) 
 */       
        Variables().startCapture();
        String sPattern = ConstRegEx.groupBrackets("[12][0-9]|3[01]|0?[1-9]", iPrevGroupNumber);
        Pattern patternLex = Pattern.compile(
                ConstRegEx.prefixWordSuffix(sPattern, sRegExPrefix, sRegExSuffix));
        boolean bRes = Find().printAllMatchResult(patternLex, sParagraph, false) ;
        if (bRes){
            Variables().saveCurrentMatch(iPrevGroupNumber[0]);
        }
        return Variables().prepareReturn(bRes);
    }    
    public boolean capture_ORDINAL_DATE_NUM(String sParagraph, String sRegExPrefix, String sRegExSuffix,
            int[] iPrevGroupNumber, boolean bMayLeave) {
/*
$ORDINAL_DATE_NUM= 
 qr/((0?[1-9]|[12][0-9]|3[01])(?{$temp = $^N;}))
 ($DASH|$WS)?(st|rd|nd|th)(?{$ordWords->val($temp);})/i;
#ordinal numbers which could be dates (1st-31st) 
 */        
        Variables().startCapture();
        int iRes = iPrevGroupNumber[0] + 1;
        String sPattern = ConstRegEx.groupBrackets(
                    ConstRegEx.groupBrackets("[12][0-9]|3[01]|0?[1-9]", iPrevGroupNumber)+
                    ConstRegEx.groupBrackets( ConstRegEx.DASH +"|"+ConstRegEx.WS
                    , iPrevGroupNumber)+"?"+
                    ConstRegEx.groupBrackets("st|rd|nd|th", iPrevGroupNumber)
                , iPrevGroupNumber);
        Pattern patternLex = Pattern.compile(
                ConstRegEx.prefixWordSuffix(sPattern, sRegExPrefix, sRegExSuffix),
                Pattern.CASE_INSENSITIVE);
        boolean bResult = Find().printAllMatchResult(patternLex, sParagraph, false);
        if (bResult) {
            try{
                Variables().tempVariables.stemp = 
                        Variables().tempVariables.matchResult.group(iRes + 1);
                Variables().tempVariables.ordWords.set_val(
                        Integer.parseInt(Variables().tempVariables.stemp));
                Variables().saveCurrentMatch(iRes);
            }catch(Exception e){
                if (!bMayLeave){ Const.writeSpecificError(e, sParagraph);}
                Variables().tempVariables.sCurrentMatch = "";
            } 
        }
        return Variables().prepareReturn(bResult);
    }    
    public boolean capture_ORDINAL_DATE(String sParagraph, String sRegExPrefix, String sRegExSuffix,
            int[] iPrevGroupNumber, boolean bMayLeave) {
/*
$ORDINAL_DATE= qr/$ORDINAL_DATE_NUM|$ORD_NAME/;
#ordinals which could be dates. Notice that word ordinals in the second rel aren't constrained to be thirty first or less   
 */       
        Variables().startCapture();
        TempResultVariables tempRes = new TempResultVariables(Variables());
        tempRes.setInitialValues(iPrevGroupNumber);
        
        if (capture_ORDINAL_DATE_NUM(sParagraph, 
                sRegExPrefix, sRegExSuffix, iPrevGroupNumber, bMayLeave)){
            tempRes.setBestResult(iPrevGroupNumber[0]);
            return Variables().prepareReturn(true);
        }
        
        tempRes.resetInitialValues(iPrevGroupNumber);
        if (pAnnotator.capture_ORD_NAME(sParagraph,
                sRegExPrefix, sRegExSuffix, iPrevGroupNumber, bMayLeave)) {
            tempRes.setBestResult(iPrevGroupNumber[0]);
            return Variables().prepareReturn(true);
        }

        tempRes.prepareReturn(iPrevGroupNumber);
        return Variables().prepareReturn(tempRes.bAtLeastOneResult);
    }
    
    public boolean capture_DATE(String sParagraph,String sRegExPrefix, String sRegExSuffix, 
             int[] iPrevGroupNumber,boolean bMayLeave){
/*
$DATE= 
 qr/($ORDINAL_DATE(?{$tempD = $ordWords->val();}))|
 (($POSS_DATE)(?{$tempD = int($^N);}))/;    
 */     
        Variables().startCapture();
        TempResultVariables tempRes = new TempResultVariables(Variables());
        tempRes.setInitialValues(iPrevGroupNumber);

        if (capture_ORDINAL_DATE(sParagraph, 
                sRegExPrefix, sRegExSuffix, iPrevGroupNumber, bMayLeave)){
            try {
                Variables().tempVariables.stempD =
                        Variables().tempVariables.ordWords.get_val().toString();
            } catch (Exception e) {
                if (!bMayLeave) {Const.writeSpecificError(e, sParagraph);}
            }
            tempRes.setBestResult(iPrevGroupNumber[0]);
            return Variables().prepareReturn(true);
        }
        
        tempRes.resetInitialValues(iPrevGroupNumber);
        if (capture_POSS_DATE(sParagraph, 
                sRegExPrefix, sRegExSuffix, iPrevGroupNumber, bMayLeave)){
            try {
                Variables().tempVariables.stempD =
                        Variables().tempVariables.sCurrentMatch;
            } catch (Exception e) {
                if (!bMayLeave) {Const.writeSpecificError(e, sParagraph);}
            }
            tempRes.setBestResult(iPrevGroupNumber[0]);
            return Variables().prepareReturn(true);
        }
        
        tempRes.prepareReturn(iPrevGroupNumber);
        return Variables().prepareReturn(tempRes.bAtLeastOneResult);
    }    
    public boolean capture_DATE_MONTH(String sParagraph,String sRegExPrefix, String sRegExSuffix, 
             int[] iPrevGroupNumber,boolean bMayLeave){
/*
 $DATE_MONTH= 
 qr/(
 (
 ($THE$WS)?$DATE$WS($OF$WS)?$MONTH_NAME_IN_CONTEXT
 )
  (?{$crtTimePoint->dayOfMonth($tempD);$crtTimePoint->month($tempMonth);})
 )/;
#(the) 4(th) (of) Feb(ruary) 
 */        
        Variables().startCapture();
        String sNewSuf = ConstRegEx.REST_ANY +sRegExSuffix;
        boolean bRes = pAnnotator.comPrefSuf.capture_THE_WS_op(sParagraph, 
                sRegExPrefix, sNewSuf, iPrevGroupNumber, bMayLeave);
        if (bRes){
            TempSaveCurrentMatch tmpMatch = new TempSaveCurrentMatch(Variables());
            sParagraph = tmpMatch.saveMatchAndMoveOverAndReset(sParagraph);
            String sNewPref = ConstRegEx.newPrefix(iPrevGroupNumber);
            
            bRes = capture_DATE(sParagraph, sNewPref, sNewSuf, 
                    iPrevGroupNumber, bMayLeave);
            if (bRes){
                sParagraph = tmpMatch.saveMatchAndMoveOverAndReset(sParagraph);
                sNewPref = ConstRegEx.newPrefix(iPrevGroupNumber);
                
                bRes = pAnnotator.comPrefSuf.capture_WS_OF_WS_op(sParagraph, 
                        sNewPref, sNewSuf, iPrevGroupNumber, bMayLeave);
                if (bRes){
                    sParagraph = tmpMatch.saveMatchAndMoveOverAndReset(sParagraph);
                    sNewPref = ConstRegEx.newPrefix(iPrevGroupNumber);
                    
                    bRes = pAnnotator.capture_MONTH_NAME_IN_CONTEXT(sParagraph, 
                       sNewPref, sRegExSuffix, iPrevGroupNumber, bMayLeave);
                    if (bRes) {
                        try {
//(?{$crtTimePoint->dayOfMonth($tempD);$crtTimePoint->month($tempMonth);})
                            Variables().tempVariables.crtTimePoint.set("dayOfMonth",
                                    Variables().tempVariables.stempD);
                            Variables().tempVariables.crtTimePoint.set("month",
                                    Variables().tempVariables.stempMonth);

                            tmpMatch.restoreCurrentMatch();
                        } catch (Exception e) {
                            if (!bMayLeave) {Const.writeSpecificError(e, sParagraph);}
                        }
                    }
                       
                }
            }
                    
        }
        return Variables().prepareReturn(bRes);
    }  
    
    public boolean capture_MONTH_DATE(String sParagraph, String sRegExPrefix, String sRegExSuffix,
            int[] iPrevGroupNumber, boolean bMayLeave) {
/*
$MONTH_DATE=
 qr/
 (
 ($MONTH_NAME_IN_CONTEXT
    ($WS($THE$WS)?|$WS?,?$WS?)
 $DATE)
 (?{$crtTimePoint->dayOfMonth($tempD); $crtTimePoint->month($tempMonth);}))/;
				#Feb(ruary) (the) 4(th), March 3 
 */       
        Variables().startCapture();
        String sNewSuf = ConstRegEx.REST_ANY + sRegExSuffix;
        boolean bRes = pAnnotator.capture_MONTH_NAME_IN_CONTEXT(
                sParagraph, sRegExPrefix, sNewSuf, iPrevGroupNumber, bMayLeave);
        if (bRes){
            try{
                TempSaveCurrentMatch tmpMatch = new TempSaveCurrentMatch(Variables());
                sParagraph = tmpMatch.saveMatchAndMoveOverAndReset(sParagraph);
                String sNewPref = ConstRegEx.newPrefix(iPrevGroupNumber);
                
                bRes = pAnnotator.comPrefSuf.capture_WS_THE_WS_op_WS_op_COMMA_op_WS_op(sParagraph, 
                        sNewPref, sNewSuf, iPrevGroupNumber, bMayLeave);
                if (bRes) {
                    sParagraph = tmpMatch.saveMatchAndMoveOverAndReset(sParagraph);
                    sNewPref = ConstRegEx.newPrefix(iPrevGroupNumber);
                    
                    bRes = capture_DATE(sParagraph,
                            sNewPref, sRegExSuffix, iPrevGroupNumber, bMayLeave);
                    if (bRes) {
                        try {
//(?{$crtTimePoint->dayOfMonth($tempD); $crtTimePoint->month($tempMonth);}))/;
                            Variables().tempVariables.crtTimePoint.set("dayOfMonth",
                                    Variables().tempVariables.stempD);
                            Variables().tempVariables.crtTimePoint.set("month",
                                    Variables().tempVariables.stempMonth);

                            tmpMatch.restoreCurrentMatch();
                        } catch (Exception e) {
                            if (!bMayLeave) {Const.writeSpecificError(e, sParagraph);}
                        }
                    }
                }
                return Variables().prepareReturn(bRes);
            } catch (Exception e) {
               if (!bMayLeave) {Const.writeSpecificError(e, sParagraph);}
           }
        }
        return Variables().prepareReturn(bRes);
    }
             
    public boolean capture_DM(String sParagraph,String sRegExPrefix, String sRegExSuffix, 
             int[] iPrevGroupNumber,boolean bMayLeave){
/*
 $DM= qr/$DATE_MONTH|$MONTH_DATE/;
#date and month in either order 
 */     
        Variables().startCapture();
        TempResultVariables tempRes = new TempResultVariables(Variables());
        tempRes.setInitialValues(iPrevGroupNumber);

        if (capture_DATE_MONTH(sParagraph, sRegExPrefix, sRegExSuffix, iPrevGroupNumber, bMayLeave)){
            tempRes.setBestResult(iPrevGroupNumber[0]);
            return Variables().prepareReturn(true);
        }
        
        tempRes.resetInitialValues(iPrevGroupNumber);
        if (capture_MONTH_DATE(sParagraph, sRegExPrefix, sRegExSuffix, iPrevGroupNumber, bMayLeave)){
            tempRes.setBestResult(iPrevGroupNumber[0]);
            return Variables().prepareReturn(true);
        }
        tempRes.prepareReturn(iPrevGroupNumber);
        return Variables().prepareReturn(tempRes.bAtLeastOneResult);
    }    
    
    public boolean capture_DAY_DM_Case1(String sParagraph,String sRegExPrefix, String sRegExSuffix, 
             int[] iPrevGroupNumber,boolean bMayLeave){
/*
$DAY_DM=
 qr/$DAY_NAME($WS?,?$WS?|$WS)$DM |

 */      
        Variables().startCapture();
        String sNewSuf = ConstRegEx.REST_ANY + sRegExSuffix;
        boolean bRes = capture_DAY_NAME(sParagraph,
                sRegExPrefix, sNewSuf, iPrevGroupNumber, bMayLeave);
        if (bRes){
            try{
                TempSaveCurrentMatch tmpMatch = new TempSaveCurrentMatch(Variables());
                sParagraph = tmpMatch.saveMatchAndMoveOverAndReset(sParagraph);                
                String sNewPref = ConstRegEx.newPrefix(iPrevGroupNumber);
                
                bRes = pAnnotator.comPrefSuf.capture_WS_op_COMMA_op_WS_op_or_WS(sParagraph, 
                        sNewPref, sNewSuf, iPrevGroupNumber, bMayLeave);
                if (bRes) {
                    sParagraph = tmpMatch.saveMatchAndMoveOverAndReset(sParagraph);
                    sNewPref = ConstRegEx.newPrefix(iPrevGroupNumber);

                    bRes = capture_DM(sParagraph,
                            sNewPref, sRegExSuffix, iPrevGroupNumber, bMayLeave);
                    if (bRes) {
                        tmpMatch.restoreCurrentMatch();
                    }
                }
                return Variables().prepareReturn(bRes);
            }catch (Exception e) {
               if (!bMayLeave) {Const.writeSpecificError(e, sParagraph);}
            }
        }
        return Variables().prepareReturn(bRes);
    }    
    
    public boolean capture_DAY_DM_Case2(String sParagraph, String sRegExPrefix, String sRegExSuffix,
            int[] iPrevGroupNumber, boolean bMayLeave) {
/*
 $DAY_DM=
 * $DAY_NAME$WS?\[$WS?$DM$WS?\]
 */      
        Variables().startCapture();
        String sNewSuf = ConstRegEx.REST_ANY + sRegExSuffix;
        boolean bRes = capture_DAY_NAME(sParagraph,
                sRegExPrefix, sNewSuf, iPrevGroupNumber, bMayLeave);
        if (bRes){
            try{
                TempSaveCurrentMatch tmpMatch = new TempSaveCurrentMatch(Variables());
                sParagraph = tmpMatch.saveMatchAndMoveOverAndReset(sParagraph);
                String sNewPref = ConstRegEx.newPrefix(iPrevGroupNumber);
                        
                bRes = pAnnotator.comPrefSuf.capture_WS_op_br_WS_op(sParagraph, 
                        sNewPref, sNewSuf, iPrevGroupNumber, bMayLeave);
                if (bRes) {
                    sParagraph = tmpMatch.saveMatchAndMoveOverAndReset(sParagraph);
                    sNewPref = ConstRegEx.newPrefix(iPrevGroupNumber);
                    
                    bRes = capture_DM(sParagraph,
                            sNewPref, sNewSuf, iPrevGroupNumber, bMayLeave);
                    if (bRes) {
                        sParagraph = tmpMatch.saveMatchAndMoveOverAndReset(sParagraph);
                        sNewPref = ConstRegEx.newPrefix(iPrevGroupNumber);
                        
                        bRes = pAnnotator.comPrefSuf.capture_WS_op_br(sParagraph, 
                                sNewPref, sRegExSuffix, iPrevGroupNumber, bMayLeave);
                        if (bRes) {
                            tmpMatch.restoreCurrentMatch();
                        }
                    }
                }
                return Variables().prepareReturn(bRes);
            }catch (Exception e) {
               if (!bMayLeave) {Const.writeSpecificError(e, sParagraph);}
            }
        }
        return Variables().prepareReturn(bRes);        
    }
    public boolean capture_DAY_DM_Case3(String sParagraph, String sRegExPrefix, String sRegExSuffix,
            int[] iPrevGroupNumber, boolean bMayLeave) {
/*
 $DAY_DM=
 $DAY_NAME$WS?\($WS?$DM$WS?\)
 */     
        Variables().startCapture();
        String sNewSuf = ConstRegEx.REST_ANY + sRegExSuffix;
        boolean bRes = capture_DAY_NAME(sParagraph,
                sRegExPrefix, sNewSuf, iPrevGroupNumber, bMayLeave);
        if (bRes){
            try{
                TempSaveCurrentMatch tmpMatch = new TempSaveCurrentMatch(Variables());
                sParagraph = tmpMatch.saveMatchAndMoveOverAndReset(sParagraph);
                String sNewPref = ConstRegEx.newPrefix(iPrevGroupNumber);
                        
                bRes = pAnnotator.comPrefSuf.capture_WS_op_bracket_WS_op(sParagraph, 
                        sNewPref, sNewSuf, iPrevGroupNumber, bMayLeave);
                if (bRes) {
                    sParagraph = tmpMatch.saveMatchAndMoveOverAndReset(sParagraph);
                    sNewPref = ConstRegEx.newPrefix(iPrevGroupNumber);
                    
                    bRes = capture_DM(sParagraph,
                            sNewPref, sNewSuf, iPrevGroupNumber, bMayLeave);
                    if (bRes) {
                        sParagraph = tmpMatch.saveMatchAndMoveOverAndReset(sParagraph);
                        sNewPref = ConstRegEx.newPrefix(iPrevGroupNumber);
                        
                        bRes = pAnnotator.comPrefSuf.capture_WS_op_bracket(sParagraph, 
                                sNewPref, sRegExSuffix, iPrevGroupNumber, bMayLeave);
                        if (bRes) {
                            tmpMatch.restoreCurrentMatch();
                        }
                    }
                }
                return Variables().prepareReturn(bRes);
            }catch (Exception e) {
               if (!bMayLeave) {Const.writeSpecificError(e, sParagraph);}
            }
        }
        return Variables().prepareReturn(bRes);        
    }
    public boolean capture_DAY_DM_Case4(String sParagraph, String sRegExPrefix, String sRegExSuffix,
            int[] iPrevGroupNumber, boolean bMayLeave) {
/*
$DAY_DM=
 * $DM$WS$WEEKEND
 */       
        Variables().startCapture();
        String sNewSuf = ConstRegEx.REST_ANY+ sRegExSuffix;
        boolean bRes = capture_DM(sParagraph, 
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
                    
                    bRes = capture_WEEKEND(sParagraph,
                            sNewPref, sRegExSuffix, iPrevGroupNumber, bMayLeave);
                    if (bRes) {
                        tmpMatch.restoreCurrentMatch();
                    }
                }
                return Variables().prepareReturn(bRes);
            }catch (Exception e) {
               if (!bMayLeave) {Const.writeSpecificError(e, sParagraph);}
            }
        }
        return Variables().prepareReturn(bRes);
    }    
    public boolean capture_DAY_DM(String sParagraph,String sRegExPrefix, String sRegExSuffix, 
             int[] iPrevGroupNumber,boolean bMayLeave){
/*
$DAY_DM=
 qr/$DAY_NAME($WS?,?$WS?|$WS)$DM |
 $DAY_NAME$WS?\[$WS?$DM$WS?\]|
 $DAY_NAME$WS?\($WS?$DM$WS?\)|
 $DM$WS$WEEKEND/;
				#Monday, May 19th 
*/        
        Variables().startCapture();
        TempResultVariables tempRes = new TempResultVariables(Variables());
        tempRes.setInitialValues(iPrevGroupNumber);
    
        if (capture_DAY_DM_Case1(sParagraph, 
                sRegExPrefix, sRegExSuffix, iPrevGroupNumber, bMayLeave)){
            tempRes.setBestResult(iPrevGroupNumber[0]);
            return Variables().prepareReturn(true);
        }

        tempRes.resetInitialValues(iPrevGroupNumber);
        if (capture_DAY_DM_Case2(sParagraph, 
                sRegExPrefix, sRegExSuffix, iPrevGroupNumber, bMayLeave)){
            tempRes.setBestResult(iPrevGroupNumber[0]);
            return Variables().prepareReturn(true);
        }

        tempRes.resetInitialValues(iPrevGroupNumber);
        if (capture_DAY_DM_Case3(sParagraph, 
                sRegExPrefix, sRegExSuffix, iPrevGroupNumber, bMayLeave)){
            tempRes.setBestResult(iPrevGroupNumber[0]);
            return Variables().prepareReturn(true);
        }
        
        tempRes.resetInitialValues(iPrevGroupNumber);
        if (capture_DAY_DM_Case4(sParagraph, 
                sRegExPrefix, sRegExSuffix, iPrevGroupNumber, bMayLeave)){
            tempRes.setBestResult(iPrevGroupNumber[0]);
            return Variables().prepareReturn(true);
        }

        tempRes.prepareReturn(iPrevGroupNumber);
        return Variables().prepareReturn(tempRes.bAtLeastOneResult);
    }    
    
    
    public boolean capture_DMY(String sParagraph, String sRegExPrefix, String sRegExSuffix,
            int[] iPrevGroupNumber, boolean bMayLeave) {
/*
 $DMY				= qr/$DM($WS?,$WS?|$WS)$YEAR_IN_DATE/;
				#May 19th, 1958
 */       
        Variables().startCapture();
        String sNewSuf = ConstRegEx.REST_ANY + sRegExSuffix;
        boolean bRes = capture_DM(sParagraph,
                sRegExPrefix, sNewSuf, iPrevGroupNumber, bMayLeave);
        if (bRes){
            try{
                TempSaveCurrentMatch tmpMatch = new TempSaveCurrentMatch(Variables());
                sParagraph = tmpMatch.saveMatchAndMoveOverAndReset(sParagraph);                
                String sNewPref = ConstRegEx.newPrefix(iPrevGroupNumber);
                
                bRes = pAnnotator.comPrefSuf.capture_WS_op_COMMA_WS_op_or_WS(sParagraph, 
                        sNewPref, sNewSuf, iPrevGroupNumber, bMayLeave);
                if (bRes) {
                    sParagraph = tmpMatch.saveMatchAndMoveOverAndReset(sParagraph);
                    sNewPref = ConstRegEx.newPrefix(iPrevGroupNumber);

                    bRes = capture_YEAR_IN_DATE(sParagraph,
                            sNewPref, sRegExSuffix, iPrevGroupNumber, bMayLeave);
                    if (bRes) {
                        tmpMatch.restoreCurrentMatch();
                    }
                }
                return Variables().prepareReturn(bRes);
            }catch (Exception e) {
               if (!bMayLeave) {Const.writeSpecificError(e, sParagraph);}
            }
        }
        return Variables().prepareReturn(bRes);
        
    }    
    
  public boolean capture_DAY_DMY_Case1(String sParagraph,String sRegExPrefix, String sRegExSuffix, 
             int[] iPrevGroupNumber,boolean bMayLeave){
/*
$DAY_DMY= qr/$DAY_NAME$WS?[,;]?$WS?$DMY|
 */     
        Variables().startCapture();
        String sNewSuf = ConstRegEx.REST_ANY + sRegExSuffix;
        boolean bRes = capture_DAY_NAME(sParagraph,
                sRegExPrefix, sNewSuf, iPrevGroupNumber, bMayLeave);
        if (bRes){
            try{
                TempSaveCurrentMatch tmpMatch = new TempSaveCurrentMatch(Variables());
                sParagraph = tmpMatch.saveMatchAndMoveOverAndReset(sParagraph);                
                String sNewPref = ConstRegEx.newPrefix(iPrevGroupNumber);
                
                bRes = pAnnotator.comPrefSuf.capture_WS_op_COMMA_SEMICOL_op_WS_op(sParagraph, 
                        sNewPref, sNewSuf, iPrevGroupNumber, bMayLeave);
                if (bRes) {
                    sParagraph = tmpMatch.saveMatchAndMoveOverAndReset(sParagraph);
                    sNewPref = ConstRegEx.newPrefix(iPrevGroupNumber);

                    bRes = capture_DMY(sParagraph,
                            sNewPref, sRegExSuffix, iPrevGroupNumber, bMayLeave);
                    if (bRes) {
                        tmpMatch.restoreCurrentMatch();
                    }
                }
                return Variables().prepareReturn(bRes);
            }catch (Exception e) {
               if (!bMayLeave) {Const.writeSpecificError(e, sParagraph);}
            }
        }
        return Variables().prepareReturn(bRes);
    }    
    
    public boolean capture_DAY_DMY_Case2(String sParagraph, String sRegExPrefix, String sRegExSuffix,
            int[] iPrevGroupNumber, boolean bMayLeave) {
/*
$DAY_DMY= qr/$DAY_NAME$WS?\[$WS?$DMY$WS?\]|
 */        
        Variables().startCapture();
        String sNewSuf = ConstRegEx.REST_ANY + sRegExSuffix;
        boolean bRes = capture_DAY_NAME(sParagraph,
                sRegExPrefix, sNewSuf, iPrevGroupNumber, bMayLeave);
        if (bRes){
            try{
                TempSaveCurrentMatch tmpMatch = new TempSaveCurrentMatch(Variables());
                sParagraph = tmpMatch.saveMatchAndMoveOverAndReset(sParagraph);
                String sNewPref = ConstRegEx.newPrefix(iPrevGroupNumber);
                        
                bRes = pAnnotator.comPrefSuf.capture_WS_op_br_WS_op(sParagraph, 
                        sNewPref, sNewSuf, iPrevGroupNumber, bMayLeave);
                if (bRes) {
                    sParagraph = tmpMatch.saveMatchAndMoveOverAndReset(sParagraph);
                    sNewPref = ConstRegEx.newPrefix(iPrevGroupNumber);
                    
                    bRes = capture_DMY(sParagraph,
                            sNewPref, sNewSuf, iPrevGroupNumber, bMayLeave);
                    if (bRes) {
                        sParagraph = tmpMatch.saveMatchAndMoveOverAndReset(sParagraph);
                        sNewPref = ConstRegEx.newPrefix(iPrevGroupNumber);
                        
                        bRes = pAnnotator.comPrefSuf.capture_WS_op_br(sParagraph, 
                                sNewPref, sRegExSuffix, iPrevGroupNumber, bMayLeave);
                        if (bRes) {
                            tmpMatch.restoreCurrentMatch();
                        }
                    }
                }
                return Variables().prepareReturn(bRes);
            }catch (Exception e) {
               if (!bMayLeave) {Const.writeSpecificError(e, sParagraph);}
            }
        }
        return Variables().prepareReturn(bRes);        
    }
    public boolean capture_DAY_DMY_Case3(String sParagraph, String sRegExPrefix, String sRegExSuffix,
            int[] iPrevGroupNumber, boolean bMayLeave) {
/*
 $DAY_DM=
    $DAY_NAME$WS?\($WS?$DMY$WS?\)|
 */        
        Variables().startCapture();
        String sNewSuf = ConstRegEx.REST_ANY + sRegExSuffix;
        boolean bRes = capture_DAY_NAME(sParagraph,
                sRegExPrefix, sNewSuf, iPrevGroupNumber, bMayLeave);
        if (bRes){
            try{
                TempSaveCurrentMatch tmpMatch = new TempSaveCurrentMatch(Variables());
                sParagraph = tmpMatch.saveMatchAndMoveOverAndReset(sParagraph);
                String sNewPref = ConstRegEx.newPrefix(iPrevGroupNumber);
                        
                bRes = pAnnotator.comPrefSuf.capture_WS_op_bracket_WS_op(sParagraph, 
                        sNewPref, sNewSuf, iPrevGroupNumber, bMayLeave);
                if (bRes) {
                    sParagraph = tmpMatch.saveMatchAndMoveOverAndReset(sParagraph);
                    sNewPref = ConstRegEx.newPrefix(iPrevGroupNumber);
                    
                    bRes = capture_DMY(sParagraph,
                            sNewPref, sNewSuf, iPrevGroupNumber, bMayLeave);
                    if (bRes) {
                        sParagraph = tmpMatch.saveMatchAndMoveOverAndReset(sParagraph);
                        sNewPref = ConstRegEx.newPrefix(iPrevGroupNumber);
                        
                        bRes = pAnnotator.comPrefSuf.capture_WS_op_bracket(sParagraph, 
                                sNewPref, sRegExSuffix, iPrevGroupNumber, bMayLeave);
                        if (bRes) {
                            tmpMatch.restoreCurrentMatch();
                        }
                    }
                }
                return Variables().prepareReturn(bRes);
            }catch (Exception e) {
               if (!bMayLeave) {Const.writeSpecificError(e, sParagraph);}
            }
        }
        return Variables().prepareReturn(bRes);        
    }
    public boolean capture_DAY_DMY_Case4(String sParagraph, String sRegExPrefix, String sRegExSuffix,
            int[] iPrevGroupNumber, boolean bMayLeave) {
/*$DAY_DMY= 
$DMY$WS?[,;\[\(]?$WS?$DAY_NAME($WS?[\]\)]?)/;
 */       
        Variables().startCapture();
        String sNewSuf = ConstRegEx.REST_ANY+ sRegExSuffix;
        boolean bRes = capture_DMY(sParagraph, 
                sRegExPrefix, sNewSuf, iPrevGroupNumber, bMayLeave);
        if (bRes){
            try{
                TempSaveCurrentMatch tmpMatch = new TempSaveCurrentMatch(Variables());
                sParagraph = tmpMatch.saveMatchAndMoveOverAndReset(sParagraph);
                String sNewPref = ConstRegEx.newPrefix(iPrevGroupNumber);
                
                bRes = pAnnotator.comPrefSuf.capture_WS_op_SIGN_op_WS_op(sParagraph, 
                        sNewPref, sNewSuf, iPrevGroupNumber, bMayLeave);
                if (bRes) {
                    sParagraph = tmpMatch.saveMatchAndMoveOverAndReset(sParagraph);
                    sNewPref = ConstRegEx.newPrefix(iPrevGroupNumber);
                    
                    bRes = capture_DAY_NAME(sParagraph,
                            sNewPref, sNewSuf, iPrevGroupNumber, bMayLeave);
                    if (bRes) {
                        sParagraph = tmpMatch.saveMatchAndMoveOverAndReset(sParagraph);
                        sNewPref = ConstRegEx.newPrefix(iPrevGroupNumber);

                        bRes = pAnnotator.comPrefSuf.capture_WS_op_br_op(sParagraph, 
                                sNewPref, sRegExSuffix, iPrevGroupNumber, bMayLeave);
                        if (bRes) {
                            tmpMatch.restoreCurrentMatch();
                        }
                    }
                }
                return Variables().prepareReturn(bRes);
            }catch (Exception e) {
               if (!bMayLeave) {Const.writeSpecificError(e, sParagraph);}
            }
        }
        return Variables().prepareReturn(bRes);
    }        
  public boolean capture_DAY_DMY(String sParagraph,String sRegExPrefix, String sRegExSuffix, 
             int[] iPrevGroupNumber,boolean bMayLeave){
/*
 $DAY_DMY= qr/$DAY_NAME$WS?[,;]?$WS?$DMY|
 $DAY_NAME$WS?\[$WS?$DMY$WS?\]|
 $DAY_NAME$WS?\($WS?$DMY$WS?\)|
 $DMY$WS?[,;\[\(]?$WS?$DAY_NAME($WS?[\]\)]?)/;
#Monday, May 19th, 1958 * 
*/        
        Variables().startCapture();
        TempResultVariables tempRes = new TempResultVariables(Variables());
        tempRes.setInitialValues(iPrevGroupNumber);
    
        if (capture_DAY_DMY_Case1(sParagraph, 
                sRegExPrefix, sRegExSuffix, iPrevGroupNumber, bMayLeave)){
            tempRes.setBestResult(iPrevGroupNumber[0]);
            return Variables().prepareReturn(true);
        }

        tempRes.resetInitialValues(iPrevGroupNumber);
        if (capture_DAY_DMY_Case2(sParagraph, 
                sRegExPrefix, sRegExSuffix, iPrevGroupNumber, bMayLeave)){
            tempRes.setBestResult(iPrevGroupNumber[0]);
            return Variables().prepareReturn(true);
        }

        tempRes.resetInitialValues(iPrevGroupNumber);
        if (capture_DAY_DMY_Case3(sParagraph, 
                sRegExPrefix, sRegExSuffix, iPrevGroupNumber, bMayLeave)){
            tempRes.setBestResult(iPrevGroupNumber[0]);
            return Variables().prepareReturn(true);
        }
        
        tempRes.resetInitialValues(iPrevGroupNumber);
        if (capture_DAY_DMY_Case4(sParagraph, 
                sRegExPrefix, sRegExSuffix, iPrevGroupNumber, bMayLeave)){
            tempRes.setBestResult(iPrevGroupNumber[0]);
            return Variables().prepareReturn(true);
        }

        tempRes.prepareReturn(iPrevGroupNumber);
        return Variables().prepareReturn(tempRes.bAtLeastOneResult);
    }        
//				#=================== DATE UNITS ====================     
    public boolean capture_DATE_UNIT(String sParagraph,String sRegExPrefix, String sRegExSuffix, 
             int[] iPrevGroupNumber,boolean bMayLeave){
/*
$DATE_UNIT			= 
 qr/(?{$tempPart = '';})($DU)(?{$tempX = $map_val{lc($^N)};})
 ($WS$AND_PARTS)?(?{$tempDU = "$tempPart$tempX";})/;
#date units
 */        
        Variables().startCapture();
        String sPattern = ConstRegEx.groupBrackets(
                Variables().hashtbLexValues.get("DU")
                , iPrevGroupNumber);
        //String sNewSuffix = ConstRegEx.REST_ANY + sRegExSuffix;//2008.10.22
        Pattern patternLex = Pattern.compile(
                ConstRegEx.prefixWordSuffix(sPattern, sRegExPrefix, sRegExSuffix));
        boolean bResult = Find().printAllMatchResult(patternLex, sParagraph, false);
        if (bResult){
            try {
                Variables().saveCurrentMatch(iPrevGroupNumber[0]);

                Variables().tempVariables.stempPart = "";
                Variables().tempVariables.stempX = Variables().getMapHashValue(
                        Variables().getResult_LowerCase(iPrevGroupNumber[0])
                        );

                TempSaveCurrentMatch tmpMatch = new TempSaveCurrentMatch(Variables());
                sParagraph = tmpMatch.saveMatchAndMoveOverAndReset(sParagraph);

                String sNewPrefix = ConstRegEx.newPrefix(iPrevGroupNumber);
                
                bResult = pAnnotator.capture_WS_AND_PART_op(sParagraph,
                        sNewPrefix, sRegExSuffix, iPrevGroupNumber, true);
                if (bResult) 
                {
                    //$tempDU = "$tempPart$tempX";
                    try {
                        Variables().tempVariables.stempDU = 
                                Variables().tempVariables.stempPart + 
                                Variables().tempVariables.stempX;

                        tmpMatch.restoreCurrentMatch();
                        
                    } catch(Exception e){
                        if (!bMayLeave){ Const.writeSpecificError(e, sParagraph);}
                    }   
                }
                return Variables().prepareReturn(true);        
            } catch(Exception e){
                if (!bMayLeave){ Const.writeSpecificError(e, sParagraph);}
            }   

        }
        return Variables().prepareReturn(bResult);
    }
    public boolean capture_PLURAL_DATE_UNIT_LEX(String sParagraph,String sRegExPrefix, String sRegExSuffix, 
             int[] iPrevGroupNumber,boolean bMayLeave){
/*
$PLURAL_DATE_UNIT_LEX		=qr/($DUP)(?{$tempDU = $map_val{lc($^N)};})/;
				#weeks, months, years etc 
 */       
        Variables().startCapture();
        String sPattern = ConstRegEx.groupBrackets(
                Variables().hashtbLexValues.get("DUP")
                , iPrevGroupNumber);
        Pattern patternLex = Pattern.compile(
                ConstRegEx.prefixWordSuffix(sPattern, sRegExPrefix, sRegExSuffix));
        boolean bResult = Find().printAllMatchResult(patternLex, sParagraph, false);
        if (bResult){
            try {
                Variables().tempVariables.stempDU = Variables().getMapHashValue(
                        Variables().getResult_LowerCase(iPrevGroupNumber[0])
                        );

                Variables().saveCurrentMatch(iPrevGroupNumber[0]);
            } catch(Exception e){
                if (!bMayLeave){ Const.writeSpecificError(e, sParagraph);}
                Variables().tempVariables.sCurrentMatch = "";
            }   

        }
        return Variables().prepareReturn(bResult);
    }
//#============== QUANT + DATE/TIME UNITS ============ 

    public boolean capture_NUM_PLURAL_DATE_Case1_1(String sParagraph,
            String sRegExPrefix, String sRegExSuffix, int[] iPrevGroupNumber,boolean bMayLeave) {
/*
 * $NUM_PLURAL_DATE		= qr/
 (($DATE_UNIT|$PLURAL_DATE_UNIT_LEX)($WS$AND_PARTS)?
 * (?{$tempNumPluralDate="$tempNo$tempPart$tempDU"})/i;
 */        
        Variables().startCapture();
        //String sNewSuf =  ConstRegEx.REST_ANY + sRegExSuffix;//2008.10.22

        TempResultVariables tempRes = new TempResultVariables(Variables());
        tempRes.setInitialValues(iPrevGroupNumber);

        if (capture_PLURAL_DATE_UNIT_LEX(sParagraph, 
                sRegExPrefix, sRegExSuffix, iPrevGroupNumber, bMayLeave)){
            tempRes.setBestResult( iPrevGroupNumber[0]);    
        }
        if (!tempRes.bAtLeastOneResult) {
            tempRes.resetInitialValues(iPrevGroupNumber);
            if (capture_DATE_UNIT(sParagraph, sRegExPrefix, 
                    sRegExSuffix, iPrevGroupNumber, bMayLeave)) {
                tempRes.setBestResult(iPrevGroupNumber[0]);
            }
        }
        tempRes.prepareReturn(iPrevGroupNumber);
        if (tempRes.bAtLeastOneResult) {
            try {
                TempSaveCurrentMatch tmpMatch = new TempSaveCurrentMatch(Variables());
                sParagraph = tmpMatch.saveMatchAndMoveOverAndReset(sParagraph);
                String sNewPref = ConstRegEx.newPrefix(iPrevGroupNumber);
                boolean bResult = pAnnotator.capture_WS_AND_PART_op(sParagraph, 
                        sNewPref, sRegExSuffix, iPrevGroupNumber, true);
                if (bResult) 
                {
                    //* (?{$tempNumPluralDate="$tempNo$tempPart$tempDU"})/i;
                    try {
                        Variables().tempVariables.stempNumPluralDate =
                                Variables().tempVariables.stempNo +
                                Variables().tempVariables.stempPart +
                                Variables().tempVariables.stempDU;
                        tmpMatch.restoreCurrentMatch();
                    } catch(Exception e){
                        if (!bMayLeave) {
                            System.out.println(e.toString());
                        }
                    }   
                    return Variables().prepareReturn(true);
                }
                //return Variables().prepareReturn(bResult);
            } catch(Exception e){
                if (!bMayLeave){ Const.writeSpecificError(e, sParagraph);}
            }   
        }
        return Variables().prepareReturn(tempRes.bAtLeastOneResult);
    }
    public boolean capture_NUM_PLURAL_DATE_Case1_2(String sParagraph,
            String sRegExPrefix, String sRegExSuffix, int[] iPrevGroupNumber,boolean bMayLeave){
/*
 $NUM_PLURAL_DATE		= qr/ 
    |$AND_PARTS$WS($PLURAL_DATE_UNIT_LEX|$DATE_UNIT)
 )(?{$tempNumPluralDate="$tempNo$tempPart$tempDU"})/i;
#"three years, a few years" 

 */     
        Variables().startCapture();
        String sNewSuffix = ConstRegEx.REST_ANY + sRegExSuffix;
        String sNewPrefix="";
        if (pAnnotator.capture_AND_PARTS(sParagraph, sRegExPrefix, sNewSuffix, iPrevGroupNumber, bMayLeave)){
            try {
                TempSaveCurrentMatch tmpMatch = new TempSaveCurrentMatch(Variables());
                sParagraph = tmpMatch.saveMatchAndMoveOverAndReset(sParagraph);
                sNewPrefix = ConstRegEx.newPrefix(iPrevGroupNumber);
                
                boolean bRes = pAnnotator.comPrefSuf.capture_WS(sParagraph, 
                        sNewPrefix, sNewSuffix, iPrevGroupNumber, bMayLeave);
                if (bRes) {
                    sParagraph = tmpMatch.saveMatchAndMoveOverAndReset(sParagraph);
                    sNewPrefix = ConstRegEx.newPrefix(iPrevGroupNumber);
                    
                    TempResultVariables tempRes = new TempResultVariables(Variables());
                    tempRes.setInitialValues(iPrevGroupNumber);
                    if (capture_PLURAL_DATE_UNIT_LEX(sParagraph, sNewPrefix, sRegExSuffix, iPrevGroupNumber, bMayLeave)) {
                        tempRes.setBestResult(iPrevGroupNumber[0]);
                    }
                    if (!tempRes.bAtLeastOneResult) {
                        tempRes.resetInitialValues(iPrevGroupNumber);
                        if (capture_DATE_UNIT(sParagraph, sNewPrefix, sRegExSuffix, iPrevGroupNumber, bMayLeave)) {
                            tempRes.setBestResult(iPrevGroupNumber[0]);
                        }
                    }
                    tempRes.prepareReturn(iPrevGroupNumber);
                    if (tempRes.bAtLeastOneResult) {
                        try {
                            Variables().tempVariables.stempNumPluralDate =
                                    Variables().tempVariables.stempNo +
                                    Variables().tempVariables.stempPart +
                                    Variables().tempVariables.stempDU;

                            tmpMatch.restoreCurrentMatch();
                        } catch (Exception e) {
                            if (!bMayLeave) {
                                System.out.println(e.toString());
                            }
                        }
                    }
                    return Variables().prepareReturn(tempRes.bAtLeastOneResult);
                }
                return Variables().prepareReturn(bRes);
            } catch(Exception e){
                if (!bMayLeave){ Const.writeSpecificError(e, sParagraph);}
            }   
        }
        return Variables().prepareReturn(false);
    }
    public boolean capture_NUM_PLURAL_DATE_Case1(String sParagraph,
            String sRegExPrefix, String sRegExSuffix, int[] iPrevGroupNumber,boolean bMayLeave){
        //$QUANTT case
/*
$NUM_PLURAL_DATE		= qr/
 ($THE$WS)?(($QUANTT|$TO_99_MIX)$WS?(?:straight)?
 (?{$tempNo = $ordWords->val();$tempPart = '';}))
 ($WS?$DASH$WS?|$WS)
 (($DATE_UNIT|$PLURAL_DATE_UNIT_LEX)($WS$AND_PARTS)?
    |$AND_PARTS$WS($PLURAL_DATE_UNIT_LEX|$DATE_UNIT)
 )(?{$tempNumPluralDate="$tempNo$tempPart$tempDU"})/i;
#"three years, a few years" 
 */        
        Variables().startCapture();
        String sNewSuffix = ConstRegEx.REST_ANY  + sRegExSuffix;
        boolean bRes = pAnnotator.comPrefSuf.capture_THE_WS_op(sParagraph, 
                sRegExPrefix, sNewSuffix, iPrevGroupNumber, bMayLeave);
        if (bRes){
            TempSaveCurrentMatch tmpMatch = new TempSaveCurrentMatch(Variables());
            sParagraph = tmpMatch.saveMatchAndMoveOverAndReset(sParagraph);
            String sNewPrefix = ConstRegEx.newPrefix(iPrevGroupNumber);
            
            /*bRes = pAnnotator.capture_QUANTT(sParagraph, 
                    sNewPrefix, sNewSuffix, iPrevGroupNumber, bMayLeave);*/
            bRes = pAnnotator.capture_QUANTT(sParagraph, 
                    sNewPrefix, sRegExSuffix, iPrevGroupNumber, bMayLeave);
            if (bRes){
                sParagraph = tmpMatch.saveMatchAndMoveOverAndReset(sParagraph);
                sNewPrefix = ConstRegEx.newPrefix(iPrevGroupNumber);
                /*2008.10.22
                bRes = pAnnotator.comPrefSuf.capture_WS_op_straight_op(sParagraph, 
                        sNewPrefix, sNewSuffix, iPrevGroupNumber, bMayLeave);*/
                bRes = pAnnotator.comPrefSuf.capture_WS_op_straight_op_WS_op_DASH_WS_op_or_WS(
                        sParagraph, 
                        sNewPrefix, sNewSuffix, iPrevGroupNumber, bMayLeave);                
                if (bRes){
                    //(?{$tempNo = $ordWords->val();$tempPart = '';}))
                    Variables().tempVariables.stempNo = 
                            Variables().tempVariables.ordWords.get_val().toString();
                    Variables().tempVariables.stempPart = "";
                    //2008.10.22
/*                    sParagraph = tmpMatch.saveMatchAndMoveOverAndReset(sParagraph);
                    sNewPrefix = ConstRegEx.newPrefix(iPrevGroupNumber);
                    bRes = pAnnotator.comPrefSuf.capture_WS_op_DASH_WS_op_or_WS(sParagraph,
                            sNewPrefix, sNewSuffix, iPrevGroupNumber, bMayLeave);*/
                    if (bRes){
                        sParagraph = tmpMatch.saveMatchAndMoveOverAndReset(sParagraph);
                        sNewPrefix = ConstRegEx.newPrefix(iPrevGroupNumber);
                        
                        try {
                            TempResultVariables tempRes = new TempResultVariables(Variables());
                            tempRes.setInitialValues(iPrevGroupNumber);

                            if (capture_NUM_PLURAL_DATE_Case1_1(sParagraph,
                                    sNewPrefix, sRegExSuffix, iPrevGroupNumber, bMayLeave)) {
                                tempRes.setBestResult(iPrevGroupNumber[0]);
                            }
                            if (!tempRes.bAtLeastOneResult) {
                                tempRes.resetInitialValues(iPrevGroupNumber);
                                if (capture_NUM_PLURAL_DATE_Case1_2(sParagraph, sNewPrefix, sRegExSuffix, iPrevGroupNumber, bMayLeave)) {
                                    tempRes.setBestResult(iPrevGroupNumber[0]);
                                }
                            }
                            tempRes.prepareReturn(iPrevGroupNumber);
                            if (tempRes.bAtLeastOneResult) {
                                tmpMatch.restoreCurrentMatch();
                            }
                            return Variables().prepareReturn(tempRes.bAtLeastOneResult);
                        } catch (Exception e) {
                            if (!bMayLeave) {
                                Const.writeSpecificError(e, sParagraph);
                            }
                        }
                    }
                }
            }
        }
        return  Variables().prepareReturn(bRes);
    }
    
     public boolean capture_NUM_PLURAL_DATE_Case2(String sParagraph,
            String sRegExPrefix, String sRegExSuffix, int[] iPrevGroupNumber,boolean bMayLeave){
        //$TO_99_MIX case
/*
$NUM_PLURAL_DATE		= qr/
 ($THE$WS)?(($QUANTT|$TO_99_MIX)$WS?(?:straight)?
 (?{$tempNo = $ordWords->val();$tempPart = '';}))
 ($WS?$DASH$WS?|$WS)
 (($DATE_UNIT|$PLURAL_DATE_UNIT_LEX)($WS$AND_PARTS)?
    |$AND_PARTS$WS($PLURAL_DATE_UNIT_LEX|$DATE_UNIT)
 )(?{$tempNumPluralDate="$tempNo$tempPart$tempDU"})/i;
#"three years, a few years" 
 */        
        Variables().startCapture();
        String sNewSuffix = ConstRegEx.REST_ANY  + sRegExSuffix;
        boolean bRes = pAnnotator.comPrefSuf.capture_THE_WS_op(sParagraph, 
                sRegExPrefix, sNewSuffix, iPrevGroupNumber, bMayLeave);
        if (bRes){
            TempSaveCurrentMatch tmpMatch = new TempSaveCurrentMatch(Variables());
            sParagraph = tmpMatch.saveMatchAndMoveOverAndReset(sParagraph);
            String sNewPrefix = ConstRegEx.newPrefix(iPrevGroupNumber);
            
            bRes = pAnnotator.capture_TO_99_MIX(sParagraph, 
                    sNewPrefix, sNewSuffix, iPrevGroupNumber, bMayLeave);
            if (bRes){
                sParagraph = tmpMatch.saveMatchAndMoveOverAndReset(sParagraph);
                sNewPrefix = ConstRegEx.newPrefix(iPrevGroupNumber);
/*                2008.10.22
                bRes = pAnnotator.comPrefSuf.capture_WS_op_straight_op(sParagraph, 
                        sNewPrefix, sNewSuffix, iPrevGroupNumber, bMayLeave);*/
                bRes = pAnnotator.comPrefSuf.capture_WS_op_straight_op_WS_op_DASH_WS_op_or_WS(
                        sParagraph, 
                        sNewPrefix, sNewSuffix, iPrevGroupNumber, bMayLeave);                
                
                if (bRes){
                    //(?{$tempNo = $ordWords->val();$tempPart = '';}))
                    Variables().tempVariables.stempNo = 
                            Variables().tempVariables.ordWords.get_val().toString();
                    Variables().tempVariables.stempPart = "";
                    //2008.10.22
/*                    sParagraph = tmpMatch.saveMatchAndMoveOverAndReset(sParagraph);
                    sNewPrefix = ConstRegEx.newPrefix(iPrevGroupNumber);
                    bRes = pAnnotator.comPrefSuf.capture_WS_op_DASH_WS_op_or_WS(sParagraph,
                            sNewPrefix, sNewSuffix, iPrevGroupNumber, bMayLeave);*/
                    if (bRes){
                        sParagraph = tmpMatch.saveMatchAndMoveOverAndReset(sParagraph);
                        sNewPrefix = ConstRegEx.newPrefix(iPrevGroupNumber);
                        
                        try {
                            TempResultVariables tempRes = new TempResultVariables(Variables());
                            tempRes.setInitialValues(iPrevGroupNumber);

                            if (capture_NUM_PLURAL_DATE_Case1_1(sParagraph,
                                    sNewPrefix, sRegExSuffix, iPrevGroupNumber, bMayLeave)) {
                                tempRes.setBestResult(iPrevGroupNumber[0]);
                            }
                            if (!tempRes.bAtLeastOneResult) {
                                tempRes.resetInitialValues(iPrevGroupNumber);
                                if (capture_NUM_PLURAL_DATE_Case1_2(sParagraph, sNewPrefix, sRegExSuffix, iPrevGroupNumber, bMayLeave)) {
                                    tempRes.setBestResult(iPrevGroupNumber[0]);
                                }
                            }
                            tempRes.prepareReturn(iPrevGroupNumber);
                            if (tempRes.bAtLeastOneResult) {
                                tmpMatch.restoreCurrentMatch();
                            }
                            return Variables().prepareReturn(tempRes.bAtLeastOneResult);
                        } catch (Exception e) {
                            if (!bMayLeave) {
                                Const.writeSpecificError(e, sParagraph);
                            }
                        }
                    }
                }
            }
        }
        return  Variables().prepareReturn(bRes);
    }
    public boolean capture_NUM_PLURAL_DATE(String sParagraph,
            String sRegExPrefix, String sRegExSuffix, int[] iPrevGroupNumber,boolean bMayLeave){
/*
$NUM_PLURAL_DATE		= qr/
 ($THE$WS)?(($QUANTT|$TO_99_MIX)$WS?(?:straight)?
 (?{$tempNo = $ordWords->val();$tempPart = '';}))
 ($WS?$DASH$WS?|$WS)
 (($DATE_UNIT|$PLURAL_DATE_UNIT_LEX)($WS$AND_PARTS)?
    |$AND_PARTS$WS($PLURAL_DATE_UNIT_LEX|$DATE_UNIT)
 )(?{$tempNumPluralDate="$tempNo$tempPart$tempDU"})/i;
#"three years, a few years" 
 */        
        Variables().startCapture();
        TempResultVariables tempRes = new TempResultVariables(Variables());
        tempRes.setInitialValues(iPrevGroupNumber);
        
        if (capture_NUM_PLURAL_DATE_Case1(sParagraph, 
                sRegExPrefix, sRegExSuffix, iPrevGroupNumber, bMayLeave)){
             tempRes.setBestResult( iPrevGroupNumber[0]);    
             return Variables().prepareReturn(true);
        }
        
        tempRes.resetInitialValues(iPrevGroupNumber);
        if (capture_NUM_PLURAL_DATE_Case2(sParagraph, 
                sRegExPrefix, sRegExSuffix, iPrevGroupNumber, bMayLeave)){
            tempRes.setBestResult( iPrevGroupNumber[0]);    
            return Variables().prepareReturn(true);
        }
        
        tempRes.prepareReturn(iPrevGroupNumber);
        return Variables().prepareReturn(tempRes.bAtLeastOneResult);
    }
    //#========== GROUP DATE NAMES TOGETHER ==============
    public boolean capture_RECURRENT_DATE_NAME(String sParagraph,String sRegExPrefix, String sRegExSuffix, 
             int[] iPrevGroupNumber,boolean bMayLeave){
/*
$RECURRENT_DATE_NAME
 = qr/(?{$tempRecDate = "";})((($DM)(?{$tempRecDate = "M=".$tempMonth.",D=".$crtTimePoint->dayOfMonth(); $crtTimePoint->clear();}))|
 (($MONTH_NAME_IN_CONTEXT)(?{$tempRecDate = "M=".$tempMonth;}))|
 (($DAY_NAME_FULL_LEX|$WEEKEND)(?{$tempRecDate = "DOW=".$crtTimePoint->dayOfWeek();}))
 |(($SEASON_NAME)(?{$tempRecDate = "M=".$tempSeasonName;$crtTimePoint->month($tempSeasonName);}))|
 (($SPECIAL_DATE_NAME)(?{$tempRecDate = "SPECIAL";})))/;	
				#named units that can be used with pre-modifiers like "last/next" e.g. January, Tuesday, winter, Christmas  
*/        
        Variables().startCapture();
        Variables().tempVariables.stempRecDate = "";
        
        TempResultVariables tempRes = new TempResultVariables(Variables());
        tempRes.setInitialValues(iPrevGroupNumber);
        
        if (capture_DM(sParagraph, sRegExPrefix, sRegExSuffix, iPrevGroupNumber, bMayLeave)){
//(?{$tempRecDate = "M=".$tempMonth.",D=".$crtTimePoint->dayOfMonth(); $crtTimePoint->clear();}))
            Variables().tempVariables.stempRecDate = 
                    "M="+
                    Variables().tempVariables.stempMonth+",D="+
                    Variables().tempVariables.crtTimePoint.get("dayOfMonth");
            Variables().tempVariables.crtTimePoint.clear();
            tempRes.setBestResult(iPrevGroupNumber[0]);
            return Variables().prepareReturn(true);
        }
        
        tempRes.resetInitialValues(iPrevGroupNumber);
        if (pAnnotator.capture_MONTH_NAME_IN_CONTEXT(sParagraph, 
                sRegExPrefix, sRegExSuffix, iPrevGroupNumber, bMayLeave)) {
//(?{$tempRecDate = "M=".$tempMonth;}))
            Variables().tempVariables.stempRecDate = 
                    "M="+
                    Variables().tempVariables.stempMonth;            
            tempRes.setBestResult(iPrevGroupNumber[0]);
            return Variables().prepareReturn(true);
        }
        
        tempRes.resetInitialValues(iPrevGroupNumber);
        if (capture_DAY_NAME_FULL_LEX(sParagraph, 
                sRegExPrefix, sRegExSuffix, iPrevGroupNumber, bMayLeave)) 
        {
            //(?{$tempRecDate = "DOW=".$crtTimePoint->dayOfWeek();}))
            Variables().tempVariables.stempRecDate = 
                    "DOW="+
                    Variables().tempVariables.crtTimePoint.get("dayOfWeek");
            tempRes.setBestResult(iPrevGroupNumber[0]);
            return Variables().prepareReturn(true);
        }        
    
        tempRes.resetInitialValues(iPrevGroupNumber);
        if  (capture_WEEKEND(sParagraph, 
                sRegExPrefix, sRegExSuffix, iPrevGroupNumber, bMayLeave)){
            //(?{$tempRecDate = "DOW=".$crtTimePoint->dayOfWeek();}))
            Variables().tempVariables.stempRecDate = 
                    "DOW="+
                    Variables().tempVariables.crtTimePoint.get("dayOfWeek");
            tempRes.setBestResult(iPrevGroupNumber[0]);
            return Variables().prepareReturn(true);
        }

        tempRes.resetInitialValues(iPrevGroupNumber);
        if (pAnnotator.capture_SEASON_NAME(sParagraph, 
                sRegExPrefix, sRegExSuffix, iPrevGroupNumber, bMayLeave)) {
//(?{$tempRecDate = "M=".$tempSeasonName;$crtTimePoint->month($tempSeasonName);}))            
            Variables().tempVariables.stempRecDate = 
                    "M="+
                    Variables().tempVariables.stempSeasonName;
            Variables().tempVariables.crtTimePoint.set("month",
                    Variables().tempVariables.stempSeasonName);
            tempRes.setBestResult(iPrevGroupNumber[0]);
            return Variables().prepareReturn(true);
        }

        tempRes.resetInitialValues(iPrevGroupNumber);
        if (capture_SPECIAL_DATE_NAME(sParagraph,
                sRegExPrefix, sRegExSuffix, iPrevGroupNumber, bMayLeave)) {
//(?{$tempRecDate = "SPECIAL";}))
            Variables().tempVariables.stempRecDate = "SPECIAL";
            tempRes.setBestResult(iPrevGroupNumber[0]);
            return Variables().prepareReturn(true);
        }
        
        tempRes.prepareReturn(iPrevGroupNumber);
        return Variables().prepareReturn(tempRes.bAtLeastOneResult);
    }    
    //#================= REPETITIVE ====================== 
    public boolean capture_PLURAL_DATES_Case1(String sParagraph,String sRegExPrefix, String sRegExSuffix, 
             int[] iPrevGroupNumber,boolean bMayLeave){
        Variables().startCapture();
//($RECURRENT_DATE_NAME((e?s))        
        TempSaveCurrentMatch tmpMatch = new TempSaveCurrentMatch(Variables());
        boolean bRes = capture_RECURRENT_DATE_NAME(sParagraph,
                sRegExPrefix, sRegExSuffix, iPrevGroupNumber, bMayLeave);

        if (bRes) {
//(?{$crtTimePoint->clear();$crtTimePoint->function("plural($tempRecDate)");})
            sParagraph = tmpMatch.saveMatchAndMoveOverAndReset(sParagraph);
            String sNewPref = ConstRegEx.newPrefix(iPrevGroupNumber);

            bRes = pAnnotator.comPrefSuf.capture_e_op_s(sParagraph,
                    sNewPref, sRegExSuffix, iPrevGroupNumber, bMayLeave);
            if (bRes) {
                Variables().tempVariables.crtTimePoint.clear();
                Variables().tempVariables.crtTimePoint.set("function",
                        "plural(" +
                        Variables().tempVariables.stempRecDate +
                        ")");
                
                tmpMatch.restoreCurrentMatch();
            }

        }
        return Variables().prepareReturn(bRes);
    }    
    public boolean capture_PLURAL_DATES(String sParagraph,String sRegExPrefix, String sRegExSuffix, 
             int[] iPrevGroupNumber,boolean bMayLeave){
/*
$PLURAL_DATES= 
 qr/($THE$WS)?
 (?!$MONTH_NAME_ABBREV_LEX(e?s))
 ($RECURRENT_DATE_NAME((e?s))(?{$crtTimePoint->clear();$crtTimePoint->function("plural($tempRecDate)");})
 |$PLURAL_DAY_PART(?{$crtTimePoint->function("plural($tempTU)");})|
 $PLURAL_DATE_UNIT_LEX(?{$crtTimePoint->function("plural($tempDU)");}))/i;
# years, mornings
 */        
        TempSaveCurrentMatch tmpMatch = new TempSaveCurrentMatch(Variables());
        String sNewSuf = ConstRegEx.REST_ANY + sRegExSuffix;
        boolean bRes =pAnnotator.comPrefSuf.capture_THE_WS_op(sParagraph, 
                sRegExPrefix, sNewSuf, iPrevGroupNumber, bMayLeave);
        if (bRes){
            sParagraph = tmpMatch.saveMatchAndMoveOverAndReset(sParagraph);
            String sNewPref = ConstRegEx.newPrefix(iPrevGroupNumber);
            
            TempResultVariables tempRes = new TempResultVariables(Variables());
            tempRes.setInitialValues(iPrevGroupNumber);
            
            if(capture_PLURAL_DATES_Case1(sParagraph, 
                    sNewPref, sNewSuf, iPrevGroupNumber, bMayLeave)){
                tempRes.setBestResult(iPrevGroupNumber[0]);
            }

            if (!tempRes.bAtLeastOneResult){
                tempRes.resetInitialValues(iPrevGroupNumber);
                if (pAnnotator.capture_PLURAL_DAY_PART(sParagraph, 
                        sNewPref, sRegExSuffix, iPrevGroupNumber, bMayLeave)){
//(?{$crtTimePoint->function("plural($tempTU)");})                    
                    Variables().tempVariables.crtTimePoint.set("function",
                        "plural("+
                            Variables().tempVariables.stempTU+
                        ")"
                        );
                     tempRes.setBestResult(iPrevGroupNumber[0]);
                }
            }

            if (!tempRes.bAtLeastOneResult){
                tempRes.resetInitialValues(iPrevGroupNumber);
                if (capture_PLURAL_DATE_UNIT_LEX(sParagraph, 
                        sNewPref, sRegExSuffix, iPrevGroupNumber, bMayLeave)){
//(?{$crtTimePoint->function("plural($tempDU)");})                  
                    Variables().tempVariables.crtTimePoint.set("function",
                        "plural("+
                            Variables().tempVariables.stempDU+
                        ")"
                        );
                     tempRes.setBestResult(iPrevGroupNumber[0]);
                }
            }
            
            tempRes.prepareReturn(iPrevGroupNumber);
            if (tempRes.bAtLeastOneResult) {
                tmpMatch.restoreCurrentMatch();
            }
            return Variables().prepareReturn(tempRes.bAtLeastOneResult);
        }
        
        return Variables().prepareReturn(bRes);
    }    
    //#======= LAST/PAST/NEXT/FIRST DATE =================
    public boolean capture_PAST_SING_DATE(String sParagraph,String sRegExPrefix, String sRegExSuffix, 
             int[] iPrevGroupNumber,boolean bMayLeave){
/*
$PAST_SING_DATE			= qr/($THE$WS)?$LAST_PAST_WORD$WS$DATE_UNIT
 (?{$tempPastDate = "$tempLP($tempDU)"; $crtTimePoint->function($tempPastDate);})/i; 
				#"past year, previous month"
 */        
        Variables().startCapture();
        String sNewSuf = ConstRegEx.REST_ANY +sRegExSuffix;
        boolean bRes = pAnnotator.comPrefSuf.capture_THE_WS_op(sParagraph, 
                sRegExPrefix, sNewSuf, iPrevGroupNumber, bMayLeave);
        if (bRes){
            TempSaveCurrentMatch tmpMatch = new TempSaveCurrentMatch(Variables());
            sParagraph = tmpMatch.saveMatchAndMoveOverAndReset(sParagraph);
            String sNewPref = ConstRegEx.newPrefix(iPrevGroupNumber);
            
            bRes = pAnnotator.capture_LAST_PAST_WORD(sParagraph, sNewPref, sNewSuf, 
                    iPrevGroupNumber, bMayLeave);
            if (bRes){
                sParagraph = tmpMatch.saveMatchAndMoveOverAndReset(sParagraph);
                sNewPref = ConstRegEx.newPrefix(iPrevGroupNumber);
                
                @SuppressWarnings("unused")
                String sDate = tmpMatch.sMatch;
                
                bRes = pAnnotator.comPrefSuf.capture_WS(sParagraph, 
                        sNewPref, sNewSuf, iPrevGroupNumber, bMayLeave);
                if (bRes){
                    sParagraph = tmpMatch.saveMatchAndMoveOverAndReset(sParagraph);
                    sNewPref = ConstRegEx.newPrefix(iPrevGroupNumber);
                    
                    bRes = capture_DATE_UNIT(sParagraph, 
                       sNewPref, sRegExSuffix, iPrevGroupNumber, bMayLeave);
                    if (bRes) {
                        try {
//(?{$tempPastDate = "$tempLP($tempDU)"; $crtTimePoint->function($tempPastDate);})/i;                             
                            Variables().tempVariables.stempPastDate =
                                    Variables().tempVariables.stempLP +
                                    "(" + Variables().tempVariables.stempDU + ")";
                            Variables().tempVariables.crtTimePoint.set("function",
                                    Variables().tempVariables.stempPastDate);
                            tmpMatch.restoreCurrentMatch();
                        } catch (Exception e) {
                            if (!bMayLeave) {Const.writeSpecificError(e, sParagraph);}
                        }
                    }
                       
                }
            }
        }
        return Variables().prepareReturn(bRes);
        
    }    
    public boolean capture_RECURRENT(String sParagraph,String sRegExPrefix, String sRegExSuffix, 
             int[] iPrevGroupNumber,boolean bMayLeave){
/*
$RECURRENT= 
 qr/$RECURRENT_DATE_NAME|
 (($DATE_UNIT)(?{$tempRecDate = $tempDU;}))/;
				#units that can be used with pre-modifiers like "last/next" e.g. January, Tuesday, winter, Christmas, week, month, year,  
 */      
        Variables().startCapture();
        TempResultVariables tempRes = new TempResultVariables(Variables());
        tempRes.setInitialValues(iPrevGroupNumber);        
        
        if (capture_RECURRENT_DATE_NAME(sParagraph, 
                sRegExPrefix, sRegExSuffix, iPrevGroupNumber, bMayLeave)){
            tempRes.setBestResult(iPrevGroupNumber[0]);
            return Variables().prepareReturn(true);            
        }
        
        tempRes.resetInitialValues(iPrevGroupNumber);            
        if (capture_DATE_UNIT(sParagraph, sRegExPrefix, 
                sRegExSuffix, iPrevGroupNumber, bMayLeave)){
//(?{$tempRecDate = $tempDU;}))
            Variables().tempVariables.stempRecDate =
                    Variables().tempVariables.stempDU;
            tempRes.setBestResult(iPrevGroupNumber[0]);
            return Variables().prepareReturn(true);            
        }
        
        tempRes.prepareReturn(iPrevGroupNumber);
        return Variables().prepareReturn(tempRes.bAtLeastOneResult);

    }    
    public boolean capture_SIMPLE_DEICTIC(String sParagraph,String sRegExPrefix, String sRegExSuffix, 
             int[] iPrevGroupNumber,boolean bMayLeave){
/*
$SIMPLE_DEICTIC= 
 qr/$LAST_NEXT_WORD$WS($VERY$WS)?
 (
 ( ($DM)(?{$tempRecDate = "M=".$tempMonth.",D=".$crtTimePoint->dayOfMonth();}))|
 $RECURRENT)(?{$crtTimePoint->clear();$crtTimePoint->function($tempLN?"$tempLN($tempRecDate)":$tempRecDate); $tempRecDate = "";})/;
				#last January, next Christmas, that very week 
*/        
        Variables().startCapture();
        String sNewSuf = ConstRegEx.REST_ANY + sRegExSuffix;
        boolean bRes = pAnnotator.capture_LAST_NEXT_WORD(sParagraph, 
                sRegExPrefix, sNewSuf, iPrevGroupNumber, bMayLeave);
        if (bRes){
            TempSaveCurrentMatch tmpMatch = new TempSaveCurrentMatch(Variables());
            sParagraph = tmpMatch.saveMatchAndMoveOverAndReset(sParagraph);
            String sNewPref = ConstRegEx.newPrefix(iPrevGroupNumber);
            
            bRes = pAnnotator.comPrefSuf.capture_WS_VERY_WS_op(sParagraph,
                    sNewPref, sNewSuf, iPrevGroupNumber, bMayLeave);
            if (bRes){
                sParagraph = tmpMatch.saveMatchAndMoveOverAndReset(sParagraph);
                sNewPref = ConstRegEx.newPrefix(iPrevGroupNumber);

                try {
                    TempResultVariables tempRes = new TempResultVariables(Variables());
                    tempRes.setInitialValues(iPrevGroupNumber);

                    if (capture_DM(sParagraph, sNewPref,
                            sRegExSuffix, iPrevGroupNumber, bMayLeave)) {
//(?{$tempRecDate = "M=".$tempMonth.",D=".$crtTimePoint->dayOfMonth();}))
                        Variables().tempVariables.stempRecDate =
                                "M=" +
                                Variables().tempVariables.stempMonth +
                                ",D=" +
                                Variables().tempVariables.crtTimePoint.get("dayOfMonth");
                        tempRes.setBestResult(iPrevGroupNumber[0]);
                    }
                    if (!tempRes.bAtLeastOneResult) {
                        tempRes.resetInitialValues(iPrevGroupNumber);
                        if (capture_RECURRENT(sParagraph, sNewPref,
                                sRegExSuffix, iPrevGroupNumber, bMayLeave)) {
//(?{$crtTimePoint->clear();$crtTimePoint->function($tempLN?"$tempLN($tempRecDate)":$tempRecDate); $tempRecDate = "";})/                       
                            Variables().tempVariables.crtTimePoint.clear();
                            String sTemp = "";
                            if (Variables().tempVariables.stempLN.length() > 0) {
                                sTemp = Variables().tempVariables.stempLN + "(" +
                                        Variables().tempVariables.stempRecDate + ")";
                            } else {
                                sTemp = Variables().tempVariables.stempRecDate;
                            }
                            Variables().tempVariables.crtTimePoint.set("function", sTemp);

                            Variables().tempVariables.stempRecDate = "";
                            tempRes.setBestResult(iPrevGroupNumber[0]);
                        }
                    }

                    tempRes.prepareReturn(iPrevGroupNumber);
                    if (tempRes.bAtLeastOneResult){
                        tmpMatch.restoreCurrentMatch();
                    }
                    return Variables().prepareReturn(tempRes.bAtLeastOneResult);
                } catch (Exception e) {
                    if (!bMayLeave){ Const.writeSpecificError(e, sParagraph);}               
                }
            }
        }
        return Variables().prepareReturn(bRes);
    }
//#============= DAY-PARTS IN CONTEXT ====================== 
    public boolean capture_PARTS_DATE_UNIT(String sParagraph,String sRegExPrefix, String sRegExSuffix, 
             int[] iPrevGroupNumber,boolean bMayLeave){
/*
$PARTS_DATE_UNIT		
 = qr/($PARTS)$WS($OF$WS)?($AANONE$WS)?$DATE_UNIT(?{$crtTimePoint->val("$tempPart$tempDU");})/;
 */    
        Variables().startCapture();
        String sNewSuffix = ConstRegEx.REST_ANY + sRegExSuffix;
        
        boolean bRes = pAnnotator.capture_PARTS(sParagraph, sRegExPrefix, 
                sNewSuffix, iPrevGroupNumber, bMayLeave);
        if (bRes){
            TempSaveCurrentMatch tmpMatch = new TempSaveCurrentMatch(Variables());
            sParagraph = tmpMatch.saveMatchAndMoveOverAndReset(sParagraph);
            String sNewPrefix = ConstRegEx.newPrefix(iPrevGroupNumber);
            
            bRes = pAnnotator.comPrefSuf.capture_WS_OF_WS_op(sParagraph, 
                    sNewPrefix, sNewSuffix, iPrevGroupNumber, bMayLeave);
            if (bRes){
                sParagraph = tmpMatch.saveMatchAndMoveOverAndReset(sParagraph);
                sNewPrefix = ConstRegEx.newPrefix(iPrevGroupNumber);
                
                bRes = pAnnotator.comPrefSuf.capture_AANONE_WS(sParagraph, 
                        sNewPrefix, sNewSuffix, iPrevGroupNumber, true);
                //if(bRes)//?
                {
                    sParagraph = tmpMatch.saveMatchAndMoveOverAndReset(sParagraph);
                    sNewPrefix = ConstRegEx.newPrefix(iPrevGroupNumber);
                    bRes = capture_DATE_UNIT(sParagraph, 
                            sNewPrefix, sRegExSuffix, iPrevGroupNumber, bMayLeave);
                    if (bRes) {
                        try {
                            Variables().tempVariables.crtTimePoint.set("val",
                                    Variables().tempVariables.stempPart + Variables().tempVariables.stempDU);

                            tmpMatch.restoreCurrentMatch();
                        } catch (Exception e) {
                            if (!bMayLeave) {System.out.println(e.toString());}
                        }
                    }
                }
            }
        }
        return Variables().prepareReturn(bRes);
    }
//				#=================== DURATIONS ==========================  
    public boolean capture_NUM_SINGULAR_DATE_Case1(String sParagraph,String sRegExPrefix, String sRegExSuffix, 
             int[] iPrevGroupNumber,boolean bMayLeave){
/*
$NUM_SINGULAR_DATE		= 
 qr/(?{$tempPart = '';$tempSingularDate = '';})
 ($AANONE?$WS?$DATE_UNIT($WS$AND_PARTS)?
 (?{$tempSingularDate = "1$tempPart$tempDU";})
 /i;
#"one year, a year and a half, one and a half years" 
 */     
        Variables().startCapture();
        String sNewSuffix = ConstRegEx.REST_ANY+sRegExSuffix;
        boolean bRes = pAnnotator.comPrefSuf.capture_AANONE_WS_op(sParagraph, 
                sRegExPrefix, sNewSuffix, iPrevGroupNumber, bMayLeave);
        if (bRes){
                TempSaveCurrentMatch tmpMatch = new TempSaveCurrentMatch(Variables());
                sParagraph = tmpMatch.saveMatchAndMoveOverAndReset(sParagraph);
                String sNewPrefix = ConstRegEx.newPrefix(iPrevGroupNumber);
                
                bRes = capture_DATE_UNIT(sParagraph, 
                        sNewPrefix, sRegExSuffix, iPrevGroupNumber, bMayLeave);//2008.10.22
                if (bRes){
                    sParagraph = tmpMatch.saveMatchAndMoveOverAndReset(sParagraph);
                    sNewPrefix = ConstRegEx.newPrefix(iPrevGroupNumber);
                    bRes = pAnnotator.capture_WS_AND_PART_op(sParagraph,
                            sNewPrefix, sRegExSuffix, iPrevGroupNumber, true);
                    if (bRes) 
                    {
                        try {
                            Variables().tempVariables.stempSingularDate = "1" +
                                    Variables().tempVariables.stempPart +
                                    Variables().tempVariables.stempDU;
                            tmpMatch.restoreCurrentMatch();
                        } catch (Exception e) {
                            if (!bMayLeave) {System.out.println(e.toString());}
                        }
                        return Variables().prepareReturn(true);
                    }
                    
                }
        }
        return Variables().prepareReturn(bRes);
    }
    
    public boolean capture_NUM_SINGULAR_DATE_Case2(String sParagraph,String sRegExPrefix, String sRegExSuffix, 
             int[] iPrevGroupNumber,boolean bMayLeave){
/*
$NUM_SINGULAR_DATE		= 
qr/|
$AANONE$WS$AND_PARTS$WS($PLURAL_DATE_UNIT_LEX 
 
 |$DATE_UNIT)(?{$tempSingularDate="1$tempPart$tempDU"}))/i;
				#"one year, a year and a half, one and a half years"  * 
 */        
         Variables().startCapture();
         String sNewSuffix = ConstRegEx.REST_ANY+sRegExSuffix;
         boolean bRes = pAnnotator.comPrefSuf.capture_AANONE_WS(sParagraph, 
                 sRegExPrefix, sNewSuffix, iPrevGroupNumber, bMayLeave);
         if (bRes){
             TempSaveCurrentMatch tmpMatch = new TempSaveCurrentMatch(Variables());
             sParagraph = tmpMatch.saveMatchAndMoveOverAndReset(sParagraph);
             String sNewPreffix = ConstRegEx.newPrefix(iPrevGroupNumber);
             
             bRes = pAnnotator.capture_AND_PARTS(sParagraph, 
                 sNewPreffix, sNewSuffix, iPrevGroupNumber, bMayLeave);
             if (bRes){
                 sParagraph = tmpMatch.saveMatchAndMoveOverAndReset(sParagraph);
                 sNewPreffix = ConstRegEx.newPrefix(iPrevGroupNumber);
                 
                 bRes = pAnnotator.comPrefSuf.capture_WS(sParagraph, 
                         sNewPreffix, sNewSuffix, iPrevGroupNumber, bMayLeave);
                 if (bRes) {
                     sParagraph = tmpMatch.saveMatchAndMoveOverAndReset(sParagraph);
                     sNewPreffix = ConstRegEx.newPrefix(iPrevGroupNumber);

                     bRes = capture_PLURAL_DATE_UNIT_LEX(sParagraph,
                             sNewPreffix, sRegExSuffix, iPrevGroupNumber, bMayLeave);
                     if (bRes) {
                         try {
                             Variables().tempVariables.stempSingularDate =
                                     "1" +
                                     Variables().tempVariables.stempPart +
                                     Variables().tempVariables.stempDU;

                             tmpMatch.restoreCurrentMatch();
                         } catch (Exception e) {
                             if (!bMayLeave) {System.out.println(e.toString());}
                         }
                     }
                 }
             }
         }
         return Variables().prepareReturn(bRes);
    }
    
    public boolean capture_NUM_SINGULAR_DATE_Case3(String sParagraph,String sRegExPrefix, String sRegExSuffix, 
             int[] iPrevGroupNumber,boolean bMayLeave){
/*
$NUM_SINGULAR_DATE		= 
qr/|
$AANONE$WS$AND_PARTS$WS(
 
 |$DATE_UNIT)(?{$tempSingularDate="1$tempPart$tempDU"}))/i;
				#"one year, a year and a half, one and a half years"  * 
 */        
         Variables().startCapture();
         String sNewSuffix = ConstRegEx.REST_ANY+sRegExSuffix;
         boolean bRes = pAnnotator.comPrefSuf.capture_AANONE_WS(sParagraph, 
                 sRegExPrefix, sNewSuffix, iPrevGroupNumber, bMayLeave);
         if (bRes){
             TempSaveCurrentMatch tmpMatch = new TempSaveCurrentMatch(Variables());
             sParagraph = tmpMatch.saveMatchAndMoveOverAndReset(sParagraph);
             String sNewPreffix = ConstRegEx.newPrefix(iPrevGroupNumber);
             
             bRes = pAnnotator.capture_AND_PARTS(sParagraph, 
                 sNewPreffix, sNewSuffix, iPrevGroupNumber, bMayLeave);
             if (bRes){
                 sParagraph = tmpMatch.saveMatchAndMoveOverAndReset(sParagraph);
                 sNewPreffix = ConstRegEx.newPrefix(iPrevGroupNumber);
                 
                 bRes = pAnnotator.comPrefSuf.capture_WS(sParagraph, 
                         sNewPreffix, sNewSuffix, iPrevGroupNumber, bMayLeave);
                 if (bRes) {
                     sParagraph = tmpMatch.saveMatchAndMoveOverAndReset(sParagraph);
                     sNewPreffix = ConstRegEx.newPrefix(iPrevGroupNumber);

                     bRes = capture_DATE_UNIT(sParagraph,
                             sNewPreffix, sRegExSuffix, iPrevGroupNumber, bMayLeave);
                     if (bRes) {
                         try {
                             Variables().tempVariables.stempSingularDate =
                                     "1" +
                                     Variables().tempVariables.stempPart +
                                     Variables().tempVariables.stempDU;

                             tmpMatch.restoreCurrentMatch();
                         } catch (Exception e) {
                             if (!bMayLeave) {System.out.println(e.toString());}
                         }
                     }
                 }
             }
         }
         return Variables().prepareReturn(bRes);
    }
    
    public boolean capture_NUM_SINGULAR_DATE(String sParagraph,String sRegExPrefix, String sRegExSuffix, 
             int[] iPrevGroupNumber,boolean bMayLeave){
/*
$NUM_SINGULAR_DATE		= 
 qr/(?{$tempPart = '';$tempSingularDate = '';})
 ($AANONE?$WS?$DATE_UNIT($WS$AND_PARTS)?
 (?{$tempSingularDate = "1$tempPart$tempDU";})
 
 |$AANONE$WS$AND_PARTS$WS($PLURAL_DATE_UNIT_LEX|$DATE_UNIT)
 (?{$tempSingularDate="1$tempPart$tempDU"}))/i;
#"one year, a year and a half, one and a half years" 
 */  
        Variables().startCapture();
        Variables().tempVariables.stempPart = "";
        Variables().tempVariables.stempSingularDate = "";
        
        TempResultVariables tempRes = new TempResultVariables(Variables());
        tempRes.setInitialValues(iPrevGroupNumber);
                
        if (capture_NUM_SINGULAR_DATE_Case1(sParagraph, 
                sRegExPrefix, sRegExSuffix, iPrevGroupNumber, bMayLeave)){
             tempRes.setBestResult( iPrevGroupNumber[0]);    
             return Variables().prepareReturn(true);
        }
        
        tempRes.resetInitialValues(iPrevGroupNumber);
        if (capture_NUM_SINGULAR_DATE_Case2(sParagraph, 
                    sRegExPrefix, sRegExSuffix, iPrevGroupNumber, bMayLeave)){
             tempRes.setBestResult( iPrevGroupNumber[0]);    
             return Variables().prepareReturn(true);
        }
        
        tempRes.resetInitialValues(iPrevGroupNumber);
        if ( capture_NUM_SINGULAR_DATE_Case3(sParagraph, 
                        sRegExPrefix, sRegExSuffix, iPrevGroupNumber, bMayLeave) ){
            tempRes.setBestResult( iPrevGroupNumber[0]);    
            return Variables().prepareReturn(true);
        }
        tempRes.prepareReturn(iPrevGroupNumber);
        return Variables().prepareReturn(tempRes.bAtLeastOneResult);
    }
    public boolean capture_RECURSIVE_DATE(String sParagraph,String sRegExPrefix, String sRegExSuffix, 
             int[] iPrevGroupNumber,boolean bMayLeave){
/*
$RECURSIVE_DATE 		= qr/
 (?{local $tRD2='';})
 ($NUM_PLURAL_DATE(?{$tRD1=$tempNumPluralDate;})|
 $NUM_SINGULAR_DATE(?{$tRD1=$tempSingularDate;}))
 (($WS($AND|$PUNCT)?$WS?)($NUM_PLURAL_DATE(?{local $tRD2=$tRD2.$tempNumPluralDate;})|$NUM_SINGULAR_DATE(?{local $tRD2=$tRD2.$tempSingularDate;})))*(?{$tempRecursiveDate=$tRD1.$tRD2})/;
				#"three weeks and two days"
  */            
        Variables().startCapture();
        String[] stRD2 =new String[1]; stRD2[0] = "";
        //String sNewSuffix =  ConstRegEx.REST_ANY + sRegExSuffix;//2008.10.22
       
        TempResultVariables tempRes = new TempResultVariables(Variables());
        tempRes.setInitialValues(iPrevGroupNumber);
        
        @SuppressWarnings("unused")
        boolean bPluralDate = false;
        boolean[] bSingularDate = new boolean[1];
        bSingularDate[0] = false;
        
        if (capture_NUM_PLURAL_DATE(sParagraph,
                sRegExPrefix, sRegExSuffix, iPrevGroupNumber, bMayLeave)){
            bPluralDate = tempRes.setBestResult( iPrevGroupNumber[0]);    
        }
        
        if (!tempRes.bAtLeastOneResult) {
            tempRes.resetInitialValues(iPrevGroupNumber);
            if (capture_NUM_SINGULAR_DATE(sParagraph, 
                    sRegExPrefix, sRegExSuffix, iPrevGroupNumber, bMayLeave)) {
                bSingularDate[0] = tempRes.setBestResult(iPrevGroupNumber[0]);
            }
        }
        tempRes.prepareReturn(iPrevGroupNumber);
        if (tempRes.bAtLeastOneResult) {
            try {
                if (bSingularDate[0]) {
                    Variables().tempVariables.stRD1 =
                            Variables().tempVariables.stempSingularDate;
                } else {
                    Variables().tempVariables.stRD1 =
                            Variables().tempVariables.stempNumPluralDate;
                }
                TempSaveCurrentMatch tmpMatch = new TempSaveCurrentMatch(Variables());
                sParagraph = tmpMatch.saveMatchAndMoveOverAndReset(sParagraph);
                String sNewPrefix = ConstRegEx.newPrefix(iPrevGroupNumber);
                
                boolean bRes = pAnnotator.capture_Recursive_Date_Suffix(sParagraph, 
                        sNewPrefix, sRegExSuffix, iPrevGroupNumber, bMayLeave
                        , bSingularDate, stRD2);
                if (bRes) 
                {

                        try {
                            
// (?{$tempRecursiveTime=$tRT1.$tRT2;})/;                      
                            Variables().tempVariables.stempRecursiveDate =
                                Variables().tempVariables.stRD1 + stRD2[0];
                            
                            tmpMatch.restoreCurrentMatch();
                        } catch (Exception e) {
                            if (!bMayLeave) {System.out.println(e.toString());}
                        }
                }
                return Variables().prepareReturn(bRes);
        } catch (Exception e) {
                if (!bMayLeave) {System.out.println(e.toString());}
        }
       }
       return Variables().prepareReturn(tempRes.bAtLeastOneResult);
    }
    
   public boolean capture_DURATION_DATE_Suffix1(String sParagraph,
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
    public boolean capture_DURATION_DATE(String sParagraph,String sRegExPrefix, 
            String sRegExSuffix, int[] iPrevGroupNumber,boolean bMayLeave){
/*
$DURATION_DATE			= qr/
 (?{$tempMod = '';})(
 (($DUR_MOD)(?{$tempMod = $map_val{lc($^N)."_DUR_MOD"};}))$WS )?
 ($RECURSIVE_DATE(?{$crtTimePoint->val($tempRecursiveDate);})|
 $PARTS_DATE_UNIT)
 $WS?$LONG?(?{$crtTimePoint->mod($tempMod);})
 ($WS$POST_DUR_MOD)?/i;
#"more than two weeks"
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
            
            sNewSuffix = ConstRegEx.REST_ANY +sRegExSuffix;
            
            TempResultVariables tempRes = new TempResultVariables(Variables());
            tempRes.setInitialValues(iPrevGroupNumber);
            
            
            @SuppressWarnings("unused")
            boolean bRecTime = false;
            @SuppressWarnings("unused")
            boolean bPartsTime = false;
            if (capture_RECURSIVE_DATE(sParagraph,
                    sNewPrefix, sRegExSuffix, iPrevGroupNumber, bMayLeave)) {
                Variables().tempVariables.crtTimePoint.set("val",
                        Variables().tempVariables.stempRecursiveDate);
                bRecTime = tempRes.setBestResult(iPrevGroupNumber[0]);
            }

            if (!tempRes.bAtLeastOneResult) {
                tempRes.resetInitialValues(iPrevGroupNumber);
                if (capture_PARTS_DATE_UNIT(sParagraph,
                        sNewPrefix, sRegExSuffix, iPrevGroupNumber, bMayLeave)) {
                    bPartsTime = tempRes.setBestResult(iPrevGroupNumber[0]);
                }
            }
            tempRes.prepareReturn(iPrevGroupNumber);
            if (tempRes.bAtLeastOneResult) {
                try {
                    sParagraph = tmpMatch.saveMatchAndMoveOverAndReset(sParagraph);
                    sNewPrefix = ConstRegEx.newPrefix(iPrevGroupNumber);
                    
                    bRes = capture_DURATION_DATE_Suffix1(sParagraph, 
                            sNewPrefix, sRegExSuffix, iPrevGroupNumber, bMayLeave);
                    
                    Variables().tempVariables.crtTimePoint.set("mod", 
                            Variables().tempVariables.stempMod);
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
    
//#==================== FIXED PHRASES ===================== 
    public boolean capture_PAST_PRES_FUTURE(String sParagraph,
            String sRegExPrefix, String sRegExSuffix, 
             int[] iPrevGroupNumber,boolean bMayLeave) {
/*
$PAST_PRES_FUTURE		
= qr/(($THE|$AANONE)$WS)?(($PPF)(?{$crtTimePoint->val($map_val{lc($^N)."_PPF"});}))/i;
				#now, nowadays, past etc.
 */       
        Variables().startCapture();
        int iRes = iPrevGroupNumber[0] + 1;
        String sPattern = ConstRegEx.groupBrackets(
                                ConstRegEx.groupBrackets(
                                ConstRegEx.groupBrackets(
                                ConstRegEx.groupBrackets( Variables().hashtbLexValues.get("THE"), iPrevGroupNumber)+"|"+
                                ConstRegEx.groupBrackets( Variables().hashtbLexValues.get("AANONE"), iPrevGroupNumber)
                                    , iPrevGroupNumber) +
                                ConstRegEx.groupBrackets(ConstRegEx.WS, iPrevGroupNumber)
                                    , iPrevGroupNumber) + "?"+
                                ConstRegEx.groupBrackets( Variables().hashtbLexValues.get("PPF"), iPrevGroupNumber)
                            , iPrevGroupNumber);
        
        Pattern patternLex = Pattern.compile(
                ConstRegEx.prefixWordSuffix(sPattern, sRegExPrefix, sRegExSuffix),
                Pattern.CASE_INSENSITIVE);
        boolean bResult =
                Find().printAllMatchResult(patternLex, sParagraph, false);
        if (bResult){
            try {
                String sCapStr = 
                        Variables().getResult_LowerCase(iPrevGroupNumber[0])+
                        "_PPF";
                Variables().tempVariables.crtTimePoint.set("val",
                        Variables().getMapHashValue(sCapStr));

                Variables().saveCurrentMatch(iRes);
            } catch(Exception e){
                if (!bMayLeave){ Const.writeSpecificError(e, sParagraph);}
                Variables().tempVariables.sCurrentMatch = "";
            }   
        }
        return Variables().prepareReturn(bResult);
    }
//#==================== NOT VERY DEFINITE TIMES ===========
    public boolean capture_NOWS(String sParagraph,
            String sRegExPrefix, String sRegExSuffix, 
             int[] iPrevGroupNumber,boolean bMayLeave) {
/*
$NOWS= qr/($NOW)(?{$crtTimePoint->val($map_val{lc($^N)});})(?!$WS$THAT)/i;
				#"now", but not "now that"
 */     
        Variables().startCapture();
        int iNow = iPrevGroupNumber[0] + 2;
        int iRes = iPrevGroupNumber[0] + 1;
        String sPattern = ConstRegEx.groupBrackets(
                ConstRegEx.groupBrackets(
                    Variables().hashtbLexValues.get("NOW"), iPrevGroupNumber) +
                    ConstRegEx.groupBrackets("?!"+
                        ConstRegEx.groupBrackets(ConstRegEx.WS, iPrevGroupNumber)+
                        ConstRegEx.groupBrackets(Variables().hashtbLexValues.get("THAT"), iPrevGroupNumber)
                    , iPrevGroupNumber)
                , iPrevGroupNumber);
        
        Pattern patternLex = Pattern.compile(
                ConstRegEx.prefixWordSuffix(sPattern, sRegExPrefix, sRegExSuffix),
                Pattern.CASE_INSENSITIVE);
        boolean bResult =
                Find().printAllMatchResult(patternLex, sParagraph, false);
        if (bResult){
            try {
                String sCapStr = Variables().getResult_LowerCase(iNow);
                Variables().tempVariables.crtTimePoint.set("val",
                        Variables().getMapHashValue(sCapStr));
                Variables().saveCurrentMatch(iRes);
            } catch(Exception e){
                if (!bMayLeave){ Const.writeSpecificError(e, sParagraph);}
                Variables().tempVariables.sCurrentMatch = "";
            }   
        }
        return Variables().prepareReturn(bResult);
    }
//#==================== RANGE ===========
    public boolean capture_RANGE_DATE_DATE_DATE_MONTH(String sParagraph,
            String sRegExPrefix, String sRegExSuffix, 
             int[] iPrevGroupNumber,boolean bMayLeave) {
/*
$RANGE_DATE_DATE_DATE_MONTH = (THEWS)?$DATE \, $WS (THEWS)?$DATE $WS AND|OR|TO $WS $DATE_MONTH
 * eq. fifth and sixth of June
 */        
        Variables().startCapture();
        String sNewSuf = ConstRegEx.REST_ANY +sRegExSuffix;
        boolean bRes = pAnnotator.comPrefSuf.capture_THE_WS_op(sParagraph, 
                sRegExPrefix, sNewSuf, iPrevGroupNumber, bMayLeave);
        if (bRes){
            TempSaveCurrentMatch tmpMatch = new TempSaveCurrentMatch(Variables());
            sParagraph = tmpMatch.saveMatchAndMoveOverAndReset(sParagraph);
            String sNewPref = ConstRegEx.newPrefix(iPrevGroupNumber);
            
            bRes = capture_DATE(sParagraph, sNewPref, sNewSuf, 
                    iPrevGroupNumber, bMayLeave);
            if (bRes){
                sParagraph = tmpMatch.saveMatchAndMoveOverAndReset(sParagraph);
                sNewPref = ConstRegEx.newPrefix(iPrevGroupNumber);
                
                String sDate = tmpMatch.sMatch;
                
                bRes = pAnnotator.comPrefSuf.capture_RANGE_Begin_WS(sParagraph, 
                        sNewPref, sNewSuf, iPrevGroupNumber, bMayLeave);
                if (bRes){
                    TimePoint tpRange = new TimePoint();
                    //2008.11.30
                    tpRange.copy(
                            Variables().tempVariables.crtTimePoint
                            );
                    tpRange.set("dayOfMonth",
                            Variables().tempVariables.stempD);
                    tpRange.set("range_Text",
                            Variables().tempVariables.sCurrentMatch);
                    tpRange.set("text",
                            sDate);                    
                    Variables().tempVariables.rangeTimePoint.add(tpRange);
                    sParagraph = tmpMatch.saveMatchAndMoveOverAndReset(sParagraph);
                    sNewPref = ConstRegEx.newPrefix(iPrevGroupNumber);
                    
                    bRes = capture_RANGE_DATE_DATE_MONTH(sParagraph, 
                       sNewPref, sRegExSuffix, iPrevGroupNumber, bMayLeave);
                    if (bRes) {
                        try {
/*                            Variables().tempVariables.crtTimePoint.set("text",
                                    Variables().tempVariables.sCurrentMatch);*/
                            tmpMatch.restoreCurrentMatch();
                        } catch (Exception e) {
                            if (!bMayLeave) {Const.writeSpecificError(e, sParagraph);}
                        }
                    }
                       
                }
            }
        }
        return Variables().prepareReturn(bRes);
    }
    
    public boolean capture_RANGE_DATE_DATE_MONTH(String sParagraph,
            String sRegExPrefix, String sRegExSuffix, 
             int[] iPrevGroupNumber,boolean bMayLeave) {
/*
$RANGE_DATE_DATE_MONTH = (THEWS)?$DATE $WS AND|OR|TO $WS $DATE_MONTH
 * eq. fifth and sixth of June
 */        
        Variables().startCapture();
        String sNewSuf = ConstRegEx.REST_ANY +sRegExSuffix;
        boolean bRes = pAnnotator.comPrefSuf.capture_THE_WS_op(sParagraph, 
                sRegExPrefix, sNewSuf, iPrevGroupNumber, bMayLeave);
        if (bRes){
            TempSaveCurrentMatch tmpMatch = new TempSaveCurrentMatch(Variables());
            sParagraph = tmpMatch.saveMatchAndMoveOverAndReset(sParagraph);
            String sNewPref = ConstRegEx.newPrefix(iPrevGroupNumber);
            
            bRes = capture_DATE(sParagraph, sNewPref, sNewSuf, 
                    iPrevGroupNumber, bMayLeave);
            if (bRes){
                sParagraph = tmpMatch.saveMatchAndMoveOverAndReset(sParagraph);
                sNewPref = ConstRegEx.newPrefix(iPrevGroupNumber);
                
                String sDate = tmpMatch.sMatch;
                
                bRes = pAnnotator.comPrefSuf.capture_WS_RANGE_WS_or_Comma_WS(sParagraph, 
                        sNewPref, sNewSuf, iPrevGroupNumber, bMayLeave);
                if (bRes){
                    //2008.11.30
                    TimePoint tpRange = new TimePoint();
                    tpRange.copy(
                            Variables().tempVariables.crtTimePoint
                            );
                    tpRange.set("dayOfMonth",
                            Variables().tempVariables.stempD);
                    tpRange.set("range_Text",
                            Variables().tempVariables.sCurrentMatch);
                    tpRange.set("text",
                            sDate);                    
                    Variables().tempVariables.rangeTimePoint.add(tpRange);
                    sParagraph = tmpMatch.saveMatchAndMoveOverAndReset(sParagraph);
                    sNewPref = ConstRegEx.newPrefix(iPrevGroupNumber);
                    
                    bRes = capture_DATE_MONTH(sParagraph, 
                       sNewPref, sRegExSuffix, iPrevGroupNumber, bMayLeave);
                    if (bRes) {
                        try {
                            Variables().tempVariables.crtTimePoint.set("text",
                                    Variables().tempVariables.sCurrentMatch);
                            tmpMatch.restoreCurrentMatch();
                        } catch (Exception e) {
                            if (!bMayLeave) {Const.writeSpecificError(e, sParagraph);}
                        }
                    }
                       
                }
            }
        }
        return Variables().prepareReturn(bRes);
    }

    public boolean capture_RANGE_DATE_DATE_DMY(String sParagraph, String sRegExPrefix, String sRegExSuffix,
            int[] iPrevGroupNumber, boolean bMayLeave) {
        /*
        $RANGE_DATE_DMY = $DATE , $WS $DATE $WS AND|OR|TO $WS $DMY
         * eq. fifth and sixth of June
         */
        Variables().startCapture();
        String sNewSuf = ConstRegEx.REST_ANY + sRegExSuffix;

        boolean bRes = pAnnotator.comPrefSuf.capture_THE_WS_op(sParagraph,
                sRegExPrefix, sNewSuf, iPrevGroupNumber, bMayLeave);

        if (bRes) {
            try {
                TempSaveCurrentMatch tmpMatch = new TempSaveCurrentMatch(Variables());
                sParagraph = tmpMatch.saveMatchAndMoveOverAndReset(sParagraph);
                String sNewPref = ConstRegEx.newPrefix(iPrevGroupNumber);

                bRes = capture_DATE(sParagraph,
                        sNewPref, sNewSuf, iPrevGroupNumber, bMayLeave);
                if (bRes) {
                    sParagraph = tmpMatch.saveMatchAndMoveOverAndReset(sParagraph);
                    sNewPref = ConstRegEx.newPrefix(iPrevGroupNumber);
                    String sDate = tmpMatch.sMatch;
                    bRes = pAnnotator.comPrefSuf.capture_RANGE_Begin_WS(sParagraph,
                            sNewPref, sNewSuf, iPrevGroupNumber, bMayLeave);
                    if (bRes) {
                        //2008.11.30
                        TimePoint tpRange = new TimePoint();
                        tpRange.copy(
                                Variables().tempVariables.crtTimePoint);
                        tpRange.set("dayOfMonth",
                                Variables().tempVariables.stempD);
                        tpRange.set("range_Text",
                                Variables().tempVariables.sCurrentMatch);
                        tpRange.set("text",
                                sDate);
                        Variables().tempVariables.rangeTimePoint.add(tpRange);
                        sParagraph = tmpMatch.saveMatchAndMoveOverAndReset(sParagraph);
                        sNewPref = ConstRegEx.newPrefix(iPrevGroupNumber);

                        bRes = capture_RANGE_DATE_DMY(sParagraph,
                                sNewPref, sRegExSuffix, iPrevGroupNumber, bMayLeave);
                        if (bRes) {
                            tmpMatch.restoreCurrentMatch();
                        }
                    }
                }
                return Variables().prepareReturn(bRes);
            } catch (Exception e) {
                if (!bMayLeave) {
                    Const.writeSpecificError(e, sParagraph);
                }
            }
        }
        return Variables().prepareReturn(bRes);

    }

    public boolean capture_RANGE_DATE_DMY(String sParagraph, String sRegExPrefix, String sRegExSuffix,
            int[] iPrevGroupNumber, boolean bMayLeave) {
        /*
        $RANGE_DATE_DMY = $DATE $WS AND|OR|TO $WS $DMY
         * eq. fifth and sixth of June
         */
        Variables().startCapture();
        String sNewSuf = ConstRegEx.REST_ANY + sRegExSuffix;

        boolean bRes = pAnnotator.comPrefSuf.capture_THE_WS_op(sParagraph,
                sRegExPrefix, sNewSuf, iPrevGroupNumber, bMayLeave);

        if (bRes) {
            try {
                TempSaveCurrentMatch tmpMatch = new TempSaveCurrentMatch(Variables());
                sParagraph = tmpMatch.saveMatchAndMoveOverAndReset(sParagraph);
                String sNewPref = ConstRegEx.newPrefix(iPrevGroupNumber);
                
                bRes = capture_DATE(sParagraph,
                        sNewPref, sNewSuf, iPrevGroupNumber, bMayLeave);
                if (bRes) {
                    sParagraph = tmpMatch.saveMatchAndMoveOverAndReset(sParagraph);
                    sNewPref = ConstRegEx.newPrefix(iPrevGroupNumber);
                    String sDate = tmpMatch.sMatch;
                    bRes = pAnnotator.comPrefSuf.capture_WS_RANGE_WS_or_Comma_WS(sParagraph,
                            sNewPref, sNewSuf, iPrevGroupNumber, bMayLeave);
                    if (bRes) {
                        //2008.11.30
                        TimePoint tpRange = new TimePoint();
                        tpRange.copy(
                                Variables().tempVariables.crtTimePoint);
                        tpRange.set("dayOfMonth",
                                Variables().tempVariables.stempD);
                        tpRange.set("range_Text",
                                Variables().tempVariables.sCurrentMatch);
                        tpRange.set("text",
                                sDate);
                        Variables().tempVariables.rangeTimePoint.add(tpRange);       
                        sParagraph = tmpMatch.saveMatchAndMoveOverAndReset(sParagraph);
                        sNewPref = ConstRegEx.newPrefix(iPrevGroupNumber);

                        bRes = capture_DMY(sParagraph,
                                sNewPref, sRegExSuffix, iPrevGroupNumber, bMayLeave);
                        if (bRes) {
                            Variables().tempVariables.crtTimePoint.set("text",
                                    Variables().tempVariables.sCurrentMatch);
                            tmpMatch.restoreCurrentMatch();
                        }
                    }
                }
                return Variables().prepareReturn(bRes);
            } catch (Exception e) {
                if (!bMayLeave) {
                    Const.writeSpecificError(e, sParagraph);
                }
            }
        }
        return Variables().prepareReturn(bRes);

    }    
    
    
    public boolean capture_DATE_ALL(String sParagraph,
            String sRegExPrefix, String sRegExSuffix, 
             int[] iPrevGroupNumber,boolean bMayLeave) {
/*
 $DATE_ALL			= 
 //qr/(?{$crtTimePoint->clear();})$DECADE|
 (?{$crtTimePoint->clear();})$D_M_Y|
 (?{$crtTimePoint->clear();})$DOUBLE_DATE1|
 (?{$crtTimePoint->clear();})$DOUBLE_DATE2|
 (?{$crtTimePoint->clear();})$DOUBLE_DATE3|
 (?{$crtTimePoint->clear();})$ANNIVERSARY|
 (?{$crtTimePoint->clear();})$PART_OF_DATE_UNIT|
 (?{$crtTimePoint->clear();})$BEGINNING_END|
 (?{$crtTimePoint->clear();})$PAST_SING_DATE|//VA
 (?{$crtTimePoint->clear();})$LAST_PLURAL_DATE_CONTEXT|
 (?{$crtTimePoint->clear();})$UNDEF_PLURAL_DATE|
 (?{$crtTimePoint->clear();})$SIMPLE_DEICTIC|//VA
 (?{$crtTimePoint->clear();})$ORD_PLURAL_DATE|
 (?{$crtTimePoint->clear();})$AGO_BEFORE_DATE|
 (?{$crtTimePoint->clear();})$THAT_DEICTIC|
 (?{$crtTimePoint->clear();})$LATE_DAY_DAY_PART|
 (?{$crtTimePoint->clear();})$LATE_EARLY|
 (?{$crtTimePoint->clear();})$LATE_ON_DAY|
 (?{$crtTimePoint->clear();})$LATE_IN_DATE|
 (?{$crtTimePoint->clear();})$BEGINNING_END_CENTURY|
 (?{$crtTimePoint->clear();})$ORD_CENTURY|
 (?{$crtTimePoint->clear();})$DATE_NAME_OF_DATE|
 (?{$crtTimePoint->clear();})$LAST_DAY|
 (?{$crtTimePoint->clear();})$QUARTS_YEAR|
 (?{$crtTimePoint->clear();})$THE_QUART|
 (?{$crtTimePoint->clear();})$QUARTS|
 * NEW VARIABLE
 * * *capture_RANGE_DATE_DATE_DMY //2008.12.06
 * *capture_RANGE_DATE_DATE_DATE_MONTH //2008.12.06
 * * capture_RANGE_RANGE_DATE_DMY
 * * (?{$crtTimePoint->clear();})$RANGE_DATE_DMY|//VA
 * capture_RANGE_DATE_DATE_MONTH
 * (?{$crtTimePoint->clear();})$RANGE_DATE_DATE_MONTH|//VA
 (?{$crtTimePoint->clear();})$DAY_DMY|//VA
 (?{$crtTimePoint->clear();})$DAY_DM|//VA
 (?{$crtTimePoint->clear();})$DMY|//VA
 (?{$crtTimePoint->clear();})$DM|//VA
 (?{$crtTimePoint->clear();})$SPECIAL_DATE_YEAR|
 (?{$crtTimePoint->clear();})$DATE_MONTH|//VA
 (?{$crtTimePoint->clear();})$MONTH_DATE|//VA
 (?{$crtTimePoint->clear();})$MONTH_YEAR|
 (?{$crtTimePoint->clear();})$DAY_NAME_FULL_LEX| //VA
 (?{$crtTimePoint->clear();})$STANDALONE_MONTHS1| //VA
 (?{$crtTimePoint->clear();})$STANDALONE_MONTHS2| //VA
 (?{$crtTimePoint->clear();})$SPECIAL_DATE_NAME| //VA
 (?{$crtTimePoint->clear();})$TODAY_DEICTIC| //VA
 (?{$crtTimePoint->clear();})$WEEKEND| //VA
 (?{$crtTimePoint->clear();})$STANDALONE_SEASONS| //VA
 (?{$crtTimePoint->clear();})$YEAR2000| //VA
 (?{$crtTimePoint->clear();})$YEAR_AD_BC| //VA
 (?{$crtTimePoint->clear();})$COPYRIGHT_YEAR| //VA
 (?{$crtTimePoint->clear();})$FY_YEAR| //VA
 (?{$crtTimePoint->clear();})$YEAR_STAND_ALONE| //VA
 (?{$crtTimePoint->clear();})$DURATION_DATE|//VA - TO VERIFY
 (?{$crtTimePoint->clear();})$NOWS| //VA
 (?{$crtTimePoint->clear();})$PAST_PRES_FUTURE| //VA
 (?{$crtTimePoint->clear();})$PLURAL_DATES/;//VA
 */      
        Variables().startCapture();
        TempResultVariables tempRes = new TempResultVariables(Variables());
        Variables().tempVariables.crtTimePoint.clear();
        Variables().tempVariables.rangeTimePoint.clear();
        tempRes.setInitialValues(iPrevGroupNumber);

        if (capture_PAST_SING_DATE(sParagraph, sRegExPrefix, sRegExSuffix, iPrevGroupNumber, bMayLeave)) {
            this.pAnnotator.writeToLogFile("PAST_SING_DATE\n");
            tempRes.setBestResult(iPrevGroupNumber[0]);
            return Variables().prepareReturn(true);
        }
        
        tempRes.resetInitialValues(iPrevGroupNumber);        
        if (capture_SIMPLE_DEICTIC(sParagraph, sRegExPrefix, sRegExSuffix, iPrevGroupNumber, bMayLeave)) {
            this.pAnnotator.writeToLogFile("SIMPLE_DEICTIC\n");
            tempRes.setBestResult(iPrevGroupNumber[0]);
            return Variables().prepareReturn(true);
        }

        tempRes.resetInitialValues(iPrevGroupNumber);        
        
        if (capture_RANGE_DATE_DATE_DMY(sParagraph, sRegExPrefix, sRegExSuffix, iPrevGroupNumber, bMayLeave)){
            this.pAnnotator.writeToLogFile("RANGE_DATE_DATE_DMY\n");
            tempRes.setBestResult( iPrevGroupNumber[0]);       
            return Variables().prepareReturn(true);
        }
        tempRes.resetInitialValues(iPrevGroupNumber);        
        if (capture_RANGE_DATE_DATE_DATE_MONTH(sParagraph, sRegExPrefix, sRegExSuffix, iPrevGroupNumber, bMayLeave)){
            this.pAnnotator.writeToLogFile("RANGE_DATE_DATE_DATE_MONTH\n");
            tempRes.setBestResult( iPrevGroupNumber[0]);       
            return Variables().prepareReturn(true);
        }
                
        tempRes.resetInitialValues(iPrevGroupNumber);        
        if (capture_RANGE_DATE_DMY(sParagraph, sRegExPrefix, sRegExSuffix, iPrevGroupNumber, bMayLeave)){
            this.pAnnotator.writeToLogFile("RANGE_DATE_DMY\n");
            tempRes.setBestResult( iPrevGroupNumber[0]);       
            return Variables().prepareReturn(true);
        }
        
        tempRes.resetInitialValues(iPrevGroupNumber);        
        if (capture_RANGE_DATE_DATE_MONTH(sParagraph, sRegExPrefix, sRegExSuffix, iPrevGroupNumber, bMayLeave)){
            this.pAnnotator.writeToLogFile("RANGE_DATE_DATE_MONTH\n");
            tempRes.setBestResult( iPrevGroupNumber[0]);       
            return Variables().prepareReturn(true);
        }
                
        tempRes.resetInitialValues(iPrevGroupNumber);        
        if (capture_DAY_DMY(sParagraph, sRegExPrefix, sRegExSuffix, iPrevGroupNumber, bMayLeave)){
            this.pAnnotator.writeToLogFile("DAY_DMY\n");
            tempRes.setBestResult( iPrevGroupNumber[0]);       
            return Variables().prepareReturn(true);
        }
        
        tempRes.resetInitialValues(iPrevGroupNumber);
        if (capture_DAY_DM(sParagraph, sRegExPrefix, sRegExSuffix, iPrevGroupNumber, bMayLeave)) {
            this.pAnnotator.writeToLogFile("DAY_DM\n");
            tempRes.setBestResult(iPrevGroupNumber[0]);
            return Variables().prepareReturn(true);
        }
        
        tempRes.resetInitialValues(iPrevGroupNumber);
        if (capture_DMY(sParagraph, sRegExPrefix, sRegExSuffix, iPrevGroupNumber, bMayLeave)){
            this.pAnnotator.writeToLogFile("DMY\n");
            tempRes.setBestResult( iPrevGroupNumber[0]);       
            return Variables().prepareReturn(true);
        }
        
        tempRes.resetInitialValues(iPrevGroupNumber);
        if (capture_DM(sParagraph, sRegExPrefix, sRegExSuffix, iPrevGroupNumber, bMayLeave)){
            this.pAnnotator.writeToLogFile("DM\n");
            tempRes.setBestResult( iPrevGroupNumber[0]);       
            return Variables().prepareReturn(true);
        }
        
        tempRes.resetInitialValues(iPrevGroupNumber);
        if (capture_DATE_MONTH(sParagraph, sRegExPrefix, sRegExSuffix, iPrevGroupNumber, bMayLeave)){
            this.pAnnotator.writeToLogFile("DATE_MONTH\n");
            tempRes.setBestResult( iPrevGroupNumber[0]);       
            return Variables().prepareReturn(true);
        }
        tempRes.resetInitialValues(iPrevGroupNumber);
        if (capture_MONTH_DATE(sParagraph, sRegExPrefix, sRegExSuffix, iPrevGroupNumber, bMayLeave)){
            this.pAnnotator.writeToLogFile("MONTH_DATE\n");
            tempRes.setBestResult( iPrevGroupNumber[0]);       
            return Variables().prepareReturn(true);
        }        
        tempRes.resetInitialValues(iPrevGroupNumber);
        if (capture_DAY_NAME_FULL_LEX(sParagraph, sRegExPrefix, sRegExSuffix, iPrevGroupNumber, bMayLeave)){
            this.pAnnotator.writeToLogFile("DAY_NAME_FULL_LEX\n");
            tempRes.setBestResult( iPrevGroupNumber[0]);       
            return Variables().prepareReturn(true);
        }
        tempRes.resetInitialValues(iPrevGroupNumber);
        if (capture_STANDALONE_MONTHS1(sParagraph, sRegExPrefix, sRegExSuffix, iPrevGroupNumber, bMayLeave)){
            this.pAnnotator.writeToLogFile("STANDALONE_MONTHS1\n");
            tempRes.setBestResult( iPrevGroupNumber[0]);       
            return Variables().prepareReturn(true);
        }
        tempRes.resetInitialValues(iPrevGroupNumber);
        if (capture_STANDALONE_MONTHS2(sParagraph, sRegExPrefix, sRegExSuffix, iPrevGroupNumber, bMayLeave)){
            this.pAnnotator.writeToLogFile("STANDALONE_MONTHS2\n");
            tempRes.setBestResult( iPrevGroupNumber[0]);       
            return Variables().prepareReturn(true);
        }
        tempRes.resetInitialValues(iPrevGroupNumber);
        if (capture_SPECIAL_DATE_NAME(sParagraph, sRegExPrefix, sRegExSuffix, iPrevGroupNumber, bMayLeave)){
            this.pAnnotator.writeToLogFile("SPECIAL_DATE_NAME\n");
            tempRes.setBestResult( iPrevGroupNumber[0]);       
            return Variables().prepareReturn(true);
        }
        tempRes.resetInitialValues(iPrevGroupNumber);
        if (capture_TODAY_DEICTIC(sParagraph, sRegExPrefix, sRegExSuffix, iPrevGroupNumber, bMayLeave)){
            this.pAnnotator.writeToLogFile("TODAY_DEICITIC\n");
            tempRes.setBestResult( iPrevGroupNumber[0]);       
            return Variables().prepareReturn(true);
        }
        tempRes.resetInitialValues(iPrevGroupNumber);
        if (capture_WEEKEND(sParagraph, sRegExPrefix, sRegExSuffix, iPrevGroupNumber, bMayLeave)){
            this.pAnnotator.writeToLogFile("WEEKEND\n");
            tempRes.setBestResult( iPrevGroupNumber[0]);       
            return Variables().prepareReturn(true);
        }
        tempRes.resetInitialValues(iPrevGroupNumber);
        if (capture_STANDALONE_SEASONS(sParagraph, sRegExPrefix, sRegExSuffix, iPrevGroupNumber, bMayLeave)){
            this.pAnnotator.writeToLogFile("STANDALONE_SEASONS\n");
            tempRes.setBestResult( iPrevGroupNumber[0]);       
            return Variables().prepareReturn(true);
        }
        tempRes.resetInitialValues(iPrevGroupNumber);
        if (capture_YEAR2000(sParagraph, sRegExPrefix, sRegExSuffix, iPrevGroupNumber, bMayLeave)){
            this.pAnnotator.writeToLogFile("YEAR_2000\n");
            tempRes.setBestResult( iPrevGroupNumber[0]);       
            return Variables().prepareReturn(true);
        }
        tempRes.resetInitialValues(iPrevGroupNumber);
        if (capture_YEAR_AD_BC(sParagraph, sRegExPrefix, sRegExSuffix, iPrevGroupNumber, bMayLeave)){
            this.pAnnotator.writeToLogFile("YEAR_AD_BC\n");
            tempRes.setBestResult( iPrevGroupNumber[0]);       
            return Variables().prepareReturn(true);
        }
        tempRes.resetInitialValues(iPrevGroupNumber);
        if (capture_COPYRIGHT_YEAR(sParagraph, sRegExPrefix, sRegExSuffix, iPrevGroupNumber, bMayLeave)){
            this.pAnnotator.writeToLogFile("COPYRIGHT_YEAR\n");
            tempRes.setBestResult( iPrevGroupNumber[0]);       
            return Variables().prepareReturn(true);
        }
        tempRes.resetInitialValues(iPrevGroupNumber);
        if (capture_FY_YEAR(sParagraph, sRegExPrefix, sRegExSuffix, iPrevGroupNumber, bMayLeave)){
            this.pAnnotator.writeToLogFile("FY_YEAR\n");
            tempRes.setBestResult( iPrevGroupNumber[0]);       
            return Variables().prepareReturn(true);
        }
        tempRes.resetInitialValues(iPrevGroupNumber);
        if (capure_YEAR_STAND_ALONE(sParagraph, sRegExPrefix, sRegExSuffix, iPrevGroupNumber, bMayLeave)){
            this.pAnnotator.writeToLogFile("YEAR_STAND_ALONE\n");            
            tempRes.setBestResult( iPrevGroupNumber[0]);       
            return Variables().prepareReturn(true);
        }
        /*
         * doesn't work well*/
        tempRes.resetInitialValues(iPrevGroupNumber);
        if (capture_DURATION_DATE(sParagraph, sRegExPrefix, sRegExSuffix, iPrevGroupNumber, bMayLeave)) {
            this.pAnnotator.writeToLogFile("DURATION_DATE\n");            
            tempRes.setBestResult( iPrevGroupNumber[0]);       
            return Variables().prepareReturn(true);
        }/**/
        tempRes.resetInitialValues(iPrevGroupNumber);
        if (capture_NOWS(sParagraph, sRegExPrefix, sRegExSuffix, iPrevGroupNumber, bMayLeave)){
            this.pAnnotator.writeToLogFile("NOWS\n");
            tempRes.setBestResult( iPrevGroupNumber[0]);       
            return Variables().prepareReturn(true);
        }
        tempRes.resetInitialValues(iPrevGroupNumber);
        if (capture_PAST_PRES_FUTURE(sParagraph, sRegExPrefix, sRegExSuffix, iPrevGroupNumber, bMayLeave)){
            this.pAnnotator.writeToLogFile("PAST_PRES_FUTURE\n");   
            tempRes.setBestResult( iPrevGroupNumber[0]);       
            return Variables().prepareReturn(true);
        }
        
        tempRes.resetInitialValues(iPrevGroupNumber);
        if (capture_PLURAL_DATES(sParagraph, sRegExPrefix, sRegExSuffix, iPrevGroupNumber, bMayLeave)){
            this.pAnnotator.writeToLogFile("PLURAL_DATES\n");   
            tempRes.setBestResult( iPrevGroupNumber[0]);       
            return Variables().prepareReturn(true);
        }
        
        tempRes.prepareReturn(iPrevGroupNumber);
        return Variables().prepareReturn(tempRes.bAtLeastOneResult);
    }
}
