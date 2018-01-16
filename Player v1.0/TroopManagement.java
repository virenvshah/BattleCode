import bc.*;
import java.util.HashMap;
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
					worker.setDirection(getRandomDirection(dirs));
					worker.moveDir();
				} else if (returnValue == 3) {
					ArrayList<Direction> dirs = earthBattleMap.
							getPassableDirections(worker.getLocation());
					if (!dirs.contains(worker.moveDirection)) {
						worker.setDirection(getRandomDirection(dirs));
						worker.moveDir();
					}
				}
				break;
			// if you're done with moving or building then look at your state
			// before last turn and decide what to do
			case Idle:
				// if the state before last turn was build, then move
				if (worker.previousState == Worker.State.Build) {
					worker.state = AbstractRobot.State.Move;
					worker.previousState = AbstractRobot.State.Move;
					build(worker);	
				// otherwise set a blueprint and begin building next turn
				// (setting a blueprint changes the worker's state to build,
				// so the worker will build next turn)
				} else if (worker.previousState == Worker.State.Move) {
System.out.println("Trying to build");
					Direction dir = getOppositeDirection(worker.moveDirection);
					AbstractStructure factory = worker.setBlueprint(dir, 
							UnitType.Factory);
System.out.println(factory);
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
					factory.setUnloadDir(getRandomDirection(dirs));
				}
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
						randomMovement(robot);
					}
				} else if (robot.previousState == AbstractRobot.State.Move) {
					randomMovement(robot);
				}
				break;
			case Move:
				if (robot.movePath != null) {
					robot.movePath();
				} else {
					randomMovement(robot);
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
	
	/**
	 * Strategy for randomMovement for a knight
	 */
	public int randomMovement(AbstractRobot robot) {
		Direction dir;
		Random r = new Random();
		int randomInt = r.nextInt(8);
		
		switch (randomInt) {
			case 0:
				dir = Direction.North;
				break;
			case 1:
				dir = Direction.Northeast;
				break;
			case 2:
				dir = Direction.Northwest;
				break;
			case 3:
				dir = Direction.South;
				break;
			case 4:
				dir = Direction.Southeast;
				break;
			case 5:
				dir = Direction.Southwest;
				break;
			case 6:
				dir = Direction.East;
				break;
			default:
				dir = Direction.West;
		}
		
		robot.setDirection(dir);
		return robot.moveDir();
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
