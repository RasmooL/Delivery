package jessmw;
import jess.*;


public class funListConn implements Userfunction {
    private static final String functionName = "SMRListConn";
	
    public String getName() {
		return functionName;
    }
	
    public Value call(ValueVector vv, Context c) throws JessException {
		if ((vv.size() > 1)) throw new JessException(functionName,"Wrong number of arguments ",vv.size() - 1);
		
		SMRcomm.listConnections();
		
		return Funcall.TRUE;
    }
}