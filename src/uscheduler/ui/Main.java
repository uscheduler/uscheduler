package uscheduler.ui;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import uscheduler.externaldata.HTMLFormatException;
import uscheduler.externaldata.NoDataFoundException;
import uscheduler.internaldata.Courses;
import uscheduler.internaldata.Sections;
import uscheduler.internaldata.Subjects;
import uscheduler.internaldata.Terms;
import uscheduler.util.Importer;
import uscheduler.util.ScheduleGenerator;
import uscheduler.util.SchedulePrinter;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception{
        FXMLLoader loader = new FXMLLoader(getClass().getResource("uSchedule.fxml"));
        Parent root = loader.load();
        Controller myController = loader.getController();
        primaryStage.setTitle("uScheduler - Kennesaw's SMART Student Scheduler");
        primaryStage.setScene(new Scene(root, 1353, 730));
        primaryStage.show();
    }

    public static void main(String[] args){ launch(args); }
}




/*
public static void main(String[] args) throws IOException, HTMLFormatException, NoDataFoundException {

        Importer.loadTerms();
        Importer.loadSubjectsAndCampuses();

        //Load Fall 2016 sections from files
        Terms.Term t = Terms.get(201601);
        Importer.loadSectionsFromFile(t);
        //Load Summer 2016 sections from files
        //t = Terms.get(201605);
        //Importer.loadSectionsFromFile(t);
        //Load Spring 2016 sections from files
        //t = Terms.get(201601);
        //Importer.loadSectionsFromFile(t);

        //Build course slist to generate schedules
        Subjects.Subject subj;
        Courses.Course crs;
        ArrayList<Sections.Section> sections;
        Sections.Section[][] sectionsArr = new Sections.Section[3][];

        //----------------------------Get CS 4720
        subj = Subjects.get("CS");
        crs = Courses.get(subj, "3501");
        sectionsArr[0] = Sections.getByCourse(t, crs, Sections.SEC_NUM_ASC).toArray(new Sections.Section[0]);
        //----------------------------Get CS 4850
        subj = Subjects.get("CS");
        crs = Courses.get(subj, "4504");
        sectionsArr[1] = Sections.getByCourse(t, crs, Sections.SEC_NUM_ASC).toArray(new Sections.Section[0]);
        //----------------------------Get SWE 3633
        subj = Subjects.get("CS");
        crs = Courses.get(subj, "3301");
        sectionsArr[2] = Sections.getByCourse(t, crs, Sections.SEC_NUM_ASC).toArray(new Sections.Section[0]);
        //----------------------------Get CS 4305
        //subj = Subjects.get("CS");
        //crs = Courses.get(subj, "4305");
        //sectionsArr[3] = Sections.getByCourse(t, crs, Sections.SEC_NUM_ASC).toArray(new Sections.Section[0]);


        //----------------------------Track Time of Generate method
        long startTime = System.currentTimeMillis();
        int numGenerated = ScheduleGenerator.generate(sectionsArr);
        long endTime = System.currentTimeMillis();
        System.out.println("Generate operation took: " + (endTime - startTime) + " millis to generate " + numGenerated + " schedules");

        startTime = System.currentTimeMillis();
        SchedulePrinter.print(new File("C:\\users\\psout\\desktop\\schedules.txt"), false);
        endTime = System.currentTimeMillis();
        System.out.println("Print operation took: " + (endTime - startTime) + " millis");}}*/