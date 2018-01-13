import bc.*;
import java.util.HashMap;

/**
 * A player that builds factories
 * @author virsain
 *
 */
public class FactoryUnloadingPlayer {
	int factoryId;
	HashMap<Integer, AbstractUnit> unitHashMap;
	
	public static void main(String args[]) {
		FactoryUnloadingPlayer fbp = new FactoryUnloadingPlayer();
		GameController gc = new GameController();
		
		// get the API specific planet Map
	   PlanetMap earthPlanetMap = gc.startingMap(Planet.Earth);
	   
	   // make our own map from the API specific map
	   BattleMap earthBattleMap = new BattleMap(earthPlanetMap);
	   fbp.unitHashMap = new HashMap<Integer, AbstractUnit>();
	   
	   
	   while (true) {
	   	System.out.println("Current Round: " + gc.round());
	   	/* if an exception occurs we don't want the program to crash because
	   	 * we will lose the game
	   	 */
	   	try {
	   		VecUnit vecUnit = gc.myUnits();
	   		System.out.println(vecUnit.size());
	      	for (int i = 0; i < vecUnit.size(); i++) {
	      		Unit unit = vecUnit.get(i);
	      		System.out.println(unit.unitType());
	      		
	      		if (unit.unitType() == UnitType.Worker) {
	      			Worker worker = (Worker) fbp.unitHashMap.get(unit.id());
	      			
	      			if (worker == null) {
	      				worker = new Worker(unit.id(), gc, earthBattleMap, 
	         		   		unit.location().mapLocation());
	      				fbp.unitHashMap.put(worker.id, worker);
	      			}
	      		
	      			fbp.build(worker);
	      		} else if (unit.unitType() == UnitType.Factory) {
	      			Factory factory = (Factory) fbp.unitHashMap.get(unit.id());
	      			
	      			if (factory == null) {
	      				System.out.println("Factory is null");
	      				System.exit(0);
	      			}
	      			
	      			fbp.produce(factory);
	      		}
	      	}
	   	} catch (Exception e) {
	   		System.out.println("Error occured");
	   		e.printStackTrace();
	   	}
	   	
	   	gc.nextTurn();
	   }
	}
	
	/**
	 * A strategy for worker building
	 */
	public void build(Worker worker) {
		switch(worker.state) {
			case Build:
				worker.build();
				break;
			case Move:
				worker.move(Direction.East);
				break;
			case Idle:
				if (worker.previousState == Worker.State.Build) {
					worker.move(Direction.East);	
				} else {
					AbstractStructure factory = worker.setBlueprint(Direction.West, 
							UnitType.Factory);
					unitHashMap.put(factory.id, factory);
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
		System.out.println(factory.state);
		System.out.println(factory.previousState);
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
			case Unload:
				// try unloading again
				factory.unload(Direction.North);
			case Idle:
				if (factory.previousState == Factory.State.Produce) {
					factory.unload(Direction.North);
				// previous state was unloading or idle
				} else {
					factory.produce(UnitType.Knight);
				}
				break;
			default:
				return;
		}
	}
}

