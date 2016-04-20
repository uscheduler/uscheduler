/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uscheduler.ui.schedules;

import javafx.scene.Node;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.VBox;
import uscheduler.internaldata.Schedules;
import uscheduler.util.ScheduleGeneratorObserver;

/**
 * The ScrollPane to house the view of Schedules. 
 * Each Schedule's view is provided by the ScheduleContainer class, thus this ScrollPane houses ScheduleContainer objects (in a single VBox).
 * This class implements ScheduleGeneratorObserver. 
 * Users of this class will add this class as an observer the ScheduleGenerator 
 * and this class will respond to the ScheduleGenerator.generate method by updating the view of schedules.
 * @author Matt Matt Bush
 */
/**
 * Tab (SchedulesTab class)
 *      ScrollPane (SchedulesScrollPane class)
 *          VBox (cSchedulesVBox)
 *              VBox (ScheduleContainer class)
 *                  AnchorPane (ScheduleHeader class)
 *                  GridPane (DetailTable2 class)
 *                  TabPane (CalendarTabPane class)
 *              
 * 
 */       
public class SchedulesScrollPane extends ScrollPane implements ScheduleGeneratorObserver{
    private VBox cSchedulesVBox = new VBox();
    
    public SchedulesScrollPane(){
        
        /**
         * "If true and if the contained node [cSchedulesVBox] is a Resizable, 
         * then the node [cSchedulesVBox] will be kept resized to match the width of the ScrollPane's viewport. 
         * If the contained node [cSchedulesVBox] is not a Resizable, this value is ignored."
         */
        super.setFitToWidth(true);
        super.setContent(cSchedulesVBox);
        super.setVbarPolicy(ScrollBarPolicy.ALWAYS);
        super.getStyleClass().add("scroll-pane-schedules");
        //
        cSchedulesVBox.setSpacing(15.0);
        
        this.buildSchedules();
    }
    private void buildSchedules(){
//        Runtime runtime = Runtime.getRuntime();
//        long used = runtime.totalMemory() - runtime.freeMemory();
//        double mb = 1024*1024;
//        System.out.printf("Used Memory BEFORE buildSchedules(): %.2f MB", used / mb);
//        System.out.println();
        
        for (Schedules.Schedule sch : Schedules.getAll1()){
            ScheduleContainer schContainer = new ScheduleContainer(sch, this);
            cSchedulesVBox.getChildren().add(schContainer);
        }      
    }
    public void rebuildSchedules() {
//        Runtime runtime = Runtime.getRuntime();
//        long used = runtime.totalMemory() - runtime.freeMemory();
//        double mb = 1024*1024;
//        System.out.printf("Used Memory BEFORE GC and before Clear / Kill: %.2f MB", used / mb);
//        System.out.println();
        
        ScheduleContainer scheduleContainer;
        for(Node n : cSchedulesVBox.getChildren()){
            if(n instanceof ScheduleContainer){
                scheduleContainer = (ScheduleContainer) n;
                scheduleContainer.kill();
            }
        }
        cSchedulesVBox.getChildren().clear();
        
        
        System.gc();
//        used = runtime.totalMemory() - runtime.freeMemory();
//        mb = 1024*1024;
//        System.out.printf("Used Memory after GC and before buildSchedules: %.2f MB", used / mb);
//        System.out.println();
        
        buildSchedules();
        
        System.gc();
//        runtime = Runtime.getRuntime();
//        used = runtime.totalMemory() - runtime.freeMemory();
//        mb = 1024*1024;
//        System.out.printf("Used Memory after GC and after buildSchedules: %.2f MB", used / mb);
//        System.out.println();
//        System.out.println();
    }
    @Override
    public void schedulesGenerated() {
        rebuildSchedules();
        super.setVvalue(super.getVmin());
    }

    public void deleteRequested(ScheduleContainer pScheduleContainer, Schedules.Schedule pSchedule) {
        pScheduleContainer.kill();
        if(!pSchedule.isDeleted()){
            Schedules.delete(pSchedule);
        }
        cSchedulesVBox.getChildren().remove(pScheduleContainer);
    }
    
}
