/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uscheduler.internaldata;

/**
 * The root interface of all conceptual "tables".
 * <p>This interface has no methods and exists purely for documenting shared properties of related classes so 
 * that the same documentation doesn't need to be repeated for each implementing class. 
 * <p><b>Static Singleton: </b>All implementing class model the singleton design pattern and consist of only static methods. 
 * <p><b>Nested "record" Class: </b>All implementing class define their corresponding "record" class as a nested class.
 * <p><b>Storage Structure: </b>All implementing classes store their corresponding "records" in a HashMap using the "table's" primary key as a key into the map. 
 * <br>The HashMap implementation provides to following advantages:
 * <br>1) Ensures that no "logically" equivalent "records" exist in the table, where logical equivalence is defined by the "table's" primary key.
 * <br>2) Provides access to a "record" in the map by key. A Set does not provide this capability.
 * <p><b>Strict Control and Constraint Enforcement: </b>All implementing classes ensure data integrity by:
 * <br>1) Defining only private constructors for their corresponding "record" class. 
 * This ensures that only the corresponding table class can create instances of its record class. 
 * <br>2) Making the private constructor of their corresponding "record" class perform constraint checks and to prevent creation of invalid "records".
 * <p><b>No Two Logically Equivalent Instances: </b>With the exception of the {@link uscheduler.internaldata.Schedules Schedules} class, 
 * none of the "tables" provides a means to delete a record. 
 * Additionally, table classes only provide references to one of their corresponding record classes if it has been added to its table. 
 * The combination of these two implementations ensure that any two "record" references that are logically equivalent in terms of primary key, must also be strictly equal in the sense that they refer to the same object in memory.
 * Again, the exception to this rule is with a {@link uscheduler.internaldata.Schedules.Schedule Schedule} instance, 
 * because the {@link uscheduler.internaldata.Schedules Schedules} table allows deletion, 
 * it is possible to have a reference to a {@link uscheduler.internaldata.Schedules.Schedule Schedule} object that is not in the {@link uscheduler.internaldata.Schedules Schedules} table 
 * and thus there is no guarantee that two logically equivalent references must refer to the same object in memory.
 * 
 * 
 * @author Matt
 * @see uscheduler.internaldata.Record
 */
public interface Table {
    
}
