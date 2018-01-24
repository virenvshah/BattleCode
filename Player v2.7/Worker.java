import bc.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

/**
 * Represents a worker robot in the game
 * @author virsain
 */
public class Worker extends AbstractRobot {
	int range[] = {2, 8, 18, 32, 50}; 
	
	public enum Occupation { Miner, Builder; };
	
	// the blueprint which the worker is currently working on
	public AbstractStructure currentBlueprint; 
	
	// the deque of things to mine
	public ArrayList<Tuple> mineLocs;
	public ArrayList<Tuple> rocketLocations;
	// the set of things to not mine
	public HashSet<Tuple> minedOut;
	public Occupation occupation;
	public int miningIndex;
	public int replicateTurns;
	public Tuple mineDestination;
	
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
	 * @param occupation
	 * 	Whether the worker is a builder or a miner
	 */
	public Worker(int i, GameController g, Map map, MapLocation location, 
			ArrayList<Tuple> minelocs, HashSet<Tuple> minedout, Occupation occ, 
			HashMap<Integer, AbstractUnit> hashMap, ArrayList<Tuple> rocketLocs) {
		
		super(i, g, map, location, UnitType.Worker, hashMap);
		state = State.Idle;
		previousState = State.Idle;
		mineLocs= minelocs;
		minedOut= minedout;
		occupation = occ;
		rocketLocations = rocketLocs;
		moveDirection = Direction.North;
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
			// since the worker isn't doing anything its in an idle state
			state = State.Idle;
			return null;
		}
		
		// lay the blueprint
		gc.blueprint(id, structureType, dir);

		// get the fields required to update the battleMap
		MapLocation blueprintLocation = currentLocation.add(dir);
		Unit blueprint = gc.senseUnitAtLocation(blueprintLocation);
		
		// update the map about the blueprint location
		battleMap.updateOccupant(blueprintLocation, blueprint.id());

		// update the state
		state = State.Build;
		
		// return the unit laid
		return blueprint;
	}
	
	public void setBlueprint(AbstractStructure blueprint) {
		currentBlueprint = blueprint;
		
		// the builder is now in a building state
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
			
			// if structure got destroyed and there's no unit left 
			// or another unit took its place
			// if another worker might have finished building the structure
			if (!gc.hasUnitAtLocation(currentBlueprint.currentLocation) ||
					gc.senseUnitAtLocation(currentBlueprint.currentLocation).unitType() 
					!= UnitType.Factory ||
					gc.unit(currentBlueprint.id).structureIsBuilt() == 1) {
				
				state = State.Idle;	
				currentBlueprint.state = AbstractStructure.State.Idle;
				currentBlueprint = null;
				return 1;
			}
			
			// otherwise don't change state, just chill
			return 2;
		}
		
		gc.build(id, currentBlueprint.id);
		
		// if the structure is completely built
		Unit structure = gc.unit(currentBlueprint.id);
		if (structure.structureIsBuilt() == 1) {
			state = State.Idle;
			currentBlueprint.state = AbstractStructure.State.Idle;
			System.out.println("Structure built");
			
			if (structure.unitType() == UnitType.Rocket) {
				MapLocation location = currentBlueprint.currentLocation;
				Tuple locTuple = new Tuple(location.getX(), location.getY());
				rocketLocations.add(locTuple);
				
				if (gc.canLoad(structure.id(), id)) {
					gc.load(structure.id(), id);
					resetPath();
					state = State.Garrison;
				}
			}
			
			currentBlueprint = null;
			
			return 1;
		}
		
		// if the structure is not done building
		state = State.Build;
		return 0;
	}
	 
	/** 
	 * Mines out a 3x3 square around the worker:
	 * @return
	 * 	-1 if unable to mine
	 * 	0 if successfully mined
	 * 	1 if successfully mined out a whole tile
	 */
	public int mine() {
		previousState = state;	
		// update the State

		if (gc.canHarvest(id, Direction.Center)) {
			gc.harvest(id, Direction.Center);
			
			if (gc.karboniteAt(currentLocation) == 0) {
				mineDestination = null;
			}
			return 1;	
		}
		
		return -1;
	}
	
	/**
	 * Returns the closest visible Karbonite location
	 */
	public MapLocation getVisibleKarboniteLocation() {
		VecMapLocation mapLocations;
		long visionRange = gc.unit(id).visionRange();
		
		for (int i = 0; i < range.length; i++) {
			if (range[i] > visionRange) break;
		
			mapLocations = gc.allLocationsWithin(currentLocation, range[i]);
		
			for (int j = 0; j < mapLocations.size(); j++) {
				MapLocation location = mapLocations.get(j);
				
				if (gc.karboniteAt(location) > 0) {
					return location;
				}
			}
		}
		
		return null;
	}
	
	/**
	 * Returns the nearby unbuilt structures for a radius = visionRange 
	 */
	public Unit getNearbyUnbuiltStruct() {
		return getNearbyUnbuiltStruct(gc.unit(id).visionRange());
	}
	
	
	/**
	 * Gets the id of adjacent unbuilt factories
	 * 
	 * @param robot
	 * 	The robot who wants the direction
	 * @param radius
	 * 	The radius in square units
	 * @return
	 * 	Null if no adjacent factories otherwise the direction to factory
	 */
	public Unit getNearbyUnbuiltStruct(long radius) {
		// only get the units that are within a unit box of the robot
		VecUnit unitList = gc.senseNearbyUnitsByTeam(currentLocation, 
				radius, battleMap.getAllyTeamColor());
		
		for (int i = 0; i < unitList.size(); i++) {
			Unit unit = unitList.get(i);
			
			if (unit.unitType() == UnitType.Factory || unit.unitType()
					== UnitType.Rocket) {
				// return the direction to the factory from the robot
				if (unit.structureIsBuilt() != 1) {
					return unit;
				}
			}
		}
		
		// no unbuilt factories :(
		return null;
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
}
