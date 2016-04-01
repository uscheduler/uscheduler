/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uscheduler.externaldata;

/**
 * A Checked exception that is thrown when a KSU web page does have the expected HTML structure, but it contains no data to be parsed.
 * @author Matt Bush
 */
public class NoDataFoundException extends Exception {
    /**
     * Constructs an instance of <code>NoDataFound</code> with the specified
     * detail message.
     *
     * @param msg the detail message.
     */
    public NoDataFoundException(String msg) {
        super(msg);
    }
}
