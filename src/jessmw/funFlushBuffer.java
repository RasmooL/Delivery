package jessmw;
import jess.*;


public class funFlushBuffer implements Userfunction {
    private static final String functionName = "SMRFlushBuffer";
	
    public String getName() {
		return functionName;
    }
	
    public Value call(ValueVector vv, Context c) throws JessException {
		String retstr = null; //new String("");
		if ((vv.size() < 2) || (vv.size() > 3)) throw new JessException(functionName,"Wrong number of arguments ",vv.size() - 1);
		
		if (vv.size() >= 3) { // second argument is unique name for connection
			SMRcomm.flushBuffer(vv.get(1).stringValue(c),vv.get(2).stringValue(c));
		} else { //use lastSmr if no smr is supplied
			SMRcomm.flushBuffer(vv.get(1).stringValue(c), null);
		}
		return new Value(retstr,RU.STRING);
    }
}