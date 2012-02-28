package temporal.rules;

import java.util.regex.*;
import java.util.*;
import java.util.ArrayList;
import javax.xml.datatype.XMLGregorianCalendar;
import java.util.List;

public class ResolveTimePoint {
    
	public List<TimePoint> lstTimePoint;
    
	public TimePoint refTimePoint;
    
    public ResolveTimePoint( ){
        lstTimePoint = new ArrayList<TimePoint>();
        refTimePoint = null;
    }
    
    public void createRefTimePoint(XMLGregorianCalendar xmlCal){
        refTimePoint = new TimePoint();
        Calendar cal = xmlCal.toGregorianCalendar();
        String sVal = 
                Integer.toString(cal.get(Calendar.YEAR))+"-"+
                TimePoint.extend_To_TwoNumbers(
                    Integer.toString(
                        GregorianCalendarHelper.CalMonth_to_TimePoint(cal))
                )+"-"+
                TimePoint.extend_To_TwoNumbers(
                    Integer.toString(cal.get(Calendar.DAY_OF_MONTH))
                )+
                "T"+
                TimePoint.extend_To_TwoNumbers(
                    Integer.toString(cal.get(Calendar.HOUR_OF_DAY))
                )+":"+
                TimePoint.extend_To_TwoNumbers(
                    Integer.toString(cal.get(Calendar.MINUTE))
                )+":"+
                TimePoint.extend_To_TwoNumbers(
                    Integer.toString(cal.get(Calendar.SECOND))
                );
        refTimePoint.set("val",sVal);
        refTimePoint.set("granularity","Y");
    }
    
    public boolean validateCandidateTE(TimePoint t, String sGran){
        int iY, iDom, iW, iS;
        String sM,sDow, sH, sMin;
        iY = t.getYear();
        //$m = getMonth($cand);
        sM = t.getMonth();
	//$dom = getDayOfMonth($cand);
        iDom = t.getDayOfMonth();
	//$w = getWeek($cand);
        iW = t.getWeek();
	//$dow = getDayOfWeek($cand);
        sDow = t.getDayOfWeek();
	//$h = getHour($cand);
        sH = t.getHour();
	//$min = getMinute($cand);
        sMin = t.getMinute();
	//$s = getSecond($cand);         
        iS = t.getSecond();
        
//return 0 if ($gran =~ /^(L|C|E|Y)$/  && $y !~ /^\d+$/);
        if (sGran.matches("^(L|C|E|Y)$") && (iY== -1))
            return false;
        
//return 0 if ($gran =~ /^(C|E|Y)$/  && $y =~ /^\d$/);
        if (sGran.matches("^(C|E|Y)$") && ( iY>=0 && iY <10) )
            return false;
        
//return 0 if ($gran =~ /^(E|Y)$/  && $y =~ /^\d\d$/);        
        if (sGran.matches("^(E|Y)$") && ( iY>=10 && iY <100) )
            return false;

//return 0 if ($gran =~ /^Y$/  && $y =~ /^\d\d\d$/);
        if (sGran.matches("^Y$") && ( iY>=100 && iY <1000) )
            return false;
        
/*    if ($gran =~ /^M$/)
	{
		return 0 if ($m  == -1 || $m !~ /^\d+$/);
	}
 */
        if (sGran.matches("^M$")){
            if (sM.compareTo("-1") == 0)
                return false;
        }
        
/*
        if ($gran =~ /^(SE|Q)$/)
	{
		return 0 if ($m == -1);
	}
 */        
        if (sGran.matches("^(SE|Q)$")){
             if (sM.compareTo("-1") == 0)
                return false;
        }
/*
        if ($gran =~ /^W$/)
	{
		return 0 if ($w == -1 && $dom == -1);
	}
 */                
        if (sGran.matches("^W$")){
            if ( (iW == -1) && (iDom == -1) )
                return false;
        }
        
/*
	if ($gran =~ /^D$/)
	{
		return 0 if ($dow == -1 && $dom == -1)
	}
 */        
        if (sGran.matches("^D$")){
            if ( (sDow.compareTo("-1") == 0) && (iDom == -1) )
                return false;
        }
/*
        if ($gran =~ /^H$/)
	{
		return 0 if ($h == -1 || $h !~ /^\d+$/);
	}
 */        
        if (sGran.matches("^H$")){
            if ( sH.compareTo("-1") == 0  )
                return false;
        }
/*
        if ($gran =~ /^MIN$/)
	{
		return 0 if ($min == -1);
	}
 */        
        if (sGran.matches("^MIN$")){
            if ( sMin.compareTo("-1") == 0 )
                return false;
        }
/*
    if ($gran =~ /^S$/)
 */        
        if (sGran.matches("^S$")){
            if ( iS == -1  )
                return false;
        }
        return true;
    }
    public void resolve( TimePoint reference, TimePoint antecedent){
/*
 * 
 */        
        
        String sVal = reference.get("val");

//($sgn, $incr) = ($findIt[1] =~ /^(\+|-)(\d+)$/);
        String sSignIncr = reference.getGranularity(1);
        char cSgn = sSignIncr.charAt(0);
        int iY=0;
        int iIncr = Integer.parseInt(sSignIncr.substring(1));
        if (cSgn != '+'){
            iIncr = 0 - iIncr;
        }
//if ($findIt[0] =~ /^(L|C|E|Y)$/ && validateCandidateTE($antecedent, $1))
        String sFindIt0 = reference.getGranularity(0);
        if ( (sFindIt0.matches("^(L|C|E|Y)$") )&& validateCandidateTE(antecedent, sFindIt0)){
            int iYY = antecedent.getYear(); String sYY = Integer.toString(iYY);
            if (sFindIt0.compareTo("L")==0){
                iY = Integer.parseInt(sYY.substring(0, 0));
            }
            if (sFindIt0.compareTo("C")==0){
                iY = Integer.parseInt(sYY.substring(0, 1));
            }
            if (sFindIt0.compareTo("E")==0){
                iY = Integer.parseInt(sYY.substring(0, 2));
            }            
            if (sFindIt0.compareTo("Y")==0){
                iY = Integer.parseInt(sYY);
            }  
            iY +=iIncr;
            String sY = Integer.toString(iY);
            if (reference.toFind_ResultList.size() > 2){
                sY += "-"+reference.toFind_ResultList.get(3);
                //reference.toFind_ResultList
            }
            if (sVal.length()>0){
                String thePattern = "^(\\d+|X+)(-|$)";
                Pattern pattern = Pattern.compile(thePattern, Pattern.CASE_INSENSITIVE);
                Matcher matcher = pattern.matcher(sVal);
                sVal = matcher.replaceAll(sY +"$2");
            }else{
                sVal = sY ;
            }
            reference.set("resolved", "1");            
            
        }else
//($findIt[0] =~ /^(M|Q|SE)$/ && validateCandidateTE($antecedent, "M"))
        if ( (sFindIt0.matches("^(M|Q|SE)$") )&& validateCandidateTE(antecedent, "M")){
//$yy = &getYear($antecedent);		$m = &getMonth($antecedent);
            int iYY = antecedent.getYear();
            String sM = antecedent.getMonth();
            /*
            if ($findIt[0] eq "M")
            {
            ($y, $m,) = &Add_Delta_YM(($yy, $m, 1), 0, $incr);
            }
             */
            if (sFindIt0.compareTo("M") == 0) {
                Calendar cal = GregorianCalendarHelper.Add_Delta_YM(iYY, Integer.parseInt(sM), 1, iIncr);
                iY = cal.get(Calendar.YEAR);
                sM = Integer.toString( GregorianCalendarHelper.CalMonth_to_TimePoint(cal));
            }else if (sFindIt0.compareTo("Q") == 0) {
                String sPattern = "Q([1-4])";
                Pattern pattern = Pattern.compile(
                        sPattern);
                Matcher m = pattern.matcher(sM);
                String sQ;
                 if (m.find()){
                     sQ = m.group(1);
                 }else{
                    sPattern = "10|11|12";
                    pattern = Pattern.compile(
                        sPattern);
                    m = pattern.matcher(sM);
                    if (m.find()){
                        sQ ="1";
                    }else{
                        sPattern = "[1-3]";
                        pattern = Pattern.compile(
                                sPattern);
                        m = pattern.matcher(sM);
                        if (m.find()){
                            sQ = "2";                            
                        }else{
                            sPattern = "[4-6]";
                            pattern = Pattern.compile(
                                    sPattern);
                            m = pattern.matcher(sM);
                            if (m.find()){
                                sQ = "3";
                            }else{
                                sQ = "4";
                            }
                        }
                    }
                 }
                int iQ = Integer.parseInt(sQ);
                iQ += iIncr;
                if (iQ <= 0){
                    if (iYY != -1) {
                        iY = iYY - (Math.abs(iQ) / 4 + 1);
                    }
                    iQ = 4- Math.abs(iQ)%4;
                }else{
                    if (iYY != -1) {
                        iY = iYY + (iQ -1)/4;
                    }
                    iQ = 4 - iQ% 4;
                }
                
                sM = "Q"+Integer.toString(iQ);
            }else if (sFindIt0.compareTo("SE") == 0) {
                String sPattern = "[3-5]|SP";
                Pattern pattern = Pattern.compile(
                        sPattern);
                Matcher m = pattern.matcher(sM);
                String sSe;
                 if (m.find()){
                     sSe = "1";
                 }else{
                     sPattern = "[6-8]|SU";
                     pattern = Pattern.compile(
                             sPattern);
                     m = pattern.matcher(sM);
                     if (m.find()) {
                         sSe = "2";
                     }else{
                         sPattern = "9|10|11|FA";
                         pattern = Pattern.compile(
                                 sPattern);
                         m = pattern.matcher(sM);                         
                         if (m.find()){
                             sSe="3";
                         }else{
                             sSe="4";
                         }
                     }                    
                 }
                int iSe = Integer.parseInt(sSe);
                iSe += iIncr;
                if (iSe <= 0) {
                    if (iYY != -1) {
                        iY = iYY - (Math.abs(iSe) / 4 + 1);
                    }
                    iSe = 4 - Math.abs(iSe) % 4;
                } else {
                    if (iYY != -1) {
                        iY = iYY + (iSe - 1) / 4;
                    }
                    iSe = 4 - iSe % 4;
                }
                switch (iSe) {
                    case 1: sM = "SP"; break;
                    case 2: sM = "SU"; break;
                    case 3: sM = "FA"; break;
                    case 4: sM = "WI"; break;                    
                }
            }
            
            if (sVal.length()>0){
                String thePattern = "^(\\d+|X+)-(\\d+|H\\d|Q\\d|SP|SU|WI|FA|X+)(-|$)";
                Pattern pattern = Pattern.compile(thePattern, Pattern.CASE_INSENSITIVE);
                Matcher matcher = pattern.matcher(sVal);
                sVal = matcher.replaceAll(Integer.toString(iY) + "-"+
                        sM+ "$3");
            }else{
                sVal = Integer.toString(iY) + "-"+sM;
            }
            reference.set("resolved", "1");
        }else 
        if ( (sFindIt0.compareTo("W") == 0)&& validateCandidateTE(antecedent, "W")){
            //2008.12.07
            
            if (reference.toFind_ResultList.size() > 2){ //granularity == "DOW"
                //eg next Monday
                String srefDOW = reference.toFind_ResultList.get(3);
                int iRefDOW = (srefDOW.compareTo("WE")==0)?6:Integer.parseInt(srefDOW);
                String sAntDOW =antecedent.getDayOfWeek();
                if (sAntDOW.compareTo("-1")==0){
                    sAntDOW = GregorianCalendarHelper.getDOW_From_YYYYMDOM(
                            antecedent.getYear(), 
                            GregorianCalendarHelper.TimePoint_to_CalMonth(
                                Integer.parseInt(antecedent.getMonth())
                             ),
                            antecedent.getDayOfMonth());
                }
                int iAntDOW = (sAntDOW.compareTo("WE")==0)?6:Integer.parseInt(sAntDOW);
                if (iAntDOW < iRefDOW){
                   if (iIncr == 1) {//next Wed
                       iIncr --;
                   }
                }else if (iAntDOW > iRefDOW) {
                   if (iIncr == -1) {//last Wed
                       iIncr ++;
                   }else if (iIncr == 0){
                       iIncr ++;
                   }
                }
                //reference.toFind_ResultList
            }
            
            int iYY = antecedent.getYear();
            String sM = antecedent.getMonth();            
            int iW = antecedent.getWeek();
            int iDays = iIncr;
            int iWeek, iDom;
            if (iW!=-1){
                Calendar cal = GregorianCalendarHelper.Add_Delta_Week_of_Year(iYY, iW, iDays);
                iWeek = cal.get(Calendar.WEEK_OF_YEAR);
                iY = cal.get(Calendar.YEAR);
            }else{
                iDom = antecedent.getDayOfMonth();
                Calendar cal = GregorianCalendarHelper.Add_Delta_Week_of_Year(iYY, 
                        Integer.parseInt(sM), iDom, iDays);
                iWeek = cal.get(Calendar.WEEK_OF_YEAR);
                iY = cal.get(Calendar.YEAR);
            }
            String sWeek = Integer.toString(iWeek);
            if (reference.toFind_ResultList.size()>2){
/*
            $week .= "-".$findIt[3];
			# make sure you change granularity
 */                
                String sDow = reference.getDayOfWeek();
                if (sDow.compareTo("-1") == 0) {
                    sWeek += "-" + reference.toFind_ResultList.get(3);
                }
            }
            if (sVal.length()>0){
                //String thePattern = "^(\\d+|X+)-W(\\d+|X+)(-|$|T)";
                String thePattern = "^(\\d+|X+)-W(\\d+|X+)(-|$)";
                Pattern pattern = Pattern.compile(thePattern, Pattern.CASE_INSENSITIVE);
                Matcher matcher = pattern.matcher(sVal);
                sVal = matcher.replaceAll(Integer.toString(iY) + "-W"+sWeek + "$3");
            }else{
                sVal = Integer.toString(iY) + "-W"+sWeek;
            }
            reference.set("resolved", "1");
         }else if ( (sFindIt0.matches("^(D|H|MIN|S)$") )&& validateCandidateTE(antecedent, "D")){
            int iYY = antecedent.getYear();
            String sMM = antecedent.getMonth();            
            int iWW = antecedent.getWeek();             
            String sDow;
            Calendar date = GregorianCalendarHelper.createSafeCal();
            if (iWW != -1){
                sDow = antecedent.getDayOfWeek();
                int iDow;
                //if ((sDow.compareTo("WE") ==0) || (sDow.compareTo("0") ==0)){
                //2008.11.29
                if ((sDow.compareTo("WE") ==0)){
                    iDow = 6;
                }else{
                    //2008.11.29
                    //iDow = Integer.parseInt(sDow) -1;
                    iDow = Integer.parseInt(sDow);
                }
                date = GregorianCalendarHelper.Add_Delta_Days(iYY, iWW, iDow);
                        //GregorianCalendarHelper.TimePoint_to_CalDOW(iDow));//2008.11.30
                reference.set("resolved","1");
            }else{
                int iDom = antecedent.getDayOfMonth();
                date.set(iYY, 
                       GregorianCalendarHelper.TimePoint_to_CalMonth(Integer.parseInt(sMM)), iDom);
                reference.set("resolved","1");
            }
            if (sFindIt0.compareTo("D") == 0) {
                date = GregorianCalendarHelper.Add_Delta_Days(date, iIncr);
                iY = date.get(Calendar.YEAR);
                int iM = GregorianCalendarHelper.CalMonth_to_TimePoint(date);
                int iD = date.get(Calendar.DATE);
                String sD = Integer.toString(iD);
                if (reference.toFind_ResultList.size() > 2) {
                    sD += "T" + reference.toFind_ResultList.get(3);
                }
                if (sVal.length() > 0) {
                    String thePattern = "^((\\d+|X+)-W?(\\d+|X+)-(\\d|WE))?(T|$)";
                    Pattern pattern = Pattern.compile(thePattern, Pattern.CASE_INSENSITIVE);
                    Matcher matcher = pattern.matcher(sVal);
                    sVal = matcher.replaceAll(Integer.toString(iY) + "-" +
                                Integer.toString(iM)
                            + "-" + sD + "$5");
                } else {
                    sVal = Integer.toString(iY) + "-" +
                                Integer.toString(iM)
                            + "-" + sD;
                }
                reference.set("resolved", "1");
            }
            if ((sFindIt0.matches("^(H|MIN|S)$")) && validateCandidateTE(antecedent, "H")) {
                /*
                $hh = getHour($antecedent);
                $mm = getMinute($antecedent);
                $ss = getSecond($antecedent);
                $mm = ($mm == -1)? 0 : $mm;
                $ss = ($ss == -1)? 0 : $ss;
                 */
                int iHH = Integer.parseInt(antecedent.getHour());
                int iMM = Integer.parseInt(antecedent.getMinute());
                int iSS = antecedent.getSecond();
                if (iMM == -1) {
                    iMM = 0;
                }
                if (iSS == -1) {
                    iSS = 0;
                }
                if (sFindIt0.compareTo("H") == 0) {
                    date = GregorianCalendarHelper.Add_Delta_YMD_HMS_Hour(date, iHH,
                            iMM, iSS, iIncr);
                } else if (sFindIt0.compareTo("MIN") == 0) {
                    date = GregorianCalendarHelper.Add_Delta_YMD_HMS_Min(date, iHH,
                            iMM, iSS, iIncr);
                } else {
                    date = GregorianCalendarHelper.Add_Delta_YMD_HMS_Sec(date, iHH,
                            iMM, iSS, iIncr);
                }
                iY = date.get(Calendar.YEAR);
                int iM = GregorianCalendarHelper.CalMonth_to_TimePoint(date);
                int iD = date.get(Calendar.DATE);
                String sH = Integer.toString(date.get(Calendar.HOUR_OF_DAY));
                if (sH.length() == 1) {
                    sH = "0" + sH;
                }
                String sMin = Integer.toString(date.get(Calendar.MINUTE));
                if (sMin.length() == 1){
                    sMin = "0"+sMin;
                }
                String sSec = Integer.toString(date.get(Calendar.SECOND));
                if (sSec.length() ==1){
                    sSec = "0"+sSec;
                }
                sVal = Integer.toString(iY)+"-"+
                        Integer.toString(iM)
                        +"-"+
                       Integer.toString(iD)+"T"+ 
                       sH + ":"+sMin+":"+sSec;
                reference.set("resolved", "1");
            }
         }
        
        reference.set("val", sVal);
    }
    public void findAntecedent(int iTimePointIndex, String sGranularity){
        TimePoint t1;
        //boolean bExistTP = false;
        for (int i=0;i<lstTimePoint.size();i++){
            if (i != iTimePointIndex){
                t1 = lstTimePoint.get(i);
                if (validateCandidateTE(t1, sGranularity)){
                    if ( 
                      (t1.get("resolved").compareTo("1")==0)||
                      (t1.get("fullySpec").compareTo("1")==0)
                      ){
                        resolve(lstTimePoint.get(iTimePointIndex), t1);
                        return;
                    }else{
                        findAntecedent(i, t1.getGranularity(0));
                        resolve(lstTimePoint.get(iTimePointIndex),
                                t1);
                        return;
                    }
                }
            }
        }
        resolve(lstTimePoint.get(iTimePointIndex), refTimePoint);
        return;
    }
    
    public void resolveTimex(XMLGregorianCalendar xmlCal) {
        createRefTimePoint(xmlCal);
        TimePoint t;

        for (int i = 0; i < lstTimePoint.size(); i++) {
            t = lstTimePoint.get(i);
            if ((t.get("resolved").compareTo("1") == 0) ||
                    (t.get("fullySpec").compareTo("1") == 0)) {
                //t.clearUnusedAttributes();//DONT DO THIS
            } else {
                findAntecedent(i, t.getGranularity(0));
            }
        }
        for (int i = 0; i < lstTimePoint.size(); i++) {
            t = lstTimePoint.get(i);
            t.postCorrections(refTimePoint);
        }
    }

}