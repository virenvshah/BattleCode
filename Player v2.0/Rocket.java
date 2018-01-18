import bc.*;

/**
 * Represents a Rocket
 * @author vineet
 */
public class Rocket extends AbstractStructure {
	Direction unloadDir;
	int robotNumInside = 0;

	/**
	 * Creates a new Rocket
	 */
	public Rocket(int id, GameController g, Map map, MapLocation location) {
		super(id, g, map, location, UnitType.Rocket);

		// factories can only be made by worker's in their blueprint stage
		state = State.Blueprint;
		previousState = State.Blueprint;
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

		// check if Rocket can unload
		if (!gc.canUnload(id, unloadDir)) {
			state = State.Unload;
			return null;
		}

		System.out.println("unloading");
		gc.unload(id, unloadDir);

		// update the Rocket state
		state = State.Idle;

		// create a robot class for the type unloaded
		MapLocation robotLocation = currentLocation.add(unloadDir);
		Unit robot = gc.senseUnitAtLocation(robotLocation);
		battleMap.updateOccupant(robotLocation, robot.id());

		return robot;
	}

	/**
	 * Loads a nearby unit into its garrison
	 * @param robotID
	 * 	The robot ID to be loaded
	 * @return
	 *   -1 if we can't
	 *    0 if we just did and there's still space
	 * 		1 if we just did and we hit capacity
	 */
	 public int load(int robotID){
		 Unit thisRocket = unit(id);
		 if (gc.canLoad(id,robotID)){
			 gc.load(id,robotID);
			 robotNumInside++;
			 if(robotNumInside*1.0==thisRocket.structureMaxCapacity()){
				 return 1;
			 }
			 return 0;
		 }
		 return -1;
	 }


	public int getOccupancy(){
			return robotNumInside;
	}

	public void setUnloadDir(Direction dir) {
		unloadDir = dir;
	}

	public MapLocation getLocation() {
		return currentLocation;
	}
}
