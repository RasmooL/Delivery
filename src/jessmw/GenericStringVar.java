package jessmw;


public class GenericStringVar extends BeanSupport {
    //private static String watch[] =    {""};
	
    private String handlename = null;
    private SMRobject handle;
    private XmlFact fact = null;
    private long tod = System.currentTimeMillis();
	/* Generic value variables */
	private String value = "";
	
	
    /* Class Constructors */
	public GenericStringVar(String h, XmlFact f) throws CloneNotSupportedException {
		handlename = new String(h);
		handle     = SMRcomm.getByHandle(handlename);
		this.fact = f;
		//watch[0] = handlename;
		if ( ! fact.getStruct().getHost().getServerName().equals("mrc")) {
			if(!handle.addGeneric(fact.getStruct().getStructName() + "." + fact.getVariable(),this)) {
				throw new CloneNotSupportedException("Fact with name " + fact.getFactName() + "already exists.");
			} 
		} else {
			if(!handle.addStreamvars(fact.getVariable(), this, fact.getStruct().getTime())) {
				throw new CloneNotSupportedException("Variable " + fact.getVariable() + " unknown to mrc.");
			}
		}
    }
	public GenericStringVar(String h) throws CloneNotSupportedException {
		handlename = new String(h);
		handle     = SMRcomm.getByHandle(handlename);
		//watch[0] = handlename;
		if ( ! fact.getStruct().getHost().getServerName().equals("mrc")) {
			if(!handle.addGeneric(fact.getStruct().getStructName() + "." + fact.getVariable(),this)) {
				throw new CloneNotSupportedException("Fact with name " + fact.getFactName() + "already exists.");
			} 
		} else {
			if(!handle.addStreamvars(fact.getVariable(), this, fact.getStruct().getTime())) {
				throw new CloneNotSupportedException("Variable " + fact.getVariable() + " unknown to mrc.");
			}
		}
    }
	
    /* Callback from SMRobject */  
	boolean updateStringValues(String ret, long time) {
		boolean changed = false;
		if (value.compareTo(ret) != 0) {
			value     = ret;
			changed = true;
    	}
		if (changed) {
    		
			tod = time;
			// Call with null because more than one property has changed
			my_pcs.firePropertyChange(null,null,null);
    	}
		return true;
	}
	
	String remove() {
		String cmd;
		if (fact.getStruct().getHost().getServerName() == "mrc")
			return "";
		else {
			cmd = "varpush flush='var " + fact.getStruct().getStructName() + "." + fact.getVariable() + " copy' struct=" + fact.getStruct().getStructName();
			if (fact.getStruct().getPushable())
				cmd = cmd + "\npush flush='" + fact.getStruct().getCmd() + "'";
		}
		cmd = cmd + "\n" + fact.getStruct().getHost().getServerName();
		return cmd;
	}
    
	
    /**************************************************************
     * Public methods - the get-methods will turn into the slots
     * of the shadow-fact in jess
     **************************************************************/ 
    public String getRobot() {
		return handlename;
    } 
    public String getValue() {
		return value;
	} 
    public String getVariable() {
    	return fact.getVariable();
    }
    public String getStruct() {
    	return fact.getStructName();
    }
    public double getLastUpdated() {
		return (double) tod / 1000.0;
	}  
}