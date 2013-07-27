package org.familysearch.standards.recognizer.trainer;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashSet;
import java.util.List;
import org.familysearch.standards.recognizer.common.LabeledToken;
import org.familysearch.standards.recognizer.dataModel.CountsCollection;
import org.familysearch.standards.recognizer.common.Visibility;
import org.familysearch.standards.recognizer.common.MultilingualTokenizer;

  public class TrainModel {
	  /** 
	   * This inner enum class will be used for identifying which half of the data to train or test on
	   * @author BoiseBound
	   *
	   */
	  public enum DataHalf {
		 EVEN(0), ODD(1);
		 private int code;
		 private DataHalf (int c) {
		   code = c;
		 }		 
		 public int getCode() {
		   return code;
		 }
	  }

 	  /**
	   * Load the list of files for processing
	   *
	   * @param fileName file to process
	   */
	  public void loadList(String fileName,DataHalf whichHalf,Visibility visibleLevel,
			  CountsCollection myModel,HashSet<String> IdsVisible) {
	    String line;
	    BufferedReader inFile = null;
	    int lineCount=0;
	    MultilingualTokenizer myTokenizer=new MultilingualTokenizer();
        if (fileName==null) {
          return;
        }
	    try {
	      InputStream in = new FileInputStream(fileName);
	      InputStreamReader reader = new InputStreamReader(in, "UTF-8");
	      inFile = new BufferedReader(reader);
	      while ((line = inFile.readLine()) != null) {
	    	lineCount++;
	    	if (whichHalf.getCode()!=(lineCount%2))  {
	    	  continue;
	    	}
	    	List<LabeledToken> parsedContents=myTokenizer.loadAndParse(line);
	    	myModel.addContentsToModel(parsedContents,visibleLevel,IdsVisible);
	      }
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
	  }	  	
	  public void train(String listofFiles,String output) {
		CountsCollection bag1=new CountsCollection();
		HashSet<String> IdsVisible=new HashSet<String>();
		loadList(listofFiles,DataHalf.ODD,Visibility.VISIBLE,bag1,IdsVisible);
		loadList(listofFiles,DataHalf.EVEN,Visibility.BLIND,bag1,IdsVisible);
		// Do some sort of reset or model keeping
		IdsVisible.clear();
		loadList(listofFiles,DataHalf.EVEN,Visibility.VISIBLE,bag1,IdsVisible);
		loadList(listofFiles,DataHalf.ODD,Visibility.BLIND,bag1,IdsVisible);	
		bag1.prepareWeightsForBackoff();
		System.out.println("BAG1::");
		bag1.printBag();
		
		// Dump the bags
		try {
		  FileOutputStream fos = new FileOutputStream(output);
          ObjectOutputStream oos = new ObjectOutputStream(fos);
          oos.writeObject(bag1);
          oos.close();
		}
		catch (Exception ex) {
		  System.err.println("Dumping problems");
		}
		
		CountsCollection bag3=new CountsCollection();
		try {
		  FileInputStream fis = new FileInputStream(output);
          ObjectInputStream ois = new ObjectInputStream(fis);
          bag3=(CountsCollection) ois.readObject();
          ois.close();		  
		}
		catch (Exception ex) {
		  System.err.println("Loading problems");
		}		

	  }
	  /**
	   * @param args
	   */
	  public static void main(String[] args) {
	    TrainModel modeler=new TrainModel();
	    if (args.length<1) {
	 	  System.err.println("USAGE: <ListOfTrainingFiles> <Output.ser>");
		  System.exit(0);
	    }
	    modeler.train(args[0],args[1]);
	  }

  }
