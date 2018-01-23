import bc.*;

/**
 * Represents a structure
 * @author virsain
 *
 */
public abstract class AbstractStructure extends AbstractUnit{
	public enum State {Produce, Unload, Bankrupt, Blueprint, Idle};
	public Direction unloadDir;
	
	public State state;
	public State previousState;
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
	public AbstractStructure(int i, GameController g, Map map, MapLocation location, UnitType structure) {
		id = i;
		currentLocation = location;
		type = structure;
		gc = g;
		battleMap = map;
		state = State.Blueprint;
		
		battleMap.updateOccupant(location, i);	
	}
	
	/**
	 * Unloads a unit in a particular direction
	 * @param dir
	 * 	The direction to unload the unit
	 * @return
	 * 	An AbstractRobot object if unloaded successfully
	 * 	null if unloading was not possible
	 */
	public Unit unload() {
		if (unloadDir == null) return null;
		
		previousState = state;
		
		// check if factory can unload
		if (!gc.canUnload(id, unloadDir)) {
			state = State.Unload;
			return null;
		}
		
		System.out.println("unloading");
		gc.unload(id, unloadDir);
		
		// update the factory state
		
		// if there are more units garrisoned
		if (gc.unit(id).structureGarrison().size() > 0) {
			// then they must still be unloaded
			state = State.Unload;
			
		// no more units garrisoned
		} else {
			state = State.Idle;
		}
		
		// update the battleMap for the unloaded unit
		MapLocation robotLocation = currentLocation.add(unloadDir);
		Unit robot = gc.senseUnitAtLocation(robotLocation);
		battleMap.updateOccupant(robotLocation, robot.id());
		
		return robot;
	}
	
	public void setUnloadDir(Direction dir) {
		unloadDir = dir;
	}

}
