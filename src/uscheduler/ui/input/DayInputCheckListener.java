/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uscheduler.ui.input;

import uscheduler.util.SectionsQuery;

/**
 *
 * @author Matt
 */
public interface DayInputCheckListener {
    public void dayCheckChange(SectionsQuery.DayTimeArg pDayTimeArg, boolean pChecked);
}
