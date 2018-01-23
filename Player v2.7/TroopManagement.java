import bc.*;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Random;
import java.util.ArrayList;


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
	ArrayList<Tuple> nonReservedMineLocs;
	ArrayList<Tuple> rocketLocsOnMars;
	ArrayList<Tuple> rocketLocations;
	HashSet<Tuple> minedOut;
	long initialWorkers;
	long replicateTurns = 45;
	Planet planet;
	UnitCounter unitCounter;

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
	TroopManagement(GameController g, UnitCounter uc) {
		gc = g;
		unitCounter = uc;
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
		nonReservedMineLocs = (ArrayList<Tuple>) mineLocs.clone();
		minedOut = new HashSet<Tuple>();
		workersAndStructs = new ArrayList<AbstractUnit>();
		rocketLocations = new ArrayList<Tuple>();
		workerAndStructAlive = new boolean[600];
		rocketLocsOnMars = new ArrayList<Tuple>();
		wasaIndex = 0;
	}

	public void mineMars(Worker worker) {
		MapLocation karbTile = null;
		long currentRound = gc.round();

		if (asteroidPattern.hasAsteroid(currentRound)&&gc.planet()==Planet.Mars){
			AsteroidStrike strike = asteroidPattern.asteroid(currentRound);
			MapLocation strikeLoc = strike.getLocation();
			Tuple strikeLocTup = new Tuple(strikeLoc.getX(),strikeLoc.getY());
			nonReservedMineLocs.add(strikeLocTup);
		}


		if (worker.movePath != null) {
			worker.movePath(false);
			return;
		}

		// otherwise if worker.movePath is null

		// if there is Karbonite on the tile the worker is on
		Tuple tupleCurrentLoc = new Tuple(worker.currentLocation.getX(), worker.currentLocation.getY());
		if (tupleCurrentLoc.equals(worker.mineDestination) && gc.karboniteAt(worker.currentLocation) > 0) {
			worker.mine();
			return;
		}

		// otherwise set a path to mine
		//karbTile = worker.getVisibleKarboniteLocation();

		// if there are no visible Karbonite locations
		// get the closest mining location
		if (nonReservedMineLocs.size() > 0) {
			Tuple locTuple = worker.getClosestLocation(nonReservedMineLocs, worker.currentLocation);
			worker.mineDestination = locTuple;
			nonReservedMineLocs.remove(locTuple);
			karbTile = new MapLocation(planet, locTuple.x, locTuple.y);
		}

		if (karbTile != null) {
			worker.setPath(worker.currentLocation, karbTile);
			worker.movePath(false);
			return;
		}

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

	public void removeUnit(int id) {
		unitHashMap.remove(id);
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
	 * A strategy for mining
	 */
	public void mine(Worker worker) {
		MapLocation karbTile = null;

		if (worker.movePath != null) {
			worker.movePath(false);
			return;
		}

		// otherwise if worker.movePath is null

		// if there is Karbonite on the tile the worker is on
		Tuple tupleCurrentLoc = new Tuple(worker.currentLocation.getX(), worker.currentLocation.getY());
		if (tupleCurrentLoc.equals(worker.mineDestination) && gc.karboniteAt(worker.currentLocation) > 0) {
			worker.mine();
			return;
		}

		// otherwise set a path to mine
		//karbTile = worker.getVisibleKarboniteLocation();

		// if there are no visible Karbonite locations
		// get the closest mining location
		if (nonReservedMineLocs.size() > 0) {
			Tuple locTuple = worker.getClosestLocation(nonReservedMineLocs, worker.currentLocation);
			worker.mineDestination = locTuple;
			nonReservedMineLocs.remove(locTuple);
			karbTile = new MapLocation(planet, locTuple.x, locTuple.y);
		}

		if (karbTile != null) {
			worker.setPath(worker.currentLocation, karbTile);
			worker.movePath(false);
			return;
		}

	}

	public void work(Worker worker, UnitType structureType) {
		if (worker.state == AbstractRobot.State.Garrison) {
			return;
		}

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
		Unit structure = worker.getNearbyUnbuiltStruct();

		if (structure != null) {
			if (worker.setPath(worker.getLocation(), structure.location().
					mapLocation())) {

				// add the mine location to the mineLocation ArrayList
				if(worker.mineDestination != null) {
					nonReservedMineLocs.add(worker.mineDestination);
				}
				// the worker is now a builder;
				worker.occupation = Worker.Occupation.Builder;
				worker.state = AbstractRobot.State.Move;
				work(worker, structureType);
				return;
			}

	      // if the path cannot be formed continue
		}

		// cost of blueprinting a factory or a rocket
		if (gc.karbonite() >= 100) {
			/*if (unitCounter.getCount(structureType) >= unitCounter.getCap(structureType)) {
				return;
			}*/

			// get a direction to blueprint
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

				// add the mine location to the mineLocation ArrayList
				if(worker.mineDestination != null) {
					nonReservedMineLocs.add(worker.mineDestination);
				}
				// the worker is now a builder;
				worker.occupation = Worker.Occupation.Builder;
				worker.resetPath();
				return;
			}
		}

		// if nothing else, just mine
		worker.occupation = Worker.Occupation.Miner;
		mine(worker);
	}

	/**
	 * Produces 2 rangers then 1 healer
	 * @param factory
	 * 	The factory which needs to produce
	 */
	public void produceDifferentUnits(Factory factory) {
		if (factory.factoryCount % 4 == 3) {
			produce(factory, UnitType.Healer);
		} else {
			produce(factory, UnitType.Ranger);
		}

	}

	/**
	 * A strategy for factory producing
	 */
	public void produce(Factory factory, UnitType type) {
		if (unitCounter.getCount(UnitType.Worker) <= (unitCounter.getCap(UnitType.Worker)/2)) {
			type = UnitType.Worker;
		}

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
		if (robot.state == AbstractRobot.State.Garrison) {
			return;
		}

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
   	int initialKarbonite = ((BattleMap) battleMap).totalKarbonite;

   	unitCounter.workerCap = 10 + (initialKarbonite / 1000);

   	for (int i = 0; i < initialWorkers; i++) {
      	Unit u = vecUnit.get(i);
      	addUnit(u);
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
   		addUnit(replicatedWorker);

   		battleMap.updateOccupant(worker.currentLocation.add(dir),
   				replicatedWorker.id());
   	}

   	return returnValue;
	}

	/**
	 * Returns true if its time to replicate, false otherwise
	 */
	public boolean timeToReplicate() {
		if (unitCounter.getCap(UnitType.Worker) > unitCounter.
				getCount(UnitType.Worker)) {

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
			if (unit.type == UnitType.Worker) {
				Worker worker = (Worker) unit;
				if (worker.mineDestination != null) {
					nonReservedMineLocs.add(worker.mineDestination);
				}
			} else if (unit.type == UnitType.Rocket) {
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

			addUnit(replicatedWorker);

			battleMap.updateOccupant(worker.currentLocation.add(dir),
					replicatedWorker.id());
		}

		return returnValue;
	}

	public void rocketUnload(Rocket rocket){
		// try unloading again
		Unit robot;
		robot = rocket.unload();

		// was able to unload the unit
		if (robot != null) {
			addUnit(robot);
		} else {
			ArrayList<Direction> dirs = battleMap.getPassableDirections(
					rocket.currentLocation);
			if (dirs.size() > 0) {
				rocket.setUnloadDir(Utility.getRandomDirection(dirs));
			}
		}
	}
}
