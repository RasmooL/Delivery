
package jessmw;
import jess.*;


public class funDisconnect implements Userfunction {
    private static final String functionName = "SMRDisconnect";
	
    public String getName() {
		return functionName;
    }
	
    public Value call(ValueVector vv, Context c) throws JessException {
		if ((vv.size() < 1) || (vv.size() > 2)) throw new JessException(functionName,"Wrong number of arguments ",vv.size() - 1);
		
		if (vv.size() >= 2) { // first argument is unique name for connection
			SMRcomm.disconnect(c, vv.get(1).stringValue(c));
		} else {
			SMRcomm.disconnect(c);
		}
		return Funcall.TRUE;
    }
}