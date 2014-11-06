package routeplanner;
import java.util.Comparator;

public class FScoreComparator implements Comparator<Node> {

	@Override
	public int compare(Node arg0, Node arg1) {
		if (arg0.f_score < arg1.f_score)
			return -1;
		if (arg0.f_score > arg1.f_score)
			return 1;
		return 0;
	}

}
