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
import javafx.geometry.Pos;
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
    private MeetingDayPartition cMeetingDayPartition;
    private final Label lblTopDay = new Label();
    private final Label lblCenterDetail = new Label();
    private final Label lblBottomSummary = new Label();
    
    public CalendarDay(DayOfWeek pDayOfWeek, MeetingDayPartition pMeetingDayPartition){
        if (pDayOfWeek == null)
            throw new IllegalArgumentException("Null pDayOfWeek argument.");
        cDayOfWeek = pDayOfWeek;
        cMeetingDayPartition = pMeetingDayPartition;
        this.setTextValues();
        
        //*********************

        lblTopDay.setMaxWidth(Double.MAX_VALUE);
        lblTopDay.setAlignment(Pos.CENTER);
        //txtTop.setTextAlignment(TextAlignment.CENTER);
        lblTopDay.setStyle("-fx-font-weight: bold; -fx-padding: 3 0 3 0;-fx-border-color: black; -fx-border-width: 1 1 1 1;");

        lblCenterDetail.setWrapText(true);
        lblCenterDetail.setMaxWidth(Double.MAX_VALUE);
        lblCenterDetail.setStyle("-fx-padding: 2 2 2 2;-fx-border-color: black; -fx-border-width: 0 1 1 1;");
        
        lblBottomSummary.setMaxWidth(Double.MAX_VALUE);
        lblBottomSummary.setAlignment(Pos.CENTER);
        lblBottomSummary.setStyle("-fx-border-color: black; -fx-border-width: 0 1 1 1;");

        super.getChildren().addAll(lblTopDay, lblCenterDetail, lblBottomSummary);
        super.setMinWidth(275);
    }
    public void setMeetingDayPartition(MeetingDayPartition pMeetingDayPartition){
        cMeetingDayPartition = pMeetingDayPartition;
        this.setTextValues();
    }

    private void setTextValues(){
        lblTopDay.setText(cDayOfWeek.getDisplayName(TextStyle.FULL, Locale.getDefault()));
        
        if(cMeetingDayPartition == null){
            lblCenterDetail.setText(null);
            lblBottomSummary.setText(null);
        } else {
            lblBottomSummary.setText(Integer.toString(cMeetingDayPartition.minutesAtSchool()));
            Set<MeetingTime> meetingTimes = cMeetingDayPartition.meetingTimes1();
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
            lblCenterDetail.setText(sb.toString());                
        }
            
            
            
            
            
            
    }
}
