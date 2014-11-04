package jessmw;

import java.text.SimpleDateFormat;
import java.util.Date;

public class GenericTimeVar extends BeanSupport {
    //private static String watch[] =    {""};
	
    private String handlename = null;
    private SMRobject handle;
    private XmlFact fact = null;
    private Date time = new Date();
    private long tod = System.currentTimeMillis();
    private SimpleDateFormat df = new SimpleDateFormat("HH:mm:ss.SSS");
	/* Generic value variables */
	private long value = (long) 0.0;
	
	
    /* Class Constructors */
	public GenericTimeVar(String h, XmlFact f) throws CloneNotSupportedException {
		handlename = new String(h);
		handle     = SMRcomm.getByHandle(handlename);
		this.fact = f;
		time.setTime(value);
		//watch[0] = handlename;
		if(!handle.addGeneric(fact.getStruct().getStructName() + "." + fact.getVariable(),this)) {
			throw new CloneNotSupportedException("Fact with name " + fact.getFactName() + "already exists.");
		}
    }
	public GenericTimeVar(String h) throws CloneNotSupportedException {
		handlename = new String(h);
		handle     = SMRcomm.getByHandle(handlename);
		time.setTime(value);
		//watch[0] = handlename;
		if(!handle.addGeneric(fact.getStruct().getStructName() + "." + fact.getVariable(),this)) {
			throw new CloneNotSupportedException("Fact with name " + fact.getFactName() + "already exists.");
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
	boolean updateValues(double[] ret, long timer) {
		boolean changed = false;
		if (value != (long) (ret[0] * 1000.0)) {
			value = (long) (ret[0] * 1000.0);
			time.setTime(value);
			changed =true;
		}if (changed) {
			tod = timer;
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
    public double getTod() {
		return (double) value / 1000.0;
	} 
    public double getLastUpdated() {
		return (double) tod / 1000.0;
	}  
    public String getVariable() {
    	return fact.getVariable();
    }
    public String getStruct() {
    	return fact.getStructName();
    }
    public String getTime() {
		return df.format(this.time);
	} 
}