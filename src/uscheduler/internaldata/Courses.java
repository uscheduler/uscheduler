/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uscheduler.internaldata;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import uscheduler.internaldata.Subjects.Subject;


/**
 * A singleton class that models the Courses table to store Course records.
 *
 * @author Matt Bush
 * 
 */
public final class Courses implements Table {
     
     /**
     * The HashMap to store Course objects using the course's pkey() as the key into the map. 
     */
    private static final HashMap<String, Course> cCourses = new HashMap();
    /**
     * Private constructor to prevent instantiation and implement as a singleton class
     */
    private Courses(){};
    
    //************************************************************************************************
    //***************************************Data Modification*****************************************
    //************************************************************************************************
    /**
     * Adds a new Course to the Courses table.
     * <p>This method first checks to see if a Course already exists in the Courses table with the Subject specified by pSubj and the course number specified by pCourseNum. 
     * If so, this method returns the already existing Course. 
     * Otherwise, this method creates a new Course with the Subject and course number specified by pSubj and pCourseNum, 
     * adds the newly created Course to the Table, and returns the newly created Course.
     * 
     * @param pSubj the Subject of the Course to add.
     * @param pCourseNum the course number of the Course to add.
     * @throws IllegalArgumentException if pSubject or pCourseNum is null or doesn't contain at least one non-white-space character.
     * @return the newly added Course if no such Course already existed, otherwise returns the already existing Course.
     */
    public static Course add(Subject pSubj, String pCourseNum){
        Course temp = new Course(pSubj, pCourseNum);
        Course found = cCourses.get(temp.pkey());
        if (found == null){
            cCourses.put(temp.pkey(), temp);
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
        return cCourses.size();
    }

    /**
     * Returns from the Courses table the Course with the specified Subject and course number.
     * 
     * @param pSubj the Subject of the course being retrieved.
     * @param pCourseNum the course number of the course being retrieved.
     * @return the Course whose Subject has the same pkey() as pSubj.pkey() and whose course number is pCourseNum, if such a course exists in the table. Null otherwise.
     */
    public static Course get(Subject pSubj, String pCourseNum){
        return cCourses.get(pSubj.pkey() + "~" + pCourseNum);
    }
    /**
     * Returns from the Courses table all Courses, in no particular order.
     * This method is used for testing and debugging and does not have a use in the final app.
     * 
     * @return ArrayList of all Courses in the Courses table. 
     */
    public static ArrayList<Course> getAll(){
        return new ArrayList<>(cCourses.values());
    }
    //************************************************************************************************
    //***************************************Comparators*********************************************
    //************************************************************************************************  
    /**
     * A Comparator of type Course that compares two Course objects based on {@link Courses.Course#pkey() Course.pkey()}
     * This Comparator will allow ordering a Collection of Course objects by {@link Courses.Course#pkey() Course.pkey()} ascending.
     */
    public static final Comparator<Course> PK_ASC = new Comparator<Course>() {
            @Override
            public int compare(Course c1, Course c2) {
                return c1.pkey().compareTo(c2.pkey());
            }
    };   
    //************************************************************************************************
    //***************************************Record Class*********************************************
    //************************************************************************************************    
    
    /**
     * Models a Course record in the Courses table. 
     * To simplify the implementation of data constraints and ensuring data integrity, 
     * Course objects are immutable and only the Courses class can create instances of this type and add them to the Courses table.
     * @author Matt Bush
     */
    public static class Course implements Record{
        private final Subject cSubject;
        private final String cCourseNum;
        /**
         * Private constructor only to be used by Courses class ensures no instances of Course will exist outside of Courses' HashMap.
         * 
         * @param pSubject the Subject of the course
         * @param pCourseNum the course number of the course
         * @throws IllegalArgumentException if pSubject or pCourseNum is null.
         */
        private Course (Subject pSubject, String pCourseNum) {
            if (pSubject == null)
                throw new IllegalArgumentException("A course's Subject cannot be null.");
            if (pCourseNum == null)
                throw new IllegalArgumentException("A course's course number cannot be null.");
            String trimmedCourseNum = pCourseNum.trim();
            if (trimmedCourseNum.isEmpty())
                throw new IllegalArgumentException("A course's course number must contain at least one non-white-space character.");
            cSubject = pSubject;
            cCourseNum = trimmedCourseNum;
        }
        /**
         * @return the Course's Subject
         */
        public Subject subject(){return cSubject;}
        /**
         * @return the Course's course number
         */
        public String courseNum(){return cCourseNum;}
        /**
         * @return "Course[subject=" + subject() + ", courseNum=" + courseNum() + "]"
         */
        @Override
        public String toString(){
            return "Course[subject=" + cSubject + ", courseNum=" + cCourseNum + "]";
        }
        /**
         * @return the course's primary key value, which is subject().pkey() + "~" + courseNum()
         */
        public String pkey() {
            return cSubject.pkey() + "~" + cCourseNum;
        }
    }
}
