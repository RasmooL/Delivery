package jessmw;

import jess.JessException;


public class SocketStatus extends BeanSupport {
    
    private String handlename = null;
    private SMRobject handle;
    private Server server;
    private boolean connected = false;
    private double alive = 0.0;
    private long tod = System.currentTimeMillis();
    
    /* Class Constructors */
    public SocketStatus(String h, Server srv) {
		handlename = new String(h);
		this.server = srv;
		handle     = SMRcomm.getByHandle(handlename);
		handle.sockStatus.put(srv.getServerName(), this);
	}
    public SocketStatus(SMRobject handle, Server srv) {
		handlename = handle.connname;
		this.handle = handle;
		this.server = srv;
		handle.sockStatus.put(srv.getServerName(),this);
	}
	
    /* Callback from SMRobject */
    boolean updateValues() {
    	boolean prevCon = connected;
    	double prevAlive = alive;
    	try {
	    	if (this.server == null) {
	    		connected = false;
	    		alive = 0.0;
	    	} else if (server.getServerName() == "mrc") { 
	    		// data is always polled from mrc before this method is called, so server.connected is always up-to-date for mrc 
	    		connected = server.getConnected();
    		} else if (server.updateConnected(handle.sendCommand("alive", server.getServerName()))) {
    			// other servers needs the test executed in the if-statement
		    	connected = true;
		    	alive = server.getAlive();
		    } else {
		    	connected = false;
		    	alive = 0.0;
		    }
		} catch (JessException e) {
				server.setConnected(false);
				connected = false;
				alive = 0.0;
		} 
    	if ((connected != prevCon) || (prevAlive != alive)) {
	    	tod = System.currentTimeMillis();
			
			// Call with null because more than one property has changed
			my_pcs.firePropertyChange(null,null,null);
    	}
		return true;
    }
	
    /**************************************************************
     * Public methods - the get-methods will turn into the slots
     * of the shadow-fact in jess
     **************************************************************/
    public String getRobot() {
		return handlename;
    }
    public boolean getConnected() {
		return connected;
    }
    public double getAlive() {
    	return alive;
    }
    public double getLastUpdated() {
		return (double) tod / 1000.0;
	}
    public String getServer() {
    	return server.getServerName();
    }
    public String getHost() {
    	return server.getHost();
    }
    public int getPort() {
    	return server.getPort();
    }
}