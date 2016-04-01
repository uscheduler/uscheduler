/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uscheduler.externaldata;

/**
 * A Checked exception that is thrown when a KSU web page does not have the expected HTML structure and thus cannot be parsed.
 * 
 * @author Matt Bush
 */
public class HTMLFormatException  extends Exception {
    /**
     * Constructs an instance of <code>HTMLFormatException</code> with the specified
     * detail message.
     *
     * @param pErrMsg the detail message.
     */    
    public HTMLFormatException(String pErrMsg)
    {
        super(pErrMsg);
    }    
}