
/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uscheduler.externaldata;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedList;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

/**
 * A singleton class consisting of a methods that parse subjects and campuses from the KSU Schedule Search page.
 * 
 * @author Matt Bush
 */
public class ScheduleSearchPageParser {
    //Private constructor to make pseudo static class
    private ScheduleSearchPageParser(){};
    /**
     * Makes a HTTP connection to the Schedule Search page and parses the returned page into a LinkedList of HTMLSubject objects 
     * and a LinkedList of campus names. The result is returned as a ScheduleSearchPageResult.
     * @param pTermNum the term number that will be provided as a parameter of the HTTP request to the Schedule Search page.
     * @return a ScheduleSearchPageResult object containing a LinkedList of HTMLSubject objects and a LinkedList of campus names.
     * @throws IOException if timed out or failed to make a connection for any reason
     * @throws uscheduler.externaldata.HTMLFormatException if the page returned does not have the HTML structure expected of the Schedule Search page.
     * @throws uscheduler.externaldata.NoDataFoundException If KSU returned a Schedule Search page with no data as a result of the provided term number parameter. 
     */    
    public static ScheduleSearchPageResult parseFromWeb(int pTermNum) throws IOException, HTMLFormatException, NoDataFoundException  {
        Document doc = DocumentRequester.getScheduleSearchPage(pTermNum);
        return parseScheduleSearchPage(doc);
    }
    
    /**
     * Parses a local Schedule Search page file into a LinkedList of HTMLSubject objects 
     * and a LinkedList of campus names. The result is returned as a ScheduleSearchPageResult.
     * @param pFile the File object of the file that is to be parsed.
     * @return a ScheduleSearchPageResult object containing a LinkedList of HTMLSubject objects and a LinkedList of campus names.
     * @throws IOException if the file could not be opened and read
     * @throws uscheduler.externaldata.HTMLFormatException if the file does not have the HTML structure expected of the Sections Page.
     * @throws uscheduler.externaldata.NoDataFoundException If the file represents a Schedule Search page that was generated with a term number in which KSU returned a page with no results. 
     */ 
    public static ScheduleSearchPageResult parseFromFile(File pFile) throws IOException, HTMLFormatException, NoDataFoundException {
        Document doc = Jsoup.parse(pFile, null);
        return parseScheduleSearchPage(doc);
    }
    
    
    private static ScheduleSearchPageResult parseScheduleSearchPage(Document pDoc) throws HTMLFormatException, NoDataFoundException{
    
        ScheduleSearchPageResult result = new ScheduleSearchPageResult();
        //Get all select elements of the document
        Elements allSelects = pDoc.select("select");
        if (allSelects.isEmpty())
            throw new HTMLFormatException("Couldn't find any select elements.");
        
        //[Subjects Select] =
        //    <select name="sel_subj" size="10" MULTIPLE ID="subj_id">
        //        <OPTION VALUE="%" SELECTED>All
        //        <OPTION VALUE="AADS">AADS-African Diaspora Studies
        //        <OPTION VALUE="ACCT">ACCT-Accounting
        //          ...
        //        <OPTION VALUE=VALUE="HMI">HMS-Healthcare Mgmt & Informat
        //        <OPTION VALUE="ICT">Info and Comm Technology
        //        <OPTION VALUE="ICT">Info and Comm Technology  //NOTE: No "-"
        //        ...
        //    </select>

        //Find the Subjects Select element, throw HTMLFormatException if not found
        Elements subjectSelects = allSelects.select("[name=sel_subj]");
        if (subjectSelects.size() != 1)
            throw new HTMLFormatException("Could not find the Subjects Select element.");
        
        //Get children option elemnts of the Subjects Select element
        //Throw a NoDataFoundException exception if there are less than 2 child option element
        Elements subjectOptions = subjectSelects.get(0).select("option");
        if (subjectOptions.size() < 2)
            throw new NoDataFoundException("No subjects found in the Subjects Select element.");
        
        //Remove first child of child option elements
        subjectOptions.remove(0);
        //iterate through each option element and create a new HTMLSubject object. 
        //Set the subjAbbr to the element's value attribute
        //Set the subjName to the portion to the right of "-" in the element's ownText()
        Iterator<Element> subjectOptionsIterator = subjectOptions.iterator();
        while (subjectOptionsIterator.hasNext()){
            Element subjectOption = subjectOptionsIterator.next();
            HTMLSubject s = new HTMLSubject();
            s.cSubjAbbr = subjectOption.attr("value");
            String[] parse1 = subjectOption.ownText().split("-", 2);
            s.cSubjName = parse1[parse1.length - 1].trim();
            result.cSubjects.add(s);
        }
        
        //[Campuses Select] =
        //
        //    <select name="sel_camp" size="3" MULTIPLE ID="camp_id">
        //        <OPTION VALUE="%" SELECTED>All
        //        <OPTION VALUE="D">Dalton Center
        //        <OPTION VALUE="GML">eCore
        //                ...
        //    </select>
        //Find the Campus Select element throw HTMLFormatException if not found
        Elements campusSelects = allSelects.select("[name=sel_camp]");
        if (campusSelects.size() != 1)
            throw new HTMLFormatException("Could not find the Campuses Select element.");
        
        //Get children option elemnts of the Campuses Select element
        //Throw a NoDataFoundException exception if there are less than 2 child option element
        Elements campusOptions = campusSelects.get(0).select("option");
        if (campusOptions.size() < 2)
            throw new NoDataFoundException("No campuses found inside the Campuses Select element.");
        
        //Remove first child of child option elements
        campusOptions.remove(0);
        //iterate through each option element and add its TEXT to the result's campus linked list
        Iterator<Element> campusOptionsIterator = campusOptions.iterator();
        while (campusOptionsIterator.hasNext()){
            Element campusOption = campusOptionsIterator.next();
            result.cCampuses.add(campusOption.ownText());
        }

        return result;
    }
    /**
     * Used to model the result of a ScheduleSearchPageParser parse operation.
     * 
     * @author Matt Bush
     */
    public static class ScheduleSearchPageResult {
        private final LinkedList<HTMLSubject> cSubjects = new LinkedList<>();
        private final LinkedList<String> cCampuses = new LinkedList<>();
        
        private ScheduleSearchPageResult(){};
        /**
         * @return the Subjects of the parse operation, which is a LinkedList of HTMLSubject objects
         */   
        public LinkedList<HTMLSubject> subjects() {return cSubjects;}
        /**
         * @return the campuses of the operation, which is a LinkedList of String campus names
         */  
        public LinkedList<String> campuses() {return cCampuses;}
    }
    /**
    * Used to model a Subject parsed from the ScheduleSearchPage.
    * 
    * @author Matt Bush
    */
    public static class HTMLSubject{
        private String cSubjAbbr;
        private String cSubjName;

        private HTMLSubject(){};
        /**
         * @return the Subject's abbreviation.
         */    
        public String subjAbbr() {return cSubjAbbr;}
        /**
         * @return the Subject's name.
         */  
        public String subjName() {return cSubjName;}
    }
}
