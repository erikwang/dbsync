/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package dbsync;


import java.util.*;
/**
 *
 * @author Erik
 */
public class ConditionalFunctionalDependency {
    private int cfdsn;
    private String[] units;
    private String CFDAUTOCLEAN;
    private String CFDSUGGESTSQL;
    private Vector<String[]> LHS;
    private Vector<String[]> RHS;

    public int getCfdsn() {
        return cfdsn;
    }

    public String getCFDAUTOCLEAN(){
        return CFDAUTOCLEAN;
    }
    
    public String getCFDSUGGESTSQL(){
        return CFDSUGGESTSQL;
    }
    
    
    public Vector<String[]> getLHS() {
        return LHS;
    }
    
    public Vector<String[]> getRHS() {
        return RHS;
    }

    public ConditionalFunctionalDependency(int cfdsn, Vector<String[]> LHS, Vector<String[]> RHS) {
        this.cfdsn = cfdsn;
        this.LHS = LHS;
        this.RHS = RHS;
    }
    
    public String getRHSUpdateString(){
        String update = "";
        for (int t=0;t< RHS.size();t++){
            update = update + " ," +RHS.get(t)[0]+"= '"+RHS.get(t)[1]+"'";
        }
        return update;
        
    }
    
 }
