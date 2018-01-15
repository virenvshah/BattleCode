import bc.*;
import java.util.*;
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

		 Direction[] dirs = Direction.values();

		// checking if the tile isn't mined out, but the unit can't mine
		// also determining the direction to mine in and updating the pointers
		// to include mined out locations
		int pointToMine=0;
 		for (int i =0;i<dirs.length;i++){
			// if worker is on cooldown
			 if(!(gc.canHarvest(id,dirs[i]))
			 && gc.karboniteAt(currentLocation.add(dirs[i])) > 0.){
				 System.out.println("Can't mine rn");return -1;
			 }
			 // if worker can mine and there's karbonite at this point
			 else if (gc.karboniteAt(currentLocation.add(dirs[i])) > 0.){
				 pointToMine=i;
				 switch(pointToMine){
					 case 0:
					 System.out.println("Mining center");break;
					 case 1:
					 System.out.println("Mining east");break;
					 case 2:
					 System.out.println("Mining north");break;
					 case 3:
					 System.out.println("Mining NE");break;
					 case 4:
					 System.out.println("Mining NW");break;
					 case 5:
					 System.out.println("Mining south");break;
					 case 6:
					 System.out.println("Mining SE");break;
					 case 7:
					 System.out.println("Mining SW");break;
					 case 8:
					 System.out.println("Mining west");break;
				 }
				 break;
			 }
			 // if worker can mine but no carbonite is available
			 else {
				 minedOut.add(currentLocation.add(dirs[i]));
				 switch(i){
					 case 0:
					 System.out.println("Done with center");break;
					 case 1:
					 System.out.println("Done with east");break;
					 case 2:
					 System.out.println("Done with north");break;
					 case 3:
					 System.out.println("Done with NE");break;
					 case 4:
					 System.out.println("Done with NW");break;
					 case 5:
					 System.out.println("Done with south");break;
					 case 6:
					 System.out.println("Done with SE");break;
					 case 7:
					 System.out.println("Done with SW");break;
					 case 8:
					 System.out.println("Done with west");break;
				 }
				 // if this is the last mine tile, then set to "done"
				 if (i==8){
					 System.out.println("Done with this area");
					 state = State.Idle;
					 pointToMine = 9;
				 }
			 }
		}

		// actually harvest the thing
		if (pointToMine>=0 && pointToMine<9){
			System.out.println(gc.canHarvest(id,dirs[pointToMine]));
			System.out.println("Harvesting");
			gc.harvest(id,dirs[pointToMine]);
			System.out.println("Harvested");
		}
		System.out.println("Karbonite pool at "+gc.karbonite());

		// return current progress
		System.out.println("Current progress index: " + pointToMine);
		return pointToMine;
	 }


}
