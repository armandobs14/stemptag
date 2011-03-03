package temporal.rules;

import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.*;
import java.util.*;
import java.io.*;

public class GlobalVariables {
	
	public  String sInputFile;

	public  String sOutputFile;
	
	public  FileWriter fOutputFile;
	
	public  BufferedWriter out;

	public  BufferedWriter log;

	public boolean bPrintOutTheResults;

	public  Hashtable<String, String> hashtblMapValue;

	public  Hashtable<String, String> hashtbLexValues;

	public String resourceDirPath = ".";

	public TempVariables tempVariables;

	public GlobalVariables( boolean bPrintOut, String prefix ) {
		resourceDirPath=prefix;
		bPrintOutTheResults = bPrintOut;
		hashtblMapValue = new Hashtable<String, String>();
		hashtbLexValues = new Hashtable<String, String>();
		tempVariables = new TempVariables();
		readLexFile();
	}

	public GlobalVariables( boolean bPrintOut ) { this(bPrintOut,"."); }
	
	public GlobalVariables(String sInput, String sOutput, boolean bPrintOut) {
		sInputFile = sInput;
		sOutputFile = sOutput;
		try {
			fOutputFile = new FileWriter(sOutput);
			out = new BufferedWriter(fOutputFile);
			FileWriter fLog = new FileWriter(sOutput+".log");
			log = new BufferedWriter(fLog);
		} catch (Exception e) { System.err.println("Error: " + e.getMessage()); }
		bPrintOutTheResults = bPrintOut;
		hashtblMapValue = new Hashtable<String, String>();
		hashtbLexValues = new Hashtable<String, String>();
		tempVariables = new TempVariables();
	}

	public void readLexFile() {
		try {
			Logger.getLogger(GlobalVariables.class.getName()).log(Level.INFO, "Loading resource file [timex.lex]");

			File lexFile = new File(resourceDirPath+"/timex.lex");
			BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(lexFile)));			
			String sLine;
			String sPattern = "^(.*?)(?:(?:\\s*\\:\\:\\s*)|\t)(.*?)(?:(?:\t)(.*))?$";
			Pattern patternLex = Pattern.compile(sPattern);
			int iNr = 1;
			String[] arrsMatch = new String[3];
			while ((sLine = in.readLine()) != null) {
				Matcher m = patternLex.matcher(sLine);
				while (m.find()) {
					try {
						if (bPrintOutTheResults) System.out.println("Found " + m.group(0) + " at position " + m.start(0));
						if (m.start(1) < m.end(1)) {
							if (bPrintOutTheResults) System.out.println("group 1 " + m.group(1));
							arrsMatch[0] = m.group(1);
						} else arrsMatch[0] = "";
						if (m.start(2) < m.end(2)) {
							if (bPrintOutTheResults) System.out.println("group 2 " + m.group(2));
							arrsMatch[1] = m.group(2);
						} else arrsMatch[1] = "";
						if (m.start(3) < m.end(3)) {
							if (bPrintOutTheResults) System.out.println("group 3 " + m.group(3));
							arrsMatch[2] = m.group(3);
						} else arrsMatch[2] = "";
						String[] ids = arrsMatch[1].split("\\s");
						for (int i = 0; i < ids.length; i++) {
							createLexVariables(ids[i], arrsMatch[0]);
							if (arrsMatch[2].compareTo("") != 0) {
								hashtblMapValue.put(arrsMatch[0] + "_" + ids[i], arrsMatch[2]);
								hashtblMapValue.put(arrsMatch[0], arrsMatch[2]);
								if (ids[i].compareTo("TIMEZONEFULL") == 0) {
									hashtblMapValue.put(arrsMatch[0].toLowerCase() + "_" + ids[i], arrsMatch[2]);
								}
							}
						}
					} catch (Exception e) { }
				}
				if (bPrintOutTheResults && sLine.matches(sPattern)) {
					System.out.println(iNr + " -OK");
					iNr++;
				}
			}
		} catch (IOException e) {
			Logger.getLogger(GlobalVariables.class.getName()).log(Level.SEVERE, "Failed to load resource file [" +resourceDirPath+"/timex.lex]",e);
		}
	}

	public void createLexVariables(String sID, String sMatch3) {
		String sLexValue = "";
		String sPush = "\\Q" + sMatch3 + "\\E";
		if (hashtbLexValues.containsKey(sID)) {
			sLexValue = hashtbLexValues.get(sID).toString() + "|" + sPush;
			hashtbLexValues.put(sID, sLexValue);
		} else {
			hashtbLexValues.put(sID, sPush);
		}
	}

	public  void saveMatchValue_FromMapHash(String sHashKey, int iGroupIndex){
		tempVariables.crtTimePoint.set(sHashKey, 
			hashtblMapValue.get(
				tempVariables.matchResult.group(iGroupIndex).toLowerCase()
			)
		);
	}

	public String getResult_LowerCase(int iResGr){
		return tempVariables.matchResult.group(iResGr).toLowerCase();
	}
	
	public String getMapHashValue(String sHashKey){
		if (hashtblMapValue.containsKey(sHashKey)) return hashtblMapValue.get(sHashKey);
		return "";
	}

	public  void saveMatchValue_FromMatchResult(String sHashKey, int iGroupIndex){
		tempVariables.crtTimePoint.set(sHashKey, tempVariables.matchResult.group(iGroupIndex).toLowerCase());
	}  

	public String passPreviousMatchResult(int iMatchIndex, int[] iGroupIndex, int iNewGroupNumber){
		String sRes = tempVariables.matchResult.group(iMatchIndex);
		iGroupIndex[0] = iNewGroupNumber;
		try {
			sRes = sRes.replaceAll("\\?", "\\\\?");
		} catch(Exception e) {
			System.out.println(e.toString());
		}
		return sRes;
	}

	public void saveCurrentMatch(int iGroupIndex){
		if (tempVariables.matchResult != null) {
			tempVariables.sCurrentMatch = tempVariables.matchResult.group(iGroupIndex);
		}
	}

	public void concatenatePrefix(int iResGr){
		if (tempVariables.matchResult != null) {
			if ((iResGr < tempVariables.matchResult.groupCount()) && (tempVariables.matchResult.group(iResGr) != null )) {
				tempVariables.sCurrentMatch = tempVariables.matchResult.group(iResGr) + tempVariables.sCurrentMatch;
			}
		}
	}

	public void concatenateSuffix(int iResGr) {
		if (tempVariables.matchResult != null) {
			if ((iResGr < tempVariables.matchResult.groupCount()) && (tempVariables.matchResult.group(iResGr) != null )) {
				tempVariables.sCurrentMatch = tempVariables.sCurrentMatch + tempVariables.matchResult.group(iResGr);
			}
		}
	}

	public boolean prepareReturn(boolean bResult) {
		if (!bResult) tempVariables.sCurrentMatch = "";
		return bResult;
	}
	
	public void startCapture() {
		tempVariables.sCurrentMatch = "";
	}

}