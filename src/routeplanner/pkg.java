package routeplanner;
import jess.*;


public class pkg implements Userpackage {
	private static Rete reteEngine;
	
    public void add(Rete engine) {
    	reteEngine = engine;
    	
    	engine.addUserfunction(new funGetRoute());

		//engine.addJessListener(new eventHandler());		 //handles retraction of facts
		//engine.setEventMask(engine.getEventMask() | JessEvent.FACT );
    }
    
    public static void evaluate(String jessCmd) {
    	try {
    		reteEngine.eval(jessCmd);
    	}
    	catch (JessException je) {}
    }
}