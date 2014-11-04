package jessmw;
import jess.*;


public class pkg implements Userpackage {
	private static Rete reteEngine;
	
    public void add(Rete engine) {
    	reteEngine = engine;
    	
    	engine.addUserfunction(new funAddFact());  	     // Jess function to add a fact at runtime
    	engine.addUserfunction(new funConnect());  	     // Jess function which reads an xml config file and connect to the specified servers
		engine.addUserfunction(new funDisconnect());     // SMR disconnection
		engine.addUserfunction(new funSMRTalk());        // Jess side user SMR-CL talk with the MRC
		engine.addUserfunction(new funReadPlan());       // Jess function to load a SMR-CL plan to mrc
		engine.addUserfunction(new funPushTalk());        // Jess side user raw talk with the AURS using the PushStream Socket
		engine.addUserfunction(new funTalk());           // Jess side user raw talk with the AURS
		engine.addUserfunction(new funFlushBuffer());    // Jess function to flush the input buffer
		engine.addUserfunction(new funListConn());       // Display list of connected SMRs

		engine.addJessListener(new eventHandler());		 //handles retraction of facts
		engine.setEventMask(engine.getEventMask() | JessEvent.FACT );
    }
    
    public static void evaluate(String jessCmd) {
    	try {
    		reteEngine.eval(jessCmd);
    	}
    	catch (JessException je) {}
    }
}