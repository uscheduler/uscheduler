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
 * A singleton class that models the Campuses table to store Campus records.
 * 
 * @author Matt Bush
 */
public final class Campuses implements Table {

    /**
     * The HashMap to store Campus objects using the campus's pkey() as the key into the map. 
     */
    private static final HashMap<String, Campus> cCampuses = new HashMap();

    
    /**
     * Private constructor to prevent instantiation and implement as a singleton class
     */
    private Campuses(){};    
    //************************************************************************************************
    //***************************************Data Modification*****************************************
    //************************************************************************************************
    /**
     * Adds a new Campus to the Campuses table.
     * <br><br>
     * This method first checks to see if a Campus already exists in the Campuses table with the campus name specified by pCampusName. 
     * If so, this method returns the already existing Campus. 
     * Otherwise, this method creates a new Campus with the name specified by pCampusName,
     * adds the newly created Campus to the Table, and returns the newly created Campus.
     * <br><br>
     * 
     * @param pCampusName the name of the Campus to add.
     * @throws IllegalArgumentException if pCampusName is null.
     * @return the newly added Campus if no such Campus already existed, otherwise returns the already existing Campus.
     */
    public static Campus add(String pCampusName){
        Campus temp = new Campus(pCampusName);
        Campus found = cCampuses.get(temp.pkey());
        if (found == null){
            cCampuses.put(temp.pkey(), temp);
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
        return cCampuses.size();
    }    
    /**
     * Returns from the Campuses table the Campus with the specified campus name.
     * 
     * @param pCampusName the campus name of the Campus being retrieved.
     * @return the Campus whose campus name is pCampusName, or null of no such Campus exists is in the Campuses table.
     */
    public static Campus get(String pCampusName){
        return cCampuses.get(pCampusName);
    }
    /**
     * Returns a list of all campuses from the Campuses table in the specified order. 
     * 
     * @param pOrder a Comparator of type Campus that specifies how to order the returned list.
     * @return A list of all Campus objects in the table in the order specified by pOrder.
     */
    public static ArrayList<Campus> getAll(Comparator<Campus> pOrder){
        ArrayList<Campus> list = new ArrayList<>(cCampuses.values());
        Collections.sort(list, pOrder);
        return list;
    }
    //************************************************************************************************
    //***************************************Comparators*********************************************
    //************************************************************************************************  
    /**
     * A Comparator of type Campus that compares two Campus objects based on {@link Campuses.Campus#pkey()}  Campus.pkey()}.
     * This Comparator will allow ordering a Collection of Campus objects by {@link Campuses.Campus#pkey()}  Campus.pkey() ascending.
     */
    public static final Comparator<Campus> PK_ASC = new Comparator<Campus>() {
            @Override
            public int compare(Campus c1, Campus c2) {
                return c1.pkey().compareTo(c2.pkey());
            }
    };     
    //************************************************************************************************
    //***************************************Record Class*********************************************
    //************************************************************************************************    


    /**
     * Models a Campus record in the Campuses table. 
     * To simplify the implementation of data constraints and ensuring data integrity, 
     * Campus objects are immutable and only the Campuses class can create instances of this type and add them to the Campuses table.
     * @author Matt Bush
     */
    public static class Campus implements Record{
        private final String cCampusName;
        /**
         * Private constructor only to be used by Campuses class ensures no instances of Campus will exist outside of Campuses' HashMap.
         * 
         * @param pCampusName the name of the campus
         * @throws IllegalArgumentException if pCampusName is null.
         */
        private Campus (String pCampusName) {
            if (pCampusName == null || pCampusName.isEmpty())
                throw new IllegalArgumentException("A campus's name cannot be null.");
            cCampusName = pCampusName;
        }
        /**
         * @return the Campus's Name
         */
        public String campusName(){return cCampusName;}
        /**
         * @return "Campus[campusName=" + campusName() + "]"
         */
        @Override
        public String toString(){
            return "Campus[campusName=" + cCampusName + "]";
        }
        /**
         * @return the campus's primary key value, which is campusName()
         */
        public String pkey() {
            return cCampusName;
        }
    }
}
