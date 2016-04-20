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
 * A singleton class consisting of a method that parse Terms from the Terms Page (KSU Dynamic Schedule).
 * 
 * @author Matt Bush
 */
public final class TermsPageParser {

    private static final String TERMS_TABLE_SUMMARY = "This layout table is used for term selection.";
    
    //Private constructor to make pseudo static class
    private TermsPageParser(){};

    /**
     * Makes a HTTP connection to the Terms Page (KSU Dynamic Schedule) and parses the returned page into a LinkedList of HTMLTerm objects.
     * @return a linked list LinkedList of HTMLTerm objects representing the terms parsed from the Terms Page
     * @throws IOException if timed out or failed to make a connection for any reason
     * @throws uscheduler.externaldata.HTMLFormatException if the page returned does not have the HTML structure expected of the Terms Page.
     */
    public static LinkedList<HTMLTerm> parseFromWeb() throws IOException, HTMLFormatException  {
        Document doc = DocumentRequester.getTermsPage();  
        return parseTermsPage(doc);
    }
    
    /**
     * Parses a local Terms Page file into a LinkedList of HTMLTerm objects.
     * @param pFile the File object of the file that is to be parsed.
     * @return a linked list LinkedList of HTMLTerm objects representing the terms parsed from the Terms Page file
     * @throws IOException if the file could not be opened and read
     * @throws uscheduler.externaldata.HTMLFormatException if the file does not have the HTML structure expected of the Terms Page.
     */
    public static LinkedList<HTMLTerm> parseFromFile(File pFile) throws IOException, HTMLFormatException {
        Document doc = Jsoup.parse(pFile, null);
        return parseTermsPage(doc);
    }
    
    private static LinkedList<HTMLTerm> parseTermsPage(Document pDoc) throws HTMLFormatException{
        
        LinkedList<HTMLTerm> termsLL = new LinkedList();
        TermsPageParser.HTMLTerm htmlTerm;
        
        //***********************Find the TermsTable
        Elements termsTables = pDoc.select("[summary=" + TERMS_TABLE_SUMMARY + "]");
        //***NOTE: Need to eventually change the following and test for the "no classes found" page
        if (termsTables.isEmpty()) 
            throw new HTMLFormatException("Could not find the TermsTable.");
        if (termsTables.size() > 1) 
            throw new HTMLFormatException("Found more than 1 TermsTable.");
        Element tt = termsTables.first();

        //Get the select element inside the TermsTable
        Elements ttSelects = tt.select("select");
        if (ttSelects.isEmpty()) 
            throw new HTMLFormatException("Could not find select element of TermsTable.");
        if (ttSelects.size() >1 ) 
            throw new HTMLFormatException("Found more than 1 select element inside TermsTable.");
        Element ttSelect = ttSelects.first();
        
        //Get the select's options and an Iterator to the options
        Elements selectOptions = ttSelect.children();
        if (selectOptions.isEmpty()) 
            throw new HTMLFormatException("Could not find any table rows inside the TermsTable.");
        //The first option in the select element acts as a label and is not useful
        selectOptions.remove(0);
        
        Iterator<Element> selectOptionsIterator = selectOptions.iterator();

        // Begin to process Terms by iterating through the each select's options.
        Element selectOption;
        
        while(selectOptionsIterator.hasNext())  {
            selectOption = selectOptionsIterator.next();
            htmlTerm = new TermsPageParser.HTMLTerm();
            htmlTerm.cTermNum = Integer.parseInt(selectOption.attr("VALUE"));
            htmlTerm.cTermName = selectOption.ownText().replaceAll(" [(]View only[)]", "");
            termsLL.add(htmlTerm);
        }
        
        //Return
        return termsLL;
    }
    /**
     * Used to model a Term parsed from the Terms Page (KSU Dynamic Schedule).
     * 
     * @author Matt Bush
     */
    public static class HTMLTerm {
        private int cTermNum;
        private String cTermName;
        
        private HTMLTerm(){};
        /**
         * @return the Term's term number.
         */    
        public int termNum() {return cTermNum;}
        /**
         * @return the Term's term name. The term name may have the string " (View only)" appended to the end of it.
         */  
        public String termName() {return cTermName;}

    }
}
