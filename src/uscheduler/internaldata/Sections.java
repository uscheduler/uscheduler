/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uscheduler.internaldata;

import java.time.DayOfWeek;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.TreeSet;
import uscheduler.internaldata.Campuses.Campus;
import uscheduler.internaldata.Courses.Course;
import uscheduler.internaldata.Instructors.Instructor;
import uscheduler.internaldata.Sessions.Session;
import uscheduler.internaldata.Terms.Term;
import uscheduler.global.InstructionalMethod;
import uscheduler.global.UTime;


/**
 * A singleton class that models the Sections table to store Section records.
 
 * @author Matt Bush
 * 
 */
public final class Sections implements Table{    
    /**
     * The HashMap to store Section objects using the section's pkey() as the key into the map. 
     */
    private static final HashMap<String, Section> cSections = new HashMap();
    
    /**
     * The HashMap to store the term+course index, in which the key of the index is: t.pkey() + "~" + c.pkey()
     * The key maps to a linked list, which stores all Sections for the given term and course.
     * This HashMap is used to provide quick access to all sections of a given term and course.
     */
    private static final HashMap<String, LinkedList<Section>> cTermCourseIndex = new HashMap();
    
    /**
     * Private constructor to prevent instantiation and implement as a singleton class
     */
    private Sections(){};

    
    //************************************************************************************************
    //***************************************Data Modification*****************************************
    //************************************************************************************************

    /**
     * Adds a new Section to the Sections table.
     * <p>This method first checks to see if a Section already exists in the Sections table with the Session specified by pSession and the crn specified by pCrn. 
     * If so, this method returns the already existing Session. 
     * Otherwise, this method creates a new Course with the provided arguments,
     * adds the newly created Section to the Table, and returns the newly created Section.
     * 
     * @param pCrn the crn of the Section to construct
     * @param pSession the Session of the Section to construct
     * @param pCourse the Course of the Section to construct
     * @param pCampus the Campus of the Section to construct
     * @param pSectionNum the section number of the Section to construct
     * @param pIMethod the instructional method of the Section to construct
     * @param pSeatsAvail the seats available of the Section to construct
     * @param pWaitlistAvail the waitlist available of the Section to construct
     * @return the newly added Section if no such Section already existed, otherwise returns the already existing Section.
     * @throws IllegalArgumentException if pSession, pCourse,  pSectionNum, or pIMethod is null.
     * @throws IllegalArgumentException if pSeatsAvail or pWaitlistAvail is less than zero.
     */
    public static Section add(int pCrn, Session pSession, Course pCourse, Campus pCampus, String pSectionNum, InstructionalMethod pIMethod, int pSeatsAvail, int pWaitlistAvail){
         
        Section temp = new Section(pCrn,  pSession,  pCourse,  pCampus,  pSectionNum,  pIMethod,  pSeatsAvail,  pWaitlistAvail);
        Section found = cSections.get(temp.pkey());
        if (found == null){
            
            //Add the new section to the "table"
            cSections.put(temp.pkey(), temp);
            
            //Add the new section to the term+course index
            String key = pSession.term().pkey() + "~" + pCourse.pkey();
            LinkedList<Section> sectionsList = cTermCourseIndex.get(key);
            if (sectionsList == null){
                sectionsList = new LinkedList();
                sectionsList.add(temp);
                cTermCourseIndex.put(key, sectionsList); 
            } else {
                sectionsList.add(temp);
            }

            return temp;
        }
        return found;
    }

    //************************************************************************************************
    //***************************************Querying*************************************************
    //************************************************************************************************
    /**
     * 
     * @return the number of records in this table
     */
    public static int size(){
        return cSections.size();
    }
    
    /**
     * Returns from the Sections table the Section with the specified Session and crn
     * 
     * @param pSession the Session of the Section being retrieved.
     * @param pCrn the crn of the Section being retrieved.
     * @return the Sesction whose Session has the same pkey() as pSession.pkey() and whose crn is pCrn, if such a Section exists in the table. Null otherwise.
     */
    public static Section get(Session pSession, int pCrn){
        return cSections.get(pSession.pkey() + "~" + pCrn);
    }

    /**
     * Returns a list of all Sections in no particular order. This method is mostly for debugging and probably has no use in finished product.
     * 
     * @return A list of all Sections in no particular order.
     */
    public static ArrayList<Section> getAll(){
        ArrayList<Section> list = new ArrayList<>(cSections.values());
        return list;
    }
    /**
     * Returns from the Sections table, a list of all sections from the specified Term and Course, in the specified order. 
     * <br>
     * @param pTerm the Term of the sections to return. Not null.
     * @param pCourse the Course of the sections to return. Not null.
     * @param pOrder a Comparator of type Section that specifies how to order the returned list. Not null.
     * @return a list of all sections from the specified whose Terms is pTerm and whose Course is pCourse, in the order specified by pOrder.
     */
    public static ArrayList<Section> getByCourse(Term pTerm, Course pCourse, Comparator<Section> pOrder){
        String key = pTerm.pkey() + "~" + pCourse.pkey();
        LinkedList<Section> sectionsLinkedList = cTermCourseIndex.get(key);
        if (sectionsLinkedList == null){
            return new ArrayList<>();
        } else {
            ArrayList<Section> returnList = new ArrayList<>(sectionsLinkedList);
            Collections.sort(returnList, pOrder);
            return returnList;
        } 
    }
    /**
     * From a List of Sections, returns a LinkedList of distinct Instructors of the Sections in the List, ordered by instructor.pkey().
     * 
     * @param pSections the list of non null Sections from which to extract distinct Instructors. Not null.
     * @return a List of distinct Instructors of the Sections in pSections, ordered by instructor.pkey().
     */
    public static ArrayList<Instructor> getDistinctInstructors(List<Section> pSections){
        TreeSet<Instructor> instructorsTree = new TreeSet(Instructors.PK_ASC);
        for (Section s: pSections)
            for(Instructor i : s.instructors())
                instructorsTree.add(i);
        return new ArrayList<>(instructorsTree);
    }
    /**
     * From a List of Sections, returns a List of distinct Sessions of the Sections in the List, ordered by session.pkey().
     * 
     * @param pSections the list of non null Sections from which to extract distinct Sessions. Not null.
     * @return a List of distinct Sessions of the Sections in pSections, ordered by session.pkey().
     */
    public static ArrayList<Session> getDistinctSessions(List<Section> pSections){
        TreeSet<Session> sessionsTree = new TreeSet(Sessions.PK_ASC);
        for (Section s: pSections)
            sessionsTree.add(s.session());
        return new ArrayList<>(sessionsTree);
    }
    /**
     * From a List of Sections, returns a List of distinct InstructionalMethods of the Sections in the List, ordered by ???
     * 
     * @param pSections the list of non null Sections from which to extract distinct InstructionalMethods. Not null.
     * @return a List of distinct InstructionalMethods of the Sections in pSections, ordered by the constant's definition in {@link uscheduler.global.InstructionalMethod InstructionalMethod}
     */
    public static ArrayList<InstructionalMethod> getDistinctMethods(List<Section> pSections){
        TreeSet<InstructionalMethod> imTree = new TreeSet();
        for (Section s: pSections)
            imTree.add(s.instructionalMethod());
        return new ArrayList<>(imTree);
    }
    //************************************************************************************************
    //***************************************Comparators*********************************************
    //************************************************************************************************  

    /**
     * A Comparator of type Section that compares two Section objects based on sectionNumber().
     * This Comparator will allow ordering a Collection of Section objects by sectionNumber() ascending.
     * <br><br>
     * <b>Note:</b> This Comparator is NOT consistent with equals() in the sense that this Comparator's compare method is performed on a Section's section number, 
     * which is not the primary key of a Section. Thus the compare method will return 0 when performed on two Section objects with the same section number, 
     * even thought they are two different Sections. Thus, <b> DO NOT </b> use this Comparator in a TreeSet.
     */
    public static final Comparator<Section> SEC_NUM_ASC = new Comparator<Section>() {
            @Override
            public int compare(Section s1, Section s2) {
                return s1.cSectionNumber.compareTo(s2.cSectionNumber);
            }
    }; 
    //************************************************************************************************
    //***************************************Record Class*********************************************
    //************************************************************************************************  
    /**
     * Models a Section record in the Sections table.
     * To simplify the implementation of data constraints and ensuring data integrity, 
     * Section objects are immutable and only the Sections class can create instances of this type and add them to the Sections table.
     * @author Matt Bush
     */
    public static class Section implements Record{
        private final int cCrn;
        private final Session cSession;
        private final Course cCourse;
        private final Campus cCampus;
        private final String cSectionNumber;
        private final InstructionalMethod cInstructionalMethod;
        private final int cSeatsAvailable;
        private final int cWaitlistAvailable;
        /**
         * The HashSet used to store a Section's Instructor objects, using the fact that 
         * no two logically equivalent instances of an Instructor can exist that are not also strictly equal in the sense they reference the same object in memory.
         * As long as the Instructors class prevents access to references of Instructor object that are NOT in its table, 
         */
        private final HashSet<Instructor> cInstructors;
        /**
         * The HashSet used to store a Section's MeetingTime objects, using MeetingTime's definition of hashCode and equals to ensure no duplicates.
         * MeetingTime MUST override both equals() and hashCode() on {section, startTime, endTime} in order to have the intended effect in a any HashSet.
         */
        private final HashSet<MeetingTime> cMeetings; //cMeetings May contain zero MeetingTimes, but it must NOT be null

        /**
         * Constructs a new section with the specified arguments.
         * 
         * Private constructor only to be used by Sections class ensures no instances of Section will exist outside of Sections' HashMap.
         * 
         * @param pCrn the crn of the Section to construct
         * @param pSession the Session of the Section to construct
         * @param pCourse the Course of the Section to construct
         * @param pCampus the Campus of the Section to construct
         * @param pSectionNum the section number of the Section to construct
         * @param pIMethod the instructional method of the Section to construct
         * @param pSeatsAvail the seats available of the Section to construct
         * @param pWaitlistAvail the waitlist available of the Section to construct
         * @throws IllegalArgumentException if pSession, pCourse,  pSectionNum, or pIMethod is null.
         * @throws IllegalArgumentException if pSeatsAvail or pWaitlistAvail is less than zero.
         */
        private Section (int pCrn, Session pSession, Course pCourse, Campus pCampus, String pSectionNum, InstructionalMethod pIMethod, int pSeatsAvail, int pWaitlistAvail) {
            if (pSession == null)
                throw new IllegalArgumentException("A Section's session cannot be null.");
            if (pCourse == null)
                throw new IllegalArgumentException("A Section's course cannot be null.");
            if (pSectionNum == null)
                throw new IllegalArgumentException("A Section's section number cannot be null.");
            if (pIMethod == null)
                throw new IllegalArgumentException("A Section's instructional method cannot be null.");
            if (pSeatsAvail < 0)
                throw new IllegalArgumentException("A Section's seats available cannot be less than zero.");
            if (pWaitlistAvail < 0)
                throw new IllegalArgumentException("A Section's waitlist available cannot be less than zero.");

            cCrn = pCrn;
            cSession = pSession;
            cCourse = pCourse;
            cSectionNumber = pSectionNum;
            cInstructionalMethod = pIMethod;
            cSeatsAvailable = pSeatsAvail;
            cWaitlistAvailable = pWaitlistAvail;
            cCampus = pCampus;
            cInstructors = new HashSet<>();
            cMeetings = new HashSet();
        }
        /**
         * Constructs a new MeetingTime with the specified arguments and adds it to this Section's set of MeetingTimes.
         * <p>More specifically,
         * <br><b>1)</b> Constructs a new MeetingTime with the specified arguments that is a child of this instance
         * <br><b>2)</b>  Tests to see if a logically equal MeetingTime already exists in this Section's set of MeetingTimes. 
         * <br>If such a Meeting time already exists, 
         * then gets a reference to the existing MeetingTime mt and calls mt.addDay(d) with each DayOfWeek in the collection of DayOfWeeks provided as an argument to this method.
         * A reference to the already existing MeetingTime is then returned and no new MeetingTime gets added to this Section's set of MeetingTimes.
         * <br>If NO such a Meeting time already exists, then adds the constructed MeetingTime to this Section's set of MeetingTimes and the returns the new MeetingTime.
         * 
         *<p><b>NOTE:</b> If a logically equal meeting time already exists and days are added to it, 
         * no checks are made by this method or the addDay(d) method to ensure this Section is not in a Schedule which would become invalid by the addition of the DayOfWeek(s).
         * 
         * @param pStartTime the start time of the MeetingTime to construct
         * @param pEndTime the end time of the MeetingTime to construct
         * @param pDays a list of DayOfWeek of the MeetingTime  to construct
         * @return the constructed MeetingTime if no such MeetingTime already existed in this Section's MeetingTimes set, or the existing MeetingTime otherwise.
         * @throws IllegalArgumentException if pStartT, pEndT, or pDays is null.
         * @throws IllegalArgumentException if !pStartT.lessThan(pEndT).
         * @throws IllegalArgumentException if pDays.isEmpty() or if for any DayOfWekk dow in pDaya, dow == null.
         */
        public MeetingTime addMeetingTime(UTime pStartTime, UTime pEndTime, List<DayOfWeek> pDays){
            
            MeetingTime newMT = new MeetingTime(this, pStartTime, pEndTime, pDays);
            
            for(MeetingTime existingMT : this.cMeetings)
                if (newMT.equals(existingMT)){
                    //Specified MeetingTime already in this Section's set of meeting thime
                    for (DayOfWeek dow : pDays)
                        existingMT.addDay(dow);
                    return existingMT;
                }
            
            //Specified MeetingTime din NOT already exist in this Section's set of meeting thime
            this.cMeetings.add(newMT);
            
            return newMT;
        }
        /**
         * Adds an Instructor to this Section. 
         * More specifically, this method ensures the specified Instructor is in this Section's Instructors set. 
         * If the specified Instructor is not already in this Section's instructors set, then this method adds the specified Instructor to the set and returns true.
         * Otherwise, this method does not modify this Section's instructors set and return false..
         *
         * @param pInstructor the Instructor to add to this Section's instructors set
         * @return true if the specified Instructor not already in the set, false otherwise
         * @throws IllegalArgumentException if pInstructor is null.
         */
        public boolean addInstructor(Instructor pInstructor) {
            if (pInstructor == null)
                throw new IllegalArgumentException("A Section's Instructor list cannot contain a null Instructor.");
            return this.cInstructors.add(pInstructor);
        }
        /**
         * Returns true if this Section overlaps with the provided other Section. 
         * Two Sections S1 and S2 overlap if S1.session().overlaps(S2.session()) AND (There exists any MeetingTime M1 in S1 and MeetingTime M2 in S2 such that M1.overlaps(M2))

         * @param other the Section to which to compare this one for overlap
         * @return true if this Section overlaps with the Section specified by other. Returns false otherwise, or if other is null.
         */
        public boolean overlaps(Section other){
            if (other == null)
                return false;
            if (this.cMeetings.isEmpty() || other.cMeetings.isEmpty())
                return false;
            if (!this.cSession.overlaps(other.cSession))
                return false;
            for (MeetingTime meetingsThis : this.cMeetings)
                for (MeetingTime meetingsOther : other.cMeetings)
                    if (meetingsThis.overlaps(meetingsOther))
                        return true;
            return false;
        }
        
        
        /**
         * @return the Section's crn
         */
        public int crn(){return cCrn;}
        /**
         * @return the Section's session
         */
        public Session session(){return cSession;}
        /**
         * @return the Section's course
         */
        public Course course(){return cCourse;}
        /**
         * @return the Section's campus, or null
         */
        public Campus campus(){return cCampus;}
        /**
         * @return the Section's section number
         */
        public String sectionNumber(){return cSectionNumber;}
        /**
         * @return the Section's crn
         */
        public InstructionalMethod instructionalMethod(){return cInstructionalMethod;}
        /**
         * @return the Section's seats available
         */
        public int seatsAvailable(){return cSeatsAvailable;}
        /**
         * @return the Section's seats available
         */
        public int waitlistAvailable(){return cWaitlistAvailable;}
        /**
         * @return a list of this Section's MeetingTimes, in no particular order
         */
        public ArrayList<MeetingTime> meetings(){
            return new ArrayList<>(cMeetings);
        }  
        /**
         * @return a list of this Section's Instructors, in no particular order
         */
        public ArrayList<Instructor> instructors(){
            return new ArrayList<>(cInstructors);
        }  
               
        /**
         * @return a String consisting of the name of each Instructor in this Section, separated by a comma and space.
         */
        public String instructorsString(){
            if(this.cInstructors.isEmpty())
                return "";
            
            StringBuilder sb = new StringBuilder();
            Iterator<Instructor> instructorsIT = this.cInstructors.iterator();
            sb.append(instructorsIT.next().instructorName());
            while(instructorsIT.hasNext())
                sb.append(", ").append(instructorsIT.next().instructorName());
            return sb.toString();
        }
        
        @Override
        public String toString(){
            return "Section[session=" + cSession + ", course=" + cCourse + ", secNum=" + 
                    cSectionNumber + ", crn=" + cCrn + ", seatsAvail=" + cSeatsAvailable + 
                    ", waitlistAvail=" + cWaitlistAvailable + ", method=" + cInstructionalMethod + 
                    ", campus=" + cCampus + ", meetings=" + cMeetings + ", instructors=" + cInstructors + "]";
        }
        /**
         * @return the section's primary key value, which is session().pkey() + "~" + crn()
         */
        public String pkey() {
            return this.cSession.pkey() + "~" + this.cCrn;
        }
    }
    /**
     * Models a MeetingTime of a Section. Although MeetingTime extends Record, and an instance of a MeetingTime can be considered a record, there is no corresponding MeetingTimes table. 
     * <br>
     * The decision not have a corresponding MeetingTimes table was based on the following:
     * <br>
     * 1) Unlike entities such as a Campus, Course, Term, Session, or Instructor, a MeetingTime only has meaning within the context of a particular instance of a Section. (i.e. A MeetingTime is only associated with one Section)
     * <br>
     * 2) The data constraints of a Section and its corresponding MeetingTime are highly interdependent (i.e. the validity of a Section is dependent of its MeetingTimes and vice versa). Thus, it makes sense for one class, the Sections class, to ensure the integrity of both.
     * <br>
     * 3) Lastly, it simplified the implementation of the guaranteeing that ALL aspects of a Section object are immutable, including its relationships / collections.
     * <br><br>
     * All aspects of a Section object are virtually immutable, including its collections of Instructor and MeetingTime objects and including each MeetingTime object's collection of DayOfWeek objects. Additionally, only the Sections class can create instances of a Section and MeetingTime and only the Sections class can add a Section to its table.
     * These restrictions ensure than no properties of a Section can change once a Section becomes associated with a Schedule, potentially invalidating the Schedule.
     * @author Matt Bush
     */
    public static class MeetingTime implements Record{
        private final Section cSection;
        private final UTime cStartTime;
        private final UTime cEndTime;
        /**
         * The HashSet used to store a MeetingTime's DayOfWeek objects, using DayOfWeek's parent Enum definition of hashCode and equals to ensure no duplicates.
        */
        private final HashSet<DayOfWeek> cDays;

        /**
         * +++++++++++++++++++++++++++++++++++++++++++This is the one to keep++++++++++++++++++++++++++++++++++++++++
         * Constructs a new MeetingTime with the specified Section, startTime, endTime, and days of week.
         * 
         * @param pSection the Section of the MeetingTime to construct
         * @param pStartT the start time of the MeetingTime to construct
         * @param pEndT the end time of the MeetingTime to construct
         * @param pDays a list containing the days of week of this MeetingTime
         * @throws IllegalArgumentException if pSection, pStartT, pEndT, or pDays is null.
         * @throws IllegalArgumentException if !pStartT.lessThan(pEndT).
         * @throws IllegalArgumentException if pDays.isEmpty() or if for any DayOfWekk dow in pDaya, dow == null.
         */
        private MeetingTime (Section pSection, UTime pStartT, UTime pEndT, List<DayOfWeek> pDays) {
            if (pSection == null)
                throw new IllegalArgumentException("A MeetingTime's section cannot be null.");
            if (pStartT == null)
                throw new IllegalArgumentException("A MeetingTime's start time cannot be null.");
            if (pEndT == null)
                throw new IllegalArgumentException("A MeetingTime's end time cannot be null.");
            if (!pStartT.lessThan(pEndT))
                throw new IllegalArgumentException("A MeetingTime's start time must be less than its end time.");
            if (pDays == null || pDays.isEmpty())
                throw new IllegalArgumentException("A MeetingTime must have at least one day.");
            
            cDays = new HashSet();
            cSection = pSection;
            cStartTime = pStartT;
            cEndTime = pEndT;
            
            for(DayOfWeek d : pDays)
                this.addDay(d);
        }

        /**
         * Adds a DayOfWeek to this MeetingTime. 
         * More specifically, this method ensures the specified DayOfWeek is in this MeetingTime's days set. 
         * If the specified DayOfWeek is not already in this MeetingTime's days set, then this method adds the specified DayOfWeek to the set and returns true.
         * Otherwise, this method does not modify this MeetingTime's days set and return false..
         * 
         *<p><b>NOTE:</b> No checks are made by this method to ensure this MeetingTime's corresponding Section is not in 
         * a Schedule which would become invalid by the addition of this DayOfWeek.
         * 
         * @param pDay the DayOfWeek to add to this MeetingTime's days set
         * @return true if the specified DayOfWeek not already in the set, false otherwise
         * @throws IllegalArgumentException if pDay is null.
         */
        private boolean addDay(DayOfWeek pDay) {
            if (pDay == null)
                throw new IllegalArgumentException("A MeetingTime's days set cannot contain a null DayOfWeek.");
            return cDays.add(pDay);
        }
        /**
         * Returns true if this MeetingTime overlaps with the provided MeetingTime. 
         * Two MeetingTimes M1 and M2 overlap if (M1.startTime &lt; M2.endTime AND M1.endTime &gt; M2.startTime) 
         * AND (There exists any DayOfWeek D1 in M1 and DayOfWeek D2 in M2 such that D1 = D2). 
         * 
         * @param other the MeetingTime to which to compare this one for overlap. Not null.
         * @return true if this MeetingTime overlaps with the MeetingTime specified by other.
         */
        boolean overlaps(MeetingTime other){
            if (this.cStartTime.lessThan(other.cEndTime) && this.cEndTime.greaterThan(other.cStartTime))
                for(DayOfWeek thisDay : this.cDays)
                    if (other.cDays.contains(thisDay))
                        return true;
            return false;
        }
                
        /**
         * @return the Section to which this MeetingTime applies
         */
        public Section section(){return cSection;}
        /**
         * @return this MeetingTime's start time
         */
        public UTime startTime(){return cStartTime;}
        /**
         * @return this MeetingTime's end time
         */
        public UTime endTime(){return cEndTime;}
        /**
         * @return an ordered list of this MeetingTime's days, where order is defined by the DayOfWeek enum.
         */
        public ArrayList<DayOfWeek> days(){
            ArrayList<DayOfWeek> daysAL = new ArrayList<>(cDays);
            Collections.sort(daysAL);
            return daysAL;
        }  
        /**
         * @return a String consisting of the short display name of each DayOfWeek in this MeetingTime, separated by a comma and space.
         */
        public String daysString(){
            if(this.cDays.isEmpty())
                return "";
            
            StringBuilder sb = new StringBuilder();
            Iterator<DayOfWeek> daysIT = this.cDays.iterator();
            sb.append(daysIT.next().getDisplayName(TextStyle.SHORT, Locale.getDefault()));
            while(daysIT.hasNext())
                sb.append(", ").append(daysIT.next().getDisplayName(TextStyle.SHORT, Locale.getDefault()));
            return sb.toString();
        }  
        /**
         * Returns true if this MeetingTime is equal to the provided object.
         * If obj is null or obj isn't an instance of MeetingTime, false is returned. 
         * Otherwise, this MeetingTime is equal to the other MeetingTime if and only if their Sections, start times, and end times are equal.
         * Equality of two UTime objects must be based on UTime's overriden equals() method, and not the default method of Object.
         * <br>
         * <br>
         * Unlike other record classes, it is critical that MeetingTime override equals() and hashCode. 
         * With other record classes, their corresponding table Class ensures that no two instances will exist that are equal based on 
         * the record's primary key value by using their primary key value as a key in a HashMap. 
         * With a MeetingTime, there is no corresponding table class and the primary storage set of a MeetingTime is a Section's meetingTime HashSet, not a map whose key is based on primary key.
         * 
         * @return true if this MeetingTime is equal to the provided object.
         */
        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final MeetingTime other = (MeetingTime) obj;
            if (!this.cSection.equals(other.cSection)) {
                return false;
            }
            if (!this.cStartTime.equals(other.cStartTime)) {
                return false;
            }
            return this.cEndTime.equals(other.cEndTime);
        }
        /**
         * Returns this MeetingTime's hash code which is based on its Section, startTime, and endTime.
         * <br>
         * <br>
         * Unlike other record classes, it is critical that MeetingTime override equals() and hashCode. 
         * With other record classes, their corresponding table Class ensures that no two instances will exist that are equal based on 
         * the record's primary key value by using their primary key value as a key in a HashMap. 
         * With a MeetingTime, there is no corresponding table class and the primary storage set of a MeetingTime is a Section's meetingTime HashSet, not a map whose key is based on primary key.
         * 
         * @return this MeetingTime's hash code.
         */
        @Override
        public int hashCode(){
            int hash = 17;
            hash = hash * 31 + cSection.hashCode();
            hash = hash * 31 + cStartTime.hashCode();
            hash = hash * 31 + cEndTime.hashCode();
            return hash;
        }
        /**
         * @return "MeetingTime[startTime=" + cStartTime + ", endTime=" + cEndTime + ", days=" + cDays +  "]"
         */
        @Override
        public String toString(){
            return "MeetingTime[startTime=" + cStartTime + ", endTime=" + cEndTime + ", days=" + cDays + "]";
        }
        /**
         * @return the MeetingTime's primary key value, which is cSection.pkey() + "~" + cStartTime + "~" + cEndTime
         */
        public String pkey() {
            return cSection.pkey() + "~" + cStartTime + "~" + cEndTime;
        }
    }
}
