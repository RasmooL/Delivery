package routeplanner;
import java.util.HashSet;
import java.util.Set;


public class Node {
	float x; 
	float y;
	
	float f_score = 0;
	float g_score = 0;
	Node cameFrom = null;
	
	String hash = "heureka";
	
	Set<Node> neighbors = new HashSet<Node>();
	
	float distance(Node node)
	{
		return (float)Math.sqrt((x-node.x)*(x-node.x) + (y-node.y)*(y-node.y));
	}
	
	Node(float x, float y) {
		this.x = x;
		this.y = y;	
	}
	
		    
    @Override
	public String toString() {  	
    	String tmp = "(" + this.x + ", " + this.y + ")";
    	/*for(Node node : neighbors)
    	{
    		tmp += "-> " + node.x + " " + node.y;
    	}*/
		return tmp;
	}

	@Override
    public int hashCode(){
        int hashcode = 0;
        hashcode = (int)((this.x*100 + this.y*100) + hash.hashCode());
        return hashcode;
    }
    
    @Override
    public boolean equals(Object obj){
        if (obj instanceof Node) {
            Node temp = (Node) obj;
            return (this.x == temp.x && this.y == temp.y);
        } else {
            return false;
        }
    }
    
}