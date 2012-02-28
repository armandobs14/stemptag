package temporal.rules;

import java.util.Hashtable;

public class OrdinalNumber {
    
	public Hashtable<String,Integer> htblVariables;
    
    public OrdinalNumber(){
        htblVariables = new Hashtable<String, Integer>();
    }
    
    public  OrdinalNumber(Integer iUnit,Integer iTy, Integer iHun, Integer iThou, Integer iVal){
        htblVariables = new Hashtable<String, Integer>();
        htblVariables.put("unit", iUnit);
        htblVariables.put("ty", iTy);
        htblVariables.put("hun", iHun);
        htblVariables.put("thou", iThou);
        htblVariables.put("val", iUnit + 10*iTy+100*iHun+1000*iThou);
    }
    
    public void set_unit(Integer iExpression) {
        htblVariables.put("unit", iExpression);
    }

    public Integer get_unit() {
        if (!htblVariables.containsKey("unit")) return 0;
        return htblVariables.get("unit");
    }
    
    public void set_ty(Integer iExpression) {
        htblVariables.put("ty", iExpression);
    }

    public Integer get_ty() {
        if (!htblVariables.containsKey("ty")) return 0;
        return htblVariables.get("ty");
    }

    public void teen(Integer iExpression) {
        this.set_ty( (int)iExpression/10);
        this.set_unit(iExpression %10);
    }

    public void set_hun(Integer iExpression) {
        htblVariables.put("hun", iExpression);
    }
    
    public Integer get_hun() {
        if (!htblVariables.containsKey("hun")) return 0;
        return htblVariables.get("hun");
    }        

    public void set_thou(Integer iExpression) {
        htblVariables.put("thou", iExpression);
    }
    
    public Integer get_thou() {
        if (!htblVariables.containsKey("thou")) return 0;
        return htblVariables.get("thou");
    }         

    public Integer test() {
        return get_unit() + get_ty() + get_hun() + get_thou();
    }

    public void set_val(Integer iExpression){
        htblVariables.put("val", iExpression);        
    }
    
    public Integer get_val(){
        if (this.test()!=0) {
            return this.get_unit() + 10*this.get_ty() + 100 * this.get_hun() + 1000 * this.get_thou();
        } else {
            if (!htblVariables.containsKey("val")) return 0;
            return htblVariables.get("val");
        }
    }
    
    public void copy(OrdinalNumber ord){
        this.htblVariables =new Hashtable<String, Integer>(ord.htblVariables);
    }

}