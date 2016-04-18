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
import javafx.geometry.HPos;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.text.Text;
import uscheduler.internaldata.Schedules.Schedule.SessionPartition.MeetingDayPartition;
import uscheduler.internaldata.Sections.Section.MeetingTime;

/**
 * A GridPane with 1 column an 3 rows to display a MeetingDayPartition. 
 * The top row displays DayaOfWeek and is a fixed size.
 * The middle row displays start time, end time, and the course for each meeting time. The middle row has grow priority.
 * The third and last row displays th summary total minutes and is a fixed size
 * @author Matt
 */
public class CalendarDay2 extends GridPane{
    private final DayOfWeek cDayOfWeek;
    private MeetingDayPartition cMeetingDayPartition;
    private final Text txtTopDay = new Text();
    private final Text txtCenterDetail = new Text();
    private final Text txtBottomSummary = new Text();
    
    public CalendarDay2(DayOfWeek pDayOfWeek, MeetingDayPartition pMeetingDayPartition){
        if (pDayOfWeek == null)
            throw new IllegalArgumentException("Null pDayOfWeek argument.");
        cDayOfWeek = pDayOfWeek;
        cMeetingDayPartition = pMeetingDayPartition;
        this.setTextValues();
        //*********************
        
        super.add(txtTopDay, 0, 0);
        super.add(txtCenterDetail, 0, 1);
        super.add(txtBottomSummary, 0, 2);
        GridPane.setHalignment(txtTopDay, HPos.CENTER);
        GridPane.setHgrow(txtCenterDetail, Priority.ALWAYS);
     
        //topText.setStyle("-fx-font-weight: bold; -fx-padding: 3 0 3 0");

    }
    public void setMeetingDayPartition(MeetingDayPartition pMeetingDayPartition){
        cMeetingDayPartition = pMeetingDayPartition;
        this.setTextValues();
    }

    private void setTextValues(){
        txtTopDay.setText(cDayOfWeek.getDisplayName(TextStyle.FULL, Locale.getDefault()));
        
        if(cMeetingDayPartition == null){
            txtCenterDetail.setText(null);
            txtBottomSummary.setText(null);
        } else {
            txtBottomSummary.setText(Integer.toString(cMeetingDayPartition.minutesAtSchool()));
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
            txtCenterDetail.setText(sb.toString());                
        }       
    }
}
