/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uscheduler.ui.input;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import javafx.beans.property.ObjectProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.scene.control.Alert;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.VBox;
import uscheduler.internaldata.Campuses;
import uscheduler.internaldata.Terms;
import uscheduler.util.ScheduleGenerator;
import uscheduler.util.SectionsQuery;

/**
 *
 * @author Matt
 */
public class CourseInputContainer extends ScrollPane implements CourseInsertRequestListener, CourseRemoveRequestListener{
    private static final String MAX_COURSE_MSG = "The maximun allowed number of course inputs have been reached.";
    
    //Pased to each child CourseInput to listen to changes in Term, Campuses, DayTimeArgs, and the requested preffered width of each CourseInput
    private final  ObjectProperty<Terms.Term> cTermValueProperty; 
    private final  ObservableList<Campuses.Campus> cCampusObservableList;
    private final  ObservableList<SectionsQuery.DayTimeArg> cDayTimeArgsObservableList;
    private final ObservableValue<? extends Number> cBindWidth;
    
    //Containe for the CourseInputs
    private final VBox cCoursesVBox = new VBox();
    
    //Collection of SectionQuery objects that will be needed by the CoursesTab when generating schedules
    private final HashSet<SectionsQuery> cSectionsQueries = new HashSet<>();
    
    public CourseInputContainer(ObjectProperty<Terms.Term> pTermValueProperty, ObservableList<Campuses.Campus> pCampusObservableList, ObservableList<SectionsQuery.DayTimeArg> pDayTimeArgsObservableList, ObservableValue<? extends Number> pBindWidth){
        
        if(pTermValueProperty == null || pCampusObservableList  == null || pDayTimeArgsObservableList == null)
            throw new IllegalArgumentException("Null argument.");
        
        cTermValueProperty = pTermValueProperty;
        cCampusObservableList = pCampusObservableList;
        cDayTimeArgsObservableList = pDayTimeArgsObservableList;
        cBindWidth = pBindWidth;
        
        //Add 1 CourseInput to the VBox by default
        CourseInput ci = new CourseInput(cTermValueProperty, cCampusObservableList, cDayTimeArgsObservableList, cBindWidth);
        ci.addCourseInsertRequestListener(this);
        ci.addCourseRemoveRequestListener(this);
        cCoursesVBox.getChildren().add(ci);
        cSectionsQueries.add(ci.sectionsQuery());
        /**
         * *********Layout
         *  ScrollPane (this)
         *      VBox cCoursesVBox (children vertically spaced at 30px)
         *          CourseInput
         *          CourseInput
         *          ....
         *          CourseInput
         */   
        cCoursesVBox.setSpacing(10.0);
        //cCoursesVBox.setAlignment(Pos.TOP_CENTER);
        super.setFitToWidth(false);//true means cCoursesVBox will be resized to always be as wide as the containing ScrollPane
        //super.setFitToHeight(true);
        super.setContent(cCoursesVBox);
        super.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        super.getStyleClass().add("scroll-pane-course-inputs");
        
    }

    @Override
    public void insertRequested(CourseInput pCourseInput) {
        if (cCoursesVBox.getChildrenUnmodifiable().size() >= ScheduleGenerator.COURSE_MAX){
            Alert alert = new Alert(Alert.AlertType.INFORMATION, MAX_COURSE_MSG);
            alert.setTitle("Max Input Reached");
            alert.showAndWait();    
            return;
        }
        int idexOf = cCoursesVBox.getChildrenUnmodifiable().indexOf(pCourseInput);
        if(idexOf >= 0){
            CourseInput ci = new CourseInput(cTermValueProperty, cCampusObservableList, cDayTimeArgsObservableList, cBindWidth);
            ci.addCourseInsertRequestListener(this);
            ci.addCourseRemoveRequestListener(this);
            cCoursesVBox.getChildren().add(idexOf + 1, ci);
            cSectionsQueries.add(ci.sectionsQuery());
        } else {
            throw new IllegalArgumentException("what the fuck!");
        }
    }

    @Override
    public void removeRequested(CourseInput pCourseInput) {
        if(cCoursesVBox.getChildrenUnmodifiable().size()>1){
            pCourseInput.removeCourseInsertRequestListener(this);
            pCourseInput.removeCourseRemoveRequestListener(this);
            cSectionsQueries.remove(pCourseInput.sectionsQuery());
            pCourseInput.kill();
            cCoursesVBox.getChildren().remove(pCourseInput);            
        }
    }
    public Set<SectionsQuery> sectionsQueries(){
        return Collections.unmodifiableSet(cSectionsQueries);
    }
}
