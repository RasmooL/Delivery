package routeplanner;
import jess.*;

import java.util.*;

public class funGetRoute implements Userfunction {
	
	private Rete engine;
	
	@Override
	public String getName() {
		return "get-route";
	}

	@Override
	public Value call(ValueVector vv, Context context) throws JessException {
		engine = context.getEngine();
		System.out.println("Current goals are:");
		
		Iterator facts = engine.listFacts();
		while(facts.hasNext())
		{
			Fact fact = (Fact) facts.next();
			System.out.println(fact.toString());
		}
		
		return jess.Funcall.NIL;
	}
	

}
