package uSchedule;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.layout.VBox;

import java.util.ArrayList;

/**
 * Created by psout on 3/8/2016.
 */
public class DayVBox extends VBox{
    private String day;
    private CheckBox checkDay = new CheckBox();
    private ComboBox<String> cmbTimeBefore = new ComboBox<>();
    private ComboBox<String> cmbTimeAfter = new ComboBox<>();
    private final ObservableList<String> comboTimeItems = FXCollections.observableArrayList(
            "9:00 am","10:00 am","11:00 am",
            "12:00 pm","1:00 pm","2:00 pm",
            "3:00 pm","4:00 pm","5:00 pm",
            "6:00 pm","7:00 pm","8:00 pm");

    public DayVBox(String dayOfWeek){
        this.day = dayOfWeek;
        checkDay.setText(day);
        cmbTimeBefore.setItems(comboTimeItems);
        cmbTimeAfter.setItems(comboTimeItems);
        cmbTimeBefore.setValue(comboTimeItems.get(0));
        cmbTimeAfter.setValue(comboTimeItems.get(0));
        checkDay.setOnAction(e -> {
                if(checkDay.isSelected() != true){
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
    public ArrayList<String> getDayData(){
        ArrayList<String> dayInfo = new ArrayList<>();
        if(checkDay.isSelected() == true){
            dayInfo.add(day);
            dayInfo.add(cmbTimeBefore.getValue());
            dayInfo.add(cmbTimeAfter.getValue());
        }else{ return null; }
        return dayInfo;
    }
}
