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
		
		// Go through facts and find goal facts
		Iterator facts = engine.listFacts();
		while(facts.hasNext())
		{
			Fact fact = (Fact) facts.next();
			System.out.println(fact.toString());
		}
		
		// Pseudocode for route plan:
		
		// Calculate shortest route between current position and all n goals:
			// foreach goal in goals:
				// a-star(current, goal)
				// foreach other_goal in goals != goal && not already calculated
					// a-star(goal, other_goal) 
		// Bruteforce TSP problem:
			// todo
		
		return jess.Funcall.NIL;
	}
	

}
