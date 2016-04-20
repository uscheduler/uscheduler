/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uscheduler.ui.input;

import com.sun.javafx.scene.control.skin.ComboBoxListViewSkin;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import javafx.beans.property.ObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.util.Callback;
import uscheduler.UScheduler;
import uscheduler.externaldata.HTMLFormatException;
import uscheduler.externaldata.NoDataFoundException;
import uscheduler.global.InstructionalMethod;
import uscheduler.internaldata.Campuses.Campus;
import uscheduler.internaldata.Courses;
import uscheduler.internaldata.Instructors.Instructor;
import uscheduler.internaldata.Sections;
import uscheduler.internaldata.Sections.Section;
import uscheduler.internaldata.Sessions.Session;
import uscheduler.internaldata.Subjects;
import uscheduler.internaldata.Subjects.Subject;
import uscheduler.internaldata.Terms.Term;
import uscheduler.util.Importer;
import uscheduler.util.SectionsQuery;
import uscheduler.util.SectionsQuery.AvailabilityArg;
import uscheduler.util.SectionsQuery.DayTimeArg;
import uscheduler.util.SectionsQueryObserver;

/** 
 * This Class provides the view to obtain user input associated with a single SectionsQuery, which when combined with other instances of the class, will be the input needed to generate schedule.
 * The controls presented in this view have many complex dependencies. The following will attempt to explain what the dependencies are and how this class manages them.
 * 
 * For clarity, the term "data source", for a given control, will mean the set of items that are presented in a control and are candidates for "selection" by the user.
 * The term "data selection", for a given control,  will mean the set of items in the control that have been selected or entered (by the user or the program)
 * 
 * The following lists the dependencies among various control's data source and data selection: 
 * 
 * Term Data Selection affects affects Section Data Source, Sessions Data Source, Instructional Methods Data Source, and Instructors Data Source (same as Term)
 * Subject Data Selection affects Course Data Source (affects which values are valid / in db)
 * Course Data Selection (input) affects Section Data Source, Sessions Data Source, Instructional Methods Data Source, and Instructors Data Source (same as Term)
 * -------------------------------
 * Each control's Data Source affects its own Data Selection
 * Each control's Data Selection affects the corresponding SectionsQuery values
 * -------------------------------

 * @author Matt Bush
 */

public class CourseInput extends HBox implements SectionsQueryObserver{

     //The SectionsQuery object will be the sole source of Term/Course state information.
    private final SectionsQuery cSectionsQuery;
    
    //Stores the CourseHBoxDeletRequestListeners which will be notified when the "remove course" button is clicked. 
    //The CourseHBoxContainer will more than likely be the only interested listener.
    private final HashSet<CourseRemoveRequestListener> cRemoveRequestListeners = new HashSet<>();
    private final HashSet<CourseInsertRequestListener> cInsertRequestListeners = new HashSet<>();
    
    //Externally Depending Values/Properties
    //The value of the currently selected in the CoursesTab class.
    //To simplify this class' methods, it will be assumed this vale will never be null
    //Exceptions will be thrown if ever tis value is null
    private final  ObjectProperty<Term> cTermValueProperty; 
    //An observable list of selected Campuses in the CoursesTab class.
    private final  ObservableList<Campus> cCampusObservableList;
    //An observable list of the selected DayTimeArgs in the DayInputContainer class.
    private final  ObservableList<DayTimeArg> cDayTimeArgsObservableList;
    
    //Controls
    private final ComboBox<Subject> cmbSubjects;
    private final TextField txtCourse;
    private final ComboBox<AvailabilityArg> cmbAvailabilityArgs;
    private final ListView<Section> lstSections;
    private final ListView<Session> lstSessions;
    private final ListView<InstructionalMethod> lstInstructionalMethods;
    private final ListView<Instructor> lstInstructors;
    private final Button btnRemove;
    private final Button btnInsert;
    private final Text txtSectionsCount;
    
    //Misc
    private static final int LIST_MAX_HEIGHT = 6 * 24 + 2;//6 cells, 24px row height + 2px for edges
    private static final String INVALID_COURSE_INPUT_MSG = "Course number must be 4 or 5 characters long and only contain alpha-numeric characters.";

    private static final Tooltip TT_SUBJECTS = new Tooltip("The number of times you would have to commute from one campus to another in the same day.");
    private static final Tooltip TT_COURSE = new Tooltip("The estimated total number of hours you would be at school with this schedule.");
    private static final Tooltip TT_AVAILABILITY= new Tooltip("The estimated total number of days you would be at school with this schedule.");
    private static final Tooltip TT_SECTIONS = new Tooltip("A saved schedule will remain after performing another schedule generation, while un-saved schedules will be removed. ALso, saved schedules can be printed.");
    private static final Tooltip TT_SESSIONS = new Tooltip("Removes this schedule.");
    private static final Tooltip TT_METHODS = new Tooltip("Removes this schedule.");
    private static final Tooltip TT_INSTRUCTORS = new Tooltip("Removes this schedule.");
    private static final Tooltip TT_REMOVE = new Tooltip("Removes this schedule.");
    private static final Tooltip TT_INSERT = new Tooltip("Inserts a new blanck course param");
                    
    /**
     * ******************************************************************************************************
     * ************Critical Selection/Value Change Listeners (Term, Subject, Course) ************
     * ******************************************************************************************************
     */
    
    /**
    * *************cTermValueProperty change listener::: 
    * Assumptions: cTermValueProperty will never be null. Exceptions thrown if it is.
    */
    private final ChangeListener<Term> TERM_VALUE_CHANGE_LISTENER = new ChangeListener<Term>() { 
        @Override
        public void changed(ObservableValue<? extends Term> observable, Term oldValue, Term newValue) {
            cSectionsQuery.setTerm(newValue);
            if(cSectionsQuery.course() != null){
                if(Sections.getByCourse1(cSectionsQuery.term(), cSectionsQuery.course()).isEmpty()){
                    try {
                        Importer.loadSections(cSectionsQuery.term(), cSectionsQuery.course().subject(), cSectionsQuery.course().courseNum());
                        cSectionsQuery.setTerm(newValue);
                    } catch (HTMLFormatException | IOException | NoDataFoundException ex) {
                    }
                }
                CourseInput.this.setVariableSouces();
            }
        }
    };
    /**
    * 
    * *************cmbSubjects.valueProperty() change listener::: 
    * Assumptions: 
    * 1) cSectionsQuery.term() will never be null. Exceptions thrown if it is.
    * 2)In the context of THIS method, cmbSubjects.getValue() will never be null.
    * 3) In the context of THIS method 
     */
    private final ChangeListener<Subject> SUBJECT_VALUE_CHANGE_LISTENER = new ChangeListener<Subject>() { 
        @Override
        public void changed(ObservableValue<? extends Subject> observable, Subject oldValue, Subject newValue) {
            if(cSectionsQuery.course() != null){//this MUST also imply that txtCourse is not null. If this implication is false, there is a bug.
                if(Courses.get(newValue, cSectionsQuery.course().courseNum())==null){//not in db
                    //attempt an import
                    try {
                        Importer.loadSections(cSectionsQuery.term(), newValue, cSectionsQuery.course().courseNum());
                        COURSE_VALUE_CHANGE_LISTENER.changed(null, "", cSectionsQuery.course().courseNum());
                    } catch (HTMLFormatException | IOException | NoDataFoundException ex) {
                        txtCourse.setText(null);
                    }   
                } else{
                    COURSE_VALUE_CHANGE_LISTENER.changed(null, "", cSectionsQuery.course().courseNum());
                }
            }
            txtCourse.setDisable(newValue==null);//txtCourse enabled only when a subject is selected. By default, txtCourse is disabled. This should enable it and it will stay enabled.
        }
    };    
    /**
    * *************txtCourse.textProperty() change listener::: 
    * Assumptions: Within THIS method, cmbSubjects.getValue() will never be null. This control should be disabled if cmbSubjects is null.
    * 
    * This method will execute on every keypress, thus this is NOT the method to attempt an import.
    * This method will setup the focusProperty listener IF the txtCourse control has the focus (implying this value set by user)
    * AND there's no matching course in the db, So that the focusProperty listener on lost focus can 
    * 1) Validate that input is 4 or 5 chars long and warn user if not and
    * 2)attempt an import after the user leaves the txtCourse control if input is valid
    * This method will guarantee that txtCourse (is null or contains only alphanumeric chars) and (is less than 6 chars long)
    * 
     */
     final ChangeListener<String> COURSE_VALUE_CHANGE_LISTENER = new ChangeListener<String>() { 

        @Override
        public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
            //Remove txtCourse.focusedProperty() at the begining and add it again at the end.
            //This ensures it will never be added twice since the fucking designers implemented the collection to store added listeners as a list and not a set
            txtCourse.focusedProperty().removeListener(COURSE_FOCUS_CHANGE_LISTENER);

            //Ensure new value only contains alphanumeric chars an is not more than 5 character. 
            //If it is, remove self as listener, and reset it it, then add self back as listener.
            if(newValue!= null){
                String cleanString = newValue.replaceAll("[^a-zA-Z0-9]", "");
                cleanString = cleanString.substring(0, Math.min(cleanString.length(), 5));
                if (!cleanString.equals(newValue)){
                    txtCourse.textProperty().removeListener(this);
                    txtCourse.setText(cleanString);
                    txtCourse.textProperty().addListener(this);
                }
                cSectionsQuery.setCourse(Courses.get(cmbSubjects.getValue(), cleanString));      
                if(cSectionsQuery.course()==null && txtCourse.focusedProperty().get()){
                    txtCourse.focusedProperty().addListener(COURSE_FOCUS_CHANGE_LISTENER); 
                }                  
            } else {
                cSectionsQuery.setCourse(null);
            }
            CourseInput.this.setVariableSouces();
        }
    };  
    /**
    * *************txtCourse.focusedProperty():::
    * This executes when txtCourse has lost focus after having the focus and being edited and when the value doesn't have a matching course in the db.
    * Note: This method should never execute if there is a matching course in the db, because that implies the input is valid and no need to import.
    * The point of this method is 
    * 1) To validate the user input (must be 4 or 5 chars long) (method above ensures no invalid chars)
    * 2) If valid, perform an import.
    */
    private final ChangeListener<Boolean> COURSE_FOCUS_CHANGE_LISTENER =new ChangeListener<Boolean>() {
        @Override
        public void changed(ObservableValue<? extends Boolean> ob, Boolean oldValue, Boolean newValue) {
            //always remove listener at the begining 
            txtCourse.focusedProperty().removeListener(this);
            //Validat input
            String cleanString = txtCourse.getText().replaceAll("[^a-zA-Z0-9]", "");
            cleanString = cleanString.substring(0, Math.min(cleanString.length(), 5));
            if (!cleanString.equals(txtCourse.getText()) || !(cleanString.length()==4 || cleanString.length()==5)){
                Alert alert = new Alert(Alert.AlertType.INFORMATION, INVALID_COURSE_INPUT_MSG);
                alert.setTitle("Invalid Course Number");
                alert.showAndWait();  
                //NOTE: It may be tempting to set focus back to txtCourse and re-add self as listener, 
                //but that implementation does not gurantee that txtCourse can't loose the focus.
                //The simpler implementation is just to set txtCourse to null after displaying message
                txtCourse.setText(null);
            } else {//attempt import
                boolean doAgain = true;
                while(doAgain){
                    doAgain=false;
                    try {
                        Importer.loadSections(cSectionsQuery.term(), cmbSubjects.getValue(), txtCourse.getText());
                        COURSE_VALUE_CHANGE_LISTENER.changed(null, "", txtCourse.getText());
                    } catch (HTMLFormatException ex) {
                        Alert alert = new Alert(Alert.AlertType.ERROR, UScheduler.HTML_EXCEPTION_MSG);
                        alert.setTitle("Game Over!");
                        alert.showAndWait();
                        txtCourse.setText(null);
                    } catch (IOException ex) {
                        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, UScheduler.IO_EXCEPTION_MSG);
                        alert.setTitle("Failed Connection");
                        Optional<ButtonType> result = alert.showAndWait();

                        doAgain = (result.get() == ButtonType.OK);
                        if(!doAgain)
                            txtCourse.setText(null);
                        
                    } catch (NoDataFoundException ex) {
                        Alert alert = new Alert(Alert.AlertType.INFORMATION, UScheduler.NO_DATA_FOUND_EXCEPTION_MSG);
                        alert.setTitle("Course Not Found");
                        alert.showAndWait(); 
                        txtCourse.setText(null);
                    } 
                }                
            }
        }
    };    
    /**
     * ******************************************************************************************************
     * ***************************NOTE FOR ALL SelectedItems ListChangeListeners*****************************
     * ******************************************************************************************************
     * Originally these method were designed to iterate through each change using c.next(), then 
     * iterating through each adding  to the SectionsQuery each item using if c.wasAdded() and for each xxx in c.getAddedSubList(). 
     * The same logic was applied for removing items using if c.wasRemoved() and for each xxx in c.getRemoved.
     * But there was some kind of bug in which c.wasAdded() is returning true, yet c.getFrom() returns -1 and c.getTo is returning 0
     * This was causing c.getAddedSubList() to fail, because c.getAddedSubList() is short for c.getSubList(c.getFrom(), c.getTo()) which was causing index out of bound.
     * The approach of this method now is to not use the Change object at all and just use the entire SelectedItems list.
     * 
     */
    /**
     * ******************************************************************
     * ************Non-Critical Selection Change Listeners ************
     * ******************************************************************
     */
    private final ListChangeListener<Campus> CAMPUS_SELECTION_CHANGE_LISTENER = new ListChangeListener<Campus>(){
        @Override
        public void onChanged(ListChangeListener.Change<? extends Campus> c) {
            cSectionsQuery.removeAllCampuses();
            for(Campus camp : cCampusObservableList){
                if (camp == null){
                    cSectionsQuery.removeAllCampuses();
                    return;
                } else{
                    cSectionsQuery.addCampus(camp);
                }

            }
        }
    };
    private final ListChangeListener<DayTimeArg> DAY_TIME_ARGS_LIST_CHANGE_LISTENER = new ListChangeListener<DayTimeArg>(){
            @Override
            public void onChanged(ListChangeListener.Change<? extends DayTimeArg> c) {
                cSectionsQuery.removeDayAllTimeArgs();
                for(DayTimeArg dta : cDayTimeArgsObservableList)
                    cSectionsQuery.addDayTimeArg(dta);
            }
        };  
    private final ChangeListener<AvailabilityArg> AVAILABILITY_SELECTION_CHANGE_LISTENER = new ChangeListener<AvailabilityArg>() { 
        @Override
        public void changed(ObservableValue<? extends AvailabilityArg> observable, AvailabilityArg oldValue, AvailabilityArg newValue) {
            cSectionsQuery.setAvailability(newValue);
        }
    };    
    private final ListChangeListener<Section> SECTION_SELECTION_CHANGE_LISTENER = new ListChangeListener<Section>(){
        @Override
        public void onChanged(ListChangeListener.Change<? extends Section> c) {
            cSectionsQuery.removeAllSections();
            if(lstSections.getSelectionModel().isSelected(0)){
                lstSections.getSelectionModel().getSelectedItems().removeListener(this);
                lstSections.getSelectionModel().clearAndSelect(0);
                lstSections.getSelectionModel().getSelectedItems().addListener(this);
            } else {
                for(Section s : lstSections.getSelectionModel().getSelectedItems()){
                    cSectionsQuery.addSection(s);
                }
            }
        }
    };
    private final ListChangeListener<Session> SESSION_SELECTION_CHANGE_LISTENER = new ListChangeListener<Session>(){
        @Override
        public void onChanged(ListChangeListener.Change<? extends Session> c) {
            cSectionsQuery.removeAllSessions();
            if(lstSessions.getSelectionModel().isSelected(0)){
                lstSessions.getSelectionModel().getSelectedItems().removeListener(this);
                lstSessions.getSelectionModel().clearAndSelect(0);
                lstSessions.getSelectionModel().getSelectedItems().addListener(this);
            } else {
                for(Session s : lstSessions.getSelectionModel().getSelectedItems()){
                    cSectionsQuery.addSession(s);
                }
            }
        }
    };   
    private final ListChangeListener<InstructionalMethod> METHODS_SELECTION_CHANGE_LISTENER = new ListChangeListener<InstructionalMethod>(){
        @Override
        public void onChanged(ListChangeListener.Change<? extends InstructionalMethod> c) {
            cSectionsQuery.removeAllInstructionalMethods();
            if(lstInstructionalMethods.getSelectionModel().isSelected(0)){
                lstInstructionalMethods.getSelectionModel().getSelectedItems().removeListener(this);
                lstInstructionalMethods.getSelectionModel().clearAndSelect(0);
                lstInstructionalMethods.getSelectionModel().getSelectedItems().addListener(this);
            } else {
                for(InstructionalMethod s : lstInstructionalMethods.getSelectionModel().getSelectedItems()){
                    cSectionsQuery.addInstructionalMethod(s);
                }
            }
        }
    };  
    private final ListChangeListener<Instructor> INSTRUCTOR_SELECTION_CHANGE_LISTENER = new ListChangeListener<Instructor>(){
        @Override
        public void onChanged(ListChangeListener.Change<? extends Instructor> c) {
            cSectionsQuery.removeAllInstructors();
            if(lstInstructors.getSelectionModel().isSelected(0)){
                lstInstructors.getSelectionModel().getSelectedItems().removeListener(this);
                lstInstructors.getSelectionModel().clearAndSelect(0);
                lstInstructors.getSelectionModel().getSelectedItems().addListener(this);
            } else {
                for(Instructor s : lstInstructors.getSelectionModel().getSelectedItems()){
                    cSectionsQuery.addInstructor(s);
                }
            }
        }
    }; 
    private final EventHandler<ActionEvent> REMOVE_BUTTON_HANDLER = new EventHandler<ActionEvent>() {            
        @Override
        public void handle(ActionEvent event) {    
            for(CourseRemoveRequestListener listener : cRemoveRequestListeners)
                listener.removeRequested(CourseInput.this);
        }
    };  
    private final EventHandler<ActionEvent> INSERT_BUTTON_HANDLER = new EventHandler<ActionEvent>() {            
        @Override
        public void handle(ActionEvent event) {    
            for(CourseInsertRequestListener listener : cInsertRequestListeners)
                listener.insertRequested(CourseInput.this);
        }
    };
    
    //Used in conjunction with SUBJECTS_ON_SHOW, to implement the auto-complete functionality of Subjects combo box
    private final EventHandler< KeyEvent > SUBJECTS_KEY_RELEASE = new EventHandler< KeyEvent > (){
        @Override
        public void handle( KeyEvent event ) {
            String key = event.getText().toUpperCase();
            if (key.matches("[A-Z]")) {
                final int selectedIndex = Math.max(0,cmbSubjects.getSelectionModel().getSelectedIndex());
                List<Subject> sourceItems = cmbSubjects.getItems();

                for (int i = selectedIndex + 1; i < sourceItems.size(); i++) {
                    if(sourceItems.get(i) != null && sourceItems.get(i).subjectAbbr().startsWith(key)){
                        cmbSubjects.getSelectionModel().select(sourceItems.get(i));
                        cmbSubjects.setValue(sourceItems.get(i));//NOTE: This is critical! In a comboBox, changing the selection value DOES NOT change the value!!
                        cmbSubjects.show();
                        return;
                    }
                }   
                for (int i = 0; i < selectedIndex; i++) {
                    if(sourceItems.get(i) != null && sourceItems.get(i).subjectAbbr().startsWith(key)){
                        cmbSubjects.getSelectionModel().select(sourceItems.get(i));
                        cmbSubjects.show();
                        return;
                    }
                } 
            }
        }
    };
    private final EventHandler<Event> SUBJECTS_ON_SHOW = new EventHandler<Event>() {
            @Override
            public void handle(Event event) {
                // Work around; need to check for proper solution.
                final int selectedIndex = cmbSubjects.getSelectionModel().getSelectedIndex();
                final ComboBoxListViewSkin<?> skin = (ComboBoxListViewSkin<?>) cmbSubjects.getSkin();
                skin.getListView().scrollTo(selectedIndex < 0 ? 0 : selectedIndex);
            }
        };
    /**
     * ******************************************************************
     * **************************Constructor ************
     * ******************************************************************
     * @param pTermValueProperty The .valueProperty() of the Terms combo box (NOT the selected item property!)
     * @param pCampusObservableList The .getSelectionModel().getSelectedItems() of the Campuses list view, which is a read only ObservableList
     * @param pDayTimeArgsObservableList The .dayTimeArgs() of the DayVBox container, which is a read only ObservableList of the selected DayTimeArgs
     * @param pBindWidth This CourseInput will bind it's preferred width to the provided ObservableValue.
     */
    public CourseInput(ObjectProperty<Term> pTermValueProperty, ObservableList<Campus> pCampusObservableList, ObservableList<DayTimeArg> pDayTimeArgsObservableList, ObservableValue<? extends Number> pBindWidth){
        
        if(pTermValueProperty == null || pCampusObservableList  == null || pDayTimeArgsObservableList == null)
            throw new IllegalArgumentException("Null argument.");
        
        super.prefWidthProperty().bind(pBindWidth);
        
        cTermValueProperty = pTermValueProperty;
        cCampusObservableList = pCampusObservableList;
        cDayTimeArgsObservableList = pDayTimeArgsObservableList;
        
        /**
         * Initialize ScetionsQuery
         */
        cSectionsQuery = new SectionsQuery(cTermValueProperty.getValue(), null);
        cSectionsQuery.addObserver(this);
        
        for(Campus c : cCampusObservableList){
            if(c!=null) 
                cSectionsQuery.addCampus(c);            
        }

        
        for(DayTimeArg dta : cDayTimeArgsObservableList)
            cSectionsQuery.addDayTimeArg(dta);
        /**
         * Add Listeners to passed in arguments
         */
        cTermValueProperty.addListener(this.TERM_VALUE_CHANGE_LISTENER);
        cCampusObservableList.addListener(this.CAMPUS_SELECTION_CHANGE_LISTENER);
        cDayTimeArgsObservableList.addListener(this.DAY_TIME_ARGS_LIST_CHANGE_LISTENER); 
        
        /*****************************************
         * ***** Build Subjects Combo Box
         * *****************************************
         */
        cmbSubjects = new ComboBox<>(FXCollections.observableArrayList());
        cmbSubjects.setPromptText("* Select Subject");
        cmbSubjects.setVisibleRowCount(15);
        cmbSubjects.valueProperty().addListener(this.SUBJECT_VALUE_CHANGE_LISTENER);
        cmbSubjects.setOnKeyReleased(SUBJECTS_KEY_RELEASE);
        cmbSubjects.setOnShown(SUBJECTS_ON_SHOW);
        cmbSubjects.setButtonCell(new SubjectListCell());
        cmbSubjects.setCellFactory(SUBJECT_CELL_FACT);
        //***************************
        cmbSubjects.getItems().addAll(Subjects.getAll(Subjects.PK_ASC));
            
        /*****************************************
         * ***** Build Course Text Field 
         * *****************************************
         */
        txtCourse = new TextField();
        txtCourse.setPromptText("* Enter Course #");
        txtCourse.textProperty().addListener(COURSE_VALUE_CHANGE_LISTENER);
        txtCourse.setDisable(true);//disabled untill a subject is selected
        /*****************************************
         * ***** Build Availability Combo Box 
         * *****************************************
         */        
        cmbAvailabilityArgs = new ComboBox<>(FXCollections.observableArrayList());
        cmbAvailabilityArgs.valueProperty().addListener(this.AVAILABILITY_SELECTION_CHANGE_LISTENER);
        cmbAvailabilityArgs.getItems().addAll(AvailabilityArg.values());
        cmbAvailabilityArgs.setValue(AvailabilityArg.OPEN_SEATS);
        /*****************************************
         * ***** Build Sections List
         * *****************************************
         */            
        lstSections = new ListView<>(FXCollections.observableArrayList());
        lstSections.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        lstSections.getSelectionModel().getSelectedItems().addListener(this.SECTION_SELECTION_CHANGE_LISTENER);
        lstSections.setCellFactory(SECTION_CELL_FACT);
        /*****************************************
         * ***** Build Sessions List
         * *****************************************
         */    
        lstSessions = new ListView<>(FXCollections.observableArrayList());
        lstSessions.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        lstSessions.getSelectionModel().getSelectedItems().addListener(this.SESSION_SELECTION_CHANGE_LISTENER);
        lstSessions.setCellFactory(SESSION_CELL_FACT);
        /*****************************************
         * ***** Build Instructional Method List
         * *****************************************
         */            
        lstInstructionalMethods = new ListView<>(FXCollections.observableArrayList());
        lstInstructionalMethods.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        lstInstructionalMethods.getSelectionModel().getSelectedItems().addListener(METHODS_SELECTION_CHANGE_LISTENER);
        lstInstructionalMethods.setCellFactory(INSTRUCTIONAL_METHOD_CELL_FACT);
        /*****************************************
         * ***** Build Instructors List
         * *****************************************
         */          
        lstInstructors = new ListView<>(FXCollections.observableArrayList());
        lstInstructors.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        lstInstructors.getSelectionModel().getSelectedItems().addListener(this.INSTRUCTOR_SELECTION_CHANGE_LISTENER);
        lstInstructors.setCellFactory(INSTRUCTOR_CELL_FACT);
        /*****************************************
         * ***** Remove Button
         * *****************************************
         */          
        btnRemove = new Button("x");
        btnRemove.getStyleClass().add("button-remove");
        btnRemove.setOnAction(REMOVE_BUTTON_HANDLER); 
        /*****************************************
         * ***** Insert Button
         * *****************************************
         */          
        btnInsert = new Button("+");
        btnInsert.getStyleClass().add("button-action");
        btnInsert.setOnAction(INSERT_BUTTON_HANDLER); 
        
        /*****************************************
         * ***** Matching Sections Text
         * *****************************************
         */      
        txtSectionsCount = new Text();
        txtSectionsCount.setText("Matching Sections : 0");
        txtSectionsCount.setFill(Color.RED);      
        /**
         * *********Layout
         *  HBox (CourseInput class) (hbox-bordered)
         *      VBox vbxLeft (vbox-course-left_inputs)
         *          txtSectionsCount
         *          cmbSubjects
         *          txtCourse
         *          cmbAvailabilityArgs
         *      lstSections
         *      lstSessions
         *      lstInstructionalMethods
         *      lstInstructors
         *      VBox vbxButtons (vbox-course-buttons-container)
         *          btnRemove
         *          btnInsert
         */  
        this.lstInstructionalMethods.setMaxHeight(LIST_MAX_HEIGHT);
        this.lstInstructors.setMaxHeight(LIST_MAX_HEIGHT);
        this.lstSections.setMaxHeight(LIST_MAX_HEIGHT);
        this.lstSessions.setMaxHeight(LIST_MAX_HEIGHT);
        this.cmbSubjects.setMaxWidth(220);
//        this.cmbSubjects.setMaxWidth(LIST_MAX_WIDTH);
//        this.lstInstructionalMethods.setMaxWidth(LIST_MAX_WIDTH);
//        this.lstInstructors.setMaxWidth(LIST_MAX_WIDTH);
//        this.lstSections.setMaxWidth(LIST_MAX_WIDTH);
//        this.lstSessions.setMaxWidth(LIST_MAX_WIDTH);

        cmbAvailabilityArgs.setMaxWidth(Double.MAX_VALUE);
        VBox vbxLeft = new VBox(txtSectionsCount, cmbSubjects, txtCourse, cmbAvailabilityArgs);
        vbxLeft.getStyleClass().add("vbox-course-left_inputs");
        btnRemove.setMaxWidth(Double.MAX_VALUE);
        btnInsert.setMaxWidth(Double.MAX_VALUE); 
        VBox vbxButtons = new VBox(btnRemove,btnInsert);
        vbxButtons.getStyleClass().add("vbox-course-buttons-container");
        super.getChildren().addAll(vbxLeft, lstSections, lstSessions, lstInstructionalMethods, lstInstructors, vbxButtons);
        super.getStyleClass().add("hbox-bordered");

    }
    
    /**
     * ******************************************************************
     * **************************Public Methods**************************
     * ******************************************************************
     */      
    //This method provides WAY to much access, but time is running out
    //THIS MUST NOT be modified by caller or anything outside this class!
    /**
     * Returns the SectionsQuery object of this CourseInput.
     * This object should not be modifies outside of this class.
     * @return the sectionsQuery of object of this CourseInput. DON NOT MODIFY!
     */
    public SectionsQuery sectionsQuery(){return cSectionsQuery;}

    public void kill(){
        cSectionsQuery.close();
        cTermValueProperty.removeListener(this.TERM_VALUE_CHANGE_LISTENER);
        cCampusObservableList.removeListener(this.CAMPUS_SELECTION_CHANGE_LISTENER);
        cDayTimeArgsObservableList.removeListener(this.DAY_TIME_ARGS_LIST_CHANGE_LISTENER); 
        cmbSubjects.valueProperty().removeListener(this.SUBJECT_VALUE_CHANGE_LISTENER);        
        txtCourse.textProperty().removeListener(COURSE_VALUE_CHANGE_LISTENER);        
        cmbAvailabilityArgs.valueProperty().removeListener(this.AVAILABILITY_SELECTION_CHANGE_LISTENER);        
        lstSections.getSelectionModel().getSelectedItems().removeListener(this.SECTION_SELECTION_CHANGE_LISTENER);        
        lstSessions.getSelectionModel().getSelectedItems().removeListener(this.SESSION_SELECTION_CHANGE_LISTENER);        
        lstInstructionalMethods.getSelectionModel().getSelectedItems().removeListener(METHODS_SELECTION_CHANGE_LISTENER);        
        lstInstructors.getSelectionModel().getSelectedItems().removeListener(this.INSTRUCTOR_SELECTION_CHANGE_LISTENER);
    }    
    
    public void addCourseRemoveRequestListener(CourseRemoveRequestListener pListener){
        if(pListener==null)
            throw new IllegalArgumentException("Null pListener argument.");    
        cRemoveRequestListeners.add(pListener);
    }
    public void removeCourseRemoveRequestListener(CourseRemoveRequestListener pListener){   
        cRemoveRequestListeners.remove(pListener);
    }
    public void addCourseInsertRequestListener(CourseInsertRequestListener pListener){
        if(pListener==null)
            throw new IllegalArgumentException("Null pListener argument.");    
        cInsertRequestListeners.add(pListener);
    }
    public void removeCourseInsertRequestListener(CourseInsertRequestListener pListener){   
        cInsertRequestListeners.remove(pListener);
    }
    @Override
    public void resultsChanged(SectionsQuery pSQ) {
        
        txtSectionsCount.setText("Matching Sections : " + pSQ.resultsSize());
        if(pSQ.resultsSize()>0)
            txtSectionsCount.setFill(Color.GREEN);
        else
            txtSectionsCount.setFill(Color.RED);
    }
    

    /**
     * ******************************************************************
     * ************ Shared Helper Methods ******************************
     * ******************************************************************
     */
//    private boolean silentImport(){
//        try {
//            Importer.loadSections(cSectionsQuery.term(), cmbSubjects.getValue(), txtCourse.getText());
//            return true;   
//        } catch (HTMLFormatException | IOException | NoDataFoundException ex) {
//            return false;  
//        }
//    }
    /**
     * Ensures that the Sections,Sessions,Instructional Methods, and Instructors Data Sources are all correct, 
     * based on the current term and course values of the SectionsQuery.
     */
    private void setVariableSouces(){

        cSectionsQuery.removeAllSections();
        cSectionsQuery.removeAllSessions();
        cSectionsQuery.removeAllInstructionalMethods();
        cSectionsQuery.removeAllInstructors();
        
        lstInstructors.getSelectionModel().getSelectedItems().removeListener(this.INSTRUCTOR_SELECTION_CHANGE_LISTENER);
        lstInstructionalMethods.getSelectionModel().getSelectedItems().removeListener(this.METHODS_SELECTION_CHANGE_LISTENER);
        lstSessions.getSelectionModel().getSelectedItems().removeListener(this.SESSION_SELECTION_CHANGE_LISTENER);
        lstSections.getSelectionModel().getSelectedItems().removeListener(this.SECTION_SELECTION_CHANGE_LISTENER);  
        
        lstSections.getItems().clear();
        lstSessions.getItems().clear();
        lstInstructionalMethods.getItems().clear();
        lstInstructors.getItems().clear();

        lstSections.getItems().add(0, null);
        lstSessions.getItems().add(0, null);
        lstInstructionalMethods.getItems().add(0, null);
        lstInstructors.getItems().add(0, null);
        
        if(cSectionsQuery.term()!=null && cSectionsQuery.course()!=null){
            List<Section> sourceSections = Sections.getByCourse1(cSectionsQuery.term(), cSectionsQuery.course(), Sections.SEC_NUM_ASC);
            lstSections.getItems().addAll(sourceSections);
            this.lstSessions.getItems().addAll(Sections.getDistinctSessions(sourceSections));
            this.lstInstructionalMethods.getItems().addAll(Sections.getDistinctMethods(sourceSections));
            this.lstInstructors.getItems().addAll(Sections.getDistinctInstructors(sourceSections));
        }

        lstSections.getSelectionModel().clearAndSelect(0);
        lstSessions.getSelectionModel().clearAndSelect(0);
        lstInstructionalMethods.getSelectionModel().clearAndSelect(0);
        lstInstructors.getSelectionModel().clearAndSelect(0);
        
        lstInstructors.getSelectionModel().getSelectedItems().addListener(this.INSTRUCTOR_SELECTION_CHANGE_LISTENER);
        lstInstructionalMethods.getSelectionModel().getSelectedItems().addListener(this.METHODS_SELECTION_CHANGE_LISTENER);
        lstSessions.getSelectionModel().getSelectedItems().addListener(this.SESSION_SELECTION_CHANGE_LISTENER);
        lstSections.getSelectionModel().getSelectedItems().addListener(this.SECTION_SELECTION_CHANGE_LISTENER);  
    }
  

    /**
     * ******************************************************************
     * **************************Custom Cell Factories************
     * ******************************************************************
     */   
    private final Callback<ListView<InstructionalMethod>, ListCell<InstructionalMethod>> INSTRUCTIONAL_METHOD_CELL_FACT = (ListView<InstructionalMethod> param) -> new InstructionalMethodListCell();    
    private final Callback<ListView<Instructor>, ListCell<Instructor>> INSTRUCTOR_CELL_FACT = new Callback<ListView<Instructor>, ListCell<Instructor>>() {

        public ListCell<Instructor> call(ListView<Instructor> param) {
            return new InstructorListCell();
        }
    };
    private final Callback<ListView<Session>, ListCell<Session>> SESSION_CELL_FACT = (ListView<Session> param) -> new SessionListCell();
    private final Callback<ListView<Section>, ListCell<Section>> SECTION_CELL_FACT = (ListView<Section> param) -> new SectionListCell();
    private final Callback<ListView<Subject>, ListCell<Subject>> SUBJECT_CELL_FACT = (ListView<Subject> param) -> new SubjectListCell();

    /**
     * ******************************************************************
     * **************************Custom Cells ***************************
     * ******************************************************************
     */       
    
    class SubjectListCell extends ListCell<Subject> {
        @Override protected void updateItem(Subject pSubject, boolean empty) {
            super.updateItem(pSubject, empty);
            if (empty || pSubject == null) {
                setText(null);
            } else {
                setText(pSubject.subjectAbbr() + " - " + pSubject.subjectName());
            }
        }
    }
    class SectionListCell extends ListCell<Section> {
        @Override protected void updateItem(Section pSection, boolean empty) {
            super.updateItem(pSection, empty);
            if (empty) {
                setText(null);
            } else if (pSection == null) {
                setText("Sections: Any");
            } else {
                setText(pSection.sectionNumber());
            }
        }
    }
    class SessionListCell extends ListCell<Session> {
        @Override protected void updateItem(Session pSession, boolean empty) {
            super.updateItem(pSession, empty);
            if (empty) {
                setText(null);
            } else if (pSession == null) {
                setText("Sessions: Any");
            } else {
                setText(pSession.sessionName());
            }
        }
    }
    class InstructionalMethodListCell extends ListCell<InstructionalMethod> {
        @Override protected void updateItem(InstructionalMethod pInstructionalMethod, boolean empty) {
            super.updateItem(pInstructionalMethod, empty);
            if (empty) {
                setText(null);
            } else if (pInstructionalMethod == null) {
                setText("Methods: Any");
            } else {
                setText(pInstructionalMethod.toString());
            }
        }
    } 
    class InstructorListCell extends ListCell<Instructor> {
        @Override protected void updateItem(Instructor pInstructor, boolean empty) {
            super.updateItem(pInstructor, empty);
            if (empty) {
                setText(null);
            } else if (pInstructor == null) {
                setText("Instructors: Any");
            } else {
                setText(pInstructor.instructorName());
            }
        }
    }  
    
}
