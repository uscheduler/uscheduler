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
 *
 * @author Matt
 */
public class ScheduleContainer extends VBox implements ScheduleDeleteRequestListener{
    
    private Schedule cSchedule;
    private ScheduleHeader cScheduleHeader;
    private DetailTable cDetailTable;
    private HashSet<ScheduleDeleteRequestListener> cDeleteRequestListeners = new HashSet<>();
    
    public ScheduleContainer(Schedule pSchedule){
        if(pSchedule==null)
            throw new IllegalArgumentException("Null pSchedule argument.");
        cSchedule = pSchedule;
        cScheduleHeader = new ScheduleHeader(cSchedule);
        cScheduleHeader.addScheduleDeleteRequestListener(this);
        cDetailTable = new DetailTable(cSchedule);
        super.getChildren().addAll(cScheduleHeader, cDetailTable);
        VBox.setVgrow(cDetailTable, Priority.ALWAYS);
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
