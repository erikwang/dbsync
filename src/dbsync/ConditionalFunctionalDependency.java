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
    int cfdsn;
    String[] units;
    Vector<String[]> LHS;
    Vector<String[]> RHS;

    public int getCfdsn() {
        return cfdsn;
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
    
    
 }
