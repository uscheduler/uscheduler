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
import sun.swing.SwingUtilities2;
import uscheduler.externaldata.HTMLFormatException;
import uscheduler.externaldata.NoDataFoundException;
import uscheduler.global.InstructionalMethod;
import uscheduler.internaldata.*;
import uscheduler.util.Importer;
import uscheduler.util.SectionsQuery;
import uscheduler.util.SectionsQueryObserver;

import java.io.IOException;
import java.time.DayOfWeek;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Created by aa8439 on 3/7/2016.
 */
public class CourseHBox extends HBox implements SectionsQueryObserver{
    /*
    Need to implement the following:

    +addCampus(c : Campus)
    +removeCampus(c : Campus)
    + MULTIPLE listeners for user selections(LISTVIEW, COMBOBOX, TEXTFIELD) to update SQ object

    empty objects means no restrictions

     */
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
    private Label remainingSections = new Label();
    private SectionsQuery sectionsQuery = new SectionsQuery();



    /**
     *
     */
    CourseHBox(){
        this.vSubjCourse.getChildren().addAll(this.cmbSubject,this.txtCourseID,this.cmbCourseAvail,this.remainingSections);
        this.resultsChanged(this.sectionsQuery);
        this.listSession.getSelectionModel().selectAll();
        String test;
        test = (listSession.getSelectionModel().getSelectedItems().isEmpty()) ? "(All)": listSession.getSelectionModel().getSelectedItems().size() + " Selected";

        this.vSession.getChildren().addAll(new Label("Desired Session " + test),this.listSession);
        this.vSection.getChildren().addAll(new Label("Desired Section"),this.listSectionNumber);
        this.vFormat.getChildren().addAll(new Label("Desired Format"),this.listFormat);
        this.vInstructor.getChildren().addAll(new Label("Desired Instructor(s)"),this.listInstructor);
        this.vButtons.getChildren().addAll(this.buttonRemove,this.buttonDisable);
        this.tooltip.setText("Press control and left mouse click\n to select multiple entries.");
        this.formatItems();
        this.fillStaticFields();
        this.getChildren().addAll(this.vSubjCourse,this.vSection,this.vSession,this.vFormat,this.vInstructor,this.vButtons);
        this.setSpacing(5);
        this.sectionsQuery.addObserver(this);
    }
    private void formatItems(){
        this.cmbSubject.setPromptText("Select Subject");
        this.cmbSubject.setMaxWidth(210);
        this.cmbCourseAvail.setPromptText("Desired Availability");
        this.cmbCourseAvail.setMaxWidth(210);
        this.txtCourseID.setPromptText("Choose Course #");
        this.txtCourseID.setMaxWidth(210);
        this.buttonRemove.setMinWidth(80);
        this.buttonDisable.setMinWidth(80);
        this.listSectionNumber.setMaxHeight(140);
        this.listSectionNumber.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        this.listSession.setMaxHeight(140);
        this.listSession.setTooltip(tooltip);
        this.listSession.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        this.listFormat.setMaxHeight(140);
        this.listFormat.setTooltip(tooltip);
        this.listFormat.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        this.listInstructor.setMaxHeight(140);
        this.listInstructor.setTooltip(tooltip);
        this.listInstructor.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        this.vSubjCourse.setMinWidth(210);
        this.vSubjCourse.setAlignment(Pos.CENTER);
        this.vSection.setMaxHeight(140);
        this.vSection.setAlignment(Pos.CENTER);
        this.vSession.setMaxHeight(140);
        this.vSession.setAlignment(Pos.CENTER);
        this.vFormat.setMaxHeight(140);
        this.vFormat.setAlignment(Pos.CENTER);
        this.vInstructor.setMaxHeight(140);
        this.vInstructor.setAlignment(Pos.CENTER);
        this.vButtons.setAlignment(Pos.CENTER);
        this.setMaxHeight(140);
        this.setAlignment(Pos.CENTER);
    }
    private void fillStaticFields(){
        final ObservableList<String> comboAvailability = FXCollections.observableArrayList(
                "Classes with open seats",
                "Show ALL classes");
        this.cmbCourseAvail.setItems(comboAvailability);
        this.buttonDisable.setOnAction(e -> {
                if(!this.disabled) {
                    this.vSubjCourse.setDisable(true);
                    this.vSection.setDisable(true);
                    this.vSession.setDisable(true);
                    this.vFormat.setDisable(true);
                    this.vInstructor.setDisable(true);
                    this.buttonDisable.setText("Enable");
                    this.disabled = true;
                }else{
                    this.vSubjCourse.setDisable(false);
                    this.vSection.setDisable(false);
                    this.vSession.setDisable(false);
                    this.vFormat.setDisable(false);
                    this.vInstructor.setDisable(false);
                    this.buttonDisable.setText("Disable");
                    this.disabled = false;
                }
        });
    }
    void setSubjects(ArrayList<Subjects.Subject> s){
        this.subjects.addAll(s);
        this.cmbSubject.setItems(this.subjects);
        this.cmbSubject.setConverter(new StringConverter<Subjects.Subject>() {
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
    void setCourseIDAction(Terms.Term t) {
        txtCourseID.focusedProperty().addListener((ob, oldValue, newValue) -> {
            if (newValue) {
                //System.out.println("TextField is in focus");
            } else {
                if(!txtCourseID.getText().isEmpty()) {
                    System.out.println(": " + txtCourseID.getText());
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
                    this.clearLists();
                    Courses.Course crs = Courses.get(this.cmbSubject.getValue(), this.txtCourseID.getText());
                    List<Sections.Section> sections = Sections.getByCourseReadOnly(t, crs, Sections.SEC_NUM_ASC);
                    System.out.println(sections);
                    this.setSections(sections);
                    this.setSessions(Sections.getDistinctSessions(sections));
                    this.formats.addAll(Sections.getDistinctMethods(sections));
                    this.listFormat.setItems(formats);
                    this.setInstructors(Sections.getDistinctInstructors(sections));
                    this.sectionsQuery.setCourse(crs);
                    this.sectionsQuery.setAvailability(SectionsQuery.AvailabilityArg.ANY);
                    this.resultsChanged(sectionsQuery);
                }
            }
        });
    }
    void setSections(List<Sections.Section> s){
        this.sections.addAll(s);
        this.listSectionNumber.setItems(this.sections);
        this.listSectionNumber.setCellFactory(new Callback<ListView<Sections.Section>, ListCell<Sections.Section>>() {
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
        this.listSectionNumber.setOnMouseClicked(e -> {
            if(this.listSectionNumber.getSelectionModel().getSelectedItems() != null){
                if(this.listSectionNumber.getSelectionModel().getSelectedItems().size() < s.size()) {
                    this.sectionsQuery.removeAllSections();
                    for (Object obj : this.listSectionNumber.getSelectionModel().getSelectedItems()) {
                        this.sectionsQuery.addSection((Sections.Section) obj);
                        //System.out.println("Results : " + sectionsQuery.results());
                    }
                }else{
                    this.sectionsQuery.removeAllSections();
                }
            }
        });
    }
    void setSessions(List<Sessions.Session> s){
        this.sessions.addAll(s);
        this.listSession.setItems(this.sessions);
        this.listSession.setCellFactory(new Callback<ListView<Sessions.Session>, ListCell<Sessions.Session>>() {
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
        this.listSession.setOnMouseClicked(e -> {
            if(this.listSession.getSelectionModel().getSelectedItems() != null){
               if(this.listSession.getSelectionModel().getSelectedItems().size() < s.size()){
                    this.sectionsQuery.removeAllSessions();
                        for (Object obj : this.listSession.getSelectionModel().getSelectedItems()) {
                            this.sectionsQuery.addSession((Sessions.Session) obj);
                        }
                }else{
                    this.sectionsQuery.removeAllSessions();
                }
            }
        });
    }
    void setInstructors(List<Instructors.Instructor> i) {
        this.instructors.addAll(i);
        this.listInstructor.setItems(this.instructors);
        this.listInstructor.setCellFactory(new Callback<ListView<Instructors.Instructor>, ListCell<Instructors.Instructor>>() {
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
        this.listInstructor.setOnMouseClicked(e -> {
            if(this.listInstructor.getSelectionModel().getSelectedItems() != null){
                if(this.listInstructor.getSelectionModel().getSelectedItems().size() < i.size()){
                    this.sectionsQuery.removeAllInstructors();
                    for (Object obj : this.listInstructor.getSelectionModel().getSelectedItems()) {
                        this.sectionsQuery.addInstructor((Instructors.Instructor) obj);
                    }
                }else{
                    this.sectionsQuery.removeAllInstructors();
                }
            }
        });
    }
    void addDayTimeArg(SectionsQuery.DayTimeArg dta){
        this.sectionsQuery.addDayTimeArg(dta);
    }
    void removeDayTimeArg(SectionsQuery.DayTimeArg dta){
        this.sectionsQuery.removeDayTimeArg(dta);
    }
    void setTerm(Terms.Term t){
        this.sectionsQuery.setTerm(t);
    }
    void addCampus(Campuses.Campus c){ this.sectionsQuery.addCampus(c);}
    void removeCampus(){}
    void safeRemove(){
        this.sectionsQuery.close();
    }

    @Override
    public void resultsChanged(SectionsQuery sq) {
        this.remainingSections.setText(this.sectionsQuery.resultsSize() + " Sections available");
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
}
