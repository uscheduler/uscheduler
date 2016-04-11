/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uscheduler.ui.schedules;

import java.util.HashSet;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import uscheduler.internaldata.Schedules;
import uscheduler.internaldata.Schedules.Schedule;

/**
 *
 * @author Matt Bush
 */
public class ScheduleHeader extends AnchorPane{
    private Schedule cSchedule;
    private HashSet<ScheduleDeleteRequestListener> cDeleteRequestListeners = new HashSet<>();
    private final Label lblTotalTime = new Label();
    private final Label lblTotalDays = new Label();
    private final Label lblTerm = new Label();
    private final Button btnRemove = new  Button("Remove");
    private final Button btnSaveUnsave = new Button();

    public ScheduleHeader(Schedule pSchedule){
        if(pSchedule==null)
            throw new IllegalArgumentException("Null pSchedule argument.");

        cSchedule = pSchedule;      
        
        btnRemove.setOnAction(new EventHandler<ActionEvent>() {
            
            @Override
            public void handle(ActionEvent event) {    
                for(ScheduleDeleteRequestListener listener : cDeleteRequestListeners)
                    listener.deleteRequested(ScheduleHeader.this, cSchedule);
            }
        });
        btnSaveUnsave.setOnAction(new EventHandler<ActionEvent>() {
            
            @Override
            public void handle(ActionEvent event) { 
                if (cSchedule.isSaved()){
                    //Schedules.unSave(cSchedule);
                    btnSaveUnsave.setText("Save");
                } else{
                    Schedules.save(cSchedule);
                    btnSaveUnsave.setText("un-Save");                    
                }
            }
        });
        
        
        HBox hbLeft = new HBox(20.0, lblTotalTime, lblTotalDays, lblTerm);
        HBox hbRight = new HBox(20.0, btnRemove, btnSaveUnsave);
        
        super.getChildren().addAll(hbLeft, hbRight);
        
        AnchorPane.setLeftAnchor(hbLeft, 5.0);
        AnchorPane.setRightAnchor(hbRight, 5.0);
        
        this.fillHeader();
        
    }

    public void setSchedule(Schedule pSchedule){
        if(pSchedule==null)
            throw new IllegalArgumentException("Null pSchedule argument.");
        cSchedule = pSchedule;
        this.fillHeader();
    }
    private void fillHeader(){
        
        lblTotalDays.setText("Total Days: " + Integer.toString((int)cSchedule.estDaysAtSchool()));
        lblTotalTime.setText("Total Minutes: " + Integer.toString((int)cSchedule.estMinutesAtSchool()));
        lblTerm.setText("Term: " + cSchedule.term().termName());
        btnSaveUnsave.setText((cSchedule.isSaved())? "Un-Save": "Save");
        
    }
    public void addScheduleDeleteRequestListener(ScheduleDeleteRequestListener pListener){
        if(pListener==null)
            throw new IllegalArgumentException("Null pListener argument.");    
        cDeleteRequestListeners.add(pListener);
    }
    public void removeScheduleDeleteRequestListener(ScheduleDeleteRequestListener pListener){   
        cDeleteRequestListeners.remove(pListener);
    }
}
