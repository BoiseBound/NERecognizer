package org.familysearch.standards.recognizer.common;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Set;
import java.util.List;
import java.util.ArrayList;
import java.io.InputStreamReader;
import java.io.BufferedReader;

public class MWUizer {
	
  Set<String> MWUSet;
  Set<String> MWUSpacers;
  public MWUizer(String resource) {
    InputStream myResource = getClass().getResourceAsStream(resource);
    if (myResource == null) {
      MWUSet=new HashSet<String>();
      System.err.println("I couldn't open the resource "+resource);
      System.exit(0);
    }
    else {
      MWUSet = loadResource(myResource);
      try {
         myResource.close();
      }
      catch (IOException e) {
      }
    }
    MWUSpacers=new HashSet<String>();
    MWUSpacers.add("<p>");	
    MWUSpacers.add("<br>");
    MWUSpacers.add("&nbsp;");
    MWUSpacers.add("<u>");
    MWUSpacers.add("</u>");
    MWUSpacers.add("<small>");
    MWUSpacers.add("</small>");

  }
  /** 
   * Load the MWU table
   * @param resource
   * @return
   */
  private Set<String> loadResource(InputStream resource) {
    Set<String> mwus = new HashSet<String>();
    /** The buffered file for reading */
    BufferedReader inFile = null;
    List<LabeledToken> myList;
    String str;

    try {
      InputStreamReader reader = new InputStreamReader(resource, "UTF-8");
      inFile = new BufferedReader(reader);
      String line;
      boolean start=false;
      
      while ((line = inFile.readLine()) != null) {
    	myList=MultilingualTokenizer.parseBuffer(line);
    	StringBuffer sb=new StringBuffer();
    	start=false;
    	for (int i=0;i<myList.size();i++) {
    	  if (myList.get(i).isNonSpace()) {
    		str=myList.get(i).getMain().toLowerCase();
    		str=str.replaceAll("\\d","\\\\d");
      	    if (start) { 
      		  sb.append(" ");
      		  mwus.add(sb.toString());
      	    }
    		sb.append(str);
    		start=true;
    	  }
    	}
        mwus.add(sb.toString());
      }
      //System.out.println("My MWUs are :");
      //Iterator<String> myIt=mwus.iterator();  while (myIt.hasNext()) { System.out.println(myIt.next()); }
      //System.exit(0);
      
    }
    catch (IOException e) {
    }
    finally {
      try {
        if (inFile != null) {
          inFile.close();
        }
      }
      catch (IOException ex) {
      }
    }
    return mwus;
  }
  /** 
   * This will determine what position should be used for a MWU whose starting position is specified
   * by firstChar and whose lastChar is the end position.
   * @param firstChar
   * @param lastChar
   * @return
   */
  public char determineNewPosition(char firstChar,char lastChar) {
	 switch(firstChar) {
	 case 'B':
		if (lastChar=='I') {
		  return 'B';
		}
		else if (lastChar=='E') {
		  return 'U';
		}
		else {
		  System.err.println("1st="+firstChar+" and last="+lastChar);
		  return 'U';
		}
	 case 'O':
		if (lastChar=='O') {
		  return 'O';
		}
		else {
		  System.err.println("1stO="+firstChar+" and last="+lastChar);
		  return 'O';
		}
	 case 'I':
		if (lastChar=='I') {
		  return 'I';
		}
		else if (lastChar=='E') {
		  return 'E';
		}
		else {
		  System.err.println("x1st="+firstChar+" and last="+lastChar);
		  return 'E';
		}
	 }
	 System.err.println("y1st="+firstChar+" and last="+lastChar);
	 return 'I';
  }
  /** 
   * 
   * @param myArray
   * @return
   */
  public List<LabeledToken> mwuIzeList(List<LabeledToken> myArray) {
	int i,j,goodj;
	boolean done=false;
	StringBuffer sb;
	String str;
	String myType, othType;
	char posn1,posnF,position;
    LabeledToken tmpLbl;

	ArrayList<LabeledToken> newList=new ArrayList<LabeledToken>();
	for (i=0;i<myArray.size();i++) {
      if (!myArray.get(i).isNonSpace()) {
    	newList.add(myArray.get(i));
    	continue;
      }
	  sb=new StringBuffer();
	  j=0;
	  goodj=0;
	  myType=myArray.get(i).getFullEnamex();
	  
	  // First, try to find legitimate blocks
	  while (!done) {
		  	
		// Disregard any space tokens
	    if (!myArray.get(i+j).isNonSpace()) {
	      j++;
	    }
	    // Make sure we are within the appropriate bounds
	    if ((i+j)>=myArray.size()) {
	      break;
	    }
	    
	    // Ensure that we are in the bounds of the same enamex type	    
	    othType=myArray.get(i+j).getFullEnamex();
	    if (!othType.equals(myType)) {
	      break;
	    }

	    str=myArray.get(i+j).getMain().toLowerCase();
	    str=str.replaceAll("\\d","\\\\d");
	    sb.append(str);
	    if (j>0) {
		  // Check to see if X_Y is a possible MWU sequence
	      if (MWUSet.contains(sb.toString())) {
	    	goodj=j;
	      }
	    }
	    sb.append(" ");
	    // Check to see if X_Y_ is a possible MWU sequence lead-in
	    if (!MWUSet.contains(sb.toString())) {
	      break;
	    }
	    j++;
	  }
	  // Now, put the units together
	  // If we didn't find any MWUs, just put in what we had originally
	  if (goodj==0) {
		newList.add(myArray.get(i));
	  }
	  else {
	    // Otherwise, build a new Labeled Token
		sb=new StringBuffer();
	    for (j=0;j<=goodj;j++) {
		  sb.append(myArray.get(i+j).getMain());
	    }
	    posn1=myArray.get(i).getPosition();
	    posnF=myArray.get(i+goodj).getPosition();
        position=determineNewPosition(posn1,posnF);
        String sbstr=sb.toString();
        if (MWUSpacers.contains(sbstr)) {
          myType="SP";
        }
        tmpLbl=new LabeledToken(sbstr,myArray.get(i).getOffset(),myType,position);
        newList.add(tmpLbl);
        //System.out.print("I created "+tmpLbl+" from ");
        //for (j=0;j<=goodj;j++) { System.out.print(myArray.get(i+j)); } System.out.println(""); 
        i+=goodj;
	  }
	}
	//System.exit(0);
	return newList;
  }
  
}
