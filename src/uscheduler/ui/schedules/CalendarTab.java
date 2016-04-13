/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uscheduler.ui.schedules;

import java.time.DayOfWeek;
import javafx.scene.control.Tab;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import uscheduler.internaldata.Schedules.Schedule.SessionPartition;
import uscheduler.internaldata.Schedules.Schedule.SessionPartition.MeetingDayPartition;

/**
 *
 * @author Matt
 */
public class CalendarTab extends Tab{
    private SessionPartition cSessionPartition;
    private final HBox hbxDaysContainer = new HBox();
    
    public CalendarTab(SessionPartition pSessionPartition){
        if(pSessionPartition==null)
            throw new IllegalArgumentException("Null pSchedule argument.");
        cSessionPartition = pSessionPartition;
        
        for (DayOfWeek dow : DayOfWeek.values()) {
            
            MeetingDayPartition meetingDayPart = cSessionPartition.getMeetingDayPartition(dow);
            CalendarDay2 calDay = new CalendarDay2(dow, meetingDayPart);
            //calDay.setMaxWidth(Double.MAX_VALUE);
            //calDay.setMaxHeight(Double.MAX_VALUE);
            System.out.println("for(DayOfWeek dow : DayOfWeek.values()...hbxDaysContainer.getChildren().add(calDay)");
            hbxDaysContainer.getChildren().add(calDay);
            HBox.setHgrow(calDay, Priority.ALWAYS);
        }        
        
        super.setText(cSessionPartition.startDate() + " - " + cSessionPartition.endDate());
        super.setContent(hbxDaysContainer);
    }
}
