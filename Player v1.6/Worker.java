import bc.*;
import java.util.ArrayList;
import java.util.HashSet;

/**
 * Represents a worker robot in the game
 * @author virsain
 */
public class Worker extends AbstractRobot {
	public enum Occupation { Miner, Builder; };
	
	// the blueprint which the worker is currently working on
	public AbstractStructure currentBlueprint; 
	
	// the deque of things to mine
	public ArrayList<Tuple> mineLocs;
	// the set of things to not mine
	public HashSet<Tuple> minedOut;
	public Occupation occupation;
	/**
	 * Creates a new building Worker
	 * @param i
	 * 	The worker's id
	 * @param g
	 * 	The GameController for the game
	 * @param map
	 * 	The BattleMap for the game
	 * @param location
	 * 	The initial location of the worker
	 */
	public Worker(int i, GameController g, Map map, MapLocation location) {
		super(i, g, map, location, UnitType.Worker);
		state = State.Idle;
		previousState = State.Idle;
		occupation = Occupation.Builder;
	}
	
	/**
	 * Creates a new mining worker
	 * @param i
	 * 	The worker's id
	 * @param g
	 * 	The GameController for the game
	 * @param map
	 * 	The BattleMap for the game
	 * @param location
	 * 	The initial location of the worker
	 * @param minelocs
	 * 	A mining data structure with the mining locations
	 * @param minedout
	 * 	A mining data structure for locations that were mined
	 */
	public Worker(int i, GameController g, Map map, MapLocation location, 
			ArrayList<Tuple> minelocs, HashSet<Tuple> minedout) {
		super(i, g, map, location, UnitType.Worker);
		state = State.Idle;
		previousState = State.Idle;
		mineLocs= minelocs;
		minedOut= minedout;
		occupation = Occupation.Miner;
	}
	
	/**
	 * Creates a new blueprint at the specified direction
	 * @param dir
	 * 	The direction from the worker
	 * @return
	 * 	A Blueprint class representing the blueprint
	 */
	public Unit layBlueprint(Direction dir, UnitType structureType) {
		previousState = state;
		
		// Check if the worker (occupantType) can lay the blueprint
		if (!gc.canBlueprint(id, structureType, dir)) {
			state = State.Idle;
			return null;
		}
		
		// lay the blueprint
		gc.blueprint(id, structureType, dir);

		// get the fields required to make the blueprint class
		MapLocation blueprintLocation = currentLocation.add(dir);
		Unit blueprint = gc.senseUnitAtLocation(blueprintLocation);
		
		// update the map about the blueprint location
		battleMap.updateOccupant(blueprintLocation, blueprint.id());

		// update the state
		state = State.Build;
		
		// return the newly created blueprint class
		return blueprint;
	}
	
	public void setBlueprint(AbstractStructure blueprint) {
		currentBlueprint = blueprint;
		previousState = state;
		state = State.Build;
	}
	
	/**
	 * Builds on a given blueprint
	 * @param blueprint
	 * 	Blueprint on which to build on
	 * @return
	 * 	0 if building, 1 if structure is finished building, 
	 * 	2 if failed to build
	 */
	public int build(AbstractStructure blueprint) {
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
		if (!gc.canBuild(id, currentBlueprint.id)) {
			
			// if structure got destroyed
			// another worker might have finished building the structure
			if (!gc.hasUnitAtLocation(currentBlueprint.currentLocation) ||
					gc.senseUnitAtLocation(currentBlueprint.currentLocation).unitType() != UnitType.Factory ||
					gc.unit(currentBlueprint.id).structureIsBuilt() == 1) {
				state = State.Idle;
				currentBlueprint = null;
				return 1;
			}
			return 2;
		}
		
		gc.build(id, currentBlueprint.id);
		
		// if the structure is completely built
		if (gc.unit(currentBlueprint.id).structureIsBuilt() == 1) {
			state = State.Idle;
			currentBlueprint.state = AbstractStructure.State.Idle;
			System.out.println("Structure built");
			currentBlueprint = null;
			return 1;
		}
		
		// if the structure is not done building
		state = State.Build;
		return 0;
	}
	
	/**
	 * Gets the worker to replicate
	 * @return 2 if it can't replicate at all, 1 if it just did
	 */
	 public int replicate(Direction dir) {
		 if(gc.canReplicate(id,dir)) {
			 gc.replicate(id, dir);
			 return 1;
		 }
		 
		 return 2;
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
	 public int mineArea() {
		 previousState = state;	
		 // update the State
		 state = State.Mine;

		 PlanetMap mapperino = gc.startingMap(gc.planet());

		 Direction[] dirs = Direction.values();

		 // checking if the tile isn't mined out, but the unit can't mine
		 // also determining the direction to mine in and updating the pointers
		 // to include mined out locations
		 int pointToMine=0;
			
		 for (int i =0;i<dirs.length;i++) {
			 // if worker is on cooldown
			 if (!(gc.canHarvest(id,dirs[i]))
					 && mapperino.onMap(currentLocation.add(dirs[i]))
					 && gc.karboniteAt(currentLocation.add(dirs[i])) > 0.) {
				 return -1;
			 // if worker can mine and there's karbonite at this point
			 } else if (mapperino.onMap(currentLocation.add(dirs[i]))
					 && gc.karboniteAt(currentLocation.add(dirs[i])) > 0.) {
				 pointToMine=i;
				 break;
			 // if worker can mine but no karbonite is available
			 } else {
				 if (mapperino.onMap(currentLocation.add(dirs[i]))) {
					 MapLocation mapLocation = currentLocation.add(dirs[i]);
					 Tuple location = new Tuple(
							 mapLocation.getX(), mapLocation.getY());
					 minedOut.add(location);
					 mineLocs.remove(location);
				 }
				 // if this is the last mine tile, then set to "done"
				 if (i==8){
					 MapLocation mapLocation = currentLocation;
					 Tuple location = new Tuple(
							 mapLocation.getX(), mapLocation.getY());
					 minedOut.add(location);
					 mineLocs.remove(location);
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
}
