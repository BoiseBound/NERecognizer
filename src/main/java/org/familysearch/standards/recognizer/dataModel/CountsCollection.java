package org.familysearch.standards.recognizer.dataModel;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.List;
import java.util.Iterator;
import java.util.ArrayList;
import java.io.ObjectOutputStream;
import java.io.ObjectInputStream;
import java.io.IOException;


import org.familysearch.standards.recognizer.common.LabeledToken;
import org.familysearch.standards.recognizer.common.Visibility;

/**
 * This routine will compute the counts of a corpus and then it will normalize the counts.
 * 
 * @author BoiseBound
 *
 */
public class CountsCollection implements java.io.Serializable {

	/**
	 * Constant used for serialization
	 */
	private static final long serialVersionUID = 743192138814192153L;
	
	  protected UnigramCountsModel<String>                                                   UnigramTokenCounts;
	  protected UnigramCountsModel<String>                                                   EntityClassCounts;
	  protected UnigramCountsModel<SpacingModel>                                             SpaceCounts;
	  protected BigramCountsModel<String,String>                                             BigramTokenCounts;
	  protected BigramCountsModel<String,String>                                             EntityClassPairCounts;	  
	  protected UnigramCountsModel<String>                                                   UniqueEntityClassPairCounts;	  
	  protected TrigramCountsModel<String,SpacingModel,String>                               BigramTokenWithSpaceCounts;
	  protected BigramCountsModel<String,String>                                             UnigramTokenToEntityCounts;
	  protected UnigramCountsModel<String>                                                   UniqueEntityOrWordPairCounts;
	  protected BigramCountsModel<BigramModel<String,String>,BigramModel<String,String>>     BigramTokenToEntityCounts;
	  
	  public static final String STARTER="==START==";
	  public static final String ENDER="==END=="; 
	  public static final String MYSTERYUC="==UNKNOWN==";
	  public static final String MYSTERYCC="==UnKnown==";
	  public static final String MYSTERYUC1ST="==Unknown==";
	  public static final String MYSTERYLC="==unknown==";
	  public static final double DISCOUNT=0.75;
	  public static final int MAXLENGTHBETWEENBREAKS=1000;
	  public static final int MAXACRONYM=4;

	  
	  public CountsCollection() {
		UnigramTokenCounts=new UnigramCountsModel<String>();
		BigramTokenCounts=new BigramCountsModel<String,String>();
		EntityClassCounts=new UnigramCountsModel<String>();
		SpaceCounts=new UnigramCountsModel<SpacingModel>();
		UnigramTokenToEntityCounts=new BigramCountsModel<String,String>();
		BigramTokenWithSpaceCounts=new TrigramCountsModel<String,SpacingModel,String>();
		EntityClassPairCounts=new BigramCountsModel<String,String>();
		BigramTokenToEntityCounts=new BigramCountsModel<BigramModel<String,String>,BigramModel<String,String>>();
		UniqueEntityClassPairCounts=new UnigramCountsModel<String>();
		UniqueEntityOrWordPairCounts=new UnigramCountsModel<String>();
	  }
	  /** 
	   * 
	   * @return
	   */
	  public UnigramCountsModel<String> getUniqueEntityOrWordPairCounts() {
		return UniqueEntityOrWordPairCounts;
	  }
	  /** 
	   * 
	   * @return
	   */
	  public BigramCountsModel<String,String> getUnigramTokenToEntityCounts() {
	    return UnigramTokenToEntityCounts;
	  }
	  public double getEntityClassCount(String str) {
	    return EntityClassCounts.get(str);
      }
	  /** 
	   * This will determine what type of "MYSTERY" token we are currently looking at
	   * @param token
	   * @return
	   */
	  public static String getMysteryString(String token) {
		String str=token.toLowerCase();
		if (token.equals(str)) {
		  return MYSTERYLC;		  
		}
		else {
		  if (token.matches("^\\p{IsUpper}+")) {
			return MYSTERYUC;   
		  }
		  if (token.matches("^\\p{IsUpper}\\p{IsLower}+")) {
			return MYSTERYUC1ST;	
		  }
		  return MYSTERYCC;
		}
	  }	
	  public double getUnigramTokenToEntityCount(String token,String rawToken,String entity) {
		String str;
		if (token==null || entity==null) {
		  return 0;
		}
		Double d=UnigramTokenToEntityCounts.get(token, entity);
		if (d==null) {
		  str=getMysteryString(rawToken);
		  d=UnigramTokenToEntityCounts.get(str, entity);		  
		  if (d==null) {
			return 0;
		  }
		}
		return d;
	  }
	  /** 
	   * This method will attempt to embed breaks into the data.
	   *  We assume that the MWU should have recombined Mr., 3.14, and other sequences that would have 
	   *  involved Period.  So if we still see it by itself, it is probably the end of a sequence.
	   *  Now a problem with this is if it is used as a wildcard in a name, like Pat..k.  Another problem
	   *  will be an abbeviated name like Wm. Jones -- so we will not break if the word preceding the period
	   *  is punctuated
	   *  
	   * @param currentWord
	   * @param prevWord
	   * @param nextSpace
	   * @param lengthSinceLastBreak
	   * @return
	   */
	  public static boolean addEndAndStartOfSequenceAfterThisWord(String currentWord,
			  String prevWord, SpacingModel nextSpace,int lengthSinceLastBreak) {
		 //System.out.println("currentWord="+currentWord+" prev="+prevWord+" nextSpace="+nextSpace+" int="+lengthSinceLastBreak);
		 if (currentWord.equals(ENDER)) {
		   return false;
		 }
		 if (currentWord.equals(".") && (!prevWord.matches("^\\p{IsUpper}.*") && prevWord.length()>MAXACRONYM)) {
		   return true;
		 }
		 if (nextSpace==null || nextSpace.hasNonIndentTabsOrMultipleCarriageReturns()) {
		   return true;
		 }
		 if (lengthSinceLastBreak>=MAXLENGTHBETWEENBREAKS) {
		   return true;
		 }
		 return false;
	  }
	  /** 
	   * Performs default serialization
	   * @param oos
	   * @throws IOException
	   */
	  private void writeObject(ObjectOutputStream oos) throws IOException {
		oos.defaultWriteObject();			  
	  }

	  /** 
	   * Deserialize the data 
	   * @param inStream
	   * @throws ClassNotFoundException
	   * @throws IOException
	   */
	  private void readObject(ObjectInputStream inStream) throws  ClassNotFoundException, IOException {
		inStream.defaultReadObject();
	  }
	  	  
	  public static String countsNormalizer(String str) {
		//Don't normalize the constants
		if (str.startsWith("==")) {
		  return str;
		}
	    // Do we want to store these as lower case? 
	    str=str.toLowerCase();
	    // What about multiple/odd spacing
		str=str.replaceAll("\\s+", " "); 
		//What about normalization of dates
		str=str.replaceAll("\\d", "\\\\d");
		return str;
	  }
	  /** 
	   * 
	   * @param s
	   * @return
	   */
	  public UnigramCountsModel<String> getWordToState(String s,String rawS) {
		if (s==null) 
		{ return null; }	
		UnigramCountsModel<String> stateModel=UnigramTokenToEntityCounts.get(s);
		if (stateModel==null) {
		  stateModel=UnigramTokenToEntityCounts.get(getMysteryString(rawS));
		}
		if (stateModel==null) {
		  stateModel=new UnigramCountsModel<String>();
		}
		return stateModel;
	  }
	  /** 
	   * 
	   * @param s
	   * @return
	   */
	  public UnigramCountsModel<String> getStateToState(String s) {
		if (s==null) 
		{ return null; }	
		UnigramCountsModel<String> stateModel=EntityClassPairCounts.get(s);
		if (stateModel==null) {
		  stateModel=new UnigramCountsModel<String>();
		}
		return stateModel;
	  }

      /** 
       * 
       * @param parsedContents
       */
	  public static void addEnder(List<LabeledToken> parsedContents) {
		int offset=0, length=0;
		if (parsedContents==null) {
		  parsedContents=new ArrayList<LabeledToken>();
		}
		if (parsedContents.size()>0) {
		  LabeledToken lastToken=parsedContents.get(parsedContents.size()-1);
		  offset=lastToken.getOffset();
		  length=lastToken.getMain().length();
		}
		LabeledToken enderToken=
		  new LabeledToken(ENDER,(offset+length),ENDER,'O');
		parsedContents.add(enderToken);
	  }
 	  /** 
	   * This will add the data to statistical models
	   * @param parsedContents
	   * @param visibleLevel
	   */
	  public void addContentsToModel(List<LabeledToken> parsedContents,Visibility visibleLevel,HashSet<String> IdsVisible) {
		String myStr="";
		String lastStr=STARTER;
		String lastEnamex=STARTER+"-O";
		String myRawStr="", lastRawStr="", twoAgoRawStr="";
		String myEnamex;
		boolean lastFlag=false, myFlag=false;
		boolean embedEndStart;
		SpacingModel defaultSpacer=new SpacingModel("");
		SpacingModel spacer;
		spacer=defaultSpacer;
		BigramModel<String,String> myPair, lastPair;
		boolean ecnew=false;
		int lengthSinceLastBreak=0;
		int repeat=0;
		
		lastRawStr=lastStr;
		lastPair=new BigramModel<String,String>(lastStr,lastEnamex);
		
		UnigramTokenCounts.increment(lastStr,1);
		EntityClassCounts.increment(lastEnamex,1);
		UnigramTokenToEntityCounts.increment(lastStr,lastEnamex,1);
		IdsVisible.add(lastStr);
		addEnder(parsedContents);
		for (int i=0;i<parsedContents.size();i++) {
		  if (!parsedContents.get(i).isNonSpace()) {
			if (spacer.equals(defaultSpacer)) {
			  spacer=new SpacingModel(parsedContents.get(i).getMain());	
			}
			else {
			  spacer=new SpacingModel(spacer.getSpace()+parsedContents.get(i).getMain());
			}
			continue;
		  }
		  embedEndStart=false;
		  if (repeat==0) {
		    embedEndStart=addEndAndStartOfSequenceAfterThisWord(lastRawStr,twoAgoRawStr,spacer,lengthSinceLastBreak);
		  }
		  // If we are supposed to embed a START sequence, then draw the strings/enamex from starts and ends
		  if (embedEndStart || repeat==1) {
			lengthSinceLastBreak=0;
			if (repeat==0) {
			  myRawStr=myStr=ENDER;
			  myEnamex=ENDER+"-O";
			  repeat++;
			}
			else {
			  myRawStr=myStr=STARTER;
			  myEnamex=STARTER+"-O";
			  repeat=0;
			}
			// Don't increase the value of i
			i--;
		  }
		  else {
			myRawStr=parsedContents.get(i).getMain();
		    myStr=countsNormalizer(myRawStr);
		    myEnamex=parsedContents.get(i).getPositionedEnamex();
		    lengthSinceLastBreak++;
		  }
		  
		  // Check on the visibility of the unit
		  if (visibleLevel==Visibility.VISIBLE) {
			IdsVisible.add(myStr); 
		  }
		  		  
		  // Now store the ID that we need based on its visibility
		  lastFlag=myFlag;
		  myFlag=false;
		  if (!IdsVisible.contains(myStr)) {
			myStr=getMysteryString(myRawStr);
			myFlag=true;
		  }
		  
		  // If this is a non-visible phase but we have already seen both
		  // the previous and the current word, don't count it or we will
		  // be doing so twice
		  myPair=new BigramModel<String,String>(myStr,myEnamex);
		  if (visibleLevel==Visibility.VISIBLE || myFlag || lastFlag) {
		  
		    // Add the counts
		    UnigramTokenCounts.increment(myStr,1);
		    BigramTokenCounts.increment(lastStr,myStr,1);
		    EntityClassCounts.increment(myEnamex,1);
		    SpaceCounts.increment(spacer,1);
		    BigramTokenWithSpaceCounts.increment(lastStr,spacer,myStr,1);
		    
		    ecnew=EntityClassPairCounts.increment(lastEnamex,myEnamex,1);
		    if (ecnew) {
		      UniqueEntityClassPairCounts.increment(lastEnamex+"<wild>",1);
		      UniqueEntityClassPairCounts.increment("<wild>"+myEnamex,1);
		      UniqueEntityClassPairCounts.increment("<wild><wild>",1);
		    }
		    ecnew=UnigramTokenToEntityCounts.increment(myStr,myEnamex,1);
		    if (ecnew) {
		      UniqueEntityOrWordPairCounts.increment(myStr+"<wild>",1);
		      UniqueEntityOrWordPairCounts.increment("<wild>"+myEnamex,1);
		      UniqueEntityOrWordPairCounts.increment("<wild><wild>",1);
		    }
		    BigramTokenToEntityCounts.increment(lastPair, myPair, 1);
		    if ( (lastEnamex.endsWith("-B") && (myEnamex.endsWith("-O") || myEnamex.endsWith("-U"))) ||
			     (lastEnamex.endsWith("-I") && (myEnamex.endsWith("-O") || myEnamex.endsWith("-U") || myEnamex.endsWith("-B")))  
			   ) {
			   System.out.println("ERROR @ i="+i+":"+parsedContents.get(i)+" LAST="+lastPair.toString(null)+" TO="+myPair.toString(null));
		    }
		    if (lastEnamex.startsWith("NONE") && !lastEnamex.endsWith("-O")) {
			  System.out.println("ERROR @ i="+i+":"+parsedContents.get(i)+" LAST="+lastPair.toString(null)+" TO="+myPair.toString(null));	  
		    }
		  }
		  spacer=defaultSpacer;
		  twoAgoRawStr=lastRawStr;
          lastStr=myStr;
          lastRawStr=myRawStr;
          lastEnamex=myEnamex;
          lastPair=myPair;
          myStr=null;
		}
	  }
	  /** 
	   * 
	   */
	  public void prepareWeightsForBackoff() {
		prepareStringPairBackoff(EntityClassCounts,EntityClassCounts,UniqueEntityClassPairCounts,EntityClassPairCounts,true);
	//	prepareStringPairBackoff(UnigramTokenCounts,EntityClassCounts,UniqueEntityOrWordPairCounts,UnigramTokenToEntityCounts,false);
		UniqueEntityClassPairCounts.reset();
	//	UniqueEntityOrWordPairCounts.reset();
	  }
	  /** 
	   * This will allow an external process to compute backoff weights without requiring huge storage costs
	   * @param pre
	   * @param post
	   * @param uniqueHash
	   * @param bigramHash
	   * @return
	   */
	  public static double computeStringPairBackoff(String pre,String post,
			 UnigramCountsModel<String> uniqueHash, BigramCountsModel<String,String> bigramHash) {
		Double retVal;
		double augment, denom, prevalue, postvalue;

		prevalue=uniqueHash.get(pre+"<wild>");
		denom=uniqueHash.get("<wild><wild>");
		retVal=bigramHash.get(pre,post);
		augment=-DISCOUNT;
		if (retVal==null || retVal==0) {
		  augment=0;
		}
		postvalue=uniqueHash.get("<wild>"+post);
		augment+=DISCOUNT*(prevalue*postvalue)/denom;	
		return augment;
	  }
	  

	  public void prepareStringPairBackoff(UnigramCountsModel<String> unigramHashLeft, UnigramCountsModel<String> unigramHashRight, 
			  UnigramCountsModel<String> uniqueHash, BigramCountsModel<String,String> bigramHash,boolean useFollows) {
		 // Prepare cross enamex categories
		 Iterator<String> myItX=unigramHashLeft.keySet().iterator();
		 Iterator<String> myItY;
		 String pre,post;
		 double augment, prevalue, postvalue, denom;
		 Double retVal;
		 boolean follow=true;

		 denom=uniqueHash.get("<wild><wild>");
		 if (denom<=0) {
		   denom=1;
		 }
		 while (myItX.hasNext()) {
		   pre=myItX.next(); 
		   myItY=unigramHashRight.keySet().iterator();
		   prevalue=uniqueHash.get(pre+"<wild>");
		   while (myItY.hasNext()) {
			 post=myItY.next();
			 if (useFollows) {
			   if (pre.equals(ENDER+"-O") && !post.equals(STARTER+"-O")) {
				 follow=false;
			   }
			   if (post.equals(STARTER+"-O") && !pre.equals(ENDER+"-O")) {
				 follow=false;
			   }
			   else {
			     follow=LabeledToken.follows(pre, post);
			   }
			 }
			 if (!follow) {
			   continue;
			 }
			 retVal=bigramHash.get(pre,post);
			 augment=-DISCOUNT;
			 if (retVal==null || retVal==0) {
			   augment=0;
			 }
			 postvalue=uniqueHash.get("<wild>"+post);
			 augment+=DISCOUNT*(prevalue*postvalue)/denom;
			 if (augment!=0) {
				 bigramHash.increment(pre, post, augment);
			 }
		   }
		 }
	  }

      public void printBag() {
    	System.out.println("UNIGRAM TOKEN COUNTS:");
    	UnigramTokenCounts.print(null,"");
        System.out.println("===========");

    	System.out.println("UNIGRAM ENAMEX COUNTS:");
    	EntityClassCounts.print(null,"");
        System.out.println("===========");

    	System.out.println("UNIGRAM SPACE COUNTS:");
    	SpaceCounts.print(null,"");
        System.out.println("===========");
        
    	System.out.println("BIGRAM TOKEN COUNTS:");
    	BigramTokenCounts.print(null,"\t");
        System.out.println("===========");        
        
    	System.out.println("BIGRAM ENTITY COUNTS:");
    	EntityClassPairCounts.print(null,"\t");
        System.out.println("===========");     
        
    	System.out.println("UNIQUE CLASS X,* or *,Y COUNTS:");       
        UniqueEntityClassPairCounts.print(null,"");
        System.out.println("===========");                
        
    	System.out.println("TOKEN-TO-ENTITY COUNTS:");
    	UnigramTokenToEntityCounts.print(null,"\t");
        System.out.println("===========");  
       
    	System.out.println("SPACED BIGRAM TOKEN COUNTS:");
    	BigramTokenWithSpaceCounts.print(null,"\t");
        System.out.println("===========");    
        
    	System.out.println("BIGRAM ENAMEX COUNTS:");
    	BigramTokenToEntityCounts.print(null,"\t");
        System.out.println("===========");

      }
}
