package jessmw;

import jess.*;

import java.util.Hashtable;
import java.util.List;


public class SMRcomm {
    private static Hashtable<String, SMRobject> smrobjs = new Hashtable<String, SMRobject>();
    private static SMRobject lastsmr = null;
    
    private SMRcomm() {
	// Empty. Just declares constructor private
    }

	/**
	 * Method which connects and creates the specific SMRobject with its respective SMR, specified when
	 * calling the function SMRConnect
	 * 
	 * @param String        name of the instance, should be unique engine-wide
	 * @param List<server>  List of servers that the given SMRobject (or instance) should
	 * 						connect to
	 * 
	 */
    
    public static void connect(String name, List<Server> serv) {
    	if(!smrobjs.containsKey(name)) {
    		lastsmr = new SMRobject(name, serv);
    		smrobjs.put(name, lastsmr);
    	}
    }
    
    /**
     * Wrapper to remove an SMRobject from the Hashtable.
     * Handles exceptions gracefully and sets lastsmr to null if 
     * it matches the supplies instance name
     * 
     * @param String        Instance name to be removed
     * 
     */
    protected static void removeFromList(String instName) {
    	try {
			if (!smrobjs.containsKey(instName)) return;
			SMRobject smr=(SMRobject)smrobjs.remove(instName);
			if (lastsmr==smr) lastsmr=null;
    	} catch (NullPointerException e) {
    		System.out.println("Error: " + e.toString());
		}
    }

    /**
	 * Method which transmits commands to the push socket from the 
	 * Talk function to the specified SMR for execution.
	 * 
	 * @param String  Command string to be transmitted to the SMR.
	 * @param String  Server name that the command should be sent to
	 * @param String  Name of the SMR to sent the command string to.
	 * 
	 * 
	 * @throws JessException  Exception thrown if the specified SMR does not exist in the hash table,
	 *                        signifying that it has not been connected.
	 */
    public static void sendPushCommand(String cmd, String server, String name) throws JessException {
		if (!smrobjs.containsKey(name)) {
			throw new JessException("sendCommand","invalid connection specifier",name);
		}
		lastsmr = (SMRobject)smrobjs.get(name);
		lastsmr.sendPushCommand(cmd, server);
    }
	
    /**
	 * Method which transmits commands to the push socket from the 
	 * Talk function to the specified SMR for execution.
	 * 
	 * @param String  Command string to be transmitted to the SMR.
	 * @param String  Server name that the command should be sent to
	 * 
	 * 
	 * @throws JessException  Exception thrown if the specified SMR does not exist in the hash table,
	 *                        signifying that it has not been connected.
	 */
    public static void sendPushCommand(String cmd, String server) throws JessException {
		if (lastsmr == null) {
			throw new JessException("sendCommand","invalid connection specifier",server);
		}
		lastsmr.sendPushCommand(cmd, server);
    }
	
    /**
	 * Method which flushes the input server socket on the supplied SMR
	 * 
	 * @param String  Server name that the command should be sent to
	 * @param String  Name of the SMR to sent the command string to.
	 * 
	 * @throws JessException  Exception thrown if the specified SMR does not exist in the hash table,
	 *                        signifying that it has not been connected.
	 */
    public static void flushBuffer(String server, String name) throws JessException {
    	if (name != null) {
			if (!smrobjs.containsKey(name)) {
				throw new JessException("sendCommand","invalid connection specifier",name);
			}
			lastsmr = (SMRobject)smrobjs.get(name);
    	}
		lastsmr.flushBuffer(server);
    }
	
    /**
	 * Method which transmits commands from the Talk function to the specified SMR for execution.
	 * 
	 * @param String  Command string to be transmitted to the SMR.
	 * @param String  Server name that the command should be sent to
	 * @param String  Name of the SMR to sent the command string to.
	 * 
	 * @return String  Returns the response from the MRC, returned by the sendCommand function.
	 * 
	 * @throws JessException  Exception thrown if the specified SMR does not exist in the hash table,
	 *                        signifying that it has not been connected.
	 */
    public static String sendCommand(String cmd, String server, String name) throws JessException {
    	if (name != null) {
			if (!smrobjs.containsKey(name)) {
				throw new JessException("sendCommand","invalid connection specifier",name);
			}
			lastsmr = (SMRobject)smrobjs.get(name);
    	}
		return lastsmr.sendCommand(cmd, server);
    }
	
    /**
	 * Method which transmits commands from the Talk function to the specified host and server for execution.
	 * 
	 * @param String  Command string to be transmitted to the SMR.
	 * @param String  Name of the server to sent the command string to.
	 * 
	 * @return String  Returns the response from the MRC, returned by the sendCommand function.
	 * 
	 * @throws JessException  Exception thrown if the specified SMR does not exist in the hash table,
	 *                        signifying that it has not been connected.
	 */
    public static String sendCommand(String cmd,String server) throws JessException {
    	if (lastsmr == null) return new String("");
		return lastsmr.sendCommand(cmd, server);
    }
	
	/**
	 * Method which disconnects the specified SMR, removing it from the hash table, resetting the last SMR
	 * indicator to null if the last SMR is the one being disconnected.
	 * 
	 * @param String  Name of the specific SMR to disconnect.
	 */
    public static void disconnect(Context c, String name) {
		SMRobject smr = (SMRobject)smrobjs.remove(name);
		if (smr == lastsmr) lastsmr = null;
		if (smr != null) smr.disconnect(c);
    }
	
	/**
	 * Method which disconnects all currently connected SMRs, clears the hash table and resets the last SMR
	 * indicator back to null.
	 */
    public static void disconnect(Context c) {
		for (java.util.Enumeration<String> e = smrobjs.keys(); e.hasMoreElements();) {
			Object key    = e.nextElement();
			SMRobject smr = (SMRobject)smrobjs.get(key);
			smr.disconnect(c);
		}
		smrobjs.clear();
		lastsmr = null;
    }
	
	/**
	 * Method which prints out a list of connected SMRs as well as the name of the host to which they are
	 * connected.
	 */
    public static void listConnections() {
		for (java.util.Enumeration<String> e = smrobjs.keys(); e.hasMoreElements();) {
			Object key    = e.nextElement();
			SMRobject smr = (SMRobject)smrobjs.get(key);
			System.out.println(key + ": connected to " + smr.getHostname());
			smr.listConnections();
		}
    }
	
	/**
	 * Method which returns a specific SMRobject pointer by specifying the name of the SMR held in the
	 * object.
	 * 
	 * @param String  Name of the SMR whose SMRobject pointer is to be returned.
	 * 
	 * @return SMRobject  Returns the SMRobject pointer containing the specified SMR name.
	 */
    public static SMRobject getByHandle(String name) {
		return (SMRobject)smrobjs.get(name);
    }
	
    /**
	 * Method to set up the structs on the servers. Resolves the correct SMRobject and calls the 
	 * equivalent method in SMRobject. Sends push commands to the servers and modules that needs it
	 * @param facts				List of XmlFacts with relevant info
	 * @param smrName			Name of SMR, uses last if null
	 * @throws JessException	Thrown if SMR is not found.
	 */
	public static void initStrucks(List<VariableStruct> structs, String smrName) throws JessException {
		if (smrName != null) {
			if (!smrobjs.containsKey(smrName)) {
				throw new JessException("setModuleStatus","invalid connection specifier",smrName);
			}
			lastsmr = (SMRobject)smrobjs.get(smrName);
		}
		lastsmr.initPush(structs);
	}
	
	/**
	 * Method to set up the structs on the servers. Resolves the correct SMRobject and calls the 
	 * equivalent method in SMRobject. Sends push commands to the servers and modules that needs it
	 * @param facts				List of XmlFacts with relevant info
	 * @param smrName			Name of SMR, uses last if null
	 * @throws JessException	Thrown if SMR is not found.
	 */
	public static void initFacts(List<XmlFact> facts, String smrName) throws JessException {
		if (smrName != null) {
			if (!smrobjs.containsKey(smrName)) {
				throw new JessException("setModuleStatus","invalid connection specifier",smrName);
			}
			lastsmr = (SMRobject)smrobjs.get(smrName);
		}
		lastsmr.initFacts(facts);
	}
	
	public static boolean retractPushvar(String key, String smrName) throws JessException {
		if (smrName != null) {
			if (!smrobjs.containsKey(smrName)) {
				throw new JessException("setModuleStatus","invalid connection specifier",smrName);
			}
			lastsmr = (SMRobject)smrobjs.get(smrName);
		}
		return lastsmr.retractPushvar(key);
	}
	
}
