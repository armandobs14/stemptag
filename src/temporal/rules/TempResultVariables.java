package temporal.rules;

import java.util.ArrayList;
import java.util.regex.*;
import java.util.*;

public class TempResultVariables {
	
    GlobalVariables pGlobalVariables;
    
    //intialValues
    public int iInitialGroupNumber;
    public  TimePoint initialTimePoint;
    public  List<TimePoint> initial_rangeTimePoint;
    boolean bAtLeastOneResult;

    //best results
    public  MatchResult bestmatchResult;
    public  TimePoint bestTimePoint;
    
    int iBestPrevGroupNumber;
    
    //temporal variables
    public TempVariables tempVariables;
    public String stempY;    

    public TempResultVariables(GlobalVariables glb) {
        pGlobalVariables = glb;
        initialTimePoint = new TimePoint();        
        bestmatchResult = null;
        bestTimePoint = new TimePoint();
        initial_rangeTimePoint = new ArrayList<TimePoint>();
    }
    
    public void setBestMatchResult(MatchResult mResult){
        bestmatchResult = mResult;
    }

    public void setCommon(){
        tempVariables = pGlobalVariables.tempVariables;
        tempVariables.copy(pGlobalVariables.tempVariables);
    }

    public void setInitialValues(int[] iPrevGroupNumber){
        iInitialGroupNumber = iPrevGroupNumber[0];
        initialTimePoint.copy( pGlobalVariables.tempVariables.crtTimePoint );
        initial_rangeTimePoint = TimePoint.copyArray(pGlobalVariables.tempVariables.rangeTimePoint);
        bAtLeastOneResult = false;        
        setCommon();
    }

    public void getCommon(){
        pGlobalVariables.tempVariables = tempVariables;
    }
    
    public void resetInitialValues(int[] iPrevGroupNumber){
        iPrevGroupNumber[0] = iInitialGroupNumber;
        pGlobalVariables.tempVariables.crtTimePoint.copy(initialTimePoint);
        pGlobalVariables.tempVariables.rangeTimePoint = TimePoint.copyArray(initial_rangeTimePoint);
        getCommon();
    }
    
    public void restoreBestResults(int[] iPrevGroupNumber){
        ConstRegEx.resetIntArray(iBestPrevGroupNumber, iPrevGroupNumber);
        pGlobalVariables.tempVariables.crtTimePoint.copy(bestTimePoint);
        pGlobalVariables.tempVariables.rangeTimePoint = TimePoint.copyArray(initial_rangeTimePoint);
        pGlobalVariables.tempVariables.matchResult = bestmatchResult;
        getCommon();
    }
    
    public boolean setBestResult( int iPrevGrNr) {
            setBestMatchResult(pGlobalVariables.tempVariables.matchResult);
            iBestPrevGroupNumber = iPrevGrNr;
            bestTimePoint.copy(pGlobalVariables.tempVariables.crtTimePoint);
            initial_rangeTimePoint = TimePoint.copyArray(pGlobalVariables.tempVariables.rangeTimePoint);
            bAtLeastOneResult = bAtLeastOneResult || true;
            setCommon();
            return true;
    }
    
    public void prepareReturn(int[] iPrevGroupNumber){
        if (bAtLeastOneResult) {
            restoreBestResults(iPrevGroupNumber);
        } else {
            resetInitialValues(iPrevGroupNumber);
        }
    }

}