package GUI;
import jess.*;


public class pkg implements Userpackage {
	private static Rete reteEngine;
	
    public void add(Rete engine) {
    	reteEngine = engine;
    	
    	engine.addUserfunction(new funGUI());
    	
    }
    
    public static void evaluate(String jessCmd) {
    	try {
    		reteEngine.eval(jessCmd);
    	}
    	catch (JessException je) {}
    }
}