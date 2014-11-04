package jessmw;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.net.UnknownHostException;

public class Server {
    private String serverName = null; //instance-wide unique name for server
    private String host = null; // tcp host name
    private int port = 0; // tcp port number
    private boolean connected = false; // are we connected
    
    private Socket conn = null; // socket for sending and receiving commands
    public DataOutputStream dataout = null; // conn output
    public BufferedReader datain = null; // conn input
    /*
     * a push command sends a lot of info back.
     * we can't determine what is a message and reply from a user command
     * and what is from a previously push command without reading the socket
     * so we have a separate socket for the push streams.  
     */
    private Boolean pushServer = true; // should we have a separate channel (probably yes, unless server is mrc)
    private Socket pushConn = null; // socket for push commands
    public DataOutputStream pushOut = null; // pushConn output
    public BufferedReader pushIn = null; // pushConn input 
    private double alive = 0.0; // reply from an alive-command
    
    private long connectedAtTime = 0;
    
    /* Class Constructors */
    public Server() {
		
    }
    public String toString() {
    	StringBuffer sb = new StringBuffer();
    	sb.append("Server details - ");
    	sb.append("Server entity " + getServerName());
		sb.append(" resides on " + getHost());
    	sb.append(":" + getPort());
    	sb.append(" (connected status " +  getConnected().toString());
    	sb.append(")\n");   
    	return sb.toString();
    }
    
    /** getters and setters **/
    
    public String getServerName() {
    	return serverName;
    }
    public void setServerName(String inp) {
    	serverName = inp;
    }
    public String getHost() {
    	return host;
    }
    public void setHost(String inp) {
    	host = inp;
    }
    
    public int getPort() {
    	return port;
    }
    public void setPort(int inp) {
    	port = inp;
    }
    public Boolean getConnected() {
    	return connected;
    }
    public void setConnected(boolean inp) {
    	connected = inp;
    }
    public double getAlive() {
    	return this.alive;
    }
    public boolean updateConnected(String reply) {
    	// It can't be detected if the other end of a tcp connection has dropped without 
    	// sending a command, so this method is called with the reply from a sendCommand("alive",servername),
    	// if an IOExeption occurred, this.disConn would be called before this method.
    	// The command is not sent directly from this class to avoid having to handle timeout.
    	// maybe a method in this class should handle that instead of the method in SMRobject
      	connected = true;
      	try {
			if (this.conn.isClosed())
				connected = false;
			else {
				if (reply.startsWith("ID")) {
					//reply from MRC: set alive to 0.0, i.e. don't care
					this.alive = 0.0;
				} else if (reply.startsWith("<alive")) {
					// we got a valid response
					String[] t = reply.split("last=\"");
					String tt = t[1].split("\"/>")[0];
					this.alive = java.lang.Double.parseDouble(tt);
				}
			}
			if (this.getPushServer() && this.pushConn.isClosed())
				connected = false;
      	} catch(NullPointerException npe) {
      		// conn or Pushconn was set to null by disConn()
      		connected = false;
      		this.disConn();
      	}
    	return connected;
    }
	public Socket getConn() {
		return conn;
	}
	public boolean initConn() {
		try {
			this.conn = new Socket(this.host, this.port);
			this.dataout = new DataOutputStream(this.conn.getOutputStream());
			this.datain = new BufferedReader(new InputStreamReader(this.conn.getInputStream()));
			if(this.getServerName() != "mrc") {
				// send xml header to UARS
				this.dataout.writeBytes("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
				this.dataout.writeBytes("<Jess name=\"UserStream\" version=\"2.0\">\n");
				
			}
			if (this.getPushServer()) {
				// send xml header to UARS
				this.pushConn = new Socket(this.host, this.port);
				this.pushOut = new DataOutputStream(this.pushConn.getOutputStream());
				this.pushIn = new BufferedReader(new InputStreamReader(this.pushConn.getInputStream()));
				this.pushOut.writeBytes("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
				this.pushOut.writeBytes("<Jess name=\"PushStream\" version=\"2.0\">\n");
			}
			this.connected = true;
			this.connectedAtTime = System.currentTimeMillis();
			// remove xml header sent from AURS from buffer
			// testserver don't send response, so do timeout
			if(this.getServerName() != "mrc") {
				while((System.currentTimeMillis() < this.connectedAtTime+1000) && (!this.datain.ready())) ;
				if (this.datain.ready()) this.datain.readLine();
				while((System.currentTimeMillis() < this.connectedAtTime+1000) && (!this.datain.ready())) ;
				if (this.datain.ready()) this.datain.readLine();
				if (this.getPushServer()) {
					while((System.currentTimeMillis() < this.connectedAtTime+1000) && (!this.pushIn.ready())) ;
					if (this.pushIn.ready()) this.pushIn.readLine();
					while((System.currentTimeMillis() < this.connectedAtTime+1000) && (!this.pushIn.ready())) ;
					if (this.pushIn.ready()) this.pushIn.readLine();
				}
			} else {
				while(System.currentTimeMillis() < this.connectedAtTime+1000) {
					; //wait for mrc to be ready
				}
			}
		} catch (UnknownHostException e) {
			System.out.println("Error: Unknown host " + this.host);
			this.connected = false;
		} catch (IOException e) {
			System.out.println("Error: I/O error connecting to host " + this.host);
			this.connected = false;
		} finally {
		}
		return this.connected;
	}
	public boolean disConn() {
		try {
			if (this.dataout != null) {
				// close connection gracefully
				if (this.serverName != "mrc") {
					this.dataout.writeBytes("</Jess>\n");				
					this.dataout.writeBytes("hup\n");
				} else {
					this.dataout.writeBytes("exit\n");
				}
				this.dataout.close();
			}
			if (this.datain != null) {
				while(this.datain.ready()) {
					this.datain.read();
				}
				this.datain.close();
			}
			if (this.conn != null) this.conn.close();
			
			if (this.pushOut != null) {
				this.pushOut.writeBytes("</Jess>\n");
				this.pushOut.writeBytes("hup\n");
				this.pushOut.close();
			}
			if (this.pushIn != null) this.pushIn.close();
			if (this.pushConn != null) this.pushConn.close();
		} catch (IOException e) {
		} finally {
			this.dataout  = null;
			this.datain   = null;
			this.conn     = null;
			this.pushOut  = null;
			this.pushIn   = null;
			this.pushConn = null;
			this.connected = false;
		}
		return true;
	}
	public Boolean getPushServer() {
		return pushServer;
	}
	public void setPushServer(Boolean pushServer) {
		this.pushServer = pushServer;
	}
	public Socket getPushConn() {
		return pushConn;
	}
	public long getConnectedAtTime() {
		return connectedAtTime;
	}
}