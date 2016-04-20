/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uscheduler.util;

/**
 * The interface implemented by a class in order to be an "observer" of changes made to a {@link uscheduler.util.SectionsQuery SectionsQuery} object.
 * An Observer "register" with a SectionsQuery object by adding itself to the SectionsQuery's observer list 
 * via the {@link uscheduler.util.SectionsQuery#addObserver(uscheduler.util.SectionsQueryObserver)   addObserver} method.
 * For various events of interest, the SectionsQuery will call the corresponding methods defined in this interface for each observer in the observers list.
 * 
 * @author Matt Bush
 */
public interface SectionsQueryObserver {
    
    /**
     * The method that will be called by the DayTimeArg, on each SectionsQueryObserver object in its observers list, 
     * when its {@link uscheduler.util.SectionsQuery#results() results} set changes.
     * 
     * @param pSQ the SectionsQuery object whose {@link uscheduler.util.SectionsQuery#results() results} set changed.
     */
    public void resultsChanged(SectionsQuery pSQ);
    
}
