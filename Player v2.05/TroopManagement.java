import bc.*;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Random;
import java.util.ArrayList;
import java.util.Collections;


/**
 * Class for all troop management
 * @author virsain
 *
 */
public class TroopManagement {
	// Stores all the troops in a hashMap where the unitId is the key
	HashMap<Integer, AbstractUnit> unitHashMap;
	Map battleMap;
	GameController gc;
	Random r;
	ArrayList<Tuple> mineLocs;
	ArrayList<Tuple> rocketLocations;
	ArrayList<Tuple> rocketLocsOnMars;
	HashSet<Tuple> minedOut;
	long initialWorkers;
	long replicateTurns = 45;
	Planet planet;
	
	ArrayList<MapLocation> enemyLocations;

	// list of all workers and structures that are alive
	ArrayList<AbstractUnit> workersAndStructs;
	
	// boolean array to see if the units in above ArrayList are alive or not 
	boolean[] workerAndStructAlive;
	
	// index for above array
	int wasaIndex;
	
	/**
	 * Creates a new TroopManagement class which 
	 * @param g
	 */
	TroopManagement(GameController g) {
		gc = g;
		r = new Random();
		
		planet = gc.planet();
		
		unitHashMap = new HashMap<Integer, AbstractUnit>();
		
		Team allyTeam = g.team();
		Team enemyTeam;
		if (allyTeam == Team.Red) enemyTeam = Team.Blue;
		else enemyTeam = Team.Red;

		battleMap = new BattleMap(gc.startingMap(planet),
	   		allyTeam, enemyTeam, gc);
		
		enemyLocations = battleMap.getEnemyLocations();
		
		mineLocs = battleMap.getKarboniteLocations();
		minedOut = new HashSet<Tuple>();
		workersAndStructs = new ArrayList<AbstractUnit>();
		rocketLocations = new ArrayList<Tuple>();
		rocketLocsOnMars = new ArrayList<Tuple>();
		workerAndStructAlive = new boolean[600];
		wasaIndex = 0;
	}
	
	/**
	 * Adds a unit to the hashMap
	 * @param unit
	 * 	The unit
	 */
	public void addUnit(Unit u) {
		AbstractUnit unit = null;
		
		switch(u.unitType()) {
			// make a worker a miner by default
			case Worker:
				unit = new Worker(u.id(), gc, battleMap, u.location().mapLocation(),
						mineLocs, minedOut, Worker.Occupation.Miner, unitHashMap, rocketLocations);
				addWorkerAndStructure(unit);
				break;
			case Knight:
				unit = new Knight(u.id(), gc, battleMap,
   		   		u.location().mapLocation(), unitHashMap);
				break;
			case Ranger:
				unit = new Ranger(u.id(), gc, battleMap,
   		   		u.location().mapLocation(), unitHashMap);
				break;
			case Mage:
				unit = new Mage(u.id(), gc, battleMap,
   		   		u.location().mapLocation(), unitHashMap);
				break;
			case Healer:
				unit = new Healer(u.id(), gc, battleMap,
   		   		u.location().mapLocation(), unitHashMap);
				break;
			case Factory:
				unit = new Factory(u.id(), gc, battleMap,
   		   		u.location().mapLocation());
				addWorkerAndStructure(unit);
				break;
			case Rocket:
				unit = new Rocket(u.id(), gc, battleMap,
   		   		u.location().mapLocation());
				addWorkerAndStructure(unit);
				break;
			default:
				return;
		}
		
		unitHashMap.put(unit.id, unit);
	}

	/**
	 * A strategy for having rockets launch. This should theoretically be called
	 * AFTER the rocket is fully loaded, and it looks for a valid location to
	 * land in.
	 * This doesn't handle rockets hitting other rockets by the way. Later on,
	 * it would be useful to create an "lz" function for units on Mars that can
	 * help rockets land in the right spots. Also, this function is not optimized
	 * for travel time.
	 */
	public void launchRocket(Rocket rocket){
		PlanetMap marsMap = gc.startingMap(Planet.Mars);
		long height = marsMap.getHeight();
		long width = marsMap.getWidth();
		for (long i = 0;i < height;i = i + (long)1){ //unfortunately this scales badly
			for (long j = 0;j < width;j = j + (long)3){
				Tuple tempLZtup = new Tuple((int)i,(int)j);
				MapLocation tempLZ = new MapLocation(Planet.Mars,(int)i,(int)j);
				// this guarantees that the location will be on the planet
				if (!(rocketLocsOnMars.contains(tempLZtup)))
				{
					rocket.setDestination(tempLZ);
					if (rocket.launch()==1){
						rocketLocsOnMars.add(tempLZtup);
						Tuple currentTup = new Tuple(rocket.currentLocation.getX(),rocket.currentLocation.getY());
						for (int k=0;k<rocketLocations.size();k++){
							if (currentTup.equals(rocketLocations.get(k))){
								rocketLocations.remove(k);
								break;
							}
						}
						// may consider disintegrating rockets as soon as they unload
						// later, but for now this is good
						return;
					}
				}
			}
		}
	}
	
	public void removeUnit(int id) {
		unitHashMap.remove(id);
	}
	
	/**
	 * If you want to add a miner worker, then just call add unit.  This method
	 * is exclusively for adding builders.
	 *
	 * @param u
	 * 	The worker unit
	 */
	public void addBuilder(Unit u) {
		AbstractUnit unit = new Worker(u.id(), gc, battleMap, u.location().mapLocation(),
				mineLocs, minedOut, Worker.Occupation.Builder, unitHashMap, rocketLocations);

		addWorkerAndStructure(unit);
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
	 * A strategy for worker building
	 */
	public void build(Worker worker, UnitType structureType) {
		replicateWorker(worker);
		
		switch(worker.state) {
			// keep building if state last turn was build
			case Build:
				worker.build();
				break;
			// keep moving if the state last turn was move
			case Move:
				// if there is a path
				if (worker.movePath != null) {
					
					// try moving
					int returnValue = worker.movePath(false);
					
					// if an obstacle is met
					if (returnValue == 4) {
						// check if the obstacle is an unbuilt factory (or if there
						// are any unbuilt factories nearby)
						Unit unit = worker.getNearbyUnbuiltStruct(2);
						
						if (unit != null) {
							// reset the worker's path
							worker.resetPath();
							
							// and have the worker begin building
							worker.setBlueprint((AbstractStructure) 
									getUnit(unit.id()));
							
							worker.build();
						}
					}
				// if no move path is set
				} else { 
					// randomly move
					worker.randomMovement();
				}
				break;
			
		   // if case is Mine, do whatever Idle tells you to do
			case Mine:
			
			// if you're done with moving or building then look at your state
			// before last turn and decide what to do
			case Idle:
				Unit structure = worker.getNearbyUnbuiltStruct();
				
				if (structure != null) {
					if (worker.setPath(worker.getLocation(), structure.location().
							mapLocation())) {
						worker.movePath(false);
						return;
					}
					
			      // if the path cannot be formed continue
				}
				
				// set a blueprint and begin building next turn
				// (setting a blueprint changes the worker's state to build,
				// so the worker will build next turn)
				if (worker.previousState == AbstractRobot.State.Move ||
						worker.previousState == AbstractRobot.State.Mine) {
				
					// get the direction to lay the blueprint
					Direction dir = Utility.getOppositeDirection(worker.moveDirection);
				
					// if there is an adjacent structure nearby, we do not
					// want to build because it will block our units
					if(worker.getNearbyStruct(worker.currentLocation.add(dir), 2) 
							!= null) {
						worker.randomMovement();
						return;
					}
					
					// lay the blueprint
					structure = worker.layBlueprint(dir, 
							structureType);
					
					// add the unit to the hashMap and set worker's blueprint to
					// the new blueprint so that it builds next turn
					if (structure != null) {
						addUnit(structure);
						worker.setBlueprint((AbstractStructure) 
								getUnit(structure.id()));
					
					// couldnt't lay the tile possibly because of lack of Karbonite
					} else {
						// try mining, if can't, move randomly
						if (worker.mine() == -1) {
							worker.randomMovement();
						} else {
							System.out.println("Builder is mining!");
						}
						
						return;
					}
				// worker's previous state is idle	
				} else {	
					worker.randomMovement();
				}
				break;
			default:
				return;
		}
	}
	
	/**
	 * A strategy for mining
	 */
	public void mine(Worker worker) {
		replicateWorker(worker);
		
		switch(worker.state){
			case Idle:
				// if no path is set
				if (worker.movePath == null) {
					
					// mix up the mining locations (so that different workers get
					// different locations)
   				Collections.shuffle(mineLocs);
   				for (int i = 0; i < worker.mineLocs.size(); i++) {
   					Tuple locTuple = worker.mineLocs.get(i);
   					MapLocation destination = new MapLocation(Planet.Earth, 
   							locTuple.x, locTuple.y);
   					
   					// try setting path, if path can be set break out of loop
   					if (worker.setPath(worker.currentLocation, destination)) {
   						break;
   					}
   				}
				}
				
				// worker needs to move
				worker.movePath(false);
	
				// we want the worker to move again in the next turn
				// (2 steps and mine is more efficient than 1 step and mine
				// because we are mining on a 3x3)
				worker.state = AbstractRobot.State.Move;
				break; 
			case Mine:
				worker.mine();
				break;
			case Move:
			 	worker.movePath(false);
	
			 	// we want the worker to begin mining
			 	worker.state = AbstractRobot.State.Mine;
				break;
			default:
		 		return;
		}
	}
	
	public void work(Worker worker) {
		replicateWorker(worker);
		
		// if the worker is right now a builder
		if (worker.occupation == Worker.Occupation.Builder) {
			switch(worker.state) {
				case Build:
					worker.build();
					return;
				case Move:
					// a path is set to a nearby structure
					if (worker.movePath != null) {
						
						// try moving
						int returnValue = worker.movePath(false);
						
						// if an obstacle is met
						if (returnValue == 4) {
							// check if the obstacle is an unbuilt factory (or if there
							// are any unbuilt factories nearby)
							Unit unit = worker.getNearbyUnbuiltStruct(2);
							
							if (unit != null) {
								// reset the worker's path
								worker.resetPath();
								
								// and have the worker begin building
								worker.setBlueprint((AbstractStructure) 
										getUnit(unit.id()));
								
								worker.build();
							}
						}
					// if no move path is set
					}
					return;
				default:
					break;
			}
		}
		
		// otherwise if the worker is a miner or an Idle Builder
	}

	/**
	 * A strategy for rocket unloading
	 */
	public void rocketUnload(Rocket rocket){
		ArrayList<Direction> dirs = battleMap.getPassableDirections(
				rocket.currentLocation);
		if (dirs.size() > 0) {
			rocket.setUnloadDir(Utility.getRandomDirection(dirs));
		}
		rocket.unload();
	}
	/**
	 * A strategy for factory producing
	 */
	public void produce(Factory factory, UnitType type) {
		Unit robot;
		
		switch(factory.state) {
			case Blueprint:
				break;
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
				
				// was able to unload the unit
				if (robot != null) {
					addUnit(robot);
				} else {
					ArrayList<Direction> dirs = battleMap.getPassableDirections(
							factory.getLocation());
					if (dirs.size() > 0) {
						factory.setUnloadDir(Utility.getRandomDirection(dirs));
					}
					
					// produce a bit
					factory.produce(type);
					// reset to unload so it can unload next turn
					factory.state = AbstractStructure.State.Unload;
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
	
	/**
	 * Produces only if the Karbonite is above a certain amount
	 */
	public void produceLess(Factory factory, UnitType type) {
		if (gc.karbonite() <= 120) return;
		
		produce(factory, type);
	}
	
	/**
	 * Strategy for troop rush
	 * @param robot
	 */
	public void goToEnemy(AbstractRobot robot) {
		// if the robot attacked, it shouldn't move because it can keep attacking 
		// next turn
		if (robot.attack() == 1) return;
		
		// first try moving toward the enemy
		if (robot.moveTowardsEnemy() == 1) {
			// do nothing and attack at the end
			
		// if you can't move towards the enemy then try moving along the path
		// toward the enemy starting location
		} else if (robot.movePath != null) {
			robot.movePath(true);
		
		// robot.movePath == null, try setting a path, otherwise move randomly
		} else {
			if (robot.setPathToEnemy()) {
				robot.movePath(true);
				return;
			}
			
			robot.randomMovement();
		}
		
		robot.attack();
	}
	
	/**
	 * Instructs the units to run to rocket
	 */
	public void evacuate(AbstractRobot robot) {
		// if the robot attacked, it shouldn't move because it can keep attacking 
		// next turn
		robot.attack();
			
		// first try moving toward the rocket
		if (robot.movePath != null) {
			// try moving
			int returnValue = robot.movePath(false);
			
			// if an obstacle is met
			if (returnValue == 4) {
				// check if the obstacle is an unbuilt factory (or if there
				// are any unbuilt factories nearby)
				Unit unit = gc.senseUnitAtLocation(robot.currentLocation.
						add(robot.moveDirection));
				
				if (unit.unitType() == UnitType.Rocket) {
					robot.load(unit.id());
				}
			}
		
		// robot.movePath == null, try setting a path, otherwise move randomly
		} else {
			if (rocketLocations.size() > 0) {
				Tuple d = robot.getClosestLocation(rocketLocations, robot.currentLocation);
				MapLocation dest = new MapLocation(planet, d.x, d.y);
						
   			if (robot.setPath(robot.currentLocation, dest)) {
   				robot.movePath(false);
   				return;
   			}
			}
			
			robot.randomMovement();
		}
	}
	
	public void initializeWorkers(GameController gc) {
   	// get the list of workers
   	VecUnit vecUnit = gc.myUnits();
   	initialWorkers = vecUnit.size();
   	
   	for (int i = 0; i < initialWorkers; i++) {
      	Unit u = vecUnit.get(i);
      	addBuilder(u);
      	Worker worker = (Worker) getUnit(u.id());
      	battleMap.updateOccupant(worker.getLocation(), worker.id);
      	
      	ArrayList<Direction> dirs = battleMap.
   				getPassableDirections(worker.getLocation());
      	
      	if (dirs.size() < 2) continue;
      	Direction dir = Utility.getRandomDirection(dirs);
			
			// lay the blueprint
			Unit factory = worker.layBlueprint(dir, UnitType.Factory);
			
			// add the unit to the hashMap and set worker's blueprint to
			// the new blueprint so that it builds next turn
			if (factory != null) {
				addUnit(factory);
				worker.setBlueprint((AbstractStructure) 
						getUnit(factory.id()));
			}
   	}
	} 
	
	/**
	 * Replicates a worker and adds the new worker to the HashMap
	 * @param worker
	 * 	The worker to be replicated
	 * @return
	 * 	-1 if its not time to replicate
	 * 	1 if replication was successful
	 * 	2 if replication is on cooldown
	 * 	3 if no passable directions
	 */
	public int replicateWorker(Worker worker) {
		if (!timeToReplicate()) return -1;
		
		int returnValue;
		ArrayList<Direction> dirs = worker.getPassableDirections();
		
		// no passable directions
		if (dirs.size() == 0) return 3;
		
		Direction dir = Utility.getRandomDirection(dirs);
		
   	if ((returnValue = worker.replicate(dir)) == 1) {
   		// get the replicated worker
   		Unit replicatedWorker = gc.senseUnitAtLocation(
   				worker.currentLocation.add(dir));
   		
   		// make the replicated worker's occupation opposite of the original
   		if (worker.occupation == Worker.Occupation.Builder) {
   			addUnit(replicatedWorker);
   		} else {
   			addBuilder(replicatedWorker);
   		}

   		battleMap.updateOccupant(worker.currentLocation.add(dir),
   				replicatedWorker.id());
   	}
   	
   	return returnValue;
	}

	/**
	 * Replicates a worker on mars and adds the new worker to the HashMap
	 * @param worker
	 * 	The worker to be replicated
	 * @return
	 * 	-1 if its not time to replicate
	 * 	1 if replication was successful
	 * 	2 if replication is on cooldown
	 * 	3 if no passable directions
	 */
	public int replicateWorkerMars(Worker worker) {

		int returnValue;
		ArrayList<Direction> dirs = worker.getPassableDirections();

		// no passable directions
		if (dirs.size() == 0) return 3;

		Direction dir = Utility.getRandomDirection(dirs);

		if ((returnValue = worker.replicate(dir)) == 1) {
			// get the replicated worker
			Unit replicatedWorker = gc.senseUnitAtLocation(
					worker.currentLocation.add(dir));

			// make the replicated worker's occupation opposite of the original
			if (worker.occupation == Worker.Occupation.Builder) {
				addUnit(replicatedWorker);
			} else {
				addBuilder(replicatedWorker);
			}

			battleMap.updateOccupant(worker.currentLocation.add(dir),
					replicatedWorker.id());
		}

		return returnValue;
	}



	/**
	 * Returns true if its time to replicate, false otherwise
	 */
	public boolean timeToReplicate() {
		if ((gc.round() + 30) % 100 == 0 || 
				gc.round() < (replicateTurns / initialWorkers) + 1) {
			return true;
		}
		
		return false;
	}
	
	/**
	 * Adds a worker or a structure to the ArrayList
	 * @param unit
	 * 	The unit to be added
	 */
	public void addWorkerAndStructure(AbstractUnit unit) {
		workersAndStructs.add(unit);
		workerAndStructAlive[wasaIndex++] = true;
	}
	
	/**
	 * Updates the worker or the structure's alive status in the ArrayList
	 * @param unit
	 * 	The unit to be updated
	 */
	public void updateWorkerAndStructure(AbstractUnit unit) {
		int index = workersAndStructs.indexOf(unit);
		workerAndStructAlive[index] = true;
	}
	
	/**
	 * Removes the dead workers and structures from the battleMap
	 */
	public void removeDeadWorkersAndStructures() {
		ArrayList<Integer> indices = new ArrayList<Integer>();
		AbstractUnit unit;
		
		// Can't directly remove from the workers and structures ArrayList 
		// because the index matching won't be true anymore
		// get the indices in reverse order, so removing from ArrayList
		// won't mess up earlier indices
		for (int i = wasaIndex - 1 ; i >= 0; i--) {
			if (workerAndStructAlive[i] == false) {
				indices.add(i);
			}
		}
		
		// remove from hashMap, battleMap and ArrayList
		for (int i : indices) {
			unit = workersAndStructs.get(i);
			
			// make sure no other unit came and took the place of this unit when
			// it died, otherwise that unit would be removed from the battleMap
			if (unit.id == battleMap.getOccupantId(unit.currentLocation)) {
				battleMap.updateOccupant(unit.currentLocation, -1);
			}
			
			System.out.println("Removed unit at " + unit.currentLocation);
			removeUnit(unit.id);
			
			// remove from rocketLocations if its a rocket
			if (unit.type == UnitType.Rocket) {
				Tuple locTuple = new Tuple(unit.currentLocation.getX(), unit.currentLocation.getY());
				System.out.println("Return value of removing from rocketLocations " + rocketLocations.remove(locTuple));
			}
			
			System.out.println("Return value of removing from workersAndStructs " + workersAndStructs.remove(i));
			wasaIndex--;
		}
		
		// reset the alive status for next turn
		for (int i = 0; i < wasaIndex; i++) {
			workerAndStructAlive[i] = false;
		}
	}
}
