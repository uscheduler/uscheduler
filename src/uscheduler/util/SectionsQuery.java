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
import java.util.Comparator;
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
import uscheduler.internaldata.Sections.MeetingTime;
import uscheduler.internaldata.Sections.Section;
import uscheduler.internaldata.Sessions.Session;
import uscheduler.internaldata.Terms.Term;


/**
 * A class used to model the complex query that produces a set of Section. 
 * 
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
    
    public SectionsQuery(){}
    
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
            cBaseSections = Sections.getByCourseReadOnly(cTerm, cCourse);
        else
            cBaseSections = new ArrayList();
    }
    
    /**
     * This should be called when a change would have become less restrictive, such as removing a campus or setting availability to ALL.
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
     * 
     * @return true if this method removed sections from the result set
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
     * The Allowed Sections set is empty: The MatchingSections set will not be restricted by Sections.
     * The Allowed Sections set is not empty: The MatchingSections set will be restricted so that each Section in it is also in the Allowed Sections set.
     * 
     * @return true if this operation removed sections from the results set.
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
     * The Allowed Sessions set is empty: The MatchingSections set will not be restricted by Sessions.
     * The Allowed Sessions set is not empty: The MatchingSections set will be restricted so that each Section in it, has a Sessions that is in the Allowed Sessions set.
     * 
     * @return true if this operation removed sections from the results set.
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
     * The Allowed InstructionalMethods set is empty: The MatchingSections set will not be restricted by InstructionalMethod.
     * The Allowed InstructionalMethods set is not empty: The MatchingSections set will be restricted so that each Section in it, has an InstructionalMethod that is in the InstructionalMethods set.
     * 
     * @return true if this operation removed sections from the results set.
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
     * The Allowed Instructors set is empty: The MatchingSections set will not be restricted by Instructor.
     * The Allowed Instructors set is not empty: The MatchingSections set will be restricted so that each Section in it, has at least one Instructor in the Allowed Instructors set.
     * 
     * @return true if this operation removed sections from the results set.
     */    
    private boolean restrictOnInstructors(){
        boolean modified = false;
        if(!cInstructors.isEmpty()){
            boolean found;
            for (Iterator<Section> resultsItr = cResults.iterator();resultsItr.hasNext();){
                found=false;
                Instructor[] secInstructors = resultsItr.next().instructors();
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
     * The Allowed Campuses set is empty: The MatchingSections set will not be restricted by Campus.
     * The Allowed Campuses set is not empty: The MatchingSections set will be restricted so that each Section in it, 
     * has either a null Campus or has a Campus that is in the Allowed Campuses set.
     * 
     * @return true if this operation removed sections from the results set.
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
     * 
     * @return true if this operation removed sections from the results set.
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
                if (resultsSec.waitlistAvailable() < 1){
                    resultsItr.remove();
                    modified = true;
                }
            }               
        }
        return modified;
    }
    /**
     * For each section in the MatchingSections set, if the section has MeetingTimes, then for each DayOfWeek of each of each MeetingTime, 
     * there must be a corresponding DayTimeArg with the same day of week. 
     * Additionally, if the corresponding DayTimeArg has a non-null minStart, 
     * then the Section cannot have a MeetingTime on the given DayOfWeek such that the MeetingTime's startTime less than minStart.
     * Additionally, if the corresponding DayTimeArg has a non-null maxEnd, 
     * then the Section cannot have a MeetingTime on the given DayOfWeek such that the MeetingTime's endTime is greater than less than maxEnd.
     * 
     * @return true if this operation removed sections from the results set.
     */    
    private boolean restrictOnDayTimeArgs(){
        boolean modified = false;
        for (Iterator<Section> resultsItr = cResults.iterator();resultsItr.hasNext();){
            Section resultsSec = resultsItr.next();
            boolean failed = false;
            MeetingTime[] secMeetings = resultsSec.meetings();
            for (int i = 0; i < secMeetings.length && !failed; i++){
                DayOfWeek[] daysOfWeek = secMeetings[i].days();
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
     * Adds an observer to this SectionsQuery. 
     * Observers of this SectionsQuery will be notified when this SectionsQuery's results set changes.
     * @param pObserver the SectionsQueryObserver to be added to this SectionsQuery's set of observers.
     */
    public void addObserver(SectionsQueryObserver pObserver){
        this.cObservers.add(pObserver);
    }
    /**
     * Removes an observer from this SectionsQuery. 
     * Observers of this SectionsQuery will be notified when this SectionsQuery's results set changes.
     * Removing an observer from this SectionsQuery means the observer will no longer be notified of changes.
     * @param pObserver the SectionsQueryObserver to be removed from this SectionsQuery's set of observers.
     */
    public void removeObserver(SectionsQueryObserver pObserver){
        this.cObservers.remove(pObserver);
    }
    /**
     * Removes this SectionsQuery as an observer of each of its corresponding DayTimeArgs.
     * <p>This class adds itself as a DayTimeArgObserver to each DayTimeArg added to this SectionsQuery through the addDayTimeArg method 
     * and removes itself as an observer of a DayTimeArg when the DayTimeArg is removed from this SectionsQuery through removeDayTimeArg method.
     * <p><b>This method should be called when a reference to it is no longer needed by the creator of this instance</b> 
     */
    public void close(){
        for (DayTimeArg dta : this.cDayTimeArgs.values())
            dta.removeObserver(this);
    }
    //************************************************************************************************
    //***************************************Data Modification*****************************************
    //************************************************************************************************
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
     * Attempts to add a DayTimeArg to this SectionsQuery. 
     * <p>More specifically, adds a DayTimeArg to this SectionsQuery if a DayTimeArg with the same DayOfWeek as the 
     * specified DayTimeArg doesn't already exist in this SectionsQuery's set of DayTimeArgs.
     * ALso, if the DayTimeArg is added to this SectionsQuery, then this method also adds this SectionsQuery to the specified DayTimeArg's list 
     * of observers so that this SectionsQuery will recalculate its results set upon modification of such a DayTimeArg.
     * 
     * <p>
     * A DayTimeArg affects this SectionsQuery's results in the following way:
     * For each section in the results set, if the section has MeetingTimes, then for each DayOfWeek of each of each MeetingTime, 
     * there must be a corresponding DayTimeArg with the same day of week. 
     * Additionally, if the corresponding DayTimeArg has a non-null minStart, 
     * then the Section cannot have a MeetingTime on the given DayOfWeek such that the MeetingTime's startTime less than minStart.
     * Additionally, if the corresponding DayTimeArg has a non-null maxEnd, 
     * then the Section cannot have a MeetingTime on the given DayOfWeek such that the MeetingTime's endTime is greater than less than maxEnd.
     * 
     * 
     * @param pDayTimeArg the DayTimeArg to be added to this SectionsQuery if no DayTimeArg already exist in this SectionsQuery with the same DayOfWeek. Not null.
     */  
    public void addDayTimeArg(DayTimeArg pDayTimeArg){
       if(!this.cDayTimeArgs.containsKey(pDayTimeArg.cDay)){
           this.cDayTimeArgs.put(pDayTimeArg.cDay, pDayTimeArg);
           pDayTimeArg.addObserver(this);
           //less restrictive
            this.lessRestriction();
       }
    }
    public void removeDayTimeArg(DayTimeArg pDayTimeArg){
       if(this.cDayTimeArgs.containsKey(pDayTimeArg.cDay)){
           this.cDayTimeArgs.remove(pDayTimeArg.cDay);
           pDayTimeArg.removeObserver(this);
           //more restrictive
           if(this.restrictOnDayTimeArgs())
               this.notifyObservers();
           

       }
    }
    public void addCampus(Campus pCampus){
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
    public void addSection(Section pSection){
       if(this.cSections.add(pSection)){
            if(this.cSections.size()>=1) //NOTE:: Peter made adjustment here, does this make sense?  Because it fixed my issue.
               //more restrictive (Was 0, now 1)
               if(this.restrictOnSections())
                   this.notifyObservers();
            else
               //less restrictive
                this.lessRestriction();
       }
        //System.out.println("cResults are : " + cResults);
    }
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
    public void removeAllSections(){
        if(this.cSections.isEmpty())
            this.lessRestriction();
        else{
            this.cSections.clear();
            this.lessRestriction();
            this.notifyObservers();
        }
    }
    public void addSession(Session pSession){
       if(this.cSessions.add(pSession)){
            if(this.cSessions.size()>=1)//NOTE:: Peter made adjustment here, does this make sense?  Because it fixed my issue.
               //more restrictive (Was 0, now 1)
               if(this.restrictOnSessions())
                   this.notifyObservers();
            else
               //less restrictive
                this.lessRestriction();
       }
    }
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
    public void removeAllSessions(){
        if(this.cSessions.isEmpty())
            this.lessRestriction();
        else{
            this.cSessions.clear();
            this.lessRestriction();
            this.notifyObservers();
        }
    }
    public void addInstructionalMethod(InstructionalMethod pInstructionalMethod){
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
    public void removeAllInstructionalMethods(){
        if(this.instructionalMethods().isEmpty())
            this.lessRestriction();
        else{
            this.cInstructionalMethods.clear();
            this.lessRestriction();
            this.notifyObservers();
        }
    }

    public void addInstructor(Instructor pInstructor){
       if(this.cInstructors.add(pInstructor)){
            if(this.cInstructors.size()>=1)//NOTE:: Peter made adjustment here, does this make sense?  Because it fixed my issue.
               //more restrictive (Was 0, now 1)
               if(this.restrictOnInstructors())
                   this.notifyObservers();
            else
               //less restrictive
                this.lessRestriction();
       }
    }
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
    public void removeAllInstructors(){
        if(this.cInstructors.isEmpty())
            this.lessRestriction();
        else{
            this.cInstructors.clear();
            this.lessRestriction();
            this.notifyObservers();
        }
    }

    
    //************************************************************************************************
    //***************************************Querying*************************************************
    //************************************************************************************************    
    public Term term(){return this.cTerm;}
    public Course course(){return this.cCourse;}
    public AvailabilityArg availability(){return this.cAvailability;}
    public DayTimeArg getDayTimeArg(DayOfWeek pDayOfWeek){return this.cDayTimeArgs.get(pDayOfWeek);}
    public Collection<DayTimeArg> dayTimeArgs(){return Collections.unmodifiableCollection(this.cDayTimeArgs.values());}
    public Set<Campus> campuses(){return Collections.unmodifiableSet(this.cCampuses);}
    public Set<Section> sections(){return Collections.unmodifiableSet(this.cSections);}
    public Set<Session> sessions(){return Collections.unmodifiableSet(this.cSessions);}
    public Set<InstructionalMethod> instructionalMethods(){return Collections.unmodifiableSet(this.cInstructionalMethods);}
    public Set<Instructor> instructors(){return Collections.unmodifiableSet(this.cInstructors);}
    public List<Section> results(){return Collections.unmodifiableList(this.cResults);}
    public int resultsSize(){return this.cResults.size();}



    @Override
    public void maxEndChanged(DayTimeArg dta, UTime pOldMaxEnd) {
        if(dta.cMaxEnd == null || (pOldMaxEnd != null && pOldMaxEnd.lessThan(dta.cMaxEnd))) 
            //less restrictive
            this.lessRestriction();
        else 
            if(this.restrictOnDayTimeArgs())
                this.notifyObservers();

    }

    @Override
    public void minStartChanged(DayTimeArg dta, UTime pOldMinStart) {
        if(dta.minStart() == null || (pOldMinStart != null && dta.cMaxEnd.lessThan(pOldMinStart))) 
            //less restrictive
            this.lessRestriction();
        else 
            if(this.restrictOnDayTimeArgs())
                this.notifyObservers();
    }
    
    /**
     * 
     * @author Matt Bush
     */
    public static enum AvailabilityArg {

        OPEN_SEATS ("Open Seats", 3),
        OPEN_WAITLIST ("Open Waitlist", 2),
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
     * This class is used to model a day and time restriction argument in a {@link uscheduler.util.SectionsQuery SectionsQuery} object.
     */
    public static class DayTimeArg{
        /**
         * Must be final for SectionsQuery to use as key in HashSet.
         */
        private final DayOfWeek cDay;
        private UTime cMinStart = null;
        private UTime cMaxEnd = null;
        private HashSet<DayTimeArgObserver> cObservers = new HashSet();

        //************************************************************************************************
        //***************************************Constructors*****************************************
        //************************************************************************************************
        /**
         * Constructs a new DayTimeArg with the specified DayOfWeek.
         * @param pDay the DayOfWeek of the new DayTimeArg
         */
        public DayTimeArg(DayOfWeek pDay){
            this.cDay = pDay;
            this.cMinStart = null;
            this.cMaxEnd = null;
        }
        public DayTimeArg(DayOfWeek pDay, UTime pMinStart, UTime pMaxEnd){
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
         * Adds an observer to this DayTimeArg. 
         * Observers of this DayTimeArg will be notified when this DayTimeArg's minStart or maxEnd values are changed by calling the observer's timesChanged() method.
         * @param pObserver the DayTimeArgObserver to be added to this DayTimeArg's set of observers.
         */
        public void addObserver(DayTimeArgObserver pObserver){
            this.cObservers.add(pObserver);
        }
        /**
         * Removes an observer from this DayTimeArg. 
         * Observers of this DayTimeArg will be notified when this DayTimeArg's minStart or maxEnd values are changed by calling the observer's timesChanged() method.
         * Removing an observer from this DayTimeArg means the observer will no longer be notified of changes.
         * @param pObserver the DayTimeArgObserver to be removed from this DayTimeArg's set of observers.
         */
        public void removeObserver(DayTimeArgObserver pObserver){
            this.cObservers.remove(pObserver);
        }
        //************************************************************************************************
        //***************************************Data Manipulation*****************************************
        //************************************************************************************************
        /**
         * Sets the minStart time of this DayTimeArg if the specified new minStart is not equal to the current minStart.
         * @param pMinStart the new minStart time of this DayTimeArg
         */
        public void setMinStart(UTime pMinStart){
            if (!this.cMinStart.equals(pMinStart)){
                UTime oldValue = this.cMinStart;
                this.cMinStart=pMinStart;
                this.notifyObserversMinChange(oldValue);
            }
        }
        
        /**
         * Sets the maxEnd time of this DayTimeArg if the specified new maxEnd is not equal to the current maxEnd.
         * @param pMaxEnd the new maxEnd time of this DayTimeArg
         */
        public void setMaxEnd(UTime pMaxEnd){
            if (!this.cMaxEnd.equals(pMaxEnd)){
                UTime oldValue = this.cMaxEnd;
                this.cMaxEnd=pMaxEnd;
                this.notifyObserversMaxChange(oldValue);
            }
        }
        //************************************************************************************************
        //***************************************Getters*****************************************
        //************************************************************************************************
        /**
         * 
         * @return the DayOfWeek of this DayTimeArg
         */
        public DayOfWeek dayOfWek(){return this.cDay;}
        /**
         * 
         * @return the minStart of this DayTimeArg
         */
        public UTime minStart(){return this.cMinStart;}        
        /**
         * 
         * @return the maxEnd of this DayTimeArg
         */
        public UTime maxEnd(){return this.cMaxEnd;}   

    }

}