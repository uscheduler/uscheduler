/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uscheduler.internaldata;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import uscheduler.internaldata.Sections.Section;
import uscheduler.internaldata.Terms.Term;


/**
 * A singleton class that models the Schedules table to store Schedule records.
 * @author Matt Bush
 * 
 */
public final class Schedules implements Table{
    
    /**
     * The HashMap to store Schedule objects using the schedule's set of Sections as the key into the map. 
     * See Java's AbstractSet implementation of hashCode and equals to know how a set will work as a key
     */
    private static final HashMap<HashSet<Section>, Schedule> cSchedules = new HashMap();
    
    /**
     * Private constructor to prevent instantiation and implement as a singleton class
     */
    private Schedules(){};
    
    //************************************************************************************************
    //***************************************Data Modification*****************************************
    //************************************************************************************************
    /**
     * Constructs a new Schedule consisting of the specified list of Sections and adds it to the Schedules table if no equal Schedule already exists in the table.
     * <br>
     * The constructed Schedule consisting of the specified list of Sections is either valid or invalid. 
     * <br>
     * The constructed Schedule is invalid and this method throws an IllegalArgumentException exception if:
     * <br>
     * <br>1) The specified list of Sections is empty or null.
     * <br>2) For any Section s in the specified list of Sections, s is null
     * <br>3) The Term of all Sections in the list is not the same. This enforces the constraint that a Schedule consists of Sections all associated with the same Term.
     * <br>4) The Course of each Section in the list is not distinct.
     * This enforces the constraint that each Section in a Schedule are associate with distinct Courses.
     * <br><br>
     * The constructed Schedule is invalid and this method returns null if:
     * <br>
     * <br>Any Section in the specified list overlaps with any other Section in the specified list. 
     * This enforces the constraint that no two Sections in a schedule can overlap. 
     * Overlap is defined by the {@link uscheduler.internaldata.Sections.Section#overlaps(uscheduler.internaldata.Sections.Section)  overlap()} method of the Schedule class.
     * <br><br>
     * If the constructed Schedule is valid, this method tests to see if an equal Schedule already exists in the Schedules table, 
     * Two Schedule are equal if their sets of Sections are equal, where set equality is based on the mathematical perspective of equality of sets. That is, two sets are equal if they contain the same objects.
     * If no equal Schedule exists, then this method adds the constructed Schedule to the table and returns it. 
     * Otherwise, this method returns the Schedule already in the table that is equal to the constructed Schedule.
     * 
     * @param pSections the list of Sections from which the constructed Schedule will consist.
     * @return the newly added Schedule if no equal Schedule already existed in the table, 
     * the already existing Schedule if an equal Schedule existed, 
     * or null if the constructed Schedule is invalid.
     * @throws IllegalArgumentException see above description
     */  
    public static Schedule addSchedule(Section... pSections){
        if (pSections == null)
            throw new IllegalArgumentException("Null argumen pSections");
        if (pSections.length == 0)
            throw new IllegalArgumentException("A Schedule must consist of at least one Section");
        
        Schedule newSchedule = new Schedule(pSections[0].session().term());
        for (Section sec : pSections){
            if (newSchedule.addSection(sec) == false)
                return null;
        }
        
        Schedule existingSchedule = cSchedules.get(newSchedule.pkey());
        if (existingSchedule == null){
            cSchedules.put(newSchedule.pkey(), newSchedule);
            return newSchedule;
        }
        return existingSchedule;
    }
    /**
     * Deletes from the Schedules table, the specified Schedule.
     * @param pSchedule the schedule to delete
     * @throws IllegalArgumentException if pSchedule == null || pSchedule.isDeleted() == true
     */
    public static void delete(Schedule pSchedule){
        if (pSchedule == null || pSchedule.isDeleted())
            throw new IllegalArgumentException("Invalid pSchedule argument");
        Schedule found = cSchedules.remove(pSchedule.pkey());
        found.markDeleted();
    }
    /**
     * Deletes from the Schedules table, all Schedules s such that s.isSaved() == false. 
     */
    public static void deleteUnsaved(){
        Iterator<Schedule> it = cSchedules.values().iterator();
        Schedule s;        
        while (it.hasNext())
        {
          s = it.next();
          if (!s.isSaved()){
              it.remove();
              s.markDeleted();
          }
        }
    }
    //************************************************************************************************
    //***************************************Querying*************************************************
    //************************************************************************************************
    /**
     * 
     * @return the number of records in this table
     */
    public static int size(){
        return cSchedules.size();
    }
    /**
     * Returns a read-only {@link Collection} view of all {@link uscheduler.internaldata.Schedules.Schedule schedules} in the Schedules table.
     * The collection is backed by the Schedules table, so changes to the table are
     * reflected in the collection.  If the Schedules table is
     * modified while an iteration over the collection is in progress, the results of the iteration are undefined. 
     *
     * <p>This method has less overhead than <tt>getAll2</tt> and should be used when an iterable read-only collection will accomplish what is needed.
     * 
     * @return a read-only {@link Collection} view of all {@link uscheduler.internaldata.Schedules.Schedule schedules} in the Schedules table
     */
    public static Collection<Schedule> getAll1(){
        return Collections.unmodifiableCollection(cSchedules.values());
    }
    /**
     * Returns a new array containing all {@link uscheduler.internaldata.Schedules.Schedule schedules} in the Schedules table.
     * <p>This method has more overhead than <tt>getAll1</tt> since all {@link uscheduler.internaldata.Schedules.Schedule schedules} in the table are copied to a new array.
     * 
     * @return a new array containing all {@link uscheduler.internaldata.Schedules.Schedule schedules} in the Schedules table.
     */  
    public static Schedule[] getAll2(){
        return cSchedules.values().toArray(new Schedule[cSchedules.size()]);
    }
    /**
     * Returns a new ArrayList containing all schedules in the Schedules table such that {@link uscheduler.internaldata.Schedules.Schedule#isSaved() isSaved()} == true.
     * 
     * @return a new ArrayList containing all schedules in the Schedules table such that {@link uscheduler.internaldata.Schedules.Schedule#isSaved() isSaved()} == true.
     */  
    public static ArrayList<Schedule> getAllSaved(){
        ArrayList<Schedule> aList = new ArrayList<>();
        for(Schedule s: cSchedules.values())
            if(s.isSaved())
                aList.add(s);
        return aList;
    }
    //************************************************************************************************
    //***************************************Comparators*********************************************
    //************************************************************************************************ 
//    /**
//     * A Comparator of type Schedule that compares two Schedule objects based on ScheduleNumber().
//     * This Comparator will allow ordering a Collection of Schedule objects by ScheduleNumber() ascending.
//     * <br><br>
//     * <b>Note:</b> This Comparator is NOT consistent with equals() in the sense that this Comparator's compare method is performed on a Schedule's Schedule number, 
//     * which is not the primary key of a Schedule. Thus the compare method will return 0 when performed on two Schedule objects with the same Schedule number, 
//     * even thought they are two different Schedules. Thus, <b> DO NOT </b> use this Comparator in a TreeSet.
//     */
//    public static final Comparator<Schedule> SEC_NUM_ASC = new Comparator<Schedule>() {
//            @Override
//            public int compare(Schedule s1, Schedule s2) {
//                return s1.cScheduleNumber.compareTo(s2.cScheduleNumber);
//            }
//    };     
    
    //************************************************************************************************
    //***************************************Record Class*********************************************
    //************************************************************************************************  


    /**
     * Models a Schedule record in the Schedules table.
     * To simplify the implementation of data constraints and ensuring data integrity, 
     * only the Schedules class can create instances of this type and add them to the Schedules table.
     * <br>
     * <b>NOTE:</b> However, unlike all other record classes, a Schedule is NOT immutable (i.e. it can change). Additionally, a Schedule can be deleted from the Schedules table.
     * This class provides a method isDeleted() that returns true if the schedule has been deleted from the Schedules table. 
     * <p>
     * <b>Design Note:</b> This class does not provide calculations for the derived attributes "average number of days of week at school" and "total time at school". 
     * The reason is that these calculations are:
     * <br>1) Very complex to implement and if implemented by this class, this class' responsibilities would become disproportionately greater than other "record" type classes. 
     * <br>2) Very resource costly, in processing and storage. 
     * <br>
     * In case it's realized that it's not feasible to implement these derived attributes, from the perspective of development time or performance, 
     * this class' design will not need to be altered.
     * <br>The current plan is to have some sort of wrapper class(es) extend the functionality of this one in order to provide the derived attributes.
     * 
     * @author Matt Bush
     */
    public static class Schedule{
        private final Term cTerm;
        private boolean cSaved;
        private boolean cDeleted;

        /**
         * The HashSet used to store a Schedule's sections, using the fact that no two same instances of a Section can exist that are equal in terms of their values to ensure no duplicates.
         */
        private final HashSet<Section> cSections;

        /**
         * Private constructor only to be used by Schedules class ensures no instances of Schedule will exist outside of Schedules' HashSet
         * 
         * @param pTerm the term that all Sections added to this Schedule must be associated with
         * @throws IllegalArgumentException if pTerm is null.
         */
        private Schedule (Term pTerm) {
            if (pTerm == null)
                throw new IllegalArgumentException("A Schedule's term cannot be null.");
            cTerm = pTerm;
            cSaved = false;
            cDeleted = false;
            cSections = new HashSet<>();
        }
        /**
         * Attempts to add the specified Section to this Schedule.<br>
         * An attempted add fails and this method throws an IllegalArgumentException exception if:<br>
         * <br>1) pSection is null
         * <br>2) pSection.session().term() != this.term(). This enforces the constraint that a Schedule consists of Sections all associated with the same Term.
         * <br>3) The Course of the specified Section equals the course of any Section previously added. 
         * This enforces the constraint that each Section in a Schedule are associate with distinct Courses.
         * <br><br>
         * An attempted add fails and this method returns false if:<br>
         * <br>3The specified Section overlaps with any Section previously added to this Schedule. 
         * This enforces the constraint that no two Sections in a schedule can overlap. 
         * Overlap is defined by the {@link uscheduler.internaldata.Sections.Section#overlaps(uscheduler.internaldata.Sections.Section)  overlap()} method of the Schedule class.
         * <br><br>
         * If an attempted add fails for any of the reasons mentioned above, then the specified Section is not added to this Schedule.
         * @param pSection the Section to add to this Schedule.
         * @return true if the Section was added to this Schedule.
         * @throws IllegalArgumentException if pSection is null, pSection.session().term() != this.term(), or the Course of the specified Section equals the course of any Section previously added. 
         */
        private boolean addSection(Section pSection) {
            if (pSection == null)
                throw new IllegalArgumentException("A Schedule cannot contain a null Section.");
            if (pSection.session().term() != this.cTerm)
                throw new IllegalArgumentException("A Schedule must consist of sections all belonging to the same term.");           
            for (Section addedSection : cSections){
                if (addedSection.course().equals(pSection.course()))
                    throw new IllegalArgumentException("A Schedule must consist of Sections from distinct Courses.");  
                if (addedSection.overlaps(pSection))
                    return false;
            }
            return cSections.add(pSection);//Note: This Should NEVER return false, because the ONLY way for it to return false is if the Section is already in cSections, which would have caused overlap.
        } 
        
        /**
         * Returns the primary key value of this Schedule.
         * @return the primary key value of this Schedule, which is its HashSet of sections.
         */
        private HashSet<Section> pkey(){return cSections;}
        
        /**
         * Sets this Schedule's status to deleted.
         */
        private void markDeleted(){
            cDeleted = true;
        } 
        
        /**
         * @return the Schedule's Term
         */
        public Term term(){return cTerm;}
        /**
         * Returns a read-only {@link Collection} view of this schedule's {@link uscheduler.internaldata.Sections.Section sections}.
         * The collection is backed by this schedule's container for sections, so changes to the container are reflected in the collection.  
         * If this schedule's container for sections is modified while an iteration over the collection is in progress, the results of the iteration are undefined. 
         *
         * <p>This method has less overhead than <tt>sections2</tt> and should be used when an iterable read-only collection will accomplish what is needed.
         * 
         * @return a read-only {@link Collection} view of this schedule's {@link uscheduler.internaldata.Sections.Section sections}.
         */
        public  Collection<Section> sections1(){
            return Collections.unmodifiableCollection(cSections);
        }
        /**
         * Returns a new array containing this schedule's {@link uscheduler.internaldata.Sections.Section sections}.
         * <p>This method has more overhead than <tt>sections1</tt> since all {@link uscheduler.internaldata.Sections.Section sections} are copied to a new array.
         * 
         * @return a new array containing this schedule's {@link uscheduler.internaldata.Sections.Section sections}.
         */  
        public Section[] sections2(){
             return cSections.toArray(new Section[cSections.size()]);
        }
        /**
         * @return true if this Schedule is saved
         */
        public boolean isSaved(){return cSaved;}
        /**
         * @return true if this Schedule has been deleted from the Schedules table, thus making it invalid.
         */
        public boolean isDeleted(){return cDeleted;}
        /**
         * 
         * @return "Schedule[saved=" + cSaved + ", sections=" + cSections +  "]";
         */      
        @Override
        public String toString(){
            return "Schedule[saved=" + cSaved + ", sections=" + cSections +  "]";
        }
//        /**
//         * Returns true if this Schedule's set of sections is equal to the other Schedule's set of sections.
//         * <br>
//         * Equality of the two sets of sections is based on the mathematical perspective of equality of sets. That is, two sets are equal if they contain the same objects.
//         * <br>
//         * If obj is null or obj isn't an instance of Schedule, false is returned. 
//         * Otherwise, this Schedule is equal to the other Schedule if and only if they contain their sets of sections.
//         * <br>
//         * <br>
//         * Unlike other record classes, it is useful that Schedule override equals() and hashCode(). 
//         * With other record classes, it is guaranteed that no two logically equal instances of records can exist because other table classes don't permit deleting. 
//         * However, with this class it is possible that two two or more logically equal instances exist, although only one can be in the schedule table at any given time.
//         * 
//         * @return true if this MeetingTime is equal to the provided object.
//         */
//        @Override
//        public boolean equals(Object obj) {
//            if (obj == null) {
//                return false;
//            }
//            if (getClass() != obj.getClass()) {
//                return false;
//            }
//            final Schedule other = (Schedule) obj;
//            return this.pkey().equals(other.pkey()); //See Java's AbstractSet implementation of equals to know what this returns
//        }
//        /**
//         * Returns this Schedule's hash code which is based on its set of sections.
//         * <br>
//         * <br>
//         * Unlike other record classes, it is useful that Schedule override equals() and hashCode(). 
//         * With other record classes, it is guaranteed that no two logically equal instances of records can exist because other table classes don't permit deleting. 
//         * However, with this class it is possible that two two or more logically equal instances exist, although only one can be in the schedule table at any given time.
//         * @return this Schedule's hash code.
//         */
//        @Override
//        public int hashCode(){
//            return this.pkey().hashCode();
//        }
    }
    
}
