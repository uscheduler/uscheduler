/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uscheduler.util;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import uscheduler.internaldata.Instructors.Instructor;
import uscheduler.internaldata.Schedules;
import uscheduler.internaldata.Schedules.Schedule;
import uscheduler.internaldata.Sections.MeetingTime;
import uscheduler.internaldata.Sections.Section;

/**
 * A singleton class consisting of a single static method that prints schedule to a file.
 * @author Matt Bush
 */
public final class SchedulePrinter {
    private static final int SESSION_LEN = 29;
    private static final int DATE_LEN = 12;
    private static final int IMETHOD_LEN = 11;
    private static final int SUBJECT_LEN = 4;
    private static final int COURSE_LEN = 5; 
    private static final int SECTION_NUM_LEN = 4;
    private static final int CRN_LEN = 5;
    private static final int SEATS_WAIT_LEN = 5;//Labels "Seats" and "Wait" longer than possible values
    private static final int CAMPUS_LEN = 22;
    private static final int TIME_LEN = 8;
    private static final int DAYS_LEN = 33;
    private static final int INSTRUCTORS_LEN = 40;
    
    
    private static final int ROW_LEN = 1 + SESSION_LEN + 1 + 2*DATE_LEN + 1 + IMETHOD_LEN + 1 + SUBJECT_LEN + 1 
                                         + COURSE_LEN + 1 + SECTION_NUM_LEN + 1 + CRN_LEN + 1 + 2*SEATS_WAIT_LEN + 1 + CAMPUS_LEN + 1
                                         + 2*TIME_LEN + 1 + DAYS_LEN + 1 + INSTRUCTORS_LEN + 1;
    
    private static final int SECTION_LEN = 1 + SESSION_LEN + 1 + 2*DATE_LEN + 1 + IMETHOD_LEN + 1 + SUBJECT_LEN + 1 
                                         + COURSE_LEN + 1 + SECTION_NUM_LEN + 1 + CRN_LEN + 1 + 2*SEATS_WAIT_LEN + 1 + CAMPUS_LEN;

    /**
     * Prints to a file, all Schedules s in the Schedules table such that s.isSaved() == true.
     * <br>
     * <b>!!!NOT YET IMPLEMENTED!!!</b>
     * <br>
     * @param pFile the file to print to. If no such file exists it will be created.
     * @param pAppend  if true, then bytes will be written to the end of the file rather than the beginning
     */
    public static void print(File pFile, boolean pAppend) throws IOException {
        try(PrintWriter schedulesOut = new PrintWriter(new BufferedWriter(new FileWriter(pFile, pAppend)))){
            char[] borders = new char[ROW_LEN]; 
            Arrays.fill(borders, '-');
                
            for (Schedule sch : Schedules.getAll()) {
                //Print top border of labels
                schedulesOut.println(borders);
                
                //Print labels
                schedulesOut.print('|');
                schedulesOut.format("%1$-" + SESSION_LEN + "s", "Session");
                schedulesOut.print('|');
                schedulesOut.format("%1$-" + DATE_LEN + "s", "StartDate");
                schedulesOut.print('|');
                schedulesOut.format("%1$-" + DATE_LEN + "s", "EndDate");
                schedulesOut.print('|');
                schedulesOut.format("%1$-" + IMETHOD_LEN + "s", "Method");
                schedulesOut.print('|');
                schedulesOut.format("%1$-" + SUBJECT_LEN + "s", "Subj");
                schedulesOut.print('|');
                schedulesOut.format("%1$-" + COURSE_LEN + "s", "Crs");
                schedulesOut.print('|');
                schedulesOut.format("%1$-" + SECTION_NUM_LEN + "s", "Sec");
                schedulesOut.print('|');
                schedulesOut.format("%1$-" + CRN_LEN + "s", "CRN");
                schedulesOut.print('|');
                schedulesOut.format("%1$-" + SEATS_WAIT_LEN + "s", "Seats");
                schedulesOut.print('|');
                schedulesOut.format("%1$-" + SEATS_WAIT_LEN + "s", "Wait");
                schedulesOut.print('|');
                schedulesOut.format("%1$-" + CAMPUS_LEN + "s", "Campus");                
                schedulesOut.print('|');
                schedulesOut.format("%1$-" + TIME_LEN + "s", "Start T");
                schedulesOut.print('|');
                schedulesOut.format("%1$-" + TIME_LEN + "s", "End T");
                schedulesOut.print('|');
                schedulesOut.format("%1$-" + DAYS_LEN + "s", "Days");
                schedulesOut.print('|');
                schedulesOut.format("%1$-" + INSTRUCTORS_LEN + "s", "Instructors");
                schedulesOut.print('|');
                
                //Print bottom border of labels
                schedulesOut.println();
                schedulesOut.println(borders);
                
                //Print Section Values
                for(Section sec: sch.sections()){

                    schedulesOut.print('|');
                    schedulesOut.format("%1$-" + SESSION_LEN + "s", sec.session().sessionName());
                    schedulesOut.print('|');
                    schedulesOut.format("%1$-" + DATE_LEN + "s", sec.session().startDate().toString());
                    schedulesOut.print('|');
                    schedulesOut.format("%1$-" + DATE_LEN + "s", sec.session().endDate().toString());
                    schedulesOut.print('|');
                    schedulesOut.format("%1$-" + IMETHOD_LEN + "s", sec.instructionalMethod().toString());
                    schedulesOut.print('|');
                    schedulesOut.format("%1$-" + SUBJECT_LEN + "s", sec.course().subject().subjectAbbr());
                    schedulesOut.print('|');
                    schedulesOut.format("%1$-" + COURSE_LEN + "s", sec.course().courseNum());
                    schedulesOut.print('|');
                    schedulesOut.format("%1$-" + SECTION_NUM_LEN + "s", sec.sectionNumber());
                    schedulesOut.print('|');
                    schedulesOut.format("%1$-" + CRN_LEN + "s", Integer.toString(sec.crn()));
                    schedulesOut.print('|');
                    schedulesOut.format("%1$-" + SEATS_WAIT_LEN + "s", sec.seatsAvailable());
                    schedulesOut.print('|');
                    schedulesOut.format("%1$-" + SEATS_WAIT_LEN + "s", sec.waitlistAvailable());
                    schedulesOut.print('|');
                    schedulesOut.format("%1$-" + CAMPUS_LEN + "s", (sec.campus()!= null) ? sec.campus().campusName(): "");
                    
                    //Print MeetingTime Values
                    ArrayList<MeetingTime> meetingTimes = sec.meetings();
                    ArrayList<Instructor> instructors = sec.instructors();
                    int maxSize = Math.max(meetingTimes.size(), instructors.size());

                    //print first MeetingTime and first Instructor on current / main line
                    schedulesOut.print('|');
                    schedulesOut.format("%1$-" + TIME_LEN + "s", (!meetingTimes.isEmpty()) ? meetingTimes.get(0).startTime() : "");
                    schedulesOut.print('|');
                    schedulesOut.format("%1$-" + TIME_LEN + "s",  (!meetingTimes.isEmpty()) ? meetingTimes.get(0).endTime(): "");
                    schedulesOut.print('|');
                    schedulesOut.format("%1$-" + DAYS_LEN + "s", (!meetingTimes.isEmpty()) ? meetingTimes.get(0).daysString(): "");                  
                    schedulesOut.print('|');
                    schedulesOut.format("%1$-" + INSTRUCTORS_LEN + "s", (!instructors.isEmpty()) ? instructors.get(0).instructorName(): "");
                    schedulesOut.print('|');
                        
                    //print each subsequent MeetingTime and Instructor on new line
                    schedulesOut.println();
                    for(int i = 1; i<maxSize;i++){
                        schedulesOut.format("%1$" + (SECTION_LEN +1) + "s", "|");
                        schedulesOut.format("%1$-" + TIME_LEN + "s", (i < meetingTimes.size()) ? meetingTimes.get(i).startTime() : "");
                        schedulesOut.print('|');
                        schedulesOut.format("%1$-" + TIME_LEN + "s",  (i < meetingTimes.size()) ? meetingTimes.get(i).endTime(): "");
                        schedulesOut.print('|');
                        schedulesOut.format("%1$-" + DAYS_LEN + "s", (i < meetingTimes.size()) ? meetingTimes.get(i).daysString(): "");                  
                        schedulesOut.print('|');
                        schedulesOut.format("%1$-" + INSTRUCTORS_LEN + "s", (i < instructors.size()) ? instructors.get(i).instructorName(): "");
                        schedulesOut.print('|');
                        schedulesOut.println();
                    }
                    
                }
                
                //Print bottom border of Schedule and then a new line
                schedulesOut.println(borders);
                schedulesOut.println();

            }
        }
    }    
    
}
