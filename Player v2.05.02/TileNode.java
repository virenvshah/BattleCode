import bc.*;

/**
 * Represents a tile on a planet
 * 
 * @author virsain
 */
public class TileNode {
	Tuple location; // the Cartesian location of the tile
	
	/* -1 represents a water tile
	 * 0 represents an empty tile
	 * > 0 represents a Karbonite tile where the num is the amount of Karbonite
	 */
	int tileType;
	
	int occupantId;
	
	// contains the coordinates of tiles that can be reached from this TileNode
	Tuple[] neighbors;
	
	// the 
	int numOfNeighbors;
	
	// the distance from the starting point
	int distance;
	
	// the sum of the distance from the starting point and
	// the heuristic function distance to the ending point
	int weight;
	
	// whether shortest path algorithm has visited this tile yet or not
	boolean wasVisited;
	
	// the parent of this TileNode
	TileNode predecessor;
	
	/**
	 * Creates a new TileNode
	 */
	public TileNode() {
		neighbors = new Tuple[8];
		numOfNeighbors = 0;
	}
	
	public String toString() {
		return location.toString();
	}
}
