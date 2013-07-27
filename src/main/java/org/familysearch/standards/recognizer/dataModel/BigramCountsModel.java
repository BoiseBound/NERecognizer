package org.familysearch.standards.recognizer.dataModel;

import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.Iterator;

public class BigramCountsModel<T,K> implements java.io.Serializable {
   protected HashMap<T,UnigramCountsModel<K>> BigramCounts;

   /**
	 * ID used for serialization
	 */
   private static final long serialVersionUID = 4109259502927530949L;
   
   /** 
    * Basic constructor
    */
   public BigramCountsModel() {
	 BigramCounts=new HashMap<T,UnigramCountsModel<K>>();
   }
   /** 
    * 
    * @param bigramPiece1
    * @return
    */
   public UnigramCountsModel<K> get(T bigramPiece1) {
	 return BigramCounts.get(bigramPiece1);
   }
   /** 
    * 
    * @param bigramPiece1
    * @param model
    */
   public void put(T bigramPiece1,UnigramCountsModel<K> model) {
	 BigramCounts.put(bigramPiece1,model);
   }
   /** 
    * 
    * @param bigramPiece1
    * @param bigramPiece2
    * @return
    */
   public Double get(T bigramPiece1,K bigramPiece2) {
	 UnigramCountsModel<K> model=get(bigramPiece1);
	 if (model==null) {
	   return null;
	 }
	 return model.get(bigramPiece2);
   }
   /** 
    * 
    * @param bigramPiece1  Left-hand object of the pair
    * @param bigramPiece2  Right-hand object of the pair
    * @param incrementSize
    * @return true if the incrementer needed to create a new pairing
    */
   public boolean increment(T bigramPiece1,K bigramPiece2,double incrementSize) {
	 UnigramCountsModel<K> model=get(bigramPiece1);
	 boolean newObject=false;

	 if (model==null) {
	   model=new UnigramCountsModel<K>();
	   put(bigramPiece1,model);
	   newObject=true;
	 }
	 newObject|=model.increment(bigramPiece2,incrementSize);
	 return newObject;
   }
   
   public Set<T> keySet() {
	 return BigramCounts.keySet();
   }
   /** 
    * 
    * @param IdsToStrings
    */
   public void print(List<String> IdsToStrings,String pad) {
     Iterator<T> biIter=BigramCounts.keySet().iterator();
     T myValue;
     UnigramCountsModel<K> model;
     int getVal;
     if (pad==null) {
       pad="";
     }
	 while (biIter.hasNext()) {
	   myValue=biIter.next();
 	   model=BigramCounts.get(myValue);
       if ((IdsToStrings!=null) && (myValue instanceof Integer)) {
         getVal=((Integer)myValue).intValue();
         System.out.println(pad+"Bigram<"+myValue.getClass().getName()+">:"+IdsToStrings.get(getVal)+"|+|");
       }
       else if (myValue instanceof BigramModel) {
         System.out.println(pad+"Bigram<"+myValue.getClass().getName()+">:"+((BigramModel)myValue).toString(IdsToStrings)+"|+|");	
        }
       else {
         System.out.println(pad+"Bigram<"+myValue.getClass().getName()+">:"+myValue+"|+|");
       }
 	   model.print(IdsToStrings,pad+pad);
	 }
   }   
}
