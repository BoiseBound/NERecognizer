package org.familysearch.standards.recognizer.common;

public class LabeledToken {
	//WORD    OffsetStartWithoutCountingEnamex	EnamexValue		SubEnamexType
    String      main;
    int         startOffset;
    String      enamexType;
    char        position;
    String      subType;
    
    /** 
     * This will test if the entry is a space or not
     * @return
     */
    public boolean isNonSpace() {
      if (enamexType.equals("SP")) {
    	return false;
      }
      return true;
    }
    /** 
     * Check out which positions can follow each other
     * @param position1
     * @param position2
     * @return
     */
    public static boolean follows(String pre,String post) {
      int lnth1, lnth2;
      lnth1=pre.length()-1;
      lnth2=post.length()-1;
      
      char position1=pre.charAt(lnth1);
      char position2=post.charAt(lnth2);

      switch(position1) {
        case 'U': case 'E': case 'O': case '=': 
        {if (position2!='I' && position2!='E')  { return true; } return false;}
        case 'B': case 'I': {
          if (position2=='I' || position2=='E')                                
          { pre =pre.substring(0,lnth1-1);
            post=post.substring(0,lnth2-1);
            if (!pre.equals(post)) {
        	  return false;
            }
            return true; 
          } 
          return false;
        }
      }
      return false;
    }
    /** 
     * This will get the main string
     * @return
     */
    public String getMain() {
      return main;
    }
    
    /** 
     * This will get the enamex string
     * @return
     */
    public String getEnamex() {
      return enamexType;
    } 
    /** 
     * This will SET the full ename
     */
    public void setFullEnamex(String name,String subtype,char position) {
      enamexType=name;
      subType   =subtype;
      this.position=position;
    }
    /** 
     * This will SET the full ename
     */
    public void setFullEnamex(String fullname) {
       int lnth=fullname.length()-1;
      int indx1=fullname.indexOf(".");
      int indx2=fullname.indexOf("-");
      int indxE=indx1;
      if (indxE<0 || indxE>indx2) {
        indxE=indx2;
      }
      if (indxE>=0) {
        this.enamexType=fullname.substring(0,indxE);
      }
      else { 
    	this.enamexType=fullname;
      }
      if (indx2==(lnth-1)) {
    	this.position=fullname.charAt(lnth);  
      }
      if (indx1>=0) {
    	if (indx2>=indx1) {
          this.subType=fullname.substring(indx1+1,indx2);
    	}
    	else {
    	  this.subType=fullname.substring(indx1+1);
    	}
      }
    }    
    /** 
     * This will get the enamex+position string
     * @return
     */
    public String getPositionedEnamex() {
      return enamexType+"-"+position;
    }     
    /** 
     * This will get the FULL enamex string
     * @return
     */
    public String getFullEnamex() {
      if (subType==null) { 
    	return enamexType; 
      }
      return enamexType+"."+subType;
    }    
    
    public int getOffset() {
      return startOffset;
    }
    
    public char getPosition() {
      return position;
    }
    
    public LabeledToken(String token,int offset,String type,char position) {
      main=token;
      startOffset=offset;
      int period=type.indexOf('.');
      if (period>=0) {
    	enamexType=type.substring(0,period);
    	subType=type.substring(period+1);
      }
      else {
        enamexType=type;
        subType=null;
      }
      this.position=position;
    }
    /**
     *  This routine will replace the enamex type's position with an ending
     */
    public void end() {
      if      (position=='B') { position='U'; }
      else if (position=='I') { position='E'; }
    }
    @Override
    public String toString() {
      String main1=main.replaceAll("\n","\\\\n");
      char delimiter='|';
      String other="";
      if (subType!=null) {
    	other=delimiter+subType;
      }
      return("("+main1+delimiter+startOffset+delimiter+enamexType+"-"+position+other+")");
    }
}
