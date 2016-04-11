/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uscheduler.internaldata;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import uscheduler.internaldata.Terms.Term;
import uscheduler.global.UDate;


/**
 * A singleton class that models the Sessions table to store Session records.
 * @author Matt Bush
 * 
 */
public final class Sessions implements Table{
    
    /**
     * The HashMap to store Session objects using the session's pkey() as the key into the map. s.
     */
    private static final HashMap<String, Session> cSessions = new HashMap<>();   
    /**
     * Private constructor to prevent instantiation and implement as a singleton class
     */
    private Sessions(){};
    
     //************************************************************************************************
    //***************************************Data Modification*****************************************
    //************************************************************************************************
     /**
     * Adds a new Session to the Sessions table.
     * <br><br>
     * This method first checks to see if a Session already exists in the Sessions table with the Term specified by pTerm and the session name specified by pSessionName. 
     * If so, this method returns the already existing Session. 
     * Otherwise, this method creates a new Session with the arguments provided, 
     * adds the newly created Session to the Table, and returns the newly created Session.
     * 
     * @param pTerm the Term of the session
     * @param pSessionName the name of the session
     * @param pStartDate the start date of the session
     * @param pEndDate the end date of the session
     * @throws IllegalArgumentException if pTerm, pSessionName, pStartDate, or pEndDate is null or if pSessionName doesn't contain at least one non-white-space character.
     * @throws IllegalArgumentException if pStartDate is not less than pEndDate.
     * @return the newly added Session if no such Session already existed, otherwise returns the already existing Session.
     */
    public static Session add(Term pTerm, String pSessionName, UDate pStartDate, UDate pEndDate){
        Session temp = new Session(pTerm, pSessionName, pStartDate, pEndDate);
        Session found = cSessions.get(temp.pkey());
        if (found == null){
            cSessions.put(temp.pkey(), temp);
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
        return cSessions.size();
    }
    /**
     * Returns from the Sessions table the Session with the specified Term and session name.
     * 
     * @param pTerm the Term of the Session being retrieved.
     * @param pSessionName the session name of the Session being retrieved.
     * @return the Session whose Term has the same pkey() as pTerm.pkey() and whose session name is pSessionName, if such a Session exists in the table. Null otherwise.
     */
    public static Session get(Term pTerm, String pSessionName){
        return cSessions.get(pTerm.pkey() + "~" + pSessionName);
    }    
    /**
     * Returns from the Sessions table all Sessions, in no particular order.
     * This method is used for testing and debugging and does not have a use in the final app.
     * 
     * @return ArrayList of all Sessions in the Sessions table. 
     */
    public static ArrayList<Session> getAll(){
        return new ArrayList<>(cSessions.values());
    }
    //************************************************************************************************
    //***************************************Comparators*********************************************
    //************************************************************************************************  
    /**
     * A Comparator of type Session that compares two Session objects based on {@link Sessions.Session#pkey() Session.pkey()}.
     * This Comparator will allow ordering a Collection of Session objects by {@link Sessions.Session#pkey() Session.pkey()} ascending.
     */
    public static final Comparator<Session> PK_ASC = new Comparator<Session>() {
            @Override
            public int compare(Session s1, Session s2) {
                return s1.pkey().compareTo(s2.pkey());
            }
    }; 
    /**
     * A Comparator of type Session that compares two Session objects based on startDate().
     * This Comparator will allow ordering a Collection of Session objects by startDate() ascending.
     * <br><br>
     * <b>Note:</b> This Comparator is NOT consistent with equals() in the sense that this Comparator's compare method is performed on a Session's start date, 
     * which is not the primary key of a Session. Thus the compare method will return 0 when performed on two Session objects with the same start date, 
     * even thought they are two different Sessions. Thus, <b> DO NOT </b> use this Comparator in a TreeSet.
     */
    public static final Comparator<Session> START_DATE_ASC = new Comparator<Session>() {
            @Override
            public int compare(Session s1, Session s2) {
                return s1.cStartDate.compareTo(s2.cStartDate);
            }
    }; 
    
    //************************************************************************************************
    //***************************************Record Class*********************************************
    //************************************************************************************************  

    /**
     * Models a Session record in the Sessions table. 
     * To simplify the implementation of data constraints and ensuring data integrity, 
     * Session objects are immutable and only the Sessions class can create instances of this type and add them to the Campuses table.
     * @author Matt Bush
     */
    public static class Session implements Record{
        private final Term cTerm;
        private final String cSessionName;
        private final UDate cStartDate;
        private final UDate cEndDate;
        
        /**
         * Private constructor only to be used by Sessions class ensures no instances of a Session will exist outside of Sessions' HashMap.
         * 
         * @param pTerm the Term of the session
         * @param pSessionName the name of the session
         * @param pStartDate the start date of the session
         * @param pEndDate the end date of the session
         * @throws IllegalArgumentException if pTerm, pSessionName, pStartDate, or pEndDate is null.
         */
        private Session (Term pTerm, String pSessionName, UDate pStartDate, UDate pEndDate) {
            if (pTerm == null)
                throw new IllegalArgumentException("A session's Term cannot be null.");
            if (pSessionName == null)
                throw new IllegalArgumentException("A session's name cannot be null.");
            if (pStartDate == null)
                throw new IllegalArgumentException("A session's start date cannot be null.");
            if (pEndDate == null)
                throw new IllegalArgumentException("A session's end date cannot be null.");
            
            String trimmedSessionName = pSessionName.trim();
            if (trimmedSessionName.isEmpty())
                throw new IllegalArgumentException("A session's name  must contain at least one non-white-space character.");            
            
            if(!pStartDate.lessThan(pEndDate))
                throw new IllegalArgumentException("A session's start date must be less than its end date.");  
            
            cTerm = pTerm;
            cSessionName = trimmedSessionName;
            cStartDate = pStartDate; //Udate guarantees objects are immutable, thus, no need to worry about setting cStartDate to a reference of unknown origin and control.
            cEndDate = pEndDate;
            
        }
        /**
         * @return the Session's Term
         */
        public Term term(){return cTerm;}
        /**
         * @return the Session's name
         */
        public String sessionName(){return cSessionName;}
        /**
         * @return the Session's start date
         */
        public UDate startDate(){return cStartDate;}
        /**
         * @return the Session's end date
         */
        public UDate endDate(){return cEndDate;}
        /**
         * Returns true if this session overlaps with the session specified by other.
         * Two sessions S1 and S1 overlap if S1.startDate &lt; S2.endDate AND S1.endDate &gt; S2.startDate
         * 
         * @param other the Session object to which to compare this one for overlap. Not null.
         * @return true if this session overlaps with the session specified by other.
         */
        public boolean overlaps(Session other){
            return (this.cStartDate.lessThan(other.cEndDate) && this.cEndDate.greaterThan(other.cStartDate));
        }        
        
        /**
         * @return "Session[subject=" + subject() + ", sessionName=" + sessionName() + ", startDate=" + startDate() + ", endDate=" + endDate() + "]"
         */
        @Override
        public String toString(){
            return "[term=" + cTerm + ", sessionName=" + cSessionName + ", startDate=" + cStartDate + ", endDate=" + cEndDate + "]";
        }
        /**
         * @return the session's primary key value, which is term().pkey() + "~" + sessionName()
         */
        public String pkey() {
            return cTerm.pkey() + "~" + cSessionName;
        }
    }
}
