package jessmw;

import jess.JessException;


public class DistSensor extends BeanSupport {
    private static String watch[] = 
      {"$irdistleft", "$irdistfrontleft", "$irdistfrontmiddle", 
       "$irdistfrontright","$irdistright"};

    private String handlename = null;
    private SMRobject handle;
    private long tod = 0;
    private double dists[]    = new double[5];
	
    /* Class Constructors */
    public DistSensor(String h) throws JessException {
		handlename = new String(h);
		handle     = SMRcomm.getByHandle(handlename);
		String watchCon = ""; //concatenated
		for (int i=0;i < watch.length; i++) {
			watchCon += watch[i] + " ";
		}
		watchCon = watchCon.trim();
		if(!handle.addStreamvars(watchCon, this, 0.0)) {
			throw new JessException("Distsensor", "Variables " + watchCon + " unknown to mrc.",0);
		}
    }
    public DistSensor(String h, SMRobject handle) throws JessException {
		handlename = new String(h);
		String watchCon = ""; //concatenated
		for (int i=0;i < watch.length; i++) {
			watchCon += watch[i] + " ";
		}
		watchCon = watchCon.trim();
		if(!handle.addStreamvars(watchCon, this, 0.0)) {
			throw new JessException("Distsensor", "Variables " + watchCon + " unknown to mrc.",0);
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
	
    boolean updateValues(double val[], long time) {
    	boolean changed = false;
		int vl = val.length;
		for (int i = 0; (i < 5) && (i < vl); ++i) {
			if (dists[i] != val[i]) {
				dists[i] = val[i];
				changed = true;
			}
		}
		if (changed) {
			this.tod = time;
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
    public double getLeft() {
		return dists[0];
    }
    public double getFrontLeft() {
		return dists[1];
    }
    public double getFrontMiddle() {
		return dists[2];
    }
    public double getFrontRight() {
		return dists[3];
    }
    public double getRight() {
		return dists[4];
    }
    public double getLastUpdated() {
		return (double) tod / 1000.0;
	}  
}