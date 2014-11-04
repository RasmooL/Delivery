package jessmw;
import jess.*;


public class funPushTalk implements Userfunction {
    private static final String functionName = "PushTalk";
	
    public String getName() {
		return functionName;
    }
	
    public Value call(ValueVector vv, Context c) throws JessException {
		String retstr = new String("");
		if ((vv.size() < 3) || (vv.size() > 4)) throw new JessException(functionName,"Wrong number of arguments ",vv.size() - 1);
		
		String cmd = vv.get(1).stringValue(c);
		
		if (vv.size() >= 4) { // third argument is unique name for connection
			SMRcomm.sendPushCommand(cmd,vv.get(2).stringValue(c),vv.get(3).stringValue(c));
		} else {
			SMRcomm.sendPushCommand(cmd,vv.get(2).stringValue(c));
		}
		return new Value(retstr,RU.STRING);
    }
}