import bc.*;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Random;
import java.util.ArrayList;
import java.util.Collections;


/**
 * Class for all troop management
 * @author vineet
 *
 */
public class TroopManagement {
	// Stores all the troops in a hashMap where the unitId is the key
	HashMap<Integer, AbstractUnit> unitHashMap;
	Map earthBattleMap;
	GameController gc;
	Random r;
	ArrayList<Tuple> mineLocs;
	HashSet<Tuple> minedOut;
	long initialWorkers;
	MapLocation rocketBlueprint;
	boolean rocketDone = false;
	MapLocation rocketLZ;
	HashSet<MapLocation> rocketLocsOnMars = new HashSet();
	ArrayDeque<MapLocation> rocketSlots = new ArrayDeque();
	/**
	 * Creates a new TroopManagement class which
	 * @param g
	 */
	TroopManagement(GameController g) {
		gc = g;
		r = new Random();

		unitHashMap = new HashMap<Integer, AbstractUnit>();

		Team allyTeam = g.team();
		Team enemyTeam;
		if (allyTeam == Team.Red) enemyTeam = Team.Blue;
		else enemyTeam = Team.Red;

		earthBattleMap = new BattleMap(gc.startingMap(Planet.Earth),
	   		allyTeam, enemyTeam, gc);

		mineLocs = earthBattleMap.getKarboniteLocations();
		minedOut = new HashSet<Tuple>();
	}

	/**
	 * Adds a unit to the hashMap
	 * @param unit
	 * 	The unit
	 */
	public void addUnit(Unit u) {
		AbstractUnit unit = null;

		switch(u.unitType()) {
			// make a miner by default
			case Worker:
				unit = new Worker(u.id(), gc, earthBattleMap,
		   		u.location().mapLocation(), mineLocs, minedOut);
				break;
			case Knight:
				unit = new Knight(u.id(), gc, earthBattleMap,
   		   		u.location().mapLocation());
				break;
			case Ranger:
				unit = new Ranger(u.id(), gc, earthBattleMap,
   		   		u.location().mapLocation());
				break;
			case Mage:
				unit = new Mage(u.id(), gc, earthBattleMap,
   		   		u.location().mapLocation());
				break;
			case Healer:
				unit = new Healer(u.id(), gc, earthBattleMap,
   		   		u.location().mapLocation());
				break;
			case Factory:
				unit = new Factory(u.id(), gc, earthBattleMap,
   		   		u.location().mapLocation());
				break;
			default:
				return;
		}

		unitHashMap.put(unit.id, unit);
	}

	public void addWorker(Unit u, Worker.Occupation occupation) {
		AbstractUnit unit = null;

		switch (occupation) {
			case Builder:
				unit = new Worker(u.id(), gc, earthBattleMap,
   		   		u.location().mapLocation());
				break;
			case Miner:
				unit = new Worker(u.id(), gc, earthBattleMap,
   		   		u.location().mapLocation(), mineLocs, minedOut);
				break;
		}

		unitHashMap.put(unit.id, unit);
	}

	/**
	 * Gets a unit based on its id
	 * @param unitId
	 * 	The unit's id
	 * @return
	 * 	The unit
	 */
	public AbstractUnit getUnit(int unitId) {
		return unitHashMap.get(unitId);
	}

	/**
	 * A strategy for building a rocket with miners
	 */
	 public void buildRocket(Worker worker){

		 switch(worker.state) {
 			// keep building if state last turn was build
 			case Build:
 				worker.build();
 				break;
 			// keep moving if the state last turn was move
 			case Move:
 				if (worker.movePath != null) {
 					if (worker.movePath() == 3) {
 						Unit unit = getNearbyUnbuiltStruct(worker, 2);

 						if (unit != null) {
 							worker.movePath = null;
 							worker.moveIndex = 0;
 							worker.setBlueprint((AbstractStructure)
 									getUnit(unit.id()));

 							worker.build();

 						// the obstruction is a worker
 						} else if (gc.senseUnitAtLocation(worker.movePath[worker.
 						           moveIndex]).unitType() == UnitType.Worker) {
 							// make a new path
 							worker.setPath(worker.getLocation(),
 									worker.movePath[worker.movePath.length-1]);
 						}
 					}
 				// if no move path is set
 				} else {
 					// try moving, if no direction is set or obstruction is met
 					// then reset direction
 					int returnValue = worker.moveDir();
 					if (returnValue == -1 || returnValue == 3) {
 						randomMovement(worker);
 					}
 				}
 				break;
 			// if you're done with moving or building then look at your state
 			// before last turn and decide what to do
 			case Idle:
 				Unit rocket = getNearbyUnbuiltStruct(worker, gc.unit(worker.
 						id).visionRange());

 				if (rocket != null) {
 					if (worker.setPath(worker.getLocation(), rocket.location().
 							mapLocation())) {
 						worker.movePath();
 						return;
 					}

 			      // if the path cannot be formed continue
 				}

 				// set a blueprint and begin building next turn
 				// (setting a blueprint changes the worker's state to build,
 				// so the worker will build next turn)
 				if (worker.previousState == AbstractRobot.State.Move) {
 					// get the direction to lay the blueprint
 					Direction dir = getOppositeDirection(worker.moveDirection);

					// check if there's already a blueprint
					if (rocketBlueprint==null){
						rocket = worker.layBlueprint(dir,
	 							UnitType.Rocket); // lay the blueprint
						rocketBlueprint = worker.currentLocation.add(dir); // and set a loc
					} else { // there exists a blueprint in a location, so go there
						if (worker.setPath(worker.getLocation(),rocketBlueprint)) {
	 						worker.movePath();
	 						return;
	 					}
					}


 					// add the unit to the hashMap and set worker's blueprint to
 					// the new blueprint so that it builds next turn
 					if (rocket != null) {
 						addUnit(rocket);
 						worker.setBlueprint((AbstractStructure)
 								getUnit(rocket.id()));
 					}	else {
 						randomMovement(worker);
 					}
 				// worker's previous state is build
			} else if (worker.previousState == AbstractRobot.State.Build){
				// add rocket locs to deque
				Unit rockett = gc.senseUnitAtLocation(rocketBlueprint);
				for (int i = 0;i < (int)rockett.structureMaxCapacity();i++){
					rocketSlots.add(rocketBlueprint);
				}
				// reset rocket blueprint
				rocketBlueprint = null;

			} else { //worker's previous state was something that wasn't building or
			// moving
 					worker.previousState = worker.state;
 					// so that the worker stays still
 					worker.state = AbstractRobot.State.Idle;
 				}
 				break;
 			default:
 				return;
 		}
	 }

	 /**
	  * A strategy for having rockets load once they are built:
		* @return the result of loading units into the rockets
		* (tho this allows for many units around)
		*/
		public void loadRocket(Rocket rocket){
			previousState=state;
			if (state=State.Ready){ //we're done
				return
			}
			state=State.Load; // update state
			VecUnit nearbyUnits = gc.senseNearbyUnitsByTeam(currentLocation,1.0,allyTeam);
			for (int i = 0;i<nearbyUnits.size();i++){
				Unit unit = nearbyUnits.get(i*1.0); //get the unit
				int idOfNear = unit.id();
				if(rocket.load(idOfNear)==1){ // we just finished
					state = State.Ready;
					break;
				}
			}
		}

		/**
		 * A strategy for having rockets launch. This should theoretically be called
		 * AFTER the rocket is fully loaded, and it looks for a valid location to
		 * land in.
		 *
		 */
		public void launchRocket(Rocket rocket){
			PlanetMap marsMap = gc.startingMap(Planet.Mars);
			long height = marsMap.getHeight();
			long width = marsMap.getWidth();
			for (long i = 0.0;i<height;i=i+1.0){ //unfortunately this scales badly
				for (long j = 0.0;j<width;j=j+1.0){
					MapLocation tempLZ = MapLocation(Planet.Mars,(int)i,(int)j);
					// this guarantees that the location will be on the planet
					if (!(rocketLocsOnMars.contains(tempLZ))){
						if (rocket.launch(tempLZ){
							rocketLocsOnMars.add(tempLZ);
							// may consider disintegrating rockets as soon as they unload
							// later, but for now this is good
							return;
						}
					}
				}
			}
		}


	/**
	 * A strategy for worker building
	 */
	public void build(Worker worker) {
		if ((gc.round() + 30) % 100 == 0 || gc.round() < (9 / initialWorkers) + 1) {
			if (replicateWorker(worker) == 1) {
				return;
			}
		}

		switch(worker.state) {
			// keep building if state last turn was build
			case Build:
				worker.build();
				break;
			// keep moving if the state last turn was move
			case Move:
				if (worker.movePath != null) {
					if (worker.movePath() == 3) {
						Unit unit = getNearbyUnbuiltStruct(worker, 2);

						if (unit != null) {
							worker.movePath = null;
							worker.moveIndex = 0;
							worker.setBlueprint((AbstractStructure)
									getUnit(unit.id()));

							worker.build();

						// the obstruction is a worker
						} else if (gc.senseUnitAtLocation(worker.movePath[worker.
						           moveIndex]).unitType() == UnitType.Worker) {
							// make a new path
							worker.setPath(worker.getLocation(),
									worker.movePath[worker.movePath.length-1]);
						}
					}
				// if no move path is set
				} else {
					// try moving, if no direction is set or obstruction is met
					// then reset direction
					int returnValue = worker.moveDir();
					if (returnValue == -1 || returnValue == 3) {
						randomMovement(worker);
					}
				}
				break;
			// if you're done with moving or building then look at your state
			// before last turn and decide what to do
			case Idle:
				Unit factory = getNearbyUnbuiltStruct(worker, gc.unit(worker.
						id).visionRange());

				if (factory != null) {
					if (worker.setPath(worker.getLocation(), factory.location().
							mapLocation())) {
						worker.movePath();
						return;
					}

			      // if the path cannot be formed continue
				}

				// set a blueprint and begin building next turn
				// (setting a blueprint changes the worker's state to build,
				// so the worker will build next turn)
				if (worker.previousState == AbstractRobot.State.Move) {
					// get the direction to lay the blueprint
					Direction dir = getOppositeDirection(worker.moveDirection);

					// lay the blueprint
					factory = worker.layBlueprint(dir,
							UnitType.Factory);

					// add the unit to the hashMap and set worker's blueprint to
					// the new blueprint so that it builds next turn
					if (factory != null) {
						addUnit(factory);
						worker.setBlueprint((AbstractStructure)
								getUnit(factory.id()));
					}	else {
						randomMovement(worker);
					}
				// worker's previous state is idle
				} else {
					randomMovement(worker);
					// so that the worker moves again
					worker.state = AbstractRobot.State.Move;
				}
				break;
			default:
				return;
		}
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
	public Unit getNearbyUnbuiltStruct(AbstractRobot robot, long radius) {
		// only get the units that are within a unit box of the robot
		VecUnit unitList = gc.senseNearbyUnitsByTeam(robot.getLocation(),
				radius, earthBattleMap.getAllyTeamColor());

		for (int i = 0; i < unitList.size(); i++) {
			Unit unit = unitList.get(i);

			if (unit.unitType() == UnitType.Factory) {
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
	 * A strategy for factory producing
	 */
	public void produce(Factory factory, UnitType type) {
		Unit robot;

		switch(factory.state) {
			case Produce:
				/* factory finished producing last turn but hasn't
			 	* done anything this turn
			 	*/
				if (factory.produce(type) == 0)
					// call produce again so factory can unload
					produce(factory, type);
				break;
			case Bankrupt:
				// try producing again
				factory.produce(type);
				break;
			// tried to unload previously but still unable to unload
			case Unload:
				// try unloading again
				robot = factory.unload();

				// was able to produce the knight
				if (robot != null) {
					addUnit(robot);
				} else {
					ArrayList<Direction> dirs = earthBattleMap.getPassableDirections(
							factory.getLocation());
					if (dirs.size() > 0) {
						factory.setUnloadDir(getRandomDirection(dirs));
					}
				}
				break;
			case Idle:
				if (factory.previousState == Factory.State.Produce) {
					factory.state = Factory.State.Unload;
					produce(factory, type);
				// previous state was unloading or idle
				} else {
					factory.produce(type);
				}
				break;
			default:
				return;
		}
	}

	public void goToEnemy(AbstractRobot robot) {
		// if the robot attacked it shouldn't move because it can keep attacking
		// next turn
		if (robot.attack() == 1) return;

		switch (robot.state) {
			case Idle:
				if (robot.previousState == AbstractRobot.State.Idle) {
					if (setPathToEnemy(robot)) {
						robot.movePath();
					} else {
						robot.previousState = AbstractRobot.State.Idle;
						robot.state = AbstractRobot.State.Move;
						goToEnemy(robot);
					}
				} else if (robot.previousState == AbstractRobot.State.Move) {
					robot.previousState = AbstractRobot.State.Idle;
					robot.state = AbstractRobot.State.Move;
					goToEnemy(robot);
				}
				break;
			case Move:
				if (robot.movePath != null) {
					robot.movePath();
				} else {
					Direction dir = robot.enemyDirection();
					if (dir == null) {
						randomMovement(robot);
					} else {
						robot.setDirection(dir);
						robot.moveDir();
					}
				}
				break;
			default:
				break;
		}
		robot.attack();
	}

	/**
	 * Sets the robot's path to one of the enemy spawining locations
	 */
	public boolean setPathToEnemy(AbstractRobot robot) {
		// get the starting locations of the enemy
		ArrayList<MapLocation> enemyLocations =
				earthBattleMap.getEnemyLocations();

		if (robot.setPath(robot.getLocation(), enemyLocations
				.get(r.nextInt(enemyLocations.size())))) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * Strategy for having the troops stand around base in a wall-like fashion
	 */
	public void troopWall(AbstractRobot robot) {
		robot.attack();
		switch(robot.state) {
			case Idle:
				if (robot.previousState == AbstractRobot.State.Idle) {
					ArrayList<Direction> dirs = earthBattleMap.
							getPassableDirections(robot.getLocation());
					robot.setDirection(getRandomDirection(dirs));
					robot.moveDir();
				} else {
					robot.idle();
				}
				break;
			case Move:
				ArrayList<Direction> dirs = earthBattleMap.
						getPassableDirections(robot.getLocation());
				robot.setDirection(getRandomDirection(dirs));
				robot.moveDir();
				break;
			default:
				robot.idle();
		}
		robot.attack();
	}

	/**
	 * Strategy for managing troops
	 */
	public void manageTroop(AbstractRobot robot) {
		// attack!!!
		robot.attack();

		// get the direction the enemy is in
		Direction dir = robot.enemyDirection();

		if (dir == null) {
			randomMovement(robot);
		// if dir is center don't move
		} else if (dir != Direction.Center) {
			robot.setDirection(dir);
			robot.moveDir();
		}

		// try attacking again in case attacking wasn't possible the first time
		robot.attack();
	}

	public void initializeWorkers(GameController gc) {
   	// get the list of workers
   	VecUnit vecUnit = gc.myUnits();
   	initialWorkers = vecUnit.size();

   	for (int i = 0; i < initialWorkers; i++) {
      	Unit u = vecUnit.get(i);
      	addWorker(u, Worker.Occupation.Builder);
      	Worker worker = (Worker) getUnit(u.id());
      	earthBattleMap.updateOccupant(worker.getLocation(), worker.id);

      	replicateWorker(worker);
   	}
	}

	public int replicateWorker(Worker worker) {
		int returnValue;
		ArrayList<Direction> dirs = earthBattleMap.
				getPassableDirections(worker.getLocation());

		if (dirs.size() == 0) return -1;

		Direction dir = getRandomDirection(dirs);

   	if ((returnValue = worker.replicate(dir)) == 1) {
   		// get the replicated worker
   		Unit replicatedWorker = gc.senseUnitAtLocation(
   				worker.getLocation().add(dir));

   		// make the replicated worker's occupation opposite of the original
   		if (worker.occupation == Worker.Occupation.Builder) {
   			addWorker(replicatedWorker, Worker.Occupation.Miner);
   		} else {
   			addWorker(replicatedWorker, Worker.Occupation.Builder);
   		}

   		earthBattleMap.updateOccupant(worker.getLocation().add(dir),
   				replicatedWorker.id());
   	}

   	return returnValue;
	}

	/**
	 * A strategy for mining
	 */
	public void mine(Worker worker) {
		if ((gc.round() + 30) % 100 == 0 || gc.round() < (9 / initialWorkers) + 1) {
			if (replicateWorker(worker) == 1) {
				return;
			}
		}

		switch(worker.state){
			case Idle:
				if (worker.previousState == AbstractRobot.State.Move) {
					worker.previousState = AbstractRobot.State.Idle;
   				worker.state = AbstractRobot.State.Mine;
   				mine(worker);
				} else {
   				if (worker.movePath == null) {

   					Collections.shuffle(mineLocs);
   					for (int i = 0; i < worker.mineLocs.size(); i++) {
   						Tuple location = worker.mineLocs.get(i);
   						MapLocation destination = new MapLocation(Planet.Earth, location.x, location.y);
   						if (worker.setPath(worker.getLocation(), destination)) {
   							System.out.println("DESTINATION " + destination.getX() + ", " + destination.getY());
   							worker.previousState = AbstractRobot.State.Idle;
   							worker.state = AbstractRobot.State.Move;
   							break;
   						}
   					}
   				}
   				worker.previousState = AbstractRobot.State.Idle;
   				worker.state = AbstractRobot.State.Move;
   				mine(worker);
				}
				break;
			case Mine:
				worker.mineArea();
				break;
			case Move:
			 	worker.movePath();
			 	worker.previousState = AbstractRobot.State.Move;
			 	worker.state = AbstractRobot.State.Mine;
				break;
			default:
		 		return;
		}
	}

	/**
	 * Strategy for randomMovement for a knight
	 */
	public int randomMovement(AbstractRobot robot) {
		ArrayList<Direction> dirs = earthBattleMap.
				getPassableDirections(robot.getLocation());
		if (dirs.size() > 0) {
			robot.setDirection(getRandomDirection(dirs));
			return robot.moveDir();
		}

		return -1;
	}

	public Direction getOppositeDirection(Direction dir) {
		switch (dir) {
			case South:
				return Direction.North;
			case Southwest:
				return Direction.Northeast;
			case Southeast:
				return Direction.Northwest;
			case North:
				return Direction.South;
			case Northwest:
				return Direction.Southeast;
			case Northeast:
				return Direction.Southwest;
			case West:
				return Direction.East;
			default:
				return Direction.West;
		}
	}

	public Direction getRandomDirection(ArrayList<Direction> dirs) {
		Random r = new Random();

		return dirs.get(r.nextInt(dirs.size()));
	}
}
