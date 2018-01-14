import bc.*;
import java.util.HashMap;
import java.util.Random;

/**
 * A player that builds factories
 * @author virsain
 *
 */
public class FactoryUnloadingPlayer {
	// The hashMap where all units are stored, the key is their id
	HashMap<Integer, AbstractUnit> unitHashMap;
	GameController gc;
	BattleMap earthBattleMap;
	
	public static void main(String args[]) {
		FactoryUnloadingPlayer fbp = new FactoryUnloadingPlayer();
		fbp.gc = new GameController();
		
		// get the team colors
		Team allyTeam = fbp.gc.team();
		Team enemyTeam;
		if (allyTeam == Team.Red) enemyTeam = Team.Blue;
		else enemyTeam = Team.Red;
		
		
		// get the API specific planet Map
	   PlanetMap earthPlanetMap = fbp.gc.startingMap(Planet.Earth);
	   
	   // make our own map from the API specific map
	   fbp.earthBattleMap = new BattleMap(earthPlanetMap, 
	   		allyTeam, enemyTeam);
	   
	   // create the hashMap where all the units are stored
	   fbp.unitHashMap = new HashMap<Integer, AbstractUnit>();
	   
	   
	   while (true) {
	   	System.out.println("Current Round: " + fbp.gc.round());
	   	/* if an exception occurs we don't want the program to crash because
	   	 * we will lose the game
	   	 */
	   	try {
	   		// get the list of units every turn
	   		VecUnit vecUnit = fbp.gc.myUnits();
	   		
	   		// iterate through the list of units
	      	for (int i = 0; i < vecUnit.size(); i++) {
	      		Unit unit = vecUnit.get(i);
	      		
	      		// check the time of unit
	      		if (unit.unitType() == UnitType.Worker) {
	      			Worker worker = (Worker) fbp.unitHashMap.get(unit.id());
	      			
	      			// if the worker isn't stored in the hashMap create a new one
	      			if (worker == null) {
	      				worker = new Worker(unit.id(), fbp.gc, fbp.earthBattleMap, 
	         		   		unit.location().mapLocation());
	      				fbp.unitHashMap.put(worker.id, worker);
	      			}
	      		
	      			fbp.build(worker);
	      		} else if (unit.unitType() == UnitType.Factory) {
	      			Factory factory = (Factory) fbp.unitHashMap.get(unit.id());
	      			
	      			/* if the factory isn't stored in the hashMap then there is 
	      			 * a bug because the worker should have added the factory 
	      			 * to the hashMap after creating it.
	      			 */
	      			if (factory == null) {
	      				System.out.println("Factory is null");
	      				System.exit(0);
	      			}
	      			
	      			fbp.produce(factory);
	      		} else if (unit.unitType() == UnitType.Knight) {
	      			Knight knight = (Knight) fbp.unitHashMap.get(unit.id());
	      			
	      			/* if the knight isn't stored in the hashMap then it means
	      			 * it hasn't been unloaded yet because the factory only adds 
	      			 * a unit to the hashMap after unloading it.  Hence the loop
	      			 * should just move on to the next unit.
	      			 */
	      			if (knight == null) {
	      				System.out.println("Knight is null");
	      				continue;
	      			}

	      			fbp.manageTroop(knight);
	      		}
	      	}
	   	} catch (Exception e) {
	   		System.out.println("Exception was thrown :(");
	   		e.printStackTrace();
	   	}
	   	
	   	fbp.gc.nextTurn();
	   }
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
				worker.move(Direction.East);
				break;
			// if you're done with moving or building then look at your state
			// before last turn and decide what to do
			case Idle:
				// if the state before last turn was build, then move
				if (worker.previousState == Worker.State.Build) {
					worker.move(Direction.East);	
				// otherwise set a blueprint and begin building next turn
				// (setting a blueprint changes the worker's state to build,
				// so the worker will build next turn)
				} else {
					AbstractStructure factory = worker.setBlueprint(Direction.West, 
							UnitType.Factory);
					if (factory != null) {
						unitHashMap.put(factory.id, factory);
					}
				}
				break;
			default:
				return;
		}
	}
	
	/**
	 * A strategy for factory producing
	 */
	public void produce(Factory factory) {
		AbstractRobot robot;
		
		switch(factory.state) {
			case Produce:
				/* factory finished producing last turn but hasn't 
			 	* done anything this turn
			 	*/
				if (factory.produce(UnitType.Knight) == 0)
					// call produce again so factory can unload
					produce(factory);
				break;
			case Bankrupt:
				// try producing again
				factory.produce(UnitType.Knight);
				break;
			// tried to unload previously but still unable to unload
			case Unload:
				// try unloading again
				robot = factory.unload(Direction.North);
				
				// was able to produce the knight
				if (robot != null) {
					unitHashMap.put(robot.id, robot);
				}
			case Idle:
				if (factory.previousState == Factory.State.Produce) {
					robot = factory.unload(Direction.North);
					
					// was able to produce the knight
					if (robot != null) {
						unitHashMap.put(robot.id, robot);
					}
				// previous state was unloading or idle
				} else {
					factory.produce(UnitType.Knight);
				}
				break;
			default:
				return;
		}
	}
	
	/**
	 * Strategy for managing troops
	 */
	public void manageTroop(AbstractRobot robot) {
		Knight knight = (Knight) robot;
		
		// attack!!!
		robot.ability();
		
		// get the direction the enemy is in
		Direction dir = knight.enemyDirection();
		
		if (dir == null) {
			randomMovement(knight);
		// if dir is center don't move
		} else if (dir != Direction.Center) {
			System.out.println("TRYING TO MOVE");
			System.out.println(knight.move(dir));
		}
		
		// try attacking again in case attacking wasn't possible the first time
		robot.ability();
	}
	
	/**
	 * Strategy for randomMovement for a knight
	 */
	public int randomMovement(Knight knight) {
		Direction dir;
		Random r = new Random();
		int randomInt = r.nextInt(16);
		
		if (randomInt < 3) {
			dir = Direction.North;
		} else if (randomInt < 8) {
			dir = Direction.Northeast;
		} else if (randomInt < 10) {
			dir = Direction.East;
		} else if (randomInt < 11) {
			dir = Direction.Southeast;
		} else if (randomInt < 12) {
			dir = Direction.South;
		} else if (randomInt < 13) {
			dir = Direction.Southwest;
		} else if (randomInt < 14) {
			dir = Direction.West;
		} else {
			dir = Direction.Northwest;
		}
		
		return knight.move(dir);
	}
}

