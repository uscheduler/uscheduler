/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uscheduler.ui.schedules;

import java.util.HashSet;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import uscheduler.internaldata.Schedules;
import uscheduler.internaldata.Schedules.Schedule;

/**
 * The AnchorPane used to house the "Total Hours", "Total Days", and "Term" of a Schedule. 
 * Additionally, this class contains the "remove" and "save/un-save" buttons and implements the associated functionality. 
 * @author Matt Bush
 */
public class ScheduleHeader extends AnchorPane{
    private Schedule cSchedule;
    private HashSet<ScheduleDeleteRequestListener> cDeleteRequestListeners = new HashSet<>();
    
    private final Text txtTotalHours = new Text();
    private final Text txtTotalDays = new Text();
    private final Text txtTerm = new Text();
    private final Button btnRemove = new  Button("Remove");
    private final Button btnSaveUnsave = new Button();

    public ScheduleHeader(Schedule pSchedule){
        if(pSchedule==null)
            throw new IllegalArgumentException("Null pSchedule argument.");

        cSchedule = pSchedule;      
        this.setTextValues();
        
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
                    System.out.println("Was saved.");
                    Schedules.unSave(cSchedule);
                    btnSaveUnsave.setText("Save");
                    
                } else{
                    System.out.println("Was NOT saved.");
                    Schedules.save(cSchedule);
                    btnSaveUnsave.setText("un-Save"); 
                    
                }
            }
        });
        
        //Create the Text that will act as labels to the TotalHours, TotalDays, and Term values
        Text txtTotalHoursLabel = new Text("Total Time:");
        txtTotalHoursLabel.setStyle("-fx-font-weight: bold");
        Text txtTotalDaysLabel = new Text("Total Days:");
        txtTotalDaysLabel.setStyle("-fx-font-weight: bold");
        Text txtTermLabel = new Text("Term:");
        txtTermLabel.setStyle("-fx-font-weight: bold");     
        
        //Create the HBox that will house the TotalHours, TotalDays, and Term "lables" and "values"
        HBox hbLeft = new HBox(10.0,txtTotalHoursLabel, txtTotalHours, txtTotalDaysLabel, txtTotalDays, txtTermLabel, txtTerm);
        //hbLeft.setPadding(new Insets(10.0, 0, 0, 0));
        
        //Create the HBox that will house the "Remove" and "Save/Un-Save" buttons
        HBox hbRight = new HBox(10.0, btnRemove, btnSaveUnsave);
        hbRight.setPadding(new Insets(5.0, 0, 5.0, 0));
        
        //Add 2 HBoxes to self and anchor "labels" and "values" to the left, and buttons to the right        
        super.getChildren().addAll(hbLeft, hbRight);
        AnchorPane.setLeftAnchor(hbLeft, 5.0);
        AnchorPane.setTopAnchor(hbLeft, 10.0);
        
        AnchorPane.setRightAnchor(hbRight, 5.0);

    }

    public void setSchedule(Schedule pSchedule){
        if(pSchedule==null)
            throw new IllegalArgumentException("Null pSchedule argument.");
        cSchedule = pSchedule;
        this.setTextValues();
    }
    private void setTextValues(){
        
        txtTotalDays.setText(Integer.toString((int)cSchedule.estDaysAtSchool()));
        txtTotalHours.setText(Integer.toString((int)cSchedule.estMinutesAtSchool()));
        txtTerm.setText(cSchedule.term().termName());
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
