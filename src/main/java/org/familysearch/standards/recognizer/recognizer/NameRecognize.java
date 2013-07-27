package org.familysearch.standards.recognizer.recognizer;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.util.List;

import org.familysearch.standards.recognizer.common.MultilingualTokenizer;
import org.familysearch.standards.recognizer.dataModel.CountsCollection;
import org.familysearch.standards.recognizer.common.LabeledToken;
import org.familysearch.standards.recognizer.common.HiddenMarkovModel;


public class NameRecognize {
	  CountsCollection myModel;
	  MultilingualTokenizer myTokenizer;
	
	  public NameRecognize(String modelFile) {
		myTokenizer=new MultilingualTokenizer();
		System.err.println("Trying to open "+modelFile);
		myModel=new CountsCollection();
		try {
		  FileInputStream fis = new FileInputStream(modelFile);
          ObjectInputStream ois = new ObjectInputStream(fis);
          myModel=(CountsCollection) ois.readObject();
          ois.close();		  
		}
		catch (Exception ex) {
		  ex.printStackTrace();
		}		
		//myModel.printBag();
		System.err.println("Load completed");
	  }
	
	  /**
	   * Load the list of files for processing
	   *
	   * @param fileName file to process
	   */
	  public void loadList(String fileName) {
	    String line;
	    BufferedReader inFile = null;
	    HiddenMarkovModel hmm=new HiddenMarkovModel(myModel);

	    try {
	      InputStream in = new FileInputStream(fileName);
	      InputStreamReader reader = new InputStreamReader(in, "UTF-8");
	      inFile = new BufferedReader(reader);
	      while ((line = inFile.readLine()) != null) {
		    System.out.println("FILENAME="+line);

	    	List<LabeledToken> parsedContents=myTokenizer.loadAndParse(line);
	    	List<LabeledToken> taggedContents=hmm.decode(parsedContents);
	    	hmm.dumpContentsToFile(taggedContents,line+".autoEnamex");
	    	//You can have two column format??
	    	//Do you want to specify the params??
	    	//Do you want to have eliminated classed in training / decoding?
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
	  public void recognize(String listofFiles) {
		loadList(listofFiles);
	  }
	/**
	 * @param args
	 */
	public static void main(String[] args) {
	    if (args.length<2) {
	 	  System.err.println("USAGE: <recognizerModels> <ListOfFilesToRecognize>");
		  System.exit(0);
	    }
		NameRecognize recognizer=new NameRecognize(args[0]);
	    recognizer.recognize(args[1]);
	}

}
