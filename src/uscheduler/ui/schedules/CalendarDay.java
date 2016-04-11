/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uscheduler.ui.schedules;

import java.time.DayOfWeek;
import java.time.format.TextStyle;
import java.util.Iterator;
import java.util.Locale;
import java.util.Set;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import uscheduler.internaldata.Schedules.Schedule.SessionPartition;
import uscheduler.internaldata.Schedules.Schedule.SessionPartition.MeetingDayPartition;
import uscheduler.internaldata.Sections.Section.MeetingTime;

/**
 *
 * @author Matt
 */
public class CalendarDay extends VBox{
    private final DayOfWeek cDayOfWeek;
    private SessionPartition cSessionPartition;
    private final Label lblDayHeader = new Label();
    private final Label lblDayDetail = new Label();
    
    public CalendarDay(DayOfWeek pDayOfWeek, SessionPartition pSessionPartition){
        if (pDayOfWeek == null)
            throw new IllegalArgumentException("Null pDayOfWeek argument.");
        cDayOfWeek = pDayOfWeek;
        cSessionPartition = pSessionPartition;
        
        lblDayHeader.setText(getHeaderText());
        lblDayHeader.setStyle("-fx-font-family: \"Comic Sans MS\"; -fx-font-size: 20; -fx-text-fill: darkred;");
        
        lblDayDetail.setText(getDetailText());
        lblDayDetail.setWrapText(true);
        lblDayDetail.setStyle("-fx-font-family: \"Comic Sans MS\"; -fx-font-size: 20; -fx-text-fill: darkred;");
        
        super.getChildren().addAll(lblDayHeader, lblDayDetail);
        
    }
    public void setSessionPartition(SessionPartition pSessionPartition){
        cSessionPartition = pSessionPartition;
        lblDayHeader.setText(getHeaderText());
        lblDayDetail.setText(getDetailText());
        
    }
    private String getHeaderText(){
        return cDayOfWeek.getDisplayName(TextStyle.FULL, Locale.getDefault());
    }
    private String getDetailText(){
        if(cSessionPartition == null)
            return "";
        MeetingDayPartition dowP = cSessionPartition.getMeetingDayPartition(cDayOfWeek);
        if(dowP == null)
            return "";
        
        Set<MeetingTime> meetingTimes = dowP.meetingTimes1();
        //if(meetingTimes.isEmpty()) //This should never happen!
        //    return "";

        StringBuilder sb = new StringBuilder();
        Iterator<MeetingTime> meetingTimesIT = meetingTimes.iterator();
        MeetingTime mt = meetingTimesIT.next();
        
        sb.append(mt.startTime())
        .append(" - ")
        .append(mt.endTime())
        .append(" ")
        .append(mt.section().course().subject().subjectAbbr())
        .append(" ")
        .append(mt.section().course().courseNum());
        
        while(meetingTimesIT.hasNext()){
            mt = meetingTimesIT.next();
            sb.append("\n")
            .append(mt.startTime())
            .append(" - ")
            .append(mt.endTime())
            .append(" ")
            .append(mt.section().course().subject().subjectAbbr())
            .append(" ")
            .append(mt.section().course().courseNum());           
        }
        return sb.toString();
    }
}
