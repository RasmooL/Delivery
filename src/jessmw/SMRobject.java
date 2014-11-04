package jessmw;

import jess.*;

import java.io.*;
import java.util.Hashtable;
import java.util.List;
import java.util.Vector;
import java.util.Enumeration;

public class SMRobject implements Runnable {
    public String connname            			 = null; // engine-wide unique identifier
    private Hashtable<String,Server> servers =  new Hashtable<String, Server>(); // table with servers this object is connected to
    
    public UpdateInterval sleepTimer = new UpdateInterval(this, 1000); // measures the update interval and instructs time between mrc pulls
    public Hashtable<String,SocketStatus> sockStatus = new Hashtable<String,SocketStatus>(); // status of the servers associated with this object
    
    private boolean debug               = false; // print out a lot of debug
    private boolean running 		    = false; // flag to signal if run() should be aborted 
    private boolean waiting_for_motion_status = false;
    private CurrentCommand cmd_listener = null; // listener for cmds
    private CommandQueue queue_listener = null; // listener for cmd queue
    private int curcmd_id = 0, queue_id = 0; // updated ID numbers
    private int streamTime = 50; //time between streaming values from MRC, in 10th of milliseconds
	
 	
    /* This class is the elements of the command queue */ 
    private class CmdQueueObj {
		private String cmdstr = null;
		private int status = -1, stopcond = -1;
		private CmdQueueObj(String cs, int st, int sc) {
			this.cmdstr   = cs;
			this.status   = st;
			this.stopcond = sc;
		}
    }
    /* Vector to hold the command queue */
    private Vector<CmdQueueObj> cmdqueue=new Vector<CmdQueueObj>();
    private CmdQueueObj emptyQueueObj = new CmdQueueObj("",CurrentCommand.CMD_UNKNOWN, -1);
	
    /* Class to hold pairs of listening beans/watches */
    private class ListenObj {
		private Object bean;
		private String watches[];
		private ListenObj(Object o, String w[]) {
			this.bean    = o;
			this.watches = w;
		}
    }
    
    /* Vector to hold ListenObjs describing all watched variables */
    private Vector<ListenObj> streamVars = new Vector<ListenObj>();
    private Hashtable<String, Object> pushVars = new Hashtable<String, Object>();
	
 
    /* Constructor - creates connection */
	public SMRobject(String name, List<Server> server_in) {
		this.connname = name;
		
		for (int index = 0; index < server_in.size();index++) {
			if(!servers.containsKey(server_in.get(index).getServerName())) {
				if(server_in.get(index).initConn()) {
					servers.put(server_in.get(index).getServerName(),server_in.get(index));
				}
			}
		}
		cmdqueue.clear();
		cmdqueue.add(emptyQueueObj);
		this.running = true;
		new Thread(this).start();
	}
	
	
	/**
	 * Method which returns the host name associated with the specific SMR to which the SMRobject has been
	 * linked. The host name is returned in sting format.
	 * 
	 * @return String  The returned host name in string format.
	 */
    public String getHostname() {
    	for(java.util.Enumeration<String> e = this.servers.keys(); e.hasMoreElements();) {
	    	Object key = e.nextElement();
	    	Server host = (Server)servers.get(key);
			return host.getHost();
    	}
    	return "UNKNOWN";
    }
    
    public Server getServerByHandle(String name) {
    	return servers.get(name);
    }
	
    
	/**
	 * Method for correctly disconnecting the SMR, closing all data readers and writers and resetting the
	 * communication ports after which the SMRobject is removed from the hash table.
	 * 
	 * @return boolean  Returns a true flag.
	 * 
	 * @throws IOException  Exception thrown if the connection close procedures fail.
	 */
    public boolean disconnect(Context c) {
    	Rete engine = c.getEngine();
    	this.running = false;
    	if (this.debug) System.out.println("Disconnecting");
    	try {
    		for (Enumeration<Object> e = pushVars.elements(); e.hasMoreElements();) {
				BeanSupport b = (BeanSupport)e.nextElement();                        // Pick up the next bean object element
				engine.undefinstance(b);
			}
    		for (Enumeration<ListenObj> e = streamVars.elements(); e.hasMoreElements();) {
				ListenObj l   = (ListenObj)e.nextElement();              // Pick up the next listen opbject element
				BeanSupport b = (BeanSupport)l.bean;                     // Pick up the next bean object element
				engine.undefinstance(b);
			}
			for (Enumeration<SocketStatus>e  = sockStatus.elements(); e.hasMoreElements();) {
				SocketStatus ss = e.nextElement();
				engine.undefinstance(ss);
			}
			for (java.util.Enumeration<String> e = servers.keys(); e.hasMoreElements();) {
				Object key    = e.nextElement();
				Server host = (Server)servers.get(key);
				host.disConn();
			}
			engine.undefinstance(sleepTimer);
			engine.undefinstance(cmd_listener);
			engine.undefinstance(queue_listener);
		} catch (JessException e1) {
			e1.printStackTrace();
		}
		SMRcomm.removeFromList(connname);
		connname = null;
		return true;
    }
	
    private int parseID(String st) {
		if (!st.startsWith("ID")) return -1;
		String idst = st.substring(2);
		int i;
		try {
			i = java.lang.Integer.parseInt(idst);
		} catch (NumberFormatException e) {
			return -1;
		}
		return i;
    }
	
    private int setCommandQueue(String cmd, int id, int stat, int sc) {
		if (id < cmdqueue.size()) {
			CmdQueueObj o = (CmdQueueObj)cmdqueue.get(id);
			if (cmd != null) o.cmdstr = cmd;
			o.status   = stat;
			o.stopcond = sc;
		} else {
			cmdqueue.setSize(id + 1);
			cmdqueue.setElementAt(new CmdQueueObj(cmd, stat, sc), id);
		}
				
		return id;
    }
    
    private void placeInCommandQueue(String cmd) {
    	cmdqueue.setElementAt(new CmdQueueObj(cmd, CurrentCommand.CMD_CHANGING, 0), 0);
    }
    
    private int updateCommandQueue(String cmd, int id, int stat, int sc) {
		CmdQueueObj o = (CmdQueueObj)cmdqueue.get(0);
		o.status = stat;
		cmdqueue.setSize(id + 1);
		cmdqueue.setElementAt(o, id);
		cmdqueue.setElementAt(emptyQueueObj, 0);
				
		return id;
    }
    
    private int getCommandStatus(int id) {
    	if (id < cmdqueue.size()) {
    		CmdQueueObj o = (CmdQueueObj)cmdqueue.get(id);
    		return o.status;
    	} else {
    		return -1;
    	}
    }
	
    private void notifyQueueCommand(int id) {
		if (queue_listener != null) {
			CmdQueueObj o = (CmdQueueObj)cmdqueue.get(id);
			queue_listener.changeQueue(o.cmdstr, id, o.status, o.stopcond);
		}
		
    }
	
    private void notifyCurrentCommand(int id) {
		if (cmd_listener != null) {
			CmdQueueObj o = (CmdQueueObj)cmdqueue.get(id);
			cmd_listener.changeCommand(o.cmdstr, id, o.status, o.stopcond);
		}
		
    }
	
    /**
	 * Method which sends the cmd to the PushStream socket of the supplied server
	 * If used with this.debug=true, it reads the pushIn stream, 
	 * thereby maybe emptying data that pushVars hasn't read
	 * 
	 *  *Note* if this.debug==true, this method empties the pushin buffer, thus maybe
	 *  preventing a single pushvar update.
	 * @param String   Command string to send to the vision server.
	 * @param String   Key to servers Hashtable
	 * 
	 */
	public synchronized void sendPushCommand(String cmd, String server) throws JessException {
		if (!this.servers.containsKey(server)) throw new JessException("sendPushCommand", "Server \""+server+"\" not found", 0);
		
		Server host = this.servers.get(server);
		if (host.getPushConn().isClosed()) throw new JessException("sendPushCommand", "Not connected to" + host.getServerName(), 0);
			
		try {
			host.pushOut.writeBytes(cmd.trim() + "\n");
			if (this.debug) {
				System.out.println("Sending push command: " + cmd);
				String retstr = "";
				Thread.sleep(250);
				while(host.pushIn.ready()) {
					retstr += host.pushIn.readLine();
					retstr += "\n";
				}
				System.out.print(retstr);
			}
			
		} catch (IOException e) {
			if (this.debug) System.out.println("IE Error in sendPushCommand - Is the socket to " + host.getServerName() + "closed?");
			host.disConn();
		} catch (NullPointerException e) {
			if (this.debug) System.out.println("Null Pointer error in sendPushCommand - Is the socket to " + host.getServerName() + "closed?");
			host.disConn();
		} catch (InterruptedException e) {
		}
		
			
	}
	
	/**
	 * Method which empties the UserStream input socket of the supplied server 
	 * 
	 * @param String   Key to servers Hashtable
	 * 
	 */
	public void flushBuffer(String server) throws JessException {
		if (!this.servers.containsKey(server)) return;
		Server host = this.servers.get(server);
		if (host.getConn().isClosed()) {
			throw new JessException("sendCommand", "Not connected to" + host.getServerName(), 0);
		}
		try {
			while(host.datain.ready()) host.datain.readLine();
		} catch (IOException e) { }
	}
	
	/**
	 * Method which sends the cmd to the UserStream socket of the supplied server 
	 * and returns the answer
	 * 
	 * @param String   Command string to send to the vision server.
	 * @param String   Key to servers Hashtable
	 * 
	 * @return String  Response string from the server.
	 * 
	 */
	public synchronized String sendCommand(String cmd, String server) throws JessException {
		String retstr    = "";
		if (!this.servers.containsKey(server)) return retstr;
		Server host = this.servers.get(server);
		try {
			if (host.getServerName() == "mrc") {
				return SendMRCCommand(cmd, host);
			} else {
				
				if (host.getConn().isClosed()) {
					throw new JessException("sendCommand", "Not connected to" + host.getServerName(), 0);
				}
				
				host.dataout.writeBytes(cmd.trim() + "\n");
				long timerWait = System.currentTimeMillis();
				while(!host.datain.ready()) {
					Thread.sleep(50);
					if(System.currentTimeMillis() > timerWait + 500) {
						break;
					}
				}
				while(host.datain.ready()) {
					retstr += host.datain.readLine();
					if (host.datain.ready()) {
						retstr += "\n";
					}
				}
				if (this.debug) System.out.println("Sending command: " + cmd);
			}
		} catch (IOException e) {
			if (this.debug) System.out.println("IE Error in sendCommand - Is the socket to " + host.getServerName() + "closed?");
			host.disConn();
		} catch (NullPointerException e) {
			if (this.debug) System.out.println("Null Pointer Error in sendCommand - Is the socket to " + host.getServerName() + "closed?");
			host.disConn();
		} catch (InterruptedException e) {
			if (this.debug) System.out.println("InterruptedExecution in sendCommand while waiting for data");
		} 		
		return retstr;
	}
	
	/**
	 * Method which handles communication to and from the MRC module of the SMR. Communication is done in
	 * SMR-CL format. The method is routinely called with eval calls for the odometry, distance sensor and
	 * linesensor JavaBean values, but can also be called from the Jess side using the SMRtalk function.
	 * 
	 * @param String  Command string to send to the MRC in SMR-CL format.
	 * 
	 * @return String  Response string which is collected from the MRC after a command call.
	 * 
	 * @throws JessException         Exception thrown if the connection is null, thus that the
	 *                               communication is not enabled.
	 * @throws IOException           Exception thrown if the communication with the MRC fails.
	 * @throws NullPointerException  Exception thrown if a null pointer error is encountered during the
	 *                               read and/or write phase.
	 */
    public synchronized String SendMRCCommand(String cmd, Server host) throws JessException {
		if (host == null) {
			if (this.servers.containsKey("mrc")) {
				host = this.servers.get("mrc");
			} else {
				throw new JessException("sendMRCCommand", "No known mrc servers", 0);
			}
		}
		
		try {
			if (host.getConn().isClosed()) {
				throw new JessException("sendMRCCommand", "Not connected", 0);
			}
			
			host.dataout.writeBytes(cmd + "\n");
			if (cmdqueue.get(0) != emptyQueueObj && this.debug) {
				System.out.println("Warning, possible queue desyncronization");
			}
			if ((!cmd.toLowerCase().split("\\s")[0].contains("plan") || cmd.toLowerCase().startsWith("runplan")) //don't add beginplan, plan or endplan
					&& !cmd.equals("getevent")
					&& !cmd.toLowerCase().equals("flushcmds")
					&& !cmd.toLowerCase().startsWith("eval")
					&& !cmd.toLowerCase().equals("")) { 
				placeInCommandQueue(cmd);
			}
	    } catch (IOException e) {
			host.disConn();
			if (this.debug) System.out.println("IO Error in sendMRCCommand, closing Socket - Is MRC crashed?");
	    }
		return getMRCreply(host, cmd);
    }
    
    public synchronized String getMRCreply(Server host,String cmd) throws JessException {
    	if (host == null) {
    		if (this.servers.containsKey("mrc")) {
    			host = this.servers.get("mrc");
    		} else {
    			throw new JessException("sendMRCCommand", "No known mrc servers", 0);
    		}
    	}
		String retstr = "";
		
		try {
			if (host.getConn().isClosed()) {
				throw new JessException("sendMRCCommand", "Not connected", 0);
			}
			
			//if (host.datain.ready()) {
				retstr = host.datain.readLine();
				
				String tokens[] = retstr.split("\\s");
				if (tokens.length >= 2) {
					String a = tokens[1];
					if (a.compareTo("queued") == 0) {
						
						queue_id = updateCommandQueue(null, parseID(tokens[0]), CurrentCommand.CMD_QUEUED, 0);
						notifyQueueCommand(queue_id);
					}					
					else if (a.compareTo("started") == 0) {						
						curcmd_id = setCommandQueue(null, parseID(tokens[0]), CurrentCommand.CMD_STARTED, 0);
						notifyCurrentCommand(curcmd_id);
						if (curcmd_id == queue_id) notifyQueueCommand(queue_id);
						if(!cmd.startsWith("getevent")) retstr = getMRCreply(host, cmd);
					} 
					else if (a.compareTo("stopcond") == 0) {						
						int sc = -1, cid;
						if (tokens.length >= 3) {
							sc = (int)java.lang.Double.parseDouble(tokens[2]);
						}
						cid = setCommandQueue(null, parseID(tokens[0]), CurrentCommand.CMD_DONE, sc);
						if (cid == curcmd_id) notifyCurrentCommand(curcmd_id);
						if (cid == queue_id) notifyQueueCommand(queue_id);
						if(!cmd.startsWith("getevent")) retstr = getMRCreply(host, cmd);
					} 
					else if (a.compareTo("syntaxerror") == 0) {						
						curcmd_id = setCommandQueue(null, parseID(tokens[0]), CurrentCommand.CMD_SYNTAXERROR, 0);
						notifyCurrentCommand(curcmd_id);
						if (curcmd_id == queue_id) notifyQueueCommand(queue_id);
					} 
					else if (a.compareTo("assignment") == 0) {						
						int cid;
						cid = setCommandQueue(null, parseID(tokens[0]), CurrentCommand.CMD_DONE, 0);
						if (cid == curcmd_id) notifyCurrentCommand(curcmd_id);
						if (cid == queue_id) notifyQueueCommand(queue_id);
						if(!cmd.startsWith("getevent")) retstr = getMRCreply(host, cmd);
					}
					else if (a.compareTo("flushed") == 0) {
						curcmd_id = setCommandQueue(null, parseID(tokens[0]), CurrentCommand.CMD_FLUSHED, 0);
						notifyCurrentCommand(curcmd_id);
						if (queue_id != curcmd_id) {
							setCommandQueue(null, queue_id, CurrentCommand.CMD_FLUSHED, 0);
						}
						notifyQueueCommand(queue_id);
					}
					else if (retstr.compareTo("motioncontrol status changed") == 0 && 
							getCommandStatus(curcmd_id) != CurrentCommand.CMD_DONE) {
						host.dataout.writeBytes("eval $motionstatus\n");
						waiting_for_motion_status = true;
						if(!cmd.startsWith("getevent"))	retstr = getMRCreply(host, cmd);
					}
					else if (a.compareTo("eventtimeout") == 0) {
						if(!cmd.startsWith("getevent"))	retstr = getMRCreply(host, cmd);
					}
					else if (tokens[0].compareTo("stream") == 0) {
						this.debug = false;
						long tod;
						if (this.debug) System.out.println("getMRCreply got: " + retstr);
						try {
							tod = (long)(java.lang.Double.parseDouble(tokens[1]) * 1000.0);
						} catch (NumberFormatException e) {
							tod = System.currentTimeMillis();
						}
						int index = 2;
						for (Enumeration<ListenObj> e = streamVars.elements(); e.hasMoreElements();) {
							ListenObj l   = (ListenObj)e.nextElement();              // Pick up the next listen object element
							BeanSupport b = (BeanSupport)l.bean;                     // Pick up the next bean object element
							String elements = "";
							for (int i = 0;i < l.watches.length; i++) {
								try {
									elements += tokens[index + i] + " ";
								} catch (ArrayIndexOutOfBoundsException ai) {
									break;
								}
							}
							elements = elements.trim();
							index += l.watches.length;
							if (this.debug) {
								String conWatches = "";
								for (int i=0; i < l.watches.length; i++) {
									conWatches +=l.watches[i] + " ";
								}
								System.out.println("Updating " + conWatches + " with values " + elements);
							}
							b.updateStringValues(elements, tod);
						}
						if (this.debug) System.out.println("");
						this.debug  =false;
						if (! cmd.startsWith("stream")) retstr = getMRCreply(host, cmd);
					}
				} else { //only recieved a one-word reply, probably response from an (user-generated) eval, or a plan-command
					if (retstr.compareTo("eventtimeout") == 0) {
						if(!cmd.startsWith("getevent")) retstr = getMRCreply(host, cmd);
					} else if (waiting_for_motion_status) {
						try {
							int sta = (int)java.lang.Double.parseDouble(retstr);
							if (sta != 0) { // Obstacle detected
								setCommandQueue(null, curcmd_id, CurrentCommand.CMD_SUSPENDED, 0);
								notifyCurrentCommand(curcmd_id);
								if (curcmd_id == queue_id) notifyQueueCommand(queue_id);
							} else { // Obstacle removed
								setCommandQueue(null, curcmd_id, CurrentCommand.CMD_RESTARTED, 0);
								notifyCurrentCommand(curcmd_id);
								if (curcmd_id == queue_id) notifyQueueCommand(queue_id);
							}
							waiting_for_motion_status = false;
							if(!cmd.startsWith("getevent")) retstr = getMRCreply(host, cmd);
						} catch (NumberFormatException e) {
							;
						}
					}
				}
			//}
		} catch (IOException e) {
			host.disConn();
			if (this.debug) System.out.println("IO Error in getMRCreply, closing Socket - Is MRC crashed?");
		}
		return retstr;
    }

	
	/**
	 * Method which sets up the listen object for the variables streamed by mrc
	 * 
	 * @param Object  Object pointer to the specific JavaBean object of  which to add the variables to
	 *                monitor.
	 * @param String  String of variables to monitor, made ready in the SMR-CL variable format to be ready
	 *                to send to the MRC.
	 * @param double  Double that specifies the desired streaming time
	 *                to send to the MRC.
	 */
   
    public boolean addStreamvars(String varName, Object o, double time) {
		if (this.debug) 
			System.out.println("Adding var " + varName + " (" + time + ")to streamvar vector");
		String watches[] = varName.split(" ");
		if (time >= 0.01)
			this.streamTime = ((int)(time*100));
		String evalCmd = "eval ";
		String streamCmd = "stream " + this.streamTime;
		for (int i=0;i < watches.length;i++) {
			evalCmd = evalCmd + watches[i] + "; ";
			streamCmd = streamCmd + " \"" + watches[i] + "\"";
		}
		evalCmd = evalCmd.substring(0,evalCmd.length()-2); //remove last semicolon
		try {
			if (this.debug) 
				System.out.println("Checking with eval: " + evalCmd);
			java.lang.Double.parseDouble(SendMRCCommand(evalCmd, null).split(" ")[0]); //if unknown mrc variable, mrc returns "syntaxerror", which throws an NumberFormatException, else a list of numbers
			SendMRCCommand(streamCmd, null);
		} catch (JessException je) {
			return false;
		} catch (NumberFormatException nf) {
			return false;
		}
		streamVars.add(new ListenObj(o, watches));
		return true;
    }
	/**
	 * Method which sets up the listen object for the generic variables.
	 * 
	 * @param Object  Object pointer to the specific JavaBean object of which to add the variables to
	 *                monitor.
	 * @param String  String of variables to monitor, made ready in the format necessary to call to the
	 *                vision server.
	 */
	public boolean addGeneric(String varName, Object o) {
		if (this.debug) System.out.println("Adding var " + varName + " to generic hashtable");
		if (pushVars.containsKey(varName.toLowerCase())) return false;
		pushVars.put(varName.toLowerCase(),o);
		return true;
    }
	
	
	/**
	 * Method which initializes the CurrentCommand JavaBean monitoring, setting up the listener and initial
	 * values using internal private methods.
	 * 
	 * @param CurrentCommand  Object pointer to the specific JavaBean object (CurrentCommand) which to keep
	 *                        updated.
	 */
    public void setCommandListener(CurrentCommand cc) {
		cmd_listener = cc;
		notifyCurrentCommand(curcmd_id);
    }
	
	/**
	 * Method which initializes the CommandQueue JavaBean monitoring, setting up the listener and initial
	 * values using internal private methods.
	 * 
	 * @param CommandQueue  Object pointer to the specific JavaBean object (CommandQueue) which to keep
	 *                      updated.
	 */
    public void setQueueListener(CommandQueue cc) {
		queue_listener = cc;
		notifyQueueCommand(queue_id);
    }
	
    private void pollEvents() {
		try {
			for (int i = 0; (i < 20); i++) {
				if (this.SendMRCCommand("getevent", servers.get("mrc")).compareTo("eventtimeout") == 0) {
					break;
				}
			}
		} catch (JessException je) {
			return;
		} catch (NullPointerException e) { }
    }
	
    
    /**
	 * Method which handles the updating of all the JavaBeans, excluding MRC Command.
	 * Update of the vision and laserscanner JavaBeans are done using each
	 * their respective send method, communicating directly with the vision and laser servers on the SMR.
	 * 
	 * @throws JessException         Exception thrown if a Jess related error is encountered during the
	 *                               communication or value update phase.
	 * @throws NullPointerException  Exception thrown if a null pointer error is encountered during the
	 *                               read and/or write phase.
	 */
    private void updatePushVars() {
    	String tempBuf = "";
    	for (java.util.Enumeration<Server> s = this.servers.elements(); s.hasMoreElements();) {
    		Server host = s.nextElement();
    		if (host.getServerName() == "mrc") continue; //mrc dont push vars
    		try {
    			while(host.pushIn.ready()) {
    				tempBuf = host.pushIn.readLine().replace('"','\'');
    				
    				if(tempBuf.toLowerCase().startsWith("<var name")) { //i.e. input <var name='gmk.IDs' typ='d' size='1' value='0'/>
    					
    					if (this.debug) System.out.println("Read \"" + tempBuf + "\" from " + host.getServerName());
        				String reply[] = tempBuf.split("name='"); // reply[1] = gmk.IDs' typ='d' size='1' value='0'/>
    					String factName = reply[1].split("'")[0].toLowerCase(); // factName = gmk.IDs
    					if (pushVars.containsKey(factName)) {
    						BeanSupport var =  (BeanSupport) pushVars.get(factName);
    						String vala = reply[1].split("value='")[1]; // vala = 0'/> 
    						String val = vala.split("'")[0]; // val = 0
    						var.updateStringValues(val, System.currentTimeMillis());
    					} else {
    						if (this.debug) System.out.println(factName + " could not be found in hashtable, fact not updated");
    					}
    				}
    			}
	    			
    		} catch(IOException ioe) {
    			if (this.debug) System.out.println("IO Error in updatePushVar() - Was socket closed during read?");
    		} catch(NullPointerException npe) {
    			if (this.debug) System.out.println("Null Pointer Error in updatePushVar() - Was socket closed during read?");
    		}
    	}
    	
    }
    
    /* This thread calls updateVariables periodically */
	/**
	 * Periodic run method which constantly loops, taking care of the periodic updating of values. Every
	 * loop the events are polled, values are updated.
	 * 
	 * @throws InterruptedException  Exception thrown if an interruption is encountered while attempting to
	 *                               execute a sleep before initiating another loop.
	 */
    public void run() { 
    	long t_start; 
    	int dt;
    	int timeout;
		while (this.running) {
			t_start = System.currentTimeMillis();
			this.pollEvents();
			this.updatePushVars();
			for (Enumeration<SocketStatus> e = sockStatus.elements(); e.hasMoreElements();) {
				SocketStatus ss = e.nextElement();                        // Pick up the next bean object element
				ss.updateValues();
			}
    		timeout = sleepTimer.getDesiredTime_ms();
    		this.streamTime = timeout / 10;
			dt = (int) (System.currentTimeMillis() - t_start);
			while (dt < timeout) {
				this.updatePushVars();
				dt = (int) (System.currentTimeMillis() - t_start);
			}
			sleepTimer.updateValues();
		}
    }
	
    /**
	 * Initializes push commands to the servers and modules that needs it
	 * @param structs			List of VariableStructs with relevent info
	 * 
	 * @throws JessException	Thrown from sendPushCommand() 
	 */
	public void initPush(List<VariableStruct> structs) throws JessException {
		String cmd = null;
		int timeGone = 0;
		for (int index = 0; index < structs.size();index++) {
			// push the struct on the server to update the var pool
			if ((structs.get(index).getInitialized() == false) && (structs.get(index).getHost().getConnected())) {
				timeGone = (int) (System.currentTimeMillis() - structs.get(index).getHost().getConnectedAtTime()); 
				if (timeGone < 3000) {
					try {
						//Server will reject push commands send by a client within 3 seconds after connecting. Ask JCA
						Thread.sleep(3000-timeGone); 
					} catch (InterruptedException ie) {
						ie.printStackTrace();
					}
				}
				
				cmd = "push cmd='" + structs.get(index).getCmd() + "' t=" + structs.get(index).getTime() ;
				if (this.debug) System.out.println("Sending " + cmd + " to " + structs.get(index).getHost().getServerName() + " in SMRobject.initPush");
				
				this.sendPushCommand(cmd, structs.get(index).getHost().getServerName());
				
				structs.get(index).setInitialized(true);
			}
		}
	}
	/**
	 * Sends pushvar commands to the servers and modules that needs it to enable pushing
	 * @param facts				List of XmlFact with relevent info
	 * @throws JessException	Thrown if SMR is not found.
	 */
	public void initFacts(List<XmlFact> facts) throws JessException {
		String cmd = null;
		for (int index = 0; index < facts.size();index++) {
			// push the varstruct back to jess
			if (facts.get(index).getInitialized() == false) {
				if (facts.get(index).getStruct().getHost().getServerName() == "mrc") {
					cmd = "";
				} else {
					cmd = "varpush cmd='var " + facts.get(index).getStruct().getStructName() + "." + facts.get(index).getVariable() +
							 " copy' struct=" + facts.get(index).getStruct().getStructName();
		
					this.sendPushCommand(cmd, facts.get(index).getStruct().getHost().getServerName());
				}
				if (this.debug) System.out.println("Sending " + cmd + " to " + facts.get(index).getStruct().getHost().getServerName() + " in SMRobject.initFacts");
				facts.get(index).setInitialized(true);
			}
		}
	}

	/**
	 * Prints out the connections for the current SMRobject
	 */
	public void listConnections() {
		for (java.util.Enumeration<String> e = servers.keys(); e.hasMoreElements();) {
			Object key    = e.nextElement();
			Server host = (Server)servers.get(key);
			System.out.print("\t|- " + host.getServerName());
			if (host.getConnected())
				System.out.println(" (Connected)");
			else
				System.out.println(" (Not connected)");
		}
    }
	
	public boolean retractPushvar(String key) throws JessException {
		if (pushVars.containsKey(key)) {
			BeanSupport var =  (BeanSupport) pushVars.get(key);
			String cmd[] = var.remove().split("\n");
			for (int index = 0; index < cmd.length-1; index++)
				this.sendPushCommand(cmd[index], cmd[cmd.length-1]);
			pushVars.remove(key);
			return true;
		}
		else
			return false;
	}
	
}