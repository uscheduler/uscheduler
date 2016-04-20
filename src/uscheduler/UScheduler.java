
/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uscheduler;

import java.io.IOException;
import java.util.Optional;
import javafx.application.Application;
import static javafx.application.Application.launch;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.image.Image;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import uscheduler.externaldata.HTMLFormatException;
import uscheduler.externaldata.NoDataFoundException;
import uscheduler.ui.input.CoursesTab;
import uscheduler.ui.schedules.SchedulesTab;
import uscheduler.util.Importer;

//        //this line is for testing and will not be useful in end design
//        grid.setBackground(new Background(new BackgroundFill(Color.BLACK, null, null)));
//        grid.setAlignment(Pos.CENTER);
        /**
         * The next line: grid.prefWidthProperty().bind(primaryStage.widthProperty()); makes the width of the grid equal to the width of the primary stage, 
         * although same logic could be used with any parent container of the grid I assume.
         * Also, despite the grid being that width, this line DOES NOT force its celle to actually use all that width. 
         * To see this in action, set the background color of the grid using: grid.setBackground(new Background(new BackgroundFill(Color.BLACK, null, null)));
         * Note: I **THINK** that if the grid is placed in a Region (or one of its subclasses) it will automatically be 
         */
        //grid.prefWidthProperty().bind(primaryStage.widthProperty());
        
        
        //
        /**
         * adding the cc.setHgrow(Priority.ALWAYS); column constraint to each column in the grid 
         * ensures that each column will grow horizontally to ensure that all columns collectively take up the entire width of the GridPane. 
         * This is only useful if the grid itself is set to grow to take up the entire width of its parent container, 
         * otherwise the grid's width will only be as wide as needed to contains its columns. 
         * The: grid.prefWidthProperty().bind(primaryStage.widthProperty()); line ensures the grid is as wide as its parent container, which in this case is the Stage.
         * Note however that cc.setHgrow(Priority.ALWAYS); does NOT force all columns to be the same width; it only forces the sum of all column widths to equal the grid's width.
         * To get ALL columns to be the same width, set the setPercentWidth(); of each column's ColumnConstraint to the same value. 
         * In the DayOfWeek example, it will be 1/7*100
         * Also note that if cc.setPercentWidth() is used, and for all column constraints the values sum to 100, then cc.setHgrow(Priority.ALWAYS) becomes irrelevant.
         */
/**
 *
 * @author Matt
 */
public class UScheduler extends Application {
    public static final String HTML_EXCEPTION_MSG = "It appears that KSU has changed the format of their web site and UScheduler is unable to extract the necessary data.\n\nSorry!";
    public static final String NO_DATA_FOUND_EXCEPTION_MSG = "UScheduler could not find find the course of the specified subject and course number.\n\nPlease make sure the entered course number is valid for the specified subject and try again.";
    public static final String IO_EXCEPTION_MSG = "UScheduler was unsuccessful at connecting to KSU’s server to obtain course information, which requires an Internet connection.\n\nWould you like to try to resolve the issue and have UScheduler try again?\n\nOK: UScheduler will retry to connect.\n\nCANCEL: The UScheduler application will shut down and close.";
        

    @Override
    public void start(Stage primaryStage) {
        boolean doAgain = true;
        while(doAgain){
            doAgain=false;
            try {
                Importer.loadTerms();
                Importer.loadSubjectsAndCampuses();
            } catch (HTMLFormatException ex) {
                Alert alert = new Alert(Alert.AlertType.ERROR, UScheduler.HTML_EXCEPTION_MSG);
                alert.setTitle("Game Over!");
                alert.showAndWait();
            } catch (IOException ex) {
                Alert alert = new Alert(Alert.AlertType.CONFIRMATION, UScheduler.IO_EXCEPTION_MSG);
                alert.setTitle("Failed Connection");
                Optional<ButtonType> result = alert.showAndWait();

                doAgain = (result.get() == ButtonType.OK);

            } catch (NoDataFoundException ex) {
            } 
        } 
        
        Tab inputTab = new CoursesTab();
        Tab schedulesTab = new SchedulesTab();
        inputTab.setClosable(false);
        schedulesTab.setClosable(false);
        //****************************** 
//        Tab favorites = new Tab("Calendar Favorites");
//        Tab schedulescalender = new Tab("Calendar");
//        MatthewsCalendaeView mct = new MatthewsCalendaeView(schedulescalender, favorites);
//        favorites.setClosable(false);
//        schedulescalender.setClosable(false);
        //******************************
        TabPane tabPane = new TabPane(inputTab, schedulesTab);

        BorderPane root = new BorderPane();
        root.setCenter(tabPane);

        primaryStage.setTitle("UScheduler");
        primaryStage.setMaximized(true);
        Scene scene = new Scene(root);
        scene.getStylesheets().add(UScheduler.class.getResource("layoutstyles.css").toExternalForm());

        primaryStage.getIcons().add( new Image(UScheduler.class.getResourceAsStream( "uscheduler_icon80.png" )) {});
        primaryStage.setScene(scene);
        primaryStage.show(); 

    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }
    
}
       