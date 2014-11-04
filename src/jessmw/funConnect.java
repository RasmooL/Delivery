package jessmw;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import jess.*;


public class funConnect implements Userfunction {
    private static final String functionName = "SMRConnect";
	
    private List<XmlFact> xmlFacts;
	private List<VariableStruct> structs;
	private List<Server> servers;
	private Document dom;
	private String connName;
	private String defaultHost;
	private int timeout = 500;
	
	private boolean debug = false;
    
    public String getName() {
		return functionName;
    }
	
    public Value call(ValueVector vv, Context c) throws JessException {
    	// XML parsing: http://www.java-samples.com/showtutorial.php?tutorialid=152
		String filename = null;
		String retval   = null;
		
		if (vv.size() == 3) {
			if (vv.get(2).stringValue(c).toLowerCase() == "debug") {
				this.debug=true;
			} else {
				System.out.println("Ignoring second argument " + vv.get(2).stringValue(c).toLowerCase() + 
						"(Try using \"debug\" instead of debug)");
			}
		}
		if ((vv.size() < 2) || (vv.size() > 3)) throw new JessException(functionName,"Invalid number of arguments",vv.size() - 1);
		
		//init vars
		filename = vv.get(1).stringValue(c);
		this.xmlFacts = new ArrayList<XmlFact>();
		this.structs = new ArrayList<VariableStruct>();
		this.servers = new ArrayList<Server>();
		this.connName = null;
		this.defaultHost = null;
		
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		
		try {
			DocumentBuilder db = dbf.newDocumentBuilder();
			
			this.dom = db.parse(filename);
			
		} catch(ParserConfigurationException pce) {
			pce.printStackTrace();
		}catch(SAXException se) {
			se.printStackTrace();
		}catch(IOException ioe) {
			ioe.printStackTrace();
		}
		// read xml file and populate lists
		parseDocument();
		
		if (this.connName != null) {
			if (this.debug) System.out.println("Connecting to " + this.connName + " with " + this.servers.size() + " server(s)");
			// connect to server and construct SMRobject
			SMRcomm.connect(this.connName, this.servers);
			
			// assert the shadow facts
			assertFacts(c);
			// setup varpush on the servers
			SMRcomm.initFacts(this.xmlFacts, this.connName);
			// setup push on the servers
			SMRcomm.initStrucks(this.structs, this.connName);
			retval = this.connName;
		}
		
		
		if (this.debug) System.out.println("Number of facts read:" + this.xmlFacts.size());
		
		return (retval == null?Funcall.FALSE:new Value(retval,RU.STRING));
    }
    
    private void assertFacts(Context c) {
    	Rete engine = c.getEngine();
    	try {
    		// always make an updateInterval for the instance
    		engine.defclass("updateInterval", "jessmw.UpdateInterval", null); //define the class
    		UpdateInterval sleepTimer = new UpdateInterval(this.connName, this.timeout); //call constructor
    		engine.definstance("updateInterval", sleepTimer, true, c); // define the actual shadow fact
    		
        	for(int i=0; i < this.servers.size(); i++) {
        		//make a status fact for each server
        		engine.defclass("status", "jessmw.SocketStatus", null);
        		SocketStatus ss = new SocketStatus(this.connName, servers.get(i)); //call constructor with each server 
        		engine.definstance("status", ss, true,c);
	       	}
    	} catch (JessException je) {System.out.println(je.getMessage() + "\n" + je.getDetail()); }
    	
    	for(int i=0; i < this.xmlFacts.size(); i++) {
    		if (this.debug) System.out.println("Asserting " + this.xmlFacts.get(i).toString());
    		if (! this.xmlFacts.get(i).getStruct().getHost().getConnected()) {
    			// don't instantiate the shadow fact if connection to the server failed
    			System.out.println(this.xmlFacts.get(i).getStruct().getHost().getServerName() + 
    					" is not connected, removing fact " + this.xmlFacts.get(i).getFactName());
    			// set flag so varpush is not attempted
    			this.xmlFacts.get(i).setInitialized(true);
    			continue;
    		}
    		if (this.xmlFacts.get(i).getStruct().getHost().getServerName() == "mrc") {
    			this.xmlFacts.get(i).setInitialized(true); // mrc facts don't use pushvars
    			if(this.xmlFacts.get(i).getVariable().toLowerCase().equals("currentcommand")) {
    				try {
    					engine.defclass(this.xmlFacts.get(i).getFactName(), "jessmw.CurrentCommand", null);
    					/* Create instances of JavaBeans (shadow-facts) */
    					CurrentCommand cc = new CurrentCommand(this.connName);
    					engine.definstance(this.xmlFacts.get(i).getFactName(),cc,true, c);
    					continue;
    				} catch (JessException je) {System.out.println(je.getDetail()); }
    			} else if(this.xmlFacts.get(i).getVariable().toLowerCase().equals("commandqueue")) {
    				try {
    					engine.defclass(this.xmlFacts.get(i).getFactName(), "jessmw.CommandQueue", null);
    					/* Create instances of JavaBeans (shadow-facts) */
    					CommandQueue cq = new CommandQueue(this.connName);
    					engine.definstance(this.xmlFacts.get(i).getFactName(),cq,true, c);
    					continue;
    				} catch (JessException je) {System.out.println(je.getDetail()); }
    			} else if(this.xmlFacts.get(i).getVariable().toLowerCase().equals("odometry")) {
    				try {
    					engine.defclass(this.xmlFacts.get(i).getFactName(), "jessmw.Odometry", null);
    					/* Create instances of JavaBeans (shadow-facts) */
    					Odometry odo = new Odometry(this.connName);
    					engine.definstance(this.xmlFacts.get(i).getFactName(),odo,true, c);
    					continue;
    				} catch (JessException je) {System.out.println(je.getDetail()); }
     			} else if(this.xmlFacts.get(i).getVariable().toLowerCase().equals("distsensor")) {
    				try {
    					engine.defclass(this.xmlFacts.get(i).getFactName(), "jessmw.DistSensor", null);
    					/* Create instances of JavaBeans (shadow-facts) */
    					DistSensor ds = new DistSensor(this.connName);
    					engine.definstance(this.xmlFacts.get(i).getFactName(),ds,true, c);
    					continue;
    				} catch (JessException je) {System.out.println(je.getDetail()); }
      			} else if((this.xmlFacts.get(i).getVariable().toLowerCase().equals("linesensor"))) {
    				try {
    					engine.defclass(this.xmlFacts.get(i).getFactName(), "jessmw.LineSensor", null);
    					/* Create instances of JavaBeans (shadow-facts) */
    					LineSensor ls = new LineSensor(this.connName);
    					engine.definstance(this.xmlFacts.get(i).getFactName(),ls,true, c);
    					continue;
    				} catch (JessException je) {System.out.println(je.getDetail()); }
      			} 
    	 		
    		}
    		try {
				if(this.xmlFacts.get(i).getType().toLowerCase().contains("string")) {
					try {
	    				engine.defclass(this.xmlFacts.get(i).getFactName(), "jessmw.GenericStringVar", null);
	    				engine.definstance(this.xmlFacts.get(i).getFactName(), new GenericStringVar(this.connName, this.xmlFacts.get(i)), true, c);
					} catch (JessException je) {System.out.println(je.getDetail()); 
					} catch (CloneNotSupportedException cnse) { System.out.println(cnse.toString()); }
				} else if(this.xmlFacts.get(i).getType().toLowerCase().contains("pose")) {
					try {
	    				engine.defclass(this.xmlFacts.get(i).getFactName(), "jessmw.GenericPoseVar", null);
	    				engine.definstance(this.xmlFacts.get(i).getFactName(), new GenericPoseVar(this.connName, this.xmlFacts.get(i)), true, c);
					} catch (JessException je) {System.out.println(je.getDetail()); 
					} catch (CloneNotSupportedException cnse) { System.out.println(cnse.toString()); }
				} else if(this.xmlFacts.get(i).getType().toLowerCase().contains("time")) {
					try {
	    				engine.defclass(this.xmlFacts.get(i).getFactName(), "jessmw.GenericTimeVar", null);
	    				engine.definstance(this.xmlFacts.get(i).getFactName(), new GenericTimeVar(this.connName, this.xmlFacts.get(i)), true, c);
					} catch (JessException je) {System.out.println(je.getDetail()); 
					} catch (CloneNotSupportedException cnse) { System.out.println(cnse.toString()); }
				} else if(this.xmlFacts.get(i).getType().toLowerCase().contains("double")) {
					switch (this.xmlFacts.get(i).getwidth()) {
					case 1:
						try {
	  	    				engine.defclass(this.xmlFacts.get(i).getFactName(), "jessmw.GenericDouble1Var", null);
	  	    				engine.definstance(this.xmlFacts.get(i).getFactName(), new GenericDouble1Var(this.connName, this.xmlFacts.get(i)), true, c);
						} catch (JessException je) {System.out.println(je.getDetail()); 
						} catch (CloneNotSupportedException cnse) { System.out.println(cnse.toString()); }
						
						break;
					case 2:
						try {
	  	    				engine.defclass(this.xmlFacts.get(i).getFactName(), "jessmw.GenericDouble2Var", null);
	  	    				engine.definstance(this.xmlFacts.get(i).getFactName(), new GenericDouble2Var(this.connName, this.xmlFacts.get(i)), true, c);
						} catch (JessException je) {System.out.println(je.getDetail()); 
						} catch (CloneNotSupportedException cnse) { System.out.println(cnse.toString()); }
						break;
					case 3:
						try {
	  	    				engine.defclass(this.xmlFacts.get(i).getFactName(), "jessmw.GenericDouble3Var", null);
	  	    				engine.definstance(this.xmlFacts.get(i).getFactName(), new GenericDouble3Var(this.connName, this.xmlFacts.get(i)), true, c);
						} catch (JessException je) {System.out.println(je.getDetail()); 
						} catch (CloneNotSupportedException cnse) { System.out.println(cnse.toString()); }
						break;
					case 4:
						try {
	  	    				engine.defclass(this.xmlFacts.get(i).getFactName(), "jessmw.GenericDouble4Var", null);
	  	    				engine.definstance(this.xmlFacts.get(i).getFactName(), new GenericDouble4Var(this.connName, this.xmlFacts.get(i)), true, c);
						} catch (JessException je) {System.out.println(je.getDetail()); 
						} catch (CloneNotSupportedException cnse) { System.out.println(cnse.toString()); }
						break;
					case 5:
						try {
	  	    				engine.defclass(this.xmlFacts.get(i).getFactName(), "jessmw.GenericDouble5Var", null);
	  	    				engine.definstance(this.xmlFacts.get(i).getFactName(), new GenericDouble5Var(this.connName, this.xmlFacts.get(i)), true, c);
						} catch (JessException je) {System.out.println(je.getDetail()); 
						} catch (CloneNotSupportedException cnse) { System.out.println(cnse.toString()); }
						break;
					case 6:
						try {
							engine.defclass(this.xmlFacts.get(i).getFactName(), "jessmw.GenericDouble6Var", null);
	  	    				engine.definstance(this.xmlFacts.get(i).getFactName(), new GenericDouble6Var(this.connName, this.xmlFacts.get(i)), true, c);
						} catch (JessException je) {System.out.println(je.getDetail()); 
						} catch (CloneNotSupportedException cnse) { System.out.println(cnse.toString()); }
						break;
					case 7:
						try {
	  	    				engine.defclass(this.xmlFacts.get(i).getFactName(), "jessmw.GenericDouble7Var", null);
	  	    				engine.definstance(this.xmlFacts.get(i).getFactName(), new GenericDouble7Var(this.connName, this.xmlFacts.get(i)), true, c);
						} catch (JessException je) {System.out.println(je.getDetail()); 
						} catch (CloneNotSupportedException cnse) { System.out.println(cnse.toString()); }
						break;
					case 8:
						try {
	  	    				engine.defclass(this.xmlFacts.get(i).getFactName(), "jessmw.GenericDouble8Var", null);
	  	    				engine.definstance(this.xmlFacts.get(i).getFactName(), new GenericDouble8Var(this.connName, this.xmlFacts.get(i)), true, c);
						} catch (JessException je) {System.out.println(je.getDetail()); 
						} catch (CloneNotSupportedException cnse) { System.out.println(cnse.toString()); }
						break;
					case 9:
						try {
	  	    				engine.defclass(this.xmlFacts.get(i).getFactName(), "jessmw.GenericDouble9Var", null);
	  	    				engine.definstance(this.xmlFacts.get(i).getFactName(), new GenericDouble9Var(this.connName, this.xmlFacts.get(i)), true, c);
						} catch (JessException je) {System.out.println(je.getDetail()); 
						} catch (CloneNotSupportedException cnse) { System.out.println(cnse.toString()); }
						break;
					
					default:
						try {
	  	    				engine.defclass(this.xmlFacts.get(i).getFactName(), "jessmw.GenericStringVar", null);
	  	    				engine.definstance(this.xmlFacts.get(i).getFactName(), new GenericStringVar(this.connName, this.xmlFacts.get(i)), true, c);
						} catch (JessException je) {System.out.println(je.getDetail());
						} catch (CloneNotSupportedException cnse) { System.out.println(cnse.toString()); }
					}
				}
    		} catch (NullPointerException npe) {
				try {
    				engine.defclass(this.xmlFacts.get(i).getFactName(), "jessmw.GenericStringVar", null);
    				engine.definstance(this.xmlFacts.get(i).getFactName(), new GenericStringVar(this.connName, this.xmlFacts.get(i)), true, c);
				} catch (JessException je) {System.out.println(je.getDetail());
				} catch (CloneNotSupportedException cnse) { System.out.println(cnse.toString()); }
			
			}
		}
    }
    
    private void parseDocument() {
		// get the root element
		Element docEle = this.dom.getDocumentElement();
		//get a nodelist of element
		this.connName = docEle.getAttribute("instance"); // unique instance name
		if (this.debug) System.out.println("Processing mobotware facts: " + this.connName);
		NodeList nl = docEle.getElementsByTagName("server");
		if(nl != null && nl.getLength() > 0) {
			for(int i =0; i < nl.getLength(); i++) {
				
				//get the server element
				Element elServ = (Element)nl.item(i);
				Server h = new Server();
				h.setPushServer(true);
				
				String port = elServ.getAttribute("port");
				String name = elServ.getAttribute("name");
				
				if (port != "") {
					h.setPort(Integer.parseInt(port));
				} else {
					if (name.toLowerCase().contains("cam")) {
						h.setPort(24920);
					} else if (name.toLowerCase().contains("lms") || name.toLowerCase().contains("laser")) {
						h.setPort(24919);
					} else if (name.toLowerCase().contains("mrc")) {
						h.setPort(31001);
					}
				}
				if (h.getPort() == 0) { //if we don't have a port number by now, we don't know where to get the fact from, so don't keep the fact
					continue;
				}
				
				if (name.toLowerCase().contains("mrc")) {
					name = "mrc";
					h.setPushServer(false);
				}
				h.setServerName(name);
				String host = elServ.getAttribute("host"); //host should at minimum be specified for the first element
				if ((host != "") && (this.defaultHost == null)) {
					this.defaultHost = host;
				} else if (host == "") {
					host = this.defaultHost;
				}
				h.setHost(host);
				NodeList nlStruct = elServ.getElementsByTagName("struct");
				if (nlStruct != null && nlStruct.getLength() > 0) {
					for(int j=0; j < nlStruct.getLength(); j++) {
						
						//get the struct element
						Element elStruct = (Element)nlStruct.item(j);
						VariableStruct s = new VariableStruct(); //construct a class for each struct, these will be used for an updating push if needed
						s.setHost(h);
						s.setStructName(elStruct.getAttribute("name"));
						NodeList nlPush = elStruct.getElementsByTagName("push");
						if (nlPush != null && nlPush.getLength() > 0) {
							//get the push element
							Element elPush = (Element)nlPush.item(0); //assumes only one push tag
							
							double time = getDoubleValue(elPush, "time"); //supports both update frequency
							if (time < 0.0) {
								time = 1.0 / getDoubleValue(elPush, "freq"); // and update time interval
							}
							if (time > 0.0)
								s.setTime(time);
							else
								s.setTime((this.timeout*1.0)/1000.0);
							
							if (h.getServerName() != "mrc") {
								s.setPushable(true); //the struct is updated via push on server
								s.setInitialized(false);
									
								String cmd = getTextValue(elPush,"cmd");
								if ((cmd != "") && (cmd != null)) {
									s.setCmd(cmd); //push has special cmd, i.e. gmk img = 30
								} else {
									s.setCmd(s.getStructName()); //push is same as struct, i.e. gmk
								}
							} else {
								if (time > 0.0) 
									this.timeout = (int) (time*1000.0);
								s.setInitialized(true); //don't try to setup push commands on mrc
								
							}
						}
						
						NodeList nlVar = elStruct.getElementsByTagName("var");
						if (nlVar != null && nlVar.getLength() > 0) {
							for(int k=0; k < nlVar.getLength(); k++) {
								Element elVar = (Element)nlVar.item(k);
								XmlFact f = new XmlFact();
								
								f.setStruct(s);
								populateXmlFact(f, elVar);
								this.xmlFacts.add(f);
								if (this.debug) System.out.println("Read fact " + f.toString());
								

							}
						}
						if (this.debug) System.out.println("Read Struct " + s.toString());
						this.structs.add(s);	
					}
				}
				if (this.debug) System.out.println("Read server " + h.toString());
				this.servers.add(h);	
			}
		}
    	
    }
    
    /**
	 * Take an XmlFact element and read the values in, create
	 * an XmlFact object and return it
	 * @param empEl
	 * @return
	 */    
    private void populateXmlFact(XmlFact f, Element factEl) {
    	
    	f.setVariable(getTextValue(factEl, "name"));
    	f.setType(getTextValue(factEl, "type"));
    	
    	int width = getIntValue(factEl, "width");
    	if (width > 0) {
    		f.setwidth(width);
    	}
    	String factName = getTextValue(factEl, "factName");
    	if (factName != null) {
    		f.setFactName(factName.replace(" ", ""));
    	} else {
   			f.setFactName(this.connName + "." + f.getStruct().getHost().getServerName() + "." + f.getStruct().getStructName() + "." + f.getVariable().replace(" ", ""));
    	}
    	
    	
    }
    
    /**
	 * I take a xml element and the tag name, look for the tag and get
	 * the text content 
	 * i.e for <employee><name>John</name></employee> xml snippet if
	 * the Element points to employee node and tagName is name I will return John  
	 * @param ele
	 * @param tagName
	 * @return
	 */
	private String getTextValue(Element ele, String tagName) {
		String textVal = null;
		NodeList nl = ele.getElementsByTagName(tagName);
		if(nl != null && nl.getLength() > 0) {
			Element el = (Element)nl.item(0);
			textVal = el.getFirstChild().getNodeValue();
		} else {
			nl = ele.getElementsByTagName(tagName.toLowerCase());
			if(nl != null && nl.getLength() > 0) {
				Element el = (Element)nl.item(0);
				textVal = el.getFirstChild().getNodeValue();
			}
		}
		return textVal;
	}
	
	/**
	 * Calls getTextValue and returns a int value
	 * @param ele
	 * @param tagName
	 * @return
	 */
	private int getIntValue(Element ele, String tagName) {
		//in production application you would catch the exception
		int retval = -1;
		try {
			retval = Integer.parseInt(getTextValue(ele,tagName));
		} catch (NumberFormatException nfe) {
			retval = -1;
		}
		return retval; 
	}
	
	/**
	 * Calls getTextValue and returns a double value
	 * @param ele
	 * @param tagName
	 * @return
	 */
	private double getDoubleValue(Element ele, String tagName) {
		//in production application you would catch the exception
		double retval = -1.0;
		try {
			retval = Double.parseDouble(getTextValue(ele,tagName));
		} catch (NumberFormatException nfe) {
			retval = -1.0;
		} catch (NullPointerException npe) {
			retval = -1.0;
		}
		return retval;
	}
	

}