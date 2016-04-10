package uscheduler.ui;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.ListView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import uscheduler.externaldata.HTMLFormatException;
import uscheduler.externaldata.NoDataFoundException;
import uscheduler.internaldata.Campuses;
import uscheduler.internaldata.Sections;
import uscheduler.internaldata.Subjects;
import uscheduler.internaldata.Terms;
import uscheduler.util.Importer;
import uscheduler.util.ScheduleGenerator;

import java.util.ArrayList;

public class Controller implements Initializable {
    /*
    When changes are made to dayvbox, specifically checked unchecked hboxes need to be iterated through to update the
    change.

    Likewise, term and campuses will need to be updated through each hbox that has been created.
    term being null, is valid, BUT should never be.
    campuses if all are selected than empty list is okay

     */

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
    GridPane grid;

    private TopHBox top = new TopHBox();
    private ObservableList<HBox> hBoxList = FXCollections.observableArrayList();
    private ArrayList<CourseHBox> hBoxes = new ArrayList<>();
    private ArrayList<Terms.Term> terms;
    private ArrayList<Campuses.Campus> campuses;
    private ArrayList<Subjects.Subject> subjects;
    private int POTENTIAL_MAX = 500;


    @Override
    public void initialize(URL location, ResourceBundle resources){
        grid.getChildren().add(0, top);
        CourseHBox course = new CourseHBox();
        hBoxList.add(0, course);
        hBoxes.add(0, course);
        setDeleteAction(0);
        listCourse.setItems(hBoxList);
        handleDayCheckBoxAction();
        getTerms();
        setInitialDayTimeArg(0);
    }
    public void handleAddButton(ActionEvent e) {
        if(hBoxes.size() == 7){
            Popup.display(Alert.AlertType.WARNING, "uScheduler", "You have exceeded the total number of classes that" +
                    " can be taken in a semester");
        }else{
            CourseHBox course = new CourseHBox();
            hBoxList.add(0, course);
            hBoxes.add(0, course);
            updateHBoxPosition();
            setDeleteAction(0);
            setCourseIDAction(0);
            setSubjectOnAdd(0);
            listCourse.setItems(hBoxList);
            setTermInHBox(0);
            setInitialDayTimeArg(0);
        }
    }
    private void setDeleteAction(int j){
        hBoxes.get(j).buttonRemove.setOnAction(e -> {
                hBoxes.get(j).safeRemove();
                hBoxList.remove(hBoxes.get(j).getOnRow());
                hBoxes.remove(hBoxes.get(j).getOnRow());
                updateHBoxPosition();
        });
    }
    private void setCourseIDAction(int j){
        hBoxes.get(j).setCourseIDAction(top.cmbTerm.getValue());
    }
    private void setSubjectOnAdd(int j){
        if(top.cmbTerm.getValue() != null){
            hBoxes.get(j).setSubjects(subjects);
        }
    }
    private void updateHBoxPosition(){
        for(int j = 0; j < hBoxes.size(); j++){
            hBoxes.get(j).setOnRow(j);
            setDeleteAction(j);
        }
    }
    private void handleDayCheckBoxAction(){
        for(DayVBox d: top.days){
            d.checkDay.setOnAction(e -> {
                if(!d.checkDay.isSelected()){
                    d.disableDay(true);
                    for(CourseHBox c: hBoxes){
                        c.removeDayTimeArg(d.dta);
                    }
                }else{
                    d.disableDay(false);
                    for(CourseHBox c: hBoxes) {
                        c.addDayTimeArg(d.dta);
                    }
                }
            });
        }
    }
    private void getTerms(){
        try {
            Importer.loadTerms();
            Importer.loadSubjectsAndCampuses();
        }catch (HTMLFormatException e){
            Popup.display(Alert.AlertType.ERROR, "uScheduler - HTMLFormatException", "It appears that KSU has changed their courses page." +
                    "There is a chance the data collected is corrupt, please contact uscheduler team for resolution.");
            Platform.exit();
        }catch (IOException e){
            Popup.display(Alert.AlertType.ERROR, "uScheduler - IOException", "Looks like you do not have Internet Connectivity." +
                    "  Please fix then relaunch the application");
            Platform.exit();
        }catch (NoDataFoundException e){
            Popup.display(Alert.AlertType.ERROR, "uSchuler - NoDataFoundException", "Unable to find campuses and/or subjects" +
                    "KSU's website may be experiencing difficulty, please try again later.");
            Platform.exit();
        }
        terms = Terms.getAll(Terms.PK_DESC);
        top.setTerms(terms);
        top.cmbTerm.valueProperty().addListener(e -> {
            setSubjectsAndCampuses();
            setCourseIDAction(0);
            for(CourseHBox c: hBoxes) {
                c.setTerm(top.cmbTerm.getValue());
            }
        });
        top.cmbTerm.getSelectionModel().selectedItemProperty().addListener( (obs, oldValue, newValue) -> {
            if(oldValue != null){
                if(Popup.userAccept("uScheduler", "By changing the term all your existing data will be lost, do you still wish to proceed?")){
                    hBoxes.clear();
                    hBoxList.clear();
                }else{
                    top.cmbTerm.setValue(oldValue);
                }
            }
        });
    }
    private void setSubjectsAndCampuses(){
        campuses = Campuses.getAll(Campuses.PK_ASC);
        top.setCampuses(campuses);

        subjects = Subjects.getAll(Subjects.PK_ASC);
        for(int j = 0; j < hBoxes.size(); j++){
            hBoxes.get(j).setSubjects(subjects);
        }
    }
    private void setInitialDayTimeArg(int j){
        for(DayVBox d : top.days){
            hBoxes.get(j).addDayTimeArg(d.dta);
        }
    }
    private void setTermInHBox(int j){
        hBoxes.get(0).setTerm(top.cmbTerm.getValue());
    }

    public void handleGenerateSchedule(ActionEvent e) {
        tabPane.getSelectionModel().select(resultsTab);
        /*/Disable Generate Schedules button if any of the sectionsquery(s) are set to 0
        Check POTENTIAL_MAX
        cannot duplicate courses
        */
        //create 2 dimensional array where first dimesion is hboxes.size();
        Sections.Section[][] courseSections = new Sections.Section[hBoxes.size()][];
        for(int i = 0; i < hBoxes.size(); i++){
            courseSections[i] = hBoxes.get(i).getSectionsQuery().results2();
        }
        int schedulesGenerated = ScheduleGenerator.generate(courseSections);
    }
}
