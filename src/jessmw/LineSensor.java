package jessmw;

import jess.JessException;


public class LineSensor extends BeanSupport {
    private static String watch[] = 
      {"$line0","$line1","$line2","$line3","$line4","$line5",
       "$line6","$line7","$linepos"};
	
    private String handlename        = null;
    private SMRobject handle;
    private double linepos,lineval[] = new double[8];
    private long tod = 0;
	
    /* Class Constructors */
    public LineSensor(String h) throws JessException {
		handlename = new String(h);
		handle     = SMRcomm.getByHandle(handlename);
		String watchCon = ""; //concatenated
		for (int i=0;i < watch.length; i++) {
			watchCon += watch[i] + " ";
		}
		watchCon = watchCon.trim();
		if(!handle.addStreamvars(watchCon, this, 0.0)) {
			throw new JessException("LineSensor", "Variables " + watchCon + " unknown to mrc.",0);
		}
    }
    public LineSensor(String h, SMRobject handle) throws JessException {
		handlename = new String(h);
		String watchCon = ""; //concatenated
		for (int i=0;i < watch.length; i++) {
			watchCon += watch[i] + " ";
		}
		watchCon = watchCon.trim();
		if(!handle.addStreamvars(watchCon, this, 0.0)) {
			throw new JessException("LineSensor", "Variables " + watchCon + " unknown to mrc.",0);
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
		for (int i = 0; (i < 8) && (i < vl); ++i) {
			if (lineval[i] != val[i]) {
				lineval[i] = val[i];
				changed = true;
			}
		}
		if ((vl > 8) && (linepos  != val[8])) {
			linepos  = val[8];
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
    public double[] getValues() {
		return lineval;
    }
    public double getLastUpdated() {
		return (double) tod / 1000.0;
	}  
    public double getLinePos() {
		return linepos;
    }
}