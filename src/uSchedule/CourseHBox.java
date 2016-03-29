package uSchedule;

import com.sun.javafx.property.adapter.PropertyDescriptor;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.awt.*;
import java.util.ArrayList;
import java.util.Collection;

/**
 * Created by aa8439 on 3/7/2016.
 */
public class CourseHBox extends HBox{
    private int onRow = 0;
    private boolean isDisabled = false;
    ComboBox<String> cmbSubject = new ComboBox<>();
    private ComboBox<String> cmbCourseAvail = new ComboBox<>();
    TextField txtCourseID = new TextField();
    private ListView listSectionNumber = new ListView();
    private ListView listSession = new ListView();
    private ListView listFormat = new ListView();
    private ListView listInstructor = new ListView();
    private VBox vSubjCourse = new VBox(5);
    private VBox vSection = new VBox(5);
    private VBox vSession = new VBox(5);
    private VBox vFormat = new VBox(5);
    private VBox vInstructor = new VBox(5);
    private VBox vButtons = new VBox(5);
    Button buttonRemove = new Button("Remove");
    private Button buttonDisable = new Button("Disable");
    private ObservableList<String> sections = FXCollections.observableArrayList();
    private ObservableList<String> sessions = FXCollections.observableArrayList();
    private ObservableList<String> formats = FXCollections.observableArrayList();
    private ObservableList<String> instructors = FXCollections.observableArrayList();
    private final Tooltip tooltip = new Tooltip();

    public CourseHBox(){
        vSubjCourse.getChildren().addAll(cmbSubject,txtCourseID,cmbCourseAvail);
        vSession.getChildren().addAll(new Label("Desired Session"),listSession);
        vSection.getChildren().addAll(new Label("Desired Section"),listSectionNumber);
        vFormat.getChildren().addAll(new Label("Desired Format"),listFormat);
        vInstructor.getChildren().addAll(new Label("Desired Instructor(s)"),listInstructor);
        vButtons.getChildren().addAll(buttonRemove,buttonDisable);
        tooltip.setText("Press control and left mouse click\n to select multiple entries.");
        formatItems();
        fillStaticFields();
        this.getChildren().addAll(vSubjCourse,vSection,vSession,vFormat,vInstructor,vButtons);
        this.setSpacing(5);
        txtCourseID.focusedProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                if(newValue)
                    System.out.println("TextField is in focus");
                else
                    System.out.println("TextField is out of focus");
            }
        });
    }
    private void formatItems(){
        cmbSubject.setPromptText("Select Subject");
        cmbSubject.setMaxWidth(210);
        cmbCourseAvail.setPromptText("Desired Availability");
        cmbCourseAvail.setMaxWidth(210);
        txtCourseID.setPromptText("Choose Course #");
        txtCourseID.setMaxWidth(210);
        buttonRemove.setMinWidth(80);
        buttonDisable.setMinWidth(80);
        listSectionNumber.setMaxHeight(140);
        listSectionNumber.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        listSession.setMaxHeight(140);
        listSession.setTooltip(tooltip);
        listSession.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        listFormat.setMaxHeight(140);
        listFormat.setTooltip(tooltip);
        listFormat.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        listInstructor.setMaxHeight(140);
        listInstructor.setTooltip(tooltip);
        listInstructor.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        vSubjCourse.setMinWidth(210);
        vSubjCourse.setAlignment(Pos.CENTER);
        vSection.setMaxHeight(140);
        vSection.setAlignment(Pos.CENTER);
        vSession.setMaxHeight(140);
        vSession.setAlignment(Pos.CENTER);
        vFormat.setMaxHeight(140);
        vFormat.setAlignment(Pos.CENTER);
        vInstructor.setMaxHeight(140);
        vInstructor.setAlignment(Pos.CENTER);
        vButtons.setAlignment(Pos.CENTER);
        this.setMaxHeight(140);
        this.setAlignment(Pos.CENTER);
    }
    private void fillStaticFields(){
        final ObservableList<String> comboAvailability = FXCollections.observableArrayList(
                "Classes with open seats",
                "Show ALL classes");
        cmbCourseAvail.setItems(comboAvailability);
        buttonDisable.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                if(isDisabled == false) {
                    vSubjCourse.setDisable(true);
                    vSection.setDisable(true);
                    vSession.setDisable(true);
                    vFormat.setDisable(true);
                    vInstructor.setDisable(true);
                    buttonDisable.setText("Enable");
                    isDisabled = true;
                }else{
                    vSubjCourse.setDisable(false);
                    vSection.setDisable(false);
                    vSession.setDisable(false);
                    vFormat.setDisable(false);
                    vInstructor.setDisable(false);
                    buttonDisable.setText("Disable");
                    isDisabled = false;
                }
            }
        });
    }
    public void setCmbSubject(ArrayList<String> subjects){
        final ObservableList<String> subjectObjects = FXCollections.observableArrayList();
        subjectObjects.addAll(subjects);
        cmbSubject.setItems(subjectObjects);
    }
    public void setCmbCourseID(ArrayList<String> courseIDs){
        final ObservableList<String> courseIDObjects = FXCollections.observableArrayList();
        courseIDObjects.setAll(courseIDs);
        //cmbCourseID.setItems(courseIDObjects);
    }
    public int getOnRow(){
        return this.onRow;
    }

    public int setOnRow(int j) {
        this.onRow = j;
        return this.onRow;
    }
    public void setLists(Collection passedSection, Collection passedSession, Collection passedFormat, Collection passedInstructor){
        this.sections.setAll(passedSection);
        if(passedSection.size() > 1){ this.sections.add(0, "All"); }
        this.sessions.setAll(passedSession);
        if(passedSession.size() > 1){ this.sessions.add(0, "All"); }
        this.formats.setAll(passedFormat);
        if(passedFormat.size() > 1){ this.formats.add(0, "All"); }
        this.instructors.setAll(passedInstructor);
        if(passedInstructor.size() > 1){ this.instructors.add(0,"All"); }
        this.listSectionNumber.setItems(this.sections);
        this.listSession.setItems(this.sessions);
        this.listFormat.setItems(this.formats);
        this.listInstructor.setItems(this.instructors);
        this.listSectionNumber.getSelectionModel().selectFirst();
        this.listSession.getSelectionModel().selectFirst();
        this.listFormat.getSelectionModel().selectFirst();
        this.listInstructor.getSelectionModel().selectFirst();
    }
    public void clearLists() {
        this.sections.clear();
        this.sessions.clear();
        this.formats.clear();
        this.instructors.clear();
    }
    public ArrayList<String> getCourseData(){
        ArrayList<String> output = new ArrayList<>();
        if(isDisabled == false){
            output.add(cmbSubject.getValue());
            output.add(cmbCourseAvail.getValue());
            output.add(txtCourseID.getText());
            output.add(listSectionNumber.getSelectionModel().getSelectedItems().toString());
            output.add(listSession.getSelectionModel().getSelectedItems().toString());
            output.add(listFormat.getSelectionModel().getSelectedItems().toString());
            output.add(listInstructor.getSelectionModel().getSelectedItems().toString());
            return output;
        }else{ return null; }
    }
}
