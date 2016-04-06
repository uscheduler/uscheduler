package uscheduler.ui;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.util.Callback;
import javafx.util.StringConverter;
import uscheduler.externaldata.HTMLFormatException;
import uscheduler.externaldata.NoDataFoundException;
import uscheduler.global.InstructionalMethod;
import uscheduler.internaldata.*;
import uscheduler.util.Importer;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Created by aa8439 on 3/7/2016.
 */
public class CourseHBox extends HBox{
    private int onRow = 0;
    private boolean disabled = false;
    ComboBox<Subjects.Subject> cmbSubject = new ComboBox<>();
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
    private ObservableList<Subjects.Subject> subjects = FXCollections.observableArrayList();
    private ObservableList<Sections.Section> sections = FXCollections.observableArrayList();
    private ObservableList<Sessions.Session> sessions = FXCollections.observableArrayList();
    private ObservableList<InstructionalMethod> formats = FXCollections.observableArrayList();
    private ObservableList<Instructors.Instructor> instructors = FXCollections.observableArrayList();
    private final Tooltip tooltip = new Tooltip();


    /**
     *
     */
    CourseHBox(){
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
        buttonDisable.setOnAction(e -> {
                if(!disabled) {
                    vSubjCourse.setDisable(true);
                    vSection.setDisable(true);
                    vSession.setDisable(true);
                    vFormat.setDisable(true);
                    vInstructor.setDisable(true);
                    buttonDisable.setText("Enable");
                    disabled = true;
                }else{
                    vSubjCourse.setDisable(false);
                    vSection.setDisable(false);
                    vSession.setDisable(false);
                    vFormat.setDisable(false);
                    vInstructor.setDisable(false);
                    buttonDisable.setText("Disable");
                    disabled = false;
                }

        });
    }
    void setSubjects(ArrayList<Subjects.Subject> s){
        this.subjects.addAll(s);
        cmbSubject.setItems(subjects);
        cmbSubject.setConverter(new StringConverter<Subjects.Subject>() {
            @Override
            public String toString(Subjects.Subject object) {
                return object.subjectAbbr() + " - " + object.subjectName();
            }

            @Override
            public Subjects.Subject fromString(String string) {
                return null;
            }
        });
    }
    int getOnRow(){
        return this.onRow;
    }

    int setOnRow(int j) {
        this.onRow = j;
        return this.onRow;
    }
    public void clearLists() {
        this.sections.clear();
        this.sessions.clear();
        this.formats.clear();
        this.instructors.clear();
    }
    ArrayList<String> getCourseData(){
        ArrayList<String> output = new ArrayList<>();
        if(!disabled){
            //output.add(cmbSubject.getValue());
            output.add(cmbCourseAvail.getValue());
            output.add(txtCourseID.getText());
            output.add(listSectionNumber.getSelectionModel().getSelectedItems().toString());
            output.add(listSession.getSelectionModel().getSelectedItems().toString());
            output.add(listFormat.getSelectionModel().getSelectedItems().toString());
            output.add(listInstructor.getSelectionModel().getSelectedItems().toString());
            return output;
        }else{ return null; }
    }
    void setCourseIDAction(Terms.Term t) {
        txtCourseID.focusedProperty().addListener((ob, oldValue, newValue) -> {
            if (newValue) {
                //System.out.println("TextField is in focus");
            } else {
                //System.out.println("TextField is out of focus");
                try {
                    Importer.loadSections(t, cmbSubject.getValue(), txtCourseID.getText());
                } catch (HTMLFormatException e) {
                    Popup.display(Alert.AlertType.ERROR, "HTMLFormatException", "It appears that KSU has changed their courses page." +
                            "There is a chance the data collected is corrupt, please contact uscheduler team for resolution.");
                    Platform.exit();
                } catch (IOException e) {
                    Popup.display(Alert.AlertType.ERROR, "IOException", "Looks like you do not have Internet Connectivity." +
                            "  Please fix then relaunch the application");
                    Platform.exit();
                } catch (NoDataFoundException e) {
                    Popup.display(Alert.AlertType.WARNING, "NoDataFoundException", "It appears you entered an incorrect " +
                            "course ID.  Please try entering another.  If you are sure the number you entered is correct," +
                            " please try entering it again.");
                }
                clearLists();
                Courses.Course crs = Courses.get(cmbSubject.getValue(), txtCourseID.getText());
                List<Sections.Section> sections = Sections.getByCourseReadOnly(t, crs, Sections.SEC_NUM_ASC);
                setSections(sections);
                setSessions(Sections.getDistinctSessions(sections));
                formats.addAll(Sections.getDistinctMethods(sections));
                listFormat.setItems(formats);
                setInstructors(Sections.getDistinctInstructors(sections));
            }
        });
    }
    void setSections(List<Sections.Section> s){
        this.sections.addAll(s);
        listSectionNumber.setItems(sections);
        listSectionNumber.setCellFactory(new Callback<ListView<Sections.Section>, ListCell<Sections.Section>>() {
            @Override
            public ListCell<Sections.Section> call(ListView<Sections.Section> param) {
                ListCell<Sections.Section> cell = new ListCell<Sections.Section>() {
                    @Override
                    protected void updateItem(Sections.Section o, boolean bln) {
                        super.updateItem(o, bln);
                        if (o != null) {
                            setText(o.sectionNumber());
                        }
                    }
                };
                return cell;
            }
        });

    }
    void setSessions(List<Sessions.Session> s){
        this.sessions.addAll(s);
        listSession.setItems(sessions);
        listSession.setCellFactory(new Callback<ListView<Sessions.Session>, ListCell<Sessions.Session>>() {
            @Override
            public ListCell<Sessions.Session> call(ListView<Sessions.Session> param) {
                ListCell<Sessions.Session> cell = new ListCell<Sessions.Session>() {
                    @Override
                    protected void updateItem(Sessions.Session o, boolean bln) {
                        super.updateItem(o, bln);
                        if (o != null) {
                            setText(o.sessionName());
                        }
                    }
                };
                return cell;
            }
        });
    }
    void setInstructors(List<Instructors.Instructor> i) {
        this.instructors.addAll(i);
        listInstructor.setItems(instructors);
        listInstructor.setCellFactory(new Callback<ListView<Instructors.Instructor>, ListCell<Instructors.Instructor>>() {
            @Override
            public ListCell<Instructors.Instructor> call(ListView<Instructors.Instructor> param) {
                ListCell<Instructors.Instructor> cell = new ListCell<Instructors.Instructor>() {
                    @Override
                    protected void updateItem(Instructors.Instructor o, boolean bln) {
                        super.updateItem(o, bln);
                        if (o != null) {
                            setText(o.instructorName());
                        }
                    }
                };
                return cell;
            }
        });
    }
}
