package jessmw;

public class VariableStruct {
    private String structName = null; // struct name in the servers varpool
    private boolean pushable = false; // does this struct have a push command associated with it for update
    private boolean initialized = true; // has the associated push command been called, should be set to true if above is false
    private double pushTime = 0.0; // t value in the push command
    private String command = null; // cmd value in the push command
    private Server host = null; // pointer to the associated server
    
    
    /* Class Constructors */
    public VariableStruct() {
		this.initialized = true;
    }
    public VariableStruct(String stName, boolean push, double pushTime, String cmd, boolean initialized) {
    	this.structName = stName;
    	this.pushable = push;
    	this.pushTime = pushTime;
    	this.command = cmd;
    	this.initialized = initialized;
    }
    
    public String toString() {
    	StringBuffer sb = new StringBuffer();
    	sb.append("Struct details - ");
    	sb.append("Struct: " + getStructName());
    	if (pushable) {
	    	sb.append(" is pushed with " + getCmd());
	    	sb.append(" at a frequency of " + getTime());
	    	sb.append(" s pr call (" + 1.0/getTime());
	    	sb.append(" Hz)\n");
    	} else {
    		sb.append(" does not have a push command\n");
    	}
    	return sb.toString();
    }
    
    public String getStructName() {
    	return structName;
    }
    public void setStructName(String inp) {
    	structName = inp;
    }
    public double getTime() {
    	return pushTime;
    }
    public void setTime(double inp) {
    	pushTime = inp;
    }
    
    public String getCmd() {
    	return command;
    }
    public void setCmd(String inp) {
    	command = inp;
    }
    public Boolean getPushable() {
    	return pushable;
    }
    public void setPushable(boolean inp) {
    	pushable = inp;
    	if (inp) {
    		setInitialized(false);
    	}
    }
    public Boolean getInitialized() {
    	return initialized;
    }
    public void setInitialized(boolean inp) {
    	initialized = inp;
    }
    public void setHost(Server inp) {
    	host = inp;
    }
    public Server getHost() {
    	return host;
    }
       
}