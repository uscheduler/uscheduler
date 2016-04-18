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
 * A singleton class that models the Instructors table to store Instructor records.
 * @author Matt Bush
 */
public final class Instructors implements Table{
    
    /**
     * The HashMap to store Instructor objects using the instructor's pkey() as the key into the map. 
     */
    private static final HashMap<String, Instructor> cInstructors = new HashMap<>();
    
    /**
     * Private constructor to prevent instantiation and implement as a singleton class
     */
    private Instructors(){};
    
    //************************************************************************************************
    //***************************************Data Modification*****************************************
    //************************************************************************************************
    /**
     * Adds a new Instructor to the Instructors table.
     * <br><br>
     * This method first checks to see if an Instructor already exists in the Instructors table with the instructor name specified by pInstructorName. 
     * If so, this method returns the already existing Instructor. 
     * Otherwise, this method creates a new Instructor with the name specified by pInstructorName,
     * adds the newly created Instructor to the Table, and returns the newly created Instructor.
     * <br><br>
     * 
     * @param pInstructorName the name of the Instructor to add.
     * @throws IllegalArgumentException if pInstructorName is null or doesn't contain at least one non-white-space character.
     * @return the newly added Instructor if no such Instructor already existed, otherwise returns the already existing Instructor.
     */
    public static Instructor add(String pInstructorName){
        Instructor temp = new Instructor(pInstructorName);
        Instructor found = cInstructors.get(temp.pkey());
        if (found == null){
            cInstructors.put(temp.pkey(), temp);
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
        return cInstructors.size();
    }
    /**
     * Returns a list of all instructors from the Instructors table in the specified order. 
     * 
     * @param pOrder a Comparator of type Instructor that specifies how to order the returned list.
     * @return A list of all Instructor objects in the table in the order specified by pOrder.
     */
    public static ArrayList<Instructor> getAll(Comparator<Instructor> pOrder){
        ArrayList<Instructor> list = new ArrayList<>(cInstructors.values());
        Collections.sort(list, pOrder);
        return list;
    }
    //************************************************************************************************
    //***************************************Comparators*********************************************
    //************************************************************************************************  
    
     /**
     * A Comparator of type Instructor that compares two Instructor objects based on {@link Instructors.Instructor#pkey() Instructor.pkey()}
     * This Comparator will allow ordering a Collection of Instructor objects by {@link Instructors.Instructor#pkey() Instructor.pkey()} ascending.
     */
    public static final Comparator<Instructor> PK_ASC = new Comparator<Instructor>() {
            @Override
            public int compare(Instructor i1, Instructor i2) {
                return i1.pkey().compareTo(i2.pkey());
            }
    }; 
    
    //************************************************************************************************
    //***************************************Record Class*********************************************
    //************************************************************************************************   
   
    /**
     * Models an Instructor record in the Instructors table. 
     * To simplify the implementation of data constraints and ensuring data integrity, 
     * Instructor objects are immutable and only the Instructors class can create instances of this type and add them to the Instructors table.
     * @author Matt Bush
     */
    public static class Instructor implements Record{
        private final String cInstructorName;
        /**
         * Private constructor only to be used by Instructors class ensures no instances of Instructor will exist outside of Instructors' HashMap.
         * 
         * @param pInstructorName the name of the instructor
         * @throws IllegalArgumentException if pInstructorName is null.
         */
        private Instructor (String pInstructorName) {
            if (pInstructorName == null)
                throw new IllegalArgumentException("An instructor's name cannot be null.");
            String trimmedInstructorName = pInstructorName.trim();
            if (trimmedInstructorName.isEmpty())
                throw new IllegalArgumentException("An instructor's name  must contain at least one non-white-space character.");            
            cInstructorName = trimmedInstructorName;
        }
        /**
         * @return the Instructor's Name
         */
        public String instructorName(){return cInstructorName;}
        /**
         * @return "Instructor[instructorName=" + instructorName() + "]"
         */
        @Override
        public String toString(){
            return "[instructorName=" + cInstructorName + "]";
        }
        /**
         * @return the instructor's primary key value, which is instructorName()
         */
        public String pkey() {
            return cInstructorName;
        }
    }
}
