package jessmw;
import jess.*;


public class funSMRTalk implements Userfunction {
    private static final String functionName = "SMRTalk";
	
    public String getName() {
		return functionName;
    }
	
    public Value call(ValueVector vv, Context c) throws JessException {
		String retstr = null; //new String("");
		if ((vv.size() < 2) || (vv.size() > 3)) throw new JessException(functionName,"Wrong number of arguments ",vv.size() - 1);
		
		String cmd = vv.get(1).stringValue(c);
		
		if (vv.size() >= 3) { // second argument is unique name for connection
			retstr = SMRcomm.sendCommand(cmd,"mrc",vv.get(2).stringValue(c));
		} else {
			retstr = SMRcomm.sendCommand(cmd,"mrc");
		}
		return new Value(retstr,RU.STRING);
    }
}