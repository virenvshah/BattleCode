package algorithms;

import java.util.Comparator;

/**
 * A Comparator for a TileNode
 * 
 * @author virsain
 */
public class TileNodeComparator implements Comparator<TileNode> {

	@Override
	public int compare(TileNode o1, TileNode o2) {
		if (o1.weight > o2.weight) return 1;
		else if (o1.weight < o2.weight) return -1;
		else return 0;
	}

}
