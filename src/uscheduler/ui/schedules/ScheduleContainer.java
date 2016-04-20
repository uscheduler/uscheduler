/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uscheduler.ui.schedules;

import java.util.HashSet;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import uscheduler.internaldata.Schedules;
import uscheduler.internaldata.Schedules.Schedule;

/**
 * The VBox to house the view of a single Schedule. 
 * The view of a single schedules consists of: 
 * 1) a ScheduleHeader
 * 2) a ScheduleDetailTable
 * 3) a CalendarTabPane
 * @author Matt
 */

/**
 * Tab (SchedulesTab class)
 *      ScrollPane (SchedulesScrollPane class)
 *          VBox (cSchedulesVBox)
 *              VBox (ScheduleContainer class)
 *                  AnchorPane (ScheduleHeader class)
 *                  GridPane (DetailTable2 class)
 *                  TabPane (CalendarTabPane class)
 *              
 * 
 */ 

public class ScheduleContainer extends VBox{
    //Data
    private Schedule cSchedule;
    private SchedulesScrollPane cParentScrollPane;
    
    //Controls
    private CheckBox chkSaved = new CheckBox();
    private Button btnRemove = new Button();
    
    //Tooltips
    private static final Tooltip TT_COMMUTES = new Tooltip("The number of times you would have to commute from one campus to another in the same day.");
    private static final Tooltip TT_TOTAL_HOURS = new Tooltip("The estimated total number of hours you would be at school with this schedule.");
    private static final Tooltip TT_TOTAL_DAYS = new Tooltip("The estimated total number of days you would be at school with this schedule.");
    private static final Tooltip TT_SAVE = new Tooltip("A saved schedule will remain after performing another schedule generation, while un-saved schedules will be removed. ALso, saved schedules can be printed.");
    private static final Tooltip TT_REMOVE = new Tooltip("Removes this schedule.");
    
    //Saved checkbox Change Listener
    private final ChangeListener<Boolean> CHECK_CHANGE_LISTENER = new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> ov,Boolean old_val, Boolean new_val) {
                if (new_val){
                    Schedules.save(cSchedule);
                } else{
                    Schedules.unSave(cSchedule);
                }
            }
    };   
    // Remove button Change Listener
    private final EventHandler<ActionEvent> REMOVE_BUTTON_HANDLER = new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {    
                cParentScrollPane.deleteRequested(ScheduleContainer.this, cSchedule);
            }
    };    
    

    public ScheduleContainer(Schedule pSchedule, SchedulesScrollPane pParentScrollPane){
        cSchedule = pSchedule;
        cParentScrollPane = pParentScrollPane;
        //cParentScrollPane = null;
        /**
         * Build Header
         */
        AnchorPane scheduleHeader  = getHeaderAnchorPane();
        if (cSchedule.sessionPartitions1().isEmpty()){
            super.getChildren().addAll(scheduleHeader, new DetailTable2(cSchedule));
        } else {
            super.getChildren().addAll(scheduleHeader, new DetailTable2(cSchedule), new CalendarTabPane(cSchedule));
        }
        super.getStyleClass().add("vbox-schedule-container");
    }
    private AnchorPane getHeaderAnchorPane(){

        AnchorPane headerAnchorPane = new AnchorPane();

        //Build Saved Check Box
        chkSaved.setText("Saved");
        Tooltip.install(chkSaved, ScheduleContainer.TT_SAVE);
        chkSaved.setSelected(cSchedule.isSaved());
        chkSaved.selectedProperty().addListener(CHECK_CHANGE_LISTENER);//*****************Must be removed to prevent mem leak**********************
        
        //Build Remove Button
        btnRemove.setText("x");
        Tooltip.install(btnRemove, ScheduleContainer.TT_REMOVE);
        btnRemove.getStyleClass().add("button-remove");
        btnRemove.setOnAction(REMOVE_BUTTON_HANDLER); 
        btnRemove.disableProperty().bind(chkSaved.selectedProperty());//*****************Must be removed to prevent mem leak**********************
        

        //Build Right HBox to contain Saved and Remove
        HBox hbRight = new HBox(15, chkSaved, btnRemove);
        
        //Reusable declarations
        Text txtLabel;
        Text txtValue;

        //Campuse Switches 2 Label, Text, and HBox
        txtLabel = new Text("Commutes: ");
        Tooltip.install(txtLabel, ScheduleContainer.TT_COMMUTES);
        txtLabel.getStyleClass().add("text-labels");
        txtValue = new Text(Integer.toString((int)cSchedule.estNumCampusSwitches()));
        HBox hbxCampusSwitches2 = new HBox(txtLabel, txtValue);
        
        //Days Label, Text, and HBox
        txtLabel = new Text("Days: ");
        Tooltip.install(txtLabel, ScheduleContainer.TT_TOTAL_DAYS);
        txtLabel.getStyleClass().add("text-labels");
        txtValue = new Text(Integer.toString((int)cSchedule.estDaysAtSchool()));
        HBox hbxDays = new HBox(txtLabel, txtValue);
        
        //Hours Label, Text, and HBox
        txtLabel = new Text("Hours: ");
        Tooltip.install(txtLabel, ScheduleContainer.TT_TOTAL_HOURS);
        txtLabel.getStyleClass().add("text-labels");
        txtValue = new Text(String.format("%.1f", cSchedule.estMinutesAtSchool()/60.0));
        HBox hbxHours = new HBox(txtLabel, txtValue);
        
        //Term Label, Text, and HBox
        txtLabel = new Text("Term: ");
        txtLabel.getStyleClass().add("text-labels");
        txtValue = new Text(cSchedule.term().termName());
        HBox hbxTerm = new HBox(txtLabel, txtValue);
        
        //Left HBox to contain all Labels and Text Fields
        HBox hbxLeft = new HBox(15, hbxCampusSwitches2, hbxDays, hbxHours, hbxTerm);
        
        AnchorPane.setLeftAnchor(hbxLeft, 5.0);
        AnchorPane.setRightAnchor(hbRight, 5.0);

        headerAnchorPane.getChildren().addAll(hbxLeft, hbRight);
        headerAnchorPane.getStyleClass().add("vbox-schedule-header");

        return headerAnchorPane;
    }    
    
    
    public void kill(){
        cSchedule=null;
        chkSaved.selectedProperty().removeListener(CHECK_CHANGE_LISTENER);//This one is no doubt needed
        btnRemove.disableProperty().unbind(); //This one is no doubt needed
        cParentScrollPane = null;
        chkSaved = null;
        btnRemove = null;
        super.getChildren().clear();
    }
    
}
