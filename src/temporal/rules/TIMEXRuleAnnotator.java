package temporal.rules;

import java.util.regex.*;
import java.io.*;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import javax.xml.datatype.*;
import temporal.NormalizedChunk;
import com.aliasi.chunk.Chunk;
import com.aliasi.chunk.ChunkFactory;
import com.aliasi.chunk.Chunker;
import com.aliasi.chunk.Chunking;
import com.aliasi.chunk.ChunkingImpl;
import com.aliasi.sentences.IndoEuropeanSentenceModel;
import com.aliasi.sentences.SentenceChunker;
import com.aliasi.tokenizer.IndoEuropeanTokenizerFactory;

/**
 * Java port of the Perl engine developed by Georgiana Puscasu
 */
public class TIMEXRuleAnnotator implements Chunker {
	
   public Chunking chunk(CharSequence cSeq) { 
	   return chunk(cSeq.toString().toCharArray(),0,cSeq.length()); 
   }
   
   public Chunking chunk(char[] cs, int start, int end) { 
	    String s = new String(cs,start,end);
	    ChunkingImpl chunks = new ChunkingImpl(s);
	    SentenceChunker schunker = new SentenceChunker(IndoEuropeanTokenizerFactory.INSTANCE, new IndoEuropeanSentenceModel() );
	    Set<Chunk> sentences = schunker.chunk(s).chunkSet();
	    for ( Chunk sentence : sentences ) {
	    	int sStart = sentence.start();
	    	List<NormalizedChunk> timexes = capture_AllTimeExpressions( s.substring(sentence.start(), sentence.end()), xmlCalendar );
		    for ( NormalizedChunk timex : timexes ) {
		    	timex.setStart(timex.start() + sStart);
		    	timex.setEnd(timex.end() + sStart);
		    	chunks.add(timex);
		    }
	    }
		return chunks;
   }

   public GlobalVariables globalVariables;
   
   public FindAll findAll;
   
   public Date_TE dateTE;
   
   public Time_TE timeTE;
   
   public MixedAll_TE mixedallTE;
   
   public Undefinite_TE undefTE;
   
   public CommonPrefixSuffix comPrefSuf;

   public ResolveTimePoint solveTimePoint;
   
   public XMLGregorianCalendar xmlCalendar;
   
   public TIMEXRuleAnnotator() throws DatatypeConfigurationException {
	   this( new GlobalVariables(false) );
   }
   
   public TIMEXRuleAnnotator(GlobalVariables variables) throws DatatypeConfigurationException {
       globalVariables = variables;
       findAll = new FindAll(globalVariables);
       dateTE = new Date_TE(this);
       timeTE = new Time_TE(this);
       mixedallTE = new MixedAll_TE(this);
       undefTE = new Undefinite_TE(this);       
       comPrefSuf = new CommonPrefixSuffix(this);
       solveTimePoint = new ResolveTimePoint();
       GregorianCalendar today = new GregorianCalendar();                
       DatatypeFactory factory = DatatypeFactory.newInstance();                
       xmlCalendar = factory.newXMLGregorianCalendar(today.get(java.util.GregorianCalendar.YEAR), today.get(java.util.GregorianCalendar.MONTH)+1, 12, 0, 0, 0, 0, 0);  
   }
   
   public GlobalVariables variables(){
        return globalVariables;
   }

   public boolean capture_PUNCT(String sParagraph, String sRegExPrefix, String sRegExSuffix, int[] iPrevGroupNumber,boolean bMayLeave){
        variables().startCapture();
        String sPattern = ConstRegEx.groupBrackets(ConstRegEx.PUNCT, iPrevGroupNumber);
        Pattern patternLex = Pattern.compile(
                ConstRegEx.prefixWordSuffix(sPattern, sRegExPrefix, sRegExSuffix));
        boolean bResult = findAll.printAllMatchResult(patternLex, sParagraph, false);
        if (bResult) { variables().saveCurrentMatch(iPrevGroupNumber[0]); }
        return variables().prepareReturn(bResult);
    }
    public boolean capture_CARD(String sParagraph, String sRegExPrefix, String sRegExSuffix, 
            int[] iPrevGroupNumber,boolean bMayLeave){
/*
$CARD    			= qr/((\d+)(?{$ordWords->val(int($^N));}))/;	
				#cardinal numbers (si compuse si cu minus inainte)
 */     
            variables().startCapture();
            String sPattern = ConstRegEx.groupBrackets("\\d+", iPrevGroupNumber);

            Pattern patternLex = Pattern.compile(
                    ConstRegEx.prefixWordSuffix(sPattern, sRegExPrefix, sRegExSuffix));
            boolean bResult =
                    findAll.printAllMatchResult(patternLex, sParagraph, false);
            if (bResult) {
                try {
                    variables().tempVariables.ordWords.set_val(Integer.parseInt(
                            variables().tempVariables.matchResult.group(iPrevGroupNumber[0])));
                    variables().saveCurrentMatch(iPrevGroupNumber[0]);
                }catch(Exception e){
                    if (!bMayLeave){ Const.writeSpecificError(e, sParagraph);}
                    variables().tempVariables.sCurrentMatch = "";
                }   
            }
            return variables().prepareReturn(bResult);
    }
    
    public boolean capture_FINANCIAL(String sParagraph, String sRegExPrefix, String sRegExSuffix, 
            int[] iPrevGroupNumber,boolean bMayLeave){
/*
 */        
        variables().startCapture();
        String sPattern = ConstRegEx.groupBrackets(
                variables().hashtbLexValues.get("FINANCIAL")
                , iPrevGroupNumber);
        Pattern patternLex = Pattern.compile(
                ConstRegEx.prefixWordSuffix(sPattern, sRegExPrefix, sRegExSuffix), 
                Pattern.CASE_INSENSITIVE);
        boolean bResult =
                findAll.printAllMatchResult(patternLex, sParagraph, false);
        if (bResult) {
            variables().saveCurrentMatch(iPrevGroupNumber[0]);
        }
        return variables().prepareReturn(bResult);
    }    

    public boolean capture_ORD_NAME_Case1(String sParagraph, String sRegExPrefix, String sRegExSuffix, int[] iPrevGroupNumber, boolean bMayLeave) {
        variables().startCapture();
        String sPattern = 
                ConstRegEx.groupBrackets(
                variables().hashtbLexValues.get("UNITH") + "|" +
                variables().hashtbLexValues.get("TEENTH") + "|" +
                variables().hashtbLexValues.get("TIETH"), iPrevGroupNumber);
        
        Pattern patternLex = Pattern.compile(
                ConstRegEx.prefixWordSuffix(sPattern, sRegExPrefix, sRegExSuffix),
                Pattern.CASE_INSENSITIVE);
        
        boolean bRes = findAll.printAllMatchResult(patternLex, sParagraph, false);
        if (bRes){
            try{
                int iVal = Integer.parseInt(variables().getMapHashValue(
                        variables().getResult_LowerCase(iPrevGroupNumber[0])
                        ));
                variables().tempVariables.iordNumber = iVal;
                
                variables().saveCurrentMatch(iPrevGroupNumber[0]);
            }catch(Exception e){
                if (!bMayLeave) {Const.writeSpecificError(e, sParagraph);}
                variables().tempVariables.sCurrentMatch = "";
            }
        }
        return variables().prepareReturn(bRes);
    }
    
    public boolean capture_ORD_NAME_Case2(String sParagraph, String sRegExPrefix, String sRegExSuffix,
            int[] iPrevGroupNumber, boolean bMayLeave) {
/* $ORD_NAME= qr/
 ( ($TY)(?{$ordNumber = int($map_val{$^N});}) )
    $WS?-?$WS?
    ( ($UNITH)(?{$ordNumber += int($map_val{$^N});}) )
 * ) /i
 */        
        variables().startCapture();
         int iRes = iPrevGroupNumber[0] + 1;
         String sPattern = ConstRegEx.groupBrackets(
                    ConstRegEx.groupBrackets(
                        variables().hashtbLexValues.get("TY") , iPrevGroupNumber) + 
                    ConstRegEx.groupBrackets(ConstRegEx.WS , iPrevGroupNumber) + "?"+
                    ConstRegEx.DASH+"?"+
                    ConstRegEx.groupBrackets(ConstRegEx.WS , iPrevGroupNumber) + "?" +
                    ConstRegEx.groupBrackets(
                        variables().hashtbLexValues.get("UNITH") , iPrevGroupNumber)
                , iPrevGroupNumber);
        
        Pattern patternLex = Pattern.compile(
                ConstRegEx.prefixWordSuffix(sPattern, sRegExPrefix, sRegExSuffix),
                Pattern.CASE_INSENSITIVE);
        
        boolean bRes = findAll.printAllMatchResult(patternLex, sParagraph, false);
        if (bRes){
            try{
                  int iValTy = Integer.parseInt(variables().getMapHashValue(
                          variables().getResult_LowerCase(iRes + 1)
                        ));
                  variables().tempVariables.iordNumber = iValTy;
                  int iValUnith = Integer.parseInt(variables().getMapHashValue(
                          variables().getResult_LowerCase(iRes + 4)
                        ));
                  
                variables().tempVariables.iordNumber += iValUnith;
                
                variables().saveCurrentMatch(iRes);
            }catch(Exception e){
                if (!bMayLeave) {Const.writeSpecificError(e, sParagraph);}
                variables().tempVariables.sCurrentMatch = "";
            }
        }
        return variables().prepareReturn(bRes);
    }
    public boolean capture_ORD_NAME(String sParagraph, String sRegExPrefix, String sRegExSuffix, 
            int[] iPrevGroupNumber,boolean bMayLeave){
/*
 $ORD_NAME= qr/
 (
    ($UNITH|$TEENTH|$TIETH)(?{$ordNumber = int($map_val{$^N});})|
 (
    ( ($TY)(?{$ordNumber = int($map_val{$^N});}) )
    $WS?-?$WS?
    ( ($UNITH)(?{$ordNumber += int($map_val{$^N});}) )
 )
 
 )(?{$ordWords->val($ordNumber);})/i if qr/$ORD/;
$ORD_NAME= qr/$ORD_NAME($WS(straight))?/i;
				#ordinal in words
 */        
        variables().startCapture();
        String sPatternCond = "(" + variables().hashtbLexValues.get("ORD") + ")"; 
        Pattern patternLex = Pattern.compile(
                ConstRegEx.conditioned_prefixWordSuffix(sPatternCond, sRegExPrefix, sRegExSuffix));

        if ( findAll.printAllMatchResult(patternLex, sParagraph, false) ){
            int[] iTempGr = new int[1]; iTempGr[0] =0;
            String sNewSuf = ConstRegEx.groupBrackets(
                     ConstRegEx.groupBrackets(
                        ConstRegEx.groupBrackets(
                            ConstRegEx.WS
                        , iTempGr)+
                        ConstRegEx.groupBrackets(
                            "straight"
                        , iTempGr)
                     , iTempGr)+"?"
                   , iTempGr)+
                   sRegExSuffix;
            TempResultVariables tempRes = new TempResultVariables(variables());
            tempRes.setInitialValues(iPrevGroupNumber);
            
            if (capture_ORD_NAME_Case1(sParagraph, sRegExPrefix, sNewSuf,
                    iPrevGroupNumber, bMayLeave)){
                tempRes.setBestResult(iPrevGroupNumber[0]);
            }
            if (!tempRes.bAtLeastOneResult){
                tempRes.resetInitialValues(iPrevGroupNumber);
                if (capture_ORD_NAME_Case2(sParagraph, sRegExPrefix, sNewSuf,
                    iPrevGroupNumber, bMayLeave)){
                    tempRes.setBestResult(iPrevGroupNumber[0]);
                }
            }
            tempRes.prepareReturn(iPrevGroupNumber);
            if (tempRes.bAtLeastOneResult){
                try{
//(?{$ordWords->val($ordNumber);}
                    variables().tempVariables.ordWords.set_val(variables().tempVariables.iordNumber);
                    variables().concatenateSuffix(iPrevGroupNumber[0] + 1);
                    iPrevGroupNumber[0] += iTempGr[0];
                } catch (Exception e) {
                    if (!bMayLeave) {Const.writeSpecificError(e, sParagraph);}
                }
            }
            return variables().prepareReturn(tempRes.bAtLeastOneResult);
        }
        return variables().prepareReturn(false);
    }    
    public boolean capture_UNIT(String sParagraph, String sRegExPrefix, String sRegExSuffix, 
            int[] iPrevGroupNumber,boolean bMayLeave){
/*
$UNIT				
 = qr/($UNIT)(?{$ordWords->unit($map_val{lc($^N)});})/i;
 */        
        variables().startCapture();
        String sPattern = ConstRegEx.groupBrackets(
                variables().hashtbLexValues.get("UNIT").toString(),
                iPrevGroupNumber
                );
        Pattern patternLex = Pattern.compile(
                ConstRegEx.prefixWordSuffix(sPattern, sRegExPrefix, sRegExSuffix), 
                Pattern.CASE_INSENSITIVE);

        boolean bResult =
                findAll.printAllMatchResult(patternLex, sParagraph, false);
        if (bResult) {
            try {

                variables().tempVariables.ordWords.set_unit(Integer.parseInt(
                        variables().getMapHashValue(
                            variables().getResult_LowerCase(iPrevGroupNumber[0])
                        )));
                variables().saveCurrentMatch(iPrevGroupNumber[0]);
            } catch (Exception e) {
               if (!bMayLeave){ Const.writeSpecificError(e, sParagraph);}
               variables().tempVariables.sCurrentMatch = "";
            }
        }
        return variables().prepareReturn(bResult);
    }
    public boolean capture_TEEN(String sParagraph, String sRegExPrefix, String sRegExSuffix, 
            int[] iPrevGroupNumber,boolean bMayLeave){
/*
 $TEEN				= 
 qr/($TEEN)(?{$ordWords->teen($map_val{lc($^N)});})/i;
 */        
        variables().startCapture();
        String sPattern = ConstRegEx.groupBrackets(
                variables().hashtbLexValues.get("TEEN").toString(),
                iPrevGroupNumber
                );
        Pattern patternLex = Pattern.compile(
                ConstRegEx.prefixWordSuffix(sPattern, sRegExPrefix, sRegExSuffix), 
                Pattern.CASE_INSENSITIVE);
        boolean bResult =
                findAll.printAllMatchResult(patternLex, sParagraph, false);        

        if (bResult) {
            try{
                variables().tempVariables.ordWords.teen(Integer.parseInt(
                        variables().getMapHashValue(
                            variables().getResult_LowerCase(iPrevGroupNumber[0])
                        )));
                variables().saveCurrentMatch(iPrevGroupNumber[0]);
            }catch(Exception e){
              if (!bMayLeave){ Const.writeSpecificError(e, sParagraph);}
              variables().tempVariables.sCurrentMatch = "";
            }
        }
        return variables().prepareReturn(bResult);
    }
    
    public boolean capture_TY(String sParagraph, String sRegExPrefix, String sRegExSuffix, 
                int[] iPrevGroupNumber,boolean bMayLeave){
/*
$TY= qr/($TY)(?{$ordWords->ty($map_val{lc($^N)}\/10);})/i;
 */
        variables().startCapture();
        String sPattern =ConstRegEx.groupBrackets(
                variables().hashtbLexValues.get("TY").toString(),
                iPrevGroupNumber
                );
        Pattern patternLex = Pattern.compile(
                ConstRegEx.prefixWordSuffix(sPattern, sRegExPrefix, sRegExSuffix),
                Pattern.CASE_INSENSITIVE);
        boolean bResult =
                findAll.printAllMatchResult(patternLex, sParagraph, false);
        if (bResult) {
            try {
                //$TY= qr/($TY)(?{$ordWords->ty($map_val{lc($^N)}\/10);})/i;
                int iTy = Integer.parseInt(variables().getMapHashValue(
                        variables().getResult_LowerCase(iPrevGroupNumber[0])
                        ));
                variables().tempVariables.ordWords.set_ty(iTy / 10);
                variables().saveCurrentMatch(iPrevGroupNumber[0]);

            } catch (Exception e) {
                if (!bMayLeave){ Const.writeSpecificError(e, sParagraph);}
                variables().tempVariables.sCurrentMatch = "";
            }
        }
        return variables().prepareReturn(bResult);
                
    }
    public boolean capture_TY_UNIT(String sParagraph, String sRegExPrefix, String sRegExSuffix, 
                int[] iPrevGroupNumber,boolean bMayLeave){
/*
TO_99 = (?{$ordWords = new OrdinalNumber();})$TY_UNIT /i;
 * 
$TY_UNIT			= qr/$TY$WS?$DASH?$WS?$UNIT/i;
 #thirty one
 * 
$TY				= qr/($TY)(?{$ordWords->ty($map_val{lc($^N)}\/10);})/i;
$UNIT				= qr/($UNIT)(?{$ordWords->unit($map_val{lc($^N)});})/i;
 */        
        variables().startCapture();
        String sNewPref = sRegExPrefix + "("; String sNewSuf = ")"+sRegExSuffix;
        iPrevGroupNumber[0]++;//brackets
        int iResGr = iPrevGroupNumber[0];
        String sPattern_TY =ConstRegEx.groupBrackets(
                variables().hashtbLexValues.get("TY").toString(),
                iPrevGroupNumber
                );
        int iTY_Index = iPrevGroupNumber[0];
        String sPattern = sPattern_TY + 
                ConstRegEx.groupBrackets(ConstRegEx.WS, iPrevGroupNumber)+"?"+
                ConstRegEx.DASH+"?"+
                ConstRegEx.groupBrackets(ConstRegEx.WS, iPrevGroupNumber)+"?"+
                ConstRegEx.groupBrackets(
                variables().hashtbLexValues.get("UNIT").toString(),
                iPrevGroupNumber
                );
        Pattern patternLex = Pattern.compile(
                ConstRegEx.prefixWordSuffix(sPattern, sNewPref, sNewSuf),
                Pattern.CASE_INSENSITIVE);
        boolean bResult =
                findAll.printAllMatchResult(patternLex, sParagraph, false);
        if (bResult) {
            //$TY= qr/($TY)(?{$ordWords->ty($map_val{lc($^N)}\/10);})/i;
            try {
                //2008.10.06 - There is a "?" at the end of this expression :((
                int iTy = Integer.parseInt(variables().getMapHashValue(
                        variables().getResult_LowerCase(iTY_Index)
                        ));
                variables().tempVariables.ordWords.set_ty(iTy / 10);
                //$UNIT= qr/($UNIT)(?{$ordWords->unit($map_val{lc($^N)});})/i;            
                int iUnit = Integer.parseInt(variables().getMapHashValue(
                        variables().getResult_LowerCase(iPrevGroupNumber[0])
                        ));
                variables().tempVariables.ordWords.set_unit(iUnit);
                variables().saveCurrentMatch(iResGr);
            }catch(Exception e){
               if (!bMayLeave){ Const.writeSpecificError(e, sParagraph);}
               variables().tempVariables.sCurrentMatch = "";
            }
        }
        return variables().prepareReturn(bResult);
        
    }
    public boolean capture_TO_99(String sParagraph, String sRegExPrefix, String sRegExSuffix, 
                int[] iPrevGroupNumber,boolean bMayLeave){
/*
$TO_99				= 
//qr/(?{$ordWords = new OrdinalNumber();})$TY_UNIT|
 //(?{$ordWords = new OrdinalNumber();})$TY|$TEEN|$UNIT/i;
#zero-ninetynine
 */    
        variables().startCapture();
        variables().tempVariables.ordWords = new OrdinalNumber();
        TempResultVariables tempRes = new TempResultVariables(variables());
        tempRes.setInitialValues(iPrevGroupNumber);
        
        if (capture_TY_UNIT(sParagraph, sRegExPrefix, sRegExSuffix, iPrevGroupNumber, bMayLeave)){
             tempRes.setBestResult( iPrevGroupNumber[0]);    
             return variables().prepareReturn(true);
        }
        
        tempRes.resetInitialValues(iPrevGroupNumber);
        if (capture_TY(sParagraph, sRegExPrefix, sRegExSuffix, iPrevGroupNumber, bMayLeave)){
             tempRes.setBestResult( iPrevGroupNumber[0]);    
             return variables().prepareReturn(true);
        }
        tempRes.resetInitialValues(iPrevGroupNumber);
        if (capture_TEEN(sParagraph, sRegExPrefix, sRegExSuffix, iPrevGroupNumber, bMayLeave)){
            tempRes.setBestResult( iPrevGroupNumber[0]);    
            return variables().prepareReturn(true);
        }
        tempRes.resetInitialValues(iPrevGroupNumber);
        if (capture_UNIT(sParagraph, sRegExPrefix, sRegExSuffix, iPrevGroupNumber, bMayLeave)){
            tempRes.setBestResult( iPrevGroupNumber[0]);    
            return variables().prepareReturn(true);
        }
        tempRes.prepareReturn(iPrevGroupNumber);
        return variables().prepareReturn(tempRes.bAtLeastOneResult);
    }
    public boolean capture_TO_99_MIX(String sParagraph, String sRegExPrefix, String sRegExSuffix, 
                int[] iPrevGroupNumber,boolean bMayLeave){
/*
$TO_99_MIX  			= qr/$TO_99|(([0-9][0-9]?)(?{$ordWords->val($^N);}))/;
				#up to 99 either in digits or words

 */        
        variables().startCapture();
        TempResultVariables tempRes = new TempResultVariables(variables());
        tempRes.setInitialValues(iPrevGroupNumber);
        
        if (capture_TO_99(sParagraph, sRegExPrefix, sRegExSuffix, iPrevGroupNumber, bMayLeave)){
           tempRes.setBestResult( iPrevGroupNumber[0]);    
           return variables().prepareReturn(true);
        }
        
        tempRes.resetInitialValues(iPrevGroupNumber);
        String sPattern = ConstRegEx.groupBrackets(
                "[0-9][0-9]?", iPrevGroupNumber);
        Pattern patternLex = Pattern.compile(
                ConstRegEx.prefixWordSuffix(sPattern, sRegExPrefix, sRegExSuffix));
        boolean bResult =
                findAll.printAllMatchResult(patternLex, sParagraph, false);
        if (bResult) {
            try{
                variables().tempVariables.ordWords.set_val(Integer.parseInt(
                        variables().tempVariables.matchResult.group(iPrevGroupNumber[0])));

                variables().saveCurrentMatch(iPrevGroupNumber[0]);
                tempRes.setBestResult(iPrevGroupNumber[0]);
                return variables().prepareReturn(true);
            }catch(Exception e){
               if (!bMayLeave){ Const.writeSpecificError(e, sParagraph);}
               variables().tempVariables.sCurrentMatch = "";
            }
        }
        
        tempRes.prepareReturn(iPrevGroupNumber);
        return variables().prepareReturn(tempRes.bAtLeastOneResult);
    }
    public boolean capture_AND_TO_99(String sParagraph, String sRegExPrefix, String sRegExSuffix, 
                int[] iPrevGroupNumber,boolean bMayLeave){
/*
$AND_TO_99			= qr/($AND$WS)?$TO_99_MIX/;						
#and one, and twenty five, and 14 - these follow hundred, thousand etc 
 */      
        variables().startCapture();
        int iPref = iPrevGroupNumber[0] + 1;
        String sNewPrefix =sRegExPrefix +
                ConstRegEx.groupBrackets(
                    ConstRegEx.groupBrackets(
                        ConstRegEx.groupBrackets(
                            variables().hashtbLexValues.get("AND"),
                            iPrevGroupNumber) +
                        ConstRegEx.groupBrackets(
                            ConstRegEx.WS,
                            iPrevGroupNumber), 
                    iPrevGroupNumber) + "?",
                iPrevGroupNumber);
        boolean bRes = capture_TO_99_MIX(sParagraph,  sNewPrefix, sRegExSuffix, iPrevGroupNumber, bMayLeave);
        if (bRes){
            try {
                variables().concatenatePrefix(iPref);
            } catch(Exception e){
                    if (!bMayLeave){ Const.writeSpecificError(e, sParagraph);}
            }
        }
        return variables().prepareReturn(bRes);
    }
    public boolean capture_HUN_Case1(String sParagraph, String sRegExPrefix, String sRegExSuffix, 
                int[] iPrevGroupNumber,boolean bMayLeave){
/*
$HUN
 = qr/
 (?{$ordWords = new OrdinalNumber();})
 ( ($AANONE$WS(hundred))(?{$ordWords->hun(1);}))
/i;	
				#a hundred, one hundred, 3 hundred ...  
 */        
        variables().startCapture();
        variables().tempVariables.ordWords = new OrdinalNumber();
        int iResGr = iPrevGroupNumber[0] + 1;
        String sPattern = ConstRegEx.groupBrackets(
                ConstRegEx.groupBrackets(
                    variables().hashtbLexValues.get("AANONE")
                , iPrevGroupNumber) +
                ConstRegEx.groupBrackets(
                    ConstRegEx.WS
                , iPrevGroupNumber)+
                ConstRegEx.groupBrackets("hundred", iPrevGroupNumber)                
                , iPrevGroupNumber);
        Pattern patternLex = Pattern.compile(
                ConstRegEx.prefixWordSuffix(sPattern, sRegExPrefix, sRegExSuffix),
                Pattern.CASE_INSENSITIVE);
        boolean bResult =
                findAll.printAllMatchResult(patternLex, sParagraph, false);
        if (bResult) {
            try {
                variables().tempVariables.ordWords.set_hun(1);
                variables().saveCurrentMatch(iResGr);
            }catch(Exception e){
              if (!bMayLeave){ Const.writeSpecificError(e, sParagraph);}
                   variables().tempVariables.sCurrentMatch = "";
            }
        }        
        return variables().prepareReturn(bResult);
    }
    public boolean capture_HUN_Case2(String sParagraph, String sRegExPrefix, String sRegExSuffix, 
                int[] iPrevGroupNumber,boolean bMayLeave){
/*
$HUN
 = qr/
 |
 (($TO_99_MIX$WS(hundred))(?{$ordWords->hun($ordWords->ty()*10+$ordWords->unit());}))/i;	
				#a hundred, one hundred, 3 hundred ...  

 */        
        variables().startCapture();
        int[] iTemp = new int[1]; iTemp[0] = 0;
        String sNewSuffix = 
                ConstRegEx.groupBrackets(
                    ConstRegEx.groupBrackets(ConstRegEx.WS, iTemp) +
                    ConstRegEx.groupBrackets("hundred", iTemp),
                iTemp) +
                sRegExSuffix;
        boolean bResult = capture_TO_99_MIX(sParagraph, sRegExPrefix, sNewSuffix, iPrevGroupNumber, bMayLeave);
        if (bResult){
            try {
                int iTy = variables().tempVariables.ordWords.get_ty() * 10;
                int iUnit = variables().tempVariables.ordWords.get_unit();
                variables().tempVariables.ordWords.set_hun(iTy + iUnit);

                variables().concatenateSuffix(iPrevGroupNumber[0] + 1);
                iPrevGroupNumber[0] += iTemp[0];
            } catch(Exception e){
               if (!bMayLeave){ Const.writeSpecificError(e, sParagraph);}
            }
        }
        return variables().prepareReturn(bResult);
    }
    public boolean capture_HUN(String sParagraph, String sRegExPrefix, String sRegExSuffix, 
                int[] iPrevGroupNumber,boolean bMayLeave){
/*
$HUN
 = qr/
 (?{$ordWords = new OrdinalNumber();})
 ( ($AANONE$WS(hundred))(?{$ordWords->hun(1);}))
 |
 (($TO_99_MIX$WS(hundred))(?{$ordWords->hun($ordWords->ty()*10+$ordWords->unit());}))/i;	
				#a hundred, one hundred, 3 hundred ...  
*/   
        variables().startCapture();
        TempResultVariables tempRes = new TempResultVariables(variables());
        tempRes.setInitialValues(iPrevGroupNumber);
        
        if (capture_HUN_Case1(sParagraph, sRegExPrefix, sRegExSuffix, iPrevGroupNumber, bMayLeave)){
             tempRes.setBestResult( iPrevGroupNumber[0]); 
             return variables().prepareReturn(true);
        }
        
        tempRes.resetInitialValues(iPrevGroupNumber);
        if ( capture_HUN_Case2(sParagraph, sRegExPrefix, sRegExSuffix, iPrevGroupNumber, bMayLeave)){
            tempRes.setBestResult( iPrevGroupNumber[0]); 
            return variables().prepareReturn(true);
        }
        tempRes.prepareReturn(iPrevGroupNumber);
        return variables().prepareReturn(tempRes.bAtLeastOneResult);
    }
    public boolean capture_HUN_99(String sParagraph, String sRegExPrefix, String sRegExSuffix, 
                int[] iPrevGroupNumber,boolean bMayLeave){
/*
$HUN_99
 = qr/($HUN)(?{$tempH = $ordWords->hun();$ordWords = new OrdinalNumber();})
 ($WS$AND_TO_99)?(?{$ordWords->hun($tempH);})/i;						
				#one hundred and eighty, five hundred and 44, ... 

 */        
        variables().startCapture();
        String sNewSuffix = ConstRegEx.REST_ANY + sRegExSuffix;
        boolean bResult = capture_HUN(sParagraph, sRegExPrefix, sNewSuffix, iPrevGroupNumber, bMayLeave);
        if (bResult){
            try {
                TempSaveCurrentMatch tmpMatch = new TempSaveCurrentMatch(variables());
                sParagraph = tmpMatch.saveMatchAndMoveOverAndReset(sParagraph);
                String sNewPrefix =ConstRegEx.newPrefix(iPrevGroupNumber);
                
                variables().tempVariables.stempH =
                        variables().tempVariables.ordWords.get_hun().toString();
                variables().tempVariables.ordWords = new OrdinalNumber();
                
                bResult = capture_WS_AND_TO_99_op(sParagraph,  
                        sNewPrefix, sRegExSuffix, iPrevGroupNumber, true);
                if (bResult) 
                {
                    try {
                        variables().tempVariables.ordWords.set_hun(
                                Integer.parseInt(variables().tempVariables.stempH));
                        
                        tmpMatch.restoreCurrentMatch();
                    } catch(Exception e){
                        if (!bMayLeave){ Const.writeSpecificError(e, sParagraph);}
                    }
                }
                return variables().prepareReturn(true);
            }catch(Exception e){
                    if (!bMayLeave){ Const.writeSpecificError(e, sParagraph);}
            }
        }
        return variables().prepareReturn(bResult);
    }
    public boolean capture_TO_999(String sParagraph, String sRegExPrefix, String sRegExSuffix, 
                int[] iPrevGroupNumber,boolean bMayLeave){
/*
$TO_999				= qr/$HUN_99|$TO_99/;  							
				#up to nine hundred and ninety nine  
 */        
        variables().startCapture();
        TempResultVariables tempRes = new TempResultVariables(variables());
        tempRes.setInitialValues(iPrevGroupNumber);
        if (capture_HUN_99(sParagraph, sRegExPrefix, sRegExSuffix, iPrevGroupNumber, bMayLeave)){
             tempRes.setBestResult( iPrevGroupNumber[0]);   
             return variables().prepareReturn(true);
        }
        tempRes.resetInitialValues(iPrevGroupNumber);
        if (capture_TO_99(sParagraph, sRegExPrefix, sRegExSuffix, iPrevGroupNumber, bMayLeave)){
             tempRes.setBestResult( iPrevGroupNumber[0]);   
             return variables().prepareReturn(true);
        }
        tempRes.prepareReturn(iPrevGroupNumber);
        return variables().prepareReturn(tempRes.bAtLeastOneResult);
    }
    public boolean capture_TO_999_MIX(String sParagraph, String sRegExPrefix, String sRegExSuffix, 
                int[] iPrevGroupNumber,boolean bMayLeave){
/*
$TO_999_MIX			
 = qr/$TO_999|(([1-9][0-9]?[0-9]?)(?{$ordWords->hun(int($^N\/100)); $ordWords->ty(int(($^N%100)\/10));
 $ordWords->unit(($^N%100)%10);}))/;				
				#up to 999 either in digits or words  
 */ 
        variables().startCapture();
        TempResultVariables tempRes = new TempResultVariables(variables());
        tempRes.setInitialValues(iPrevGroupNumber);
        
        if (capture_TO_999(sParagraph, sRegExPrefix, sRegExSuffix, iPrevGroupNumber, bMayLeave)){
             tempRes.setBestResult( iPrevGroupNumber[0]);    
             return variables().prepareReturn(true);
        }
        
        tempRes.resetInitialValues(iPrevGroupNumber);
        String sPattern = ConstRegEx.groupBrackets("[1-9][0-9]?[0-9]?", iPrevGroupNumber);
        Pattern patternLex = Pattern.compile(
                ConstRegEx.prefixWordSuffix(sPattern, sRegExPrefix, sRegExSuffix));
        boolean bResult =
                findAll.printAllMatchResult(patternLex, sParagraph, false);
        if (bResult) {
            try {
                int iN = Integer.parseInt(variables().tempVariables.matchResult.group(iPrevGroupNumber[0]));
                variables().tempVariables.ordWords.set_hun(iN / 100);
                variables().tempVariables.ordWords.set_ty((iN % 100) / 10);
                variables().tempVariables.ordWords.set_unit((iN % 100) % 10);

                variables().saveCurrentMatch(iPrevGroupNumber[0]);
                tempRes.setBestResult(iPrevGroupNumber[0]);
            }catch(Exception e){
                  if (!bMayLeave){ Const.writeSpecificError(e, sParagraph);}
            }
        }
        
        tempRes.prepareReturn(iPrevGroupNumber);
        return variables().prepareReturn(tempRes.bAtLeastOneResult);
    }
    
    public boolean capture_THOU_Case1(String sParagraph, String sRegExPrefix, String sRegExSuffix, 
                int[] iPrevGroupNumber,boolean bMayLeave){
/*
$THOU
 = qr/(?{$ordWords = new OrdinalNumber();})
 (($AANONE$WS(thousand))(?{ordWords->thou(1);}))|
/i;		
#a thousand, one thousand, 3 hundred thousand, 300 thousand ...  
 */        
        variables().startCapture();
        variables().tempVariables.ordWords = new OrdinalNumber();
        int iResGr = iPrevGroupNumber[0] + 1;
        String sPattern = ConstRegEx.groupBrackets(
                ConstRegEx.groupBrackets(
                    variables().hashtbLexValues.get("AANONE")
                , iPrevGroupNumber) +
                ConstRegEx.groupBrackets(
                    ConstRegEx.WS
                , iPrevGroupNumber)+
                ConstRegEx.groupBrackets("thousand", iPrevGroupNumber)                
                , iPrevGroupNumber);
        Pattern patternLex = Pattern.compile(
                ConstRegEx.prefixWordSuffix(sPattern, sRegExPrefix, sRegExSuffix),
                Pattern.CASE_INSENSITIVE);
        boolean bResult =
                findAll.printAllMatchResult(patternLex, sParagraph, false);
        if (bResult) {
            try {
                variables().tempVariables.ordWords.set_thou(1);
                variables().saveCurrentMatch(iResGr);
            }catch(Exception e){
               if (!bMayLeave){ Const.writeSpecificError(e, sParagraph);}
               variables().tempVariables.sCurrentMatch = "";
            }
        }        
        return variables().prepareReturn(bResult);
        
    }
    
    public boolean capture_THOU_Case2(String sParagraph, String sRegExPrefix, String sRegExSuffix, 
                int[] iPrevGroupNumber,boolean bMayLeave){
/*
$THOU
 = qr/
 (($TO_999_MIX$WS(thousand))
 (?{$ordWords->thou($ordWords->hun()*100+$ordWords->ty()*10+$ordWords->unit());}))/i;		
#a thousand, one thousand, 3 hundred thousand, 300 thousand ...  

 */        
        variables().startCapture();
        int[] iTemp = new int[1]; iTemp[0] = 0;
        String sNewSuffix =
                ConstRegEx.groupBrackets(
                    ConstRegEx.groupBrackets(ConstRegEx.WS, iTemp) +
                    ConstRegEx.groupBrackets("thousand", iTemp),
                iTemp) +
                sRegExSuffix;
        boolean bResult = capture_TO_99_MIX(sParagraph, sRegExPrefix, sNewSuffix, iPrevGroupNumber, bMayLeave);
        if (bResult){
            try {
                int iHun = variables().tempVariables.ordWords.get_hun();
                int iTy = variables().tempVariables.ordWords.get_ty();
                int iUnit = variables().tempVariables.ordWords.get_unit();
                variables().tempVariables.ordWords.set_thou(iHun * 100 + iTy * 10 + iUnit);

                variables().concatenateSuffix(iPrevGroupNumber[0] + 1);
                iPrevGroupNumber[0] += iTemp[0];
            }catch(Exception e){
                    if (!bMayLeave){ Const.writeSpecificError(e, sParagraph);}
            }
        }
        return variables().prepareReturn(bResult);
    }
    public boolean capture_THOU(String sParagraph, String sRegExPrefix, String sRegExSuffix, 
                int[] iPrevGroupNumber,boolean bMayLeave){
/*
$THOU
 = qr/(?{$ordWords = new OrdinalNumber();})
 (($AANONE$WS(thousand))(?{ordWords->thou(1);}))|
 (($TO_999_MIX$WS(thousand))
 (?{$ordWords->thou($ordWords->hun()*100+$ordWords->ty()*10+$ordWords->unit());}))/i;		
#a thousand, one thousand, 3 hundred thousand, 300 thousand ...  

 */        
        variables().startCapture();
        TempResultVariables tempRes = new TempResultVariables(variables());
        tempRes.setInitialValues(iPrevGroupNumber);
        
        if (capture_THOU_Case1(sParagraph, sRegExPrefix, sRegExSuffix, iPrevGroupNumber, bMayLeave)){
              tempRes.setBestResult( iPrevGroupNumber[0]);    
              return variables().prepareReturn(true);
        }
        tempRes.resetInitialValues(iPrevGroupNumber);
        if (capture_THOU_Case2(sParagraph, sRegExPrefix, sRegExSuffix, iPrevGroupNumber, bMayLeave)){
            tempRes.setBestResult( iPrevGroupNumber[0]);    
            return variables().prepareReturn(true);
        }
        
        tempRes.prepareReturn(iPrevGroupNumber);
        return variables().prepareReturn(tempRes.bAtLeastOneResult);
    }
    public boolean capture_THOU_999(String sParagraph, String sRegExPrefix, String sRegExSuffix, 
                int[] iPrevGroupNumber,boolean bMayLeave){
/*
$THOU_999			= 
 qr/($THOU)(?{$tempT = $ordWords->thou();$ordWords = new OrdinalNumber();})
 ($WS$TO_999_MIX)?(?{$ordWords->thou($tempT);})/i;						
				#four thousand three hundred and twenty two ...  
*/        
        variables().startCapture();
        String sNewSuffix = ConstRegEx.REST_ANY + sRegExSuffix;
        boolean bResult = capture_THOU(sParagraph, sRegExPrefix, sNewSuffix, iPrevGroupNumber, bMayLeave);
        if (bResult){
            try {
                TempSaveCurrentMatch tmpMatch = new TempSaveCurrentMatch(variables());
                sParagraph = tmpMatch.saveMatchAndMoveOverAndReset(sParagraph);
                String sNewPrefix = ConstRegEx.newPrefix(iPrevGroupNumber);
                
                variables().tempVariables.stempT = variables().tempVariables.ordWords.get_thou().toString();
                variables().tempVariables.ordWords = new OrdinalNumber();
                
                bResult = capture_WS_TO_999_MIX_op(sParagraph,  
                        sNewPrefix, sRegExSuffix, iPrevGroupNumber, true);
                if (bResult) 
                {
                    try {
                        variables().tempVariables.ordWords.set_thou(
                                Integer.parseInt(variables().tempVariables.stempT));

                        tmpMatch.restoreCurrentMatch();    
                    }catch(Exception e){
                        if (!bMayLeave){ Const.writeSpecificError(e, sParagraph);}
                    }
                }
                return variables().prepareReturn(true);
            }catch(Exception e){
                    if (!bMayLeave){ Const.writeSpecificError(e, sParagraph);}
            }
        }
        return variables().prepareReturn(bResult);
    }
    public boolean capture_THOU_99(String sParagraph, String sRegExPrefix, String sRegExSuffix, 
                int[] iPrevGroupNumber,boolean bMayLeave){
/*
$THOU_99=
 qr/($THOU)(?{$tempT = $ordWords->thou();$ordWords = new OrdinalNumber();})
 ($WS$AND_TO_99)?(?{$ordWords->thou($tempT);})/i;						
				#four thousand, four thousand and twenty two ...  

 */        
        variables().startCapture();
        String sNewSuffix = ConstRegEx.REST_ANY + sRegExSuffix;
        boolean bResult = capture_THOU(sParagraph, sRegExPrefix, sNewSuffix, iPrevGroupNumber, bMayLeave);
        if (bResult){
            try {
                TempSaveCurrentMatch tmpMatch = new TempSaveCurrentMatch(variables());
                sParagraph = tmpMatch.saveMatchAndMoveOverAndReset(sParagraph);
                String sNewPrefix = ConstRegEx.newPrefix(iPrevGroupNumber);
                
                variables().tempVariables.stempT =
                        variables().tempVariables.ordWords.get_thou().toString();
                variables().tempVariables.ordWords = new OrdinalNumber();
                bResult = capture_WS_AND_TO_99_op(sParagraph,  
                        sNewPrefix, sRegExSuffix, iPrevGroupNumber, true);
                if (bResult) 
                {
                    try {
                        variables().tempVariables.ordWords.set_thou(Integer.parseInt(variables().tempVariables.stempT));

                        tmpMatch.restoreCurrentMatch();
                    }catch(Exception e){
                        if (!bMayLeave){ Const.writeSpecificError(e, sParagraph);}
                    }
                }
                return variables().prepareReturn(true);
            }catch(Exception e){
                    if (!bMayLeave){ Const.writeSpecificError(e, sParagraph);}
            }
        }
        return variables().prepareReturn(bResult);
    }
    public boolean capture_TEXTNUM(String sParagraph, String sRegExPrefix, String sRegExSuffix, 
                int[] iPrevGroupNumber,boolean bMayLeave){
/*
$TEXTNUM			= qr/$THOU_999|$THOU_99|$TO_999/;
				#up to 999999
 */        
        variables().startCapture();
        TempResultVariables tempRes = new TempResultVariables(variables());
        tempRes.setInitialValues(iPrevGroupNumber);

        if (capture_THOU_999(sParagraph, sRegExPrefix, sRegExSuffix, iPrevGroupNumber, bMayLeave)){
            tempRes.setBestResult( iPrevGroupNumber[0]);    
            return variables().prepareReturn(true);
        }
        tempRes.resetInitialValues(iPrevGroupNumber);
        if (capture_THOU_99(sParagraph, sRegExPrefix, sRegExSuffix, iPrevGroupNumber, bMayLeave)){
            tempRes.setBestResult( iPrevGroupNumber[0]);    
            return variables().prepareReturn(true);
        }
        tempRes.resetInitialValues(iPrevGroupNumber);
        if ( capture_TO_999(sParagraph, sRegExPrefix, sRegExSuffix, iPrevGroupNumber, bMayLeave) ){
            tempRes.setBestResult( iPrevGroupNumber[0]);    
            return variables().prepareReturn(true);
        }
        tempRes.prepareReturn(iPrevGroupNumber);
        return variables().prepareReturn(tempRes.bAtLeastOneResult);
    }
    
    public boolean capture_YEAR_ORD(String sParagraph, String sRegExPrefix, String sRegExSuffix, 
                int[] iPrevGroupNumber,boolean bMayLeave){
/*$YEAR_ORD			= 
 qr/(($TEEN)(?{$temp = $ordWords->val();$ordWords = new OrdinalNumber();}))$WS(($TO_99)(?{$ordWords->hun($temp);}))/i;
 */
        variables().startCapture();
        int[] iTempGr = new int[1]; iTempGr[0] = 0;
        String sNewSuffix = ConstRegEx.groupBrackets(ConstRegEx.WS, iTempGr)+
                ConstRegEx.REST_ANY + sRegExSuffix;
        String sNewPrefix;
        String sTeen ;
        
        if (capture_TEEN(sParagraph, sRegExPrefix, sNewSuffix, iPrevGroupNumber, bMayLeave)){
            try {
                variables().concatenateSuffix(iPrevGroupNumber[0] + 1);

                TempSaveCurrentMatch tmpMatch = new TempSaveCurrentMatch(variables());
                sParagraph = tmpMatch.saveMatchAndMoveOverAndReset(sParagraph);

                sTeen = variables().tempVariables.matchResult.group(iPrevGroupNumber[0]);
                sNewPrefix = ConstRegEx.newPrefix(iPrevGroupNumber) +
                        "("//iPrevGroupNumber[0]++;
                        ;
                iPrevGroupNumber[0]++;
                if (capture_TO_99(sParagraph,  sNewPrefix, sRegExSuffix, iPrevGroupNumber, bMayLeave)) {
                    try {
                        //(?{$temp = $ordWords->val();$ordWords = new OrdinalNumber();}))
                        variables().tempVariables.stemp = sTeen;
                        //Variables().tempVariables.ordWords.get_val().toString(); //because 
                        //capture_TEEN( ...., false)
                        variables().tempVariables.ordWords = new OrdinalNumber();
                        variables().tempVariables.ordWords.set_hun(
                                Integer.parseInt(variables().tempVariables.stemp));
                        
                        tmpMatch.restoreCurrentMatch();
                    }catch(Exception e){
                        if (!bMayLeave){ Const.writeSpecificError(e, sParagraph);}
                    }
                    return variables().prepareReturn(true);
                }
            }catch(Exception e){
                    if (!bMayLeave){ Const.writeSpecificError(e, sParagraph);}
            }
        }
        return variables().prepareReturn(false);
    }
    public boolean capture_PARTS_Case1(String sParagraph, String sRegExPrefix, String sRegExSuffix,
                int[] iPrevGroupNumber,boolean bMayLeave){
/*
 $PARTS				= 
 qr/($AANONE$WS)?(($PART)(?{$tempPart = "1".$map_val{lc($^N)};}))/i;
 */        
        variables().startCapture();
        int iResGr = iPrevGroupNumber[0] + 1;
        String sPattern = ConstRegEx.groupBrackets(
                    ConstRegEx.groupBrackets(
                        ConstRegEx.groupBrackets(
                            variables().hashtbLexValues.get("AANONE")
                        , iPrevGroupNumber)+
                        ConstRegEx.groupBrackets(
                            ConstRegEx.WS
                        , iPrevGroupNumber)
                        , iPrevGroupNumber)+"?"+
                ConstRegEx.groupBrackets(
                   variables().hashtbLexValues.get("PART")
                , iPrevGroupNumber)
                , iPrevGroupNumber);
          Pattern patternLex = Pattern.compile(
                ConstRegEx.prefixWordSuffix(sPattern, sRegExPrefix, sRegExSuffix),
                Pattern.CASE_INSENSITIVE);
        boolean bResult =
                findAll.printAllMatchResult(patternLex, sParagraph, false);
        if (bResult) {
            try {
                variables().tempVariables.stempPart = "1" + variables().getMapHashValue(
                        variables().getResult_LowerCase(iPrevGroupNumber[0])
                        );
                variables().saveCurrentMatch(iResGr);
            } catch(Exception e){
                    if (!bMayLeave){ Const.writeSpecificError(e, sParagraph);}
                    variables().tempVariables.sCurrentMatch = "";
            }
        }
        return variables().prepareReturn(bResult);
    }
    
    public boolean capture_PARTS_Case2(String sParagraph, String sRegExPrefix, String sRegExSuffix,
                int[] iPrevGroupNumber,boolean bMayLeave){
/*
$PARTS				= 
 qr/|
 $UNIT$WS(($PARTP)(?{$tempPart = $ordWords->val().$map_val{lc($^N)};}))/i;
 */        
        variables().startCapture();
        int[] iPart_Number =new int[1]; iPart_Number[0]=0;
        String sNewSuffix = 
                ConstRegEx.groupBrackets(
                    ConstRegEx.groupBrackets(
                        ConstRegEx.WS
                        , iPart_Number)+
                    ConstRegEx.groupBrackets(
                        variables().hashtbLexValues.get("PARTP")
                        , iPart_Number)
                , iPart_Number)+
                sRegExSuffix;
        boolean bResult = capture_UNIT(sParagraph, sRegExPrefix, sNewSuffix, iPrevGroupNumber, bMayLeave);
        if (bResult){
            try {
                variables().concatenateSuffix(iPrevGroupNumber[0] + 1);
                iPrevGroupNumber[0] += iPart_Number[0];
                variables().tempVariables.stempPart =
                        variables().tempVariables.ordWords.get_val() +
                        variables().getMapHashValue(
                            variables().getResult_LowerCase(iPrevGroupNumber[0])
                        );
            }catch(Exception e){
                    if (!bMayLeave){ Const.writeSpecificError(e, sParagraph);}
            }
        }
        return variables().prepareReturn(bResult);
    }
    public boolean capture_PARTS(String sParagraph, String sRegExPrefix, String sRegExSuffix,
                int[] iPrevGroupNumber,boolean bMayLeave){
/*
$PARTS				= 
 qr/($AANONE$WS)?(($PART)(?{$tempPart = "1".$map_val{lc($^N)};}))|
 $UNIT$WS(($PARTP)(?{$tempPart = $ordWords->val().$map_val{lc($^N)};}))/i;
 */        
        variables().startCapture();
        TempResultVariables tempRes = new TempResultVariables(variables());
        tempRes.setInitialValues(iPrevGroupNumber);
        
        if (capture_PARTS_Case1(sParagraph, sRegExPrefix, sRegExSuffix, iPrevGroupNumber, bMayLeave)){
            tempRes.setBestResult( iPrevGroupNumber[0]);    
            return variables().prepareReturn(true);
        }
        
        tempRes.resetInitialValues(iPrevGroupNumber);
        if ( capture_PARTS_Case2(sParagraph, sRegExPrefix, sRegExSuffix, iPrevGroupNumber, bMayLeave) ){
            tempRes.setBestResult( iPrevGroupNumber[0]);    
            return variables().prepareReturn(true);
        }
        tempRes.prepareReturn(iPrevGroupNumber);
        return variables().prepareReturn(tempRes.bAtLeastOneResult);
    }
    public boolean capture_AND_PARTS(String sParagraph, String sRegExPrefix, String sRegExSuffix,
                int[] iPrevGroupNumber,boolean bMayLeave){
/*
$AND_PARTS= qr/
 ($AND$WS)?(?{$ordWords = new OrdinalNumber();})$PARTS(?{$tempPart = "+$tempPart";})/i;
 */        
        variables().startCapture();
        int iPref = iPrevGroupNumber[0]+1;
        String sNewPrefix = sRegExPrefix+
                ConstRegEx.groupBrackets(
                    ConstRegEx.groupBrackets(
                        ConstRegEx.groupBrackets(
                            variables().hashtbLexValues.get("AND")
                            , iPrevGroupNumber
                        )+
                        ConstRegEx.groupBrackets(
                            ConstRegEx.WS
                            , iPrevGroupNumber)                    
                    , iPrevGroupNumber)+"?"
                , iPrevGroupNumber);
        //to delete: 2008.09.29
/*        OrdinalNumber ordTemp =new OrdinalNumber();
        ordTemp.copy(Variables().tempVariables.ordWords);/*/
        variables().tempVariables.ordWords = new OrdinalNumber();
        boolean bResult = capture_PARTS(sParagraph,  sNewPrefix, sRegExSuffix, iPrevGroupNumber, bMayLeave);
        if (bResult){
            try {
                variables().tempVariables.stempPart = "+" + variables().tempVariables.stempPart;

                variables().concatenatePrefix(iPref);
            }catch(Exception e){
                    if (!bMayLeave){ Const.writeSpecificError(e, sParagraph);}
            }
        }
        //to delete: 2008.09.29
        /*else{
            Variables().tempVariables.ordWords.copy(ordTemp);
        }*/
        return variables().prepareReturn(bResult);
    }
    //#=========== TIMEX RULES ========================== 
    public boolean capture_SOME_PREPS(String sParagraph, String sRegExPrefix, String sRegExSuffix,
                int[] iPrevGroupNumber,boolean bMayLeave){
/*
$SOME_PREPS			= qr/$PREP/i;
 */        
        variables().startCapture();
        String sPattern = ConstRegEx.groupBrackets(
                variables().hashtbLexValues.get("PREP"),
                iPrevGroupNumber);
                
        Pattern patternLex = Pattern.compile(
                ConstRegEx.prefixWordSuffix(sPattern, sRegExPrefix, sRegExSuffix), 
                Pattern.CASE_INSENSITIVE);
        boolean bRes = findAll.printAllMatchResult(patternLex, sParagraph, false); 
        if (bRes){
            variables().saveCurrentMatch(iPrevGroupNumber[0]);
        }
        return variables().prepareReturn(bRes);
    }
    public boolean capture_SOME_CONJS(String sParagraph, String sRegExPrefix, String sRegExSuffix, 
                    int[] iPrevGroupNumber,boolean bMayLeave) {
/*
$SOME_CONJS			= qr/$CONJ/i;
 */        
        variables().startCapture();
        String sPattern = ConstRegEx.groupBrackets(
                variables().hashtbLexValues.get("CONJ"), iPrevGroupNumber);
                
        Pattern patternLex = Pattern.compile(
                ConstRegEx.prefixWordSuffix(sPattern, sRegExPrefix, sRegExSuffix), 
                Pattern.CASE_INSENSITIVE);
        boolean bRes = findAll.printAllMatchResult(patternLex, sParagraph, false);
        if (bRes) {
            variables().saveCurrentMatch(iPrevGroupNumber[0]);
        }
        return variables().prepareReturn(bRes);
        
    }
    
    //#=================== MONTHS ======================== 
    public boolean capture_MONTH_NAME_FULL_LEX(String sParagraph, String sRegExPrefix, 
            String sRegExSuffix, int[] iPrevGroupNumber,boolean bMayLeave){
/*
 $MONTH_NAME_FULL_LEX		= 
 qr/(($MON)(?{$tempMonth = $map_val{lc($^N)};}))/i;
 */     
        variables().startCapture();
        String sPattern = ConstRegEx.groupBrackets(
                variables().hashtbLexValues.get("MON").toString(),
                iPrevGroupNumber
                );
        Pattern patternLex = Pattern.compile(
                ConstRegEx.prefixWordSuffix(sPattern, sRegExPrefix, sRegExSuffix), 
                Pattern.CASE_INSENSITIVE);
        boolean bResult=findAll.printAllMatchResult(patternLex, sParagraph, false);
        if (bResult){
            try {
                variables().tempVariables.stempMonth =
                        variables().getMapHashValue(
                            variables().getResult_LowerCase(iPrevGroupNumber[0])
                        );
                variables().saveCurrentMatch(iPrevGroupNumber[0]);
            }catch(Exception e){
                    if (!bMayLeave){ Const.writeSpecificError(e, sParagraph);}
                    variables().tempVariables.sCurrentMatch = "";
            }
        }
        return variables().prepareReturn(bResult);
    }
    
    public boolean capture_MONTH_NAME_ABBREV_LEX(String sParagraph, String sRegExPrefix, String sRegExSuffix, int[] iPrevGroupNumber,boolean bMayLeave){
/*$MONTH_NAME_ABBREV_LEX		
    = qr/(($MONA)(?{$tempMonth = $map_val{lc($^N)};}))\.?/i;	
    #Jan., Jan, Feb., Feb, ...("^[A-Z][A-z]*\.?$" attached in file) */
        variables().startCapture();
        String sNewPref = sRegExPrefix + "("; String sNewSuf = ")"+sRegExSuffix;
        iPrevGroupNumber[0]++;//brackets
        int iResGr = iPrevGroupNumber[0];

        String sPattern = ConstRegEx.groupBrackets(
                variables().hashtbLexValues.get("MONA").toString(),
                iPrevGroupNumber
                ) +"\\.?";
        Pattern patternLex = Pattern.compile(
                ConstRegEx.prefixWordSuffix(sPattern, sNewPref, sNewSuf),
                Pattern.CASE_INSENSITIVE);
        boolean bResult=findAll.printAllMatchResult(patternLex, sParagraph, false);
        if (bResult){
            try {
                variables().tempVariables.stempMonth =
                        variables().getMapHashValue(
                            variables().getResult_LowerCase(iPrevGroupNumber[0])
                        );
                variables().saveCurrentMatch(iResGr);
            }catch(Exception e){
                    if (!bMayLeave){ Const.writeSpecificError(e, sParagraph);}
                    variables().tempVariables.sCurrentMatch = "";
            }
        }
        return variables().prepareReturn(bResult);        
    }
    
    public boolean capture_MONTH_NAME_IN_CONTEXT(String sParagraph, String sRegExPrefix, String sRegExSuffix, int[] iPrevGroupNumber,boolean bMayLeave){
        variables().startCapture();
        TempResultVariables tempRes = new TempResultVariables(variables());
        tempRes.setInitialValues(iPrevGroupNumber);

        if (capture_MONTH_NAME_FULL_LEX(sParagraph, sRegExPrefix, sRegExSuffix, iPrevGroupNumber, bMayLeave)){
             tempRes.setBestResult( iPrevGroupNumber[0]); 
             return variables().prepareReturn(true);
        }
        tempRes.resetInitialValues(iPrevGroupNumber);
        if (capture_MONTH_NAME_ABBREV_LEX(sParagraph, sRegExPrefix, sRegExSuffix, iPrevGroupNumber, bMayLeave)){
            tempRes.setBestResult( iPrevGroupNumber[0]);   
            return variables().prepareReturn(true);
        }
        tempRes.prepareReturn(iPrevGroupNumber);
        return variables().prepareReturn(tempRes.bAtLeastOneResult);
    }
    
// #=================== SEASONS =======================     
    public boolean capture_SEASON_NAME(String sParagraph, String sRegExPrefix, String sRegExSuffix, int[] iPrevGroupNumber,boolean bMayLeave) {
/*
 $SEASON_NAME			= 
 qr/($SEA)(?{$tempSeasonName = $map_val{lc($^N)};})/;
				#Season names 
 */        
        variables().startCapture();
        String sPattern = ConstRegEx.groupBrackets(variables().hashtbLexValues.get("SEA"), iPrevGroupNumber);
        Pattern patternLex = Pattern.compile(
                ConstRegEx.prefixWordSuffix(sPattern, sRegExPrefix, sRegExSuffix));
        boolean bResult=findAll.printAllMatchResult(patternLex, sParagraph, false);
        if (bResult){
            try {
                variables().tempVariables.stempSeasonName =
                        variables().getMapHashValue(
                            variables().getResult_LowerCase(iPrevGroupNumber[0])
                        );
                variables().saveCurrentMatch(iPrevGroupNumber[0]);
            }catch(Exception e){
                    if (!bMayLeave){ Const.writeSpecificError(e, sParagraph);}
                    variables().tempVariables.sCurrentMatch = "";
            }
        }
        return variables().prepareReturn(bResult);          
    }
    //#=================== TIME UNITS ==================== 
    public boolean capture_TIME_UNIT_LEX(String sParagraph,
            String sRegExPrefix, String sRegExSuffix, int[] iPrevGroupNumber
            ,boolean bMayLeave ){
/*
$TIME_UNIT_LEX= 
 qr/(?{$tempPart = '';})($TU)(?{$tempTX = $map_val{lc($^N)."_TU"};})
 ($WS$AND_PARTS)?(?{$tempTU = "$tempPart$tempTX"})/i;
				#minute, hour, half-hour 
 */        
        variables().startCapture();
        variables().tempVariables.stempPart = "";
        String sPattern = ConstRegEx.groupBrackets(
                variables().hashtbLexValues.get("TU"), iPrevGroupNumber);
        Pattern patternLex = Pattern.compile(
                ConstRegEx.prefixWordSuffix(sPattern, sRegExPrefix, sRegExSuffix),
                Pattern.CASE_INSENSITIVE);
        boolean bResult=findAll.printAllMatchResult(patternLex, sParagraph, false);
        if (bResult){
            try {
                variables().tempVariables.stempTX =
                        variables().getMapHashValue(
                            variables().getResult_LowerCase(iPrevGroupNumber[0])+
                            "_TU"
                        );
                variables().saveCurrentMatch(iPrevGroupNumber[0]);
                
                TempSaveCurrentMatch tmpMatch = new TempSaveCurrentMatch(variables());
                sParagraph = tmpMatch.saveMatchAndMoveOverAndReset(sParagraph);
                String sNewPref = ConstRegEx.newPrefix(iPrevGroupNumber);
                boolean bResult2 = capture_WS_AND_PART_op(sParagraph,
                        sNewPref, sRegExSuffix, iPrevGroupNumber, bMayLeave);      
                if (bResult2)
                {
                    try {
                        //(?{$tempTU = "$tempPart$tempTX"})/i;
                        variables().tempVariables.stempTU =
                                variables().tempVariables.stempPart+
                                variables().tempVariables.stempTX;
                        
                        tmpMatch.restoreCurrentMatch();
                    } catch (Exception e) {
                        if (!bMayLeave) {Const.writeSpecificError(e, sParagraph);}
                    }
                }
                
            }catch(Exception e){
                    if (!bMayLeave){ Const.writeSpecificError(e, sParagraph);}
                    variables().tempVariables.sCurrentMatch = "";
            }
        }
        return variables().prepareReturn(bResult);          
    }    
    
    public boolean capture_PLURAL_TIME_UNIT_LEX(String sParagraph,
            String sRegExPrefix, String sRegExSuffix, int[] iPrevGroupNumber,boolean bMayLeave ){
/*
$PLURAL_TIME_UNIT_LEX		= qr/($TUP)(?{$tempTU = $map_val{lc($^N)};})/i;
				#minutes, hours 
 */        
        variables().startCapture();
        String sPattern = ConstRegEx.groupBrackets(
                variables().hashtbLexValues.get("TUP"), iPrevGroupNumber);
        Pattern patternLex = Pattern.compile(
                ConstRegEx.prefixWordSuffix(sPattern, sRegExPrefix, sRegExSuffix),
                Pattern.CASE_INSENSITIVE);
        boolean bResult=findAll.printAllMatchResult(patternLex, sParagraph, false);
        if (bResult){
            try {
                variables().tempVariables.stempTU =
                        variables().getMapHashValue(
                            variables().getResult_LowerCase(iPrevGroupNumber[0])
                        );
                variables().saveCurrentMatch(iPrevGroupNumber[0]);
            }catch(Exception e){
                    if (!bMayLeave){ Const.writeSpecificError(e, sParagraph);}
                    variables().tempVariables.sCurrentMatch = "";
            }
        }
        return variables().prepareReturn(bResult);          
    }    
    public boolean capture_DAY_PART(String sParagraph,
            String sRegExPrefix, String sRegExSuffix, int[] iPrevGroupNumber,boolean bMayLeave ){
/*
$DAY_PART			= qr/($DP)(?{$tempTU = $map_val{lc($^N)};})/i;
				#morning, afternoon, evening etc. 
 */       
        variables().startCapture();
        String sPattern = ConstRegEx.groupBrackets(
                variables().hashtbLexValues.get("DP"), iPrevGroupNumber);
       Pattern patternLex = Pattern.compile(
                ConstRegEx.prefixWordSuffix(sPattern, sRegExPrefix, sRegExSuffix),
                Pattern.CASE_INSENSITIVE
                );
        boolean bResult= findAll.printAllMatchResult(patternLex, sParagraph, false);
        if (bResult){
            try {
                variables().tempVariables.stempTU = 
                        variables().getMapHashValue(
                            variables().getResult_LowerCase(iPrevGroupNumber[0])
                );
                variables().saveCurrentMatch(iPrevGroupNumber[0]);
            } catch(Exception e){
                    if (!bMayLeave){ Const.writeSpecificError(e, sParagraph);}
                    variables().tempVariables.sCurrentMatch = "";
            }
        }
        return variables().prepareReturn(bResult);            
    }    
    public boolean capture_PLURAL_DAY_PART(String sParagraph,
            String sRegExPrefix, String sRegExSuffix, int[] iPrevGroupNumber,boolean bMayLeave ){
/*
$PLURAL_DAY_PART		= qr/($DPP)(?{$tempTU = $map_val{lc($^N)};})/i;
				#mornings, nights etc. 
 */        
        variables().startCapture();
        String sPattern = ConstRegEx.groupBrackets(
                variables().hashtbLexValues.get("DPP"), iPrevGroupNumber);
       Pattern patternLex = Pattern.compile(
                ConstRegEx.prefixWordSuffix(sPattern, sRegExPrefix, sRegExSuffix),
                Pattern.CASE_INSENSITIVE
                );
        boolean bResult= findAll.printAllMatchResult(patternLex, sParagraph, false);
        if (bResult){
            try {
                variables().tempVariables.stempTU = 
                        variables().getMapHashValue(
                            variables().getResult_LowerCase(iPrevGroupNumber[0])
                );
                variables().saveCurrentMatch(iPrevGroupNumber[0]);
            } catch(Exception e){
                    if (!bMayLeave){ Const.writeSpecificError(e, sParagraph);}
                    variables().tempVariables.sCurrentMatch = "";
            }
        }
        return variables().prepareReturn(bResult);            
    }    
    
    public boolean capture_TIME_NAME(String sParagraph,
            String sRegExPrefix, String sRegExSuffix, int[] iPrevGroupNumber,boolean bMayLeave ){
/*
$TIME_NAME= 
 qr/($TN)(?{$tempHour = $map_val{lc($^N)};$tempMin = '';})/i;
				#noon, midday, etc. 
 */        
        variables().startCapture();
        String sPattern = ConstRegEx.groupBrackets(
                variables().hashtbLexValues.get("TN"), iPrevGroupNumber);
       Pattern patternLex = Pattern.compile(
                ConstRegEx.prefixWordSuffix(sPattern, sRegExPrefix, sRegExSuffix),
                Pattern.CASE_INSENSITIVE
                );
        boolean bResult= findAll.printAllMatchResult(patternLex, sParagraph, false);
        if (bResult){
            try {
                variables().tempVariables.stempHour = 
                        variables().getMapHashValue(
                            variables().getResult_LowerCase(iPrevGroupNumber[0])
                            );
                variables().tempVariables.stempMin = "";
                variables().saveCurrentMatch(iPrevGroupNumber[0]);
            } catch(Exception e){
                    if (!bMayLeave){ Const.writeSpecificError(e, sParagraph);}
                    variables().tempVariables.sCurrentMatch = "";
            }
        }
        return variables().prepareReturn(bResult);            
    }     
//				#============ QUANTITY ================ 
    public boolean capture_ALL_QUANT(String sParagraph,
            String sRegExPrefix, String sRegExSuffix, int[] iPrevGroupNumber,boolean bMayLeave ){
/*
$ALL_QUANT			= qr/($QUANT$WS?)+(?{$ordWords->val("X");})/;
				#several thousands (of), hundreds of thousands (of) .... 
 */        
        variables().startCapture();
        String sNewPref = sRegExPrefix + "("; String sNewSuf = ")"+sRegExSuffix;
        iPrevGroupNumber[0]++;//brackets
        int iResGr = iPrevGroupNumber[0];

        String sPattern = ConstRegEx.groupBrackets(
                    ConstRegEx.groupBrackets(
                        variables().hashtbLexValues.get("QUANT"), iPrevGroupNumber)+
                    ConstRegEx.groupBrackets(ConstRegEx.WS, iPrevGroupNumber)+"?"
                , iPrevGroupNumber) + "+";
        Pattern patternLex = Pattern.compile(
                ConstRegEx.prefixWordSuffix(sPattern, sNewPref, sNewSuf)
                );
        boolean bResult= findAll.printAllMatchResult(patternLex, sParagraph, false);
        if (bResult){
            try {
                //Variables().tempVariables.ordWords.set_val(Integer.parseInt("X"));
                variables().tempVariables.ordWords.set_val(-9);
                variables().saveCurrentMatch(iResGr);
            } catch(Exception e){
                    if (!bMayLeave){ Const.writeSpecificError(e, sParagraph);}
                    variables().tempVariables.sCurrentMatch = "";
            }
        }
        return variables().prepareReturn(bResult);
    }
    //#============== QUANT + DATE/TIME UNITS ============ 
    public boolean capture_QUANTT(String sParagraph,
            String sRegExPrefix, String sRegExSuffix, int[] iPrevGroupNumber,boolean bMayLeave){
/*
$QUANTT				= qr/$CARD|$TEXTNUM|$ALL_QUANT/i;
				#fie C=CD fie PHR=CD fie PHR=QUANT fie PHR=RANGE 

 */     
       variables().startCapture();
       TempResultVariables tempRes = new TempResultVariables(variables());
       tempRes.setInitialValues(iPrevGroupNumber);
       
       if (capture_CARD(sParagraph, sRegExPrefix, sRegExSuffix, iPrevGroupNumber, bMayLeave)) {
            tempRes.setBestResult( iPrevGroupNumber[0]);    
            return variables().prepareReturn(true);
       }
       
       tempRes.resetInitialValues(iPrevGroupNumber);
       if (capture_TEXTNUM(sParagraph, 
               sRegExPrefix, sRegExSuffix, iPrevGroupNumber, bMayLeave)){
           tempRes.setBestResult( iPrevGroupNumber[0]);    
           return variables().prepareReturn(true);
       }
        
       tempRes.resetInitialValues(iPrevGroupNumber);
       if (capture_ALL_QUANT(sParagraph, sRegExPrefix, sRegExSuffix, iPrevGroupNumber, bMayLeave)){
           tempRes.setBestResult( iPrevGroupNumber[0]);    
           return variables().prepareReturn(true);
       }
       tempRes.prepareReturn(iPrevGroupNumber);
       return variables().prepareReturn(tempRes.bAtLeastOneResult);
    }    
    //#======= LAST/PAST/NEXT/FIRST DATE ================= 
 public boolean capture_LAST_PAST_WORD(String sParagraph,
            String sRegExPrefix, String sRegExSuffix, int[] iPrevGroupNumber,boolean bMayLeave) {
/*
$LAST_PAST_WORD			= qr/(($FP)(?{$tempLP = $map_val{lc($^N)."_FP"};}))/i;
				#last, next, past, following, previous 
 */        
        variables().startCapture();
        String sPattern = ConstRegEx.groupBrackets(
                variables().hashtbLexValues.get("FP"), iPrevGroupNumber);
        Pattern patternLex = Pattern.compile(
                ConstRegEx.prefixWordSuffix(sPattern, sRegExPrefix, sRegExSuffix),
                Pattern.CASE_INSENSITIVE
                );
        boolean bResult= findAll.printAllMatchResult(patternLex, sParagraph, false);
        if (bResult){
            try {
                variables().tempVariables.stempLP = 
                        variables().getMapHashValue(
                            variables().getResult_LowerCase(iPrevGroupNumber[0]) 
                            + "_FP"
                ) ;
                variables().saveCurrentMatch(iPrevGroupNumber[0]);
            } catch(Exception e){
                    if (!bMayLeave){ Const.writeSpecificError(e, sParagraph);}
                    variables().tempVariables.sCurrentMatch = "";
            }
        }
        return variables().prepareReturn(bResult);        
    }         
    public boolean capture_LAST_NEXT_WORD(String sParagraph,
            String sRegExPrefix, String sRegExSuffix, int[] iPrevGroupNumber,boolean bMayLeave) {
/*
$LAST_NEXT_WORD			= qr/(($LN)(?{$tempLN = $map_val{lc($^N)};}))/i;
				#last, next, this 
 */        
        variables().startCapture();
        String sPattern = ConstRegEx.groupBrackets(
                variables().hashtbLexValues.get("LN"), iPrevGroupNumber);
        Pattern patternLex = Pattern.compile(
                ConstRegEx.prefixWordSuffix(sPattern, sRegExPrefix, sRegExSuffix),
                Pattern.CASE_INSENSITIVE
                );
        boolean bResult= findAll.printAllMatchResult(patternLex, sParagraph, false);
        if (bResult){
            try {
                variables().tempVariables.stempLN = 
                        variables().getMapHashValue(
                            variables().getResult_LowerCase(iPrevGroupNumber[0])
                );
                variables().saveCurrentMatch(iPrevGroupNumber[0]);
            } catch(Exception e){
                    if (!bMayLeave){ Const.writeSpecificError(e, sParagraph);}
                    variables().tempVariables.sCurrentMatch = "";
            }
        }
        return variables().prepareReturn(bResult);        
    }    
    //=========  )? case
    public boolean capture_WS_AND_PART_op(String sParagraph,
            String sRegExPrefix, String sRegExSuffix, int[] iPrevGroupNumber,boolean bMayLeave) {
//($WS$AND_PARTS)?            
        variables().startCapture();
        String sNewSuf = ConstRegEx.REST_ANY + sRegExSuffix;    
        boolean bRes = comPrefSuf.capture_WS(sParagraph, 
                sRegExPrefix, sNewSuf, iPrevGroupNumber, bMayLeave);
        if (bRes){
            TempSaveCurrentMatch tmpMatch = new TempSaveCurrentMatch(variables());
            sParagraph = tmpMatch.saveMatchAndMoveOverAndReset(sParagraph);
            String sNewPref = ConstRegEx.newPrefix(iPrevGroupNumber);
            
            bRes = capture_AND_PARTS(sParagraph, 
                    sNewPref, sRegExSuffix, iPrevGroupNumber, bMayLeave);
            if (bRes){
                tmpMatch.restoreCurrentMatch();
                return variables().prepareReturn(true);
            }
        }
        variables().tempVariables.sCurrentMatch = "";
        return variables().prepareReturn(true);
    }    

    public boolean capture_LAST_NEXT_WORD_WS_op(String sParagraph, String sRegExPrefix, String sRegExSuffix, int[] iPrevGroupNumber,boolean bMayLeave) {
        variables().startCapture();
        String sNewSuf = ConstRegEx.REST_ANY + sRegExSuffix;    
        boolean bRes = capture_LAST_NEXT_WORD(sParagraph, sRegExPrefix, sNewSuf, iPrevGroupNumber, bMayLeave);
        if (bRes){
            TempSaveCurrentMatch tmpMatch = new TempSaveCurrentMatch(variables());
            sParagraph = tmpMatch.saveMatchAndMoveOverAndReset(sParagraph);
            String sNewPref = ConstRegEx.newPrefix(iPrevGroupNumber);
            
            bRes = comPrefSuf.capture_WS(sParagraph, 
                    sNewPref, sRegExSuffix, iPrevGroupNumber, bMayLeave);
            if (bRes){
                tmpMatch.restoreCurrentMatch();
                return variables().prepareReturn(true);
            }
        }
        variables().tempVariables.sCurrentMatch = "";
        return variables().prepareReturn(true);
    }    
    
    public boolean capture_Recursive_Date_Suffix(String sParagraph,
            String sRegExPrefix, String sRegExSuffix, int[] iPrevGroupNumber
            ,boolean bMayLeave, boolean[] bSingularDate, String[] stRD2) {
/*
 (($WS($AND|$PUNCT)?$WS?)($NUM_PLURAL_DATE 
 */        
        variables().startCapture();
        String sNewSuffix = ConstRegEx.REST_ANY + sRegExSuffix;
        boolean bRes = comPrefSuf.capture_WS_AND_PUNCT_op_WS_op(sParagraph,
                sRegExPrefix, sNewSuffix, iPrevGroupNumber, bMayLeave);
        if (bRes) {
            TempSaveCurrentMatch tmpMatch = new TempSaveCurrentMatch(variables());
            sParagraph = tmpMatch.saveMatchAndMoveOverAndReset(sParagraph);
            String sNewPrefix = ConstRegEx.newPrefix(iPrevGroupNumber);

            TempResultVariables tempRes = new TempResultVariables(variables());
            tempRes.setInitialValues(iPrevGroupNumber);
            if (dateTE.capture_NUM_PLURAL_DATE(sParagraph,
                    sNewPrefix, sRegExSuffix, iPrevGroupNumber, true)) {
                stRD2[0] += variables().tempVariables.stempNumPluralDate;
                bSingularDate[0] = false;
                tempRes.setBestResult(iPrevGroupNumber[0]);
            }
            if (!tempRes.bAtLeastOneResult) {
                tempRes.resetInitialValues(iPrevGroupNumber);
                if (dateTE.capture_NUM_SINGULAR_DATE(sParagraph,
                        sNewPrefix, sRegExSuffix, iPrevGroupNumber, true)) {
                    stRD2[0] += variables().tempVariables.stempSingularDate;
                    tempRes.setBestResult(iPrevGroupNumber[0]);
                    bSingularDate[0] = true;
                }
            }
            tempRes.prepareReturn(iPrevGroupNumber);
            if (tempRes.bAtLeastOneResult){
                tmpMatch.restoreCurrentMatch();
                return variables().prepareReturn(true);                
            }
        }
        variables().tempVariables.sCurrentMatch = "";
        return variables().prepareReturn(true);
    }       
    
    
   public boolean capture_Recursive_TIME_Suffix(String sParagraph,
            String sRegExPrefix, String sRegExSuffix, int[] iPrevGroupNumber
            ,boolean bMayLeave, boolean[] bSingularTime, String[] stRT2) {
/*
 (($WS($AND|$PUNCT)?$WS?)($NUM_PLURAL_DATE 
 */        
        variables().startCapture();
        String sNewSuffix = ConstRegEx.REST_ANY + sRegExSuffix;
        boolean bRes = comPrefSuf.capture_WS_AND_PUNCT_op_WS_op(sParagraph,
                sRegExPrefix, sNewSuffix, iPrevGroupNumber, bMayLeave);
        if (bRes) {
            TempSaveCurrentMatch tmpMatch = new TempSaveCurrentMatch(variables());
            sParagraph = tmpMatch.saveMatchAndMoveOverAndReset(sParagraph);
            String sNewPrefix = ConstRegEx.newPrefix(iPrevGroupNumber);

            TempResultVariables tempRes = new TempResultVariables(variables());
            tempRes.setInitialValues(iPrevGroupNumber);
            if (timeTE.capture_NUM_PLURAL_TIME(sParagraph,
                    sNewPrefix, sRegExSuffix, iPrevGroupNumber, true)) {
                stRT2[0] += variables().tempVariables.stempNumPluralTime;
                bSingularTime[0] = false;
                tempRes.setBestResult(iPrevGroupNumber[0]);
            }
            if (!tempRes.bAtLeastOneResult) {
                tempRes.resetInitialValues(iPrevGroupNumber);
                if (timeTE.capture_NUM_SINGULAR_TIME(sParagraph,
                        sNewPrefix, sRegExSuffix, iPrevGroupNumber, true)) {
                    stRT2[0] += variables().tempVariables.stempSingularTime;
                    tempRes.setBestResult(iPrevGroupNumber[0]);
                    bSingularTime[0] = true;
                }
            }
            tempRes.prepareReturn(iPrevGroupNumber);
            if (tempRes.bAtLeastOneResult){
                tmpMatch.restoreCurrentMatch();
                return variables().prepareReturn(true);                
            }
        }
        variables().tempVariables.sCurrentMatch = "";
        return variables().prepareReturn(true);
    }       
    
        
  public boolean capture_WS_TO_999_MIX_op(String sParagraph,
            String sRegExPrefix, String sRegExSuffix, int[] iPrevGroupNumber,boolean bMayLeave) {
////($WS$TO_999_MIX)?
        variables().startCapture();
        String sNewSuf = ConstRegEx.REST_ANY + sRegExSuffix;    
        boolean bRes = comPrefSuf.capture_WS(sParagraph, 
                sRegExPrefix, sNewSuf, iPrevGroupNumber, bMayLeave);
        if (bRes){
            TempSaveCurrentMatch tmpMatch = new TempSaveCurrentMatch(variables());
            sParagraph = tmpMatch.saveMatchAndMoveOverAndReset(sParagraph);
            String sNewPref = ConstRegEx.newPrefix(iPrevGroupNumber);
            
            bRes = capture_TO_999_MIX(sParagraph, 
                    sNewPref, sRegExSuffix, iPrevGroupNumber, bMayLeave);
            if (bRes){
                tmpMatch.restoreCurrentMatch();
                return variables().prepareReturn(true);
            }
        }
        variables().tempVariables.sCurrentMatch = "";
        return variables().prepareReturn(true);
    }    
          
  public boolean capture_WS_AND_TO_99_op(String sParagraph,
            String sRegExPrefix, String sRegExSuffix, int[] iPrevGroupNumber,boolean bMayLeave) {
//capture_AND_TO_99           
        variables().startCapture();
        String sNewSuf = ConstRegEx.REST_ANY + sRegExSuffix;    
        boolean bRes = comPrefSuf.capture_WS(sParagraph, 
                sRegExPrefix, sNewSuf, iPrevGroupNumber, bMayLeave);
        if (bRes){
            TempSaveCurrentMatch tmpMatch = new TempSaveCurrentMatch(variables());
            sParagraph = tmpMatch.saveMatchAndMoveOverAndReset(sParagraph);
            String sNewPref = ConstRegEx.newPrefix(iPrevGroupNumber);
            
            bRes = capture_AND_TO_99(sParagraph, 
                    sNewPref, sRegExSuffix, iPrevGroupNumber, bMayLeave);
            if (bRes){
                tmpMatch.restoreCurrentMatch();
                return variables().prepareReturn(true);
            }
        }
        variables().tempVariables.sCurrentMatch = "";
        return variables().prepareReturn(true);
    }    
      
    public boolean capture_ALL_quick(String sParagraph, String sRegExPrefix, String sRegExSuffix, int[] iPrevGroupNumber,boolean bMayLeave) {
        TempResultVariables tempRes = new TempResultVariables(variables());
        tempRes.setInitialValues(iPrevGroupNumber);

        if (mixedallTE.capture_MIXED_ALL(sParagraph, sRegExPrefix, sRegExSuffix, iPrevGroupNumber, bMayLeave)){
             tempRes.setBestResult( iPrevGroupNumber[0]);    
             return variables().prepareReturn(true);
        }
        tempRes.resetInitialValues(iPrevGroupNumber);
        if (dateTE.capture_DATE_ALL(sParagraph, sRegExPrefix, sRegExSuffix, iPrevGroupNumber, bMayLeave)){
             tempRes.setBestResult( iPrevGroupNumber[0]);    
             return variables().prepareReturn(true);
        }
        
        if (timeTE.capture_TIME_ALL(sParagraph, 
                sRegExPrefix, sRegExSuffix, iPrevGroupNumber, bMayLeave)){
            tempRes.setBestResult( iPrevGroupNumber[0]);    
            return variables().prepareReturn(true);
        }
        
        tempRes.resetInitialValues(iPrevGroupNumber);
        if ( undefTE.capture_UNDEFINITE_ALL(sParagraph, 
                sRegExPrefix, sRegExSuffix, iPrevGroupNumber, bMayLeave)){
            tempRes.setBestResult( iPrevGroupNumber[0]);    
            return variables().prepareReturn(true);
        }

        tempRes.prepareReturn(iPrevGroupNumber);
        return variables().prepareReturn(tempRes.bAtLeastOneResult);
    }
     public String moveoverParagraph(String sParagraph, String[] sPref, String[] sWS){
       int[] iGroupNumber = new int[1];
       iGroupNumber[0] = 0;
       String sPrefix = 
                //"(^|" +
                "("+
                "[.\\(\\)\\[\\]\\,?!\\'\\-\"`]"+ "|"+
                ConstRegEx.groupBrackets(ConstRegEx.WS, iGroupNumber) +
                ")";
        String[] sUnits = sParagraph.split(sPrefix);
        int iMatchPos =0;
         if (sUnits.length == 1) {
             sPref[0] = sUnits[0];
             sWS[0] = sParagraph.substring(sPref[0].length());
             return "";
         } else if (sUnits.length > 1) {
             int j = 1; boolean bFound = false;
             for (j = 1; j < sUnits.length; j++) {
                 if (sUnits[j].length() > 0) {
                     bFound = true;
                     break;
                 }
             }
             if (bFound) {
                 iMatchPos = sParagraph.indexOf(sUnits[j], sUnits[0].length());
               //  System.out.println("'" + sParagraph.substring(iMatchPos) + "'");
                 sPref[0] = sParagraph.substring(0, iMatchPos);
                 return sParagraph.substring(iMatchPos);
             }else{
                 sPref[0] = sUnits[0];
                 return sParagraph.substring(sUnits[0].length());
             }
         }
        sPref[0] = "";
        return "";
    }
     
    public String moveoverCurrentMatch(String sParagraph){
        return  sParagraph.substring(variables().tempVariables.sCurrentMatch.length());
    }
    
    public void afterCapture (int iStart, List<NormalizedChunk> trsetAnPos) {
        List<TimePoint> lstTp =variables().tempVariables.rangeTimePoint;
        lstTp.add(variables().tempVariables.crtTimePoint);
        if (lstTp.size() == 1) {
            afterCapture();
            iStart+=variables().tempVariables.sPrep.length();
        }
        @SuppressWarnings("unused")
        String sNorm = "";
        String sText ="";  
        String sDisp ="";
        try {
            for (int i=0; i < lstTp.size(); i++){
                TimePoint tpTemp = lstTp.get(i);
                
                sDisp = tpTemp.display();
                TimePoint newTP = tpTemp.interpretTimex();

                sNorm = newTP.displayWithoutTimexTag();
                sText = newTP.displayText();
                solveTimePoint.lstTimePoint.add(newTP);

                NormalizedChunk ann = new NormalizedChunk(ChunkFactory.createChunk(iStart, iStart + sText.length(), "TIMEX2" ));
                ann.setNormalized("");
                trsetAnPos.add(ann);
                { 
                    iStart += 
                    variables().tempVariables.rangeTimePoint.get(i).get("range_Text").length();                    
                }                
                iStart += sText.length();
            }
        } catch (Exception e) {//Catch exception if any
            System.err.println("Error (afterCapture): " + e.getMessage());
            System.err.println("Error (afterCapture): " + sDisp);
        }
        variables().tempVariables.crtTimePoint = new TimePoint();
        variables().tempVariables.refTimePoint = new TimePoint();
        variables().tempVariables.rangeTimePoint = new ArrayList<TimePoint>();
    }
    
    public void saveCapture() {
        List<TimePoint> lstTp =variables().tempVariables.rangeTimePoint;
        lstTp.add(variables().tempVariables.crtTimePoint);
        
        if (lstTp.size() == 1) {
            afterCapture();
        }else{
            variables().tempVariables.sPrep = "";
        }
        String sDisp="";
        try {
            for (int i = 0; i < lstTp.size(); i++) {
                TimePoint tpTemp = lstTp.get(i);
                sDisp = tpTemp.display();
                TimePoint newTP = tpTemp.interpretTimex();
                variables().out.write("\n\t //DISPLAY\\\\:\n");
                variables().out.write("\t"+sDisp);
                variables().out.write("\n\t \\\\DISPLAY//\n");
                variables().out.write("\n\t //INTERPRET\\\\\n");
                
                if (i>=variables().tempVariables.rangeTimePoint.size()){
                    variables().out.write(variables().tempVariables.sPrep +
                        newTP.display());
                    
                }else{
                    variables().tempVariables.sPrep =
                            variables().tempVariables.rangeTimePoint.get(i).get("range_Text");                    
                    variables().out.write(newTP.display() + variables().tempVariables.sPrep
                        );
                }
                variables().out.write("\n\t \\\\INTERPRET//:\n");
                solveTimePoint.lstTimePoint.add(newTP);
            }
        } catch (Exception e) {//Catch exception if any
            
            System.err.println("Error: (saveCapture)" + e.getMessage());
            System.err.println("Error: (saveCapture)" + sDisp);
        }

        variables().tempVariables.crtTimePoint = new TimePoint();
        variables().tempVariables.refTimePoint = new TimePoint();
        variables().tempVariables.rangeTimePoint = new ArrayList<TimePoint>();
        variables().tempVariables.sMatch = "";
        variables().tempVariables.sCurrentMatch = "";
        variables().tempVariables.sPrep = "";
    }
    
    public void afterCapture() {
        int[] iTemp = new int[1];
        iTemp[0] = 0;
        String sPatternSpace = "^(" +
                ConstRegEx.groupBrackets(
                    variables().hashtbLexValues.get("PREP").toString() 
                , iTemp)+
                ConstRegEx.groupBrackets(ConstRegEx.WS, iTemp) + "|" +
                ConstRegEx.groupBrackets(
                    variables().hashtbLexValues.get("OF").toString() 
                , iTemp)+
                ConstRegEx.groupBrackets(ConstRegEx.WS, iTemp) + "|" +
                ConstRegEx.groupBrackets(
                    variables().hashtbLexValues.get("TO").toString() 
                , iTemp)+                    
                ConstRegEx.groupBrackets(ConstRegEx.WS, iTemp) + "|" +
                ConstRegEx.groupBrackets(
                    variables().hashtbLexValues.get("CONJ").toString() 
                , iTemp)+                    
                ConstRegEx.groupBrackets(ConstRegEx.WS, iTemp) + "|" +
                ConstRegEx.groupBrackets(ConstRegEx.PUNCT, iTemp) +
                ConstRegEx.groupBrackets(ConstRegEx.WS, iTemp) +
                ")(.*)";
        Pattern patternSpace = Pattern.compile(sPatternSpace);

        boolean bRes = findAll.printAllMatchResult(patternSpace, 
                variables().tempVariables.sMatch, false); 
        if (bRes){
            String sSuf =variables().tempVariables.matchResult.group(1);
            variables().tempVariables.sPrep = sSuf;
            variables().tempVariables.sMatch =
                    variables().tempVariables.sMatch.substring(
                    sSuf.length());
        }else{
            variables().tempVariables.sPrep = "";
        }
        variables().tempVariables.crtTimePoint.set("text",
                variables().tempVariables.sMatch);
    }
   
    /**
     * The main method of the temporal expressions annotation engine 
     * 
     * @param sParagraph the text to be analysed
     * @param xmlCal providing a temporal context (time and date)
     * 
     * @return a set of {link {@link AnnotationPosition annotations} marking the temporal expressions identified  
     */
    public List<NormalizedChunk> capture_AllTimeExpressions(String sParagraph, XMLGregorianCalendar xmlCal ) {
       List<NormalizedChunk> trsetAnPos = new ArrayList<NormalizedChunk>(); 
       int[] iGroupNumber = new int[1];iGroupNumber[0] = 1;
       int[] iTemp = new int[1]; iTemp[0]=0;
       String sPrefix = "(";
       String sSuffix =")"+
                        //"(?=$|"+//2008.10.23
                        "($|"+
                        ConstRegEx.PUNCT+
                        "|"+
                        ConstRegEx.groupBrackets(ConstRegEx.WS, iTemp)+
                        "|\\'|\\-)";
        boolean bResult;
        String[] sMatch = new String[1];sMatch[0] = "";
        String[] sPrep = new String[1];
        String[] sWS = new String[1]; sWS[0]="";
        int iStartPos = 0;
        solveTimePoint.lstTimePoint.clear();
        while (sParagraph.length() > 0) {
            ConstRegEx.resetIntArray(1, iGroupNumber);
            bResult = capture_ALL_quick(sParagraph, sPrefix, sSuffix, iGroupNumber, false);
            if (!bResult) {
                sParagraph = moveoverParagraph(sParagraph, sPrep, sWS);
                variables().tempVariables.sPrep += sPrep[0];
                try {
                    iStartPos+= sPrep[0].length();
                    if (sParagraph.length() == 0) { iStartPos+= sWS[0].length(); }
                    variables().tempVariables.matchResult = null;
                    variables().tempVariables.sMatch = "";
                    variables().tempVariables.sCurrentMatch = "";
                } catch (Exception e) { System.err.println("Error: " + e.getMessage()); }
            } else {
                sParagraph = moveoverCurrentMatch(sParagraph);
                variables().tempVariables.sMatch = variables().tempVariables.sCurrentMatch;
                afterCapture(iStartPos, trsetAnPos);
                iStartPos+= variables().tempVariables.sCurrentMatch.length();
                try {
                    String sRemain = sParagraph.substring(0, 1);
                    sParagraph = sParagraph.substring(sRemain.length());
                    iStartPos+=sRemain.length();
                } catch (Exception e) {
                }
                
            }
        }
        solveTimePoint.resolveTimex(xmlCal);
        try {
            int i = 0;
            Iterator<NormalizedChunk> it = trsetAnPos.iterator();
            while (it.hasNext()) {
                TimePoint tp = solveTimePoint.lstTimePoint.get(i);
                NormalizedChunk annPos = it.next();
                annPos.setNormalized(tp.displayWithoutTimexTag().toUpperCase());
                i++;
            }
        } catch (Exception e) { }
        return trsetAnPos;
    }
    
    public void capture_AllTimeExpressions_quick(String sParagraph, XMLGregorianCalendar xmlCal) {
       int[] iGroupNumber = new int[1];iGroupNumber[0] = 1;
       int[] iTemp = new int[1]; iTemp[0]=0;
       String sPrefix = "(";
       String sSuffix =")"+
                        //"(?=$|"+//2008.10.23
                        "($|"+
                        ConstRegEx.PUNCT+
                        "|"+
                        ConstRegEx.groupBrackets(ConstRegEx.WS, iTemp)+
                        "|\'|-)";
        boolean bResult;
        String[] sMatch = new String[1];sMatch[0] = "";
        String[] sPrep = new String[1];
        String[] sWS = new String[1]; sWS[0]="";
        //2008.11.20
        solveTimePoint.lstTimePoint.clear();
        while (sParagraph.length()> 0)  {
           ConstRegEx.resetIntArray(1, iGroupNumber);
           bResult = capture_ALL_quick(sParagraph, sPrefix, sSuffix, iGroupNumber, false);
           if (bResult && (variables().tempVariables.sCurrentMatch.length()==0)){
               writeToLogFile("\t****\t'" +
                       "Error(empty sCurrentMatch): " + sParagraph+
                        "'\t****\n");
           }
           bResult = bResult && (variables().tempVariables.sCurrentMatch.length()>0);
            if (!bResult) {
                sParagraph = moveoverParagraph(sParagraph, sPrep, sWS);
                variables().tempVariables.sPrep += sPrep[0];
                try {
                    variables().out.write(sPrep[0]);
                    if (sParagraph.length() == 0) {
                        variables().out.write(sWS[0]);
                    }
                    variables().tempVariables.matchResult =null;
                    variables().tempVariables.sMatch = "";
                    variables().tempVariables.sCurrentMatch = "";
                } catch (Exception e) {//Catch exception if any
                    System.err.println("Error: " + e.getMessage());
                }
            } else {
                sParagraph = moveoverCurrentMatch(sParagraph);
                variables().tempVariables.sMatch = variables().tempVariables.sCurrentMatch;
                writeToLogFile("\t****\t'" + variables().tempVariables.sMatch + "'\t****\n");
                saveCapture();
                try {
                    String sRemain = sParagraph.substring(0, 1);
                    sParagraph = sParagraph.substring(sRemain.length());
                    variables().out.write(sRemain);
                } catch (Exception e) {
                }
                
            }
        }
        solveTimePoint.resolveTimex(xmlCal);
        try {
            if (solveTimePoint.lstTimePoint.size() > 0) {
                variables().out.write("\n\t //NORMALIZED\\\\:");
            }
            for (int i = 0; i < solveTimePoint.lstTimePoint.size(); i++) {

                TimePoint tp = solveTimePoint.lstTimePoint.get(i);
                variables().out.write("\n\t" + tp.display() + "\n");

            }
            if (solveTimePoint.lstTimePoint.size() > 0) {
                variables().out.write("\t \\\\NORMALIZED//\n");
            }

        } catch (Exception e) { }
    }
    
     public void eliminateSomeWords(String[] sInput){
        try{
            sInput[0] = sInput[0].replaceAll("\\[[^\\[\\]]*\\]", "");
            sInput[0] = sInput[0].replaceAll("  ", " ");
        }catch(Exception e){
            System.out.println(e.toString());
        }
    }
     
    public void splitIntoParagraphs( ) {
        int i = 0;
        String[] paragraphs = new String[1024];
        try {
            FileInputStream fis = new FileInputStream(new File(variables().sInputFile));
            InputStreamReader isr = new InputStreamReader(fis);
            char[] text = new char[fis.available()];
            isr.read(text, 0, fis.available());
            isr.close();
            String[] sNewData  = new String[1];
            sNewData[0] = new String(text);
            eliminateSomeWords(sNewData);
            paragraphs = sNewData[0].split(Const.EOL_WIN32);
            if (variables().bPrintOutTheResults) {
                System.out.println("Number of paragraphs of text: " + paragraphs.length + "\n");
            }            
            for ( i= 0; i < paragraphs.length; i++) {
                if (variables().bPrintOutTheResults) {
                    System.out.println("Paragraph " + (i + 1) + ": " + paragraphs[i]);
                }
                capture_AllTimeExpressions_quick(paragraphs[i], xmlCalendar);
                if (variables().bPrintOutTheResults) {
                    System.out.println("\n------------------------\n");
                }
                variables().out.write(Const.EOL_WIN32);
            }
            
            variables().out.close();
            variables().log.close();
        } catch (Exception e) {
            System.out.println("The Paragraph : "+ i);
            
            System.out.println(e.toString());
            try {
                System.out.println(paragraphs[i]);
                variables().out.close();
                variables().log.close();
            } catch (Exception ee) {
            }
        }
    }
    
    public void writeToLogFile(String sText){
        try {
            if (variables().log != null) { variables().log.write(sText); }
        } catch (Exception e) { System.err.println("Error: " + e.getMessage()); }
    }


}
