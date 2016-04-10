/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uscheduler.internaldata;

import java.time.DayOfWeek;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import uscheduler.global.UDate;
import uscheduler.global.UTime;
import uscheduler.internaldata.Sections.Section;
import uscheduler.internaldata.Sections.Section.MeetingTime;
import uscheduler.internaldata.Terms.Term;


/**
 * A singleton class that models the Schedules table to store {@link uscheduler.internaldata.Schedules.Schedule Schedule} records.
 * @author Matt Bush
 * 
 */
public final class Schedules implements Table{
    

    /**
     * This Comparator is used in the cSchedules TreeSet to try to accomplish 2 things:
     * 1) Have TreeSet ordered by "importance" of a Schedule. 
     * One schedule sch1 is more "important" than another sch2 if sch1.isSaved()==true and sch2.isSaved==false.
     * If sch1.isSaved() == sch2.isSaved, then sch1 is more important than sch2 if sch1.estMinutesAtSchool() is less than sch2.estMinutesAtSchool() 
     * If sch1.isSaved() == sch2.isSaved and sch1.estMinutesAtSchool() == sch2.estMinutesAtSchool(), then both are equally "important"
     * 2) The Comparator must return 0 on two logically equal Schedules, independent of "importance". Logical equivalence is based on the their set of sections.
     */
    private static final Comparator<Schedule> SAVED_MINUTES_ASC_UNIQUE = new Comparator<Schedule>() {
            @Override
            public int compare(Schedule sch1, Schedule sch2) {
                if(sch1.cSections.equals(sch2.cSections))
                    return 0;
                //Saved "more important" than unsaved Schedules
                if(sch1.cSaved != sch2.cSaved)
                    return (sch1.cSaved) ? -1 : 1;

                int diff;
                
                diff = (int) (sch1.estDaysAtSchool() - sch2.estDaysAtSchool());
                if (diff != 0)
                    return diff;
                
                diff = (int) (sch1.estMinutesAtSchool() - sch2.estMinutesAtSchool());
                if (diff != 0)
                    return diff;
                
                //At this point, order is not important. 
                //All that matters is that there isn't  "collision" on logically un-equal schedules
                return sch1.hashCode() - sch2.hashCode();
 
            }
    };
    
    /**
     * The TreeSet used to store Schedules sorted by "importance" and enforcing uniqueness using  the SAVED_MINUTES_ASC_UNIQUE Comparator.
     */
    private static final TreeSet<Schedule> cSchedules = new TreeSet(SAVED_MINUTES_ASC_UNIQUE);
    
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
    public static boolean addSchedule(Section... pSections){
        if (pSections == null)
            throw new IllegalArgumentException("Null argumen pSections");
        if (pSections.length == 0)
            throw new IllegalArgumentException("A Schedule must consist of at least one Section");
        
        Schedule newSchedule = new Schedule(pSections[0].session().term());
        for (Section sec : pSections){
            if (newSchedule.addSection(sec) == false)
                return false;
        }
        //create <code>Session Partitions</code> ***BEFORE*** adding the schedule to the table!
        //The comparator used in the sSchedules is dependent of attributes derived from the <code>Session Partitions</code>.
        newSchedule.buildSessionPartitions();
            
        if (!cSchedules.contains(newSchedule)){
            cSchedules.add(newSchedule);
            return true;
        }
        return false;
    }
    /**
     * Marks the specified schedule as so that <code>{@link uscheduler.internaldata.Schedules.Schedule#isSaved()  isSaved()} == true</code>.
     * If at the time of the call, <code>{@link uscheduler.internaldata.Schedules.Schedule#isSaved()  isSaved()} == true</code>, then no change is made to the Schedule.
     * Otherwise, the schedule's position will change in the Schedule's table as a result of setting the schedule to saved..
     * @param pSchedule the schedule to mark as "saved". Not null and <code>{@link uscheduler.internaldata.Schedules.Schedule#isDeleted()  isDeleted()} == false</code>.
     * @throws IllegalArgumentException if pSchedule == null || pSchedule.isDeleted() == true
     */
    public static void save(Schedule pSchedule){
        /**
         * Must first remove pScheduled from cSchedules TreeSet, then change cSaved, then add back to cSchedules TreeSet.
         * This is because schedule.cSaved is used in the compare method used in the Comparator provided to the cSchedules TreeSet
         */
        if (pSchedule == null || pSchedule.isDeleted())
            throw new IllegalArgumentException("Invalid pSchedule argument");
        if (!pSchedule.cSaved){
            cSchedules.remove(pSchedule);
            pSchedule.cSaved = true;
            cSchedules.add(pSchedule);            
        }
    }
    
    /**
     * Deletes from the Schedules table, the specified Schedule.
     * @param pSchedule the schedule to delete. Not null and <code>{@link uscheduler.internaldata.Schedules.Schedule#isDeleted()  isDeleted()} == false</code>.
     * @throws IllegalArgumentException if pSchedule == null || pSchedule.isDeleted() == true
     */
    public static void delete(Schedule pSchedule){
        if (pSchedule == null || pSchedule.isDeleted())
            throw new IllegalArgumentException("Invalid pSchedule argument");
        cSchedules.remove(pSchedule);
        pSchedule.cDeleted = true;
    }
    /**
     * Deletes from the Schedules table, all Schedules s such that <code>{@link uscheduler.internaldata.Schedules.Schedule#isSaved()  isSaved()} == false</code>.
     */
    public static void deleteUnsaved(){
        Iterator<Schedule> it = cSchedules.iterator();
        Schedule s;        
        while (it.hasNext())
        {
          s = it.next();
          if (!s.isSaved()){
              it.remove();
              s.cDeleted = true;
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
     * Returns a read-only {@link Set} view of all {@link uscheduler.internaldata.Schedules.Schedule schedules} in the Schedules table.
     * The collection is backed by the Schedules table, so changes to the table are
     * reflected in the set.  If the Schedules table is
     * modified while an iteration over the collection is in progress, the results of the iteration are undefined. 
     *
     * <p>This method has less overhead than <tt>getAll2</tt> and should be used when an iterable read-only set will accomplish what is needed.
     * 
     * @return a read-only {@link Set} view of all schedules in the Schedules table
     */
    public static Set<Schedule> getAll1(){
        return Collections.unmodifiableSet(cSchedules);
    }
    /**
     * Returns a new array containing all {@link uscheduler.internaldata.Schedules.Schedule schedules} in the Schedules table.
     * <p>This method has more overhead than <tt>getAll1</tt> since all {@link uscheduler.internaldata.Schedules.Schedule schedules} in the table are copied to a new array.
     * 
     * @return a new array containing all {@link uscheduler.internaldata.Schedules.Schedule schedules} in the Schedules table.
     */  
    public static Schedule[] getAll2(){
        return cSchedules.toArray(new Schedule[cSchedules.size()]);
    }
    /**
     * Returns a new List containing all schedules in the Schedules table such that {@link uscheduler.internaldata.Schedules.Schedule#isSaved() isSaved()} == true.
     * 
     * @return a new List containing all schedules in the Schedules table such that {@link uscheduler.internaldata.Schedules.Schedule#isSaved() isSaved()} == true.
     */  
    public static List<Schedule> getAllSaved(){
        List<Schedule> aList = new ArrayList<>();
        for(Schedule s: cSchedules)
            if(s.isSaved())
                aList.add(s);
        return aList;
    }   
    
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
     *
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
         * Stores this schedule's <code>Session Partitions</code> using  comparator on the start date of the <code>Session Partition</code>, which is unique relative to a schedule.
         */
        private final TreeSet<SessionPartition> cSessionPartitions;
        /**
         * A Comparator of type SessionPartition that compares two SessionPartition objects based on the SessionPartition's startTime(), 
         * which will be unique for SessionPartitions of the same Schedule.
         */
        private final Comparator<SessionPartition> START_DATE_ASC = new Comparator<SessionPartition>() {
            @Override
            public int compare(SessionPartition sp1, SessionPartition sp2) {
                return sp1.cStartDate.compareTo(sp2.cStartDate);
            }
        };  
        

    //************************************************************************************************
    //***************************************Data Manipulation*********************************************
    //************************************************************************************************ 
        
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
            cSaved = false; //only to be set by Schedules class
            cDeleted = false; //only to be set by Schedules class
            cSections = new HashSet<>();
            cSessionPartitions = new TreeSet(START_DATE_ASC);
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
         * Builds the <code>Session Partitions</code> of this schedule. 
         * This should be called by the Schedules class right before adding this Schedule to the table and once no more additions of sections to this SChedule will be made.
         */
        private void buildSessionPartitions(){
            TreeSet<UDate> datesTree = new TreeSet();
            //For each section in this schedule, add the section's session's start date and end date to a TreeSet<UDate> to get the distinct <code>Session Partition</code> dates ordered.
            for(Section sec: cSections){
                datesTree.add(sec.session().startDate());
                datesTree.add(sec.session().endDate());
            }
            //construct new <code>Session Partitions</code> based on ordered distinct dates
            UDate[] datesArray = datesTree.toArray(new UDate[datesTree.size()]);
            for(int i = 1; i < datesArray.length; i++){
                cSessionPartitions.add(new SessionPartition(datesArray[i-1], datesArray[i]));
            }
        }
        //************************************************************************************************
        //***************************************Querying*********************************************
        //************************************************************************************************ 
        
//        /**
//         * Returns the primary key value of this Schedule.
//         * @return the primary key value of this Schedule, which is its HashSet of sections.
//         */
//        private HashSet<Section> pkey(){return cSections;}

        /**
         * @return the Schedule's Term
         */
        public Term term(){return cTerm;}
        /**
         * Returns a read-only {@link java.util.Set Set} view of this schedule's {@link uscheduler.internaldata.Sections.Section sections}.
         *
         * <p>This method has less overhead than <tt>sections2</tt> and should be used when an iterable read-only set will accomplish what is needed.
         * 
         * @return a read-only {@link java.util.Set Set} view of this schedule's {@link uscheduler.internaldata.Sections.Section sections}.
         */
        public  Set<Section> sections1(){
            return Collections.unmodifiableSet(cSections);
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
         * Returns a read-only Set view of this schedule's {@link uscheduler.internaldata.Schedules.Schedule.SessionPartition SessionPartitions}.
         *
         * <p>This method has less overhead than <tt>sessionPartitions2</tt> and should be used when an iterable read-only set will accomplish what is needed.
         * 
         * @return a read-only Set view of this schedule's {@link uscheduler.internaldata.Schedules.Schedule.SessionPartition SessionPartitions}.
         */
        public  Set<SessionPartition> sessionPartitions1(){
            return Collections.unmodifiableSet(cSessionPartitions);
        }
        /**
         * Returns a new array containing this schedule's {@link uscheduler.internaldata.Schedules.Schedule.SessionPartition SessionPartitions}
         * <p>This method has more overhead than <tt>sections1</tt> since all {@link uscheduler.internaldata.Schedules.Schedule.SessionPartition SessionPartitions} are copied to a new array.
         * 
         * @return a new array containing this schedule's {@link uscheduler.internaldata.Schedules.Schedule.SessionPartition SessionPartitions}
         */  
        public SessionPartition[] sessionPartitions2(){
             return cSessionPartitions.toArray(new SessionPartition[cSessionPartitions.size()]);
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
        /**
         * 
         * @return the estimated number of days at school of this schedule
         */      
        public double estDaysAtSchool(){
            double sum=0;
            for(SessionPartition sp : cSessionPartitions)
                sum = sum + sp.estDaysAtSchool();
            return sum;
        }
        /**
         * 
         * @return the estimated number of minutes at school of this schedule
         */      
        public double estMinutesAtSchool(){
            double sum=0;
            for(SessionPartition sp : cSessionPartitions)
                sum = sum + sp.estMinutesAtSchool();
            return sum;
        }


        //************************************************************************************************
        //***************************************Inner Class*********************************************
        //************************************************************************************************    
        /** This class models a Session Partition of a {@link uscheduler.internaldata.Schedules.Schedule Schedule}.
         * 
         * <p>Let <code>{sec[1], sec[2], ..., sec[n]}</code> be the set of sections in the schedule <code>U</code>.
         * <br>Let <code>(dt[1], dt[2], ..., dt[k]) </code>be the sequence of distinct dates composed of the
         * {@link uscheduler.internaldata.Sessions.Session#startDate() startDate()} and {@link uscheduler.internaldata.Sessions.Session#endDate() endDate()} 
         * of the {@link uscheduler.internaldata.Sections.Section#session() session} of each <code>sec[i]</code> in <code>U</code>, 
         * ordered by date ascending such that <code>dt[i] &lt; dt[i+1]</code>.
         * <p>The <b><code>Session Partitions</code></b> of a schedule <code>U</code> is the sequence of ordered pairs <code>(sp[1], sp[2], ... , sp[k-1])</code> 
         * such that <code>sp[i] = (dt[i], dt[i+1])</code>. Each <code>sp[i]</code> is said to be a <b><code>Session Partition</code></b> of U. 
         * <p><b>Example:</b> Suppose a schedule <code>U</code> consists of the sections <code>{sec[1], sec[2], sec[3], sec[4]}</code> such that:
         * <br>
         * <code>
         * <br>sec[1].session().startDate() == 5/16/2016 and sec[1].session().endDate() == 7/27/2016 
         * <br>sec[2].session().startDate() == 5/16/2016 and sec[2].session().endDate() == 5/26/2016 
         * <br>sec[3].session().startDate() == 6/01/2016 and sec[3].session().endDate() == 7/27/2016 
         * <br>sec[4].session().startDate() == 6/01/2016 and sec[4].session().endDate() == 6/24/2016  
         * <br>sec[5].session().startDate() == 6/08/2016 and sec[5].session().endDate() == 7/21/2016  
         * <br>sec[5].session().startDate() == 6/28/2016 and sec[5].session().endDate() == 7/27/2016  
         * </code>
         * <br><br>Then the <code>Session Partitions</code> of <code>U</code> are:
         * <br>
         * <code>
         * <br>sp[1] = (5/16, 5/26)
         * <br>sp[2] = (5/26, 6/01)
         * <br>sp[3] = (6/01, 6/08)
         * <br>sp[4] = (6/08, 6/24) 
         * <br>sp[5] = (6/24, 6/28)
         * <br>sp[6] = (6/28, 7/21)
         * <br>sp[7] = (7/21, 7/25)
         * <br>sp[8] = (7/25, 7/27)
         * </code>
         * <br>
         * <br> where each date in the ordered pair is the <code>Session Partition's</code> <b>{@link uscheduler.internaldata.Schedules.Schedule.SessionPartition#startDate() startDate}</b> 
         * and <b>{@link uscheduler.internaldata.Schedules.Schedule.SessionPartition#endDate()  endDate}</b> respectively.
         * 
         * <p> The <b>{@link uscheduler.internaldata.Schedules.Schedule.SessionPartition#lenght() length}</b> of a <code>Session Partition</code> <code>sp[i]</code> 
         * is the number of days between <code>sp[i].startDate</code> and <code>sp[i].endDate</code>. The <code>length</code> of <code>sp[1]</code> above is <code>10</code>.
 
         * <p>A section <code>sec</code> in a schedule <code>U</code> is said to be <b>in the session partition</b> <code>sp[i]</code> 
         * if <code>sp[i]</code> is a session partition of <code>U</code> 
         * and <code>sec.session().startDate()</code> &lt;= <code>sp[i].startDate()</code> 
         * and <code>sp[i].endDate()</code> &lt;= <code>sec.session().endDate()</code>.
         * 
         * <p>Thus, continuing with the above example:
         * <br>
         * <code>
         * <br>sp[1].sections = {sec[1], sec[2]}
         * <br>sp[2].sections = {sec[1]}
         * <br>sp[3].sections = {sec[1], sec[3], sec[4]}
         * <br>sp[4].sections = {sec[1], sec[3], sec[4], sec[5]} 
         * <br>sp[5].sections = {sec[1], sec[3], sec[5]}
         * <br>sp[6].sections = {sec[1], sec[3], sec[5], sec[6]}
         * <br>sp[7].sections = {sec[1], sec[3],  sec[6]}
         * <br>sp[8].sections = {sec[1]}
         * </code>
         * <br>

         */
        public class SessionPartition{
            private final UDate cStartDate;
            private final UDate cEndDate;
            private final HashSet<Section>  cPartitionSections;
            private final TreeMap<DayOfWeek, DayOfWeekPartition> cDayOfWeekPartitions;
            

            private SessionPartition(UDate pStartDate, UDate pEndDate){
                cStartDate = pStartDate;
                cEndDate = pEndDate;
                cPartitionSections = new HashSet();
                cDayOfWeekPartitions = new TreeMap();
                
                for(Section sec: cSections)
                    //This Section is "in" this SessionPartition. Add its MeetingTimes to the map
                    if(sec.session().startDate().lessThanOrEqual(pStartDate) && pEndDate.lessThanOrEqual(sec.session().endDate())){
                        cPartitionSections.add(sec);
                        for(MeetingTime mt: sec.meetings1())
                            for(DayOfWeek dow : mt.days1()){
                                DayOfWeekPartition foundSpmd = cDayOfWeekPartitions.get(dow);
                                if (foundSpmd == null)
                                    cDayOfWeekPartitions.put(dow, new DayOfWeekPartition(dow, mt));
                                else
                                    foundSpmd.addMeetingTime(mt);
                            }
                    }
            }
            //************************************************************************************************
            //***************************************Querying*********************************************
            //************************************************************************************************ 
            /**
             * @return the Schedule to which this SessionPartition belongs.
             */
            public Schedule schedule() {
                return Schedule.this;
            }
            public UDate startDate(){
               return cStartDate;
            }
            public UDate endDate(){
               return cEndDate;
            }
            /**
             * Returns a read-only {@link java.util.Set Set} view of this SessionPartition's sections.
             * 
             * @return a read-only Set view of this SessionPartition's sections.
             */
            public Set<Section> sections(){
                return Collections.unmodifiableSet(cPartitionSections);
            }
            /**
             * Returns a read-only {@link java.util.Set Set} view of this SessionPartition's cDayOfWeekPartitions.
             * 
             * @return a read-only Set view of this SessionPartition's DayOfWeekPartitions.
             */
            public Collection<DayOfWeekPartition> dayOfWeekPartitions(){
                return Collections.unmodifiableCollection(cDayOfWeekPartitions.values());
            }
            
            public int lenght(){
               return cStartDate.daysTo(cEndDate);
            }
            public double weeks(){
               return lenght() / 7.0;
            }
            public int daysOfWeekAtSchool(){
               return cDayOfWeekPartitions.size();
            }
            public double estDaysAtSchool(){
               return daysOfWeekAtSchool() * weeks();
            }
            public double estMinutesAtSchool(){
               int sum = 0;
               for(DayOfWeekPartition spmd : cDayOfWeekPartitions.values())
                   sum = sum + spmd.minutesAtSchool();
               return sum  * weeks();
            }
            //************************************************************************************************
            //***************************************Inner Class*********************************************
            //************************************************************************************************ 
            
            public class DayOfWeekPartition{
                private final DayOfWeek cDayOfWeek;
                /**
                 * Consists of all MeetingTimes of each Section in the SessionPartition for which mt.days().contains(cDayOfWeek) == true.
                 */
                private final TreeSet<MeetingTime> cMeetingTimes;
                
                /**
                 * A Comparator of type MeetingTime that compares two MeetingTime objects based on the MeetingTime's startTime(), 
                 * which will be unique so long as, for each MeetingTime mt in the set:
                 * 1) mt.days().contains(cDayOfWeek) == true.
                 * 2) All MeetingTimes are associated with Sections that are all part of the same valid schedule 
                 * 3) All MeetingTimes are associated with Sections belonging to the same <code>Session Partition</code>. 
                 * This will allow order in the set by start times for the given DayOfWeek
                 */
                private final Comparator<MeetingTime> START_TIME_ASC = new Comparator<MeetingTime>() {
                    @Override
                    public int compare(MeetingTime mt1, MeetingTime mt2) {
                        return mt2.startTime().minutesTo(mt1.startTime());
                    }
                };  
                private DayOfWeekPartition(DayOfWeek pDayOfWeek, MeetingTime pMeetingTime){
                    cDayOfWeek = pDayOfWeek;
                    cMeetingTimes = new TreeSet(START_TIME_ASC);
                    cMeetingTimes.add(pMeetingTime);
                }
                
                /**
                 * Adds a MeetingTime pMeetingTime to this DayOfWeekPartition. It must be true that:
                 * 1) pMeetingTime.days().contains(cDayOfWeek) == true.
                 * 2) All MeetingTimes in cMeetingTimes are associated with Sections that are all part of the same valid (non-overlapping) schedule 
                 * 3) All MeetingTimes are associated with Sections belonging to the same <code>Session Partition</code>. 
                 * @param pMeetingTime the MeetingTime to add to cMeetingTimes. No checks are made on validity.
                 */
                private void addMeetingTime(MeetingTime pMeetingTime){
                    cMeetingTimes.add(pMeetingTime);
                }
                //************************************************************************************************
                //***************************************Querying*********************************************
                //************************************************************************************************ 
                /**
                 * @return the SessionPartition to which this DayOfWeekPartition belongs.
                 */
                public SessionPartition sessionPartition() {
                    return SessionPartition.this;
                }
                /**
                 * @return the DayOfWeek of this DayOfWeekPartition.
                 */
                public DayOfWeek dayOfWeek(){return cDayOfWeek;}
                /**
                 * Returns a read-only {@link java.util.Set Set} view of this DayOfWeekPartition's {@link uscheduler.internaldata.Sections.Section.MeetingTime meeting times}.
                 *
                 * <p>This method has less overhead than <tt>meetingTimes2</tt> and should be used when an iterable read-only set will accomplish what is needed.
                 * 
                 * @return a read-only {@link java.util.Set Set} view of this DayOfWeekPartition's meeting times
                 */
                public Set<MeetingTime> meetingTimes1(){
                    return Collections.unmodifiableSet(cMeetingTimes);
                }
                /**
                 * Returns the first {@link uscheduler.internaldata.Sections.Section.MeetingTime#startTime()   startTime} in this DayOfWeekPartition.
                 * @return the first startTime in this DayOfWeekPartition.
                 */
                public UTime minStart(){
                    return cMeetingTimes.first().startTime();
                }
                /**
                 * @return the last {@link uscheduler.internaldata.Sections.Section.MeetingTime#endTime()  endTime} in this DayOfWeekPartition.
                 */
                public UTime maxEnd(){
                    return cMeetingTimes.last().endTime();
                }
                /**
                 * @return the minutes between minStart() and maxEnd().
                 */
                public int minutesAtSchool(){
                    return minStart().minutesTo(maxEnd());
                }

                
            }
        }
    }
    
}
