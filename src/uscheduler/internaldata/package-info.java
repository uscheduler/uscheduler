/**
 * This package contains all the classes that implement the model in the MVC architecture of our project.
 * <p>This package contains the classes needed to model the entities in the problem domain and to provide the minimally needed operations on each entity.
 * This package has been designed to appear like a database in the sense that each entity in the problem domain has 
 * a corresponding "table" class that stores instances of its entity types as "records". 
 * The terms "table" and "record" are used purely for conceptual purposes since this package provides no means to store entity data in a database or persistently.
 * <p><b>Responsibilities:</b>The primary responsibilities of this package are to:
 * <br>1) Provide the storage structures ("tables" and "records") to model entities in the problem domain.
 * <br>2) Interact with the uscheduler.externaldata package to import the needed KSU data into the storage structures.
 * <br>3) Provide methods to query data in the storage structures
 * 
 */
package uscheduler.internaldata;
