package temporal.rules;

import java.util.regex.*;
import java.util.*;
import java.util.ArrayList;

public class TempVariables {
	
    public String stempY = "";
    public String stempX= "";
    public String stemp= "";
    public String stempDU= "";
    public String stempH= "";
    public String stempT= "";
    public String stempNo= "";
    public String stempMod= "";
    public String stRD1= "";
    
    public String stempHour= "";
    public String stempMin= "";
    public String stempSec= "";
    public String stempMiliSec= "";
    public String stempAP= "";
    
    public String stempLER= "";
    public String stempLN= "";
    public String stempLP= "";
    public String stempPastDate="";
    
    public String stempTU= "";
    public String stempTZone = "";
    
    public String stempPart= "";
    public String stempSingularDate= "";
    public String stempNumPluralDate= "";
    public String stempRecursiveDate= "";
    
    public String stempSingularTime = "";
    public String stempTX = "";
    public String stempNumPluralTime ="";
    public String stRT1 = "";
    public String stempRecursiveTime = "";
    
    public String stempRecDate = "";
    
    public String stempMonth= "";
    public String stempSeasonName= "";
    public int iordNumber;
    public String stempD = "";
    public OrdinalNumber ordWords;
    
    //results
    public  String sMatch ="";
    public  String sCurrentMatch = "";
    public  String sPrep= "";
    public  MatchResult matchResult;
    
    //TimePoint
    public  TimePoint crtTimePoint;
    public  List<TimePoint> rangeTimePoint;
    public  TimePoint refTimePoint;

    public TempVariables(){
        ordWords = new OrdinalNumber();
        crtTimePoint = new TimePoint();
        refTimePoint = new TimePoint();
        rangeTimePoint = new ArrayList<TimePoint>();
        sMatch = "";
        sPrep = "";
    }

    public void copy(TempVariables tmp){
        ordWords.copy(tmp.ordWords);
        crtTimePoint.copy(tmp.crtTimePoint);
        refTimePoint.copy(tmp.crtTimePoint);
    }

}