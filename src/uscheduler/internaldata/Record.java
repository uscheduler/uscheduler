/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uscheduler.internaldata;

/**
 * The root interface of all conceptual "record" classes.
 * <p>This interface has no methods and exists purely for documenting shared properties of related classes so 
 * that the same documentation doesn't need to be repeated for each implementing class. 
 * <p>Each "record" class is defined as a nested class inside its corresponding "table" class. 
 * <p>Each "record" class has only private constructors so that only its corresponding "table" class can create instances of it.
 * 
 * 
 * @see uscheduler.internaldata.Table
 * @author Matt Bush
 */
public interface Record {

}
