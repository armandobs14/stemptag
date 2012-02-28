package temporal.rules;

public class Const {
	
    static public int IfDef = 1; 
    
    static public String EOL_WIN32 = "\r\n";
    
    public static void writeSpecificError(Exception e, String sParagraph) {
        System.out.println(
        e.getStackTrace()[0].getMethodName() + " - " +e.toString()+"\n"+sParagraph);
    }

}
