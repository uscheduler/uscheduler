/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uscheduler.ui.schedules;

import java.util.HashSet;
import javafx.scene.Node;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import uscheduler.internaldata.Schedules.Schedule;

/**
 * The VBox to house the view of a single Schedule. 
 * The view of a single schedules consists of: 
 * 1) a ScheduleHeader
 * 2) a ScheduleDetailTable
 * 3) a CalendarTabPane
 * @author Matt
 */
public class ScheduleContainer extends VBox implements ScheduleDeleteRequestListener{
    
    private Schedule cSchedule;
    private ScheduleHeader cScheduleHeader;
    private DetailTable cDetailTable;
    private CalendarTabPane pCalendarTabPane;
    
    private final HashSet<ScheduleDeleteRequestListener> cDeleteRequestListeners = new HashSet<>();
    
    public ScheduleContainer(Schedule pSchedule){
        if(pSchedule==null)
            throw new IllegalArgumentException("Null pSchedule argument.");
        cSchedule = pSchedule;
        cScheduleHeader = new ScheduleHeader(cSchedule);
        cDetailTable = new DetailTable(cSchedule);
        pCalendarTabPane = new CalendarTabPane(cSchedule);
        
        super.setStyle("-fx-border-color: black; -fx-border-width: 2;");
        super.getChildren().addAll(cScheduleHeader, cDetailTable);
        //super.getChildren().addAll( pCalendarTabPane);
        /**
         * Sets the vertical grow priority for the child [pCalendarTabPane] when contained by an vbox. 
         * If set, the vbox [this class] will use the priority to allocate additional space if the vbox is resized larger than it's preferred height. 
         * If multiple vbox children have the same vertical grow priority, then the extra space will be split evenly between them. 
         * If no vertical grow priority is set on a child, the vbox will never allocate it additional vertical space if available. 
         * Setting the value to null will remove the constraint.
         */
        //VBox.setVgrow(pCalendarTabPane, Priority.ALWAYS);
        
        cScheduleHeader.addScheduleDeleteRequestListener(this);
    }
    public void addScheduleDeleteRequestListener(ScheduleDeleteRequestListener pListener){
        if(pListener==null)
            throw new IllegalArgumentException("Null pListener argument.");    
        cDeleteRequestListeners.add(pListener);
    }
    public void removeScheduleDeleteRequestListener(ScheduleDeleteRequestListener pListener){   
        cDeleteRequestListeners.remove(pListener);
    }
    @Override
    public void deleteRequested(Node pNode, Schedule pSchedule) {
        for(ScheduleDeleteRequestListener listener : cDeleteRequestListeners){
            listener.deleteRequested(this, cSchedule);
        }
            
    }
    
}
