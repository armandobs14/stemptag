package temporal.rules;

import java.util.*;

public class GregorianCalendarHelper {
	
    public static Calendar createSafeCal(){
        Calendar cal = Calendar.getInstance();
        cal.setFirstDayOfWeek(Calendar.SUNDAY);
        return cal;
    }
    
    public static int TimePoint_to_CalMonth(int iMonth){
        return iMonth -1;
    }
    
    public static int CalMonth_to_TimePoint(Calendar cal) {
        return cal.get(Calendar.MONTH) +1;
    }

    public static int TimePoint_to_CalDOW(int iDayOfWeek) {
        return iDayOfWeek + 1;
    }
    
    public static int CalDOW_to_TimePoint(Calendar cal) {
        return cal.get(Calendar.DAY_OF_WEEK) - 1;
    }
    
    public static Calendar Add_Delta_YM(int iYear, int iMonth,int iDate, int iIncr){
        Calendar cal = createSafeCal();
        cal.set(iYear, TimePoint_to_CalMonth(iMonth), iDate);
        cal.add(Calendar.MONTH, iIncr);
        return cal;
    }
    
    public static Calendar Add_Delta_Week_of_Year(int iYear, int iWeek,int iIncr){
        Calendar cal = createSafeCal();
        cal.set(Calendar.YEAR, iYear);
        cal.set(Calendar.WEEK_OF_YEAR, iWeek);
        cal.add(Calendar.WEEK_OF_YEAR, iIncr);
        return cal;
    }
    
    public static Calendar Add_Delta_Week_of_Year(int iYear, int iMonth,int iDayOfMonth, int iIncr){
        Calendar cal = createSafeCal();
        cal.set(Calendar.YEAR, iYear);
        cal.set(Calendar.MONTH, TimePoint_to_CalMonth(iMonth));        
        cal.set(Calendar.DAY_OF_MONTH, iDayOfMonth);
        cal.add(Calendar.WEEK_OF_YEAR, iIncr);
        return cal;
    }
    
    public static Calendar Add_Delta_Days(int iYear, int iWeek,int iDayOfWeek){
        Calendar cal = createSafeCal();
        cal.set(Calendar.YEAR, iYear);
        cal.set(Calendar.WEEK_OF_YEAR, iWeek);
        cal.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY);
        cal.add(Calendar.DAY_OF_WEEK, iDayOfWeek);
        return cal;
    }    
    
    public static Calendar Add_Delta_Days(Calendar calDate, int iIncrDay){
        calDate.add(Calendar.DATE, iIncrDay);
        return calDate;
    }    
    
    public static Calendar Add_Delta_YMD_HMS_Hour(Calendar calDate, int iHour, int iMin, int iSec, int iIncrHour){
        calDate.set(Calendar.HOUR_OF_DAY, iHour);
        calDate.set(Calendar.MINUTE, iMin);
        calDate.set(Calendar.SECOND, iSec);
        calDate.add(Calendar.HOUR_OF_DAY, iIncrHour);
        return calDate;
    }
    
    public static Calendar Add_Delta_YMD_HMS_Min(Calendar calDate, int iHour, int iMin, int iSec, int iIncrMin){
        calDate.set(Calendar.HOUR_OF_DAY, iHour);
        calDate.set(Calendar.MINUTE, iMin);
        calDate.set(Calendar.SECOND, iSec);
        calDate.add(Calendar.MINUTE, iIncrMin);
        return calDate;
    }

    public static Calendar Add_Delta_YMD_HMS_Sec(Calendar calDate, int iHour, int iMin, int iSec, int iIncrSec){
        calDate.set(Calendar.HOUR_OF_DAY, iHour);
        calDate.set(Calendar.MINUTE, iMin);
        calDate.set(Calendar.SECOND, iSec);
        calDate.add(Calendar.SECOND, iIncrSec);
        return calDate;
    }
    
    public static String getYYYYMMDD_From_YYYY_W_DOW(int iYear, int iWeekOfYear, int iDayOfWeek) {
        String sYMD = "";
        Calendar cal = createSafeCal();
        cal.set(Calendar.YEAR, iYear);
        cal.set(Calendar.WEEK_OF_YEAR, iWeekOfYear);
        cal.set(Calendar.DAY_OF_WEEK, TimePoint_to_CalDOW(iDayOfWeek));
        String sMonth = TimePoint.extend_To_TwoNumbers(Integer.toString(CalMonth_to_TimePoint(cal)));
        sYMD = Integer.toString(cal.get(Calendar.YEAR)) + "-" +
                sMonth
                 + "-" +
                 TimePoint.extend_To_TwoNumbers(
                  Integer.toString(cal.get(Calendar.DAY_OF_MONTH))
                  );
        return sYMD;
    }
    
    public static String getYYYY_W_From_YYYYMDOM(int iYear, int iMonthOfYear, int iDayOfMonth) {
        String sYW = "";
        Calendar cal = createSafeCal();
        cal.set(Calendar.YEAR, iYear);
        cal.set(Calendar.MONTH, TimePoint_to_CalMonth(iMonthOfYear) );
        cal.set(Calendar.DAY_OF_MONTH, iDayOfMonth);
        sYW = Integer.toString(cal.get(Calendar.YEAR)) + "-W" +
                TimePoint.extend_To_TwoNumbers(
                Integer.toString(cal.get(Calendar.WEEK_OF_YEAR)));
        return sYW;
    }
    
    public static String getW_From_YYYYMDOM(int iYear, int iMonthOfYear, int iDayOfMonth) {
        String sYW = "";
        Calendar cal = createSafeCal();
        cal.set(Calendar.YEAR, iYear);
        cal.set(Calendar.MONTH, TimePoint_to_CalMonth(iMonthOfYear) );
        cal.set(Calendar.DAY_OF_MONTH, iDayOfMonth);
        sYW = TimePoint.extend_To_TwoNumbers(Integer.toString(cal.get(Calendar.WEEK_OF_YEAR)));
        return sYW;
    }
    
    public static String getDOW_From_YYYYMDOM(int iYear, int iMonthOfYear, int iDayOfMonth) {
        String sYW = "";
        Calendar cal = createSafeCal();
        cal.set(Calendar.YEAR, iYear);
        cal.set(Calendar.MONTH, iMonthOfYear );
        cal.set(Calendar.DAY_OF_MONTH, iDayOfMonth);
        sYW = TimePoint.extend_To_TwoNumbers(Integer.toString(CalDOW_to_TimePoint(cal))) ;
        return sYW;
    }
    
}
