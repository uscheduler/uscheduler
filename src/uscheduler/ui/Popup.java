package uscheduler.ui;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;

/**
 * Created by aa8439 on 3/31/2016.
 */
public class Popup {

    public static void display(Alert.AlertType type, String title, String message){
        Alert alert = new Alert(type, message, ButtonType.CLOSE);
        alert.setTitle(title);
        alert.showAndWait();
    }
}
