package uscheduler.ui;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;

import java.util.Optional;

/**
 * Created by aa8439 on 3/31/2016.
 */
public class Popup {

    public static void display(Alert.AlertType type, String title, String message){
        Alert alert = new Alert(type, message, ButtonType.CLOSE);
        alert.setTitle(title);
        alert.showAndWait();
    }
    public static boolean userAccept(String title, String message){
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, message);
        alert.setTitle(title);
        Optional<ButtonType> result = alert.showAndWait();
        if(alert.getResult() == ButtonType.OK)
            return true;
        else
            return false;
    }
}
