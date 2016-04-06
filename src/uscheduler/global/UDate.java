/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uscheduler.global;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.concurrent.TimeUnit;

/**
 * A simple class to model a date without modeling time.
 * Internal, this class stores a date with year, month, and day of month precision. 
 * 
 * <p>This class is designed to represent the data type of a Session start date and end date.
 * To simplify the implementation of data constraints and ensuring data integrity associated with a Session's start date and end date, 
 * this class is designed so that objects of this type are immutable. Thus, this class ensures that a UDate object's value can not change after construction.
 * @author Matt Bush
 */
public class UDate implements Comparable<UDate>{
    private final int cYear;
    private final int cMonth;
    private final int cDayOfMonth;
    private final String cStringRep;
    
    /**
     * Private constructor to be used by the copy() method. Since private, no need to validate inputs.
     */
    private UDate(int pYear, int pMonth, int pDay, String pStringRep){
        cYear = pYear;
        cMonth = pMonth;
        cDayOfMonth = pDay;
        cStringRep = pStringRep;
    }
    /**
     * Construct and returns the Calendar representation the this UDate
     * @return the Calendar representation of the specified UDate
     */  
    private Calendar calendar(){
        Calendar cal = new GregorianCalendar();
        cal.set(this.cYear, this.cMonth - 1, this.cDayOfMonth);
        return cal;
    }
    // KSU Date format = Dec 12, 2016
    /**
     * Constructs a new UDate from a String representation of a date.
     * <p>The expected format of the string is "MMM d, y", where:
     * <br><i>MMM</i> is the month abbreviation or full name
     * <br><i>d</i> is a one or two digit day of month
     * <br><i>y</i> is a two or four digit year
     * <p>Examples of valid strings are: "Dec 12, 2016", "Dec 12, 16", "December 12, 16"
     * @param pDate the string representation of the date from which to construct the UDate.
     * @throws ParseException if the specified date string cannot be parsed.
     */
    public UDate(String pDate) throws ParseException{
        Calendar cal = new GregorianCalendar();
        cal.setTime(new SimpleDateFormat("MMM d, y").parse(pDate));
        cYear = cal.get(Calendar.YEAR);
        //Note: Calendar class returns month index, starting at 0. Thus Jan = 1, Feb = 1, Mar = 3, .. etc. This is not intuitive and thus cMonth does not store this way.
        cMonth = cal.get(Calendar.MONTH) + 1; 
        cDayOfMonth = cal.get(Calendar.DAY_OF_MONTH);
        cStringRep = pDate;
    }
    /**
     * Returns a new UDate that is a copy of this Udate.
     * @return a new UDate that is a copy of this UDate
     */
    public UDate copy(){
        return new UDate(this.cYear, this.cMonth, this.cDayOfMonth, this.cStringRep);
    }

    /**
     * Returns the distance, in terms of days, from this UDate to some other UDate.
     * The distance is positive if this UDate is less than the other UDate, 0 if this UDate equals the other UDate, negative otherwise.
     * @param pOther the other date in which to calculate the distance to
     * @return the distance, in terms of days, from this UDate to pOther
     */  
    public int daysTo(UDate pOther){
        Calendar thisCal = this.calendar();
        Calendar otherCal = pOther.calendar();
        long diff = otherCal.getTimeInMillis() - thisCal.getTimeInMillis();
        return (int) TimeUnit.DAYS.convert(diff, TimeUnit.MILLISECONDS);
    }

    /**
     * Returns true if this UDate is less than some other UDate.
     * @param pOther the other date in which to test if this UDate is less than. Not null.
     * @return true if this UDate is less than pOther
     */  
    public boolean lessThan(UDate pOther){
        if (this.cYear != pOther.cYear)
            return this.cYear < pOther.cYear;
        if (this.cMonth != pOther.cMonth)
            return this.cMonth < pOther.cMonth;
        
        return this.cDayOfMonth < pOther.cDayOfMonth;   
    }  
    
    /**
     * Returns true if this UDate is greater than some other UDate.
     * @param pOther the other date in which to test if this UDate is greater than. Not null.
     * @return true if this UDate is greater than pOther
     */  
    public boolean greaterThan(UDate pOther){
        if (this.cYear != pOther.cYear)
            return this.cYear > pOther.cYear;
        if (this.cMonth != pOther.cMonth)
            return this.cMonth > pOther.cMonth;
        
        return this.cDayOfMonth > pOther.cDayOfMonth;   
    }     
    
    /**
     * Returns the string from which this UDate was constructed
     * @return the string from which this UDate was constructed
     */
    @Override
    public String toString(){return this.cStringRep;}

    /**
     * Returns true if this UDate is equal to the provided object.
     * If obj is null or obj isn't an instance of UDate, false is returned. 
     * Otherwise, this UDate is equal to the other UDate if and only if their year, month, and day are equal.

     * @return true if this MeetingTime is equal to the provided object.
     */
    @Override
    public boolean equals(Object obj){
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final UDate other = (UDate) obj;
        return (this.cYear == other.cYear) && (this.cMonth == other.cMonth) && (this.cDayOfMonth == other.cDayOfMonth);
    }
    /**
     * Returns this UDate's hash code which is based on its year, month, and day.
     * @return this UDate's hash code.
     */
    @Override
    public int hashCode(){
        int hash = 17;
        hash = hash * 31 + this.cYear;
        hash = hash * 31 + this.cMonth;
        hash = hash * 31 + this.cDayOfMonth;
        return hash;
    }
    /**
     * Compares this UDate with another UDate.
     * @param pOther the UDate to compare to this UDate
     * @return a negative number if this UDate is less than pOther, 0 if this UDate is equal to pOther, a positive number otherwise
     */
    @Override
    public int compareTo(UDate pOther) {
        if (this.cYear != pOther.cYear)
            return this.cYear - pOther.cYear;
        if (this.cMonth != pOther.cMonth)
            return this.cMonth - pOther.cMonth;
        return this.cDayOfMonth - pOther.cDayOfMonth;  
    }
}
