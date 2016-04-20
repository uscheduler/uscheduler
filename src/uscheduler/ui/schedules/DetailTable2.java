/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uscheduler.ui.schedules;

import javafx.scene.control.Label;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import uscheduler.internaldata.Instructors.Instructor;
import uscheduler.internaldata.Schedules.Schedule;
import uscheduler.internaldata.Sections;
import uscheduler.internaldata.Sections.Section;
import uscheduler.internaldata.Sections.Section.MeetingTime;

//http://stackoverflow.com/questions/13381067/simplestringproperty-and-simpleintegerproperty-tableview-javafx

/**
 *
 * @author Matt Bush
 */
public class DetailTable2 extends GridPane{
    
    private final Schedule cSchedule;

    private static final int TABLE_MIN_WIDTH = 1065;
    //The percent widths of each column
    private static final double SUBJ_AND_COURSE_PW = 100*95/1065d;//95/1065
    private static final double SEC_NUM_PW = 100*50/1065d;
    private static final double CRN_PW = 100*65/1065d;
    private static final double SEATS_PW = 100*50/1065d;
    private static final double WAIT_PW = 100*50/1065d;
    private static final double INSTRUCTIONAL_PW = 100*100/1065d;
    private static final double CAMPUS_PW = 100*120/1065d;
    private static final double DAYS_PW = 100*125/1065d;
    private static final double MEETING_TIMES_PW = 100*110/1065d;
    private static final double INSTRUCOR_PW = 100*150/1065d;
    private static final double SESS_NAME_PW = 100*150/1065d;

    
    public DetailTable2(Schedule pSchedule){
        if(pSchedule==null)
            throw new IllegalArgumentException("Null pSchedule argument.");
        cSchedule = pSchedule;
        
        super.setMinWidth(TABLE_MIN_WIDTH);
        super.setGridLinesVisible(true);
        
        this.addCoulmConstraints();
        this.writeColumnLabels();
        int rowIndex = 1;
        for (Section sec : cSchedule.sections2(Sections.COURSE_PK_ASC)) {
            MeetingTime[] meetingTimes = sec.meetings2();
            Instructor[] instructors = sec.instructors2();
            int max = Math.max(meetingTimes.length, instructors.length);
            
            writeMainSectionRow(rowIndex, sec, (meetingTimes.length > 0)?meetingTimes[0]:null, (instructors.length > 0)?instructors[0]:null);
            
            rowIndex++;
            
            for(int i = 1; i < max; i++){
                writeContinuedSectionRow(rowIndex, (meetingTimes.length > i)?meetingTimes[i]:null, (instructors.length > i)?instructors[i]:null);
                rowIndex++;
            }
        } 
    }

    private void writeColumnLabels() {

        Label lbl;

        lbl = new Label("Course");       
        super.add(lbl, 0, 0);
        lbl.getStyleClass().add("label-detail-table-label");

        lbl = new Label("Sec#");       
        super.add(lbl, 1, 0);
        lbl.getStyleClass().add("label-detail-table-label");

        lbl = new Label("CRN");       
        super.add(lbl, 2, 0);
        lbl.getStyleClass().add("label-detail-table-label");

        lbl = new Label("Seats");       
        lbl.getStyleClass().add("label-detail-table-label");
        super.add(lbl, 3, 0);

        lbl = new Label("Wait");       
        lbl.getStyleClass().add("label-detail-table-label");
        super.add(lbl, 4, 0);    

        lbl = new Label("Method");       
        super.add(lbl, 5, 0);
        lbl.getStyleClass().add("label-detail-table-label");

        lbl = new Label("Campus");       
        lbl.getStyleClass().add("label-detail-table-label");
        super.add(lbl, 6, 0);                    

        lbl = new Label("Days");       
        lbl.getStyleClass().add("label-detail-table-label");
        super.add(lbl, 7, 0);      

        lbl = new Label("Times");       
        lbl.getStyleClass().add("label-detail-table-label");
        super.add(lbl, 8, 0);                 

        lbl = new Label("Instructors");       
        lbl.getStyleClass().add("label-detail-table-label");
        super.add(lbl, 9, 0);                    

        lbl = new Label("Session");       
        lbl.getStyleClass().add("label-detail-table-label");
        super.add(lbl, 10, 0);

    }
    
    private void writeMainSectionRow(int pRowIndex, Section pSection, MeetingTime pMeetingTime, Instructor pInstructor) {

        Label lbl;

        lbl = new Label(pSection.course().subject().subjectAbbr() + " " + pSection.course().courseNum());       
        super.add(lbl, 0, pRowIndex);
        lbl.getStyleClass().add("label-detail-table-data");

        lbl = new Label(pSection.sectionNumber());       
        super.add(lbl, 1, pRowIndex);
        lbl.getStyleClass().add("label-detail-table-data");

        lbl = new Label(Integer.toString(pSection.crn()));       
        super.add(lbl, 2, pRowIndex);
        lbl.getStyleClass().add("label-detail-table-data");

        lbl = new Label(Integer.toString(pSection.seatsAvailable()));       
        lbl.getStyleClass().add("label-detail-table-data");
        super.add(lbl, 3, pRowIndex);

        lbl = new Label(Integer.toString(pSection.waitlistAvailable()));       
        lbl.getStyleClass().add("label-detail-table-data");
        super.add(lbl, 4, pRowIndex);    

        lbl = new Label(pSection.instructionalMethod().toString());       
        super.add(lbl, 5, pRowIndex);
        lbl.getStyleClass().add("label-detail-table-data");

        if(pSection.campus()!=null){
            lbl = new Label(pSection.campus().campusName());       
            lbl.getStyleClass().add("label-detail-table-data");
            super.add(lbl, 6, pRowIndex);                    
        }

        if(pMeetingTime != null){
            lbl = new Label(pMeetingTime.daysString(", "));       
            lbl.getStyleClass().add("label-detail-table-data");
            super.add(lbl, 7, pRowIndex);      

            lbl = new Label(pMeetingTime.startTime().toString() + " - " + pMeetingTime.endTime().toString());       
            lbl.getStyleClass().add("label-detail-table-data");
            super.add(lbl, 8, pRowIndex);                 
        }

        if(pInstructor != null){
            lbl = new Label(pInstructor.instructorName());       
            lbl.getStyleClass().add("label-detail-table-data");
            super.add(lbl, 9, pRowIndex);                    
        }

        lbl = new Label(pSection.session().sessionName());       
        lbl.getStyleClass().add("label-detail-table-data");
        super.add(lbl, 10, pRowIndex);

    }

    private void writeContinuedSectionRow(int pRowIndex, MeetingTime pMeetingTime, Instructor pInstructor) {
        Label lbl;
        if(pMeetingTime != null){
            lbl = new Label(pMeetingTime.daysString(", "));       
            lbl.getStyleClass().add("label-detail-table-data");
            super.add(lbl, 7, pRowIndex);      

            lbl = new Label(pMeetingTime.startTime().toString() + " - " + pMeetingTime.endTime().toString());       
            lbl.getStyleClass().add("label-detail-table-data");
            super.add(lbl, 8, pRowIndex);                 
        }

        if(pInstructor != null){
            lbl = new Label(pInstructor.instructorName());       
            lbl.getStyleClass().add("label-detail-table-data");
            super.add(lbl, 9, pRowIndex);                    
        }
    }
    private void addCoulmConstraints(){
        ColumnConstraints cc;

        cc = new ColumnConstraints();
        cc.setPercentWidth(SUBJ_AND_COURSE_PW);
        cc.setFillWidth(true);
        super.getColumnConstraints().add(cc);
        
        cc = new ColumnConstraints();
        cc.setPercentWidth(SEC_NUM_PW);
        cc.setFillWidth(true);
        super.getColumnConstraints().add(cc);
        
        cc = new ColumnConstraints();
        cc.setPercentWidth(CRN_PW);
        cc.setFillWidth(true);
        super.getColumnConstraints().add(cc);
      
        cc = new ColumnConstraints();
        cc.setPercentWidth(SEATS_PW);
        cc.setFillWidth(true);
        super.getColumnConstraints().add(cc);
        
        cc = new ColumnConstraints();
        cc.setPercentWidth(WAIT_PW);
        cc.setFillWidth(true);
        super.getColumnConstraints().add(cc);
    
        cc = new ColumnConstraints();
        cc.setPercentWidth(INSTRUCTIONAL_PW);
        cc.setFillWidth(true);
        super.getColumnConstraints().add(cc);     
        
        cc = new ColumnConstraints();
        cc.setPercentWidth(CAMPUS_PW);
        cc.setFillWidth(true);
        super.getColumnConstraints().add(cc);

        cc = new ColumnConstraints();
        cc.setPercentWidth(DAYS_PW);
        cc.setFillWidth(true);
        super.getColumnConstraints().add(cc);
        
        cc = new ColumnConstraints();
        cc.setPercentWidth(MEETING_TIMES_PW);
        cc.setFillWidth(true);
        super.getColumnConstraints().add(cc);
        
        cc = new ColumnConstraints();
        cc.setPercentWidth(INSTRUCOR_PW);
        cc.setFillWidth(true);
        super.getColumnConstraints().add(cc);
        
        cc = new ColumnConstraints();
        cc.setPercentWidth(SESS_NAME_PW);
        cc.setFillWidth(true);
        super.getColumnConstraints().add(cc);        
    }

}
