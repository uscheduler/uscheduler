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
import javafx.scene.control.Button;
import javafx.scene.control.Tab;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import uscheduler.internaldata.Schedules;
import uscheduler.util.ScheduleGenerator;
import uscheduler.util.SchedulePrinter;

/**
 *
 * @author Matt
 */
public class SchedulesTab extends Tab{
    
    
    private final Button btnDeleteUnsaved = new Button("Delete Un-Saved");
    private final Button btnPrintSaved = new Button("Print Saved");
    private final HBox hbxButtonContainer = new HBox(btnDeleteUnsaved, btnPrintSaved);
    private final SchedulesScrollPane spSchedulesScrollPane = new SchedulesScrollPane();
    private final VBox vbxMainContainer = new VBox(hbxButtonContainer, spSchedulesScrollPane);
    
    private final FileChooser fileChooser = new FileChooser();
    private Desktop desktop = Desktop.getDesktop();
    
    public SchedulesTab(){
        super.setText("Schedules");
        super.setContent(vbxMainContainer);
        vbxMainContainer.setSpacing(5);
        
        ScheduleGenerator.addObserver(spSchedulesScrollPane);
        VBox.setVgrow(spSchedulesScrollPane, Priority.ALWAYS);

        
        btnDeleteUnsaved.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {    
                //if(Schedules.deleteUnsaved()>0){
                    spSchedulesScrollPane.rebuildSchedules();
                //}
            }
        });
        btnPrintSaved.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) { 
                Stage stage = (Stage)SchedulesTab.this.btnPrintSaved.getScene().getWindow();
                File file = fileChooser.showOpenDialog(stage);
                if (file != null) {
                    try {
                        SchedulePrinter.printSaved(file, true);
                        desktop.open(file);
                    } catch (IOException ex) {
                        Logger.getLogger(SchedulesTab.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }                
            }
        });        
    }

}
