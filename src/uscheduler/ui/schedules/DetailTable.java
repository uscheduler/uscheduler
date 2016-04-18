/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uscheduler.ui.schedules;

import javafx.beans.binding.Bindings;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import uscheduler.internaldata.Instructors.Instructor;
import uscheduler.internaldata.Schedules.Schedule;
import uscheduler.internaldata.Sections.Section;
import uscheduler.internaldata.Sections.Section.MeetingTime;
import uscheduler.ui.schedules.DetailTable.SectionRow;

//http://stackoverflow.com/questions/13381067/simplestringproperty-and-simpleintegerproperty-tableview-javafx

/**
 *
 * @author Matt Bush
 */
public class DetailTable extends TableView<SectionRow>{
    
    private Schedule cSchedule;
    private final ObservableList<SectionRow> cSectionRows = FXCollections.observableArrayList();

    
    public DetailTable(Schedule pSchedule){
        if(pSchedule==null)
            throw new IllegalArgumentException("Null pSchedule argument.");
        cSchedule = pSchedule;
        
        super.setEditable(false);

        TableColumn<SectionRow,String> cSessionNameCol = new TableColumn<>("Session");
        cSessionNameCol.setMinWidth(223);
        cSessionNameCol.setCellValueFactory(new PropertyValueFactory<SectionRow, String>("sessionName"));
        cSessionNameCol.setResizable(false);
        cSessionNameCol.setSortable(false);
          
        TableColumn<SectionRow,String> cSessionStartDateCol = new TableColumn<>("Start");
        cSessionStartDateCol.setMinWidth(80);
        cSessionStartDateCol.setCellValueFactory(new PropertyValueFactory<SectionRow, String>("sessionStartDate"));
        cSessionStartDateCol.setResizable(false);
        cSessionStartDateCol.setSortable(false);
        
        TableColumn<SectionRow,String> cSessionEndDateCol = new TableColumn<>("End");
        cSessionEndDateCol.setMinWidth(80);
        cSessionEndDateCol.setCellValueFactory(new PropertyValueFactory<SectionRow, String>("sessionEndDate"));
        cSessionEndDateCol.setResizable(false);
        cSessionEndDateCol.setSortable(false);
        
        TableColumn<SectionRow,String> cInstructionalMethodCol = new TableColumn<>("Method");
        cInstructionalMethodCol.setMinWidth(84);
        cInstructionalMethodCol.setCellValueFactory(new PropertyValueFactory<SectionRow, String>("instructionalMethod"));
        cInstructionalMethodCol.setResizable(false);
        cInstructionalMethodCol.setSortable(false);
     
        TableColumn<SectionRow,String> cCampusCol = new TableColumn<>("Campus");
        cCampusCol.setMinWidth(214);
        cCampusCol.setCellValueFactory(new PropertyValueFactory<SectionRow, String>("campusName"));
        cCampusCol.setResizable(false);
        cCampusCol.setSortable(false);        
 
        
        TableColumn<SectionRow,String> cSubjectCol = new TableColumn<>("Subj.");
        cSubjectCol.setPrefWidth(32);
        cSubjectCol.setCellValueFactory(new PropertyValueFactory<SectionRow, String>("subjectAbbr"));
        cSubjectCol.setResizable(false);
        cSubjectCol.setSortable(false);

     
        TableColumn<SectionRow,String> cCourseCol = new TableColumn<>("Course");
        cCourseCol.setMinWidth(46);
        cCourseCol.setCellValueFactory(new PropertyValueFactory<SectionRow, String>("courseNum"));
        cCourseCol.setResizable(false);
        cCourseCol.setSortable(false);
  
        TableColumn<SectionRow,String> cSectionCol = new TableColumn<>("Sec#");
        cSectionCol.setMinWidth(32);
        cSectionCol.setCellValueFactory(new PropertyValueFactory<SectionRow, String>("sectionNum"));
        cSectionCol.setResizable(false);
        cSectionCol.setSortable(false);
      
        TableColumn<SectionRow,String> cCrnCol = new TableColumn<>("CRN");
        cCrnCol.setMinWidth(56);
        cCrnCol.setCellValueFactory(new PropertyValueFactory<SectionRow, String>("crn"));
        cCrnCol.setResizable(false);
        cCrnCol.setSortable(false);
        
        TableColumn<SectionRow,String> cSeatsCol = new TableColumn<>("Seats");
        cSeatsCol.setMinWidth(34);
        cSeatsCol.setCellValueFactory(new PropertyValueFactory<SectionRow, String>("seats"));
        cSeatsCol.setResizable(false);
        cSeatsCol.setSortable(false);
        
        TableColumn<SectionRow,String> cWaitlistCol = new TableColumn<>("Waitlist");
        cWaitlistCol.setMinWidth(49);
        cWaitlistCol.setCellValueFactory(new PropertyValueFactory<SectionRow, String>("waitlist"));
        cWaitlistCol.setResizable(false);
        cWaitlistCol.setSortable(false);
        
        TableColumn<SectionRow,String> cStartTimeCol = new TableColumn<>("StartTime");
        cStartTimeCol.setMinWidth(62);
        cStartTimeCol.setCellValueFactory(new PropertyValueFactory<SectionRow, String>("startTime"));
        cStartTimeCol.setResizable(false);
        cStartTimeCol.setSortable(false);
 
        TableColumn<SectionRow,String> cEndTimeCol = new TableColumn<>("EndTime");
        cEndTimeCol.setMinWidth(62);
        cEndTimeCol.setCellValueFactory(new PropertyValueFactory<SectionRow, String>("endTime"));
        cEndTimeCol.setResizable(false);
        cEndTimeCol.setSortable(false);
        
        TableColumn<SectionRow,String> cDaysCol = new TableColumn<>("Days");
        cDaysCol.setMinWidth(216);
        cDaysCol.setCellValueFactory(new PropertyValueFactory<SectionRow, String>("days"));
        cDaysCol.setResizable(false);
        cDaysCol.setSortable(false);
       
        TableColumn<SectionRow,String> cInstructorsCol = new TableColumn<>("Instructors");  
        cInstructorsCol.setMinWidth(305);
        cInstructorsCol.setCellValueFactory(new PropertyValueFactory<SectionRow, String>("instructor"));
        cInstructorsCol.setResizable(false);
        cInstructorsCol.setSortable(false);
        
        populateSectionRows();
        super.setItems(cSectionRows);
        
        super.getColumns().addAll(cSessionNameCol, 
                                cSessionStartDateCol, 
                                cSessionEndDateCol, 
                                cInstructionalMethodCol,
                                cCampusCol,
                                cSubjectCol,
                                cCourseCol,
                                cSectionCol,
                                cCrnCol,
                                cSeatsCol,
                                cWaitlistCol,
                                cStartTimeCol,
                                cEndTimeCol,
                                cDaysCol,
                                cInstructorsCol);
        super.setFixedCellSize(25);
        super.prefHeightProperty().bind(Bindings.size(super.getItems()).multiply(super.getFixedCellSize()).add(40));
        super.minHeightProperty().bind(super.prefHeightProperty());
        super.maxHeightProperty().bind(super.prefHeightProperty());
        
        
    }
    public void setSchedule(Schedule pSchedule){
        if(pSchedule==null)
            throw new IllegalArgumentException("Null pSchedule argument.");
        cSchedule = pSchedule;
        populateSectionRows();
    }
    
    private void populateSectionRows(){
        
        cSectionRows.clear();
        
        for (Section sec : cSchedule.sections1()){
            MeetingTime[] meetingTimes = sec.meetings2();
            Instructor[] instructors = sec.instructors2();
            int max = Math.max(meetingTimes.length, instructors.length);
            cSectionRows.add(new SectionRow(sec, (meetingTimes.length > 0)?meetingTimes[0]:null, (instructors.length > 0)?instructors[0]:null));
            for(int i = 1; i < max; i++)
                cSectionRows.add(new SectionRow((meetingTimes.length > i)?meetingTimes[i]:null, (instructors.length > i)?instructors[i]:null));
        }
            
    }
    /**
     * This class is used to model a row in a DetailTable. 
     * In order to show the one to many relationship of a Section to MeetingTimes and a Section to Instructors in the DetailTable...
     * This class is immutable and does not need to provide "property" methods. 
     * Providing a property method would allow an observer to do something in response to a change in the property. 
     * However, this feature is not useful since this class is immutable.
     * When a DetailTable,s Schedule changes, it would probably be quicker to delete all the SectionRows and then create and add to new ones to 
     * its Observable list than the keep the old ones and just change the properties to reflect the new Sections.
     */
    public static class SectionRow {
        private final String sessionName;
        private final String sessionStartDate;
        private final String sessionEndDate;
        private final String instructionalMethod;
        private final String campusName;
        private final String subjectAbbr;
        private final String courseNum;
        private final String sectionNum;
        private final String crn;
        private final String seats;
        private final String waitlist;
        private final String startTime;
        private final String endTime;
        private final String days;
        private final String instructor;
        

        /**
         * Constructs a "main" SectionRow from a Section
         * @param pSection the Section from which this SectionRow will be modeled
         */
        private SectionRow(Section pSection, MeetingTime pMeetingTime, Instructor pInstructor) {
            sessionName = pSection.session().sessionName();
            sessionStartDate = pSection.session().startDate().toString();
            sessionEndDate = pSection.session().endDate().toString();
            instructionalMethod = pSection.instructionalMethod().toString();
            campusName = (pSection.campus()!=null) ? pSection.campus().campusName() : "";
            subjectAbbr = pSection.course().subject().subjectAbbr();
            courseNum = pSection.course().courseNum();
            sectionNum = pSection.sectionNumber();
            crn = Integer.toString(pSection.crn());
            seats = Integer.toString(pSection.seatsAvailable());
            waitlist = Integer.toString(pSection.waitlistAvailable());
            if(pMeetingTime != null){
                startTime = pMeetingTime.startTime().toString();
                endTime = pMeetingTime.endTime().toString();
                days =  pMeetingTime.daysString(", ");                
            } else {
                startTime = "";
                endTime = "";
                days =  "";                  
            }
            instructor = (pInstructor != null) ? pInstructor.instructorName() : "";
        }
         /**
         * Constructs a "continued" SectionRow from a Section and Instructor. 
         * A "continued" SectionRow is a row needed to show a MeetingTime or Instructor on a line after the Section for a Section with many  MeetingTimes or Instructors.
         */
        private SectionRow(MeetingTime pMeetingTime, Instructor pInstructor) {
            sessionName = "";
            sessionStartDate = "";
            sessionEndDate = "";
            instructionalMethod = "";
            campusName = "";
            subjectAbbr = "";
            courseNum = "";
            sectionNum = "";
            crn = "";
            seats = "";
            waitlist = "";
            if(pMeetingTime != null){
                startTime = pMeetingTime.startTime().toString();
                endTime = pMeetingTime.endTime().toString();
                days =  pMeetingTime.daysString(", ");                
            } else {
                startTime = "";
                endTime = "";
                days =  "";                  
            }
            instructor = (pInstructor != null) ? pInstructor.instructorName() : "";
        }
        
        public String getSessionName() {return sessionName;}
        public String getSessionStartDate() {return sessionStartDate;}
        public String getSessionEndDate() {return sessionEndDate;}
        public String getInstructionalMethod() {return instructionalMethod;}
        public String getCampusName() {return campusName;}
        public String getSubjectAbbr() {return subjectAbbr;}
        public String getCourseNum() {return courseNum;}
        public String getSectionNum() {return sectionNum;}
        public String getCrn() {return crn;}
        public String getSeats() {return seats;}
        public String getWaitlist() {return waitlist;}
        public String getStartTime() {return startTime;}
        public String getEndTime() {return endTime;}
        public String getDays() {return days;}
        public String getInstructor() {return instructor;}
    }
}
