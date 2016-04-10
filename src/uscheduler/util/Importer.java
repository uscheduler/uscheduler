/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uscheduler.util;

import java.io.File;
import uscheduler.global.UDate;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import uscheduler.externaldata.HTMLFormatException;
import uscheduler.externaldata.NoDataFoundException;
import uscheduler.externaldata.ScheduleSearchPageParser;
import uscheduler.externaldata.SectionsPageParser;
import uscheduler.externaldata.TermsPageParser;
import uscheduler.internaldata.Campuses;
import uscheduler.internaldata.Courses;
import uscheduler.internaldata.Instructors;
import uscheduler.internaldata.Instructors.Instructor;
import uscheduler.internaldata.Sections;
import uscheduler.internaldata.Sections.Section;
import uscheduler.internaldata.Sections.UnattachedMeetingTime;
import uscheduler.internaldata.Sessions;
import uscheduler.internaldata.Subjects;
import uscheduler.internaldata.Terms;

/**
 *
 * @author Matt
 */
public final class Importer {
    
    private Importer(){};
    
     /**
     * Extracts terms from KSU and imports them into the Terms table. 
     * <p>More specifically, extracts terms from KSU via a call to {@link uscheduler.externaldata.TermsPageParser#parseFromWeb() externaldata.TermsPageParser.parseFromWeb} 
     * then imports the 3 most recent terms into the Terms table via calls to {@link uscheduler.internaldata.Terms#add(int, java.lang.String) uscheduler.internaldata.Terms#add}
     * 
     * @throws uscheduler.externaldata.HTMLFormatException See: {@link uscheduler.externaldata.TermsPageParser#parseFromWeb() TermsPageParser.parseFromWeb} for details.
     * @throws java.io.IOException See: {@link uscheduler.externaldata.TermsPageParser#parseFromWeb() TermsPageParser.parseFromWeb} for details.
     */
    public static void loadTerms() throws HTMLFormatException, IOException{
        LinkedList<TermsPageParser.HTMLTerm> hTerms = TermsPageParser.parseFromWeb();
        for (int i = 0; i < 3 && i < hTerms.size()-1; i++){
           TermsPageParser.HTMLTerm ht = hTerms.get(i);
           Terms.add(ht.termNum(), ht.termName());
        }
    } 
    
     /**
     * Extracts subjects and campuses from KSU and imports them into the Subjects and Campuses table. 
     * <p>More specifically, extracts subjects and campuses from KSU via a call to 
     * {@link uscheduler.externaldata.ScheduleSearchPageParser#parseFromWeb(int) ScheduleSearchPageParser.parseFromWeb(t)} for each Term t in the Terms table.
     * Then imports the parsed Campuses and Subjects into the Subjects and Campuses table via calls to: 
     * {@link uscheduler.internaldata.Campuses#add(java.lang.String)  uscheduler.internaldata.Campuses#add} and {@link uscheduler.internaldata.Subjects#add(java.lang.String, java.lang.String)  uscheduler.internaldata.Subjects#add}
     * 
     * @throws java.io.IOException See: {@link uscheduler.externaldata.ScheduleSearchPageParser#parseFromWeb(int) parseFromWeb} for details.
     * @throws uscheduler.externaldata.HTMLFormatException See: {@link uscheduler.externaldata.ScheduleSearchPageParser#parseFromWeb(int) ScheduleSearchPageParser.parseFromWeb} for details.
     * @throws uscheduler.externaldata.NoDataFoundException See: {@link uscheduler.externaldata.ScheduleSearchPageParser#parseFromWeb(int) ScheduleSearchPageParser.parseFromWeb} for details.
     */
    public static void loadSubjectsAndCampuses() throws HTMLFormatException, IOException, NoDataFoundException{
        ArrayList<Terms.Term> terms = Terms.getAll(Terms.PK_DESC);
        for (Terms.Term t : terms){
            ScheduleSearchPageParser.ScheduleSearchPageResult sspr = ScheduleSearchPageParser.parseFromWeb(t.termNum());
            for(ScheduleSearchPageParser.HTMLSubject hSubj : sspr.subjects()){
                Subjects.add(hSubj.subjAbbr(), hSubj.subjName());
            }
            for(String hCamp : sspr.campuses()){
                Campuses.add(hCamp);
            }
        }
    }
    

    //************************************************************************************************
    //***************************************Sections*************************************************
    //************************************************************************************************
     /**
     * Extracts sections, instructors, sessions, and courses from A local file and imports into the corresponding tables.
     * 
     * @deprecated <p><b>NOTE:</b>This method is for testing and debugging purposes only and will not be used in the final product.
     * 
     * @param pTerm the Term  of the Sections to parse and load.
     * @throws IOException if the file could not be opened and read
     * @throws uscheduler.externaldata.HTMLFormatException if the file does not have the HTML structure expected of the Sections Page.
     * @throws uscheduler.externaldata.NoDataFoundException If the file represents a Sections page that was generated with a term number in which KSU returned a page with no results.  
     */
    @Deprecated
    public static void loadSectionsFromFile(Terms.Term pTerm) throws HTMLFormatException, IOException, NoDataFoundException{
        File f;
        switch (pTerm.termNum()) {
            case 201601:  f = new File("C:\\MyApps\\myJava\\UScheduler2\\src\\uscheduler2\\2016_Spring_All.txt");
                     break;
            case 201605:  f = new File("C:\\MyApps\\myJava\\UScheduler2\\src\\uscheduler2\\2016_Summer_All.txt");
                     break;
            case 201608:  f = new File("C:\\MyApps\\myJava\\UScheduler2\\src\\uscheduler2\\2016_Fall_All.txt");
                     break;
            default: f = null;
                     break;
        }
        LinkedList<SectionsPageParser.HTMLSection> parsedSections = SectionsPageParser.parseFromFile(f, pTerm.termNum());
        loadSections(pTerm, parsedSections);
    }    
     /**
     * Extracts sections, instructors, sessions, and courses from KSU and imports into the corresponding tables.
     * <p>More specifically, extracts sections from KSU via a call to 
     * {@link uscheduler.externaldata.SectionsPageParser#parseFromWeb(int, String, String) SectionsPageParser.parseFromWeb(pTerm.termNum(), pSubject.subjectAbbr(), pCourseNum)}.
     * Then extracts from the parsed sections, the Sections, Instructors, Sessions, and Courses, then imports into the corresponding tables via calls to:
     * {@link uscheduler.internaldata.Sections#add(int, uscheduler.internaldata.Sessions.Session, uscheduler.internaldata.Courses.Course, uscheduler.internaldata.Campuses.Campus, java.lang.String, uscheduler.global.InstructionalMethod, int, int, java.util.Set, java.util.Collection)   Sections.add}, 
     * {@link uscheduler.internaldata.Instructors#add(java.lang.String)  Instructors.add},
     * {@link uscheduler.internaldata.Sessions#add(uscheduler.internaldata.Terms.Term, java.lang.String, uscheduler.global.UDate, uscheduler.global.UDate)  Sessions.add}, and
     * {@link uscheduler.internaldata.Courses#add(uscheduler.internaldata.Subjects.Subject, java.lang.String)   Courses.add}
     * 
     * @param pTerm the Term  to use in the call to {@link uscheduler.externaldata.SectionsPageParser#parseFromWeb(int, String, String) SectionsPageParser.parseFromWeb(pTerm.termNum(), pSubject.subjectAbbr(), pCourseNum)}.
     * @param pSubject the Subject to use in the call to {@link uscheduler.externaldata.SectionsPageParser#parseFromWeb(int, String, String) SectionsPageParser.parseFromWeb(pTerm.termNum(), pSubject.subjectAbbr(), pCourseNum)}.
     * @param pCourseNum the course number  to use in the call to {@link uscheduler.externaldata.SectionsPageParser#parseFromWeb(int, String, String) SectionsPageParser.parseFromWeb(pTerm.termNum(), pSubject.subjectAbbr(), pCourseNum)}.
     * @throws java.io.IOException See: {@link uscheduler.externaldata.SectionsPageParser#parseFromWeb(int, String, String) SectionsPageParser.parseFromWeb} for details.
     * @throws uscheduler.externaldata.HTMLFormatException See: {@link uscheduler.externaldata.SectionsPageParser#parseFromWeb(int, String, String) SectionsPageParser.parseFromWeb} for details.
     * @throws uscheduler.externaldata.NoDataFoundException See: {@link uscheduler.externaldata.SectionsPageParser#parseFromWeb(int, String, String) SectionsPageParser.parseFromWeb} for details.
     */
    public static void loadSections(Terms.Term pTerm, Subjects.Subject pSubject, String pCourseNum) throws HTMLFormatException, IOException, NoDataFoundException{
        LinkedList<SectionsPageParser.HTMLSection> parsedSections = SectionsPageParser.parseFromWeb(pTerm.termNum() , pSubject.subjectAbbr(), pCourseNum);
        loadSections(pTerm, parsedSections);
    }
     /**
     * Extracts sections, instructors, sessions, and courses from KSU and imports into the corresponding tables.
     * <p>More specifically, extracts sections from KSU via a call to 
     * {@link uscheduler.externaldata.SectionsPageParser#parseFromWeb(int, String) SectionsPageParser.parseFromWeb(pTerm.termNum(), pSubject.subjectAbbr())}.
     * Then extracts from the parsed sections, the Sections, Instructors, Sessions, and Courses, then imports into the corresponding tables via calls to:
     * {@link uscheduler.internaldata.Sections#add(int, uscheduler.internaldata.Sessions.Session, uscheduler.internaldata.Courses.Course, uscheduler.internaldata.Campuses.Campus, java.lang.String, uscheduler.global.InstructionalMethod, int, int, java.util.Set, java.util.Collection)  Sections.add}, 
     * {@link uscheduler.internaldata.Instructors#add(java.lang.String)   Instructors.add},
     * {@link uscheduler.internaldata.Sessions#add(uscheduler.internaldata.Terms.Term, java.lang.String, uscheduler.global.UDate, uscheduler.global.UDate)  Sessions.add}, and
     * {@link uscheduler.internaldata.Courses#add(uscheduler.internaldata.Subjects.Subject, java.lang.String)   Courses.add}
     * 
     * 
     * @param pTerm the Term  to use in the call to {@link uscheduler.externaldata.SectionsPageParser#parseFromWeb(int, String) SectionsPageParser.parseFromWeb(pTerm.termNum(), pSubject.subjectAbbr())}.
     * @param pSubject the Subject to use in the call to {@link uscheduler.externaldata.SectionsPageParser#parseFromWeb(int, String) SectionsPageParser.parseFromWeb(pTerm.termNum(), pSubject.subjectAbbr())}.

     * @throws java.io.IOException See: {@link uscheduler.externaldata.SectionsPageParser#parseFromWeb(int, String) SectionsPageParser.parseFromWeb} for details.
     * @throws uscheduler.externaldata.HTMLFormatException See: {@link uscheduler.externaldata.SectionsPageParser#parseFromWeb(int, String) SectionsPageParser.parseFromWeb} for details.
     * @throws uscheduler.externaldata.NoDataFoundException See: {@link uscheduler.externaldata.SectionsPageParser#parseFromWeb(int, String) SectionsPageParser.parseFromWeb} for details.
     */
    public static void loadSections(Terms.Term pTerm, Subjects.Subject pSubject) throws HTMLFormatException, IOException, NoDataFoundException{
        LinkedList<SectionsPageParser.HTMLSection> parsedSections = SectionsPageParser.parseFromWeb(pTerm.termNum() , pSubject.subjectAbbr());
        loadSections(pTerm, parsedSections);
    }
     /**
     * Extracts sections, instructors, sessions, and courses from KSU and imports into the corresponding tables.
     * <p>More specifically, extracts sections from KSU via a call to 
     * {@link uscheduler.externaldata.SectionsPageParser#parseFromWeb(int) SectionsPageParser.parseFromWeb(pTerm.termNum())}.
     * Then extracts from the parsed sections, the Sections, Instructors, Sessions, and Courses, then imports into the corresponding tables via calls to:
     * {@link uscheduler.internaldata.Sections#add(int, uscheduler.internaldata.Sessions.Session, uscheduler.internaldata.Courses.Course, uscheduler.internaldata.Campuses.Campus, java.lang.String, uscheduler.global.InstructionalMethod, int, int, java.util.Set, java.util.Collection) Sections.add}, 
     * {@link uscheduler.internaldata.Instructors#add(java.lang.String) Instructors.add()},
     * {@link uscheduler.internaldata.Sessions#add(uscheduler.internaldata.Terms.Term, java.lang.String, uscheduler.global.UDate, uscheduler.global.UDate)   Sessions.add},
     * and
     * {@link uscheduler.internaldata.Courses#add(uscheduler.internaldata.Subjects.Subject, java.lang.String) Courses.add}
     * 
     *@deprecated <p><b>NOTE:</b>This method is for testing and debugging purposes only and will not be used in the final product.
     * 
     * @param pTerm the Term  to use in the call to {@link uscheduler.externaldata.SectionsPageParser#parseFromWeb(int) SectionsPageParser.parseFromWeb(pTerm.termNum())}.

     * @throws java.io.IOException See: {@link uscheduler.externaldata.SectionsPageParser#parseFromWeb(int) SectionsPageParser.parseFromWeb} for details.
     * @throws uscheduler.externaldata.HTMLFormatException See: {@link uscheduler.externaldata.SectionsPageParser#parseFromWeb(int) SectionsPageParser.parseFromWeb} for details.
     * @throws uscheduler.externaldata.NoDataFoundException See: {@link uscheduler.externaldata.SectionsPageParser#parseFromWeb(int) SectionsPageParser.parseFromWeb} for details.
     */
    @Deprecated 
    public static void loadSections(Terms.Term pTerm) throws HTMLFormatException, IOException, NoDataFoundException{
        LinkedList<SectionsPageParser.HTMLSection> parsedSections = SectionsPageParser.parseFromWeb(pTerm.termNum());
        loadSections(pTerm, parsedSections);
    }
/**
     * Private method to transform and load parsed HTMLSections into corresponding records in the Sections, Instructors, Sessions, and Courses tables.
     * 
     * @param pTerm the Term in which all the of parsed sections apply
     * @param pParsedSections the parsed sections from which to load data into the corresponding "tables" 
     */
    private static void loadSections(Terms.Term pTerm, LinkedList<SectionsPageParser.HTMLSection> pParsedSections){
        HashSet<Instructor> setInstructors = new HashSet<>();
        LinkedList<UnattachedMeetingTime> llUnattachedMTs = new LinkedList();
        /**
         * Call loadSessions with the list of parsed Importer. 
         * loadSessions() will iterate through each parsed section, extracting the min start date and max end date of each of each section 
         * associated with a given session and applying the min start date and max end date to the corresponding session start date and end dates.
         * loadSessions() ensures that each session in this parsed list of sections has been added to the sessions table with the most appropriate start and end dates.
         */
        Importer.loadSessions(pTerm, pParsedSections);
          /**
         * Iterate through each parsed section, adding the corresponding transformed Section to the Sections table.
         * Parsed section that have no HTMLMeetingPlaceTimes will not be transformed and added to the Sections table. 
         * This is because the HTMLMeetingPlaceTimes contain InstructionalMethod, Campus, session dates, and Instructors info.
         * KSU puts this info in the html table that corresponds to a HTMLMeetingPlaceTime, even if there is no real meeting. 
         * In cases in which there is no real world meeting, there is no specified start times, end times, or meeting days. 
         * It is very rare for a HTMLSection to have zero HTMLMeetingPlaceTimes, like 10 out of 5000, and these sections are usually graduate / thesis type sections. 
         * Thus ignoring this information will not lead to a great loss of information or functionality.
         */
        for(SectionsPageParser.HTMLSection currentHTMLSection : pParsedSections){
            if (!currentHTMLSection.meetings().isEmpty()){
                 /**
                 * Extract the session from the parsed section and get reference to it's corresponding Sessions is in the Sessions table.
                 * Calling Importer.loadSessions() at top of this for loop ensured it will be in the Sessions table.
                 */
                Sessions.Session internalSession = Sessions.get(pTerm, currentHTMLSection.session());    
                
                /**
                 * Only process this current HTMLSection of it's corresponding internal Section does not already exist.
                 */
                Section existingSection = Sections.get(internalSession, currentHTMLSection.crn());
                
                if(existingSection == null){

                    /**
                     * Get from the Subjects table, the Subject that corresponds to this parsed section's subject.
                     * Subjects.loadSubjectsAndCampuses() *Should have been called before this method was called.
                     * If Subjects.loadSubjectsAndCampuses() has not been called, 
                     * there will be no corresponding Subject in the Subjects table and this currently parsed section cannot be transformed and loaded into the Sections table.
                     */
                    Subjects.Subject internalSubject = Subjects.get(currentHTMLSection.subjectAbbr());
                    if (internalSubject != null){
                        /**
                         * Iterate though each HTMLMeetingPlaceTime of the current HTMLSection
                         * 1) Add all of its instructors to the Instructors table and a temp set that will be provided to the Sections.add() method
                         * 2) Add each instructor to the internal Section's Instructor list.
                         * 3) Create the corresponding UnattachedMeetingTime if and only if the  HTMLMeetingPlaceTime has non null times and at least one DayOfWeek.
                         * Then add the UnattachedMeetingTime to a temp collection that will provided to the Sections.add() method.
                         */
                        setInstructors.clear();
                        llUnattachedMTs.clear();
                        for(SectionsPageParser.HTMLMeetingPlaceTime currMPT : currentHTMLSection.meetings()){
                            for(String currInstructorName : currMPT.instructors())
                                setInstructors.add(Instructors.add(currInstructorName));     

                            if(currMPT.startTime() != null && currMPT.endTime() != null && !currMPT.daysOfWeek().isEmpty()){
                               llUnattachedMTs.add(new UnattachedMeetingTime(currMPT.startTime(), currMPT.endTime(), currMPT.daysOfWeek()));
                            }   
                        } 
                        
                        /**
                         * Extract the course from the parsed section and ensure it's corresponding Course is in the courses table, getting a reference to it in the process. 
                         */
                        Courses.Course internalCourse= Courses.add(internalSubject, currentHTMLSection.courseNum());

                        /**
                         * In parsed / external date, Campus and InstructionalMethod are attributes of a HTMLMeetingPlaceTime, not HTMLSection, 
                         * but in internal data, theses are Attributes of a Section. 
                         * From all previous parses of all terms, all of a given HTMLSection's HTMLMeetingPlaceTime have the same InstructionslMethod and campus.
                         * Use as the InstrcutionalMethod and Campus, the values of any HTMLMeetingPlaceTime of the current HTMLSection.
                         * 
                         * Construct new Section from transformed values.
                         */
                        SectionsPageParser.HTMLMeetingPlaceTime firstMPT = currentHTMLSection.meetings().get(0);
                        Section newSection = Sections.add(currentHTMLSection.crn(), 
                                                        internalSession, 
                                                        internalCourse, 
                                                        Campuses.get(firstMPT.campus()), 
                                                        currentHTMLSection.sectionNum(), 
                                                        firstMPT.instructionalMethod(), 
                                                        currentHTMLSection.seatsAvailable(), 
                                                        currentHTMLSection.waitlistAvailability(),
                                                        setInstructors,
                                                        llUnattachedMTs);

                    }
                }
            }
            
        }
    }    
    /**
     * Private helper method to load sessions from a section parse, before loading sections.
     * The relational structure of the data in the web pages and thus corresponding parse does not match that of the more properly structured of the data in the "tables".
     * In the parse, session dates are an attribute of a HTMLMeetingPlaceTime.
     * @param pTerm
     * @param pParsedHTMLSections the LinkedList of parsed HTMLSection objects
     */
    private static void loadSessions(Terms.Term pTerm, LinkedList<SectionsPageParser.HTMLSection> pParsedHTMLSections){
        
        //Loop through each section, testing the existence of Sessions in Sessions table, and collecting min start date and max end date for for Sessions that don't exist
        
        //Hash map to store the min start date and max end data associated with each session found.
        HashMap<String, UDate[]> newSessionsMap = new HashMap();
        
        for(SectionsPageParser.HTMLSection hSec : pParsedHTMLSections){

            //Check to see if the current HTMLSection's Session has already been added to the Sessions table.
            //If it already exists, nothing needs to be done.
            String sessName = hSec.session();
            Sessions.Session sessFound = Sessions.get(pTerm, sessName);

            if (sessFound == null){
                //Current session has not been been added to  the Sessions table.
                //Get the current section's min meeeting start date and max meeting end date
                UDate minStartOfCurrent = hSec.minMeetingStartDate();
                UDate maxEndOfCurrent = hSec.maxMeetingEndDate();
                //Only continueif the current sections has min and max dates.
                if(minStartOfCurrent != null){
                    //Check to see if the current HTMLSection's Session has been added to the map
                    UDate[] datesInMap = newSessionsMap.get(sessName);
                    if (datesInMap == null){
                        //Current session has not been added to the map
                        //Add current session to map with its minStart and maxEnd dates
                        datesInMap = new UDate[2];
                        datesInMap[0] = minStartOfCurrent;
                        datesInMap[1] = maxEndOfCurrent;
                        newSessionsMap.put(sessName, datesInMap);
                    } else{
                        //Current session has been added to the map
                        if (minStartOfCurrent.lessThan(datesInMap[0]))
                            datesInMap[0] = minStartOfCurrent;
                        if (datesInMap[1].lessThan(maxEndOfCurrent))
                            datesInMap[1] = maxEndOfCurrent;                        
                    }
                }
            } 
        }
        
        //Iterate through any sessions thate were added to the hashMap and add them to the Sessions table
        for (Map.Entry<String, UDate[]> entry : newSessionsMap.entrySet())
            Sessions.add(pTerm, entry.getKey(), entry.getValue()[0], entry.getValue()[1]);       
    }
}
