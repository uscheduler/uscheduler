/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uscheduler.util;

/**
 * The interface implemented by a class in order to be an "observer" of the execution of 
 * the {@link uscheduler.util.ScheduleGenerator#generate(uscheduler.internaldata.Sections.Section[][]) ScheduleGenerator.generate} method.
 * An Observer "registers" with the <code>ScheduleGenerator</code> class by adding itself to its observer list 
 * via the {@link uscheduler.util.ScheduleGenerator#addObserver(uscheduler.util.ScheduleGeneratorObserver) addObserver} method.
 * The <code>ScheduleGenerator</code> will call {@link #schedulesGenerated() schedulesGenerated()}  for each observer in the observers list after it executes its <code>generate</code> method.
 * 
 * @author Matt Bush
 */
public interface ScheduleGeneratorObserver {
    /**
     * The method that will be called by the <code>ScheduleGenerator</code>, on each <code>ScheduleGeneratorObserver</code> in its observers list, 
     * after it executes {@link uscheduler.util.ScheduleGenerator#generate(uscheduler.internaldata.Sections.Section[][]) generate}.
     */
    public void schedulesGenerated();    
}


