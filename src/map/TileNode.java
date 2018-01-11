/**
 * Represents a tile on a planet
 * 
 * @author virsain
 */
public class TileNode {
	Tuple location; // the Cartesian location of the tile
	
	/* -1 represents a Karbonite site
	 * 0 represents an empty tile
	 * 1 represents a water tile
	 */
	int tileType;
	
	/* An integer representing the occupant type
	 * 2 represents an ally worker
 	 * 3 represents an ally knight
 	 * 4 represents an ally ranger
    * 5 represents an ally mage
    * 6 represents an ally healer
    * 7 represents an ally rocket
    * 8 represents an ally factory
	 * 12 represents an enemy worker
    * 13 represents an enemy knight
    * 14 represents an enemy ranger
    * 15 an enemy mage
    * 16 an enemy healer
    * 17 An enemy rocket
    * 18 an enemy factory
	 */
	int occupant;
	
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
