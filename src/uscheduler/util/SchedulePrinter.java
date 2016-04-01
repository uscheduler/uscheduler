/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uscheduler.util;

import java.io.File;

/**
 * A singleton class consisting of a single static method that prints schedule to a file.
 * @author Matt Bush
 */
public final class SchedulePrinter {

    /**
     * Prints to a file, all Schedules s in the Schedules table such that s.isSaved() == true.
     * <br>
     * <b>!!!NOT YET IMPLEMENTED!!!</b>
     * <br>
     * @param pFile the file to print to. If no such file exists it will be created.
     * @param pOverwrite specifies if the file should be overwritten if the file already exists
     */
    public static void print(File pFile, boolean pOverwrite) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
}
