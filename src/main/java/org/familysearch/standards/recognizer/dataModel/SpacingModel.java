package org.familysearch.standards.recognizer.dataModel;

public class SpacingModel implements java.io.Serializable{

   /**
	 * Needed for serialization
	 */
   private static final long serialVersionUID = -783988398243843929L;
	
   String space;      // Compact representation for the space
   String fullSpace;  // Non-compact representation of the space
   public SpacingModel() 
   { this(""); }
   public SpacingModel(String str) {
	 this.space=compact(str);
	 this.fullSpace=str;
   }
   
   public String getFullSpace() {
	  return fullSpace;
   }
   
   public static String compact(String str) {
	 char[] array=str.toCharArray();
	 char lastChar='a';
	 String carrRtn="\\n";
	 String tabRtn="\\t";
	 int counter=0;
	 StringBuffer sb=new StringBuffer();
	 for (int i=0;i<array.length;i++) {
	   if (i==0 || array[i]!=lastChar) {
		 if (counter>1) {
		   sb.append(counter);
		 }
		 lastChar=array[i];
		 counter=1;
		 if (lastChar=='\n') {
		   sb.append(carrRtn);
		 }
		 else if (lastChar=='\t') {
		   sb.append(tabRtn);
		 }
		 else {
		   sb.append(lastChar);
		 }
	   }
	   else {
		 counter++;
	   }
	 }
	 if (counter==2) {
	   sb.append(counter);  
	 }
	 else if (counter>2) {
	   sb.append("3+");
	 }
	 return(sb.toString());
   }
   public String getSpace() {
	 return space;
   }   
   public boolean hasNonIndentTabsOrMultipleCarriageReturns() {
	 int lnth=space.length();
	 if (lnth<2) {
	   return false;
	 }
	 if (lnth<4 && !space.contains("\\t")) {
	   return false;
	 }
     return true;
   }
   public String toString() {
	 return "["+space+"]";
   }
   @Override
   public int hashCode() {
	 final int prime = 31;
	 int result = 1;
	 result = prime * result + ((space == null) ? 0 : space.hashCode());
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
	SpacingModel other = (SpacingModel) obj;
	if (space == null) {
		if (other.space != null) {
		  return false;
		}
	} 
	else if (!space.equals(other.space)) {
	  return false;
	}
	return true;
}


   

}
