package uscheduler.externaldata;

import java.io.IOException;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

/**
 * A singleton class consisting of static methods that make requests to KSU web pages and return the corresponding Jsoup.Document.
 * @author Matt Bush
 */
public final class DocumentRequester {
    /**
     * The timeout argument used in all Jsoup.connect() methods, currently set to 60000 (60 seconds). 
     * The timeout parameter specifies the number of milliseconds (thousandths of a second) before timing out connects or reads. 
     * A timeout of zero is treated as an infinite timeout.  
     */
    public static final int TIMEOUT = 60000;
    public static final String TERMS_PAGE_URL = "https://owlexpress.kennesaw.edu/prodban/bwckschd.p_disp_dyn_sched";
    public static final String SUBJECTS_PAGE_URL = "https://owlexpress.kennesaw.edu/prodban/bwckgens.p_proc_term_date";
    public static final String SECTIONS_PAGE_URL = "https://owlexpress.kennesaw.edu/prodban/bwckschd.p_get_crse_unsec";

    /**
     * Makes a HTTP connection to the Terms Page (KSU Dynamic Schedule) and returns the read response as a Jsoup.Document object.
     * @return Jsoup.Document object of the Terms Page
     * @throws IOException if timed out or failed to make a connection for any reason
     */
    public static Document getTermsPage() throws IOException {
        return Jsoup.connect(TERMS_PAGE_URL)
                .timeout(TIMEOUT)
                .userAgent("Mozilla")
                .get();
    }
    /**
     * Makes a HTTP connection to the KSU Schedule Search Page for the specified term and returns the read response as a Jsoup.Document object.
     * @param pTermNum the term number for which the Schedule Search Page applies
     * @return Jsoup.Document object of the specified Schedule Search Page
     * @throws IOException if timed out or failed to make a connection for any reason
     */
    public static Document getScheduleSearchPage(int pTermNum) throws IOException {
	//params.add(HttpConnection.KeyVal.create("p_calling_proc", "bwckschd.p_disp_dyn_sched"));
        return Jsoup.connect(SUBJECTS_PAGE_URL)
            .data("p_calling_proc", "bwckschd.p_disp_dyn_sched")
            .data("p_term", Integer.toString(pTermNum))
            .timeout(TIMEOUT)
            .userAgent("Mozilla")
            .post();
    }    

    /**
     * Makes a HTTP connection to the Sections Page (KSU Class Schedule Listing) for the specified term, subject, and course and returns the read response as a Jsoup.Document object.
     * <p><b>NOTE:</b>For much of the design of this project, it was assumed that the course argument provide to KSU's web server
     * was treated as "... where courseNum="argument"". 
     * <br>However, it has been discovered late in the project that KSU treats the course argument as "... where courseNum like("argument*")".
     * <br>Additionally, It was also assumed that any provided courseNum argument provided that did not correspond to a real course
     * would result in KSU returning the sections found page, but with no sections. The parser would encounter this and throw a NoDataFoundException. 
     * This assumption turned out to be wrong. KSU will only return an empty sections page if the courseNum argument is less than 6 character and is alpha-numeric. 
     * <p>
     * @param pTermNum the term number for which the get Sections
     * @param pSubjAbbr the subject abbreviation for which the get Sections
     * @param pCourseNum the course number for which the get Sections. Not null. Must be 4 or 5 chars long and only contain alphanumeric characters.
     * @return Jsoup.Document object of the specified Sections Page
     * @throws IOException if timed out or failed to make a connection for any reason
     */
    public static Document getSectionsPage(int pTermNum, String pSubjAbbr, String pCourseNum) throws IOException {
	//params.add(HttpConnection.KeyVal.create("p_calling_proc", "bwckschd.p_disp_dyn_sched"));
        String cleanString = pCourseNum.replaceAll("[^a-zA-Z0-9]", "");
        cleanString = cleanString.substring(0, Math.min(cleanString.length(), 5));
        if (!cleanString.equals(pCourseNum) || !(cleanString.length() == 4 || cleanString.length() == 5))
            throw new IllegalArgumentException("Invalid pCourseNum");
        
        Connection con = Jsoup.connect(SECTIONS_PAGE_URL)
  		.data("term_in", Integer.toString(pTermNum))
		.data("sel_subj", "dummy")
		.data("sel_subj", pSubjAbbr.toUpperCase())                
		.data("sel_crse", pCourseNum)
		.data("sel_day", "dummy")
		.data("sel_schd", "dummy")
		.data("sel_insm", "dummy")
		.data("sel_camp", "dummy")
		.data("sel_levl", "dummy")
		.data("sel_sess", "dummy")
		.data("sel_instr", "dummy")
		.data("sel_ptrm", "dummy")
		.data("sel_attr", "dummy")
		.data("sel_title", "")
		.data("sel_insm", "%")
		.data("sel_from_cred", "")
		.data("sel_to_cred", "")
		.data("sel_camp", "%")
		.data("sel_levl", "%")
		.data("sel_ptrm", "%")
		.data("sel_instr", "%")
		.data("begin_hh", "0")
		.data("begin_mi", "0")
		.data("begin_ap", "a")
		.data("end_hh", "0")
		.data("end_mi", "0")
		.data("end_ap", "a")  
                //.userAgent("Mozilla")
                /**
                 * .maxBodySize(0) Is a MUST!!
                 * I noticed strange behavior when a parse failed on Term = 201608, subj = ENGL, and no course specified. 
                 * It wasn't parsing the entire document when parsed from web, but is was when parsed from file.
                 * At first I didn't think it had to do with maxBodySize because it actually parsed the entire term (all sections of all subjects) from web with no problem.
                 * Once I set maxBodySize(0), the problem resolved itself. I have no idea why it would parse the entire term but cut off short when parsing a single subject.
                 * Oh well, it works now.
                 */
                .maxBodySize(0)
                .timeout(TIMEOUT);
                return con.post();
    } 
}
