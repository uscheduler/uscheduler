package uscheduler.ui;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.layout.VBox;
import uscheduler.global.UTime;

import java.text.ParseException;
import java.util.ArrayList;

/**
 * Created by psout on 3/8/2016.
 */
public class DayVBox extends VBox{
    private String day;
    private CheckBox checkDay = new CheckBox();
    private ComboBox<UTime> cmbTimeBefore = new ComboBox<>();
    private ComboBox<UTime> cmbTimeAfter = new ComboBox<>();
    private ArrayList<UTime> times = new ArrayList<>();
    private String[] pTimes = {"12:00 am", "1:00 am", "2:00 am", "3:00 am", "4:00 am", "5:00 am", "6:00 am",
            "7:00 am", "8:00 am", "9:00 am", "10:00 am", "11:00 am", "12:00 pm", "1:00 pm", "2:00 pm",
            "3:00 pm", "4:00 pm", "5:00 pm", "6:00 pm", "7:00 pm", "8:00 pm", "9:00 pm", "10:00 pm", "11:00 pm"};
    private final ObservableList<UTime> cmbTimes = FXCollections.observableArrayList();

    DayVBox(String dayOfWeek, int startTime, int endTime){
        this.day = dayOfWeek;
        fillTimes(startTime, endTime);
        cmbTimes.addAll(times);
        checkDay.setText(day);
        cmbTimeBefore.setItems(cmbTimes);
        cmbTimeAfter.setItems(cmbTimes);
        cmbTimeBefore.setValue(cmbTimes.get(0));
        cmbTimeAfter.setValue(cmbTimes.get(cmbTimes.size()-1));
        checkDay.setOnAction(e -> {
                if(!checkDay.isSelected()){
                    cmbTimeAfter.setDisable(true);
                    cmbTimeBefore.setDisable(true);
                }else{
                    cmbTimeAfter.setDisable(false);
                    cmbTimeBefore.setDisable(false);
                }
        });
        if((dayOfWeek.compareTo("Saturday") == 0) || dayOfWeek.compareTo("Sunday") == 0){
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
    }

    private void fillTimes(int startTime, int endTime) {
        UTime t = null;
        for(int i = startTime; i <= endTime; i++){
            try {
                t = new UTime(pTimes[i]);
                times.add(t);
            }catch (ParseException e){

            }
        }
    }

    ArrayList<String> getDayData(){
        ArrayList<String> dayInfo = new ArrayList<>();
        if(checkDay.isSelected()){
            dayInfo.add(day);
            dayInfo.add(cmbTimeBefore.getValue().toString());
            dayInfo.add(cmbTimeAfter.getValue().toString());
        }else{ return null; }
        return dayInfo;
    }
}
