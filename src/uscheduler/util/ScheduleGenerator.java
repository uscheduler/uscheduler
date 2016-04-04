/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uscheduler.util;

import uscheduler.internaldata.Schedules;
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
    public static final int COURSE_MAX = 6;
    
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
     */
    public synchronized static int generate(Section[][] pSections) {
        if(pSections == null)
            throw new IllegalArgumentException("Null pSections");
        if(pSections.length < 2)
            throw new IllegalArgumentException("pSections must contain at least 2 Arrays of Sections");
        
        Schedules.deleteUnsaved();
        
        switch (pSections.length) {
            case 2:  return generate2(pSections);
            case 3:  return generate3(pSections);
            case 4:  return generate4(pSections);
            case 5:  return generate5(pSections);
            case 6:  return generate6(pSections);
            default: throw new IllegalArgumentException("Too many lists of sections.");
        }
    }
    

    private static int generate2(Section[][] pSections) {
        int schedulesSizeBefore = Schedules.size();
        for (Section sec0 : pSections[0]) 
            for (Section sec1 : pSections[1]) 
                Schedules.addSchedule(sec0, sec1);

        return Schedules.size() - schedulesSizeBefore;     
    }
    
    private static int generate3(Section[][] pSections) {
        int schedulesSizeBefore = Schedules.size();
        for (Section sec0 : pSections[0]) 
            for (Section sec1 : pSections[1]) 
                if (!sec0.overlaps(sec1)) 
                    for (Section sec2 : pSections[2]) 
                        Schedules.addSchedule(sec0, sec1, sec2);
        
        return Schedules.size() - schedulesSizeBefore;
    }
    private static int generate4(Section[][] pSections) {
        int schedulesSizeBefore = Schedules.size();
        
        for (Section sec0 : pSections[0]) 
            for (Section sec1 : pSections[1]) 
                if (!sec0.overlaps(sec1)) 
                    for (Section sec2 : pSections[2]) 
                        if (!sec0.overlaps(sec2) && !sec1.overlaps(sec2))
                            for (Section sec3 : pSections[3]) 
                                Schedules.addSchedule(sec0, sec1, sec2, sec3);
            
        return Schedules.size() - schedulesSizeBefore;
    }
    
    private static int generate5(Section[][] pSections) {
        int schedulesSizeBefore = Schedules.size();
        
        for (Section sec0 : pSections[0]) 
            for (Section sec1 : pSections[1]) 
                if (!sec0.overlaps(sec1)) 
                    for (Section sec2 : pSections[2]) 
                        if (!sec0.overlaps(sec2) && !sec1.overlaps(sec2))
                            for (Section sec3 : pSections[3]) 
                                if (!sec0.overlaps(sec3) && !sec1.overlaps(sec3) && !sec2.overlaps(sec3))
                                    for (Section sec4 : pSections[4]) 
                                        Schedules.addSchedule(sec0, sec1, sec2, sec3, sec4);
        return Schedules.size() - schedulesSizeBefore;

    }
    
    private static int generate6(Section[][] pSections) {
        int schedulesSizeBefore = Schedules.size();
        
        for (Section sec0 : pSections[0]) 
            for (Section sec1 : pSections[1]) 
                if (!sec0.overlaps(sec1)) 
                    for (Section sec2 : pSections[2]) 
                        if (!sec0.overlaps(sec2) && !sec1.overlaps(sec2))
                            for (Section sec3 : pSections[3]) 
                                if (!sec0.overlaps(sec3) && !sec1.overlaps(sec3) && !sec2.overlaps(sec3))
                                    for (Section sec4 : pSections[4]) 
                                        if (!sec0.overlaps(sec4) && !sec1.overlaps(sec4) && !sec2.overlaps(sec4) && !sec3.overlaps(sec4))
                                            for (Section sec5 : pSections[5])
                                                Schedules.addSchedule(sec0, sec1, sec2, sec3, sec4, sec5);
        return Schedules.size() - schedulesSizeBefore;

    }
}
