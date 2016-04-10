/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uscheduler.util;

import java.time.DayOfWeek;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import uscheduler.global.InstructionalMethod;
import uscheduler.global.UTime;
import uscheduler.internaldata.Campuses.Campus;
import uscheduler.internaldata.Courses.Course;
import uscheduler.internaldata.Instructors.Instructor;
import uscheduler.internaldata.Sections;
import uscheduler.internaldata.Sections.Section;
import uscheduler.internaldata.Sections.Section.MeetingTime;
import uscheduler.internaldata.Sessions.Session;
import uscheduler.internaldata.Terms.Term;


/**
 * This class provides a means to perform a query on {@link uscheduler.internaldata.Sections Sections} by accepting multiples types of criteria and restrictions on the sections.
 * The results of the query are obtained via the {@link uscheduler.util.SectionsQuery#results() results()} method.
 * 
 * <p>This class implements {@link uscheduler.util.DayTimeArgObserver DayTimeArgObserver} 
 * and adds itself as an DayTimeArgObserver to each <code>DayTimeArg</code> in {@link #dayTimeArgs()  dayTimeArgs()} 
 * so that it can be notified of changes to the <code>DayTimeArg</code> and update <code>results()</code>. 
 * It is important that users of this class call {@link #close() close()} 
 * when finished with an instance so that the <code>SectionsQuery</code> can 
 * remove itself as a DayTimeArgObserver from each DayTimeArgObserver in <code>dayTimeArgs()</code> and no longer be notified of changes.
 * 
 * <br>
 * <p>Additionally, this class provides a means for a {@link uscheduler.util.SectionsQueryObserver SectionsQueryObserver} 
 * to add itself as an observer to an object of this class via {@link #addObserver(uscheduler.util.SectionsQueryObserver) addObserver}. 
 * The <code>SectionsQueryObserver</code> will be notified when <code>results()</code> changes.
 * 
 * <p>In order for a query to contain any results, the minimal amount of input into the query is a {@link uscheduler.internaldata.Terms.Term Term} and 
 *  a {@link uscheduler.internaldata.Courses.Course Course}. 
 * From there, more criteria can be specified that further restrict the sections in the results of the query.
 * 
 * <p>The following lists the various criteria and their effect:
 * <p><b>Term:</b> all sections in <code>results()</code> must belong to the specified <code>Term</code>. If null, <code>results()</code> will be empty.
 * 
 * <p><b>Course:</b> all sections in <code>results()</code> must belong to the specified <code>Course</code>. If null, <code>results()</code> will be empty.
 * 
 * <p><b>Section Restriction List:</b> If this list is empty, <code>results()</code> will not be affected. 
 * Otherwise, no sections will exist in <code>results()</code> that are not also in this list.
 * 
 * <p><b>Session Restriction List:</b> If this list is empty, <code>results()</code> will not be affected.
 * Otherwise, no sections will exist in <code>results()</code> that don't have 
 * a {@link uscheduler.internaldata.Sections.Section#session()  Section.session()} that is in this list.

 * <p><b>InstructionalMethod Restriction List:</b> If this list is empty, <code>results()</code> will not be affected.
 * Otherwise, no sections will exist in <code>results()</code> that don't have 
 * a {@link uscheduler.internaldata.Sections.Section#instructionalMethod() Section.instructionalMethod()} that is in this list.

 * <p><b>Instructor Restriction List:</b> If this list is empty, <code>results()</code> will not be affected.
 * Otherwise, no sections will exist in <code>results()</code> that don't have 
 * at least one instructor  in {@link uscheduler.internaldata.Sections.Section#instructors1()  Section.instructors} that is in this list.

 * <p><b>Campus Restriction List:</b> If this list is empty, <code>results()</code> will not be affected.
 * Otherwise, no sections will exist in <code>results()</code> that have a non-null {@link uscheduler.internaldata.Sections.Section#campus()  Section.campus()} 
 * and don't have {@link uscheduler.internaldata.Sections.Section#campus()  Section.campus()} that is in this list.

 * <p><b>Availability Restriction:</b> If <code>{@link uscheduler.util.SectionsQuery#availability() availability()}=={@link uscheduler.util.SectionsQuery.AvailabilityArg#ANY ANY}</code>, 
 * <code>results()</code> will not be affected by this value.
 * <br>If {@link uscheduler.util.SectionsQuery.AvailabilityArg#OPEN_SEATS OPEN_SEATS},
 *  no sections will exist in <code>results()</code> such that <code>{@link uscheduler.internaldata.Sections.Section#seatsAvailable() Section.seatsAvailable()} == 0</code>.
 * <br>If {@link uscheduler.util.SectionsQuery.AvailabilityArg#OPEN_WAITLIST OPEN_WAITLIST},
 *  no sections will exist in <code>results()</code> such that 
 * <code>{@link uscheduler.internaldata.Sections.Section#seatsAvailable() Section.seatsAvailable()}==0 
 * AND {@link uscheduler.internaldata.Sections.Section#waitlistAvailable() Section.waitlistAvailable()}==0</code>.
 * 
 * <p><b>Day Time Restriction List:</b> A <code>Section</code> must meet the following criteria for it to be in <code>results()</code>:
 * <br>If <code>{@link uscheduler.internaldata.Sections.Section#meetings1()  Section.meetings().size > 0}</code>, 
 * then for each <code>DayOfWeek dow[i]</code> in <code>{@link uscheduler.internaldata.Sections.Section.MeetingTime#days1()  MeetingTime.days()}</code>, 
 * for each <code>MeetingTime mt[j]</code> in <code>Section.meetings()</code>, 
 * this list must contain a <code>{@link uscheduler.util.SectionsQuery.DayTimeArg DayTimeArg}</code> 
 * such that <code>{@link uscheduler.util.SectionsQuery.DayTimeArg#dayOfWek()  DayTimeArg.dayOfWek()}== dow[i]</code>.
 * <br>Additionally, if for the corresponding <code>DayTimeArg, {@link uscheduler.util.SectionsQuery.DayTimeArg#minStart() minStart()} != null</code>,
 * then it must be true that 
 * <code>minStart() &lt;= {@link uscheduler.internaldata.Sections.Section.MeetingTime#startTime()  mt[j].startTime()}</code>. 
 * <br>Additionally, if for the corresponding <code>DayTimeArg, {@link uscheduler.util.SectionsQuery.DayTimeArg#maxEnd()  maxEnd()} != null</code>,
 * then it must be true that 
 * <code>maxEnd() &gt;= {@link uscheduler.internaldata.Sections.Section.MeetingTime#endTime()   mt[j].endTime()}</code>.



 * @author Matt Bush
 */
public final class SectionsQuery implements DayTimeArgObserver{
    
    private Term cTerm = null;
    private Course cCourse = null;
    private List<Section> cBaseSections = new ArrayList<>(); //Eventually this will be set to a read-only list.
    private HashMap<DayOfWeek, DayTimeArg> cDayTimeArgs = new HashMap();
    private HashSet<Campus> cCampuses = new HashSet();
    private AvailabilityArg cAvailability = AvailabilityArg.ANY;
    private HashSet<Section> cSections = new HashSet();
    private HashSet<Session> cSessions = new HashSet();
    private HashSet<InstructionalMethod> cInstructionalMethods = new HashSet();
    private HashSet<Instructor> cInstructors = new HashSet();
    private ArrayList<Section> cResults = new ArrayList();
    private HashSet<SectionsQueryObserver> cObservers = new HashSet();
    
    //************************************************************************************************
    //***************************************Constructors*****************************************
    //************************************************************************************************
    /**
     * Constructs a new <code>SectionsQuery</code> without specifying a term or course. 
     * After construction, <code>results()</code> will be empty.
     */
    public SectionsQuery(){}
    /**
     * Constructs a new <code>SectionsQuery</code> withe the specified term and course. 
     * After construction, <code>results()</code> will contain all sections belonging to the specified term and course.
     * @param pTerm the <code>Term</code> the sections in <code>results()</code> must belong to. A null value will cause <code>results()</code> to be empty.
     * @param pCourse the <code>Course</code> the sections in <code>results()</code> must belong to. A null value will cause <code>results()</code> to be empty.
     */    
    public SectionsQuery(Term pTerm, Course pCourse){
        cTerm = pTerm;
        cCourse = pCourse;
        this.resetBase();
        this.resetResults();
    }
    
    //************************************************************************************************
    //***************************************Private Helpers*****************************************
    //************************************************************************************************
    private void resetBase(){
        if (cTerm != null && cCourse != null)
            cBaseSections = Sections.getByCourse1(cTerm, cCourse);
        else
            cBaseSections = new ArrayList();
    }
    
    /**
     * This should be called when a change would have become less restrictive, such as adding a <code>Campus</code> or setting availability to ALL.
     * This method does not call notifyObservers.
     */
    private void resetResults(){
        cResults.clear();
        cResults.addAll(cBaseSections);        
    }
    
    private void lessRestriction(){
        int countBefore = cResults.size();
        this.resetBase();
        this.resetResults();
        this.restrictOnAll();   
        if (!(countBefore == 0 && this.cResults.isEmpty()))
            this.notifyObservers(); 
    }
    /**
     * calls each "restrictOnXXX" method and returns true if one of the  restrictOnXXX methods returned true.
     * @return true if one of the  restrictOnXXX methods returned true.
     */
    private boolean restrictOnAll(){
        boolean modified = false;
        if(cResults.isEmpty()){
            return false ;
        }
        modified = (restrictOnSections()) ? true : modified;
        modified = (restrictOnSessions()) ? true : modified;
        modified = (restrictOnInstructionalMethods()) ? true : modified;
        modified = (restrictOnInstructors()) ? true : modified;
        modified = (restrictOnCampuses()) ? true : modified;
        modified = (restrictOnAvailability()) ? true : modified;
        modified = (restrictOnDayTimeArgs()) ? true : modified;
        
        return modified;
    }
    /**
     * @return true if this operation removed sections from the <code>Section</code> Results Set.
     */    
    private boolean restrictOnSections(){
        boolean modified = false;
        if(!cSections.isEmpty()){
            for (Iterator<Section> resultsItr = cResults.iterator();resultsItr.hasNext();){
                Section resultsSec = resultsItr.next();
                if (!cSections.contains(resultsSec)){
                    resultsItr.remove();
                    modified = true;
                }
            }
        }
        return modified;
    }

    /**
     * @return true if this operation removed sections from the <code>Section</code> Results Set.
     */ 
    private boolean restrictOnSessions(){
        boolean modified = false;
        if(!cSessions.isEmpty()){
            for (Iterator<Section> resultsItr = cResults.iterator();resultsItr.hasNext();){
                Section resultsSec = resultsItr.next();
                if (!cSessions.contains(resultsSec.session())){
                    resultsItr.remove();
                    modified = true;
                }
            }
        }
        return modified;
    }
    /**
     * @return true if this operation removed sections from the <code>Section</code> Results Set.
     */ 
    private boolean restrictOnInstructionalMethods(){
        boolean modified = false;
        if(!cInstructionalMethods.isEmpty()){
            for (Iterator<Section> resultsItr = cResults.iterator();resultsItr.hasNext();){
                Section resultsSec = resultsItr.next();
                if (!cInstructionalMethods.contains(resultsSec.instructionalMethod())){
                    resultsItr.remove();
                    modified = true;
                }
            }
        }
        return modified;
    }
    /**
     * @return true if this operation removed sections from the <code>Section</code> Results Set.
     */ 
    private boolean restrictOnInstructors(){
        boolean modified = false;
        if(!cInstructors.isEmpty()){
            boolean found;
            for (Iterator<Section> resultsItr = cResults.iterator();resultsItr.hasNext();){
                found=false;
                Instructor[] secInstructors = resultsItr.next().instructors2();
                for(int i = 0; i < secInstructors.length && !found; i++){
                    found = cInstructors.contains(secInstructors[i]);                 
                }
                if(!found){
                    resultsItr.remove();
                    modified = true;
                }
            }
        }
        return modified;
    }
    /**
     * @return true if this operation removed sections from the <code>Section</code> Results Set.
     */  
    private boolean restrictOnCampuses(){
        boolean modified = false;
        if(!cCampuses.isEmpty()){
            for (Iterator<Section> resultsItr = cResults.iterator();resultsItr.hasNext();){
                Section resultsSec = resultsItr.next();
                if (resultsSec.campus() != null && !cCampuses.contains(resultsSec.campus())){
                    resultsItr.remove();
                    modified = true;
                }
            }
        }
        return modified;
    }
    /**
    /**
     * @return true if this operation removed sections from the <code>Section</code> Results Set.
     */    
    private boolean restrictOnAvailability(){
        boolean modified = false;
        if(cAvailability == AvailabilityArg.OPEN_SEATS){
            for (Iterator<Section> resultsItr = cResults.iterator();resultsItr.hasNext();){
                Section resultsSec = resultsItr.next();
                if (resultsSec.seatsAvailable() < 1){
                    resultsItr.remove();
                    modified = true;
                }
            }            
        } else if (cAvailability == AvailabilityArg.OPEN_WAITLIST){
            for (Iterator<Section> resultsItr = cResults.iterator();resultsItr.hasNext();){
                Section resultsSec = resultsItr.next();
                if (resultsSec.seatsAvailable() < 1 && resultsSec.waitlistAvailable() < 1){
                    resultsItr.remove();
                    modified = true;
                }
            }               
        }
        return modified;
    }  
    /**
     * @return true if this operation removed sections from the <code>Section</code> Results Set.
     */  
    private boolean restrictOnDayTimeArgs(){
        boolean modified = false;
        for (Iterator<Section> resultsItr = cResults.iterator();resultsItr.hasNext();){
            Section resultsSec = resultsItr.next();
            boolean failed = false;
            MeetingTime[] secMeetings = resultsSec.meetings2();
            for (int i = 0; i < secMeetings.length && !failed; i++){
                DayOfWeek[] daysOfWeek = secMeetings[i].days2();
                for (int j = 0; j < daysOfWeek.length && !failed; j++){
                    DayTimeArg foundDayTimeArg = cDayTimeArgs.get(daysOfWeek[j]);
                    if(foundDayTimeArg == null)
                        failed = true;
                    else
                        if ((foundDayTimeArg.cMinStart != null && secMeetings[i].startTime().lessThan(foundDayTimeArg.cMinStart)) || (foundDayTimeArg.cMaxEnd != null && secMeetings[i].endTime().greaterThan(foundDayTimeArg.cMaxEnd)))
                            failed = true;
                }
            }
            if (failed){
                resultsItr.remove();
                modified = true;
            }
        }
        return modified;
    }
    //************************************************************************************************
    //***************************************Observer*****************************************
    //************************************************************************************************    
    private void notifyObservers(){
        for(SectionsQueryObserver obs : this.cObservers)
            obs.resultsChanged(this);
    }
    
    /**
     * Adds a {@link uscheduler.util.SectionsQueryObserver SectionsQueryObserver} to this <code>SectionsQuery</code>. 
     * A <code>SectionsQueryObserver</code> object <code>sqo[i]</code> added to this <code>SectionsQuery</code> will be notified when <code>results()</code> 
     * changes via a call to {@link uscheduler.util.SectionsQueryObserver#resultsChanged(uscheduler.util.SectionsQuery) resultsChanged}.
     * @param pObserver the <code>SectionsQueryObserver</code> to be added to this <code>SectionsQuery</code>. Not null.
     */
    public void addObserver(SectionsQueryObserver pObserver){
        if(pObserver==null)
            throw new IllegalArgumentException("Null pObserver argument");
        this.cObservers.add(pObserver);
    }
    /**
     * Removes a {@link uscheduler.util.SectionsQueryObserver SectionsQueryObserver} from this <code>SectionsQuery</code>. 
     * Removing a <code>SectionsQueryObserver</code> from this <code>SectionsQuery</code> means the <code>SectionsQueryObserver</code> will no longer be notified when <code>results()</code> changes.
     * @param pObserver the <code>SectionsQueryObserver</code> to be removed from this <code>SectionsQuery</code>.
     * @see <code>{@link #addObserver(uscheduler.util.SectionsQueryObserver) addObserver}</code>
     */
    public void removeObserver(SectionsQueryObserver pObserver){
        this.cObservers.remove(pObserver);
    }
    /**
     * Removes this <code>SectionsQuery</code>, which implements {@link uscheduler.util.DayTimeArgObserver DayTimeArgObserver}, 
     * from each <code>DayTimeArg</code> in {@link #dayTimeArgs() dayTimeArgs()}. 
     * <p>This class adds itself as an <code>DayTimeArgObserver</code> to each <code>DayTimeArg</code> in {@link #dayTimeArgs() dayTimeArgs()} 
     * so that it can be notified of changes to the <code>DayTimeArg</code> and update <code>results()</code>. 
     * It is important that users of this class call {@link #close() close()} 
     * when finished with an instance so that the <code>SectionsQuery</code> can 
     * remove itself as a <code>DayTimeArgObserver</code> from each <code>DayTimeArg</code> in <code>dayTimeArgs()</code> and no longer be notified of changes.
     */
    public void close(){
        for (DayTimeArg dta : this.cDayTimeArgs.values())
            dta.removeObserver(this);
    }
    //************************************************************************************************
    //***************************************Data Modification*****************************************
    //************************************************************************************************
    /**
     * Sets the <code>Term</code> of this <code>SectionsQuery</code>, recalculates <code>results()</code>, and notifies each <code>SectionsQueryObserver</code> if <code>results()</code> changed.
     * @param pTerm the <code>Term</code> the sections in <code>results()</code> must belong to. A null value will cause <code>results()</code> to be empty.
     * .
     */
    public void setTerm(Term pTerm){
        if(cTerm != pTerm){
            cTerm = pTerm;
            int countBefore = cResults.size();
            this.resetBase();
            this.resetResults();
            this.restrictOnAll();
            if (!(countBefore == 0 && cResults.isEmpty()))
                this.notifyObservers();
        }
    }
    /**
     * Sets the <code>Course</code> of this <code>SectionsQuery</code>, recalculates <code>results()</code>, and notifies each <code>SectionsQueryObserver</code> if <code>results()</code> changed..
     * @param pCourse the <code>Course</code> the sections in <code>results()</code> must belong to. A null value will cause <code>results()</code> to be empty.
     */
    public void setCourse(Course pCourse){
        if(cCourse != pCourse){
            cCourse = pCourse;
            int countBefore = cResults.size();
            this.resetBase();
            this.resetResults();
            this.restrictOnAll();   
            if (!(countBefore == 0 && this.cResults.isEmpty()))
                this.notifyObservers();   
        }
    }
    /**
     * Sets the Availability Restriction of this <code>SectionsQuery</code>, recalculates <code>results()</code>, and notifies each <code>SectionsQueryObserver</code> if <code>results()</code> changed.
     * 
     * @param pAvailability the Availability Restriction of this <code>SectionsQuery</code>. Not null.
     */
    public void setAvailability(AvailabilityArg pAvailability){
       if(this.cAvailability != pAvailability){
           
            if(pAvailability.lessThan(this.cAvailability)){
               //the new argument is "less restrictive"
                this.cAvailability = pAvailability;
                this.lessRestriction();
            } else {
               //the new argument is "more restrictive
                this.cAvailability = pAvailability;
                if (this.restrictOnAvailability())
                    this.notifyObservers(); 
           }
       }
    }
    /**
    * Adds the provided <code>DayTimeArg</code> to the DayTimeArg Restriction List if no <code>DayTimeArg</code> with the same {@link uscheduler.util.SectionsQuery.DayTimeArg#dayOfWek() dayOfWek()} already in it, recalculates <code>results()</code>, and notifies each <code>SectionsQueryObserver</code> if <code>results()</code> changed.
    * 
    * @param pDayTimeArg the <code>DayTimeArg</code> to be added to this list if no <code>DayTimeArg</code> with the same
    * {@link uscheduler.util.SectionsQuery.DayTimeArg#dayOfWek() dayOfWek()} already in it. Not null.
    */ 
    public void addDayTimeArg(DayTimeArg pDayTimeArg){
       if(!this.cDayTimeArgs.containsKey(pDayTimeArg.cDay)){
           this.cDayTimeArgs.put(pDayTimeArg.cDay, pDayTimeArg);
           pDayTimeArg.addObserver(this);
           //less restrictive
            this.lessRestriction();
       }
    }
    /**
    * Removes the <code>DayTimeArg</code> from the DayTimeArg Restriction List if in it, recalculates <code>results()</code>, and notifies each <code>SectionsQueryObserver</code> if <code>results()</code> changed.
    * 
    * @param pDayTimeArg the <code>DayTimeArg</code> to remove from this list if in it. Not null.
    */ 
    public void removeDayTimeArg(DayTimeArg pDayTimeArg){
       if(this.cDayTimeArgs.containsKey(pDayTimeArg.cDay)){
           this.cDayTimeArgs.remove(pDayTimeArg.cDay);
           pDayTimeArg.removeObserver(this);
           //more restrictive
           if(this.restrictOnDayTimeArgs())
               this.notifyObservers();
       }
    }
    /**
    * Removes all <code>DayTimeArgs</code> from the DayTimeArg Restriction List, recalculates <code>results()</code>, and notifies each <code>SectionsQueryObserver</code> if <code>results()</code> changed.
    */ 
    public void removeDayAllTimeArgs(){
        if(!cDayTimeArgs.isEmpty()){
            for(DayTimeArg dta : cDayTimeArgs.values())
                dta.removeObserver(this);
            cDayTimeArgs.clear();
            //became more restrictive
            this.restrictOnDayTimeArgs();
            this.notifyObservers();
       }
    }
    
    /**
    * Adds the provided <code>Campus</code> to the Campus Restriction List if not already in it, recalculates <code>results()</code>, and notifies each <code>SectionsQueryObserver</code> if <code>results()</code> changed.
    * 
    * @param pCampus the <code>Campus</code> to be added to this list  if not already in it. Not null.
    */  
    public void addCampus(Campus pCampus){
        if (pCampus == null)
            throw new IllegalArgumentException("Null pCampus argument.");
       if(this.cCampuses.add(pCampus)){
            if(this.cCampuses.size()==1)
               //more restrictive (Was 0, now 1)
               if(this.restrictOnCampuses())
                   this.notifyObservers();
            else
               //less restrictive
                this.lessRestriction();
       }
    }
    /**
    * Removes the <code>Campus</code> from the the Campus Restriction List if in it, recalculates <code>results()</code>, and notifies each <code>SectionsQueryObserver</code> if <code>results()</code> changed.
    * 
    * @param pCampus the <code>Campus</code> to remove from this list if in it. Not null.
    */  
    public void removeCampus(Campus pCampus){
       if(this.cCampuses.remove(pCampus)){
            if(this.cCampuses.isEmpty())
                //less restrictive (Was 1, now 0)
                this.lessRestriction();
            else
               //more restrictive
               if(this.restrictOnCampuses())
                   this.notifyObservers();
       }
    }
    /**
    * Removes all <code>Campuses</code> from the Campus Restriction List, recalculates <code>results()</code>, and notifies each <code>SectionsQueryObserver</code> if <code>results()</code> changed.
    */ 
    public void removeAllCampuses(){
        if(!cCampuses.isEmpty()){
            cCampuses.clear();
            lessRestriction();
            notifyObservers();
        }
    }
    
    /**
    * Adds the provided <code>Section</code> to the Section Restriction List if not in it, recalculates <code>results()</code>, and notifies each <code>SectionsQueryObserver</code> if <code>results()</code> changed.
    * 
    * @param pSection the <code>Section</code> to be added to this list if not already in it. Not null.
    */  
    public void addSection(Section pSection){
        if (pSection == null)
            throw new IllegalArgumentException("Null pSection argument.");
       if(this.cSections.add(pSection)){
            if(this.cSections.size()==1)
               //more restrictive (Was 0, now 1)
               if(this.restrictOnSections())
                   this.notifyObservers();
            else
               //less restrictive
                this.lessRestriction();
       }
    }
    /**
    * Removes the <code>Section</code> from the Section Restriction List if in it, recalculates <code>results()</code>, and notifies each <code>SectionsQueryObserver</code> if <code>results()</code> changed.
    * 
    * @param pSection the <code>Section</code> to remove from this list if in it. Not null.
    */ 
    public void removeSection(Section pSection){
       if(this.cSections.remove(pSection)){
            if(this.cSections.isEmpty())
                //less restrictive (Was 1, now 0)
                this.lessRestriction();
            else
               //more restrictive
               if(this.restrictOnSections())
                   this.notifyObservers();
       }
    }

    /**
    * Removes all <code>Sections</code> from the Section Restriction List, recalculates <code>results()</code>, and notifies each <code>SectionsQueryObserver</code> if <code>results()</code> changed.
    */ 
    public void removeAllSections(){
        if(!cSections.isEmpty()){
            cSections.clear();
            lessRestriction();
            notifyObservers();
        }
    }
    
    /**
    * Adds the provided <code>Session</code> to the Session Restriction List if not already in it, recalculates <code>results()</code>, and notifies each <code>SectionsQueryObserver</code> if <code>results()</code> changed.
    * 
    * @param pSession the <code>Session</code> to be added to this list if not already in it. Not null.
    */  
    public void addSession(Session pSession){
        if (pSession == null)
            throw new IllegalArgumentException("Null pSession argument.");
       if(this.cSessions.add(pSession)){
            if(this.cSessions.size()==1)
               //more restrictive (Was 0, now 1)
               if(this.restrictOnSessions())
                   this.notifyObservers();
            else
               //less restrictive
                this.lessRestriction();
       }
    }
    /**
    * Removes the provided <code>Session</code> from the Session Restriction List if in it, recalculates <code>results()</code>, and notifies each <code>SectionsQueryObserver</code> if <code>results()</code> changed.
    * 
    * @param pSession the <code>Session</code> to remove from this list if in it. Not null.
    */  
    public void removeSession(Session pSession){
       if(this.cSessions.remove(pSession)){
            if(this.cSessions.isEmpty())
                //less restrictive (Was 1, now 0)
                this.lessRestriction();
            else
               //more restrictive
               if(this.restrictOnSessions())
                   this.notifyObservers();
       }
    }
    /**
    * Removes all <code>Sessions</code> from the Session Restriction List, recalculates <code>results()</code>, and notifies each <code>SectionsQueryObserver</code> if <code>results()</code> changed.
    */ 
    public void removeAllSessions(){
        if(!cSessions.isEmpty()){
            cSessions.clear();
            lessRestriction();
            notifyObservers();
        }
    }
    
    /**
    * Adds the provided <code>InstructionalMethod</code> to the InstructionalMethod Restriction List if not already in it, recalculates <code>results()</code>, and notifies each <code>SectionsQueryObserver</code> if <code>results()</code> changed.
    * 
    * @param pInstructionalMethod the <code>InstructionalMethod</code> to be added to this list if not already in it. Not null.
    */  
    public void addInstructionalMethod(InstructionalMethod pInstructionalMethod){
        if (pInstructionalMethod == null)
            throw new IllegalArgumentException("Null pInstructionalMethod argument.");
       if(this.cInstructionalMethods.add(pInstructionalMethod)){
            if(this.cInstructionalMethods.size()==1)
               //more restrictive (Was 0, now 1)
               if(this.restrictOnInstructionalMethods())
                   this.notifyObservers();
            else
               //less restrictive
                this.lessRestriction();
       }
    }
    /**
    * Removes the provided <code>InstructionalMethod</code> from the InstructionalMethod Restriction List if in it, recalculates <code>results()</code>, and notifies each <code>SectionsQueryObserver</code> if <code>results()</code> changed.
    * 
    * @param pInstructionalMethod the <code>InstructionalMethod</code> to from this this list if in it. Not null.
    */  
    public void removeInstructionalMethod(InstructionalMethod pInstructionalMethod){
       if(this.cInstructionalMethods.remove(pInstructionalMethod)){
            if(this.cInstructionalMethods.isEmpty())
                //less restrictive (Was 1, now 0)
                this.lessRestriction();
            else
               //more restrictive
               if(this.restrictOnInstructionalMethods())
                   this.notifyObservers();
       }
    }
    /**
    * Removes all <code>InstructionalMethods</code> from the InstructionalMethod Restriction List, recalculates <code>results()</code>, and notifies each <code>SectionsQueryObserver</code> if <code>results()</code> changed.
    */ 
    public void removeAllInstructionalMethods(){
        if(!cInstructionalMethods.isEmpty()){
            cInstructionalMethods.clear();
            lessRestriction();
            notifyObservers();
        }
    }
    
    
    /**
    * Adds the provided <code>Instructor</code> to the Instructor Restriction List if not already in it, recalculates <code>results()</code>, and notifies each <code>SectionsQueryObserver</code> if <code>results()</code> changed.
    * 
    * @param pInstructor the <code>Instructor</code> to be added to this list if not already in it. Not null.
    */  
    public void addInstructor(Instructor pInstructor){
        if (pInstructor == null)
            throw new IllegalArgumentException("Null pInstructor argument.");
       if(this.cInstructors.add(pInstructor)){
            if(this.cInstructors.size()==1)
               //more restrictive (Was 0, now 1)
               if(this.restrictOnInstructors())
                   this.notifyObservers();
            else
               //less restrictive
                this.lessRestriction();
       }
    }
    /**
    * Removes the provided <code>Instructor</code> from the Instructor Restriction List if in it, recalculates <code>results()</code>, and notifies each <code>SectionsQueryObserver</code> if <code>results()</code> changed.
    * 
    * @param pInstructor the <code>Instructor</code> to remove from this list if in it. Not null.
    */      
    public void removeInstructor(Instructor pInstructor){
       if(this.cInstructors.remove(pInstructor)){
            if(this.cInstructors.isEmpty())
                //less restrictive (Was 1, now 0)
                this.lessRestriction();
            else
               //more restrictive
               if(this.restrictOnInstructors())
                   this.notifyObservers();
       }
    }

    /**
    * Removes all <code>Instructors</code> from the Instructor Restriction List, recalculates <code>results()</code>, and notifies each <code>SectionsQueryObserver</code> if <code>results()</code> changed.
    */ 
    public void removeAllInstructors(){
        if(!cInstructors.isEmpty()){
            cInstructors.clear();
            lessRestriction();
            notifyObservers();
        }
    }
    
    
    //************************************************************************************************
    //***************************************Querying*************************************************
    //************************************************************************************************  
    
    /**
     * Returns the <code>Term</code> of this <code>SectionsQuery</code>. 
     * See {@link uscheduler.util.SectionsQuery class documentation} at top for a details on how <code>Term</code> affects <code>results()</code>.
     * @return the <code>Term</code> of this <code>SectionsQuery</code>.
     */
    public Term term(){return this.cTerm;}
    /**
     * Returns the <code>Course</code> of this <code>SectionsQuery</code>. 
     * See {@link uscheduler.util.SectionsQuery class documentation} at top for a details on how <code>Course</code> affects <code>results()</code>.
     * @return the <code>Course</code> of this <code>SectionsQuery</code>.
     */
    public Course course(){return this.cCourse;}
    /**
     * Returns the <code>AvailabilityArg</code> of this <code>SectionsQuery</code>. 
     * See {@link uscheduler.util.SectionsQuery class documentation} at top for a details on how <code>AvailabilityArg</code> affects <code>results()</code>.
     * @return the <code>AvailabilityArg</code> of this <code>SectionsQuery</code>.
     */
    public AvailabilityArg availability(){return this.cAvailability;}
    /**
     * Returns from the Day Time Restriction List, the <code>DayTimeArg</code> whose {@link uscheduler.util.SectionsQuery.DayTimeArg#dayOfWek()  dayOfWek()} is <code>pDayOfWeek</code>. 
     * If no such <code>DayTimeArg</code> exists in the Day Time Restriction List, null is returned.
     * <p>See {@link uscheduler.util.SectionsQuery class documentation} at top for a details on how the Day Time Restriction List affects <code>results()</code>.
     * @param pDayOfWeek the <code>DayOfWeek</code> of the <code>DayTimeArg</code> to return from  the Day Time Restriction List.
     * @return the <code>DayTimeArg</code> whose <code>dayOfWek()</code> is <code>pDayOfWeek</code> or null if no such <code>DayTimeArg</code> exists.
     */
    public DayTimeArg getDayTimeArg(DayOfWeek pDayOfWeek){return this.cDayTimeArgs.get(pDayOfWeek);}
    /**
     * Returns a read-only <code>Collection</code> view of this <code>SectionQuery's</code> Day Time Restriction List.
     * The <code>Collection</code> is backed by this <code>SectionQuery's</code> <code>HashMap</code> used 
     * to represent the Day Time Restriction List, so changes to the Day Time Restriction List are reflected in the <code>Collection</code>.  
     * If Day Time Restriction List is modified while an iteration over the <code>Collection</code> is in progress, the results of the iteration are undefined. 
     * <p>See {@link uscheduler.util.SectionsQuery class documentation} at top for a details on how the Day Time Restriction List affects <code>results()</code>.
     * 
     * @return a read-only <code>Collection</code> view of this <code>SectionQuery's</code> Day Time Restriction List.
     */
    public Collection<DayTimeArg> dayTimeArgs(){return Collections.unmodifiableCollection(this.cDayTimeArgs.values());}
    /**
     * Returns a read-only <code>Set</code> view of this <code>SectionQuery's</code> Campus Restriction List.
     * The <code>Set</code> is backed by this <code>SectionQuery's</code> <code>HashSet</code> used 
     * to represent the Campus Restriction List, so changes to the Campus Restriction List are reflected in the <code>Set</code>.  
     * If Campus Restriction List is modified while an iteration over the <code>Set</code> is in progress, the results of the iteration are undefined. 
     * <p>See {@link uscheduler.util.SectionsQuery class documentation} at top for a details on how the Campus Restriction List affects <code>results()</code>.
     * 
     * @return a read-only <code>Set</code> view of this <code>SectionQuery's</code> Campus Restriction List.
     */
    public Set<Campus> campuses(){return Collections.unmodifiableSet(this.cCampuses);}
    /**
     * Returns a read-only <code>Set</code> view of this <code>SectionQuery's</code> Section Restriction List.
     * The <code>Set</code> is backed by this <code>SectionQuery's</code> <code>HashSet</code> used 
     * to represent the Section Restriction List, so changes to the Section Restriction List are reflected in the <code>Set</code>.  
     * If Section Restriction List is modified while an iteration over the <code>Set</code> is in progress, the results of the iteration are undefined. 
     * <p>See {@link uscheduler.util.SectionsQuery class documentation} at top for a details on how the Section Restriction List affects <code>results()</code>.
     * 
     * @return a read-only <code>Set</code> view of this <code>SectionQuery's</code> Section Restriction List.
     */
    public Set<Section> sections(){return Collections.unmodifiableSet(this.cSections);}
    /**
     * Returns a read-only <code>Set</code> view of this <code>SectionQuery's</code> Session Restriction List.
     * The <code>Set</code> is backed by this <code>SectionQuery's</code> <code>HashSet</code> used 
     * to represent the Session Restriction List, so changes to the Session Restriction List are reflected in the <code>Set</code>.  
     * If Session Restriction List is modified while an iteration over the <code>Set</code> is in progress, the results of the iteration are undefined. 
     * <p>See {@link uscheduler.util.SectionsQuery class documentation} at top for a details on how the Session Restriction List affects <code>results()</code>.
     * 
     * @return a read-only <code>Set</code> view of this <code>SectionQuery's</code> Session Restriction List.
     */
    public Set<Session> sessions(){return Collections.unmodifiableSet(this.cSessions);}
    /**
     * Returns a read-only <code>Set</code> view of this <code>SectionQuery's</code> InstructionalMethod Restriction List.
     * The <code>Set</code> is backed by this <code>SectionQuery's</code> <code>HashSet</code> used 
     * to represent the InstructionalMethod Restriction List, so changes to the InstructionalMethod Restriction List are reflected in the <code>Set</code>.  
     * If InstructionalMethod Restriction List is modified while an iteration over the <code>Set</code> is in progress, the results of the iteration are undefined. 
     * <p>See {@link uscheduler.util.SectionsQuery class documentation} at top for a details on how the InstructionalMethod Restriction List affects <code>results()</code>.
     * 
     * @return a read-only <code>Set</code> view of this <code>SectionQuery's</code> InstructionalMethod Restriction List.
     */
    public Set<InstructionalMethod> instructionalMethods(){return Collections.unmodifiableSet(this.cInstructionalMethods);}
    /**
     * Returns a read-only <code>Set</code> view of this <code>SectionQuery's</code> Instructor Restriction List.
     * The <code>Set</code> is backed by this <code>SectionQuery's</code> <code>HashSet</code> used 
     * to represent the Instructor Restriction List, so changes to the Instructor Restriction List are reflected in the <code>Set</code>.  
     * If Instructor Restriction List is modified while an iteration over the <code>Set</code> is in progress, the results of the iteration are undefined. 
     * <p>See {@link uscheduler.util.SectionsQuery class documentation} at top for a details on how the Instructor Restriction List affects <code>results()</code>.
     * 
     * @return a read-only <code>Set</code> view of this <code>SectionQuery's</code> Instructor Restriction List.
     */
    public Set<Instructor> instructors(){return Collections.unmodifiableSet(this.cInstructors);}
    
    /**
     * Returns a <code>List</code> of all sections that meet the criteria provided to this <code>SectionQuery</code>.
     * 
     * The returned <code>List</code> is a read-only view of this <code>SectionQuery's</code> results.
     * The <code>List</code> is backed by this <code>SectionQuery's</code> <code>LinkedList</code> used 
     * to store the section results, so changes to the results are reflected in the <code>List</code>.  
     * If results list is modified while an iteration over the <code>List</code> is in progress, the results of the iteration are undefined. 
     * <p>See {@link uscheduler.util.SectionsQuery class documentation} at top for a details on how the various criteria affects <code>results()</code>.
     * 
     * @return a read-only <code>List</code> view of this <code>SectionQuery's</code> results.
     */    
    public List<Section> results1(){return Collections.unmodifiableList(this.cResults);}

    public Section[] results2(){return this.cResults.toArray(new Section[cResults.size()]);}
    
    /**
     * Returns the number of sections in {@link #results() results()}.
     * 
     * <p>See {@link uscheduler.util.SectionsQuery class documentation} at top for a details on how the various criteria affects <code>results()</code>.
     * 
     * @return the number of sections in <code>results()</code>.
     */  
    public int resultsSize(){return this.cResults.size();}

    /**
     * The method that will be called by the DayTimeArg, on each DayTimeArgObserver object in its observers list, 
     * when its {@link uscheduler.util.SectionsQuery.DayTimeArg#maxEnd() maxEnd()} value changes.
     * 
     * @param pDTA the  DayTimeArg object whose maxEnd value changed.
     * @param pOldMaxEnd the value of maxEnd before it changed.
     */
    
    /**
     * Implementation of <code>DayTimeArgObserver</code> interface to receive notifications from a <code>DayTimeArg</code> 
     * in the Day Time Restrictions List when its {@link uscheduler.util.SectionsQuery.DayTimeArg#maxEnd() DayTimeArg.maxEnd()} value changes.
     * This method recalculates <code>results()</code> based on the changes made to <code>maxEnd()</code>, 
     * and notifies each <code>SectionsQueryObserver</code> if <code>results()</code> changed.
     * <p>See: {@link uscheduler.util.DayTimeArgObserver#maxEndChanged(uscheduler.util.SectionsQuery.DayTimeArg, uscheduler.global.UTime) DayTimeArgObserver.maxEndChanged}
     * @param pDTA see <code>DayTimeArgObserver.maxEndChanged</code>
     * @param pOldMaxEnd see <code>DayTimeArgObserver.maxEndChanged</code>
     * 
     */
    @Override
    public void maxEndChanged(DayTimeArg pDTA, UTime pOldMaxEnd) {
        System.out.println("In maxEndChanged - Entered Method");
        if(pDTA.cMaxEnd == null || (pOldMaxEnd != null && pOldMaxEnd.lessThan(pDTA.cMaxEnd))) {
            //less restrictive
            System.out.println("In maxEndChanged - Less Restriction");
            this.lessRestriction();
        }else {
            System.out.println("In maxEndChanged - More Restriction");
            if (this.restrictOnDayTimeArgs())
                this.notifyObservers();
        }
    }
    /**
     * Implementation of <code>DayTimeArgObserver</code> interface to receive notifications from a <code>DayTimeArg</code> 
     * in the Day Time Restrictions List when its {@link uscheduler.util.SectionsQuery.DayTimeArg#minStart() DayTimeArg.minStart()} value changes.
     * This method recalculates <code>results()</code> based on the changes made to <code>minStart()</code>, 
     * and notifies each <code>SectionsQueryObserver</code> if <code>results()</code> changed.
     * <p>See: {@link uscheduler.util.DayTimeArgObserver#minStartChanged(uscheduler.util.SectionsQuery.DayTimeArg, uscheduler.global.UTime) DayTimeArgObserver.minStartChanged}
     * @param pDTA see <code>DayTimeArgObserver.minStartChanged</code>
     * @param pOldMinStart see <code>DayTimeArgObserver.minStartChanged</code>
     * 
     */
    @Override
    public void minStartChanged(DayTimeArg pDTA, UTime pOldMinStart) {
        if(pDTA.minStart() == null || (pOldMinStart != null && pDTA.cMinStart.lessThan(pOldMinStart)))
            //less restrictive
            this.lessRestriction();
        else 
            if(this.restrictOnDayTimeArgs())
                this.notifyObservers();
    }
    
    /**
     * <code>AvailabilityArg</code> is an enum representing the 3 possible values of a <code>SectionQuery's</code> {@link #availability() availability()} restriction.  
     * @author Matt Bush
     */
    public static enum AvailabilityArg {
        /**
         * Used to specify a Availability Restriction in a <code>SectionsQuery</code>. 
         * See {@link uscheduler.util.SectionsQuery SectionsQuery} for details of how it uses this value.
         */
        OPEN_SEATS ("Open Seats", 3),
        /**
         * Used to specify a Availability Restriction in a <code>SectionsQuery</code>. 
         * See {@link uscheduler.util.SectionsQuery SectionsQuery} for details of how it uses this value.
         */
        OPEN_WAITLIST ("Open Waitlist", 2),
        /**
         * Used to specify a Availability Restriction in a <code>SectionsQuery</code>. 
         * See {@link uscheduler.util.SectionsQuery SectionsQuery} for details of how it uses this value.
         */
        ANY ("Any", 1);

        private final String cStringValue;
        private final int cIntValue;
        AvailabilityArg(String pStringValue, int pIntValue) {
            this.cStringValue = pStringValue;
            this.cIntValue = pIntValue;
        }

        @Override
        public String toString(){return cStringValue;}
        
        /**
         * Returns true if this AvailabilityArg is less than the other AvailabilityArg, where less than is defined as: ANY &lt; OPEN_WAITLIST &lt; OPEN_SEATS.
         * The less than logic is based on ANY is less restrictive than OPEN_WAITLIST is less restrictive than  OPEN_SEATS.
         * @param pOther  the other AvailabilityArg in which to test if this AvailabilityArg is less than. Not null. 
         * @return true if this AvailabilityArg is less than the other AvailabilityArg, where less than is defined as: ANY &lt; OPEN_WAITLIST &lt; OPEN_SEATS.
         */
        public boolean lessThan(AvailabilityArg pOther){
            return this.cIntValue < pOther.cIntValue;
        }
    }
    /**
     * This class is used to specify a Day Time Restriction in a <code>SectionsQuery</code>. 
     * A Day Time Restriction consist of a {@link #dayOfWek() dayOfWeek}, {@link #minStart()  minStart}, and {@link #maxEnd()  maxEnd} 
     * corresponding to restriction criteria for a section's {@link uscheduler.internaldata.Sections.Section.MeetingTime MeetingTime}. 
     * While the <code>minStart()</code> and <code>maxEnd()</code> of a <code>DayTimeArg</code> can change, <code>dayOfWeek()</code> is immutable.
     * 
    * <p>This class also provides a means for a {@link uscheduler.util.DayTimeArgObserver DayTimeArgObserver} 
    * to add itself as an observer to an object of this class via {@link #addObserver(uscheduler.util.DayTimeArgObserver)  addObserver}. 
    * The <code>DayTimeArgObserver</code> will be notified when <code>minStart()</code> and <code>maxEnd()</code>changes.
     * See {@link uscheduler.util.SectionsQuery SectionsQuery} for details of how it uses a <code>DayTimeArg</code>.
     */
    public static class DayTimeArg{
        /**
         * Must be final for <code>SectionsQuery</code> to use as key in HashSet.
         */
        private final DayOfWeek cDay;
        private UTime cMinStart = null;
        private UTime cMaxEnd = null;
        private final HashSet<DayTimeArgObserver> cObservers = new HashSet();

        //************************************************************************************************
        //***************************************Constructors*****************************************
        //************************************************************************************************
        /**
         * Constructs a new <code>DayTimeArg</code> with the specified <code>DayOfWeek</code>.
         * See {@link uscheduler.util.SectionsQuery class documentation} at top for details on how it uses this value in its Day Time Restrictions Lists.
         * @param pDay the <code>DayOfWeek</code> of the new <code>DayTimeArg</code>. Not Null.
         */
        public DayTimeArg(DayOfWeek pDay){
            if(pDay==null)
                throw new IllegalArgumentException("Null pDay argument");
            this.cDay = pDay;
            this.cMinStart = null;
            this.cMaxEnd = null;
        }
        /**
         * Constructs a new <code>DayTimeArg</code> with the specified <code>DayOfWeek</code>, <code>minStart</code>, and <code>maxEnd</code>.
         * See {@link uscheduler.util.SectionsQuery class documentation} at top for details on how it uses these value in its Day Time Restrictions Lists.
         * @param pDay the <code>DayOfWeek</code> of the new <code>DayTimeArg</code>. Not Null.
         * @param pMinStart the <code>minStart</code> of the new <code>DayTimeArg</code>. 
         * @param pMaxEnd the <code>maxEnd</code> of the new <code>DayTimeArg</code>.
         */
        public DayTimeArg(DayOfWeek pDay, UTime pMinStart, UTime pMaxEnd){
            if(pDay==null)
                throw new IllegalArgumentException("Null pDay argument");
            this.cDay = pDay;
            this.cMinStart = pMinStart;
            this.cMaxEnd = pMaxEnd;
        }
        //************************************************************************************************
        //***************************************Observer*****************************************
        //************************************************************************************************
        private void notifyObserversMinChange(UTime pOldMin){
            for(DayTimeArgObserver obs : this.cObservers)
                obs.minStartChanged(this, pOldMin);
        } 
        private void notifyObserversMaxChange(UTime pOldMax){
            for(DayTimeArgObserver obs : this.cObservers)
                obs.maxEndChanged(this, pOldMax);
        } 
    
        /**
         * Adds a {@link uscheduler.util.DayTimeArgObserver DayTimeArgObserver} to this <code>DayTimeArg</code>. 
         * A <code>DayTimeArgObserver</code> object <code>dto[i]</code> added to this <code>DayTimeArg</code> 
         * will be notified when <code>minStart()</code> and <code>maxEnd()</code> changes 
         * via calls to {@link #setMinStart(uscheduler.global.UTime) setMinStart} and {@link #setMaxEnd(uscheduler.global.UTime) setMaxEnd}.
         * @param pObserver the <code>DayTimeArgObserver</code> to be added to this <code>DayTimeArg</code>. Not null.
         */
        public void addObserver(DayTimeArgObserver pObserver){
            if(pObserver==null)
                throw new IllegalArgumentException("Null pObserver argument");
            this.cObservers.add(pObserver);
        }
        /**
         * Removes a {@link uscheduler.util.DayTimeArgObserver DayTimeArgObserver} from this <code>DayTimeArg</code>. 
         * Removing a <code>DayTimeArgObserver</code> from this <code>DayTimeArg</code> means the <code>DayTimeArgObserver</code> 
         * will no longer be notified when <code>minStart()</code> and <code>maxEnd()</code> changes.
         * @param pObserver the <code>DayTimeArgObserver</code> to be removed from this <code>DayTimeArg</code>.
         */
        public void removeObserver(DayTimeArgObserver pObserver){
            this.cObservers.remove(pObserver);
        }
        //************************************************************************************************
        //***************************************Data Manipulation*****************************************
        //************************************************************************************************
        /**
         * Sets the <code>minStart</code> of this <code>DayTimeArg</code> 
         * to the specified value if different than the current <code>minStart</code> 
         * and notifies each <code>DayTimeArgObserver</code> if <code>minStart</code> changed 
         * by calling {@link uscheduler.util.DayTimeArgObserver#minStartChanged(uscheduler.util.SectionsQuery.DayTimeArg, uscheduler.global.UTime) minStartChanged}.
         * <p>see {@link uscheduler.util.SectionsQuery class documentation} at top for details on how it uses this value in its Day Time Restrictions List.
         * @param pMinStart the new <code>minStart</code> of this <code>DayTimeArg</code>.
         */
        public void setMinStart(UTime pMinStart){
            if (cMinStart != null){
                if(!cMinStart.equals(pMinStart)){
                    UTime oldValue = this.cMinStart;
                    this.cMinStart=pMinStart;
                    this.notifyObserversMinChange(oldValue);                  
                }                
            }else if(pMinStart != null){
                UTime oldValue = this.cMinStart;
                this.cMinStart=pMinStart;
                this.notifyObserversMinChange(oldValue);                  
            }
        }
        
        /**
         * Sets the <code>maxEnd</code> of this <code>DayTimeArg</code> 
         * to the specified value if different than the current <code>maxEnd</code> 
         * and notifies each <code>DayTimeArgObserver</code> if <code>maxEnd</code> changed 
         * by calling {@link uscheduler.util.DayTimeArgObserver#maxEndChanged(uscheduler.util.SectionsQuery.DayTimeArg, uscheduler.global.UTime) maxEndChanged}.
         * <p>see {@link uscheduler.util.SectionsQuery class documentation} at top for details on how it uses this value in its Day Time Restrictions List.
         * @param pMaxEnd the new <code>maxEnd</code> of this <code>DayTimeArg</code>.
         */
        public void setMaxEnd(UTime pMaxEnd){
            if (cMaxEnd != null){
                if(!cMaxEnd.equals(pMaxEnd)){
                    UTime oldValue = this.cMaxEnd;
                    this.cMaxEnd=pMaxEnd;
                    this.notifyObserversMaxChange(oldValue);
                }                
            }else if(pMaxEnd != null){
                UTime oldValue = this.cMaxEnd;
                this.cMaxEnd=pMaxEnd;
                this.notifyObserversMaxChange(oldValue);
            }
        }
        //************************************************************************************************
        //***************************************Getters*****************************************
        //************************************************************************************************
        /**
         * Returns the <code>dayOfWeek</code> of this <code>DayTimeArg</code>.
         * <p>see {@link uscheduler.util.SectionsQuery class documentation} at top for details on how it uses this value in its Day Time Restrictions List.
         * @return the <code>dayOfWeek</code> of this <code>DayTimeArg</code>
         */
        public DayOfWeek dayOfWek(){return this.cDay;}
        /**
         * Returns the <code>minStart</code> of this <code>DayTimeArg</code>.
         * <p>see {@link uscheduler.util.SectionsQuery class documentation} at top for details on how it uses this value in its Day Time Restrictions List.
         * @return the <code>minStart</code> of this <code>DayTimeArg</code>
         */
        public UTime minStart(){return this.cMinStart;}        
        /**
         * Returns the <code>maxEnd</code> of this <code>DayTimeArg</code>.
         * <p>see {@link uscheduler.util.SectionsQuery class documentation} at top for details on how it uses this value in its Day Time Restrictions List.
         * @return the <code>maxEnd</code> of this <code>DayTimeArg</code>
         */
        public UTime maxEnd(){return this.cMaxEnd;}   

    }

}