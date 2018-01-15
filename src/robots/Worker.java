import bc.*;

/**
 * Represents a worker robot in the game
 * @author virsain
 */
public class Worker extends AbstractRobot {
	// the blueprint which the worker is currently working on
	public AbstractStructure currentBlueprint; 
	
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
	 */
	public Worker(int i, GameController g, Map map, MapLocation location) {
		super(i, g, map, location, UnitType.Worker);
		state = State.Idle;
		previousState = State.Idle;
	}
	
	/**
	 * Creates a new blueprint at the specified direction
	 * @param dir
	 * 	The direction from the worker
	 * @return
	 * 	A Blueprint class representing the blueprint
	 */
	public AbstractStructure setBlueprint(Direction dir, UnitType structureType) {
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
		int blueprintId = gc.senseUnitAtLocation(blueprintLocation).id();
		
		// update the map about the blueprint location
		battleMap.updateOccupant(blueprintLocation, structureType);

		// update the state
		state = State.Build;
		
		if (structureType == UnitType.Factory)
			currentBlueprint = new Factory(blueprintId, gc, battleMap, blueprintLocation);
		
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
		if (!gc.canBuild(id, currentBlueprint.id)) return 2;
		
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
}
