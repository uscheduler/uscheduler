/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uscheduler.ui.schedules;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Tab;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import uscheduler.internaldata.Schedules;
import uscheduler.util.ScheduleGenerator;
import uscheduler.util.SchedulePrinter;

/**
 * The Tab to house the entire view of Schedules. 
 * This Tab is the top level container for the view of schedules.
 * This Tab houses two buttons to print saved schedules and delete unsaved schedules.
 * Additionally, this Tab houses the SchedulesScrollPane, which is responsible for providing a view of all schedules.
 * 
 * @author Matt Bush
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
public class SchedulesTab extends Tab{
    
    //****************Controls
    private final Button btnDeleteUnsaved;
    private final Button btnPrintSaved;
    
    private final SchedulesScrollPane spSchedulesScrollPane;
    
    //****************Misc
    private final FileChooser fileChooser = new FileChooser();
    private Desktop desktop = Desktop.getDesktop();
    
    /********************************************************
     * ****************Action Event Handlers
     * ******************************************************
     */
    private final EventHandler<ActionEvent> HELP_ACTION = new EventHandler<ActionEvent>() {
        @Override
        public void handle(ActionEvent event) {    
            ///
        }
    };
    private final EventHandler<ActionEvent> REMOVE_UNSAVED_ACTION = new EventHandler<ActionEvent>() {
        @Override
        public void handle(ActionEvent event) {    
            if(Schedules.deleteUnsaved()>0){
                spSchedulesScrollPane.rebuildSchedules();
            }
        }
    };
    private final EventHandler<ActionEvent> PRINT_SAVED_ACTION = new EventHandler<ActionEvent>() {
        @Override
        public void handle(ActionEvent event) { 
            Stage stage = (Stage)SchedulesTab.this.btnPrintSaved.getScene().getWindow();
            final DirectoryChooser directoryChooser = new DirectoryChooser();
            final File selectedDirectory = directoryChooser.showDialog(stage);

            if (selectedDirectory != null) {
                File outputFile = new File(selectedDirectory,"UScheduler.txt");
                try {
                    SchedulePrinter.printSaved(outputFile, true);
                    desktop.open(outputFile);
                } catch (IOException ex) {
                    Logger.getLogger(SchedulesTab.class.getName()).log(Level.SEVERE, null, ex);
                }

            }               
        }
    };
    public SchedulesTab(){
        //Remove Unsaved Button
        btnDeleteUnsaved = new Button("X all");
        btnDeleteUnsaved.getStyleClass().add("button-remove");
        btnDeleteUnsaved.setOnAction(REMOVE_UNSAVED_ACTION); 
        
        //Print Saved Button
        btnPrintSaved = new Button("Print Saved");
        btnPrintSaved.getStyleClass().add("button-action");
        btnPrintSaved.setOnAction(PRINT_SAVED_ACTION); 
        
        

        //Schedules Scroll Pane
        spSchedulesScrollPane = new SchedulesScrollPane();
        ScheduleGenerator.addObserver(spSchedulesScrollPane);
        
        /**
         * *********Layout
         *  Tab (this)
         *      VBox vbxMainContainer (VBox.setVgrow(spSchedulesScrollPane, Priority.ALWAYS);)
         *          AnchorPane anchorPane (AnchorPane.setRightAnchor(hbxButtonContainer, 5.0);)
         *              HBox hbxButtonContainer (.setPadding(new Insets(10.0, 0, 10.0, 0));)
         *                  btnDeleteUnsaved, btnPrintSaved
         *          spSchedulesScrollPane
         *              
         */   
        
        //Create the HBox that will house the "Delete Un-Saved" and "Print Saved" buttons
        //Then add that HBox to an AnchorPane and anchor them to the right of pane
        HBox hbxButtonContainer = new HBox(btnDeleteUnsaved, btnPrintSaved);
        hbxButtonContainer.getStyleClass().add("hbox-main-button-container");
        AnchorPane anchorPane = new AnchorPane(hbxButtonContainer);
        AnchorPane.setRightAnchor(hbxButtonContainer, 5.0);
        
        //Create VBox to house anchor pane with bottons on the top, and the SchedulesScrollPane on bottom     
        VBox vbxMainContainer = new VBox(anchorPane, spSchedulesScrollPane);        
        /**
         * Sets the vertical grow priority for the child [spSchedulesScrollPane] when contained by [vbxMainContainer]. 
         * If set, [vbxMainContainer] will use the priority to allocate additional space if the vbox is resized larger than it's preferred height. 
         * If multiple vbox children have the same vertical grow priority, then the extra space will be split evenly between them. 
         * If no vertical grow priority is set on a child, the vbox will never allocate it additional vertical space if available. 
         * Setting the value to null will remove the constraint.
         */
        VBox.setVgrow(spSchedulesScrollPane, Priority.ALWAYS);

        super.setText("Schedules");
        super.setContent(vbxMainContainer);   
    }

}
