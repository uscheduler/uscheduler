package uscheduler.ui;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import javafx.stage.Window;

import java.io.File;
import java.util.Optional;

/**
 * Created by aa8439 on 3/31/2016.
 */
public class Popup{

    public static void display(Alert.AlertType type, String title, String message){
        Alert alert = new Alert(type, message, ButtonType.CLOSE);
        alert.setTitle(title);
        alert.showAndWait();
    }
    public static boolean userAccept(String title, String message){
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, message);
        alert.setTitle(title);
        Optional<ButtonType> result  = alert.showAndWait();
        if(result.isPresent() && result.get() == ButtonType.OK)
            return true;
        else if(result.isPresent()) {

            //alert.close();
            return false;
        }else {
            alert.close();
            return false;
        }
    }
    public static File getSaveLocation(){
        DirectoryChooser chooser = new DirectoryChooser();
        chooser.setTitle("uScheduler - Save Results");
        File defaultDirectory = new File("c:\\");
        chooser.setInitialDirectory(defaultDirectory);
        File selectedDirectory = chooser.showDialog(new Stage());
        return selectedDirectory;
    }
}
