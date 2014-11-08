package routeplanner;
import jess.*;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.util.Map.Entry;

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
		if(vv.size() != 3) throw new JessException(functionName, "Wrong number of arguments ", vv.size() - 1);
		String file = vv.get(1).stringValue(context);
		int start_node = vv.get(2).intValue(context);
		
		// Go through facts and find goal facts
		Iterator<Fact> facts = engine.listFacts();
		int numGoals = 0;
		List<Fact> goals = new ArrayList<Fact>();
		while(facts.hasNext())
		{
			Fact fact = facts.next();
			if(!fact.getName().equals("MAIN::goal"))
				continue;
			numGoals++;
			goals.add(fact);
		}
		if(numGoals < 1) throw new JessException(functionName, "No goals ", 0);
		
		loadWaypoints(file);
		
		AStar as = new AStar(nodes);
		
		// Map[goal_from].Map[goal_to].List<Node> -- Symmetric
		Map<Integer, Map<Integer, List<Node>>> paths = new HashMap<Integer, Map<Integer, List<Node>>>();
		for(int g = 0; g < numGoals; g++) // g = goal_from
		{
			int goal_from = goals.get(g).getSlotValue("waypoint").intValue(context);
			Map<Integer, List<Node>> inner_map = new HashMap<Integer, List<Node>>();
			
			// Start node
			inner_map.put(start_node, as.calculate(goal_from, start_node));
			
			// Goal nodes
			for(int i = 0; i < numGoals; i++) // i = goal_to
			{
				//System.out.println(g + " => " + i);
				int goal_to = goals.get(i).getSlotValue("waypoint").intValue(context);
				if(i == g) continue; // Same goal
				inner_map.put(goal_to, as.calculate(goal_from, goal_to));
			}
			paths.put(goal_from, inner_map);
		}
		Map<Integer, List<Node>> inner_map = new HashMap<Integer, List<Node>>();
		for(int i = 0; i < numGoals; i++)
		{
			int goal_to = goals.get(i).getSlotValue("waypoint").intValue(context);
			inner_map.put(goal_to, as.calculate(start_node, goal_to));
		}
		paths.put(start_node, inner_map);
		System.out.println(paths.toString());
		
		// TSP bruteforce, super slow with n>10 :O
		List<Integer> goalList = new ArrayList<Integer>(paths.keySet());
		Collections.sort(goalList);
		Integer[] goalArray = new Integer[goalList.size()];
		goalList.toArray(goalArray);
		Integer[] bestRoute = null;
		float currentLength = Float.MAX_VALUE;
		float newLength = Float.MAX_VALUE;
		long startTime = System.currentTimeMillis();
		do
		{
			if(!goalArray[0].equals(start_node)) continue; // We only want permutations that start at our start node
			newLength = routeLength(paths, goalArray);
			//System.out.println(Arrays.toString(goalArray) + " = " + newLength); // Prints all permutations (slow!)
			if(newLength < currentLength)
			{
				bestRoute = goalArray.clone();
				currentLength = newLength;
			}
		}
		while(next_permutation(goalArray));
		long endTime = System.currentTimeMillis();
		System.out.println("Best route: " + Arrays.toString(bestRoute) + " = " + currentLength + "(" + (endTime-startTime) + " ms)");
		
		// Pseudocode for route plan - pretty clever, A* is used to create TSP problem which is then solved
		
		// Calculate shortest routes between every pair of goals and current pos
			// foreach goal in goals:
				// a-star(current, goal)
				// foreach other_goal in goals != goal && not already calculated
					// a-star(goal, other_goal) 
		// Bruteforce TSP problem
			// foreach circuit in goal graph - runs (n-1)! times
				// calculate circuit length
			// choose circuit with smallest length
		
		return jess.Funcall.NIL;
	}
	
	// Calculates route length, ending back at the first node
	float routeLength(Map<Integer, Map<Integer, List<Node>>> paths, Integer[] route)
	{
		float length = 0;
		for(int goal_num = 0; goal_num < route.length; goal_num++)
		{
			int fromNode = route[goal_num];
			int toNode = route[(goal_num + 1) % route.length];
			List<Node> path = paths.get(fromNode).get(toNode);
			
			Node curNode = nodes.get(fromNode);
			for(Node waypoint : path)
			{
				if(curNode == waypoint) continue;
				length += curNode.distance(waypoint);
				curNode = waypoint;
			}
		}
		return length;
	}
	
	boolean next_permutation(Integer[] p)
	{
		for (int a = p.length - 2; a >= 0; --a)
		{
			if (p[a] < p[a + 1])
			{
				for (int b = p.length - 1;; --b)
				{
					if (p[b] > p[a])
					{
						int t = p[a];
						p[a] = p[b];
						p[b] = t;
						for (++a, b = p.length - 1; a < b; ++a, --b)
						{
							t = p[a];
							p[a] = p[b];
							p[b] = t;
						}
					return true;
					}
				}
			}
		}
		return false;
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
		// Debug
		/*for(Map.Entry<Integer, Node> node : nodes.entrySet())
		{
			System.out.println("Neighbors of node " + node.getKey() + ":\n" + node.getValue().neighbors.toString());
		}*/
		
	}

}
