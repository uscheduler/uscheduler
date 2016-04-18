/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uscheduler.internaldata;

import java.time.DayOfWeek;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Set;
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
    private static final HashMap<String, Section> cSections = new HashMap<>();
    
    /**
     * The HashMap to store the term+course index, in which the key of the index is: t.pkey() + "~" + c.pkey()
     * The key maps to an ArrayList, which stores all Sections for the given term and course.
     * This HashMap is used to provide quick access to all sections of a given term and course.
     */
    private static final HashMap<String, ArrayList<Section>> cTermCourseIndex = new HashMap<>();
    
    
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
     * If so, this method returns the already existing Session and makes no modifications to the existing section.
     * Otherwise, this method creates a new Section with the provided arguments,
     * adds the newly created Section to the Table, and returns the newly created Section.
     * 
     * @param pCrn the crn of the Section to construct
     * @param pSession the Session of the Section to construct. Not null.
     * @param pCourse the Course of the Section to construct. Not null.
     * @param pCampus the Campus of the Section to construct
     * @param pSectionNum the section number of the Section to construct. Not null.
     * @param pIMethod the instructional method of the Section to construct. Not null.
     * @param pSeatsAvail the seats available of the Section to construct
     * @param pWaitlistAvail the waitlist available of the Section to construct
     * @param pInstructors the set of the instructors of the section to construct. Can be null, but must not contain nulls.
     * @param pMeetings the collection of UnattachedMeetingTimes that specify the section's meeting times. Can be null, but must not contain nulls.
     * @return the newly added Section if no such Section already existed, otherwise returns the already existing Section.
     * @throws IllegalArgumentException if pSession, pCourse,  pSectionNum, or pIMethod is null.
     * @throws IllegalArgumentException if pSeatsAvail or pWaitlistAvail is less than zero.
     */
    public static Section add(int pCrn, Session pSession, Course pCourse, Campus pCampus, String pSectionNum, InstructionalMethod pIMethod, 
            int pSeatsAvail, int pWaitlistAvail, Set<Instructor> pInstructors, Collection<UnattachedMeetingTime> pMeetings){
        
        Section temp = new Section(pCrn,  pSession,  pCourse,  pCampus,  pSectionNum,  pIMethod,  pSeatsAvail,  pWaitlistAvail);
        Section found = cSections.get(temp.pkey());
        if (found == null){
            //Add the provided instructors and meeting times to the new section
            if(pInstructors != null)
                for(Instructor inst : pInstructors)
                    temp.addInstructor(inst);
            if(pMeetings != null)
                for(UnattachedMeetingTime mtUnattached : pMeetings)
                    temp.addMeetingTime(mtUnattached.cStartTime, mtUnattached.cEndTime, mtUnattached.cDays);
            
            //Add the new section to the "table"
            cSections.put(temp.pkey(), temp);
            
            //Add the new section to the term+course index
            String key = pSession.term().pkey() + "~" + pCourse.pkey();
            ArrayList<Section> sectionsList = cTermCourseIndex.get(key);
            if (sectionsList == null){
                sectionsList = new ArrayList<>();
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

//    /**
//     * Returns a read-only list of all Sections in no particular order. This method is mostly for debugging and probably has no use in finished product.
//     * 
//     * @return A read-only list of all Sections in no particular order.
//     */
//    public static ArrayList<Section> getAll(){
//        return (ArrayList<Section>) Collections.unmodifiableList((List<Section>) cSections.values());
//    }
   
    /**
     * Returns from the Sections table, a read-only list of all sections from the specified Term and Course.
     * <br>
     * @param pTerm the Term of the sections to return. Not null.
     * @param pCourse the Course of the sections to return. Not null.
     * @return a read-only list of all sections whose Terms is pTerm and whose Course is pCourse.
     */
    public static List<Section> getByCourse1(Term pTerm, Course pCourse){
        
        List<Section> sectionsList = cTermCourseIndex.get(pTerm.pkey() + "~" + pCourse.pkey());
        if (sectionsList == null){
            return Collections.unmodifiableList(new ArrayList<Section>());
        } else {
            return Collections.unmodifiableList(sectionsList);
        } 
    }    
    /**
     * Returns from the Sections table, a read-only list of all sections from the specified Term and Course, in the specified order. 
     * <br>
     * @param pTerm the Term of the sections to return. Not null.
     * @param pCourse the Course of the sections to return. Not null.
     * @param pOrder a Comparator of type Section that specifies how to order the returned list. Not null.
     * @return a read-only list of all sections from the specified whose Terms is pTerm and whose Course is pCourse, in the order specified by pOrder.
     */
    public static List<Section> getByCourse1(Term pTerm, Course pCourse, Comparator<Section> pOrder){
        ArrayList<Section> sectionsList = cTermCourseIndex.get(pTerm.pkey() + "~" + pCourse.pkey());
        if (sectionsList == null){
            return Collections.unmodifiableList(new ArrayList<Section>());
        } else {
            Collections.sort(sectionsList, pOrder);
            return Collections.unmodifiableList(sectionsList);
        } 
    }
    /**
     * From a List of Sections, returns a LinkedList of distinct Instructors of the Sections in the List, ordered by ordered by {@link uscheduler.internaldata.Instructors.Instructor#pkey()  Instructor.pkey()}
     * 
     * @param pSections the list of non null Sections from which to extract distinct Instructors. Not null.
     * @return a List of distinct Instructors of the Sections in pSections, ordered by {@link uscheduler.internaldata.Instructors.Instructor#pkey()  Instructor.pkey()}
     */
    public static List<Instructor> getDistinctInstructors(List<Section> pSections){
        TreeSet<Instructor> instructorsTree = new TreeSet<>(Instructors.PK_ASC);
        for (Section s: pSections)
            for(Instructor i : s.instructors1())
                instructorsTree.add(i);
        return new ArrayList<>(instructorsTree);
    }
    /**
     * From a List of Sections, returns a List of distinct Sessions of the Sections in the List, ordered by {@link uscheduler.internaldata.Sessions.Session#pkey() Session.pkey()}
     * 
     * @param pSections the list of non null Sections from which to extract distinct Sessions. Not null.
     * @return a List of distinct Sessions of the Sections in pSections,ordered by {@link uscheduler.internaldata.Sessions.Session#pkey() Session.pkey()}
     */
    public static List<Session> getDistinctSessions(List<Section> pSections){
        TreeSet<Session> sessionsTree = new TreeSet<>(Sessions.PK_ASC);
        for (Section s: pSections)
            sessionsTree.add(s.session());
        return new ArrayList<>(sessionsTree);
    }
    /**
     * From a List of Sections, returns a List of distinct InstructionalMethods of the Sections in the List, ordered by the constant's  declaration in {@link uscheduler.global.InstructionalMethod InstructionalMethod}
     * 
     * @param pSections the list of non null Sections from which to extract distinct InstructionalMethods. Not null.
     * @return a List of distinct InstructionalMethods of the Sections in pSections, ordered by the constant's declaration in {@link uscheduler.global.InstructionalMethod InstructionalMethod}
     */
    public static List<InstructionalMethod> getDistinctMethods(List<Section> pSections){
        TreeSet<InstructionalMethod> imTree = new TreeSet<>();
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
    /**
     * A Comparator of type Section that compares two Section objects based on <code>Section.course().subject().subjectAbbr()</code>
     * This Comparator will allow ordering a Collection of Section objects by sectionNumber() ascending.
     * <br><br>
     * <b>Note:</b> This Comparator is NOT consistent with equals() in the sense that this Comparator's compare method is performed on a  <code>Section.course().subject().subjectAbbr()</code>, 
     * which is not the primary key of a Section. Thus the compare method will return 0 when performed on two Section objects with the same course, 
     * even thought they are two different Sections. Thus, <b> DO NOT </b> use this Comparator in a TreeSet.
     */
    public static final Comparator<Section> SUBJ_ABBR_ASC = new Comparator<Section>() {
            @Override
            public int compare(Section s1, Section s2) {
                return s1.course().subject().subjectAbbr().compareTo(s2.course().subject().subjectAbbr());
            }
    }; 
    
    //************************************************************************************************
    //***************************************Nested Helper Class**************************************
    //************************************************************************************************
    /**
     * A class used to contain MeetingTime arguments in a call to 
     * {@link uscheduler.internaldata.Sections#add(int, uscheduler.internaldata.Sessions.Session, uscheduler.internaldata.Courses.Course, uscheduler.internaldata.Campuses.Campus, java.lang.String, uscheduler.global.InstructionalMethod, int, int, java.util.Set, java.util.Collection)   Sections.add()}.
     * This class is needed so that all data associated with a section, including its meeting time's can be specified in a Sections.add() method call. 
     * Without this class, either:
     * <br>1) The Sections.add() method would not provide a parameter to specify a section's meeting times, 
     * which would then require that the {@link uscheduler.internaldata.Sections.Section Section} provide a public addMeetingTime() method, 
     * which would then allow the modification of a section's meeting time's when that section is potentially in a {@link uscheduler.internaldata.Schedules.Schedule Schedule} 
     * in which the modification would make the Schedule invalid by containing overlapping sections.
     * <br>2) The Sections.add() method would provide a parameter to specify a section's meeting times in terms of a regular {@link uscheduler.internaldata.Sections.Section.MeetingTime MeetingTime}, 
     * which would then break the contract that every reference to a {@link uscheduler.internaldata.Sections.Section Section} by a class outside of the {@link uscheduler.internaldata.Sections Sections} class is a reference to a Section that is in the Sections table.
     * 
     */
    public static class UnattachedMeetingTime{
        private final UTime cStartTime;
        private final UTime cEndTime;
        private final Collection<DayOfWeek> cDays;
        public UnattachedMeetingTime(UTime pStartTime, UTime pEndTime, Collection<DayOfWeek> pDays){
            cStartTime = pStartTime;
            cEndTime = pEndTime;
            cDays = pDays;
        }
        
    }
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

        //************************************************************************************************
        //***************************************Data Modification*****************************************
        //************************************************************************************************
   
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
            cMeetings = new HashSet<>();
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
         *<p><b>NOTE:</b> This method must be private to ensure no meeting times are added to a Section once the section is in the Meetings table and potentially in a schedule.
         * 
         * @param pStartTime the start time of the MeetingTime to construct. Not Null.
         * @param pEndTime the end time of the MeetingTime to construct. Not Null.
         * @param pDays a set of DayOfWeek of the MeetingTime  to construct. Not Null.
         * @return the constructed MeetingTime if no such MeetingTime already existed in this Section's MeetingTimes set, or the existing MeetingTime otherwise.
         * @throws IllegalArgumentException if pStartT, pEndT, or pDays is null.
         * @throws IllegalArgumentException if !pStartT.lessThan(pEndT).
         * @throws IllegalArgumentException if pDays.isEmpty() or if for any DayOfWekk dow in pDaya, dow == null.
         */
        private MeetingTime addMeetingTime(UTime pStartTime, UTime pEndTime, Collection<DayOfWeek> pDays){
            
            MeetingTime newMT = new MeetingTime(pStartTime, pEndTime, pDays);
            
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
         * Otherwise, this method does not modify this Section's instructors set and return false.
         *
         * @param pInstructor the Instructor to add to this Section's instructors set
         * @return true if the specified Instructor not already in the set, false otherwise
         * @throws IllegalArgumentException if pInstructor is null.
         */
        private boolean addInstructor(Instructor pInstructor) {
            if (pInstructor == null)
                throw new IllegalArgumentException("A Section's Instructor list cannot contain a null Instructor.");
            return this.cInstructors.add(pInstructor);
        }

        //************************************************************************************************
        //***************************************Querying*************************************************
        //************************************************************************************************
        
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
         * @return the Section's waitlist available
         */
        public int waitlistAvailable(){return cWaitlistAvailable;}

        /**
         * Returns a read-only {@link java.util.Set Set} view of this section's {@link uscheduler.internaldata.Sections.Section.MeetingTime meeting times}.
         * The set is backed by this section's container for meeting times, so changes to the container are reflected in the set.  
         * If this section's container for meeting times is modified while an iteration over the set is in progress, the results of the iteration are undefined. 
         *
         * <p>This method has less overhead than <tt>meetings2</tt> and should be used when an iterable read-only set will accomplish what is needed.
         * 
         * @return a read-only {@link java.util.Set Set} view of this section's {@link uscheduler.internaldata.Sections.Section.MeetingTime meeting times}.
         */
        public  Set<MeetingTime> meetings1(){
            return Collections.unmodifiableSet(cMeetings);
        }
        /**
         * Returns a new array containing this section's {@link uscheduler.internaldata.Sections.Section.MeetingTime meeting times}.
         * <p>This method has more overhead than <tt>meetings1</tt> since all meeting times are copied to a new array.
         * 
         * @return a new array containing this section's {@link uscheduler.internaldata.Sections.Section.MeetingTime meeting times}.
         */  
        public MeetingTime[] meetings2(){
             return cMeetings.toArray(new MeetingTime[cMeetings.size()]);
        }
        /**
         * Returns a read-only {@link java.util.Set Set} view of this section's {@link uscheduler.internaldata.Instructors instructors}.
         * The set is backed by this section's container for instructors, so changes to the container are reflected in the set.  
         * If this section's container for instructors is modified while an iteration over the set is in progress, the results of the iteration are undefined. 
         *
         * <p>This method has less overhead than <tt>instructors2</tt> and should be used when an iterable read-only set will accomplish what is needed.
         * 
         * @return a read-only {@link Collection} view of this section's {@link uscheduler.internaldata.Instructors instructors}.
         */
        public  Set<Instructor> instructors1(){
            return Collections.unmodifiableSet(cInstructors);
        }
        /**
         * Returns a new array containing this section's {@link uscheduler.internaldata.Instructors instructors}.
         * <p>This method has more overhead than <tt>instructors1</tt> since all instructors are copied to a new array.
         * 
         * @return a new array containing this section's {@link uscheduler.internaldata.Instructors instructors}.
         */  
        public Instructor[] instructors2(){
             return cInstructors.toArray(new Instructor[cInstructors.size()]);
        }
        /** Returns a String consisting of the name of each instructor in this Section, separated by the provided string. 
         * @param pSeparator the String that will separate each Instructor in the returned String
         * @return a String consisting of the name of each Instructor in this Section, separated by the string pSeperator.
         */
        public String instructorsString(String pSeparator){
            if(this.cInstructors.isEmpty())
                return "";
            
            StringBuilder sb = new StringBuilder();
            Iterator<Instructor> instructorsIT = this.cInstructors.iterator();
            sb.append(instructorsIT.next().instructorName());
            while(instructorsIT.hasNext())
                sb.append(pSeparator).append(instructorsIT.next().instructorName());
            return sb.toString();
        }
        
        @Override
        public String toString(){
            return "[session=" + cSession + ", course=" + cCourse + ", secNum=" + 
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
        
    //************************************************************************************************
    //***************************************Record Class*****************************************
    //************************************************************************************************

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
    public class MeetingTime implements Record{
        private final UTime cStartTime;
        private final UTime cEndTime;
        /**
         * The TreeSet used to store a MeetingTime's DayOfWeek objects, using DayOfWeek's parent Enum implementation of Comparable
        */
        private final TreeSet<DayOfWeek> cDays;

    //************************************************************************************************
    //***************************************Data Modification*****************************************
    //************************************************************************************************
        /**
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
        private MeetingTime (UTime pStartT, UTime pEndT, Collection<DayOfWeek> pDays) {
            if (pStartT == null)
                throw new IllegalArgumentException("A MeetingTime's start time cannot be null.");
            if (pEndT == null)
                throw new IllegalArgumentException("A MeetingTime's end time cannot be null.");
            if (!pStartT.lessThan(pEndT))
                throw new IllegalArgumentException("A MeetingTime's start time must be less than its end time.");
            if (pDays == null || pDays.isEmpty())
                throw new IllegalArgumentException("A MeetingTime must have at least one day.");
            
            cDays = new TreeSet<>();
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

        //************************************************************************************************
        //***************************************Querying*************************************************
        //************************************************************************************************
        
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
        public Section section(){return Section.this;}
        /**
         * @return this MeetingTime's start time
         */
        public UTime startTime(){return cStartTime;}
        /**
         * @return this MeetingTime's end time
         */
        public UTime endTime(){return cEndTime;}
        /**
         * Returns a read-only {@link java.util.Set Set} view of this MeetingTime's days of week.
         * The set is backed by this schedule's container for days of week, so changes to the container are reflected in the collection.  
         * If this schedule's container for days of week is modified while an iteration over the collection is in progress, the results of the iteration are undefined. 
         *
         * <p>This method has less overhead than <tt>days2</tt> and should be used when an iterable read-only collection will accomplish what is needed.
         * 
         * @return  a read-only {@link java.util.Set Set} view of this MeetingTime's days of week.
         */
        public  Set<DayOfWeek> days1(){
            return Collections.unmodifiableSet(cDays);
        }
        /**
         * Returns a new array containing this schedule's days of week.
         * <p>This method has more overhead than <tt>days</tt> since each DayOfWeek is copied to a new array.
         * 
         * @return a new array containing this schedule's days of week.
         */  
        public DayOfWeek[] days2(){
             return cDays.toArray(new DayOfWeek[cDays.size()]);
        }
        /**
         * Returns a String consisting of the short display name of each DayOfWeek in this MeetingTime, separated by the provided string
         * @param pSeparator the String that will separate each DayOfWeek in the returned String
         * @return a String consisting of the short display name of each DayOfWeek in this MeetingTime, separated by pSeparator.
         */
        public String daysString(String pSeparator){
            if(this.cDays.isEmpty())
                return "";
            
            StringBuilder sb = new StringBuilder();
            Iterator<DayOfWeek> daysIT = this.cDays.iterator();
            sb.append(daysIT.next().getDisplayName(TextStyle.SHORT, Locale.getDefault()));
            while(daysIT.hasNext())
                sb.append(pSeparator).append(daysIT.next().getDisplayName(TextStyle.SHORT, Locale.getDefault()));
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
            if (!(obj instanceof MeetingTime)) {
                return false;
            }
            final MeetingTime other = (MeetingTime) obj;
            if (!Section.this.equals(other.section())) {
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
            hash = hash * 31 + Section.this.hashCode();
            hash = hash * 31 + cStartTime.hashCode();
            hash = hash * 31 + cEndTime.hashCode();
            return hash;
        }
        /**
         * @return "MeetingTime[startTime=" + cStartTime + ", endTime=" + cEndTime + ", days=" + cDays +  "]"
         */
        @Override
        public String toString(){
            return "[startTime=" + cStartTime + ", endTime=" + cEndTime + ", days=" + cDays + "]";
        }
        /**
         * @return the MeetingTime's primary key value, which is Section.this.pkey() + "~" + cStartTime + "~" + cEndTime
         */
        public String pkey() {
            return Section.this.pkey() + "~" + cStartTime + "~" + cEndTime;
        }
    }
    }
}
