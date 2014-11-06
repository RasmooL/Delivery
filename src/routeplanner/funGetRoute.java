package routeplanner;
import jess.*;

import java.util.*;

public class funGetRoute implements Userfunction {
	
	private Rete engine;
	private String functionName = "get-route";
	
	@Override
	public String getName() {
		return functionName;
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
		
		// Pseudocode for route plan - pretty clever, A* is used to create TSP problem which is then solved
		
		// Calculate shortest routes between every pair of goals and current pos
			// foreach goal in goals:
				// a-star(current, goal)
				// foreach other_goal in goals != goal && not already calculated
					// a-star(goal, other_goal) 
		// Bruteforce TSP problem (for n < ~13), guaranteed optimal solution (see perhaps http://bonsaicode.wordpress.com/2010/03/12/programming-praxis-traveling-salesman-brute-force/)
			// foreach circuit in goal graph - runs (n-1)! times
				// calculate circuit length
			// choose circuit with smallest length
		
		return jess.Funcall.NIL;
	}
	

}
