import bc.*;

/**
 * Represents a structure
 * @author virsain
 *
 */
public abstract class AbstractStructure extends AbstractUnit{
	public enum State {Produce, Unload, Bankrupt, Blueprint, Idle};
	
	public State state;
	public State previousState;
	
	public MapLocation currentLocation; // the location of the structure
	public UnitType type;
	public GameController gc;
	public Map battleMap;
	/**
	 * Creates a new AbstractStructure
	 * @param id
	 * 	The blueprint ID
	 * @param location
	 * 	The location of the blueprint
	 * @param structure
	 * 	The structure type
	 */
	public AbstractStructure(int id, GameController g, Map map, MapLocation location, UnitType structure) {
		this.id = id;
		currentLocation = location;
		type = structure;
		gc = g;
		battleMap = map;
	}
}
