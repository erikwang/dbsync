/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package dbsync;

/**
 *
 * @author Erik
 */
public class FunctionalDependency {
    int fdtype; // 1 = string; 2 = numeric
    int id;
    String attribute; // LHS
    String operator;
    float value; // RHS

    public float getValue() {
        return value;
    }

    public String getOperator() {
        return operator;
    }

    public int getId() {
        return id;
    }

    public int getFdtype() {
        return fdtype;
    }

    public String getAttribute() {
        return attribute;
    }


    public FunctionalDependency(int fdtype, int id, String attribute, String operator, float value) {
        this.fdtype = fdtype;
        this.id = id;
        this.attribute = attribute;
        this.operator = operator;
        this.value = value;
    }

}
