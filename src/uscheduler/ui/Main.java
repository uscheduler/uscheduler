package uscheduler.ui;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import uscheduler.externaldata.HTMLFormatException;
import uscheduler.externaldata.NoDataFoundException;
import uscheduler.global.InstructionalMethod;
import uscheduler.internaldata.*;
import uscheduler.util.Importer;
import uscheduler.util.ScheduleGenerator;
import uscheduler.util.SchedulePrinter;
import uscheduler.util.SectionsQuery;
import java.io.File;
import java.io.IOException;
import java.time.DayOfWeek;
import java.util.ArrayList;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception{
        FXMLLoader loader = new FXMLLoader(getClass().getResource("uSchedule.fxml"));
        Parent root = loader.load();
        Controller myController = loader.getController();
        primaryStage.setTitle("uScheduler - Kennesaw's SMART Student Scheduler");
        primaryStage.setScene(new Scene(root, 1355, 735));
        primaryStage.show();
    }

    public static void main(String[] args){ launch(args); }
}
/*
public static void main(String[] args) throws IOException, HTMLFormatException, NoDataFoundException {

        Importer.loadTerms();
        Importer.loadSubjectsAndCampuses();

        //Load Fall 2016 sections from files

    Terms.Term t = Terms.get(201608);
        Importer.loadSectionsFromFile(t);
//        //Load Summer 2016 sections from files
//        t = Terms.get(201605);
//        Importer.loadSectionsFromFile(t);
//         //Load Spring 2016 sections from files
//        t = Terms.get(201601);
//        Importer.loadSectionsFromFile(t);

        //Build course slist to generate schedules
        Subjects.Subject subj;
        Courses.Course crs;
        ArrayList<Sections.Section> sections;
        Sections.Section[][] sectionsArr = new Sections.Section[4][];
        SectionsQuery.DayTimeArg thurDta = new SectionsQuery.DayTimeArg(DayOfWeek.THURSDAY);
        SectionsQuery.DayTimeArg tuesDta = new SectionsQuery.DayTimeArg(DayOfWeek.TUESDAY);

        //----------------------------Get ENGL 1101
        subj = Subjects.get("ENGL");
        crs = Courses.get(subj, "1101");
        SectionsQuery sq1 = new SectionsQuery(t,crs);
        sq1.setAvailability(SectionsQuery.AvailabilityArg.OPEN_SEATS);
        sq1.addInstructionalMethod(InstructionalMethod.CLASSROOM);
        sq1.addDayTimeArg(thurDta);
        sq1.addDayTimeArg(tuesDta);
        sectionsArr[0] = (Sections.Section[]) sq1.results().toArray(new Sections.Section[sq1.resultsSize()]);

        //----------------------------Get KSU 1101
        subj = Subjects.get("KSU");
        crs = Courses.get(subj, "1101");
        SectionsQuery sq2 = new SectionsQuery(t,crs);
        sq2.setAvailability(SectionsQuery.AvailabilityArg.OPEN_SEATS);
        sq2.addInstructionalMethod(InstructionalMethod.CLASSROOM);
        sq2.addDayTimeArg(thurDta);
        sq2.addDayTimeArg(tuesDta);
        sectionsArr[1] = sq2.results().toArray(new Sections.Section[sq2.resultsSize()]);
        //----------------------------Get WELL 1000
        subj = Subjects.get("WELL");
        crs = Courses.get(subj, "1000");
        SectionsQuery sq3 = new SectionsQuery(t,crs);
        sq3.setAvailability(SectionsQuery.AvailabilityArg.OPEN_SEATS);
        sq3.addInstructionalMethod(InstructionalMethod.CLASSROOM);
        sq3.addDayTimeArg(thurDta);
        sq3.addDayTimeArg(tuesDta);
        sectionsArr[2] = (Sections.Section[]) sq3.results().toArray(new Sections.Section[sq3.resultsSize()]);
        //----------------------------Get POLS 1101
        subj = Subjects.get("POLS");
        crs = Courses.get(subj, "1101");
        SectionsQuery sq4 = new SectionsQuery(t,crs);
        sq4.setAvailability(SectionsQuery.AvailabilityArg.OPEN_SEATS);
        sq4.addInstructionalMethod(InstructionalMethod.CLASSROOM);
        sq4.addDayTimeArg(thurDta);
        sq4.addDayTimeArg(tuesDta);
        sectionsArr[3] = (Sections.Section[]) sq4.results().toArray(new Sections.Section[sq4.resultsSize()]);

        //----------------------------Track Time of Generate method
        /*long startTime = System.currentTimeMillis();
        int numGenerated = ScheduleGenerator.generate(sectionsArr);
        long endTime = System.currentTimeMillis();
        System.out.println("Generate operation took: " + (endTime - startTime) + " millis to generate " + numGenerated + " schedules");

        startTime = System.currentTimeMillis();
        SchedulePrinter.printAll(new File("C:\\users\\psout\\desktop\\schedules.txt"), false);
        endTime = System.currentTimeMillis();
        System.out.println("Print operation took: " + (endTime - startTime) + " millis");*/

