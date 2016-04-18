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
 * A singleton class that models the Terms table to store Term records.
 * @author Matt Bush
 * 
 */
public final class Terms implements Table {
    /**
     * The HashMap to store Term objects using the term's pkey() as the key into the map. 
     */
    private static final HashMap<String, Term> cTerms = new HashMap<>();
    /**
     * Private constructor to prevent instantiation and implement as a singleton class
     */
    private Terms(){};
    
    //************************************************************************************************
    //***************************************Data Modification*****************************************
    //************************************************************************************************

    /**
     * Adds a new Terms to the Terms table.
     * <br><br>
     * This method first checks to see if a Term already exists in the Terms table with the term number specified by pTermNum. 
     * If so, this method returns the already existing Term. 
     * Otherwise, this method creates a new Term with the term number and term name specified by pTermNum and pTermName, 
     * adds the newly created Term to the Table, and returns the newly created Term.
     * <br><br>
     * 
     * @param pTermNum the term number of the Term to add.
     * @param pTermName the name of the Term to add.
     * @throws IllegalArgumentException if pTermName is null.
     * @return the newly added Term if no such Term already existed, otherwise returns the already existing Term.
     */
    public static Term add(int pTermNum, String pTermName){
        Term temp = new Term(pTermNum, pTermName);
        Term found = cTerms.get(temp.pkey());
        if (found == null){
            cTerms.put(temp.pkey(), temp);
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
        return cTerms.size();
    }
    /**
     * Returns from the Terms table the Term with the specified term number.
     * 
     * @param pTermNum the term number of the term being retrieved.
     * @return the Term whose termNum is pTermNum if such a term exists in the table. Null otherwise.
     */
    public static Term get(int pTermNum){
        return cTerms.get(Integer.toString(pTermNum));
    }
    /**
     * Method to return a list of all terms in the table in the specified order. 
     * 
     * @param pOrder a Comparator of type Term that specifies how to order the returned list.
     * @return A list of all terms in the table in the order specified by pOrder.
     */
    public static ArrayList<Term> getAll(Comparator<Term> pOrder){
        ArrayList<Term> list = new ArrayList<>(cTerms.values());
        Collections.sort(list, pOrder);
        return list;
    }
    //************************************************************************************************
    //***************************************Comparators*********************************************
    //************************************************************************************************  
    /**
     * A Comparator of type Term that compares two Term objects based on {@link uscheduler.internaldata.Terms.Term#pkey() Term.pkey()}.
     * This Comparator will allow ordering a Collection of Term objects by {@link uscheduler.internaldata.Terms.Term#pkey() Term.pkey()} descending.
     */
    public static final Comparator<Term> PK_DESC = new Comparator<Term>() {
            @Override
            public int compare(Term t1, Term t2) {
                return t2.pkey().compareTo(t1.pkey());
            }
    }; 
    //************************************************************************************************
    //***************************************Record Class*********************************************
    //************************************************************************************************  

    /**
     * Models a Term record in the Terms table.
     * To simplify the implementation of data constraints and ensuring data integrity, 
     * Term objects are immutable and only the Terms class can create instances of this type and add them to the Terms table.
     * @author Matt Bush
     */
    public static class Term implements Record{
        private  final int cTermNum;
        private  final String cTermName;
        /**
         * Private constructor only to be used by Terms class ensures no instances of Term will exist outside of Terms' HashMap.
         * 
         * @param pTermNum the term number
         * @param pTermName the term name
         * @throws IllegalArgumentException if pTermName is null.
         */
        private Term (int pTermNum, String pTermName) {
            if (pTermName == null)
                throw new IllegalArgumentException("A terms's name cannot be null.");
            cTermNum = pTermNum;
            cTermName = pTermName;
        }
        /**
         * @return the Term's term number
         */
        public int termNum(){return cTermNum;}
        /**
         * @return the Term's Name
         */
        public String termName(){return cTermName;}
        /**
         * @return "Term[termNum=" + termNum() + ", termName=" + termName() + "]"
         */
        @Override
        public String toString(){
            return "[termNum=" + cTermNum + ", termName=" + cTermName + "]";
        }
        /**
         * @return the term's primary key value, which is "" + termNum()
         */
        public String pkey() {
            return "" + cTermNum;
        }
    }
}
