/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uscheduler.util;

import uscheduler.internaldata.Sections.Section;

/**
 * A singleton class consisting of a single static method that generates schedules.
 * @author Matt Bush
 */
public final class ScheduleGenerator {
    /**
     * The constant that specifies the max number of courses allowed in a GenerateSchedules() operation. 
     * All code that depends on this value should reference this variable. 
     * The value is currently set at 8, but it can and probably will be changed once we run such an operation and determine performance.
     */
    public static final int COURSE_MAX = 8;
    
    /**
     * The constant that specifies the max number of schedules that will be generated in a GenerateSchedules() operation. 
     * All code that depends on this value should reference this variable. 
     * The value is currently set at 10000, but it can and probably will be changed once we run such an operation and determine performance.
     */
    public static final int SCHEDULE_MAX = 10000;

    /**
     * <br>
     * <b>!!!NOT YET IMPLEMENTED!!!</b>
     * <br>
     * @param pSections the array of arrays of Section objects from which to generate schedules.
     * @return the number of new Schedules that were added to the Schedules table as a result of this method.
     */
    public static int generate(Section[][] pSections) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
}
