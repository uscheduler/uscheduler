/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uscheduler.internaldata;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;


/**
 * A singleton class that models the Subjects table to store Subject records.
 * @author Matt Bush
 * 
 */
public final class Subjects implements Table{

    /**
     * The HashMap to store Subject objects using the subject's pkey() as the key into the map.
     */
    private static final HashMap<String, Subject> cSubjects = new HashMap<>();
    /**
     * Private constructor to prevent instantiation and implement as a singleton class
     */
    private Subjects(){};
    
    //************************************************************************************************
    //***************************************Data Modification*****************************************
    //************************************************************************************************
    
     /**
     * Adds a new Subject to the Subjects table.
     * <br><br>
     * This method first checks to see if a Subject already exists in the Subjects table with the abbreviation specified by pSubjAbbr. 
     * If so, this method returns the already existing Subject. 
     * Otherwise, this method creates a new Subject with the abbreviation and name specified by pSubjAbbr and pSubjName, 
     * adds the newly created Subject to the Table, and returns the newly created Subject.
     * <br><br>
     * 
     * @param pSubjAbbr the abbreviation of the Subject to add.
     * @param pSubjName the name of the Subject to add.
     * @throws IllegalArgumentException if pSubjAbbr or pSubjName is null or if pSubjAbbr doesn't contain at least one non-white-space character.
     * @return the newly added Subject if no such Subject already existed, otherwise returns the already existing Subject.
     */
    public static Subject add(String pSubjAbbr, String pSubjName){
        Subject temp = new Subject(pSubjAbbr, pSubjName);
        Subject found = cSubjects.get(temp.pkey());
        if (found == null){
            cSubjects.put(temp.pkey(), temp);
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
        return cSubjects.size();
    }
    /**
     * Returns from the Subjects table the Subject with the specified subject abbreviation.
     * 
     * @param pSubjAbbr the Subject abbreviation of the Subject being retrieved.
     * @return the Subject whose Subject abbreviation is pSubjAbbr or null of no such subject is in the Subjects table.
     */
    public static Subject get(String pSubjAbbr){
        return cSubjects.get(pSubjAbbr);
    }
    /**
     * Returns a list of all subjects from the Subjects table in the specified order. 
     * 
     * @param pOrder a Comparator of type Subject that specifies how to order the returned list.
     * @return A list of all subjects in the table in the order specified by pOrder.
     */
    public static ArrayList<Subject> getAll(Comparator<Subject> pOrder){
        ArrayList<Subject> list = new ArrayList<>(cSubjects.values());
        Collections.sort(list, pOrder);
        return list;
    }
    
    //************************************************************************************************
    //***************************************Comparators*********************************************
    //************************************************************************************************  
    
    /**
     * A Comparator of type Subject that compares two Subject objects based on {@link Subjects.Subject#pkey() Subject.pkey()}
     * This Comparator will allow ordering a Collection of Subject objects by {@link Subjects.Subject#pkey() Subject.pkey()} ascending.
     */
    public static final Comparator<Subject> PK_ASC = new Comparator<Subject>() {
            @Override
            public int compare(Subject s1, Subject s2) {
                return s1.pkey().compareTo(s2.pkey());
            }
    }; 
    
    

    //************************************************************************************************
    //***************************************Record Class*********************************************
    //************************************************************************************************  
    
    /**
     * Models a Subject record in the Subjects table.
     * To simplify the implementation of data constraints and ensuring data integrity, 
     * Subject objects are immutable and only the Subjects class can create instances of this type and add them to the Subjects table.
     * @author Matt Bush
     */
    public static class Subject implements Record{
        private final String cSubjectAbbr;
        private final String cSubjectName;
        /**
         * Private constructor only to be used by Subjects class ensures no instances of Subject will exist outside of Subjects' HashMap.
         * 
         * @param pSubjAbbr the abbreviation of the subject
         * @param pSubjName the name of the subject
         * @throws IllegalArgumentException if pSubjAbbr or pSubjName is null.
         */
        private Subject (String pSubjAbbr, String pSubjName) {
            if (pSubjAbbr == null)
                throw new IllegalArgumentException("A subject's abbreviation cannot be null.");
            if (pSubjName == null)
                throw new IllegalArgumentException("A subject's name cannot be null.");
            String trimmedSubjAbbr = pSubjAbbr.trim();
            if (trimmedSubjAbbr.isEmpty())
                throw new IllegalArgumentException("A subject's abbreviation must contain at least one non-white-space character.");      
            cSubjectAbbr = trimmedSubjAbbr;
            cSubjectName = pSubjName;
        }
        /**
         * @return the Subject's abbreviation
         */
        public String subjectAbbr(){return cSubjectAbbr;}
        /**
         * @return the Subject's Name
         */
        public String subjectName(){return cSubjectName;}
        /**
         * @return "Subject[subjectAbbr=" + subjectAbbr() + ", subjectName=" + subjectName() + "]"
         */
        @Override
        public String toString(){
            return "[subjectAbbr=" + cSubjectAbbr + ", subjectName=" + cSubjectName + "]";
        }
        /**
         * @return the subject's primary key value, which is subjectAbbr()
         */
        public String pkey() {
            return cSubjectAbbr;
        }
    }
}
