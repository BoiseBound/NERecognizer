package org.familysearch.standards.recognizer.common;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.ArrayList;

import java.lang.StringBuffer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MultilingualTokenizer {
      
	  private MWUizer myMWUizer;
	  private static final String  defaultMWUlist="resources/MWUList";
	  
	  public MultilingualTokenizer(String MWUlist) {
		if (MWUlist==null) {
		  MWUlist=defaultMWUlist;
		}
		myMWUizer=new MWUizer(MWUlist);
	  }
	  
	  public MultilingualTokenizer() {
		this(null); 
	  }
	  
	  /** 
	   * 
	   * @param buffer
	   * @return
	   */
	  public List<LabeledToken> parseBuffer(StringBuffer buffer) {
		return(this.parseBuffer(buffer,true));
	  }
	  /** 
	   * This version of the parseBuffer will allow for the system to recombine
	   * the entries using MWU tables.
	   * 
	   * @param buffer
	   * @param useMWUs
	   * @return
	   */
	  public List<LabeledToken> parseBuffer(StringBuffer buffer,boolean useMWUs) {
		 List<LabeledToken> myArray=MultilingualTokenizer.parseBuffer(buffer.toString());
		 if (useMWUs) {
			myArray=myMWUizer.mwuIzeList(myArray);
		 }
		 return(myArray);
	  }
	  /** 
	   * This process will load a StringBuffer and convert it to a sequence of 
	   * LabeledTokens
	   * 
	   * @param buffer
	   * @param useMWUs  : Should the system try to merge things back together as MWUs?
	   * @return
	   */
	  public static List<LabeledToken> parseBuffer(String buffer) {
		 ArrayList<LabeledToken> myArray=new ArrayList<LabeledToken>();
		 int nonEnamexCounter=0;
		 int length;
		 int offset;
		 String piece;
		 String form;
		 String eclass="";
		 char position='O';
		 LabeledToken savedLabel=null;
		 
		 Matcher m = Pattern.compile("(\\<\\/?(?:ENA|NU|TI)MEX.*?\\>|\\p{IsSpace}+|\\.+|\\++|\\-+|[\\p{IsPunct}&&[^\\<\\>]]|[\\<\\>]|\\p{IsAlnum}+|.)").matcher(buffer);
		 while (m.find()) {
		   piece=m.group();
		   form="";
		   // Handle punction first so as to distinguish <|> from enamex parts
		   if (piece.matches("(\\p{IsPunct}|\\<|\\>)+")) {
			 form="NONE";  
			 length=piece.length();
			 if (eclass.length()>0) {
			   form=eclass;
			 }
		   }
		   // Next, handle the enamex parts
		   else if (piece.charAt(0)=='<') {
			 // This is the enamex-ender
			 if (piece.charAt(1)=='/' ) { 
			   savedLabel.end();
			   eclass="";
			   position='O';
			 }
			 // This is the start of an enamex category
			 else {
			   offset=piece.indexOf('=');
			   piece=piece.substring(offset+2, piece.length()-2);
			   eclass=piece;
			   position='B';
			 }
			 length=0;
		   }
		   else if (piece.matches("\\p{IsSpace}+")) {
			 form="SP";  
			 length=piece.length();
		   }
		   else if (piece.matches("\\p{IsAlnum}+")) {
			 form="NONE";
			 if (eclass.length()>0) {
			   form=eclass;
			 }
			 length=piece.length();
		   }
		   else  {
			 form="NONE";
			 if (eclass.length()>0) {
			   form=eclass;
			 }
			 length=piece.length();
		   }
		   if (length>0) {
			 // Always keep the last label around
			 if (savedLabel!=null) {
			   myArray.add(savedLabel); 
			 }
			 savedLabel=new LabeledToken(piece,nonEnamexCounter,form,position);
			 if (!form.equals("SP") && position=='B') {
			   position='I';
			 }
		     nonEnamexCounter+=length;
		   }
		 }
		 if (savedLabel!=null) {
		   myArray.add(savedLabel); 
		 }		 
		 return myArray;
	  }
	  /** 
	   * Do both the loading and the parsing of the file
	   * @param fileName
	   * @return
	   */
	  public List<LabeledToken> loadAndParse(String fileName) {
		return parseBuffer(loadOneFile(fileName));
	  }
	  /**
	   * Load the truth file (if one is provided)
	   *
	   * @param fileName file to process
	   * @return map of truth data
	   */
	  public static StringBuffer loadOneFile(String fileName) {
	    String line;
	    BufferedReader inFile = null;
	    StringBuffer myBuffer=new StringBuffer();
	    if (fileName==null) {
	      return myBuffer;
	    }

	    try {
	      InputStream in = new FileInputStream(fileName);
	      InputStreamReader reader = new InputStreamReader(in, "UTF-8");
	      inFile = new BufferedReader(reader);
	      while ((line = inFile.readLine()) != null) {
            myBuffer.append(line+"\n");
	      }
	    }
	    catch (FileNotFoundException ex) {
	      System.err.println("ERR=Could not find input file ");
	    }
	    catch (UnsupportedEncodingException ue) {
	      System.err.println("ERR=Encoding not supported");
	    }
	    catch (IOException e) {
	      System.err.println("ERR=" + e.getMessage());
	    }
	    finally {
	      try {
	        if (inFile != null) {
	          inFile.close();
	        }
	      }
	      catch (IOException ex) {
	        ex.printStackTrace();
	      }
	    }
	    return myBuffer;
	  }
	

}
