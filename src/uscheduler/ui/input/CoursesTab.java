/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uscheduler.ui.input;

import java.awt.Desktop;
import java.net.URI;
import java.util.HashSet;
import java.util.Set;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.Tab;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.util.Callback;
import uscheduler.internaldata.Campuses;
import uscheduler.internaldata.Campuses.Campus;
import uscheduler.internaldata.Courses.Course;
import uscheduler.internaldata.Schedules;
import uscheduler.internaldata.Sections.Section;
import uscheduler.internaldata.Terms;
import uscheduler.internaldata.Terms.Term;
import uscheduler.util.ScheduleGenerator;
import uscheduler.util.SectionsQuery;

/**
 * The Tab to house the entire view of Input. 
 * This Tab is the top level container for the view of Input.
 * 
 * @author Matt Bush
 */
public class CoursesTab extends Tab{
    //Controls
    private final ComboBox<Term> cmbTerms;
    private final ListView<Campus> lstCampuses;
    private final Button btnGenerate;
    private final Button btnHelp;
    private final DayInputContainer vbxDayInputContainer;
    private final CourseInputContainer spCourseInputContainer;

    //Misc
    
    /*
     *************************************************************
     * **************************** Listeners and Handlers
     * **************************************************************
     */
    private final EventHandler<ActionEvent> HELP_ACTION = new EventHandler<ActionEvent>() {
        @Override
        public void handle(ActionEvent event) {   
            Desktop desktop = Desktop.isDesktopSupported() ? Desktop.getDesktop() : null;
            if (desktop != null && desktop.isSupported(Desktop.Action.BROWSE)) {
                try {
                    desktop.browse(new URI("http://ksuscheduler.com/app/uScheduler-UserGuide.pdf"));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    };
    
    private final EventHandler<ActionEvent> GENERATE_BUTTON_HANDLER = new EventHandler<ActionEvent>() {            
        @Override
        public void handle(ActionEvent event) {   
            /**
             * 1) Test for less than 2 courses
             * 2) Test for 2 of the same course
             * 3) Test for 1 course parameter having 0 matching sections
             * 5) execute, display how many were generated and tell to click on schedule tab to see results
             */
            HashSet<Course> courses = new HashSet<>();
            Set<SectionsQuery> sectionQueries = spCourseInputContainer.sectionsQueries();
            
            if(sectionQueries.size()<2){
                Alert alert = new Alert(Alert.AlertType.INFORMATION, "At least 2 courses must be specified in order to generate schedules.");
                alert.setTitle("Too Few Courses");
                alert.showAndWait();
                return;                
            }
            for(SectionsQuery sq : sectionQueries){
                if (sq.resultsSize()==0){
                    Alert alert = new Alert(Alert.AlertType.INFORMATION, "Each specified course parameter must have at least 1 matching section.");
                    alert.setTitle("Course Parameter With 0 Matches");
                    alert.showAndWait();
                    return;
                }
            }
            for(SectionsQuery sq : sectionQueries){
                if (courses.contains(sq.course())){
                    Alert alert = new Alert(Alert.AlertType.INFORMATION, "The course parameters provided must consist of unique courses.");
                    alert.setTitle("Duplicate Course");
                    alert.showAndWait();
                    return;
                }
                courses.add(sq.course());
            }

            int currentPotential = 1;
            for(SectionsQuery sq : sectionQueries)
                currentPotential = currentPotential * sq.resultsSize();
            
//            if (currentPotential > MAX_GENERATE_POTENTIAL){
//                String msg = "The current course parameters could potentially lead to " + String.format("%,d", currentPotential) + " generated schedules." + "\n" + "\n" +
//                        "Please specify more restrictions on the specified courses so that the potential is less than " + String.format("%,d", MAX_GENERATE_POTENTIAL) + ".";
//                Alert alert = new Alert(Alert.AlertType.INFORMATION, msg);
//                alert.setTitle("Potential Number of Schedules Too High");
//                alert.showAndWait();
//                return;
//            }
            //Build Array to pass to schedule generator
            Section[][] sectionsArr = new Section[sectionQueries.size()][];
            int i = 0;
            for(SectionsQuery sq : sectionQueries){
                sectionsArr[i] = sq.results2();
                i++;
            }

            int numGenerated = ScheduleGenerator.generate(sectionsArr);
            String msg;
            if(numGenerated > Schedules.MAX_SCHEDULE_COUNT){
                msg = String.format("%,d", numGenerated) + " possible schedules exist for the specified course parameters. UScheduler analyzed all of them and kept the best " +
                        String.format("%,d", Schedules.MAX_SCHEDULE_COUNT) + " schedules.\n\nClick the Schedules tab to see the results.";
                            
            } else {
                msg = String.format("%,d", numGenerated) + " schedules generated.\n\nClick the Schedules tab to see the results.";
            }
            Alert alert = new Alert(Alert.AlertType.INFORMATION, msg);
            alert.setTitle("Schedules Generated!");
            alert.showAndWait();
 
        }
    };
    private final ChangeListener<Term> TERM_CHANGE_LISTENER = new ChangeListener<Term>() {
        @Override
        public void changed(ObservableValue<? extends Term> observable, Term oldValue, Term newValue) {
            spCourseInputContainer.setDisable(newValue==null);
        }
    };
    private final ListChangeListener<Campus> CAMPUS_SELECTION_CHANGE_LISTENER = new ListChangeListener<Campus>(){
        @Override
        public void onChanged(ListChangeListener.Change<? extends Campus> c) {
            if(lstCampuses.getSelectionModel().isSelected(0)){
                lstCampuses.getSelectionModel().getSelectedItems().removeListener(this);
                lstCampuses.getSelectionModel().clearAndSelect(0);
                lstCampuses.getSelectionModel().getSelectedItems().addListener(this);
            }
        }
    };

    
    /*
     *************************************************************
     * **************************** Listeners and Handlers
     * **************************************************************
     */    
    public CoursesTab(){
        /*****************************************
         * ***** Build Terms Combo Box
         * *****************************************
         */
        cmbTerms = new ComboBox<Term>(FXCollections.observableArrayList());
        cmbTerms.setVisibleRowCount(3);
        cmbTerms.setEditable(false);
        cmbTerms.setPromptText("* Select Term");
        

        //***************************
        cmbTerms.setButtonCell(new TermListCell());
        cmbTerms.setCellFactory(TERM_CELL_FACT);
        cmbTerms.getItems().addAll(Terms.getAll(Terms.PK_DESC));
        cmbTerms.valueProperty().addListener(this.TERM_CHANGE_LISTENER);
        
        /*****************************************
         * ***** Build Campuses List
         * *****************************************
         */            
        lstCampuses = new ListView<Campus>(FXCollections.observableArrayList());
        lstCampuses.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        lstCampuses.getSelectionModel().getSelectedItems().addListener(this.CAMPUS_SELECTION_CHANGE_LISTENER);
        lstCampuses.setCellFactory(this.CAMPUS_CELL_FACT);
        lstCampuses.getItems().add(null);
        lstCampuses.getItems().addAll(Campuses.getAll(Campuses.PK_ASC));

        /*****************************************
         * ***** Generate Button
         * *****************************************
         */          
        btnGenerate = new Button("Generate");
        btnGenerate.getStyleClass().add("button-action");
        btnGenerate.setOnAction(GENERATE_BUTTON_HANDLER); 
        
        //Help Button
        btnHelp = new Button("?");
        btnHelp.getStyleClass().add("button-help");
        btnHelp.setOnAction(HELP_ACTION); 
        
        /*
         ****************************************
         * ***** Day Input Container and Course Input Container
         * *****************************************
         */    


        
        /** 
         *  Tab (CoursesTabe class)
         *      BorderPane bordrPane
         *          AnchorPane anchorPane (AnchorPane.setRightAnchor(hbxButtonContainer, 5.0))
         *              HBox hbxButtonContainer (.setPadding(new Insets(10.0, 0, 10.0, 0)), .setSpacing(1))
         *                  btnGenerate
         *                  btnHelp
         *          GiidPane grid: 1 column, 3 rows (buttons, global inputs, course inputs scroll pane)
         *              HBox hbxMainTop (new Insets(10.0, 10.0, 10.0, 10.0));hbxMainTop.setAlignment(Pos.TOP_CENTER)
         *                  cmbTerms
         *                  lstCampuses
         *                  vbxDayInputContainer ("-fx-border-width: 1px;-fx-border-color: gray;");
         *              ScrolPane CourseInputContainer (
         *                          NOTE: By default, a SrollPane will want to be as tall as possible. 
         *                          Its width by default is always as wide as the width of it children and it does not resize its children 
         *                              (resizing children to fit scroll pane area defeats the purpose of scroll pane) 
         *                          When in a GridPane, the height of ScrollPane will only be as tall as it needs to be to contain its children. 
         */
        
        
        lstCampuses.setPrefHeight(160);
        HBox hbxButtonContainer = new HBox(btnGenerate, btnHelp);
        hbxButtonContainer.getStyleClass().add("hbox-main-button-container");
        AnchorPane anchorPane = new AnchorPane(hbxButtonContainer);
        AnchorPane.setRightAnchor(hbxButtonContainer, 5.0);

        HBox hbxMainTop = new HBox();
        vbxDayInputContainer = new DayInputContainer();
        vbxDayInputContainer.getStyleClass().add("hbox-bordered");
//        lstCampuses.minHeightProperty().bind(vbxDayInputContainer.heightProperty());
 //       lstCampuses.maxHeightProperty().bind(vbxDayInputContainer.heightProperty());
        
        spCourseInputContainer = new CourseInputContainer(cmbTerms.valueProperty(),
                                                        lstCampuses.getSelectionModel().getSelectedItems(),
                                                        vbxDayInputContainer.dayTimeArgs(),
                                                        hbxMainTop.widthProperty().subtract(26)); //This is the value that each CourseInput will bind its width property to. +30 for V scroll bar 
        spCourseInputContainer.setDisable(true);//disabled untill a term is selected
        
        hbxMainTop.getChildren().addAll(cmbTerms, lstCampuses, vbxDayInputContainer);
        hbxMainTop.getStyleClass().add("hbox-bordered");

        GridPane grid = new GridPane();
        grid.add(hbxMainTop, 0, 0);
        grid.add(spCourseInputContainer, 0, 1);
        grid.setGridLinesVisible(true);
        //NOTE: A grid by default takes up the entire width of its container, which in this case is a BorderPane. 
        //Note that the width of the grid can be much wider than the width needed for its cells.
        //Set the backround of this grid to some color to see that it is the entire screen.
        //The gridlines are NOT an indicator of the width of the grid, only the width of the columns of the grid.
        //This method centers the grid's content (its columns/cells) in the center, which is exactly wahat is needed.
        grid.setAlignment(Pos.TOP_CENTER);
        
        
        BorderPane bordrPane = new BorderPane();
        bordrPane.setTop(anchorPane);
        bordrPane.setCenter(grid);
        
        super.setText("Course Parameters");
        super.setContent(bordrPane);
    }
    private final Callback<ListView<Campus>, ListCell<Campus>> CAMPUS_CELL_FACT = new Callback<ListView<Campus>, ListCell<Campus>>() {

        public ListCell<Campus> call(ListView<Campus> param) {
            return new CoursesTab.CampusListCell();
        }
    };
    private final Callback<ListView<Term>, ListCell<Term>> TERM_CELL_FACT = new Callback<ListView<Term>, ListCell<Term>>() {

        public ListCell<Term> call(ListView<Term> param) {
            return new TermListCell();
        }
    };
    class CampusListCell extends ListCell<Campus> {
            @Override protected void updateItem(Campus pCampus, boolean empty) {
                super.updateItem(pCampus, empty);
                if (empty) {
                    setText(null);
                } else if (pCampus == null) {
                    setText("Campuses: Any");
                } else {
                    setText(pCampus.campusName());
                }
            }
     }
    class TermListCell extends ListCell<Term> {
        @Override protected void updateItem(Term pTerm, boolean empty) {
            super.updateItem(pTerm, empty);
            if (empty || pTerm == null) {
                setText(null);
            } else {
                setText(pTerm.termName());
            }
        }
    }
}
