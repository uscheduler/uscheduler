package uscheduler.ui.input;

import com.sun.javafx.scene.control.skin.ComboBoxListViewSkin;
import javafx.collections.FXCollections;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.layout.VBox;
import uscheduler.global.UTime;
import java.time.DayOfWeek;
import java.time.format.TextStyle;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.input.KeyEvent;
import javafx.util.Callback;
import uscheduler.util.SectionsQuery.DayTimeArg;

/**
 * @author Matt Bush
 */
public class DayInput extends VBox{
    //Data
    private final DayOfWeek cDayOfWeek;
    private final DayTimeArg cDayTimeArg;
    private final HashSet<DayInputCheckListener> cDayVBoxCheckListeners = new HashSet<>();
    
    //Controls
    private final CheckBox checkDay;
    private final ComboBox<UTime> cmbMinStart;
    private final ComboBox<UTime> cmbMaxEnd;
    
    //Listeners / Handlers
    private final ChangeListener<UTime> MAX_END_CHANGE_LISTENER = new ChangeListener<UTime>() {
        @Override
        public void changed(ObservableValue<? extends UTime> observable, UTime oldValue, UTime newValue) {
            cDayTimeArg.setMaxEnd(newValue);
        }
     };
    private final ChangeListener<UTime> MIN_START_CHANGE_LISTENER = new ChangeListener<UTime>() {
        @Override
        public void changed(ObservableValue<? extends UTime> observable, UTime oldValue, UTime newValue) {
            cDayTimeArg.setMinStart(newValue);
        }
    };
    private final ChangeListener<Boolean> CHECHED_CHANGE_LISTENER =  new ChangeListener<Boolean>() {
        @Override
        public void changed(ObservableValue<? extends Boolean> ov,Boolean old_val, Boolean new_val) {
            for(DayInputCheckListener listener : cDayVBoxCheckListeners)
                listener.dayCheckChange(cDayTimeArg, new_val);
            cmbMinStart.setDisable(!new_val);
            cmbMaxEnd.setDisable(!new_val);
        }
     };
    //Used in conjunction with MIN_START_ON_SHOW, to implement the auto-complete functionality of min start combo box
    private final EventHandler< KeyEvent > MIN_START_KEY_RELEASE = new EventHandler< KeyEvent > (){
        @Override
        public void handle( KeyEvent event ) {
            String key = event.getText().toUpperCase();
            if (key.matches("\\d")) {
                final int selectedIndex = Math.max(0,cmbMinStart.getSelectionModel().getSelectedIndex());
                List<UTime> sourceItems = cmbMinStart.getItems();

                for (int i = selectedIndex + 1; i < sourceItems.size(); i++) {
                    if(sourceItems.get(i) != null && sourceItems.get(i).toString().startsWith(key)){
                        cmbMinStart.getSelectionModel().select(sourceItems.get(i));
                        cmbMinStart.setValue(sourceItems.get(i));//NOTE: This is critical! In a comboBox, changing the selection value DOES NOT change the value!!
                        cmbMinStart.show();
                        return;
                    }
                }   
                for (int i = 0; i < selectedIndex; i++) {
                    if(sourceItems.get(i) != null && sourceItems.get(i).toString().startsWith(key)){
                        cmbMinStart.getSelectionModel().select(sourceItems.get(i));
                        cmbMinStart.setValue(sourceItems.get(i));//NOTE: This is critical! In a comboBox, changing the selection value DOES NOT change the value!!
                        cmbMinStart.show();
                        return;
                    }
                }                     
            }
        }
    };
    private final EventHandler<Event> MIN_START_ON_SHOW = new EventHandler<Event>() {
        @Override
        public void handle(Event event) {
            // Work around; need to check for proper solution.
            final int selectedIndex = cmbMinStart.getSelectionModel().getSelectedIndex();
            final ComboBoxListViewSkin<?> skin = (ComboBoxListViewSkin<?>) cmbMinStart.getSkin();
            skin.getListView().scrollTo(selectedIndex < 0 ? 0 : selectedIndex);
        }
    };
 //Used in conjunction with MAX_END_ON_SHOW, to implement the auto-complete functionality of min start combo box
    private final EventHandler< KeyEvent > MAX_END_KEY_RELEASE = new EventHandler< KeyEvent > (){
        @Override
        public void handle( KeyEvent event ) {
            String key = event.getText().toUpperCase();
            if (key.matches("\\d")) {
                final int selectedIndex = Math.max(0,cmbMaxEnd.getSelectionModel().getSelectedIndex());
                List<UTime> sourceItems = cmbMaxEnd.getItems();

                for (int i = selectedIndex + 1; i < sourceItems.size(); i++) {
                    if(sourceItems.get(i) != null && sourceItems.get(i).toString().startsWith(key)){
                        cmbMaxEnd.getSelectionModel().select(sourceItems.get(i));
                        cmbMaxEnd.setValue(sourceItems.get(i));//NOTE: This is critical! In a comboBox, changing the selection value DOES NOT change the value!!
                        cmbMaxEnd.show();
                        return;
                    }
                }   
                for (int i = 0; i < selectedIndex; i++) {
                    if(sourceItems.get(i) != null && sourceItems.get(i).toString().startsWith(key)){
                        cmbMaxEnd.getSelectionModel().select(sourceItems.get(i));
                        cmbMaxEnd.setValue(sourceItems.get(i));//NOTE: This is critical! In a comboBox, changing the selection value DOES NOT change the value!!
                        cmbMaxEnd.show();
                        return;
                    }
                }                     
            }
        }
    };
    private final EventHandler<Event> MAX_END_ON_SHOW = new EventHandler<Event>() {
        @Override
        public void handle(Event event) {
            // Work around; need to check for proper solution.
            final int selectedIndex = cmbMaxEnd.getSelectionModel().getSelectedIndex();
            final ComboBoxListViewSkin<?> skin = (ComboBoxListViewSkin<?>) cmbMaxEnd.getSkin();
            skin.getListView().scrollTo(selectedIndex < 0 ? 0 : selectedIndex);
        }
    };
    /**
     * **************************************
     * ***********Constructor************
     * **************************************
     */
    DayInput(DayOfWeek dayOfWeek){
        this.cDayOfWeek = dayOfWeek;
        cDayTimeArg = new DayTimeArg(cDayOfWeek);
        
        
        /*****************************************
         * ***** Min Start Combo Box
         * *****************************************
         */
        cmbMinStart = new ComboBox<>(FXCollections.observableArrayList());
        cmbMinStart.valueProperty().addListener(this.MIN_START_CHANGE_LISTENER);
        cmbMinStart.setButtonCell(new UTimeListCell());
        cmbMinStart.setCellFactory(UTIME_CELL_FACT);
        cmbMinStart.setOnKeyReleased(MIN_START_KEY_RELEASE);
        cmbMinStart.setOnShown(MIN_START_ON_SHOW);
        //***************************
        cmbMinStart.getItems().add(null);
        for(int i = 0; i < 24; i++)
            cmbMinStart.getItems().add(new UTime(i,0));
        cmbMinStart.setValue(null);
        
        /*****************************************
         * ***** Max End Combo Box
         * *****************************************
         */
        cmbMaxEnd = new ComboBox<>(FXCollections.observableArrayList());
        cmbMaxEnd.valueProperty().addListener(this.MAX_END_CHANGE_LISTENER);
        cmbMaxEnd.setButtonCell(new UTimeListCell());
        cmbMaxEnd.setCellFactory(UTIME_CELL_FACT);
        cmbMaxEnd.setOnKeyReleased(MAX_END_KEY_RELEASE);
        cmbMaxEnd.setOnShown(MAX_END_ON_SHOW);        
        //***************************
        cmbMaxEnd.getItems().add(null);
        for(int i = 0; i < 24; i++)
            cmbMaxEnd.getItems().add(new UTime(i,0));    
        cmbMaxEnd.setValue(null);
        /*****************************************
         * ***** Selected Check Box
         * *****************************************
         */    
        checkDay= new CheckBox(cDayOfWeek.getDisplayName(TextStyle.FULL, Locale.getDefault()));           
        checkDay.selectedProperty().addListener(CHECHED_CHANGE_LISTENER);
        checkDay.setSelected(cDayOfWeek != DayOfWeek.SATURDAY && cDayOfWeek != DayOfWeek.SUNDAY);     
        
        
        /**
         ******************************************
         * ***** Layout Controls in VBox
         * *****************************************
         */
        super.getChildren().addAll(checkDay,cmbMinStart,cmbMaxEnd);
        super.getStyleClass().add("vbox-day-input");
    }


    
    DayTimeArg dayTimeArg(){return cDayTimeArg;}
    public boolean isSelected(){return checkDay.isSelected();}
    
    public void addDayVBoxCheckListener(DayInputCheckListener pListener){
        if(pListener==null)
            throw new IllegalArgumentException("Null pListener argument.");    
        cDayVBoxCheckListeners.add(pListener);
    }
    public void removeDayVBoxCheckListener(DayInputCheckListener pListener){   
        cDayVBoxCheckListeners.remove(pListener);
    }
    /**
     * ******************************************************************
     * **************************Custom Cell Factories************
     * ******************************************************************
     */   
    private final Callback<ListView<UTime>, ListCell<UTime>> UTIME_CELL_FACT = new Callback<ListView<UTime>, ListCell<UTime>>() {

        public ListCell<UTime> call(ListView<UTime> param) {
            return new DayInput.UTimeListCell();
        }
    };
    /**
     * ******************************************************************
     * **************************Custom Cells ***************************
     * ******************************************************************
     */      
    class UTimeListCell extends ListCell<UTime> {
        @Override protected void updateItem(UTime pUTime, boolean empty) {
            super.updateItem(pUTime, empty);
            if (empty) {
                setText(null);
            } else if (pUTime == null) {
                setText(null);
            } else {
                setText(pUTime.toString());
            }
        }
    } 
}