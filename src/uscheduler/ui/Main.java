package uscheduler.ui;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception{
        FXMLLoader loader = new FXMLLoader(getClass().getResource("uSchedule.fxml"));
        Parent root = loader.load();
        Controller myController = loader.getController();
        primaryStage.setTitle("uScheduler - Kennesaw's SMART Student Scheduler");
        primaryStage.setScene(new Scene(root, 1355, 735));
        primaryStage.getIcons().add(new Image("uscheduler/uSchedulerIcon.png"));
        primaryStage.show();
    }

    public static void main(String[] args){ launch(args); }
}