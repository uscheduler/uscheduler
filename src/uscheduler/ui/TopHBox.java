package uscheduler.ui;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.util.Callback;
import javafx.util.StringConverter;
import uscheduler.internaldata.Campuses;
import uscheduler.internaldata.Subjects;
import uscheduler.internaldata.Terms;
import java.util.ArrayList;

/**
 * Created by aa8439 on 3/31/2016.
 */
public class TopHBox extends HBox {
    ComboBox<Terms.Term> cmbTerm = new ComboBox<>();
    private ListView<Campuses.Campus> listCampus = new ListView<>();
    private VBox vCampus = new VBox(5);
    private VBox vLabels = new VBox();
    private Label before = new Label("No Classes Before:");
    private Label after = new Label("No Classes After:");
    private final Tooltip tooltip = new Tooltip();
    private ObservableList<Terms.Term> terms = FXCollections.observableArrayList();
    private ObservableList<Campuses.Campus> campuses = FXCollections.observableArrayList();
    private ArrayList<DayVBox> days = new ArrayList<>();

    TopHBox() {
        vCampus.getChildren().addAll(new Label("Desired Campuses"),listCampus);
        vLabels.getChildren().addAll(before, after);
        this.getChildren().addAll(cmbTerm, vCampus, vLabels);
        days.add(new DayVBox("Monday", 8, 20));
        days.add(new DayVBox("Tuesday", 8, 20));
        days.add(new DayVBox("Wednesday", 8, 20));
        days.add(new DayVBox("Thursday", 8, 20));
        days.add(new DayVBox("Friday", 8, 20));
        days.add(new DayVBox("Saturday", 8, 20));
        days.add(new DayVBox("Sunday", 8, 20));
        this.getChildren().addAll(days);
        formatItems();
    }
    private void formatItems(){
        tooltip.setText("Press control and left mouse click\n to select multiple entries.");
        listCampus.setMaxHeight(75);
        listCampus.setMaxWidth(175);
        listCampus.setTooltip(tooltip);
        listCampus.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        vCampus.setAlignment(Pos.CENTER);
        vLabels.setAlignment(Pos.CENTER);
        before.setPadding(new Insets(30, 10, 0, 0));
        after.setPadding(new Insets(20, 0, 0, 0));
        this.setAlignment(Pos.CENTER);
        this.setSpacing(15);
    }
    public void setTerms(ArrayList<Terms.Term> t){
        this.terms.addAll(t);
        cmbTerm.setItems(terms);
        cmbTerm.setConverter(new StringConverter<Terms.Term>() {
            @Override
            public String toString(Terms.Term object) {
                return object.termName();
            }

            @Override
            public Terms.Term fromString(String string) {
                return null;
            }
        });
    }
    public void setCampuses(ArrayList<Campuses.Campus> c){
        this.campuses.addAll(c);
        listCampus.setItems(campuses);
        listCampus.setCellFactory(new Callback<ListView<Campuses.Campus>, ListCell<Campuses.Campus>>() {
            @Override
            public ListCell<Campuses.Campus> call(ListView<Campuses.Campus> param) {
                ListCell<Campuses.Campus> cell = new ListCell<Campuses.Campus>() {
                    @Override
                    protected void updateItem(Campuses.Campus o, boolean bln) {
                        super.updateItem(o, bln);
                        if (o != null) {
                            setText(o.campusName());
                        }
                    }
                };
                return cell;
            }
        });
    }
}
