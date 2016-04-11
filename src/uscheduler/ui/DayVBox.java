package uscheduler.ui;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.layout.VBox;
import uscheduler.global.UTime;
import uscheduler.util.DayTimeArgObserver;
import uscheduler.util.SectionsQuery;

import java.text.ParseException;
import java.time.DayOfWeek;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.Locale;

/**
 * Created by psout on 3/8/2016.
 */
public class DayVBox extends VBox{
    private DayOfWeek day;
    CheckBox checkDay = new CheckBox();
    private ComboBox<UTime> cmbTimeBefore = new ComboBox<>();
    private ComboBox<UTime> cmbTimeAfter = new ComboBox<>();
    private ArrayList<UTime> times = new ArrayList<>();
    private String[] pTimes = {"12:00 am", "1:00 am", "2:00 am", "3:00 am", "4:00 am", "5:00 am", "6:00 am",
            "7:00 am", "8:00 am", "9:00 am", "10:00 am", "11:00 am", "12:00 pm", "1:00 pm", "2:00 pm",
            "3:00 pm", "4:00 pm", "5:00 pm", "6:00 pm", "7:00 pm", "8:00 pm", "9:00 pm", "10:00 pm", "11:00 pm"};
    private final ObservableList<UTime> cmbTimes = FXCollections.observableArrayList();
    SectionsQuery.DayTimeArg dta;

    DayVBox(DayOfWeek dayOfWeek, int startTime, int endTime){
        this.day = dayOfWeek;
        fillTimes(startTime, endTime);
        cmbTimes.addAll(times);
        checkDay.setText(day.getDisplayName(TextStyle.FULL, Locale.getDefault()));
        cmbTimeBefore.setItems(cmbTimes);
        cmbTimeAfter.setItems(cmbTimes);
        cmbTimeBefore.setValue(cmbTimes.get(0));
        cmbTimeAfter.setValue(cmbTimes.get(0));
        if((dayOfWeek.compareTo(DayOfWeek.SATURDAY) == 0) || dayOfWeek.compareTo(DayOfWeek.SUNDAY) == 0){
            checkDay.setSelected(false);
            cmbTimeAfter.setDisable(true);
            cmbTimeBefore.setDisable(true);
        }else{
            checkDay.setSelected(true);
        }
        this.getChildren().addAll(checkDay,cmbTimeBefore,cmbTimeAfter);
        this.setSpacing(15);
        this.setAlignment(Pos.CENTER);
        checkDay.setAlignment(Pos.CENTER);
        dta = new SectionsQuery.DayTimeArg(day);
        this.cmbTimeBefore.getSelectionModel().selectedItemProperty().addListener( (obs, oldValue, newValue) -> {
            if(newValue == null) {
                dta.setMinStart(null);
            }else if(cmbTimeAfter.getValue() == null){
                dta.setMinStart(newValue);
            }else if(newValue.compareTo(this.cmbTimeAfter.getValue()) < 0){ //handle comparing to null
                dta.setMinStart(newValue);
            }else{
                Popup.display(Alert.AlertType.WARNING, "Invalid Time", "The time you are attempting to select is not" +
                        " valid.  Please select a time that is less than the \"No Classes After\" ");
                this.cmbTimeBefore.setValue(oldValue);
            }
        });
        this.cmbTimeAfter.getSelectionModel().selectedItemProperty().addListener( (obs, oldValue, newValue) -> {
            if(newValue == null) {
                dta.setMaxEnd(null);
            }else if(cmbTimeBefore.getValue() == null) {
                dta.setMaxEnd(newValue);
            }else if(newValue.compareTo(this.cmbTimeBefore.getValue()) > 0){
                dta.setMaxEnd(newValue);
            }else{
                Popup.display(Alert.AlertType.WARNING, "Invalid Time", "The time you are attempting to select is not" +
                        " valid.  Please select a time that is greater than the \"No Classes Before\" ");
                this.cmbTimeAfter.setValue(oldValue);
            }
        });
    }
    private void fillTimes(int startTime, int endTime) {
        times.add(null);
        UTime t = null;
        for(int i = startTime; i <= endTime; i++){
            try {
                t = new UTime(pTimes[i]);
                times.add(t);
            }catch (ParseException e){

            }
        }
    }
    void disableDay(boolean b){
        cmbTimeBefore.setDisable(b);
        cmbTimeAfter.setDisable(b);
    }
}