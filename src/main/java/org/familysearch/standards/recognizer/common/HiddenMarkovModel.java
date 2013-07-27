/**
 * 
 */
package org.familysearch.standards.recognizer.common;
import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import org.familysearch.standards.recognizer.dataModel.CountsCollection;
import org.familysearch.standards.recognizer.dataModel.UnigramCountsModel;
import org.familysearch.standards.recognizer.common.LabeledToken;
import org.familysearch.standards.recognizer.dataModel.SpacingModel;
import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.File;
import java.io.Writer;
import java.io.OutputStreamWriter;


/**
 * @author BoiseBound
 *
 */
public class HiddenMarkovModel {
	
	CountsCollection myModel=new CountsCollection();
	HashMap<String,List<String>> ViterbiPath;
	HashMap<String,Double> ViterbiEndpoint;
	int VERBOSE=0;
	
	public HiddenMarkovModel(CountsCollection myModel) {
	  this.myModel=myModel;
	  ViterbiPath=new HashMap<String,List<String>>();
	  ViterbiEndpoint=new HashMap<String,Double>();
	}
	/** 
	 * Clean up the Viterbi tracker
	 */
	public void resetViterbi() {
	  ViterbiPath.clear();
	  ViterbiEndpoint.clear();
	  List<String> myList=new ArrayList<String>();
	  ViterbiEndpoint.put(CountsCollection.STARTER+"-O",0.0);
	  ViterbiPath.put(CountsCollection.STARTER+"-O",myList);
	}
	
	/** 
	 * 
	 * @param currState
	 * @param prevState
	 * @param totalValue
	 * @param bestToFrom
	 * @param bestScoreTo
	 */
	public void addValueToHashes(String currState,String prevState,double totalValue,
			HashMap<String,String> bestToFrom,HashMap<String,Double> bestScoreTo) {
	  Double myVal=bestScoreTo.get(currState);
	  if (myVal==null || myVal<totalValue) {
		myVal=new Double(totalValue);
		bestToFrom.put(currState,prevState);
		bestScoreTo.put(currState,myVal);
	  }
	}
	
	public void addWeightsToViterbi(HashMap<String,String> bestToFrom,HashMap<String,Double> bestScoreTo) {
	  Iterator<String> myIt=bestScoreTo.keySet().iterator();
	  String myStr, preStr;
	  HashMap<String,List<String>> tmpPath=new HashMap<String,List<String>>();
	  List<String> myList,newList;
	  ViterbiEndpoint.clear();
	  while (myIt.hasNext()) {
		myStr=myIt.next();
		preStr=bestToFrom.get(myStr);
		myList=ViterbiPath.get(preStr);
		newList=new ArrayList<String>();
		newList.addAll(myList);
		newList.add(myStr);
		tmpPath.put(myStr, newList);
		ViterbiEndpoint.put(myStr, bestScoreTo.get(myStr));
	  }
	  ViterbiPath=tmpPath;
	}
	
	public void printViterbi(boolean getBest) {
	  Iterator<String> myIt=ViterbiEndpoint.keySet().iterator();
	  String myStr;
	  boolean entered=false;
	  double bestVal=2;
	  String bestStr="";
	  while (myIt.hasNext()) {
		myStr=myIt.next();
		if (!entered || bestVal< ViterbiEndpoint.get(myStr)) 
		{bestVal=ViterbiEndpoint.get(myStr);bestStr=myStr;entered=true;}
		if (getBest) {continue;}
		System.out.println("SCORE("+myStr+")="+ViterbiEndpoint.get(myStr));
		System.out.println("\t"+ViterbiPath.get(myStr));
	  }
	  if (getBest) {
		System.out.println("SCORE("+bestStr+")="+ViterbiEndpoint.get(bestStr));  
		System.out.println("\t"+ViterbiPath.get(bestStr));		
	  }
	}
	/** 
	 * 
	 * @return
	 */
	public List<String> getBestViterbiPath() {
	  Iterator<String> myIt=ViterbiEndpoint.keySet().iterator();
	  String myStr, bestStr="";
	  double bestVal=0;
	  boolean entered=false;
	  while (myIt.hasNext()) {
		myStr=myIt.next();
		if (!entered || bestVal< ViterbiEndpoint.get(myStr)) 
		{bestVal=ViterbiEndpoint.get(myStr);bestStr=myStr;entered=true;}		
	  }
	  if (!entered) { return (new ArrayList<String>()); }
	  return ViterbiPath.get(bestStr);
	}
	/** 
	 * 
	 * @param taggedContents
	 * @param outFile
	 */
	public void dumpContentsToFile(List<LabeledToken> taggedContents,String outFile) {
	   Writer out =null;
	   LabeledToken myToken;
	   String leader;
	   String fullEnamex;


	   try {	 
		  out = new BufferedWriter(new OutputStreamWriter(
					new FileOutputStream(new File(outFile)), "UTF8"));
		  for (int i=0;i<taggedContents.size();i++) {
			myToken=taggedContents.get(i);  
			if (myToken.getMain().equals(CountsCollection.ENDER)) {
			  continue;
			}
			if (myToken.isNonSpace()) {
			  leader="ENA";
			  fullEnamex=myToken.getPositionedEnamex();
			  if      (fullEnamex.matches("MONEY.*"))                                {leader="NU";}
			  else if (fullEnamex.matches("DATE.*") || fullEnamex.matches("TIME.*")) {leader="TI";}
			  else if (fullEnamex.equals(CountsCollection.ENDER))                    { continue;}
			  if (fullEnamex.endsWith("-B") || fullEnamex.endsWith("-U")) { 
				out.write("<"+leader+"MEX TYPE=\""+myToken.getEnamex()+"\">");
			  }
			  out.write(myToken.getMain());
			  if (fullEnamex.endsWith("-E") || fullEnamex.endsWith("-U")) { 
				out.write("</"+leader+"MEX>");
			  } 
			}
			else {
			  out.write(myToken.getMain());	
			}
		  }
		} 
		catch (Exception e) {
		  System.out.println(e.getMessage());
		}
	   finally {
		 if (out!=null) {
			try {
				out.flush();
				out.close();					
			}
			catch (Exception e) {
			  System.out.println(e.getMessage());
			}
		 }
	   
	   }
	}
    /** 
     * 
     * @param parsedContents
     * @param startIndex
     */
	public void addTagsToOriginalStream(List<LabeledToken> parsedContents,int startIndex) {
	  List<String> myPath=getBestViterbiPath();
	  int i=0,j=0;
	  String fullEnamex;
	  String leader;
	  StringBuffer outBuffer=new StringBuffer();
	  LabeledToken myToken;
	  while (i<myPath.size() && (startIndex+i+j<parsedContents.size())) {
		if (parsedContents.get(i+j+startIndex).isNonSpace()) {
		  fullEnamex=myPath.get(i);
		  if (fullEnamex.equals(CountsCollection.ENDER+"-O")) {
			break;
		  }
//		  leader="ENA";
		  parsedContents.get(i+j+startIndex).setFullEnamex(fullEnamex);
//		  if      (fullEnamex.matches("MONEY.*"))                                {leader="NU";}
//		  else if (fullEnamex.matches("DATE.*") || fullEnamex.matches("TIME.*")) {leader="TI";}
//		  if (fullEnamex.endsWith("-B") || fullEnamex.endsWith("-U")) { 
//			outBuffer.append("<"+leader+"MEX TYPE=\""+myToken.getEnamex()+"\">");
//		  }
//		  outBuffer.append(myToken.getMain());
//		  if (fullEnamex.endsWith("-E") || fullEnamex.endsWith("-U")) { 
//			outBuffer.append("</"+leader+"MEX>");
//		  }          
		  i++;
		}
		else {
//		  myToken=parsedContents.get(i+j+startIndex);
//		  outBuffer.append(myToken.getMain());
		  j++;
		}
	  }
	  System.out.print(outBuffer.toString());
	}
	
	public List<LabeledToken> decode(List<LabeledToken> parsedContents) {
	  UnigramCountsModel<String> previousStateCounter,
	                             currentStateBasedOnPrevWordCounter, 
	                             currentStateBasedOnPrevStateCounter;
	  Set<String> previousState, nextState, tmpState, currentStateBasedOnPrevWord,
	                             currentStateBasedOnPrevState;
	  Iterator<String> prevIt,currIt;
	  String prevState, currState, token;
	  String myRawStr="",lastRawStr="",lastStr="",twoAgoRawStr="";
	  SpacingModel spacer;
	  SpacingModel defaultSpacer=new SpacingModel("");
	  HashMap<String,String> bestToFrom=new HashMap<String,String>();
	  HashMap<String,Double> bestScoreTo=new HashMap<String,Double>();
	  Double preVal;
	  double preValue, totalValue, transValue, obsValue;

	  double augmentAmount, value;
	  int repeat=0,startIndex=0;
	  boolean embedEndStart;
	  int lengthSinceLastBreak=0;
	  
	  nextState=new HashSet<String>();
	  previousStateCounter=getStateSet(CountsCollection.STARTER,CountsCollection.STARTER);
	  previousState=previousStateCounter.keySet();
	  lastRawStr=CountsCollection.STARTER;
	  lastStr=CountsCollection.STARTER;
	  spacer=defaultSpacer;
	  resetViterbi();
	  CountsCollection.addEnder(parsedContents);

	  
	  // Need to have a function that tells use when to end the current sequence.
	  // Will need to put this in the CountsCollection, too.
	  // So we can let the tokenizer determine what's there, and then let the
	  // counters/count-users determine what they want to do with the sequences
      System.err.println("File start...");
	  for (int i=0;i<parsedContents.size();i++) {
		// handle spaces
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
		  embedEndStart=CountsCollection.addEndAndStartOfSequenceAfterThisWord(lastRawStr,twoAgoRawStr,spacer,lengthSinceLastBreak);
		}
		if (embedEndStart || repeat==1) {
			lengthSinceLastBreak=0;
			if (repeat==0) {
			  myRawStr=token=CountsCollection.ENDER;
			  repeat++;
			}
			else {
			  myRawStr=token=CountsCollection.STARTER;
			  repeat=0;
			}
			// Don't increase the value of i
			i--;
		}
		else {
		  myRawStr=parsedContents.get(i).getMain();
		  token=CountsCollection.countsNormalizer(myRawStr);
		  parsedContents.get(i).setFullEnamex("NONE","",'O');  //Flush all categories (though we could have something to keep certain classes)
		  lengthSinceLastBreak++;
		}
		currentStateBasedOnPrevWordCounter=getStateSet(token,myRawStr);
		currentStateBasedOnPrevWord=currentStateBasedOnPrevWordCounter.keySet();
		currIt=currentStateBasedOnPrevWord.iterator();
		
		// Don't actually do anything with this non-token except restart the Viterbi process
		if (VERBOSE>0) {System.out.println("TOKEN="+token);}
		if (token.equals(CountsCollection.STARTER)) {
		  resetViterbi();
		  startIndex=i+1;
		  previousState.clear();
		  previousState.add(CountsCollection.STARTER+"-O");
		}
		else {
	 	 bestToFrom.clear();
	   	 bestScoreTo.clear();
	   	 nextState=new HashSet<String>();

	 	 prevIt=previousState.iterator();
		 while (prevIt.hasNext()) {
		   prevState=prevIt.next();
		   preVal=ViterbiEndpoint.get(prevState);
		   if (preVal==null) { continue; }
		   preValue=preVal.doubleValue();
		   currentStateBasedOnPrevStateCounter=getSubsequentState(prevState);
		   currentStateBasedOnPrevState=currentStateBasedOnPrevStateCounter.keySet();

		   currIt=currentStateBasedOnPrevState.iterator();
		   while (currIt.hasNext()) {
			 currState=currIt.next();
			 if (currState.equals(CountsCollection.ENDER+"-O") && !token.equals(CountsCollection.ENDER)) {
			   continue;
			 }
			 // Eliminate bogus states
			 augmentAmount=CountsCollection.computeStringPairBackoff(token,currState,
					  myModel.getUniqueEntityOrWordPairCounts(),myModel.getUnigramTokenToEntityCounts());
			 value=(myModel.getUnigramTokenToEntityCount(token,myRawStr,currState)+augmentAmount)/myModel.getEntityClassCount(currState);
			 if (value==0) {
			  continue;
			 }
			 // The token must be both in the S(i-1)->S(i) AND the W(i)->S(i)
			 totalValue=preValue;
			 transValue=Math.log(currentStateBasedOnPrevStateCounter.get(currState)/myModel.getEntityClassCount(prevState));
			 obsValue=Math.log(value);
			 totalValue+=transValue+obsValue;
             if (VERBOSE>1) {
			   System.out.println("Checking prev="+prevState+" and currState="+currState+" with word="+token);
			   System.out.println("\tTransitionLogProb="+transValue);
			   System.out.println("\tObservationLogProb="+obsValue);
			   System.out.println("\t\tTotalLogProb="+totalValue);
             }
             nextState.add(currState);
			 addValueToHashes(currState,prevState,totalValue,bestToFrom,bestScoreTo);
		   }
		 }
		 addWeightsToViterbi(bestToFrom,bestScoreTo);
		}
		if (token.equals(CountsCollection.ENDER)) {
          if (VERBOSE>0) {
		    printViterbi(true);
          }
		  addTagsToOriginalStream(parsedContents,startIndex);
		  // Probably need to do subtyping now

		  
		}
		// Let previous and next swap memory space
		previousState=nextState;
		spacer=defaultSpacer;
		twoAgoRawStr=lastRawStr;
        lastStr=token;
        lastRawStr=myRawStr;
	  }
	  return parsedContents;
	}
	/** 
	 * 
	 * @param s
	 * @return
	 */
	protected UnigramCountsModel<String> getStateSet(String s,String rawS) {
	  return myModel.getWordToState(s,rawS);
	}
	/** 
	 * This will get the state sequence
	 * @param s
	 * @return
	 */
	protected UnigramCountsModel<String> getSubsequentState(String s) {
	  return myModel.getStateToState(s);
	}

}
