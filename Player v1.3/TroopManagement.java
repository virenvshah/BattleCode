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
	Map earthBattleMap;
	GameController gc;
	Random r;
	ArrayList<Tuple> mineLocs;
	HashSet<Tuple> minedOut;
	
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
			case Worker:
				unit = new Worker(u.id(), gc, earthBattleMap, 
		   		u.location().mapLocation());
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
	 * A strategy for worker building
	 */
	public void build(Worker worker) {
		if (gc.round() % 60 == 0) {
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
				// try moving, if no direction is set or obstruction is met
				// then reset direction
				int returnValue = worker.moveDir();
				if (returnValue == -1) {
					ArrayList<Direction> dirs = earthBattleMap.
							getPassableDirections(worker.getLocation());
					if (dirs.size() > 0) {
						worker.setDirection(getRandomDirection(dirs));
						worker.moveDir();
					}
				} else if (returnValue == 3) {
					ArrayList<Direction> dirs = earthBattleMap.
							getPassableDirections(worker.getLocation());
					if (!dirs.contains(worker.moveDirection) && dirs.size() > 0) {
						worker.setDirection(getRandomDirection(dirs));
						worker.moveDir();
					}
				}
				break;
			// if you're done with moving or building then look at your state
			// before last turn and decide what to do
			case Idle:
				// if the state before last turn was build, then move
				if (worker.previousState == AbstractRobot.State.Build) {
					worker.state = AbstractRobot.State.Move;
					worker.previousState = AbstractRobot.State.Move;
					build(worker);	
				// otherwise set a blueprint and begin building next turn
				// (setting a blueprint changes the worker's state to build,
				// so the worker will build next turn)
				} else if (worker.previousState == AbstractRobot.State.Move) {
					Direction dir = getOppositeDirection(worker.moveDirection);
					AbstractStructure factory = worker.setBlueprint(dir, 
							UnitType.Factory);
					if (factory != null) {
						unitHashMap.put(factory.id, factory);
					}
				} else {
					worker.state = AbstractRobot.State.Move;
					worker.previousState = AbstractRobot.State.Move;
					build(worker);	
				}
				break;
			default:
				return;
		}
	}
	
	/**
	 * A strategy for factory producing
	 */
	public void produce(Factory factory, UnitType type) {
		if (gc.round() % 150 < 7) {
			type = UnitType.Worker;
		}
		
		AbstractRobot robot;
		
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
					unitHashMap.put(robot.id, robot);
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
		robot.attack();
		switch (robot.state) {
			case Idle:
				if (robot.previousState == AbstractRobot.State.Idle) {
					ArrayList<MapLocation> enemyLocations = 
							earthBattleMap.getEnemyLocations();
					if (robot.setPath(robot.getLocation(), enemyLocations
							.get(r.nextInt(enemyLocations.size())))) {
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
   	
   	for (int i = 0; i < vecUnit.size(); i++) {
      	Unit u = vecUnit.get(i);
      	addUnit(u);
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
   		Unit replicatedWorker = gc.senseUnitAtLocation(
   				worker.getLocation().add(dir));
   		addWorker(replicatedWorker, Worker.Occupation.Miner);
   		earthBattleMap.updateOccupant(worker.getLocation().add(dir), 
   				replicatedWorker.id());
   	}
   	
   	return returnValue;
	}
	
	/**
	 * A strategy for mining
	 */
	public void mine(Worker worker) {
		if (gc.round() % 60 == 0) {
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
