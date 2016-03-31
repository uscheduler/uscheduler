package uSchedule;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.util.ArrayList;

/**
 * Created by aa8439 on 3/31/2016.
 */
public class TopHBox extends HBox {
    private ComboBox<Term> cmbTerm = new ComboBox<>();
    private ListView<String> listCampus = new ListView<>();
    private VBox vTerm = new VBox(5);
    private final Tooltip tooltip = new Tooltip();

    private final ObservableList<Term> terms = FXCollections.observableArrayList();
    private ObservableList<String> campuses = FXCollections.observableArrayList();
    private ArrayList<DayVBox> days = new ArrayList<>();

    TopHBox() {
        days.add(new DayVBox("Monday"));
        days.add(new DayVBox("Tuesday"));
        days.add(new DayVBox("Wednesday"));
        days.add(new DayVBox("Thursday"));
        days.add(new DayVBox("Friday"));
        days.add(new DayVBox("Saturday"));
        days.add(new DayVBox("Sunday"));
        this.getChildren().addAll(days);
        vTerm.getChildren().addAll(new Label("Desired Campuses"),listCampus);
        this.getChildren().add(0, vTerm);
        this.getChildren().add(0, cmbTerm);
        formatItems();
    }
    private void formatItems(){
        tooltip.setText("Press control and left mouse click\n to select multiple entries.");
        listCampus.setMaxHeight(75);
        listCampus.setMaxWidth(175);
        listCampus.setTooltip(tooltip);
        listCampus.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        vTerm.setAlignment(Pos.CENTER);
    }
}
