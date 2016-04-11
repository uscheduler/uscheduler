/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uscheduler.ui.schedules;

import javafx.scene.Node;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.VBox;
import uscheduler.internaldata.Schedules;
import uscheduler.util.ScheduleGeneratorObserver;

/**
 *
 * @author Matt
 */
public class SchedulesScrollPane extends ScrollPane implements ScheduleGeneratorObserver, ScheduleDeleteRequestListener{
    private final VBox cSchedulesVBox = new VBox();
    
    public SchedulesScrollPane(){
        
        this.buildSchedules();
        
        super.setFitToWidth(true);
        super.setFitToHeight(true);
        super.setContent(cSchedulesVBox);
        //sp.setHbarPolicy(ScrollBarPolicy.NEVER);
        super.setVbarPolicy(ScrollBarPolicy.ALWAYS);
        
        //ScheduleGenerator.addObserver(this);
    }
    private void buildSchedules(){
        for (Schedules.Schedule sch : Schedules.getAll1()){
            ScheduleContainer schContainer = new ScheduleContainer(sch);
            schContainer.addScheduleDeleteRequestListener(this);
            cSchedulesVBox.getChildren().add(schContainer);
        }      
    }
    public void rebuildSchedules() {
        cSchedulesVBox.getChildren().clear();
        buildSchedules();
    }
    @Override
    public void schedulesGenerated() {
        rebuildSchedules();
    }

    @Override
    public void deleteRequested(Node pNode, Schedules.Schedule pSchedule) {
        if(!pSchedule.isDeleted()){
            Schedules.delete(pSchedule);
            cSchedulesVBox.getChildren().remove(pNode);
        }
    }
    
}
