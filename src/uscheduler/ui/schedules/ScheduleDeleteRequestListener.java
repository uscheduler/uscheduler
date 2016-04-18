/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uscheduler.ui.schedules;

import javafx.scene.Node;
import uscheduler.internaldata.Schedules.Schedule;

/**
 *
 * @author Matt
 */
public interface ScheduleDeleteRequestListener {
    public void deleteRequested(Node pNode, Schedule pSchedule);
    
}
