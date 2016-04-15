package uscheduler.ui;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.ListView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.text.Font;
import uscheduler.externaldata.HTMLFormatException;
import uscheduler.externaldata.NoDataFoundException;
import uscheduler.internaldata.*;
import uscheduler.ui.schedules.SchedulesTab;
import uscheduler.util.Importer;
import uscheduler.util.ScheduleGenerator;
import java.util.ArrayList;


import java.text.ParseException;
import java.time.DayOfWeek;
import java.util.Collection;
import uscheduler.global.UDate;
import uscheduler.global.UTime;
import javafx.scene.paint.Color;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;

public class Controller implements Initializable {
    @FXML
    TabPane tabPane;
    @FXML
    Tab resultsTab;
    @FXML
    Tab inputTab;
    @FXML
    Button buttonGenerateSchedule;
    @FXML
    Button buttonAddCourse;
    @FXML
    ListView listCourse;
    @FXML
    GridPane grid;

    private TopHBox top = new TopHBox();
    private ObservableList<HBox> hBoxList = FXCollections.observableArrayList();
    private ArrayList<CourseHBox> hBoxes = new ArrayList<>();
    private ArrayList<Terms.Term> terms;
    private ArrayList<Campuses.Campus> campuses;
    private ArrayList<Subjects.Subject> subjects;
    private Font font1 = new Font(25);
    private Font font2 = new Font(15);
    private int POTENTIAL_MAX = 100000;
    private SchedulesTab newResultsTab = new SchedulesTab();
    private final ChangeListener<Terms.Term> termChangeListener = new ChangeListener<Terms.Term>()
    {
        @Override
        public void changed(ObservableValue<? extends Terms.Term> observable, Terms.Term oldValue, Terms.Term newValue){
            if(oldValue != null) {
                if (Popup.userAccept("uScheduler - Warning", "By changing the term all of your data below will be removed. " +
                        "Do you still wish to proceed?")){
                    hBoxes.clear();
                    hBoxList.clear();
                }else{
                    top.cmbTerm.getSelectionModel().selectedItemProperty().removeListener(termChangeListener);
                    top.cmbTerm.setValue(oldValue);
                    top.cmbTerm.getSelectionModel().selectedItemProperty().addListener(termChangeListener);
                }
            }
        }
    };


    @Override
    public void initialize(URL location, ResourceBundle resources){
        grid.getChildren().add(0, top);
        CourseHBox course = new CourseHBox();
        hBoxList.add(0, course);
        hBoxes.add(0, course);
        setDeleteAction(0);
        listCourse.setItems(hBoxList);
        handleDayCheckBoxAction();
        getTerms();
        setInitialDayTimeArg(0);
        tabPane.getTabs().add(newResultsTab);
        tabPane.getTabs().remove(resultsTab);
    }
    public void handleAddButton(ActionEvent e) {
        if(hBoxes.size() == 7){
            Popup.display(Alert.AlertType.WARNING, "uScheduler", "You have exceeded the total number of classes that" +
                    " can be taken in a semester");
        }else{
            CourseHBox course = new CourseHBox();
            hBoxList.add(0, course);
            hBoxes.add(0, course);
            updateHBoxPosition();
            setDeleteAction(0);
            setCourseIDAction(0);
            setSubjectOnAdd(0);
            listCourse.setItems(hBoxList);
            setTermInHBox(0);
            setInitialDayTimeArg(0);
            setCampusesInHBox();
        }
    }
    private void setDeleteAction(int j){
        hBoxes.get(j).buttonRemove.setOnAction(e -> {
                hBoxes.get(j).safeRemove();
                hBoxList.remove(hBoxes.get(j).getOnRow());
                hBoxes.remove(hBoxes.get(j).getOnRow());
                updateHBoxPosition();
        });
    }
    private void setCourseIDAction(int j){
        hBoxes.get(j).setCourseIDAction(top.cmbTerm.getValue());
    }
    private void setSubjectOnAdd(int j){
        if(top.cmbTerm.getValue() != null){
            hBoxes.get(j).setSubjects(subjects);
        }
    }
    private void updateHBoxPosition(){
        for(int j = 0; j < hBoxes.size(); j++){
            hBoxes.get(j).setOnRow(j);
            setDeleteAction(j);
        }
    }
    private void handleDayCheckBoxAction(){
        for(DayVBox d: top.days){
            d.checkDay.setOnAction(e -> {
                if(!d.checkDay.isSelected()){
                    d.disableDay(true);
                    for(CourseHBox c: hBoxes){
                        c.removeDayTimeArg(d.dta);
                    }
                }else{
                    d.disableDay(false);
                    for(CourseHBox c: hBoxes) {
                        c.addDayTimeArg(d.dta);
                    }
                }
            });
        }
    }
    private void getTerms(){
        try {
            Importer.loadTerms();
            Importer.loadSubjectsAndCampuses();
        }catch (HTMLFormatException e){
            Popup.display(Alert.AlertType.ERROR, "uScheduler - HTMLFormatException", "It appears that KSU has changed their courses page." +
                    "There is a chance the data collected is corrupt, please contact uscheduler team for resolution.");
            Platform.exit();
        }catch (IOException e){
            Popup.display(Alert.AlertType.ERROR, "uScheduler - IOException", "Looks like you do not have Internet Connectivity." +
                    "  Please fix then relaunch the application");
            Platform.exit();
        }catch (NoDataFoundException e){
            Popup.display(Alert.AlertType.ERROR, "uSchuler - NoDataFoundException", "Unable to find campuses and/or subjects" +
                    "KSU's website may be experiencing difficulty, please try again later.");
            Platform.exit();
        }
        terms = Terms.getAll(Terms.PK_DESC);
        top.setTerms(terms);
        top.cmbTerm.valueProperty().addListener(e -> {
            setSubjectsAndCampuses();
            setCourseIDAction(0);
            for(CourseHBox c: hBoxes) {
                c.setTerm(top.cmbTerm.getValue());
            }
        });
        top.cmbTerm.getSelectionModel().selectedItemProperty().addListener(termChangeListener);
    }
    private void setSubjectsAndCampuses(){
        campuses = Campuses.getAll(Campuses.PK_ASC);
        top.setCampuses(campuses);

        subjects = Subjects.getAll(Subjects.PK_ASC);
        for(int j = 0; j < hBoxes.size(); j++){
            hBoxes.get(j).setSubjects(subjects);
        }
        setCampusesInHBox();

    }
    private void setInitialDayTimeArg(int j){
        for(DayVBox d : top.days){
            hBoxes.get(j).addDayTimeArg(d.dta);
        }
    }
    private void setTermInHBox(int j){
        hBoxes.get(j).setTerm(top.cmbTerm.getValue());
    }
    private void setCampusesInHBox() {
        top.listCampus.setOnMouseClicked(e -> {
            if(top.listCampus.getSelectionModel().getSelectedItems() != null){
                if(top.listCampus.getSelectionModel().getSelectedItems().size() < campuses.size()) {
                    for(int j = 0; j < hBoxes.size(); j++) {
                        hBoxes.get(j).removeAllCampuses();
                        for (Object obj : top.listCampus.getSelectionModel().getSelectedItems()) {
                            hBoxes.get(j).addCampus((Campuses.Campus) obj);
                        }
                    }
                }else{
                    for(int j = 0; j < hBoxes.size(); j++) {
                        hBoxes.get(j).removeAllCampuses();
                    }
                }
            }
        });
    }
    public void handleGenerateSchedule(ActionEvent e) {
        tabPane.getSelectionModel().select(newResultsTab);
        /*/Disable Generate Schedules button if any of the sectionsquery(s) are set to 0
        Check POTENTIAL_MAX
        cannot duplicate courses
        */
        //create 2 dimensional array where first dimesion is hboxes.size();
        int numOfPossibleSchedules = hBoxes.get(0).getSectionsQueryResultCount();
        for(int j = 1; j < hBoxes.size(); j++){
            numOfPossibleSchedules = (numOfPossibleSchedules * hBoxes.get(j).getSectionsQueryResultCount());
            System.out.println(numOfPossibleSchedules);
        }

        if(numOfPossibleSchedules > POTENTIAL_MAX){
            Popup.display(Alert.AlertType.ERROR, "uScheduler - Too Many Results", "Your current selections will result" +
                    " in a huge number of schedules.  Please try restricting your selections a bit more.");
        }else if(numOfPossibleSchedules == 0) {
            Popup.display(Alert.AlertType.ERROR, "uScheduler - Zero Sections", "One of the courses you have entered" +
                    " has zero possible sections. Please try adjusting your criteria and try again.");
        }else {
            Sections.Section[][] courseSections = new Sections.Section[hBoxes.size()][];
            for (int i = 0; i < hBoxes.size(); i++) {
                courseSections[i] = hBoxes.get(i).getSectionsQuery().results2();
            }

                int schedulesGenerated = ScheduleGenerator.generate(courseSections);
            /*try {
                File saveLocation = Popup.getSaveLocation();
                SchedulePrinter.printAll(new File(saveLocation + "\\uScheduler_Generated_Schedules.txt"), false);
            } catch (IOException e1) {
                e1.printStackTrace();
            }*/
                //drawSchedules();
        }
    }

















/*
        MATTHEW's OUTPUT Drawing


    private GraphicsContext makeCRNbox(GraphicsContext gc, Color c, int factor, String crn, String prof, String avail, String campus, String session, String type, String subj, String coursenum, String section){

        gc.setFont(font1);
        //gc.fillRect(145,510,120,150);

        //make box and CRN
        gc.setFill(c);
        gc.fillRect(185+(factor*135),502,120,25);
        gc.setFill(Color.BLACK);
        gc.fillText(crn,210+(factor*135),525);
        gc.fillRect(185+(factor*135),502,120,2);
        gc.fillRect(185+(factor*135),502,2,150);
        gc.fillRect(185+(factor*135),652,120,2);
        gc.fillRect(305+(factor*135),502,2,152);

        //make tabled data
        gc.setFont(font2);
        gc.fillRect(185+(factor*135),527,120,2);
        gc.fillText(subj + " " +  coursenum + "/" + section,190+(factor*135),547,110);
        gc.fillText(prof,190+(factor*135),567,110);
        gc.fillText(type,190+(factor*135),587,110);
        gc.fillText(avail,190+(factor*135),607,110);
        gc.fillText(session,190+(factor*135),627,110);
        gc.fillText(campus,190+(factor*135),647,110);
        return gc;
    }
    private void drawSchedules() {
        Collection<Schedules.Schedule> schs = Schedules.getAll1();

        Color[] Carr = {Color.rgb(230, 159, 0), Color.rgb(86, 180, 233), Color.rgb(0, 158, 115), Color.rgb(240, 228, 66), Color.rgb(0, 114, 178), Color.rgb(213, 94, 0), Color.rgb(204, 121, 167)};
        ObservableList<Canvas> schedules = FXCollections.observableArrayList();
        UDate tmpstart = null;
        UDate tmpend = null;
        try {
            tmpstart = new UDate("Aug 16, 2016");
            tmpend = new UDate("Dec 12, 2016");
        } catch (ParseException e) {
            e.printStackTrace();
        }

        int termspan = tmpstart.daysTo(tmpend);
        UTime displaystart = new UTime(6, 0);
        int timespanminutes = 17 * 60;
        double minutetounitconversion = (26.0 / 60.0);

        int color = 0;
        int xx = 0;
        for (Schedules.Schedule s : schs) {
            color = -1;
            Canvas sched = new Canvas(1300, 800);
            sched.setFocusTraversable(false);
            GraphicsContext gc = sched.getGraphicsContext2D();
            for (Sections.Section sec : s.sections2()) {
                color += 1;

                UDate secstart = sec.session().startDate();
                UDate secend = sec.session().endDate();
                double sesstart = tmpstart.daysTo(secstart) + 1;
                double sesend = (secend.daysTo(tmpend));

                //System.out.println("sesstart: " + sesstart + "    sesend: " + sesend);
                String aval = "";
                if (sec.seatsAvailable() > 0) {
                    aval += "Seats Availible";
                } else if (sec.seatsAvailable() == 0 && sec.waitlistAvailable() > 0) {
                    aval += "Waitlist Availible";
                } else {
                    aval += "Full Capacity";
                }

                gc = makeCRNbox(gc, Carr[color], color, Integer.toString(sec.crn()), sec.instructorsString(), aval, sec.campus().campusName(), sec.session().sessionName(), sec.instructionalMethod().toString(), sec.course().subject().subjectAbbr(), sec.course().courseNum(), sec.sectionNumber());
                for (Sections.Section.MeetingTime metT : sec.meetings2()) {
                    gc.setFill(Carr[color]);

                    UTime startT = metT.startTime();
                    UTime endT = metT.endTime();
                    double distY = startT.minutesTo(endT) * minutetounitconversion;
                    System.out.println("con: " + minutetounitconversion);
                    System.out.println("minstoend: " + (startT.minutesTo(endT) * minutetounitconversion));
                    double finalstartY = 50 + (displaystart.minutesTo(startT) * minutetounitconversion);
                    System.out.println("distY: " + distY + "     finalstartY: " + finalstartY);

                    for (DayOfWeek d : metT.days2()) {

                        int columnwidth = findColumnWidth(d.name());
                        int startX = findStartX(d.name());
                        int endX = findEndX(d.name());
                        double convertedsesend = ((columnwidth * sesend) / termspan);
                        double finalstartX = startX + sesstart;
                        double finalendX = endX - convertedsesend;
                        double distX = finalendX - finalstartX;


                        gc.fillRect(finalstartX, finalstartY, distX, distY);
                        //System.out.println("finalstartX: " + finalstartX + "     distX: " + distX);
                    }
                }
                gc = makeOutline(gc);
            }
            schedules.add(xx, sched);
            xx++;
        }

        ListView<Canvas> dTlistview = new ListView<>();
        dTlistview.setItems(schedules);
    }

    private int findStartX(String day){
        if(day.equals("MONDAY")){
            return 180;
        }
        if(day.equals("TUESDAY")){
            return 360;
        }
        if(day.equals("WEDNESDAY")){
            return 540;
        }
        if(day.equals("THURSDAY")){
            return 720;
        }
        if(day.equals("FRIDAY")){
            return 900;
        }
        if(day.equals("SATURDAY")){
            return 1080;
        }
        if(day.equals("SUNDAY")){
            return 1180;
        }
        return 3;
    }


    private int findEndX(String day){
        if(day.equals("MONDAY")){
            return 360;
        }
        if(day.equals("TUESDAY")){
            return 540;
        }
        if(day.equals("WEDNESDAY")){
            return 720;
        }
        if(day.equals("THURSDAY")){
            return 900;
        }
        if(day.equals("FRIDAY")){
            return 1080;
        }
        if(day.equals("SATURDAY")){
            return 1180;
        }
        if(day.equals("SUNDAY")){
            return 1280;
        }
        return 3;
    }


    private int findColumnWidth(String day){
        if(day.equals("MONDAY")){
            return 180;
        }
        if(day.equals("TUESDAY")){
            return 180;
        }
        if(day.equals("WEDNESDAY")){
            return 180;
        }
        if(day.equals("THURSDAY")){
            return 180;
        }
        if(day.equals("FRIDAY")){
            return 190;
        }
        if(day.equals("SATURDAY")){
            return 100;
        }
        if(day.equals("SUNDAY")){
            return 100;
        }
        return 3;
    }
    private static void drawDottedLine(int ycoord, GraphicsContext gc){
        for(int i=180;i<=1060;i+=30){
            gc.fillRect(i,ycoord,20,1);
        }
        for(double i=1065;i<=1276;i+=16.6){
            gc.fillRect(i,ycoord,11.1,1);
        }
        //gc.fillRect(1240,ycoord,13.3,1);
    }

    private GraphicsContext makeOutline(GraphicsContext gc){
        // <editor-fold defaultstate="collapsed" desc="schedule draw">
        //draw the outline shell
        gc.setFill(Color.BLACK);
        gc.fillRect(80,20,1200,2);
        gc.fillRect(80,20,2,642);
        gc.fillRect(80,662,1200,2);
        gc.fillRect(1280, 20, 2, 644);
        gc.fillRect(80,50,1200,2);
        gc.fillRect(180,20,2,472);
        gc.fillRect(80,492,1200,2);

        //draw the vertical day lines and the CRN box
        gc.setFont(font1);
        gc.fillText("CRNs:", 100, 524);
        gc.fillRect(360,20,2,472);
        gc.fillRect(540,20,2,472);
        gc.fillRect(720,20,2,472);
        gc.fillRect(900,20,2,472);
        gc.fillRect(1080,20,2,472);
        gc.fillRect(1180,20,2,472);
        gc.setFont(font2);
        gc.fillText("Monday",240,40);
        gc.fillText("Tuesday",420,40);
        gc.fillText("Wednesday",595,40);
        gc.fillText("Thursday",780,40);
        gc.fillText("Friday",970,40);
        gc.fillText("Saturday",1100,40);
        gc.fillText("Sunday",1205,40);


        //draw the lines that symbolize the times
        drawDottedLine(63,gc);
        gc.fillRect(180, 76, 1100, 1);
        drawDottedLine(89,gc);
        gc.fillRect(180, 102, 1100, 1);
        drawDottedLine(115,gc);
        gc.fillRect(180, 128, 1100, 1);
        drawDottedLine(141,gc);
        gc.fillRect(180, 154, 1100, 1);
        drawDottedLine(167,gc);
        gc.fillRect(180, 180, 1100, 1);
        drawDottedLine(193,gc);
        gc.fillRect(180, 206, 1100, 1);
        drawDottedLine(219,gc);
        gc.fillRect(180, 232, 1100, 1);
        drawDottedLine(245,gc);
        gc.fillRect(180, 258, 1100, 1);
        drawDottedLine(271,gc);
        gc.fillRect(180, 284, 1100, 1);
        drawDottedLine(297,gc);
        gc.fillRect(180, 310, 1100, 1);
        drawDottedLine(323,gc);
        gc.fillRect(180, 336, 1100, 1);
        drawDottedLine(349,gc);
        gc.fillRect(180, 362, 1100, 1);
        drawDottedLine(375,gc);
        gc.fillRect(180, 388, 1100, 1);
        drawDottedLine(401,gc);
        gc.fillRect(180, 414, 1100, 1);
        drawDottedLine(427,gc);
        gc.fillRect(180, 440, 1100, 1);
        drawDottedLine(453,gc);
        gc.fillRect(180, 466, 1100, 1);
        drawDottedLine(479,gc);

        //put times on left side
        gc.fillText("7:00 am", 110, 79);
        gc.fillText("8:00 am", 110, 105);
        gc.fillText("9:00 am", 110, 131);
        gc.fillText("10:00 am", 110, 157);
        gc.fillText("11:00 am", 110, 183);
        gc.fillText("12:00 pm", 110, 209);
        gc.fillText("1:00 pm", 110, 235);
        gc.fillText("2:00 pm", 110, 261);
        gc.fillText("3:00 pm", 110, 287);
        gc.fillText("4:00 pm", 110, 313);
        gc.fillText("5:00 pm", 110, 339);
        gc.fillText("6:00 pm", 110, 365);
        gc.fillText("7:00 pm", 110, 391);
        gc.fillText("8:00 pm", 110, 417);
        gc.fillText("9:00 pm", 110, 443);
        gc.fillText("10:00 pm", 110, 469);


        return gc;
        //</editor-fold>
    }
*/

}
