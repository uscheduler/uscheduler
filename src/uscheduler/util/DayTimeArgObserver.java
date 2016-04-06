/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uscheduler.util;

import uscheduler.global.UTime;
import uscheduler.util.SectionsQuery.DayTimeArg;


/**
 * A class can implement the DayTimeArgObserver interface when it wants to be informed of changes in DayTimeArg objects.
 * @author Matt Bush
 */
public interface DayTimeArgObserver {
    
    /**
     * This method is called by the DayTimeArg object, for each DayTimeArgObserver in it's observers set, when the  maxEnd value of the DayTimeArg object changes.
     * 
     * @param dta the  DayTimeArg object whose maxEnd value changed.
     * @param pOldMaxEnd the value of maxEnd before it changed
     */
    public void maxEndChanged(DayTimeArg dta, UTime pOldMaxEnd);
    
    /**
     * This method is called by the DayTimeArg object, for each DayTimeArgObserver in it's observers set, when the minStart value of the DayTimeArg object changes.
     * 
     * @param dta the  DayTimeArg object whose minStart value changed.
     * @param pOldMinStart the value of minStart before it changed
     */
    public void minStartChanged(DayTimeArg dta, UTime pOldMinStart);
    
}
