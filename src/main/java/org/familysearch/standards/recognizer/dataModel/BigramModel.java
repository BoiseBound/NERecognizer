package org.familysearch.standards.recognizer.dataModel;

import java.util.List;

public class BigramModel<T,K> implements java.io.Serializable {
   /**
	 * Used for serialization
	 */
	private static final long serialVersionUID = -9032726045544593443L;
   T key;
   K value;
   /** 
    * Basic model for a pair of entries 
    * @param value1
    * @param value2
    */
   public BigramModel(T value1,K value2) {
	 key  =value1;
	 value=value2;
   }
   public String toString() {
	 return("("+key.toString()+"|"+value.toString()+")");
   }
   public String toString(List<String> IdsToStrings) {
	 String s1,s2;
	 if ((IdsToStrings!=null) && (key instanceof Integer)) {
	   s1=IdsToStrings.get(((Integer)key).intValue());
	 }
	 else {
	   s1=key.toString();
	   
	 }
	 if ((IdsToStrings!=null) && (value instanceof Integer)) {
	   s2=IdsToStrings.get(((Integer)value).intValue());
	 }
	 else {
	   s2=value.toString();   
	 }	 
	 return("("+s1+"|"+s2+")");
   }
   @Override
   public int hashCode() {
	 final int prime = 31;
	 int result = 1;
	 result = prime * result + ((key == null) ? 0 : key.hashCode());
	 result = prime * result + ((value == null) ? 0 : value.hashCode());
	 return result;
   }
   @Override
   public boolean equals(Object obj) {
 	 if (this == obj) {
		return true;
 	 }
	 if (obj == null) {
		return false;
	 }
	 if (getClass() != obj.getClass()) {
		return false;
	 }
	 BigramModel<T,K> other = (BigramModel<T,K>) obj;
	 if (key == null) {
		if (other.key != null) {
			return false;
		}
	 } 
	 else if (!key.equals(other.key)) {
		return false;
	 }
	 if (value == null) {
		if (other.value != null) {
			return false;
		}
	 } 
	 else if (!value.equals(other.value)) {
		return false;
	 }
	 return true;
  }

}
