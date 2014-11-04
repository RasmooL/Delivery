package jessmw;

import jess.JessException;

public class UpdateInterval extends BeanSupport {
    //private static String watch[] =    {""};
	
    private String handlename = null;
    private SMRobject handle;
    private long tod = System.currentTimeMillis();
    	
	private int measuredTime = 500;
	private int desiredSleepTime = 500;
	/* Class Constructors */
	public UpdateInterval(String h) {
		handlename = new String(h);
		handle     = SMRcomm.getByHandle(handlename);

		handle.sleepTimer = this;
    }
	
	public UpdateInterval(String h, int timeout) {
		handlename = new String(h);
		handle     = SMRcomm.getByHandle(handlename);
		desiredSleepTime = timeout;
		handle.sleepTimer = this;
    }
	
	public UpdateInterval(SMRobject h, int timeout) {
    	handle     = h;
		handlename = h.getHostname();
		desiredSleepTime = timeout;
    }

	/* Callback from SMRobject */  
	boolean updateValues() {
		measuredTime = (int) (System.currentTimeMillis() - tod);
		tod = System.currentTimeMillis();
		// Call with null because more than one property has changed
		my_pcs.firePropertyChange(null,null,null);
		return true;
    } 
	
    /**************************************************************
     * Public methods - the get-methods will turn into the slots
     * of the shadow-fact in jess
     **************************************************************/ 
    public String getRobot() {
		return handlename;
    } 
    public double getLastUpdated() {
		return (double) tod / 1000.0;
	}  
	public int getMeasuredTime_ms() {
		return measuredTime;
	}
	public double getMeasuredFreq() {
		return 1.0/(measuredTime/1000.0);
	}
	public void setDesiredTime_ms(int inp) {
		if (inp % 10 != 0) {
			inp = (int)(Math.round(inp/10.0) * 10);
		}
		if (inp == 0) {
			inp = 10;
		}
		desiredSleepTime = inp;
		try {
			handle.SendMRCCommand("stream " + desiredSleepTime/10, null);
		} catch (JessException je) {}
	}
	public void setDesiredTime_ms(double inp) {
		setDesiredTime_ms((int) inp);
	}
	public int getDesiredTime_ms() {
		return desiredSleepTime;
	}
	public void setDesiredFreq(double inp) {
		setDesiredTime_ms((int) (1/(inp/1000.0)));
	}
	
	public void setDesiredFreq(int inp) {
		setDesiredTime_ms((int) (1/(inp/1000.0)));
	}
	public double getDesiredFreq() {
		return 1.0/(desiredSleepTime/1000.0);
	}
}