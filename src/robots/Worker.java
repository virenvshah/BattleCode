import bc.*;
import java.util.*;
/**
 * Represents a worker robot in the game
 * @author virsain
 */
public class Worker extends AbstractRobot {
	public enum State { Build, Move, Mine, Idle; };

	public State state;
	public State previousState;

	// the blueprint which the worker is currently working on
	public Blueprint currentBlueprint;


	// the deque which contains all of the locations to mine from
	public ArrayDeque<MapLocation> mineLocs;

	// the set which contains all of the mined out MapLocations
	public HashSet<MapLocation> minedOut;


	/**
	 * Creates a new Worker
	 * @param i
	 * 	The worker's id
	 * @param g
	 * 	The GameController for the game
	 * @param map
	 * 	The BattleMap for the game
	 * @param location
	 * 	The initial location of the worker
	 * @param minelocs
	 *  A pointer to the deque which contains all of the locations to mine from
	 * @param minedout
	 * A pointer to the hashset which contains all of the mined out locations.
	 */
	public Worker(int i, GameController g, Map map, MapLocation location,
	ArrayDeque<MapLocation> minelocs, HashSet<MapLocation> minedout) {
		super(i, g, map, location);
		occupantType = UnitType.Worker;
		state = State.Idle;
		previousState = State.Idle;
		mineLocs = minelocs;
		minedOut = minedout;
	}

	/**
	 * Mines out a 3x3 square around the worker:
	 * @return
	 * -1 if unable to mine, 0 if the center is being mined, 1 if the
	 * east is being mined, 2 if the north is being mined, 3 if the northeast is
	 * being mined, 4 if the northwest is being mined, 5 if the south is being
	 * mined, 6 if the southwest is being mined, 7 if the southeast is being
	 * mined, 8 if the west is being mined, and 9 if the area is completely mined
	 * out.
	 */
	public int mineArea(){
		 previousState = state;
		 // update the State
		 state = State.Mine;

		 Direction[] dirs = Direction.value();

		// checking if the tile isn't mined out, but the unit can't mine
		// also determining the direction to mine in and updating the pointers
		// to include mined out locations
		int pointToMine;
 		for (int i =0;i<dirs.length;i++){
			// if worker is on cooldown
			 if(!(gc.canHarvest(id,dirs[i]))
			 && karboniteAt(currentLocation.add(dirs[i])) > 0.){
				 return -1;
			 }
			 // if worker can mine and there's karbonite at this point
			 else if (karboniteAt(currentLocation.add(dirs[i])) > 0.){
				 pointToMine=i;
			 }
			 // if worker can mine but no carbonite is available
			 else {
				 minedOut.add(currentLocation.add(dirs[i]));
				 // if this is the last mine tile, then set to "done"
				 if (i==8){
					 state = State.Idle;
					 pointToMine = 9;
				 }
			 }
		}

		// actually harvest the thing
		if (pointToMine>=0 && pointToMine<9){
			gc.harvest(id,dirs[pointToMine]);
		}

		// return current progress
		return pointToMine;
	 }

	/**
	 * Creates a new blueprint at the specified direction
	 * @param dir
	 * 	The direction from the worker
	 * @return
	 * 	A Blueprint class representing the blueprint
	 */
	public Blueprint setBlueprint(Direction dir, UnitType structureType) {
		previousState = state;

		// Check if the worker (occupantType) can lay the blueprint
		if (!gc.canBlueprint(id, structureType, dir)) return null;

		// lay the blueprint
		gc.blueprint(id, structureType, dir);

		// get the fields required to make the blueprint class
		MapLocation blueprintLocation = currentLocation.add(dir);
		int blueprintId = gc.senseUnitAtLocation(blueprintLocation).id();

		// update the map about the blueprint location
		battleMap.updateOccupant(blueprintLocation, structureType);

		// update the state
		state = State.Build;

		currentBlueprint = new Blueprint(blueprintId, blueprintLocation,
				structureType);

		// return the newly created blueprint class
		return currentBlueprint;
	}

	/**
	 * Builds on a given blueprint
	 * @param blueprint
	 * 	Blueprint on which to build on
	 * @return
	 * 	0 if building, 1 if structure is finished building,
	 * 	2 if failed to build
	 */
	public int build(Blueprint blueprint) {
		currentBlueprint = blueprint;
		return build();
	}


	/**
	 * Builds on an existing blueprint
	 * @return
	 * 	0 if building, 1 if structure is finished building,
	 * 	2 if failed to build
	 */
	public int build() {
		previousState = state;
		// check to see if the worker can build
		if (!gc.canBuild(id, currentBlueprint.id)) return 2;

		System.out.println("building");
		gc.build(id, currentBlueprint.id);

		// if the structure is completely built
		if (gc.unit(currentBlueprint.id).structureIsBuilt() == 1) {
			state = State.Idle;
			currentBlueprint = null;
			return 1;
		}

		// if the structure is not done building
		state = State.Build;
		return 0;
	}

	/**
	 * Tries to move the robot along a path.
	 * @return
	 * 	Returns -1 if no path is set
	 * 	Returns  0 if successfully moved
	 * 	Returns  1 if successfully moved and reached destination
	 * 	Returns  2 if not ready to move (still on cooldown)
	 * 	Returns  3 if path is blocked
	 */
	public int move() {
		previousState = state;
		int returnValue = super.move();

		// if no path is set, or worker finished moving, worker's state is null
		if (returnValue == -1 && returnValue == 1)
			state = State.Idle;

		// if worker is still moving along path, worker's state is MOVE
		else
			state = State.Move;

		return returnValue;

	}

	/**
	 * Tries to move the robot in a particular direction
	 *
	 * @param dir
	 * 	The direction in which the robot must move
	 * @return
	 * 	Returns 1 if successfully moved
	 * 	Returns 2 if not ready to move (still on cooldown)
	 * 	Returns 3 if path is blocked
	 */
	public int move(Direction dir) {
		previousState = state;
		int returnValue = super.move(dir);

		// worker finished moving, worker's state is null
		if (returnValue == 1) {
			System.out.println("moved");
			state = State.Idle;
		}

		// if worker is still must move along direction, worker's state is MOVE
		else
			state = State.Move;

		return returnValue;
	}
}
