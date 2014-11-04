package jessmw;

public class XmlFact {
    static final int TYPE_ERROR      = -1;
    static final int TYPE_UNKNOWN    =  0;
    static final int TYPE_DOUBLE     =  1;
    static final int TYPE_POSE       =  2;
    static final int TYPE_TIME       =  3;
    static final int TYPE_STRING     =  4;
    
    private String factName = null; // the shadow fact's name
    private String type = null; //what type of Generic**Var should be used
    private String variable = null; // the var name on the server
    private int width = 0; // width if a double[] var
    private boolean initialized = false; // have varpush been called for this fact on the server
    private VariableStruct struct = null; // pointer to the struct of this fact
    
    
    /* Class Constructors */
    public XmlFact() {
    	this.initialized = false;
    }
        
    public String toString() {
    	StringBuffer sb = new StringBuffer();
    	sb.append("Fact details - ");
    	sb.append("Fact name: " + getFactName());
    	sb.append(" (Variable is derived from ");
    	sb.append(struct.getStructName());
    	sb.append("." + getVariable());
    	sb.append(" on ");
    	sb.append(struct.getHost().getServerName());
    	sb.append(") is a " + getType());
    	sb.append(" with width " + getwidth());
    	sb.append("\n");
    	
    	return sb.toString();
    }
    
    public String getFactName() {
    	return factName;
    }
    public void setFactName(String inp) {
    	factName = inp;
    }
    public String getType() {
    	return type;
    }
    public void setType(String inp) {
    	type = inp;
    }
    
    public String getVariable() {
    	return variable;
    }
    public void setVariable(String inp) {
    	variable = inp;
    }
    
    public int getwidth() {
    	return width;
    }
    public void setwidth(int inp) {
    	width = inp;
    }
    public VariableStruct getStruct() {
    	return struct;
    }
    
    public void setStruct(VariableStruct inp) {
    	struct = inp;
    }
    
    public String getStructName() {
    	return struct.getStructName();
    }

	public boolean getInitialized() {
		return initialized;
	}

	public void setInitialized(boolean initialized) {
		this.initialized = initialized;
	}
    
}