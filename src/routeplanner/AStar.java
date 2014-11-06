package routeplanner;
import java.util.*;

public class AStar {
	Comparator<Node> compare = new FScoreComparator();
	PriorityQueue<Node> frontier;
	HashMap<Integer, Node> nodes;
	Set<Node> explored = new HashSet<Node>();
	
	public AStar(HashMap<Integer, Node> nodes)
	{
		this.nodes = nodes;
		frontier = new PriorityQueue<Node>(100, compare);
	}
	
	private Boolean nodeExists(Integer n) {
		for (Integer node : this.nodes.keySet()) {
			if (node.equals(n)) {
				return true;
			}
		}
		return false;
	}
	
	public void calculate(int start, int goal)
	{
		if(!nodeExists(start) || !nodeExists(goal))
		{
			System.out.println("A*: Start or end node doesn't exist!")
			return;
		}
		
		frontier.add(nodes.get(start));
		
		while(!frontier.isEmpty())
		{
			Node currentNode = frontier.poll();

			if (currentNode.equals(goal)) {
				reconstructPath(start, currentNode);
				break;
			}

			frontier.remove(currentNode);
			explored.add(currentNode);

			for (Node node : currentNode.neighbors) {
				// If node is already explored, goto next node.
				if (explored.contains(node))
					continue;
				// Preliminary g score
				float pre_gscore = currentNode.g_score
						+ node.distance(currentNode);
				
				// If the preliminary g score is < than the nodes current score
				// Or if the frontier does not contain this node
				if (!frontier.contains(node) || pre_gscore < node.g_score) {
					node.cameFrom = currentNode;
					node.g_score = pre_gscore;
					node.f_score = node.g_score + heuristicEstimate(node, nodes.get(goal));
					frontier.add(node);
				}

			}
		}
	}
	
	private void reconstructPath(Node start, Node current) {
		List<Node> nodes = new ArrayList<Node>();
		while (current.cameFrom != null) {
			nodes.add(current);
			current = current.cameFrom;
		}
		nodes.add(start);

		for (int i = nodes.size() - 1; i > 0; i--) {
			Node from = nodes.get(i);
			Node to = nodes.get(i - 1);
			int count = 0;
			for (Edge edge : this.edges) {
				if (count == 1)
					break;
				if (edge.getEdge(from, to) != null) {
					System.out.println(from + " " + edge.getEdge(from, to)
							+ " " + to);
					count++;
				}

			}
		}

	}
	
	private float heuristicEstimate(Node start, Node goal)
	{
		return start.distance(goal);
	}

}
