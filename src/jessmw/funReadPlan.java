package jessmw;
import jess.*;

import java.io.*;


public class funReadPlan implements Userfunction {
    private static final String functionName = "SMRLoadPlan";
	
    public String getName() {
		return functionName;
    }
	
    public Value call(ValueVector vv, Context c) throws JessException {
		String retstr = null; //new String("");
		String hostname = null;
		if ((vv.size() < 3) || (vv.size() > 4)) throw new JessException(functionName,"Wrong number of arguments, use (" + functionName + " filename planname [instance])",vv.size() - 1);
		
		String filename = vv.get(1).stringValue(c);
		String planname = vv.get(2).stringValue(c);
		if (vv.size() >= 4) { // third argument is unique name for connection
			hostname = vv.get(3).stringValue(c);
		}
		
		retstr = SMRcomm.sendCommand("beginplan " + planname,"mrc",hostname);
		if (!retstr.contains("ok")){
			return new Value("Failed to begin plan: " + retstr,RU.STRING);
		}
		try {
			FileInputStream fstream = new FileInputStream(filename);
			DataInputStream in = new DataInputStream(fstream);
			BufferedReader br = new BufferedReader(new InputStreamReader(in));
			
			String strLine, planLines = "";
			while ((strLine = br.readLine()) != null)   {
					planLines += "plan " + strLine + "\n"; // readLine strips \n
			}
			retstr = SMRcomm.sendCommand(planLines,"mrc",hostname);
			br.close();
			in.close();
			fstream.close();
		} catch (FileNotFoundException fe) {
			throw new JessException(functionName, "File not found", filename);
		} catch (SecurityException se) {
			throw new JessException(functionName, "Access to file denied", filename);
		} catch (IOException e) {
			throw new JessException(functionName, "IOError", e.getMessage());
		} finally {
		retstr = SMRcomm.sendCommand("endplan","mrc",hostname);
		}
		return new Value(retstr,RU.STRING);
    }
}