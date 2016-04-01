/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uscheduler.global;

/**
 *
 * @author Matt Bush
 */
public enum InstructionalMethod {

    CLASSROOM ("Classroom"),
    HYBRID ("Hybrid"),
    ONLINE_95 ("95% Online"),
    ONLINE_100 ("100% Online");
    
    private final String cStringValue;
    
    InstructionalMethod(String pStringValue) {
        this.cStringValue = pStringValue;
    }
    
    @Override
    public String toString(){return cStringValue;}
}