package org.familysearch.standards.recognizer.dataModel;

import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.Iterator;

public class UnigramCountsModel<T> implements java.io.Serializable {

	/**
	 * Value required for serialization
	 */
	private static final long serialVersionUID = 3558664048741986754L;
	
	protected HashMap<T,Double> UnigramCounts;
	
	public UnigramCountsModel() {
	  UnigramCounts=new HashMap<T,Double>();
	}
	
	public void reset() {
	  UnigramCounts.clear();
	}
		
	public Set<T> keySet() {
	  return UnigramCounts.keySet();
	}
		
	public double get(T myKey) {
	  if (UnigramCounts.containsKey(myKey)) {
	    return UnigramCounts.get(myKey).doubleValue();
	  }
	  return 0;
	}
	public void put(T myKey,Double myValue) {
	   UnigramCounts.put(myKey,myValue);
	}
	public boolean increment(T myKey,double increment) {
	  boolean newObject=false;
	  Double myValue=get(myKey);
	  if (myValue==0) {
		myValue=new Double(increment);
		if (increment>0) {
		  newObject=true;
		}
	  }
	  else {
		myValue=new Double(myValue.doubleValue()+increment);
	  }
	  put(myKey,myValue);
	  return newObject;
	}
	public void print(List<String> IdsToStrings,String pad) {
      Iterator<T> uniIter=UnigramCounts.keySet().iterator();
      T myValue;
      Double myVal;
      int getVal;
      if (pad==null) {
    	pad="";
      }
      
      while (uniIter.hasNext()) {
        myValue=uniIter.next();
        myVal=UnigramCounts.get(myValue);
        if ((IdsToStrings!=null) && (myValue instanceof Integer)) {
          getVal=((Integer)myValue).intValue();
          System.out.println(pad+"Unigram<"+myValue.getClass().getName()+">:"+IdsToStrings.get(getVal)+" and COUNT="+myVal);
        }
        else if (myValue instanceof BigramModel) {
           System.out.println(pad+"Unigram<"+myValue.getClass().getName()+">:"+((BigramModel)myValue).toString(IdsToStrings)+" and COUNT="+myVal);	
        }
        else {
          System.out.println(pad+"Unigram<"+myValue.getClass().getName()+">:"+myValue+" and COUNT="+myVal);
        }
      }
	}
}
