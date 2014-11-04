package jessmw;

public class GenericDouble3Var extends BeanSupport {
    //private static String watch[] =    {""};
	
    private String handlename = null;
    private SMRobject handle;
    private XmlFact fact = null;
    private long tod = System.currentTimeMillis();
	/* Generic value variables */
	private double value[] = new double[3];
	
	
    /* Class Constructors */
	public GenericDouble3Var(String h, XmlFact f) throws CloneNotSupportedException {
		handlename = new String(h);
		handle     = SMRcomm.getByHandle(handlename);
		this.fact = f;
		if ( ! fact.getStruct().getHost().getServerName().equals("mrc")) {
			if(!handle.addGeneric(fact.getStruct().getStructName() + "." + fact.getVariable(),this)) {
				throw new CloneNotSupportedException("Fact with name " + fact.getFactName() + " already exists.");
			} 
		} else {
			if(!handle.addStreamvars(fact.getVariable(), this, fact.getStruct().getTime())) {
				throw new CloneNotSupportedException("Variable " + fact.getVariable() + " unknown to mrc.");
			}
		}
    }
	public GenericDouble3Var(String h) throws CloneNotSupportedException {
		handlename = new String(h);
		handle     = SMRcomm.getByHandle(handlename);
		if ( ! fact.getStruct().getHost().getServerName().equals("mrc")) {
			if(!handle.addGeneric(fact.getStruct().getStructName() + "." + fact.getVariable(),this)) {
				throw new CloneNotSupportedException("Fact with name " + fact.getFactName() + " already exists.");
			} 
		} else {
			if(!handle.addStreamvars(fact.getVariable(), this, fact.getStruct().getTime())) {
				throw new CloneNotSupportedException("Variable " + fact.getVariable() + " unknown to mrc.");
			}
		}
    }
	
    /* Callback from SMRobject */  
	boolean updateStringValues(String ret, long time) {
		String tokens[] = ret.split("\\s");
		double val[] = new double[tokens.length];
		for (int i=0; i < tokens.length; i++) {
			val[i] = java.lang.Double.parseDouble(tokens[i]);
		}
		return updateValues(val, time);
	}
	boolean updateValues(double[] ret, long time) {
		boolean changed = false;
		for (int i=0; i < Math.min(value.length, ret.length); i++) {
			if (value[i] != ret[i]) {
				value[i] = ret[i];
				changed = true;
			}
		}
		if (changed) {
			tod = time;
			my_pcs.firePropertyChange(null, null, null);
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
    public double getValue1() {
		return value[0];
	} 
    public double getValue2() {
		return value[1];
	} 
    public double getValue3() {
		return value[2];
	} 
    public double[] getValue() {
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