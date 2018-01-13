import bc.*;

/**
 * Represents either a factory or a rocket blueprint
 * @author virsain
 *
 */
public class Blueprint {
	public int id;	// the id of the blueprint
	public MapLocation currentLocation; // the location of the blueprint
	public UnitType type;
	
	/**
	 * Creates a new Blueprint
	 * @param id
	 * 	The blueprint ID
	 * @param location
	 * 	The location of the blueprint
	 * @param structure
	 * 	The structure type
	 */
	public Blueprint(int id, MapLocation location, UnitType structure) {
		this.id = id;
		currentLocation = location;
		type = structure;
	}
}
