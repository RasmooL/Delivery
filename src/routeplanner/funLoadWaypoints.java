package routeplanner;
import jess.*;

import java.util.*;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;



public class funLoadWaypoints implements Userfunction {
	
	private Rete engine;
	private String functionName = "load-waypoints";
	private Map<Integer, Node> nodes;
	
	@Override
	public String getName() {
		return functionName;
	}

	@Override
	public Value call(ValueVector vv, Context context) throws JessException {
		engine = context.getEngine();
		if(vv.size() != 2) throw new JessException(functionName,"Wrong number of arguments ",vv.size() - 1);
		
		nodes = new HashMap<Integer, Node>();
		
		BufferedReader reader = null;
		String read = "";
		
		try
		{
			reader = new BufferedReader(new FileReader(vv.get(1).stringValue(context)));
			while((read = reader.readLine()) != null)
			{
				if(read.isEmpty()) continue;
				String[] tokens = read.split(" ");
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
		
		return (Value) nodes;
	}
	

}
