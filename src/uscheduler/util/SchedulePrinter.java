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
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import uscheduler.internaldata.Instructors.Instructor;
import uscheduler.internaldata.Schedules;
import uscheduler.internaldata.Schedules.Schedule;
import uscheduler.internaldata.Sections.Section;
import uscheduler.internaldata.Sections.Section.MeetingTime;

/**
 * A singleton class consisting of static methods that print schedules to a file.
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
    
    private static final int ROW_LEN = 16 + SESSION_LEN + 2*DATE_LEN + IMETHOD_LEN + SUBJECT_LEN +
                                         + COURSE_LEN + SECTION_NUM_LEN + CRN_LEN  + 2*SEATS_WAIT_LEN + CAMPUS_LEN +
                                         + 2*TIME_LEN  + DAYS_LEN + INSTRUCTORS_LEN;
    
    private static final int SECTION_LEN = 11 + SESSION_LEN + 2*DATE_LEN + IMETHOD_LEN + SUBJECT_LEN +
                                         + COURSE_LEN + SECTION_NUM_LEN + CRN_LEN  + 2*SEATS_WAIT_LEN + CAMPUS_LEN;


    /**
     * Prints to a file, all {@link uscheduler.internaldata.Schedules.Schedule Schedules s} in the Schedules table such that {@link uscheduler.internaldata.Schedules.Schedule#isSaved()  isSaved()}<code>==true</code>.
     *
     * @param pFile  a File object to write to
     * @param pAppend    if <code>true</code>, then bytes will be written to the end of the file rather than the beginning
     * @throws IOException  if the file exists but is a directory rather than a regular file, does not exist but cannot be created, or cannot be opened for any other reason
     */  
    public static void printSaved(File pFile, boolean pAppend) throws IOException {
        print(Schedules.getAllSaved(), pFile, pAppend);
    }
    /**
     * Prints to a file, all {@link uscheduler.internaldata.Schedules.Schedule Schedules s} in the Schedules table.
     *
     * @param pFile  a File object to write to
     * @param pAppend  if <code>true</code>, then bytes will be written to the end of the file rather than the beginning
     * @throws IOException  if the file exists but is a directory rather than a regular file, does not exist but cannot be created, or cannot be opened for any other reason
     */  
    public static void printAll(File pFile, boolean pAppend) throws IOException {
        print(Schedules.getAll1(), pFile, pAppend);
    }
    private static void print(Collection<Schedule> pSchedules, File pFile, boolean pAppend) throws IOException {
        
        try(PrintWriter schedulesOut = new PrintWriter(new BufferedWriter(new FileWriter(pFile, pAppend)))){
            
            //an array of '-' characters or length ROW_LEN used to print horizontal dividing lines
            char[] dividingLineChars = new char[ROW_LEN]; 
            Arrays.fill(dividingLineChars, '-');
            
            //print report/print job header
            SimpleDateFormat sdfDate = new SimpleDateFormat("MMM d, y (h:mm a)");
            Date now = new Date();
            String strDate = sdfDate.format(now);
            
            schedulesOut.println(dividingLineChars);
            schedulesOut.println("***UScheduler Generated Schedules***");
            schedulesOut.println("Number of schedules printed: " + pSchedules.size());
            schedulesOut.println("Date printed: " + strDate);
            schedulesOut.println(dividingLineChars);
            schedulesOut.println();
            
            for (Schedule sch : pSchedules) {
                //Print top border of labels
                schedulesOut.println(dividingLineChars);
                
                //Print labels
                schedulesOut.print('|'); schedulesOut.format("%1$-" + SESSION_LEN + "s", "Session");
                schedulesOut.print('|'); schedulesOut.format("%1$-" + DATE_LEN + "s", "StartDate");
                schedulesOut.print('|'); schedulesOut.format("%1$-" + DATE_LEN + "s", "EndDate");
                schedulesOut.print('|'); schedulesOut.format("%1$-" + IMETHOD_LEN + "s", "Method");
                schedulesOut.print('|'); schedulesOut.format("%1$-" + SUBJECT_LEN + "s", "Subj");
                schedulesOut.print('|'); schedulesOut.format("%1$-" + COURSE_LEN + "s", "Crs");
                schedulesOut.print('|'); schedulesOut.format("%1$-" + SECTION_NUM_LEN + "s", "Sec");
                schedulesOut.print('|'); schedulesOut.format("%1$-" + CRN_LEN + "s", "CRN");
                schedulesOut.print('|'); schedulesOut.format("%1$-" + SEATS_WAIT_LEN + "s", "Seats");
                schedulesOut.print('|'); schedulesOut.format("%1$-" + SEATS_WAIT_LEN + "s", "Wait");
                schedulesOut.print('|'); schedulesOut.format("%1$-" + CAMPUS_LEN + "s", "Campus");                
                schedulesOut.print('|'); schedulesOut.format("%1$-" + TIME_LEN + "s", "Start T");
                schedulesOut.print('|'); schedulesOut.format("%1$-" + TIME_LEN + "s", "End T");
                schedulesOut.print('|'); schedulesOut.format("%1$-" + DAYS_LEN + "s", "Days");
                schedulesOut.print('|'); schedulesOut.format("%1$-" + INSTRUCTORS_LEN + "s", "Instructors");
                schedulesOut.print('|');
                
                //Print bottom border of labels
                schedulesOut.println();
                schedulesOut.println(dividingLineChars);
                
                //Print Section Values
                for(Section sec: sch.sections1()){

                    schedulesOut.print('|'); schedulesOut.format("%1$-" + SESSION_LEN + "s", sec.session().sessionName());
                    schedulesOut.print('|'); schedulesOut.format("%1$-" + DATE_LEN + "s", sec.session().startDate().toString());
                    schedulesOut.print('|'); schedulesOut.format("%1$-" + DATE_LEN + "s", sec.session().endDate().toString());
                    schedulesOut.print('|'); schedulesOut.format("%1$-" + IMETHOD_LEN + "s", sec.instructionalMethod().toString());
                    schedulesOut.print('|'); schedulesOut.format("%1$-" + SUBJECT_LEN + "s", sec.course().subject().subjectAbbr());
                    schedulesOut.print('|'); schedulesOut.format("%1$-" + COURSE_LEN + "s", sec.course().courseNum());
                    schedulesOut.print('|'); schedulesOut.format("%1$-" + SECTION_NUM_LEN + "s", sec.sectionNumber());
                    schedulesOut.print('|'); schedulesOut.format("%1$-" + CRN_LEN + "s", Integer.toString(sec.crn()));
                    schedulesOut.print('|'); schedulesOut.format("%1$-" + SEATS_WAIT_LEN + "s", sec.seatsAvailable());
                    schedulesOut.print('|'); schedulesOut.format("%1$-" + SEATS_WAIT_LEN + "s", sec.waitlistAvailable());
                    schedulesOut.print('|'); schedulesOut.format("%1$-" + CAMPUS_LEN + "s", (sec.campus()!= null) ? sec.campus().campusName(): "");
                    
                    //Print MeetingTime Values
                    MeetingTime[] meetingTimes = sec.meetings2();
                    Instructor[] instructors = sec.instructors2();
                    int maxSize = Math.max(meetingTimes.length, instructors.length);

                    //print first MeetingTime and first Instructor on current / main line
                    schedulesOut.print('|'); schedulesOut.format("%1$-" + TIME_LEN + "s", (meetingTimes.length > 0) ? meetingTimes[0].startTime() : "");
                    schedulesOut.print('|'); schedulesOut.format("%1$-" + TIME_LEN + "s",  (meetingTimes.length > 0) ? meetingTimes[0].endTime(): "");
                    schedulesOut.print('|'); schedulesOut.format("%1$-" + DAYS_LEN + "s", (meetingTimes.length > 0) ? meetingTimes[0].daysString(", "): "");                  
                    schedulesOut.print('|'); schedulesOut.format("%1$-" + INSTRUCTORS_LEN + "s", (instructors.length > 0) ? instructors[0].instructorName(): "");
                    schedulesOut.print('|');
                        
                    //print each subsequent MeetingTime and Instructor on new line
                    schedulesOut.println();
                    for(int i = 1; i<maxSize;i++){
                        schedulesOut.format("%1$" + (SECTION_LEN +1) + "s", "|");
                        schedulesOut.format("%1$-" + TIME_LEN + "s", (i < meetingTimes.length) ? meetingTimes[i].startTime() : "");
                        schedulesOut.print('|'); schedulesOut.format("%1$-" + TIME_LEN + "s",  (i < meetingTimes.length) ? meetingTimes[i].endTime(): "");
                        schedulesOut.print('|'); schedulesOut.format("%1$-" + DAYS_LEN + "s", (i < meetingTimes.length) ? meetingTimes[i].daysString(", "): "");                  
                        schedulesOut.print('|'); schedulesOut.format("%1$-" + INSTRUCTORS_LEN + "s", (i < instructors.length) ? instructors[i].instructorName(): "");
                        schedulesOut.print('|');
                        schedulesOut.println();
                    }
                    
                }
                //Print a new line, then est # days and est # minutes, each on a new line
                schedulesOut.println();
                schedulesOut.println("Est. # days at school: " + (int)sch.estDaysAtSchool());
                schedulesOut.println("Est. # min. at school: " + (int)sch.estMinutesAtSchool());
                
                //Print bottom border of Schedule and then a new line
                schedulesOut.println(dividingLineChars);
                schedulesOut.println();

            }
        }
    }    
    
}
