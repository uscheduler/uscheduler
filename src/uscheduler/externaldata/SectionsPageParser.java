/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uscheduler.externaldata;
import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.time.DayOfWeek;
import java.util.Iterator;
import java.util.LinkedList;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import uscheduler.global.InstructionalMethod;
import uscheduler.global.UDate;
import uscheduler.global.UTime;

/**
 * A singleton class consisting of a methods that parse Sections from the Sections Page (KSU Class Schedule Listing).
 * 
 * @author Matt Bush
 */
public final class SectionsPageParser {
    private static final String DATA_DISPLAY_TABLE_SUMMARY = "This layout table is used to present the sections found";
    private static final String SEATING_NUMBERS_TABLE_SUMMARY = "This layout table is used to present the seating numbers.";
    private static final String MEETING_TIMES_TABLE_SUMMARY = "This table lists the scheduled meeting times and assigned instructors for this class..";
    private static final String NO_SECTIONS_FOUND_TABLE_SUMMARY = "This layout table holds message information";
    
    //Private constructor to make pseudo static class
    private SectionsPageParser(){};
    /**
     * Parses KSU sections from the Sections Page (KSU Class Schedule Listing) into a List of HTMLSection objects. 
     * The KSU Sections Page is retrieved via a call to {@link uscheduler.externaldata.DocumentRequester#getSectionsPage(int, java.lang.String, java.lang.String) DocumentRequester.getSectionsPage(pTermNum, pSubjAbbr, pCourseNum)}
     * 
     * <p><b>NOTE:</b>For much of the design of this project, it was assumed that the course argument provide to KSU's web server
     * was treated as "... where courseNum="argument"". 
     * <br>However, it has been discovered late in the project that KSU treats the course argument as "... where courseNum like("argument*")".
     * <br>Additionally, It was also assumed that any provided courseNum argument provided that did not correspond to a real course
     * would result in KSU returning the sections found page, but with no sections. 
     * This assumption turned out to be wrong. KSU will only return an empty sections page if the courseNum argument is less than 6 character and is alpha-numeric. 
     * <p>
     * @param pTermNum the term number for which the get Sections
     * @param pSubjAbbr the subject abbreviation for which the get Sections
     * @param pCourseNum the course number for which the get Sections. Not null. Must be 4 or 5 chars long and only contain alphanumeric characters.
     * @return a linked list LinkedList of HTMLSections objects representing the sections parsed from the Sections Page
     * @throws IOException if timed out or failed to make a connection for any reason
     * @throws uscheduler.externaldata.HTMLFormatException if the page returned does not have the HTML structure expected of the Sections Page.
     * @throws uscheduler.externaldata.NoDataFoundException If KSU returned a Sections page with no data as a result of the provided term number, subject, and course parameters. 
     */    
    public static LinkedList<HTMLSection> parseFromWeb(int pTermNum, String pSubjAbbr, String pCourseNum) throws IOException, HTMLFormatException, NoDataFoundException  {
        Document doc = DocumentRequester.getSectionsPage(pTermNum, pSubjAbbr, pCourseNum);
        return parseSectionsPage(doc, pTermNum );
    }
    /**
     * Parses KSU sections from the Sections Page (KSU Class Schedule Listing) into a List of HTMLSection objects. 
     * The KSU Sections Page is retrieved via a call to {@link uscheduler.externaldata.DocumentRequester#getSectionsPage(int, java.lang.String, java.lang.String) DocumentRequester.getSectionsPage(pTermNum, pSubjAbbr, "")}
     * @param pTermNum the term number that will be used as the term argument in the KSU HTTP request. 
     * @param pSubjAbbr the subject abbreviation that will be used as the subject argument in the KSU HTTP request. 
     * @return a LinkedList of HTMLSections objects representing the sections parsed from the retrieved Sections Page
     * @throws IOException if timed out or failed to make a connection for any reason
     * @throws uscheduler.externaldata.HTMLFormatException if the page returned does not have the HTML structure expected of the Sections Page.
     * @throws uscheduler.externaldata.NoDataFoundException If KSU returned a Sections page with no data as a result of the provided term number, subject, and course parameters. 
     */    
    public static LinkedList<HTMLSection> parseFromWeb(int pTermNum, String pSubjAbbr) throws IOException, HTMLFormatException, NoDataFoundException  {
        Document doc = DocumentRequester.getSectionsPage(pTermNum, pSubjAbbr, "");
        return parseSectionsPage(doc, pTermNum );
    }   
    /**
     * Parses KSU sections from the Sections Page (KSU Class Schedule Listing) into a List of HTMLSection objects. 
     * The KSU Sections Page is retrieved via a call to {@link uscheduler.externaldata.DocumentRequester#getSectionsPage(int, java.lang.String, java.lang.String) DocumentRequester.getSectionsPage(pTermNum, "", "")}
     * 
     * @deprecated <p><b>NOTE:</b>This method is for testing and debugging purposes only and will not be used in the final product.
     * 
     * @param pTermNum the term number that will be used as the term argument in the KSU HTTP request. 
     * @return a LinkedList of HTMLSections objects representing the sections parsed from the retrieved Sections Page
     * @throws IOException if timed out or failed to make a connection for any reason
     * @throws uscheduler.externaldata.HTMLFormatException if the page returned does not have the HTML structure expected of the Sections Page.
     * @throws uscheduler.externaldata.NoDataFoundException If KSU returned a Sections page with no data as a result of the provided term number, subject, and course parameters. 
     */    
    @Deprecated
    public static LinkedList<HTMLSection> parseFromWeb(int pTermNum) throws IOException, HTMLFormatException, NoDataFoundException  {
        Document doc = DocumentRequester.getSectionsPage(pTermNum, "", "");
        return parseSectionsPage(doc, pTermNum );
    }  
    /**
     * Parses a local Sections Page file into a LinkedList of HTMLSection objects.
     * 
     * @deprecated <p><b>NOTE:</b>This method is for testing and debugging purposes only and will not be used in the final product.
     * 
     * @param pFile the File object of the file that is to be parsed.
     * @param pTermNum the term number for the Sections apply
     * @return a linked list LinkedList of HTMLSection objects representing the sections parsed from the Sections Page file
     * @throws IOException if the file could not be opened and read
     * @throws uscheduler.externaldata.HTMLFormatException if the file does not have the HTML structure expected of the Sections Page.
     * @throws uscheduler.externaldata.NoDataFoundException If the file represents a Sections page that was generated with a term number, subject, and course parameters in which KSU returned a page with no results. 
     */ 
    @Deprecated
    public static LinkedList<HTMLSection> parseFromFile(File pFile, int pTermNum) throws IOException, HTMLFormatException, NoDataFoundException {
        Document doc = Jsoup.parse(pFile, null);
        return parseSectionsPage(doc, pTermNum);
    }
  
    private static LinkedList<HTMLSection> parseSectionsPage(Document pDoc, int pTermNum) throws HTMLFormatException, NoDataFoundException{
        
        LinkedList<HTMLSection> sectionsLL = new LinkedList<>();
        HTMLSection htmlSec = new HTMLSection();
        
        //***********************Find the DataDisplayTable
        Elements dataDisplayTables = pDoc.select("[summary=" + DATA_DISPLAY_TABLE_SUMMARY + "]");
        if (dataDisplayTables.isEmpty()) {
            Elements noSectionsFoundTables = pDoc.select("[summary=" + NO_SECTIONS_FOUND_TABLE_SUMMARY + "]");
            if (noSectionsFoundTables.isEmpty()) 
                throw new HTMLFormatException("Could not find the DataDisplayTable.");
            else
                throw new NoDataFoundException("No data found in the Sections Page.");            
        }
        if (dataDisplayTables.size() > 1) 
            throw new HTMLFormatException("Found more than 1 DataDisplayTable.");
        Element ddt = dataDisplayTables.first();

        //Get the tbody element of the DataDisplayTable
        Elements ddtTbodies = ddt.children();
        if (ddtTbodies.isEmpty()) 
            throw new HTMLFormatException("Could not find table body element of DataDisplayTable.");
        if (ddtTbodies.size() != 2) 
            throw new HTMLFormatException("Expected to find 2 table body element inside DataDisplayTable.");
        Element ddtTbody = ddtTbodies.get(1);
        if (!ddtTbody.tagName().equals("tbody"))
            throw new HTMLFormatException("Could not find the table body of the DataDisplayTable.");
        
        //Get the Section TRs and an Iterator to the Section TRs
        Elements sectionTRs = ddtTbody.children();
        if (sectionTRs.isEmpty()) 
            throw new HTMLFormatException("Could not find any table rows inside the DataDisplayTable.");
        Iterator<Element> sectionTRsIterator = sectionTRs.iterator();

        /* Begin to process Sections by iterating through each table row of the Sections Found Table.
        A new "Section" is found and will be processed each time a Section Table Header is found.
        When a Section Table Header is found, the previously processed Section will be added to the  sectionsLL Linked List.
        The first Section added to the LinkedList will be "dummy" section 
        in order to avoid having to check if the current Section being processed is the first Section encountered.
        It is assumed a Section Table Header is found when the child of the current sectionTR is a th element instead of a td element.
        Once no more Section Table Headers are found, add the previously processed Section to the sectionsLL Linked List!!!
        */
        Elements sectionTRChildren;
        Element sectionTRChild;
        Elements sectionTRGrandChildren;
        Element sectionTRGrandChild;

        while(sectionTRsIterator.hasNext())  {
            //Get the child element of the Sections Found Table's current table row element. 
            //Should either be a table header element or a table data element.
            sectionTRChildren = sectionTRsIterator.next().children();
            if (sectionTRChildren.isEmpty()) 
                throw new HTMLFormatException("Current DataDisplayTable table row has no child elements.");
            if (sectionTRChildren.size() > 1) 
                throw new HTMLFormatException("Current DataDisplayTable table row has more than 1 child elements.");
            sectionTRChild = sectionTRChildren.first();
            
            //Test if current table row's child element is a table header element (i.e. is the Section Table Header)
            if (sectionTRChild.tagName().equals("th")){
                //Add previously processed Section to the LinkedList and create new Section. First one added will be discarder at the end. 
                sectionsLL.add(htmlSec);
                htmlSec = new HTMLSection();//last one created will be added at the end.
                htmlSec.cTermNum = pTermNum;
                
                //Process the Section Table Header
                parseSectionTableHeader(htmlSec, sectionTRChild);                
            } else if (sectionTRChild.tagName().equals("td")){
                //Current Sections Found Table's table row does not contain a Section Table Header.
                //Test to see what it contains (e.g.Seating Numbers Table, Meeting Times Table, nothing, etc)
                sectionTRGrandChildren = sectionTRChild.children();
                if (!sectionTRGrandChildren.isEmpty()) {
                    sectionTRGrandChild = sectionTRGrandChildren.first();
                    if (!sectionTRGrandChild.tagName().equals("table"))
                        throw new HTMLFormatException("Unexpected sequence of Section table rows inside the DataDisplayTable.");

                    if (sectionTRGrandChild.attr("summary").equals(SEATING_NUMBERS_TABLE_SUMMARY)){
                        parseSeatingNumbersTable(htmlSec,sectionTRGrandChild);
                    } else if (sectionTRGrandChild.attr("summary").equals(MEETING_TIMES_TABLE_SUMMARY)){
                        parseMeetingTimesTable(htmlSec,sectionTRGrandChild);
                    }
                }                 
            } else
                throw new HTMLFormatException("Child of table row of Sections Found Table is niether a td nor th element."); 
        }
        //Remove the first "dummy" section from Sections and add the last one processed, but not added to LL.
        if (!sectionsLL.isEmpty()) {
            sectionsLL.add(htmlSec);
            sectionsLL.removeFirst();
        }
            
        
        //Return
        return sectionsLL;
    }
            
    private static void parseSectionTableHeader(HTMLSection pSection, Element pSectionTableHeader) throws HTMLFormatException{
        //Get the th's child anchor element
        Elements children = pSectionTableHeader.children();
        if (children.isEmpty()) 
            throw new HTMLFormatException("Assumed Section Table Header has no children."); 
        if (children.size() != 1) 
            throw new HTMLFormatException("Assumed Section Table Header has more children tan expected."); 
        Element a = children.first();
        if (!a.tagName().equals("a"))
            throw new HTMLFormatException("Could not find anchor element in assumed Section Table Header"); 
        
        //Get and parse the SectionTitle
        //SectionTitle = [subjectAbbr] [courseNum]/[sectionNum] - [courseName]
        
        String[] parse1 = a.ownText().split(" - ", 2);
        if (parse1.length !=2)
            throw new HTMLFormatException("Unexpected format in SectionTitle of Section Table Header"); 
         /* After split,
            parse1[0] = [subjectAbbr] [courseNum]/[sectionNum]
            parse1[1] = [courseName]
        */
        String[] parse2 = parse1[0].split(" ", 2);
        if (parse2.length !=2)
            throw new HTMLFormatException("Unexpected format in SectionTitle of Section Table Header"); 
         /* After split,
            parse1[0] = [subjectAbbr] [courseNum]/[sectionNum]
            parse1[1] = [courseName]
            parse2[0] = [subjectAbbr]
            parse2[1] = [courseNum]/[sectionNum]
        */
        String[] parse3 = parse2[1].split("/", 2);
        if (parse2.length !=2)
            throw new HTMLFormatException("Unexpected format in SectionTitle of Section Table Header"); 
         /* After split,
            parse1[0] = [subjectAbbr] [courseNum]/[sectionNum]
            parse1[1] = [courseName]
            parse2[0] = [subjectAbbr]
            parse2[1] = [courseNum]/[sectionNum]
            parse3[0] = [courseNum]
            parse3[1] = [sectionNum]
        */
         pSection.cSubjectAbbr = parse2[0];
         pSection.cCourseNum = parse3[0];
         pSection.cCourseName = parse1[1];
         pSection.cSectionNum = parse3[1];
         
         //Remove " (Online - 95% Online)" or " (Hybrid)" from courseName if it's in there
         pSection.cCourseName = pSection.cCourseName.replaceAll(" [(]Online - 95% Online[)]", "");
         pSection.cCourseName = pSection.cCourseName.replaceAll(" [(]Hybrid[)]", "");

    }
    private static void parseSeatingNumbersTable(HTMLSection pSection, Element pSeatingNumbersTable) throws HTMLFormatException{
        /* Get the table's table data elements
        <td class="dddefault">[crn]</td>
        <td class="dddefault">[creditHours]</td>
        <td class="dddefault">[session]</td>
        <td class="dddefault">[capacity]</td>  
        <td class="dddefault">[numEnrolled]</td>
        <td class="dddefault">[seatsAvailable]</td>
        <td class="dddefault">[waitlistCapacity]</td>
        <td class="dddefault">[waitlistCount]</td>
        <td class="dddefault">[waitlistAvailability]</td>
        */      
        Elements tdElms = pSeatingNumbersTable.select("td");
        if (tdElms.size()!=9) 
            throw new HTMLFormatException("Assumed Section Table Header has no children."); 

        pSection.cCRN = Integer.parseInt(tdElms.get(0).ownText());
        pSection.cSession = tdElms.get(2).ownText();
        
        //Remove &nbsp; from [seatsAvailable]. 
        String seatsAvail = tdElms.get(5).ownText().replaceAll("[^0-9]+", "");
        if (seatsAvail.isEmpty())
            pSection.cSeatsAvailable = 0;
        else
            pSection.cSeatsAvailable = Integer.parseInt(seatsAvail);
        
        pSection.cWaitlistAvailability = Integer.parseInt(tdElms.get(8).ownText());
    }
    private static void parseMeetingTimesTable(HTMLSection pSection, Element pMeetingTimesTable) throws HTMLFormatException{
        /* Get the assummed Meeting Times Table's tbody child
        <table class="datadisplaytable" summary="This table lists the scheduled meeting times and assigned instructors for this class.." width="100%">
            <tbody>
                [Meeting Times Header Table Row][Meeting Times Table Row(s)]
            </tbody>
        </table>  
        */
        Elements children = pMeetingTimesTable.children();
        if (children.size()!=1) 
            throw new HTMLFormatException("Assumed Meeting Times Table has unexpected number of children.");  
        Element tbody = children.first();

        /* Geth all the tbody's table row elements
        [Meeting Times Header Table Row] =
            <tr>
                <th class="ddheader" scope="col">Campus</th>
                <th class="ddheader" scope="col">Instructional Method</th>
                <th class="ddheader" scope="col">Where</th>
                <th class="ddheader" scope="col">Days</th>
                <th class="ddheader" scope="col">Time</th>
                <th class="ddheader" scope="col">Start Date</th>
                <th class="ddheader" scope="col">End Date</th>
                <th class="ddheader" scope="col">Instructors</th>
            </tr>
        [Meeting Time Table Row] = One Of:
            <tr>
                <td class="dddefault">[campus]</td>
                <td class="dddefault">[instructionalMethod]</td>
                <td class="dddefault">[where]</td>
                <td>[Days of Week Table]</td> OR <td>TBA</td>
                <td class="dddefault">[time]</td>
                <td class="dddefault">[startDate]</td>
                <td class="dddefault">[endDate]</td>
                <td class="dddefault">[instructors]</td>
            </tr>     
        */   
        Elements grandChildren = tbody.children();
        if (grandChildren.isEmpty()) 
            throw new HTMLFormatException("Assumed Meeting Times Table has no grandchildren.");
        //Remove the table row containing the header information
        grandChildren.remove(0);
        if (grandChildren.isEmpty()) 
            throw new HTMLFormatException("Assumed Meeting Times Table has only one grandchild.");
        
        for (Element tr : grandChildren){
            HTMLMeetingPlaceTime meeting = new HTMLMeetingPlaceTime();
            Elements tds = tr.children();
            if (tds.size() != 8){
                System.out.println(pSection);
                System.out.println(pMeetingTimesTable.html());
                //System.exit(-1);
                throw new HTMLFormatException("Meeting Times Table Row has unexpected number of td children.");
            }
            
            meeting.cCampus = tds.get(0).ownText();
            
            
            //Classroom - 100%
            //Hybrid
            //Online - 100% Online
            //Online - 95% Online
            String iMethod = tds.get(1).ownText().toLowerCase();
            if(iMethod.contains("classroom"))
                meeting.cInstructionalMethod = InstructionalMethod.CLASSROOM;
            else if (iMethod.contains("hybrid"))
                meeting.cInstructionalMethod = InstructionalMethod.HYBRID;
            else if (iMethod.contains("online")){
                 if (iMethod.contains("100"))
                    meeting.cInstructionalMethod = InstructionalMethod.ONLINE_100;
                 else
                     meeting.cInstructionalMethod = InstructionalMethod.ONLINE_95;
            } else
                throw new HTMLFormatException("InstructionalMethod has unexpected value.");


            
            // Skip Building + Room

            //Process Days Of Week
            Elements currTdsChildren = tds.get(3).children();
            if (currTdsChildren.size() == 1){
                Element table = currTdsChildren.first();
                if (!table.tagName().equals("table"))
                    throw new HTMLFormatException("Could not find Days Of Week Table.");
                parseDaysOfWeekTable(meeting, table);
            }
            /*Get Start Time + End Time, Ignore  Meeting Type (lab / lecture)
                [time] = One Of:
                    [startTime] – [endTime]<br>[meetingType]
                    OR
                    [startTime] – [endTime]<br>
                    OR
                    <abbr title="To Be Announced">TBA</abbr>   
            */
            //NOTE: Jsoup.Element.ownText() does not preserve line breaks. 
            
            String[] splitTime1 = tds.get(4).html().split("<br>", 2);
            if (splitTime1.length == 2){
                /*
                    splitTime1[0] = [startTime] – [endTime]
                    splitTime1[1] = [meetingType]
                */
                String[] splitTime2 = splitTime1[0].split(" - ");
                if (splitTime2.length == 2){
                    /*
                        splitTime2[0] = [startTime]
                        splitTime2[1] = [endTime]
                    */
                    try {
                        meeting.cStartTime = new UTime(splitTime2[0]);
                        meeting.cEndTime = new UTime(splitTime2[1]);
                    } catch (ParseException ex) {
                        throw new HTMLFormatException("Unexpected format for StartTime and EndTime");
                    }
                    
                } else
                    throw new HTMLFormatException("Unexpected format for StartTime and EndTime");

            } else {
                /*
                    splitTime1[0] = [startTime] – [endTime] OR <abbr title="To Be Announced">TBA</abbr>  
                */
                String[] splitTime2 = tds.get(4).ownText().split(" – ", 2);
                if (splitTime2.length == 2){
                    /*
                    splitTime2[0] = [startTime]
                    splitTime2[1] = [endTime]
                    */                    
                    try {
                        meeting.cStartTime = new UTime(splitTime2[0]);
                        meeting.cEndTime = new UTime(splitTime2[1]);
                    } catch (ParseException ex) {
                        throw new HTMLFormatException("Unexpected format for StartTime and EndTime");
                    }
                    
                                      
                } else {
                    meeting.cStartTime = null;
                    meeting.cEndTime = null;                   
                }
            }
            
           
            try {
                meeting.cStartDate = new UDate(tds.get(5).ownText());
                meeting.cEndDate = new UDate(tds.get(6).ownText());
            } catch (ParseException ex) {
                throw new HTMLFormatException("Unexpected format for StartDate and EndDate");
            }
            
            //Get Instructors
            String instructors = tds.get(7).ownText().replaceAll(" [(][)]", "");
            if (!instructors.equals("")){
                //Split instructors on ", " unless it is followed by "II" or "Sr" or "Jr"
                //Then add each instructor
                for (String instructor: instructors.split(", (?!(?:(II)|(Sr)|(Jr)))")){ 
                    meeting.cInstructors.add(instructor.trim());
                }
            }
            
            //Add Meeting Time to Section
            pSection.cMeetings.add(meeting);
            
        }
    }
    
    private static void parseDaysOfWeekTable(HTMLMeetingPlaceTime pSchedule, Element pDaysOfWeekTable) throws HTMLFormatException{
        /* 
            <table ,="" border="1" cellspacing="0">
                <tbody>
                    <tr align="center" bgcolor="#CCCCCC">
                        <td><strong>U</strong></td>
                        <td><strong>M</strong></td>
                        <td><strong>T</strong></td>
                        <td><strong>W</strong></td>
                        <td><strong>R</strong></td>
                        <td><strong>F</strong></td>
                        <td><strong>S</strong></td>
                    </tr>
                    <tr>
                        <td>[meetsSunday]</td>
                        <td>[meetsMonday]</td>
                        <td>[meetsTuesday]</td>
                        <td>[meetsWednesday]</td>
                        <td>[meetsThursday]</td>
                        <td>[meetsFriday]</td>
                        <td>[meetsSaturday]</td>
                    </tr>
                </tbody>
            </table>
        */
        //Get the assummed Days Of Week Table's tbody child
        Elements children = pDaysOfWeekTable.children();
        if (children.size()!=1) 
            throw new HTMLFormatException("Assumed Days Of Week Table has unexpected number of children.");  
        Element tbody = children.first();

        /* Geth all the tbody's table row elements */   
        Elements grandChildren = tbody.children();
        if (grandChildren.size() != 2) 
            throw new HTMLFormatException("Assumed Days Of Week Table has unexpected number of grandchildren.");
        
        //Remove the table row containing the header information and get the tr containing data
        grandChildren.remove(0);
        Element tr = grandChildren.first();
        
        //Get the TDs
        Elements tds = tr.children();
        if (tds.size() != 7)
            throw new HTMLFormatException("Days Of Week Table has unexpected number of td children.");
        if (tds.get(0).ownText().equals("X"))
            pSchedule.cDays.add(DayOfWeek.SUNDAY);
        if (tds.get(1).ownText().equals("X"))
            pSchedule.cDays.add(DayOfWeek.MONDAY);
        if (tds.get(2).ownText().equals("X"))
            pSchedule.cDays.add(DayOfWeek.TUESDAY);  
        if (tds.get(3).ownText().equals("X"))
            pSchedule.cDays.add(DayOfWeek.WEDNESDAY);
        if (tds.get(4).ownText().equals("X"))
            pSchedule.cDays.add(DayOfWeek.THURSDAY);
        if (tds.get(5).ownText().equals("X"))
            pSchedule.cDays.add(DayOfWeek.FRIDAY);  
        if (tds.get(6).ownText().equals("X"))
            pSchedule.cDays.add(DayOfWeek.SATURDAY);          
          
    }

    /**
     * Used to model a Section parsed from the Sections Page (KSU Class Schedule Listing). 
     * 
     * @author Matt Bush
     */
    public static class HTMLSection {
        private int cTermNum;
        private String cSubjectAbbr;
        private String cCourseNum;
        private String cCourseName;
        
        private int cCRN;
        private String cSectionNum;
        private String cSession;
        private int cSeatsAvailable;
        private int cWaitlistAvailability;
        private final LinkedList<HTMLMeetingPlaceTime> cMeetings = new LinkedList<>();
        
        private HTMLSection(){};
        
        @Override
        public String toString(){
            return Integer.toString(cTermNum) + '\t' + cSubjectAbbr + '\t' + cCourseNum + '\t' + cCourseName + 
                    '\t'  + Integer.toString(cCRN) + '\t' + cSession + '\t' +  Integer.toString(cSeatsAvailable) + '\t' + Integer.toString(cWaitlistAvailability);
        }
        
        /**
         *
         * @return this HTMLSection's term number.
         */
        public int termNum() {return cTermNum;}
        /**
         *
         * @return this HTMLSection's subject abbreviation.
         */
        public String subjectAbbr() {return cSubjectAbbr;}
        /**
         *
         * @return this HTMLSection's course number.
         */
        public String courseNum() {return cCourseNum;}
        /**
         *
         * @return this HTMLSection's course name.
         */
        public String courseName() {return cCourseName;}
        /**
         *
         * @return this HTMLSection's CRN.
         */
        public int crn() {return cCRN;}
        /**
         *
         * @return this HTMLSection's waitlist availability. 
         */
        public String sectionNum() {return cSectionNum;}
        /**
         *
         * @return this HTMLSection's session name.
         */
        public String session() {return cSession;}
        /**
         *
         * @return this HTMLSection's seats available. g.
         * 
         */
        public int seatsAvailable() {return cSeatsAvailable;}
        /**
         *
         * @return this HTMLSection's waitlist availability. 
         */
        public int waitlistAvailability() {return cWaitlistAvailability;}
        /**
         *
         * @return a LinkedList of HTMLMeetingPlaceTime object corresponding to the Section's Meeting Place Times. 
         * The LinkedList will never be null, however it may be empty in cases when the Section had no corresponding Meeting Place Times.
         */
        public LinkedList<HTMLMeetingPlaceTime> meetings() {return cMeetings;}
        
        /**
         * Returns the max end date from this HTMLSection's HTMLMeetingPlaceTimes
         * @return the max end date from this HTMLSection's HTMLMeetingPlaceTimes
         */
        public UDate maxMeetingEndDate(){
            UDate max = null;
            for(HTMLMeetingPlaceTime meeting : cMeetings){
                if(meeting.cEndDate != null && (max == null || max.lessThan(meeting.cEndDate)))
                    max = meeting.cEndDate;
            }
            return max;
        }
        /**
         * Returns the min start date from this HTMLSection's HTMLMeetingPlaceTimes
         * @return the min start date from this HTMLSection's HTMLMeetingPlaceTimes
         */
        public UDate minMeetingStartDate(){
            UDate min = null;
            for(HTMLMeetingPlaceTime meeting : cMeetings){
                if(meeting.cStartDate != null && (min == null || meeting.cStartDate.lessThan(min)))
                    min = meeting.cStartDate;
            }
            return min;
        }
        
    }
    /**
     * Used to model a Meeting Place Time of a Section parsed from the Sections Page (KSU Class Schedule Listing). 
     * 
     * @author Matt Bush
     */
    public static class HTMLMeetingPlaceTime {
        private InstructionalMethod cInstructionalMethod;
        private String cCampus;
        private UDate cStartDate;
        private UDate cEndDate;
        private UTime cStartTime;
        private UTime cEndTime;
        private final LinkedList<String> cInstructors = new LinkedList<>();
        private final LinkedList<DayOfWeek> cDays = new LinkedList<>();

        private HTMLMeetingPlaceTime(){};
        
        @Override
        public String toString(){
            return cInstructionalMethod.toString() + '\t' + cCampus + '\t'  + cStartDate + '\t' + cEndDate + '\t' + cStartTime + '\t' + cEndTime;
        }

        /**
         *
         * @return the Meeting Place Time's Section's instructional method.
         */
        public InstructionalMethod instructionalMethod() {return cInstructionalMethod;}      
        /**
         * Returns the Meeting Place Time's Section's campus name. 
         * The value returned may not correspond to the name of an actual KSU campus since it is common for values such 
         * as "Off Campus" or "Online Course" to be displayed for the campus value in the Sections Page.
         * It is up to the client class to determine how to handle such cases.
         * 
         * @return the Meeting Place Time's Section's campus name. 
         */
        public String campus() {return cCampus;}
        
        /**
         *
         * @return a UDate of the Meeting Place Time's start date, or null if the Meeting Place Time didn't have a specified start date
         */
        public UDate startDate() {return cStartDate;}

        /**
         *
         * @return a UDate of the Meeting Place Time's end date, or null if the Meeting Place Time didn't have a specified end date
         */
        public UDate endDate() {return cEndDate;}

        /**
         * @return a UTime of the Meeting Place Time's start time, or null if the Meeting Place Time didn't have a specified start time
         */        
        public UTime startTime() {return cStartTime;}

        /**
         *
         * @return a UTime of the Meeting Place Time's end time, or null if the Meeting Place Time didn't have a specified end time
         */        
        public UTime endTime() {return cEndTime;}
        /**
         *
         * @return a LinkedList of Strings of the Meeting Place Time's Section's Intructor's names.
         * The LinkedList will never be null, however it may be empty in cases when no Instructors where specified for the Meeting Place Time.
         */
        public LinkedList<String> instructors() {return cInstructors;}

        /**
         *
         * @return a LinkedList of DayOfWeek objects corresponding to the Meeting Place Time's days of week.
         * The LinkedList will never be null, however it may be empty in cases when no days of week where specified for the Meeting Place Time.
         */
        public LinkedList<DayOfWeek> daysOfWeek() {return cDays;}

    }

}
