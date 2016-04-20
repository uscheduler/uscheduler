/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uscheduler.ui.input;

import java.time.DayOfWeek;
import java.util.Collections;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.ListChangeListener;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import uscheduler.util.SectionsQuery;
import uscheduler.util.SectionsQuery.DayTimeArg;

/**
 *
 * @author Matt
 */
public class DayInputContainer extends VBox implements DayInputCheckListener{
    //Data
    /**
     * A set of DayTimeArgs corresponding to the ones that are selected
     * This list will be added to and removed from when days are checked and unchecked.
     * The method dayCheckChange that overrides DayInputCheckListener handles adding and removing based on checked/unchecked.
     * This list is observable so that it can be listened to by an object of the CourseInputClas
     */
    private final ObservableList<DayTimeArg> cDayTimeArgs = FXCollections.observableArrayList();
    

    
    /**
     * Creates 7 DayInputs corresponding to 7 days of week and lays them out in the 2nd row of this GridPane
     * Each DayInputs will fill a single column of the 2nd row.
     * The first row will contain text that gives user instruction/tip on what the input controls are.
     * This 1st row text will span all 7 columns and will be centered
     */
    public DayInputContainer(){
        
        /** **************** Layout
         * VBox (this)
         *      StackPane stackPane 
         *          Text txtLabel
         *      HBox hbxDayTimeArgs
         *          VBox DayInput class
         *          ...
         *          VBox DayInput class
         */
        Text txt = new Text("Allowable Meeting Days, Min class start time, Max class end time");
        StackPane stackPane = new StackPane(txt);
        stackPane.getStyleClass().add("stack-pane-grey-label-back");

        HBox hbxDayTimeArgs = new HBox();
        hbxDayTimeArgs.getStyleClass().add("hbox-day-time-args");
        for(DayOfWeek dow : DayOfWeek.values()){
            
            DayInput dvBox = new DayInput(dow);
            //add self as listener of the check event so that can modify the cDayTimeArgs observable list accordingly
            dvBox.addDayVBoxCheckListener(this);
            if(dvBox.isSelected())
                cDayTimeArgs.add(dvBox.dayTimeArg());
            hbxDayTimeArgs.getChildren().add(dvBox);
        }
        super.getChildren().addAll(stackPane,hbxDayTimeArgs);
            
    }
    
    public ObservableList<DayTimeArg> dayTimeArgs(){
        return FXCollections.unmodifiableObservableList(cDayTimeArgs);
    }
    public void addListChangeListener(ListChangeListener<? super DayTimeArg> listener){
        cDayTimeArgs.addListener(listener);
    }
    public void removeListChangeListener(ListChangeListener<? super DayTimeArg> listener){
        cDayTimeArgs.removeListener(listener);
    }
    @Override
    public void dayCheckChange(SectionsQuery.DayTimeArg pDayTimeArg, boolean pChecked) {
        if(pChecked){
            if (!cDayTimeArgs.contains(pDayTimeArg))
            cDayTimeArgs.add(pDayTimeArg);
        }
        else
            cDayTimeArgs.removeAll(Collections.singleton(pDayTimeArg));
    }
    
}
