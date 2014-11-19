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
		List<Integer> bestRoute = null;
		//Integer[] bestRoute = null;
		float currentLength = Float.MAX_VALUE;
		float newLength = Float.MAX_VALUE;
		long startTime = System.currentTimeMillis();
		do
		{
			if(!goalList.get(0).equals(start_node)) continue; // We only want permutations that start at our start node
			newLength = routeLength(paths, goalList);
			//System.out.println(Arrays.toString(goalArray) + " = " + newLength); // Prints all permutations (slow!)
			if(newLength < currentLength)
			{
				bestRoute = new ArrayList<Integer>(goalList);
				currentLength = newLength;
			}
		}
		while(next_permutation(goalList));
		long endTime = System.currentTimeMillis();
		bestRoute.add(bestRoute.get(0)); // End = start
		System.out.println("Best route: " + bestRoute.toString() + " = " + currentLength + "(" + (endTime-startTime) + " ms)");
		
		int waynum = 0;
		for(int num = 1; num < bestRoute.size(); num++)
		{
			int from = bestRoute.get(num - 1);
			int to = bestRoute.get(num);
			Node fromNode = nodes.get(from);
			Node toNode = nodes.get(to);
			List<Node> waypoints = paths.get(from).get(to);
			for(Node waypoint : waypoints)
			{
				engine.assertString("(move-plan " + waynum++ + " " + waypoint.x + " " + waypoint.y +  " " + waypoint.danger + ")");
			}
		}
			
		
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
	float routeLength(Map<Integer, Map<Integer, List<Node>>> paths, List<Integer> route)
	{
		float length = 0;
		for(int goal_num = 0; goal_num < route.size(); goal_num++)
		{
			int fromNode = route.get(goal_num);
			int toNode = route.get((goal_num + 1) % route.size());
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
	
	boolean next_permutation(List<Integer> p)
	{
		for (int a = p.size() - 2; a >= 0; --a)
		{
			if (p.get(a) < p.get(a + 1))
			{
				for (int b = p.size() - 1;; --b)
				{
					if (p.get(b) > p.get(a))
					{
						int t = p.get(a);
						p.set(a, p.get(b));
						p.set(b, t);
						for (++a, b = p.size() - 1; a < b; ++a, --b)
						{
							t = p.get(a);
							p.set(a, p.get(b));
							p.set(b, t);
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
					String third = tokens[3];
					nodes.get(first).neighbors.add(nodes.get(second));
					nodes.get(second).neighbors.add(nodes.get(first));
					if(third.equals("D")) // door
					{
						nodes.get(first).danger = 1;
						nodes.get(second).danger = 1;
					}
					else if(third.equals("R")) // robot
					{
						nodes.get(first).danger = 2;
						nodes.get(second).danger = 2;
					}
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
