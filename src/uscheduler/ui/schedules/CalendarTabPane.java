/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uscheduler.ui.schedules;

import javafx.scene.control.TabPane;
import uscheduler.internaldata.Schedules.Schedule;
import uscheduler.internaldata.Schedules.Schedule.SessionPartition;

/**
 *
 * @author Matt
 */
public class CalendarTabPane extends TabPane{
    
    private Schedule cSchedule;
    
    public CalendarTabPane(Schedule pSchedule){
        if(pSchedule==null)
            throw new IllegalArgumentException("Null pSchedule argument.");
        cSchedule = pSchedule;
        //super.setMaxHeight(Double.MAX_VALUE);
        for(SessionPartition sp : cSchedule.sessionPartitions1()){
            System.out.println("for(SessionPartition sp : cSchedule.sessionPartitions1())...super.getTabs().add(new CalendarTab(sp));");
            super.getTabs().add(new CalendarTab(sp));
        }
    }
    
}
