/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uscheduler.util;

/**
 * A class can implement the SectionsQueryObserver interface when it wants to be informed of changes in SectionsQuery objects.
 * @author Matt Bush
 */
public interface SectionsQueryObserver {
    
    /**
     * This method is called by the SectionsQuery object, for each SectionsQueryObserver in it's observers set, when the results of the SectionsQuery object changes.
     * 
     * @param sq the  SectionsQuery object whose results changed.
     */
    public void resultsChanged(SectionsQuery sq);
    
}
