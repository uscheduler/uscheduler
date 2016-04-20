/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uscheduler.util;

import uscheduler.global.UTime;
import uscheduler.util.SectionsQuery.DayTimeArg;


/**
 * The interface implemented by a class in order to be an "observer" of changes made to a {@link uscheduler.util.SectionsQuery.DayTimeArg DayTimeArg} object.
 * An Observer "register" with a DayTimeArg object by adding itself to the DayTimeArg's observer list 
 * via the {@link uscheduler.util.SectionsQuery.DayTimeArg#addObserver(uscheduler.util.DayTimeArgObserver)  addObserver} method.
 * For various events of interest, the DayTimeArg will call the corresponding methods defined in this interface for each observer in the observers list.
 * 
 * @author Matt Bush
 */
public interface DayTimeArgObserver {
    
    /**
     * The method that will be called by the DayTimeArg, on each DayTimeArgObserver object in its observers list, 
     * when its {@link uscheduler.util.SectionsQuery.DayTimeArg#maxEnd() maxEnd()} value changes.
     * 
     * @param pDTA the  DayTimeArg object whose maxEnd value changed.
     * @param pOldMaxEnd the value of maxEnd before it changed.
     */
    public void maxEndChanged(DayTimeArg pDTA, UTime pOldMaxEnd);
    
    /**
     * The method that will be called by the DayTimeArg, on each DayTimeArgObserver object in its observers list, 
     * when its {@link uscheduler.util.SectionsQuery.DayTimeArg#minStart() minStart()} value changes.
     * 
     * @param pDTA the  DayTimeArg object whose minStart value changed.
     * @param pOldMinStart the value of minStart before it changed.
     */
    public void minStartChanged(DayTimeArg pDTA, UTime pOldMinStart);
    
}
