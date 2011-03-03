package temporal.rules;

import java.util.regex.*;

public class CommonPrefixSuffix {

    private TIMEXRuleAnnotator pAnnotator;
    
    public CommonPrefixSuffix(TIMEXRuleAnnotator pAnnot){
        pAnnotator = pAnnot;
    }
    
    public GlobalVariables Variables(){
        return pAnnotator.globalVariables;
    }
    
    public FindAll Find(){
        return pAnnotator.findAll;
    }
    
      public boolean capture_WS_TO_WS(String sParagraph, String sRegExPrefix, String sRegExSuffix, int[] iPrevGroupNumber, boolean bMayLeave) {
        Variables().startCapture();
        int iPref = iPrevGroupNumber[0] + 1;
        String sPattern = 
                ConstRegEx.groupBrackets(
                    ConstRegEx.groupBrackets(ConstRegEx.WS, iPrevGroupNumber) +
                    ConstRegEx.groupBrackets(Variables().hashtbLexValues.get("TO"),
                    iPrevGroupNumber) +
                    ConstRegEx.groupBrackets(ConstRegEx.WS, iPrevGroupNumber)
                , iPrevGroupNumber);
        Pattern patternLex = Pattern.compile(
                ConstRegEx.prefixWordSuffix(sPattern, sRegExPrefix, sRegExSuffix),
                Pattern.CASE_INSENSITIVE);
        boolean bResult = Find().printAllMatchResult(patternLex, sParagraph, false);
        if (bResult) Variables().saveCurrentMatch(iPref);
        return Variables().prepareReturn(bResult);
    }
      
   public boolean capture_THE_WS_op(String sParagraph, String sRegExPrefix, String sRegExSuffix, int[] iPrevGroupNumber,boolean bMayLeave){
        Variables().startCapture();
        int iPref = iPrevGroupNumber[0] +1;
        String sPattern = 
                ConstRegEx.groupBrackets(
                    ConstRegEx.groupBrackets(                
                        ConstRegEx.groupBrackets(
                            Variables().hashtbLexValues.get("THE"), iPrevGroupNumber)+
                        ConstRegEx.groupBrackets(ConstRegEx.WS, iPrevGroupNumber)
                   , iPrevGroupNumber) + "?"
                , iPrevGroupNumber);
        Pattern patternLex = Pattern.compile(
                ConstRegEx.prefixWordSuffix(sPattern, sRegExPrefix, sRegExSuffix),
                Pattern.CASE_INSENSITIVE
                );
        @SuppressWarnings("unused")
        boolean bResult = Find().printAllMatchResult(patternLex, sParagraph, false);
        Variables().saveCurrentMatch(iPref);
        return Variables().prepareReturn(true);
    }      

      public boolean capture_RANGE_Begin_WS(String sParagraph, String sRegExPrefix, String sRegExSuffix, int[] iPrevGroupNumber,boolean bMayLeave){
        Variables().startCapture();
        int iPref = iPrevGroupNumber[0] +1;
        String sPattern = 
          ConstRegEx.groupBrackets(
                    ConstRegEx.groupBrackets(ConstRegEx.WS, iPrevGroupNumber) + "?"+
                    ConstRegEx.groupBrackets(",", iPrevGroupNumber)+
                    ConstRegEx.groupBrackets(ConstRegEx.WS, iPrevGroupNumber)
                , iPrevGroupNumber);
        Pattern patternLex = Pattern.compile(
                ConstRegEx.prefixWordSuffix(sPattern, sRegExPrefix, sRegExSuffix),
                Pattern.CASE_INSENSITIVE
                );
        boolean bResult = Find().printAllMatchResult(patternLex, sParagraph, false);
        if (bResult) Variables().saveCurrentMatch(iPref);
        return Variables().prepareReturn(bResult);
    }      

   public boolean capture_WS_RANGE_WS_or_Comma_WS(String sParagraph,
            String sRegExPrefix, String sRegExSuffix, int[] iPrevGroupNumber,boolean bMayLeave){
/*
$RANGE = AND|OR|TO|TILL|UNTIL
/i;
 */
        Variables().startCapture();
        int iPref = iPrevGroupNumber[0] +1;
        String sPattern = 
                ConstRegEx.groupBrackets(
                    ConstRegEx.groupBrackets(
                        ConstRegEx.groupBrackets(ConstRegEx.WS, iPrevGroupNumber) +
                        ConstRegEx.groupBrackets("and|or|to|till|until", iPrevGroupNumber)+
                        ConstRegEx.groupBrackets(ConstRegEx.WS, iPrevGroupNumber)
                    , iPrevGroupNumber) +"|"+
                    ConstRegEx.groupBrackets(
                        ConstRegEx.groupBrackets(",", iPrevGroupNumber)+
                        ConstRegEx.groupBrackets(ConstRegEx.WS, iPrevGroupNumber)
                    , iPrevGroupNumber)                    
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
   
    public boolean capture_WS_PAST_WS(String sParagraph,
            String sRegExPrefix, String sRegExSuffix, int[] iPrevGroupNumber, boolean bMayLeave) {
/*
         $TIME_IN_WORDS_PAST= qr/
 
 $WS$PAST$WS
  /i;
 */        
        Variables().startCapture();
        int iPref = iPrevGroupNumber[0] + 1;
        String sPattern = 
                ConstRegEx.groupBrackets(
                    ConstRegEx.groupBrackets(ConstRegEx.WS, iPrevGroupNumber) +
                    ConstRegEx.groupBrackets(Variables().hashtbLexValues.get("PAST"),
                    iPrevGroupNumber) +
                    ConstRegEx.groupBrackets(ConstRegEx.WS, iPrevGroupNumber)
                , iPrevGroupNumber);
        
        Pattern patternLex = Pattern.compile(
                ConstRegEx.prefixWordSuffix(sPattern, sRegExPrefix, sRegExSuffix),
                Pattern.CASE_INSENSITIVE);
        boolean bResult = Find().printAllMatchResult(patternLex, sParagraph, false);
        if (bResult) 
        {
            Variables().saveCurrentMatch(iPref);
        }
        return Variables().prepareReturn(bResult);
    }
    
    public boolean capture_AANONE_WS(String sParagraph,
            String sRegExPrefix, String sRegExSuffix, int[] iPrevGroupNumber,
            boolean bMayLeave) {
/*
$NUM_SINGULAR_TIME= 
 * 
 * $AANONE$WS
 * /i;* 
 */        
        Variables().startCapture();
        int iPref = iPrevGroupNumber[0] + 1;
        String sPattern = 
                ConstRegEx.groupBrackets(
                    ConstRegEx.groupBrackets(
                        Variables().hashtbLexValues.get("AANONE"), iPrevGroupNumber)+
                    ConstRegEx.groupBrackets(ConstRegEx.WS, iPrevGroupNumber)
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
   
   public boolean capture_WS_AND_PUNCT_op_WS_op(String sParagraph,
            String sRegExPrefix, String sRegExSuffix, int[] iPrevGroupNumber,
            boolean bMayLeave) {
/*
 ($WS($AND|$PUNCT)?$WS?)
 */        
        Variables().startCapture();
        int iPref = iPrevGroupNumber[0] + 1;
        String sPattern =
                ConstRegEx.groupBrackets(
                ConstRegEx.groupBrackets(ConstRegEx.WS, iPrevGroupNumber) +
                ConstRegEx.groupBrackets(
                Variables().hashtbLexValues.get("AND") + "|" +
                ConstRegEx.PUNCT, iPrevGroupNumber) + "?" +
                ConstRegEx.groupBrackets(ConstRegEx.WS, iPrevGroupNumber) + "?"
                , iPrevGroupNumber);
        
        Pattern patternLex = Pattern.compile(
                ConstRegEx.prefixWordSuffix(sPattern, sRegExPrefix, sRegExSuffix),
                Pattern.CASE_INSENSITIVE);
        boolean bResult = Find().printAllMatchResult(patternLex, sParagraph, false);
        if (bResult) 
        {
            Variables().saveCurrentMatch(iPref);
        }
        return Variables().prepareReturn(bResult);
        
    }       
   

    public boolean capture_WS_op_DASH_WS_op_or_WS(String sParagraph,
            String sRegExPrefix, String sRegExSuffix, int[] iPrevGroupNumber, boolean bMayLeave) {
/*
 ($WS?$DASH$WS?|$WS)
 * /i;
 */        
        Variables().startCapture();
        int iPref = iPrevGroupNumber[0] + 1;
        String sPattern = 
                    ConstRegEx.groupBrackets(
                    ConstRegEx.groupBrackets(ConstRegEx.WS, iPrevGroupNumber) + "?" +
                    ConstRegEx.DASH +
                    ConstRegEx.groupBrackets(ConstRegEx.WS, iPrevGroupNumber) + "?" +
                    "|" +
                    ConstRegEx.groupBrackets(ConstRegEx.WS, iPrevGroupNumber)
                    , iPrevGroupNumber);         
        Pattern patternLex = Pattern.compile(
                ConstRegEx.prefixWordSuffix(sPattern, sRegExPrefix, sRegExSuffix),
                Pattern.CASE_INSENSITIVE);
        boolean bResult = Find().printAllMatchResult(patternLex, sParagraph, false);
        if (bResult) 
        {
            Variables().saveCurrentMatch(iPref);
        }
        return Variables().prepareReturn(bResult);
    }

    public boolean capture_WS(String sParagraph,
            String sRegExPrefix, String sRegExSuffix, int[] iPrevGroupNumber,
            boolean bMayLeave) {
/*
 * /i;* 
 */        
        Variables().startCapture();
        int iPref = iPrevGroupNumber[0] + 1;
        String sPattern = ConstRegEx.groupBrackets(ConstRegEx.WS, iPrevGroupNumber);
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
    
     public boolean capture_WS_op_OCLOCK(String sParagraph,
            String sRegExPrefix, String sRegExSuffix, int[] iPrevGroupNumber,
            boolean bMayLeave) {
/*
 $NUM_OCLOCK= qr/$ONE_TO_TWELVE$WS?(o\'clock|O\'CLOCK)/i;  
 */        
        Variables().startCapture();
        int iPref = iPrevGroupNumber[0] + 1;
        String sPattern =  ConstRegEx.groupBrackets(
                    ConstRegEx.groupBrackets(ConstRegEx.WS, iPrevGroupNumber)+ "?"+
                    ConstRegEx.groupBrackets("o\'clock|O\'CLOCK", iPrevGroupNumber)
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

   public boolean capture_WS_op_AMPM(String sParagraph,
            String sRegExPrefix, String sRegExSuffix, int[] iPrevGroupNumber,
            boolean bMayLeave) {
/*
 $TIME_AMPM= 
 qr/$PRE_AMPM$WS?$AMPM(?{$tempHour=($tempAP eq "p")?($tempHour != 12 ? $tempHour+12 : 12):($tempHour == 12 ? 24 : $tempHour);})/;
				#6:30 a.m., six o'clock a.m., 6 o'clock a.m., 6 a.m., six a.m 
$AMPM				= qr/([ap])\.?m\.?(?{$tempAP = lc($^N);})/i; 

 */        
        Variables().startCapture();
        int iPref = iPrevGroupNumber[0] + 1;
        String sPattern = 
                ConstRegEx.groupBrackets(
                    ConstRegEx.groupBrackets(
                    ConstRegEx.WS, iPrevGroupNumber)+ "?"+
                    ConstRegEx.groupBrackets("[ap]", iPrevGroupNumber)+
                    "\\.?\\s?\\.?m\\.?"
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
   
   public boolean capture_WS_op_COMMA_op_WS_op(String sParagraph,
            String sRegExPrefix, String sRegExSuffix, int[] iPrevGroupNumber,
            boolean bMayLeave) {
/*
 $TIME_TIMEZONE			= qr/$PRE_TZONE$WS?,?$WS?$TZONE/;
				#6:30/6/six (a.m.)/18:30 local time/BST 

 */        
        Variables().startCapture();
        int iPref = iPrevGroupNumber[0] + 1;
       String sPattern = ConstRegEx.groupBrackets(
                    ConstRegEx.groupBrackets(ConstRegEx.WS, iPrevGroupNumber)+ "?"+
                    ",?"+
                    ConstRegEx.groupBrackets(ConstRegEx.WS, iPrevGroupNumber)+ "?"
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
   
   public boolean capture_WS_VERY_WS_op(String sParagraph,
            String sRegExPrefix, String sRegExSuffix, int[] iPrevGroupNumber,
            boolean bMayLeave) {
/*
$SIMPLE_DEICTIC= 
 qr/$WS($VERY$WS)?
 (
/;
 */        
       Variables().startCapture();
       int iPref = iPrevGroupNumber[0] + 1;
       String sPattern = ConstRegEx.groupBrackets(
               ConstRegEx.groupBrackets(ConstRegEx.WS, iPrevGroupNumber) +
               ConstRegEx.groupBrackets(
                    ConstRegEx.groupBrackets(ConstRegEx.WS, iPrevGroupNumber) + 
                    ConstRegEx.groupBrackets(
                        Variables().hashtbLexValues.get("VERY"), iPrevGroupNumber) 
                , iPrevGroupNumber)+
                    "?"
                , iPrevGroupNumber);
       Pattern patternLex = Pattern.compile(
               ConstRegEx.prefixWordSuffix(sPattern, sRegExPrefix, sRegExSuffix)
               );
       boolean bResult = Find().printAllMatchResult(patternLex, sParagraph, false);
       if (bResult) {
           Variables().saveCurrentMatch(iPref);
       }
       return Variables().prepareReturn(bResult);
    }         
   
  public boolean capture_e_op_s(String sParagraph,
            String sRegExPrefix, String sRegExSuffix, int[] iPrevGroupNumber,
            boolean bMayLeave) {
/*
$SIMPLE_DEICTIC= 
 qr/$WS($VERY$WS)?
 (
/;
 */        
       Variables().startCapture();
       int iPref = iPrevGroupNumber[0] + 1;
       String sPattern = ConstRegEx.groupBrackets("e?s", iPrevGroupNumber);
       Pattern patternLex = Pattern.compile(
               ConstRegEx.prefixWordSuffix(sPattern, sRegExPrefix, sRegExSuffix),
               Pattern.CASE_INSENSITIVE);
       boolean bResult = Find().printAllMatchResult(patternLex, sParagraph, false);
       if (bResult) {
           Variables().saveCurrentMatch(iPref);
       }
       return Variables().prepareReturn(bResult);
    }    

 public boolean capture_FINANCIAL_WS_YEAR_op_WS_op(String sParagraph,
            String sRegExPrefix, String sRegExSuffix, int[] iPrevGroupNumber,
            boolean bMayLeave) {
/*
$FY_YEAR			= 
//qr/
 ($FINANCIAL($WS$YEAR)?$WS? 
 */        
       Variables().startCapture();
       int iPref = iPrevGroupNumber[0] + 1;
       String sPattern = 
                 ConstRegEx.groupBrackets(
                 ConstRegEx.groupBrackets(
                        Variables().hashtbLexValues.get("FINANCIAL"),
                        iPrevGroupNumber
                    ) +
                    ConstRegEx.groupBrackets(
                    ConstRegEx.groupBrackets(ConstRegEx.WS, iPrevGroupNumber)+
                    Variables().hashtbLexValues.get("YEAR"),
                    iPrevGroupNumber) + "?"+
                    ConstRegEx.groupBrackets(ConstRegEx.WS, iPrevGroupNumber)+"?"
                ,iPrevGroupNumber);
       
       Pattern patternLex = Pattern.compile(
               ConstRegEx.prefixWordSuffix(sPattern, sRegExPrefix, sRegExSuffix),
               Pattern.CASE_INSENSITIVE);
       boolean bResult = Find().printAllMatchResult(patternLex, sParagraph, false);
       if (bResult) {
           Variables().saveCurrentMatch(iPref);
       }
       return Variables().prepareReturn(bResult);
    }      
  
 
public boolean capture_WS_FINANCIAL_WS_YEAR_op(String sParagraph,
            String sRegExPrefix, String sRegExSuffix, int[] iPrevGroupNumber,
            boolean bMayLeave) {
/*
$FY_YEAR			= 
//qr/
 $WS$FINANCIAL($WS$YEAR)?)
 */        
       Variables().startCapture();
       int iPref = iPrevGroupNumber[0] + 1;
       String sPattern = 
                    ConstRegEx.groupBrackets(
                    ConstRegEx.groupBrackets(ConstRegEx.WS, iPrevGroupNumber) +
                    ConstRegEx.groupBrackets(
                    Variables().hashtbLexValues.get("FINANCIAL"),
                    iPrevGroupNumber) +
                    ConstRegEx.groupBrackets(
                    ConstRegEx.groupBrackets(ConstRegEx.WS, iPrevGroupNumber) +
                    Variables().hashtbLexValues.get("YEAR"),
                    iPrevGroupNumber) + "?", iPrevGroupNumber);
       
       Pattern patternLex = Pattern.compile(
               ConstRegEx.prefixWordSuffix(sPattern, sRegExPrefix, sRegExSuffix),
               Pattern.CASE_INSENSITIVE);
       boolean bResult = Find().printAllMatchResult(patternLex, sParagraph, false);
       if (bResult) {
           Variables().saveCurrentMatch(iPref);
       }
       return Variables().prepareReturn(bResult);
    }      
  

public boolean capture_WS_OF_WS_op(String sParagraph,
            String sRegExPrefix, String sRegExSuffix, int[] iPrevGroupNumber,
            boolean bMayLeave) {
/*
$WS($OF$WS)?
 */        
       Variables().startCapture();
       int iPref = iPrevGroupNumber[0] + 1;
       String sPattern = 
                ConstRegEx.groupBrackets(
               ConstRegEx.groupBrackets(ConstRegEx.WS, iPrevGroupNumber)+
                ConstRegEx.groupBrackets(
                    ConstRegEx.groupBrackets(Variables().hashtbLexValues.get("OF"), iPrevGroupNumber)+
                    ConstRegEx.groupBrackets(ConstRegEx.WS, iPrevGroupNumber)
                , iPrevGroupNumber)+"?"
               , iPrevGroupNumber);       
       Pattern patternLex = Pattern.compile(
               ConstRegEx.prefixWordSuffix(sPattern, sRegExPrefix, sRegExSuffix)
               );
       boolean bResult = Find().printAllMatchResult(patternLex, sParagraph, false);
       if (bResult) {
           Variables().saveCurrentMatch(iPref);
       }
       return Variables().prepareReturn(bResult);
    }      

    public boolean capture_WS_THE_WS_op_WS_op_COMMA_op_WS_op(String sParagraph,
            String sRegExPrefix, String sRegExSuffix, int[] iPrevGroupNumber,
            boolean bMayLeave) {
        /*
        ($WS($THE$WS)?|$WS?,?$WS?)
         */
        Variables().startCapture();
        int iPref = iPrevGroupNumber[0] + 1;
        String sPattern =
                ConstRegEx.groupBrackets(
                ConstRegEx.groupBrackets(
                ConstRegEx.groupBrackets(ConstRegEx.WS, iPrevGroupNumber) +
                ConstRegEx.groupBrackets(
                ConstRegEx.groupBrackets(Variables().hashtbLexValues.get("THE"), iPrevGroupNumber) +
                ConstRegEx.groupBrackets(ConstRegEx.WS, iPrevGroupNumber), iPrevGroupNumber) + "?", iPrevGroupNumber) + "|" +
                ConstRegEx.groupBrackets(
                ConstRegEx.groupBrackets(ConstRegEx.WS, iPrevGroupNumber) + "?" +
                ",?" +
                ConstRegEx.groupBrackets(ConstRegEx.WS, iPrevGroupNumber) + "?", iPrevGroupNumber), iPrevGroupNumber);
        Pattern patternLex = Pattern.compile(
                ConstRegEx.prefixWordSuffix(sPattern, sRegExPrefix, sRegExSuffix));
        boolean bResult = Find().printAllMatchResult(patternLex, sParagraph, false);
        if (bResult) {
            Variables().saveCurrentMatch(iPref);
        }
        return Variables().prepareReturn(bResult);
    }


    public boolean capture_WS_op_COMMA_op_WS_op_or_WS(String sParagraph,
            String sRegExPrefix, String sRegExSuffix, int[] iPrevGroupNumber,
            boolean bMayLeave) {
        /*
        ($WS?,?$WS?|$WS)
         */
        Variables().startCapture();
        int iPref = iPrevGroupNumber[0] + 1;
        String sPattern =
                ConstRegEx.groupBrackets(
                ConstRegEx.groupBrackets(ConstRegEx.WS, iPrevGroupNumber) + "?" +
                ",?" +
                ConstRegEx.groupBrackets(ConstRegEx.WS, iPrevGroupNumber) + "?" +
                "|" +
                ConstRegEx.groupBrackets(ConstRegEx.WS, iPrevGroupNumber), iPrevGroupNumber);
        Pattern patternLex = Pattern.compile(
                ConstRegEx.prefixWordSuffix(sPattern, sRegExPrefix, sRegExSuffix));
        boolean bResult = Find().printAllMatchResult(patternLex, sParagraph, false);
        if (bResult) {
            Variables().saveCurrentMatch(iPref);
        }
        return Variables().prepareReturn(bResult);
    }

   public boolean capture_WS_op_COMMA_WS_op_or_WS(String sParagraph,
            String sRegExPrefix, String sRegExSuffix, int[] iPrevGroupNumber,
            boolean bMayLeave) {
        /*
        ($WS?,$WS?|$WS)
         */
        Variables().startCapture();
        int iPref = iPrevGroupNumber[0] + 1;
        String sPattern =
                ConstRegEx.groupBrackets(
                ConstRegEx.groupBrackets(ConstRegEx.WS, iPrevGroupNumber) + "?" +
                "," +
                ConstRegEx.groupBrackets(ConstRegEx.WS, iPrevGroupNumber) + "?" +
                "|" +
                ConstRegEx.groupBrackets(ConstRegEx.WS, iPrevGroupNumber), iPrevGroupNumber);
        Pattern patternLex = Pattern.compile(
                ConstRegEx.prefixWordSuffix(sPattern, sRegExPrefix, sRegExSuffix));
        boolean bResult = Find().printAllMatchResult(patternLex, sParagraph, false);
        if (bResult) {
            Variables().saveCurrentMatch(iPref);
        }
        return Variables().prepareReturn(bResult);
    }

   public boolean capture_WS_op_br_WS_op(String sParagraph,
            String sRegExPrefix, String sRegExSuffix, int[] iPrevGroupNumber,
            boolean bMayLeave) {
        /*
     $WS?\[$WS?
         */
        Variables().startCapture();
        int iPref = iPrevGroupNumber[0] + 1;
        String sPattern =
                ConstRegEx.groupBrackets(
                    ConstRegEx.groupBrackets(ConstRegEx.WS, iPrevGroupNumber)+"?"+
                     ConstRegEx.groupBrackets(
                            "\\["+
                            ConstRegEx.groupBrackets(ConstRegEx.WS, iPrevGroupNumber)+ "?"
                    , iPrevGroupNumber)
                , iPrevGroupNumber);
        Pattern patternLex = Pattern.compile(
                ConstRegEx.prefixWordSuffix(sPattern, sRegExPrefix, sRegExSuffix));
        boolean bResult = Find().printAllMatchResult(patternLex, sParagraph, false);
        if (bResult) {
            Variables().saveCurrentMatch(iPref);
        }
        return Variables().prepareReturn(bResult);
    }    
   public boolean capture_WS_op_br(String sParagraph,
            String sRegExPrefix, String sRegExSuffix, int[] iPrevGroupNumber,
            boolean bMayLeave) {
        /*
     WS?\]
         */
        Variables().startCapture();
        int iPref = iPrevGroupNumber[0] + 1;
        String sPattern =
                        ConstRegEx.groupBrackets(
                            ConstRegEx.groupBrackets(ConstRegEx.WS, iPrevGroupNumber)+ "?"+
                            "\\]"
                        , iPrevGroupNumber);
        Pattern patternLex = Pattern.compile(
                ConstRegEx.prefixWordSuffix(sPattern, sRegExPrefix, sRegExSuffix));
        boolean bResult = Find().printAllMatchResult(patternLex, sParagraph, false);
        if (bResult) {
            Variables().saveCurrentMatch(iPref);
        }
        return Variables().prepareReturn(bResult);
    }    

   public boolean capture_WS_op_bracket_WS_op(String sParagraph,
            String sRegExPrefix, String sRegExSuffix, int[] iPrevGroupNumber,
            boolean bMayLeave) {
        /*
     $WS?\($WS?
         */
        Variables().startCapture();
        int iPref = iPrevGroupNumber[0] + 1;
        String sPattern =
                ConstRegEx.groupBrackets(
                    ConstRegEx.groupBrackets(ConstRegEx.WS, iPrevGroupNumber)+"?"+
                     ConstRegEx.groupBrackets(
                            "\\("+
                            ConstRegEx.groupBrackets(ConstRegEx.WS, iPrevGroupNumber)+ "?"
                    , iPrevGroupNumber)
                , iPrevGroupNumber);
        Pattern patternLex = Pattern.compile(
                ConstRegEx.prefixWordSuffix(sPattern, sRegExPrefix, sRegExSuffix));
        boolean bResult = Find().printAllMatchResult(patternLex, sParagraph, false);
        if (bResult) {
            Variables().saveCurrentMatch(iPref);
        }
        return Variables().prepareReturn(bResult);
    }    
   public boolean capture_WS_op_bracket(String sParagraph,
            String sRegExPrefix, String sRegExSuffix, int[] iPrevGroupNumber,
            boolean bMayLeave) {
        /*
     WS?\)
         */
        Variables().startCapture();
        int iPref = iPrevGroupNumber[0] + 1;
        String sPattern =
                        ConstRegEx.groupBrackets(
                            ConstRegEx.groupBrackets(ConstRegEx.WS, iPrevGroupNumber)+ "?"+
                            "\\)"
                        , iPrevGroupNumber);
        Pattern patternLex = Pattern.compile(
                ConstRegEx.prefixWordSuffix(sPattern, sRegExPrefix, sRegExSuffix));
        boolean bResult = Find().printAllMatchResult(patternLex, sParagraph, false);
        if (bResult) {
            Variables().saveCurrentMatch(iPref);
        }
        return Variables().prepareReturn(bResult);
    }    
    public boolean capture_WS_or_WS_op_COMMA_SEMICOL_WS_op(String sParagraph,
            String sRegExPrefix, String sRegExSuffix, int[] iPrevGroupNumber,
            boolean bMayLeave) {
        Variables().startCapture();
        int iPref = iPrevGroupNumber[0] + 1;
        String sPattern =
                ConstRegEx.groupBrackets(
                    ConstRegEx.groupBrackets(ConstRegEx.WS, iPrevGroupNumber) + "|" +
                    ConstRegEx.groupBrackets(
                            ConstRegEx.groupBrackets(ConstRegEx.WS, iPrevGroupNumber) + "?" +
                            "[,;]" +
                            ConstRegEx.groupBrackets(ConstRegEx.WS, iPrevGroupNumber) + "?"
                    , iPrevGroupNumber)
                , iPrevGroupNumber);
        Pattern patternLex = Pattern.compile(
                   ConstRegEx.prefixWordSuffix(sPattern, sRegExPrefix, sRegExSuffix));
        boolean bResult = Find().printAllMatchResult(patternLex, sParagraph, false);
        if (bResult) {
            Variables().saveCurrentMatch(iPref);
        }
        return Variables().prepareReturn(bResult);
    }
       
       public boolean capture_WS_op_COMMA_SEMICOL_op_WS_op(String sParagraph,
            String sRegExPrefix, String sRegExSuffix, int[] iPrevGroupNumber,
            boolean bMayLeave) {
        /*
        $WS?[,;]?$WS?
         */
        Variables().startCapture();
        int iPref = iPrevGroupNumber[0] + 1;
        String sPattern =
                   ConstRegEx.groupBrackets(
                   ConstRegEx.groupBrackets(ConstRegEx.WS, iPrevGroupNumber) + "?" +
                   "[,;]?" +
                   ConstRegEx.groupBrackets(ConstRegEx.WS, iPrevGroupNumber) + "?"
                   , iPrevGroupNumber);
        Pattern patternLex = Pattern.compile(
                   ConstRegEx.prefixWordSuffix(sPattern, sRegExPrefix, sRegExSuffix));
        boolean bResult = Find().printAllMatchResult(patternLex, sParagraph, false);
        if (bResult) {
            Variables().saveCurrentMatch(iPref);
        }
        return Variables().prepareReturn(bResult);
    }

public boolean capture_WS_op_SIGN_op_WS_op(String sParagraph,
            String sRegExPrefix, String sRegExSuffix, int[] iPrevGroupNumber,
            boolean bMayLeave) {
        /*
        WS?[,;\[\(]?$WS?
         */
        Variables().startCapture();
        int iPref = iPrevGroupNumber[0] + 1;
        String sPattern =
                    ConstRegEx.groupBrackets(
                        ConstRegEx.groupBrackets(ConstRegEx.WS, iPrevGroupNumber ) + "?"+
                        "[,;\\[\\(]?"+
                        ConstRegEx.groupBrackets(ConstRegEx.WS, iPrevGroupNumber) + "?"
                , iPrevGroupNumber);
       Pattern patternLex = Pattern.compile(
                   ConstRegEx.prefixWordSuffix(sPattern, sRegExPrefix, sRegExSuffix));
        boolean bResult = Find().printAllMatchResult(patternLex, sParagraph, false);
        if (bResult) {
            Variables().saveCurrentMatch(iPref);
        }
        return Variables().prepareReturn(bResult);
    }

        public boolean capture_WS_op_br_op(String sParagraph,
            String sRegExPrefix, String sRegExSuffix, int[] iPrevGroupNumber,
            boolean bMayLeave) {
        /*
        ($WS?[\]\)]?)
         */
        Variables().startCapture();
        int iPref = iPrevGroupNumber[0] + 1;
        String sPattern =
                        ConstRegEx.groupBrackets(
                        ConstRegEx.groupBrackets(ConstRegEx.WS, iPrevGroupNumber) + "?"+
                            "[\\]\\)]?", iPrevGroupNumber);
        Pattern patternLex = Pattern.compile(
                   ConstRegEx.prefixWordSuffix(sPattern, sRegExPrefix, sRegExSuffix));
        boolean bResult = Find().printAllMatchResult(patternLex, sParagraph, false);
        if (bResult) {
            Variables().saveCurrentMatch(iPref);
        }
        return Variables().prepareReturn(bResult);
    }
        public boolean capture_WS_op_straight_op_WS_op_DASH_WS_op_or_WS(String sParagraph,
            String sRegExPrefix, String sRegExSuffix, int[] iPrevGroupNumber,
            boolean bMayLeave) {
        /*
        $WS?(?:straight)?
         */
        Variables().startCapture();
        int iPref = iPrevGroupNumber[0] + 1;
        String sPattern =
                        ConstRegEx.groupBrackets(
                            ConstRegEx.groupBrackets(ConstRegEx.WS, iPrevGroupNumber)+"?"+
                            "(?:straight)?"
                            +
                        ConstRegEx.groupBrackets(
                        ConstRegEx.groupBrackets(ConstRegEx.WS, iPrevGroupNumber) + "?" +
                        ConstRegEx.DASH +
                        ConstRegEx.groupBrackets(ConstRegEx.WS, iPrevGroupNumber) + "?" +
                        "|" +
                        ConstRegEx.groupBrackets(ConstRegEx.WS, iPrevGroupNumber)
                        , iPrevGroupNumber)
                    , iPrevGroupNumber);         
        
        Pattern patternLex = Pattern.compile(
                   ConstRegEx.prefixWordSuffix(sPattern, sRegExPrefix, sRegExSuffix));
        boolean bResult = Find().printAllMatchResult(patternLex, sParagraph, false);
        if (bResult) {
            Variables().saveCurrentMatch(iPref);
        }
        return Variables().prepareReturn(bResult);
    }

    public boolean capture_AANONE_WS_op(String sParagraph,
            String sRegExPrefix, String sRegExSuffix, int[] iPrevGroupNumber,
            boolean bMayLeave) {
/*
($AANONE$WS)?
 * /i;* 
 */        
        Variables().startCapture();
        int iPref = iPrevGroupNumber[0] + 1;
        String sPattern = 
                ConstRegEx.groupBrackets(
                ConstRegEx.groupBrackets(
                    ConstRegEx.groupBrackets(
                        Variables().hashtbLexValues.get("AANONE")
                    , iPrevGroupNumber)+ 
                    ConstRegEx.groupBrackets(
                        ConstRegEx.WS
                        , iPrevGroupNumber)
                    , iPrevGroupNumber)+"?"
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

        
}

