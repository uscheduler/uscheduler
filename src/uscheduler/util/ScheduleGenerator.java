/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uscheduler.util;

import java.util.HashSet;
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
    public static final int COURSE_MAX = 7;


    private static final HashSet<ScheduleGeneratorObserver> cObservers = new HashSet<>();
    
    
    /**
     * Adds a {@link uscheduler.util.ScheduleGeneratorObserver ScheduleGeneratorObserver} to the <code>ScheduleGenerator</code>. 
     * A <code>ScheduleGeneratorObserver</code> object <code>sgo[i]</code> added to the <code>ScheduleGenerator</code> 
     * will be notified after an execution of <code>{@link #generate(uscheduler.internaldata.Sections.Section[][]) generate}</code>.
     * @param pObserver the <code>ScheduleGeneratorObserver</code> to be added to the <code>ScheduleGenerator</code>. Not null.
     */    
    public static void addObserver(ScheduleGeneratorObserver pObserver){
        if(pObserver==null)
            throw new IllegalArgumentException("Null pObserver argument");
        cObservers.add(pObserver);
    }
    /**
     * Removes a {@link uscheduler.util.ScheduleGeneratorObserver ScheduleGeneratorObserver} from the <code>ScheduleGenerator</code>. 
     * Removing a <code>ScheduleGeneratorObserver</code> from the <code>ScheduleGenerator</code> 
     * means the <code>ScheduleGeneratorObserver</code> will no longer be notified 
     * when <code>{@link #generate(uscheduler.internaldata.Sections.Section[][]) generate}</code> executes.
     * @param pObserver the <code>ScheduleGeneratorObserver</code> to be removed from the <code>ScheduleGenerator</code>. 
     */
    public static void removeObserver(ScheduleGeneratorObserver pObserver){
        cObservers.remove(pObserver);
    }
    
    /**
     * Need to describe...
     * 
     * @param pSections a two-dimensional array of Section objects from which to generate schedules.
     */
    public static int generate(Section[][] pSections) {
        if(pSections == null)
            throw new IllegalArgumentException("Null pSections");
        if(pSections.length < 2)
            throw new IllegalArgumentException("pSections must contain at least 2 Arrays of Sections");
        
        Schedules.deleteUnsaved();
        int count;
        switch (pSections.length) {
            case 2:  
                count = generate2(pSections);
                notifyObservers();
                break;
            case 3:  
                count = generate3(pSections);
                notifyObservers();
                break;              
            case 4:  
                count = generate4(pSections);
                notifyObservers();
                break;  
            case 5:  
                count = generate5(pSections);
                notifyObservers();
                break;             
            case 6:  
                count = generate6(pSections);
                notifyObservers();
                break;               
            case 7:  
                count = generate7(pSections);
                notifyObservers();
                break;               
            default: throw new IllegalArgumentException("Too many lists of sections.");
        }
        return count;
    }
    
    private static void notifyObservers(){
        for(ScheduleGeneratorObserver sgo : cObservers)
            sgo.schedulesGenerated();
    }
    private static int generate2(Section[][] pSections) {
        int numGeneratd = 0;
        for (Section sec0 : pSections[0]) 
            for (Section sec1 : pSections[1]) 
                if(Schedules.addSchedule(sec0, sec1))
                    numGeneratd++;

        return numGeneratd;     
    }
    
    private static int generate3(Section[][] pSections) {
        int numGeneratd = 0;
        for (Section sec0 : pSections[0]) 
            for (Section sec1 : pSections[1]) 
                if (!sec0.overlaps(sec1)) 
                    for (Section sec2 : pSections[2]) 
                        if(Schedules.addSchedule(sec0, sec1, sec2))
                            numGeneratd++;
        
        return numGeneratd;
    }
    private static int generate4(Section[][] pSections) {
        int numGeneratd = 0;
        
        for (Section sec0 : pSections[0]) 
            for (Section sec1 : pSections[1]) 
                if (!sec0.overlaps(sec1)) 
                    for (Section sec2 : pSections[2]) 
                        if (!sec0.overlaps(sec2) && !sec1.overlaps(sec2))
                            for (Section sec3 : pSections[3]) 
                                if(Schedules.addSchedule(sec0, sec1, sec2, sec3))
                                    numGeneratd++;
            
        return numGeneratd;
    }
    
    private static int generate5(Section[][] pSections) {
        int numGeneratd = 0;
        
        for (Section sec0 : pSections[0]) 
            for (Section sec1 : pSections[1]) 
                if (!sec0.overlaps(sec1)) 
                    for (Section sec2 : pSections[2]) 
                        if (!sec0.overlaps(sec2) && !sec1.overlaps(sec2))
                            for (Section sec3 : pSections[3]) 
                                if (!sec0.overlaps(sec3) && !sec1.overlaps(sec3) && !sec2.overlaps(sec3))
                                    for (Section sec4 : pSections[4]) 
                                        if(Schedules.addSchedule(sec0, sec1, sec2, sec3, sec4))
                                            numGeneratd++;
        
        return numGeneratd;

    }
    
    private static int generate6(Section[][] pSections) {
        int numGeneratd = 0;
        
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
                                                if(Schedules.addSchedule(sec0, sec1, sec2, sec3, sec4, sec5))
                                                    numGeneratd++;
        return numGeneratd;

    }
    private static int generate7(Section[][] pSections) {
        int numGeneratd = 0;
        
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
                                                if (!sec0.overlaps(sec5) && !sec1.overlaps(sec5) && !sec2.overlaps(sec5) && !sec3.overlaps(sec5) && !sec4.overlaps(sec5))
                                                    for (Section sec6 : pSections[6])
                                                        if(Schedules.addSchedule(sec0, sec1, sec2, sec3, sec4, sec5, sec6))
                                                            numGeneratd++;
        return numGeneratd;

    }
}
