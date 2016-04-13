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
 * The ScrollPane to house the view of Schedules. 
 * Each Schedule's view is provided by the ScheduleContainer class, thus this ScrollPane houses ScheduleContainer objects (in a single VBox).
 * This class implements ScheduleGeneratorObserver. 
 * Users of this class will add this class as an observer the ScheduleGenerator 
 * and this class will respond to the ScheduleGenerator.generate method by updating the view of schedules.
 * @author Matt Matt Bush
 */
public class SchedulesScrollPane extends ScrollPane implements ScheduleGeneratorObserver, ScheduleDeleteRequestListener{
    private final VBox cSchedulesVBox = new VBox();
    
    public SchedulesScrollPane(){
        
        /**
         * "If true and if the contained node [cSchedulesVBox] is a Resizable, 
         * then the node [cSchedulesVBox] will be kept resized to match the width of the ScrollPane's viewport. 
         * If the contained node [cSchedulesVBox] is not a Resizable, this value is ignored."
         */
        super.setFitToWidth(true);
        super.setFitToHeight(true);
        super.setContent(cSchedulesVBox);
        super.setVbarPolicy(ScrollBarPolicy.ALWAYS);
        
        //
        cSchedulesVBox.setSpacing(30.0);
        
        this.buildSchedules();
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
