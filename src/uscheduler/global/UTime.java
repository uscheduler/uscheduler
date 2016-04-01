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

/**
 * A simple class to model a time of day without modeling second precision, time zones, or a date.
 * Internal, this class stores a time with hour and minute precision.
 * 
 * <p>This class is designed to aid the implementation of a {@link uscheduler.internaldata.Sections.MeetingTime MeetingTime}'s start time and end time.
 * To simplify the implementation of data constraints and ensuring data integrity associated with a MeetingTime's start time and end time, 
 * this class is designed so that objects of this type are immutable. Thus, this class ensures that a UTime object's value can not change after construction.
 * 
 * @author Matt
 */
public class UTime implements Comparable<UTime>{
    //Hour is stored in 24 hour format, with midnight as the earliest part of day and interanlly represented as cHour = 0, cMinute = 0
    private final int cHour;
    private final int cMinute;
    private final String cStringRep;
    
    /**
     * Private constructor to be used by the copy() method. Since private, no need to validate inputs.
     */
    private UTime(int pHour, int pMinute, String pStringRep){
        cHour = pHour;
        cMinute = pMinute;
        cStringRep = pStringRep;
    }
    // KSU Time format = "10:00 am"
    /**
     * Constructs a new UTime from a String representation of a time.
     * <p>The expected format of the string is "h:mm a", where:
     * <br><i>h</i> is a one or two digit hour value using a 12 hour format. 
     * <br><i>mm</i> is two digit minute value
     * <br><i>a</i> is one of "am" or "pm"
     * <p>Examples of valid strings are: "10:00 am", "1:35 pm"
     * <p><b>Midnight and Noon: </b> "12:00 am" is parsed as midnight and is considered the earliest time of the day. "12:00 pm" is parsed as noon. 
     * Thus, 12:00 am (midnight) &lt; 12:01 am, and 12:00 pm (noon) &lt;  11:59 pm (last minute of day)
     * @param pTime the string representation of the time from which to construct the UTime.
     * @throws ParseException 
     */
    public UTime(String pTime) throws ParseException{
        Calendar cal = new GregorianCalendar();
        cal.setTime(new SimpleDateFormat("h:mm a").parse(pTime));
        cHour = cal.get(Calendar.HOUR_OF_DAY);
        cMinute = cal.get(Calendar.MINUTE); 
        cStringRep = pTime;
    }
    /**
     * Returns a new UTime that is a copy of this UTime.
     * @return a new UTime that is a copy of this UTime
     */
    public UTime copy(){
        return new UTime(this.cHour, this.cMinute, this.cStringRep);
    }

    /**
     * Returns the distance, in terms of minutes, from this UTime to some other UTime.
     * The distance is positive if this UTime is less than the other UTime, 0 if this UTime equals the other UTime, negative otherwise.
     * @param pOther the other UTime in which to calculate the distance to
     * @return the distance, in terms of minutes, from this UTime to pOther
     */  
    public int minutesTo(UTime pOther){
        return (this.cHour * 24 + this.cHour) - (pOther.cHour * 24 + pOther.cHour);
    }

    /**
     * Returns true if this UTime is less than some other UTime. 
     * "12:00 am" (midnight) is the earliest minute of the day while "12:59 pm" is the last minute of the day.
     * @param pOther the other Time in which to test if this UTime is less than
     * @return true if this UTime is less than pOther
     */  
    public boolean lessThan(UTime pOther){
        if (this.cHour != pOther.cHour)
            return this.cHour < pOther.cHour;
        return this.cMinute < pOther.cMinute;
    }  
    
    /**
     * Returns the string from which this UTime was constructed
     * @return the string from which this UTime was constructed
     */
    @Override
    public String toString(){return this.cStringRep;}

    /**
     * Returns true if this UTime is equal to the provided object.
     * If obj is null or obj isn't an instance of UTime, false is returned. 
     * Otherwise, this UTime is equal to the other UTime if and only if their hours and minute are equal.

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
        final UTime other = (UTime) obj;
        return (this.cHour == other.cHour) && (this.cMinute == other.cMinute) ;
    }
    /**
     * Returns this UTime's hash code which is based on its hour and minute.
     * @return this UTime's hash code.
     */
    @Override
    public int hashCode(){
        int hash = 17;
        hash = hash * 31 + this.cHour;
        hash = hash * 31 + this.cMinute;
        return hash;
    }
    /**
     * Compares this UTime with another UTime.
     * @param pOther the UTime to compare to this UTime
     * @return a negative number if this UTime is less than pOther, 0 if this UTime is equal to pOther, a positive number otherwise
     */
    @Override
    public int compareTo(UTime pOther) {
        if (this.cHour != pOther.cHour)
            return this.cHour - pOther.cHour;
        return this.cMinute - pOther.cMinute;
    }
}
