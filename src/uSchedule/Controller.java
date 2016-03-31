package uSchedule;

import javafx.fxml.FXML;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.ListView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import java.util.ArrayList;
import javafx.geometry.Pos;

public class Controller implements Initializable {

    @FXML
    TabPane tabPane;
    @FXML
    Tab resultsTab;
    @FXML
    Tab inputTab;
    @FXML
    Button buttonGenerateSchedule;
    @FXML
    Button buttonAddCourse;
    @FXML
    ListView listCourse;
    @FXML
    Label displayText;
    @FXML
    HBox top;

    private ComboBox<Term> cmbTerm = new ComboBox<>();
    private ListView<String> listCampus = new ListView<>();
    private VBox vTerm = new VBox(5);

    private final ObservableList<Term> terms = FXCollections.observableArrayList();
    private ObservableList<String> campuses = FXCollections.observableArrayList();
    private ObservableList<HBox> hBoxList = FXCollections.observableArrayList();

    private ArrayList<CourseHBox> hBoxes = new ArrayList<>();
    private ArrayList<DayVBox> days = new ArrayList<>();
    private final Tooltip tooltip = new Tooltip();

    private InternalDataManager idb = new InternalDataManager();

    @Override
    public void initialize(URL location, ResourceBundle resources){
        vTerm.getChildren().addAll(new Label("Desired Campuses"),listCampus);
        top.getChildren().add(0, vTerm);
        top.getChildren().add(0, cmbTerm);
        listCampus.setMaxHeight(75);
        listCampus.setMaxWidth(175);
        tooltip.setText("Press control and left mouse click\n to select multiple entries.");
        listCampus.setTooltip(tooltip);
        listCampus.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        getTerms();
        CourseHBox course = new CourseHBox();
        hBoxList.add(0, course);
        hBoxes.add(0, course);
        setDeleteAction(0);
        setCourseNumAction(0);
        listCourse.setItems(hBoxList);
        vTerm.setAlignment(Pos.CENTER);
        days.add(new DayVBox("Monday"));
        days.add(new DayVBox("Tuesday"));
        days.add(new DayVBox("Wednesday"));
        days.add(new DayVBox("Thursday"));
        days.add(new DayVBox("Friday"));
        days.add(new DayVBox("Saturday"));
        days.add(new DayVBox("Sunday"));
        top.getChildren().addAll(days);
    }
    public void handleAddButton(ActionEvent e) {
        if(hBoxes.size() == 7){
            Popup.display(Alert.AlertType.WARNING, "Warning", "You have exceeded the total number of classes that" +
                    "can be taken in a semester");
        }else{
            CourseHBox course = new CourseHBox();
            hBoxList.add(0, course);
            hBoxes.add(0, course);
            updateHBoxPosition();
            setDeleteAction(0);
            setCourseNumAction(0);
            setSubjectOnAdd(0);
            setSubjectOnAction(0);
            listCourse.setItems((hBoxList));
        }
    }
    private void setDeleteAction(int j){
        hBoxes.get(j).buttonRemove.setOnAction(e -> {
                hBoxList.remove(hBoxes.get(j).getOnRow());
                hBoxes.remove(hBoxes.get(j).getOnRow());
                updateHBoxPosition();
        });
    }
    private void setSubjectOnAction(int j){
        hBoxes.get(j).cmbSubject.setOnAction(e -> {
                hBoxes.get(j).setCmbCourseID(idb.getCourseNum(hBoxes.get(j).cmbSubject.getValue()));
        });
    }
    private void setCourseNumAction(int j){
        hBoxes.get(j).txtCourseID.setOnAction(e -> {
            hBoxes.get(j).setLists(
                    idb.getSections(hBoxes.get(j).cmbSubject.getValue(), hBoxes.get(j).txtCourseID.getText()),
                    idb.getSessions(hBoxes.get(j).cmbSubject.getValue(), hBoxes.get(j).txtCourseID.getText()),
                    idb.getFormat(hBoxes.get(j).cmbSubject.getValue(), hBoxes.get(j).txtCourseID.getText()),
                    idb.getInstructors(hBoxes.get(j).cmbSubject.getValue(), hBoxes.get(j).txtCourseID.getText())
            );
        });
    }
    private void setSubjectOnAdd(int j){
        if(cmbTerm.getValue() != null){
            //hBoxes.get(j).setCmbSubject(idb.getSubjects(cmbTerm.getValue()));
        }
    }
    private void updateHBoxPosition(){
        for(int j = 0; j < hBoxes.size(); j++){
            hBoxes.get(j).setOnRow(j);
            setDeleteAction(j);
            setCourseNumAction(j);
            setSubjectOnAction(j);
            System.out.println(hBoxes.get(j).getOnRow());
        }
    }
    private void getTerms(){
        //terms.addAll(idb.getOpenTerms());
        Term testTerm = new Term();
        testTerm.setTerm(20160106, "Spring 2016");
        testTerm.toString();
        terms.add(testTerm);
        cmbTerm.setItems(terms);
        cmbTerm.setPromptText("Select Term");

       /* cmbTerm.valueProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                //Need to write more code to reset everything if new term is selected
                campuses.addAll(idb.getCampuses(newValue));
                listCampus.setItems(campuses);
                listCampus.getSelectionModel().selectFirst();
                for(int j = 0; j < hBoxes.size(); j++){
                    hBoxes.get(j).setCmbSubject(idb.getSubjects(newValue));
                    setSubjectOnAction(j);
                }
            }
        });*/
    }
    public void handleGenerateSchedule(ActionEvent e) {
        tabPane.getSelectionModel().select(resultsTab);
        String output = "";
        output += "Term: \n=====================\n";
        output += cmbTerm.getValue() + "\n" ;
        output += "=====================\nCampuses: \n=====================\n";
        output += listCampus.getSelectionModel().getSelectedItems().toString() + "\n";
        output += "=====================\nDays: \n=====================\n";
        for(DayVBox day: days){
            if(day.getDayData() != null){
                output += (day.getDayData().toString()) + "\n";
            }
        }
        output += "=====================\nCourses: \n=====================\n";
        for(CourseHBox row: hBoxes){
            if(row.getCourseData() != null){
                output += (row.getCourseData().toString()) + "\n";
            }
        }
        displayText.setText(output);
    }
}