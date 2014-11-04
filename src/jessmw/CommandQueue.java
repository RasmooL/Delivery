package jessmw;

import java.beans.*;


public class CommandQueue {
    static final int CMD_CHANGING    = -1;
    static final int CMD_UNKNOWN     =  0;
    static final int CMD_QUEUED      =  1;
    static final int CMD_STARTED     =  2;
    static final int CMD_SUSPENDED   =  3;
    static final int CMD_RESTARTED   =  4;
    static final int CMD_DONE        =  5;
    static final int CMD_FLUSHED     =  6;
    static final int CMD_SYNTAXERROR =  7;
	
    private String handlename = null;
    private SMRobject handle;
	private long tod = 0;
    private int cmd_id = -1, status = 0, stopcondition = 0;
    private String command = "";
	
    /* Class Constructors */
    public CommandQueue(String h) {
		handlename = new String(h);
		handle     = SMRcomm.getByHandle(handlename);
		handle.setQueueListener(this);
    }
    public CommandQueue(String h, SMRobject handle) {
		handlename = new String(h);
		handle.setQueueListener(this);
    }
	
	
    /* This is a JavaBean */
    private PropertyChangeSupport my_pcs = new PropertyChangeSupport(this);
	
    public void addPropertyChangeListener(PropertyChangeListener p) {
		my_pcs.addPropertyChangeListener(p);
    }
    public void removePropertyChangeListener(PropertyChangeListener p) {
		my_pcs.removePropertyChangeListener(p);
    }
    
    /* Callback from SMRcomm */
    void changeQueue(String cmd, int id, int stat, int stopcond) {
		this.status        = CMD_CHANGING;
		this.cmd_id        = id;
		this.command       = cmd;
		this.status        = stat;
		this.stopcondition = stopcond;
		this.tod = System.currentTimeMillis();
		// Call with null because more than one property has changed
		my_pcs.firePropertyChange(null,null,null);
    }
	
    /* Helper function to decode enumeration */
    private String decodeStatus(int st) {
		switch (st) {
			case CMD_CHANGING:
				return "CHANGING";
			case CMD_QUEUED:
				return "queued";
			case CMD_STARTED:
				return "started";
			case CMD_SUSPENDED:
				return "suspended";
			case CMD_RESTARTED:
				return "restarted";
			case CMD_DONE:
				return new String("done "+stopcondition);
			case CMD_FLUSHED:
				return "flushed";
			case CMD_SYNTAXERROR:
				return "syntaxerror";
			case CMD_UNKNOWN:
			default:
				return "UNKNOWN";
		}
    }
	
    /**************************************************************
     * Public methods - the get-methods will turn into the slots
     * of the shadow-fact in jess
     **************************************************************/
    public String getRobot() {
		return handlename;
    }
    public int getId() {
		return cmd_id;
    }
    public String getCmdStr() {
		return command;
    }
    public String getStatus() {
		return decodeStatus(status);
    }
    public double getLastUpdated() {
		return (double) tod / 1000.0;
	}  
}