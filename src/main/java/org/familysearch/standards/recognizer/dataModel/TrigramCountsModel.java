package org.familysearch.standards.recognizer.dataModel;

import java.util.Iterator;
import java.util.List;

public class TrigramCountsModel<K1,K2,K3> implements java.io.Serializable {

   /**
	 * Needed for serialization
	 */
   private static final long serialVersionUID = 8907997649848460636L;
	
   protected BigramCountsModel<K1,BigramModel<K2,K3>> TrigramCounts;
   
   TrigramCountsModel() {
	 TrigramCounts=new BigramCountsModel<K1,BigramModel<K2,K3>>();
   }
   /** 
    * 
    * @param bigramPiece1
    * @return
    */
   public UnigramCountsModel<BigramModel<K2,K3>> get(K1 trigramPiece1) {
	 return TrigramCounts.get(trigramPiece1);
   }
   /** 
    * 
    * @param bigramPiece1
    * @param model
    */
   public void put(K1 trigramPiece1,UnigramCountsModel<BigramModel<K2,K3>> model) {
	 TrigramCounts.put(trigramPiece1,model);
   }
   /** 
    * 
    * @param trigramPiece1
    * @param trigramPiece2
    * @param trigramPiece3
    * @return
    */
   public Double get(K1 trigramPiece1,K2 trigramPiece2,K3 trigramPiece3) {
	 return get(trigramPiece1,new BigramModel<K2,K3>(trigramPiece2,trigramPiece3));
   }
   /** 
    * 
    * @param trigramPiece1
    * @param biModel
    * @return
    */
   public Double get(K1 trigramPiece1,BigramModel<K2,K3> biModel) {
	 UnigramCountsModel<BigramModel<K2,K3>> model=get(trigramPiece1);
	 if (model==null) {
	   return null;
	 }
	return model.get(biModel);	   
   }
   /** 
    * 
    * @param bigramPiece1
    * @param bigramPiece2
    * @param incrementSize
    */
   public void increment(K1 trigramPiece1,BigramModel<K2,K3> biModel,int incrementSize) {
	 UnigramCountsModel<BigramModel<K2,K3>> model=get(trigramPiece1);
	 if (model==null) {
	   model=new UnigramCountsModel<BigramModel<K2,K3>>();
	   put(trigramPiece1,model);
	 }
	 model.increment(biModel,incrementSize);
   }
   /** 
    * 
    * @param bigramPiece1
    * @param bigramPiece2
    * @param incrementSize
    */
   public void increment(K1 trigramPiece1,K2 trigramPiece2,K3 trigramPiece3,int incrementSize) {
	 this.increment(trigramPiece1,new BigramModel<K2,K3>(trigramPiece2,trigramPiece3),incrementSize);
   }   
   /** 
    * 
    * @param IdsToStrings
    */
   public void print(List<String> IdsToStrings,String pad) {
     Iterator<K1> triIter=TrigramCounts.keySet().iterator();
     K1 myValue;
     UnigramCountsModel<BigramModel<K2,K3>> model;
     int getVal;
     if (pad==null) {
       pad="";
     }
	 while (triIter.hasNext()) {
	   myValue=triIter.next();
 	   model=TrigramCounts.get(myValue);
       if ((IdsToStrings!=null) && (myValue instanceof Integer)) {
         getVal=((Integer)myValue).intValue();
         System.out.println("Trigram<"+myValue.getClass().getName()+">:"+IdsToStrings.get(getVal)+"|+|");
       }
       else {
         System.out.println("Trigram<"+myValue.getClass().getName()+">:"+myValue+"|+|");
       }
 	   model.print(IdsToStrings,pad);
	 }
   }   
}
