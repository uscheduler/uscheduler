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
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Tab;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import uscheduler.internaldata.Schedules.Schedule.SessionPartition;
import uscheduler.internaldata.Schedules.Schedule.SessionPartition.MeetingDayPartition;
import uscheduler.internaldata.Sections;

/**
 * The Tab used to display a SessionPartition and the corresponding MeetingDayPartitions in a calendar format.
 * @author Matt Bush
 */
public class CalendarTab extends Tab{
    private final SessionPartition cSessionPartition;
    
    private static final Insets ROW_1_MARGIN_INSETS = new Insets(5, 0, 5, 0);
    private static final Insets ROW_2_MARGIN_INSETS = new Insets(2, 2, 2, 2);
    private static final Insets ROW_3_MARGIN_INSETS = new Insets(2, 2, 2, 2);
    private static final ColumnConstraints COLUMN_CONSTRAINTS = new ColumnConstraints();
    
    public CalendarTab(SessionPartition pSessionPartition){
        if(pSessionPartition==null)
            throw new IllegalArgumentException("Null pSchedule argument.");
        cSessionPartition = pSessionPartition;
    
        super.setClosable(false);
        super.setText(cSessionPartition.startDate() + " - " + cSessionPartition.endDate());
        super.setContent(getGrid());
    }
    /**
     * This method builds the GridPane that will be used to display the 7 days of week and the corresponding meeting times associated with the each day of week.
     * There will be 7 columns in the grid corresponding to the 7 days of week.
     * There will be 3 rows in the grid: 
     * The first row displays the name of the DayOfWeek 
     * The second row displays each MeetingTime start time and end time, for each MeetingTime in the cSessionPartition for the given DayOfWeek
     * The third row displays the summary total time at school for the given day of week.
     * Each column in the grid will have the same width. 
     * This is accomplished by adding 7 ColumnConstrains object to the grid and setting its .percentWidth value to (int)(100/7)
     * 
     * @return 
     */
    private GridPane getGrid(){

        GridPane grid = new GridPane();
        grid.setGridLinesVisible(true);
        COLUMN_CONSTRAINTS.setPercentWidth(25);
        int columnIndex = 0;
        /**
         * Iterate through each DayOfWeek and populating the corresponding grid's column with the 1st, 2nd, and 3rd row values.
         * Also, for each iteration, add the ColumnConstraints to the grid.
         */
        /**
         * GridPane (below represents a single column of 7 equal columns)
         *      StackPane sp11 (GridPane.setFillHeight(sp11, true); sp11.setStyle("-fx-background-color: #E3E5EE;");) 
         *          Text txt11 (StackPane.setAlignment(txt11, Pos.CENTER); StackPane.setMargin(txt11, ROW_1_MARGIN_INSETS); )
         *      Text txt12 (GridPane.setMargin(txt12, ROW_2_MARGIN_INSETS);)
         *      Text txt13 (GridPane.setHalignment(txt13, HPos.CENTER); GridPane.setMargin(txt13, ROW_3_MARGIN_INSETS);)
         *      ...
         *      StackPane sp71
         *          Text txt71 
         *      Text txt72 
         *      Text txt73 
         */
        for (DayOfWeek dow : DayOfWeek.values()) {
            
            /**
             * Build first row (The day of week) for the current column 
             * Create a stack pane and put text on top of it
             * Color stack pane grey
             * make sure stack pane expands to fill entire cell
             */
            Text txt = new Text(getRow1Text(dow));
            txt.getStyleClass().add("text-calendar-header");

            StackPane sp = new StackPane(txt);
            StackPane.setAlignment(txt, Pos.CENTER);
            StackPane.setMargin(txt, ROW_1_MARGIN_INSETS);
            sp.setStyle("-fx-background-color: #E3E5EE;");
            
            GridPane.setFillHeight(sp, true);
            GridPane.setFillWidth(sp, true);

            grid.add(sp, columnIndex, 0); 
            
            //Get the corresponding MeetingDayPartition for the current DayOfWeek, if one exists (may be null.
            MeetingDayPartition mdp =cSessionPartition.getMeetingDayPartition(dow);
            
            //Build 2nd row (The times) for the current column 
            txt = new Text(getRow2Text(mdp));
            GridPane.setMargin(txt, ROW_2_MARGIN_INSETS);
            grid.add(txt, columnIndex, 1); 
            
            //Build third row (The total time summary) for the current column 
            txt = new Text(getRow3Text(mdp));
            GridPane.setHalignment(txt, HPos.CENTER);
            GridPane.setMargin(txt, ROW_3_MARGIN_INSETS);
            grid.add(txt, columnIndex, 2);    
            
            //Add the ColumnConstraints to the grid
            grid.getColumnConstraints().add(COLUMN_CONSTRAINTS);
            
            //increment column counter
            columnIndex++;
        }   
        
        return grid;
    }
    
    private String getRow1Text(DayOfWeek pDayOfWeek){
        return pDayOfWeek.getDisplayName(TextStyle.FULL, Locale.getDefault());
    }
    
    private String getRow2Text(MeetingDayPartition pMeetingDayPartition){
        
        if(pMeetingDayPartition == null)
            return null;

        Set<Sections.Section.MeetingTime> meetingTimes = pMeetingDayPartition.meetingTimes1();
        //if(meetingTimes.isEmpty()) //This should never happen!
        //    return "";

        StringBuilder sb = new StringBuilder();
        Iterator<Sections.Section.MeetingTime> meetingTimesIT = meetingTimes.iterator();
        Sections.Section.MeetingTime mt = meetingTimesIT.next();
       
        sb.append(String.format("%8s", mt.startTime()))
        .append(" - ")
        .append(String.format("%8s", mt.endTime()))
        .append(" ")
        .append(mt.section().course().subject().subjectAbbr())
        .append(" ")
        .append(mt.section().course().courseNum())
        .append(" ")
        .append((mt.section().campus() != null) ?  "(" + mt.section().campus().campusName().substring(0,1) + ")" : ""); 

        while(meetingTimesIT.hasNext()){
            mt = meetingTimesIT.next();
            sb.append("\n")
            .append(String.format("%8s", mt.startTime()))
            .append(" - ")
            .append(String.format("%8s", mt.endTime()))
            .append(" ")
            .append(mt.section().course().subject().subjectAbbr())
            .append(" ")
            .append(mt.section().course().courseNum())
            .append(" ")
            .append((mt.section().campus() != null) ?  "(" + mt.section().campus().campusName().substring(0,1) + ")" : "");          
        }
        return sb.toString();                          
    }
    private String getRow3Text(MeetingDayPartition pMeetingDayPartition){
        if(pMeetingDayPartition == null)
            return null;
        return String.format("%.1f hrs", pMeetingDayPartition.minutesAtSchool()/60.0);
    }
}
