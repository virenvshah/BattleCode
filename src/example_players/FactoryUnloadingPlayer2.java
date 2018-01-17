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
					worker.move(Direction.West);
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
	      			
	      			fbp.produce(factory, UnitType.Ranger);
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
	      		else if (unit.unitType() == UnitType.Ranger) {
	      			Ranger ranger = (Ranger) fbp.unitHashMap.get(unit.id());
	      			
	      			/* if the ranger isn't stored in the hashMap then it means
	      			 * it hasn't been unloaded yet because the factory only adds 
	      			 * a unit to the hashMap after unloading it.  Hence the loop
	      			 * should just move on to the next unit.
	      			 */
	      			if (ranger == null) {
	      				System.out.println("Ranger is null");
	      				continue;
	      			}

	      			fbp.manageTroop(ranger);
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
				worker.move(Direction.West);
				break;
			// if you're done with moving or building then look at your state
			// before last turn and decide what to do
			case Idle:
				// if the state before last turn was build, then move
				if (worker.previousState == Worker.State.Build) {
					worker.move(Direction.West);	
				// otherwise set a blueprint and begin building next turn
				// (setting a blueprint changes the worker's state to build,
				// so the worker will build next turn)
				} else {
					AbstractStructure factory = worker.setBlueprint(Direction.East, 
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
				robot = factory.unload(Direction.South);
				
				// was able to produce the robot
				if (robot != null) {
					unitHashMap.put(robot.id, robot);
				}
			case Idle:
				if (factory.previousState == Factory.State.Produce) {
					robot = factory.unload(Direction.South);
					
					// was able to produce the robot
					if (robot != null) {
						unitHashMap.put(robot.id, robot);
					}
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
			robot.move(dir);
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
		int randomInt = r.nextInt(16);
		
		if (randomInt < 3) {
			dir = Direction.South;
		} else if (randomInt < 8) {
			dir = Direction.Southwest;
		} else if (randomInt < 10) {
			dir = Direction.West;
		} else if (randomInt < 11) {
			dir = Direction.Northwest;
		} else if (randomInt < 12) {
			dir = Direction.North;
		} else if (randomInt < 13) {
			dir = Direction.Northeast;
		} else if (randomInt < 14) {
			dir = Direction.East;
		} else {
			dir = Direction.Southeast;
		}
		
		return robot.move(dir);
	}
}

