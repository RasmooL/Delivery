package routeplanner;
import jess.*;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

public class funGetRoute implements Userfunction {
	
	private Rete engine;
	private String functionName = "get-route";
	HashMap<Integer, Node> nodes;
	
	@Override
	public String getName() {
		return functionName;
	}

	@Override
	public Value call(ValueVector vv, Context context) throws JessException {
		engine = context.getEngine();
		if(vv.size() != 2) throw new JessException(functionName, "Wrong number of arguments ", vv.size() - 1);
		// Go through facts and find goal facts
		/*Iterator facts = engine.listFacts();
		while(facts.hasNext())
		{
			Fact fact = (Fact) facts.next();
			System.out.println(fact.toString());
		}*/
		
		loadWaypoints(vv.get(1).stringValue(context));
		//AStar as = new AStar(nodes);
		//List<Node> sol = as.calculate(1, 6);
		//System.out.println(sol.toString());
		
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
	
	public void loadWaypoints(String file) throws JessException {
		nodes = new HashMap<Integer, Node>();
		
		BufferedReader reader = null;
		String read = "";
		
		try
		{
			reader = new BufferedReader(new FileReader(file));
			while((read = reader.readLine()) != null)
			{
				if(read.isEmpty()) continue;
				String[] tokens = read.split("\t");
				if(tokens[0].equals("N"))
				{
					int id = Integer.parseInt(tokens[1]);
					float x = Float.parseFloat(tokens[2]);
					float y = Float.parseFloat(tokens[3]);
					nodes.put(id, new Node(x, y));
				}
				else if(tokens[0].equals("E"))
				{
					int first = Integer.parseInt(tokens[1]);
					int second = Integer.parseInt(tokens[2]);
					nodes.get(first).neighbors.add(nodes.get(second));
					nodes.get(second).neighbors.add(nodes.get(first));
				}
			}
			
			reader.close();
		}
		catch(FileNotFoundException e)
		{
			throw new JessException(functionName, e.getMessage(), 0);
		}
		catch(IOException e)
		{
			throw new JessException(functionName, e.getMessage(), 0);
		}
		
	}

}
