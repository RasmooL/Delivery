package jessmw;

import jess.JessException;


public class Odometry extends BeanSupport {
    private static String watch[] = 
      {"$odox","$odoy","$odoth","$ododist","$ododistleft",
       "$ododistright","$odovelocity"};
	
    private String handlename = null;
    private SMRobject handle;
    private double x, y, theta, dist, distl, distr, vel;
	private long tod = 0;
    /* Class Constructors */
    public Odometry(String h) throws JessException {
		handlename = new String(h);
		handle     = SMRcomm.getByHandle(handlename);
		//handle.addPollvars(this,watch);
		String watchCon = ""; //concatenated
		for (int i=0;i < watch.length; i++) {
			watchCon += watch[i] + " ";
		}
		watchCon = watchCon.trim();
		if(!handle.addStreamvars(watchCon, this, 0.0)) {
			throw new JessException("Odometry", "Variables " + watchCon + " unknown to mrc.",0);
		}
    }
    public Odometry(String h, SMRobject handle) throws JessException {
		handlename = new String(h);
		//handle.addPollvars(this,watch);
		String watchCon = ""; //concatenated
		for (int i=0;i < watch.length; i++) {
			watchCon += watch[i] + " ";
		}
		watchCon = watchCon.trim();
		if(!handle.addStreamvars(watchCon, this, 0.0)) {
			throw new JessException("Odometry", "Variables " + watchCon + " unknown to mrc.",0);
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
		if ((vl > 0) && (x != val[0])) {
			x     = val[0];
			changed = true;
		}
		if ((vl > 1) && (y != val[1])) {
			y     = val[1];
			changed = true;
		}
		if ((vl > 2) && (theta != val[2])) {
			theta = val[2];
			changed = true;
		}
		if ((vl > 3) && (dist != val[3])){
			dist  = val[3];
			changed = true;
		}
		if ((vl > 4) && (distl != val[4])){
			distl = val[4];
			changed = true;
		}
		if ((vl > 5) && (distr != val[5])){
			distr = val[5];
			changed = true;
		}
		if ((vl > 6) && (vel != val[6])) {
			vel   = val[6];
			changed = true;
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
    public double getX() {
		return x;
    }
    public double getY() {
		return y;
    }
    public double getTheta() {
		return theta;
    }
    public double getDist() {
		return dist;
    }
    public double getDistLeft() {
		return distl;
    }
    public double getDistRight() {
		return distr;
    }
    public double getVelocity() {
		return vel;
    }
    public double getLastUpdated() {
		return (double) tod / 1000.0;
	}  
}