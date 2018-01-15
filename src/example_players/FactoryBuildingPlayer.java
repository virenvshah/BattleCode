import bc.*;

/**
 * A player that builds factories
 * @author virsain
 *
 */
public class FactoryBuildingPlayer {
	public static void main(String args[]) {
		FactoryBuildingPlayer fbp = new FactoryBuildingPlayer();
		GameController gc = new GameController();
		Worker worker = null;
		
		// get the API specific planet Map
	   PlanetMap earthPlanetMap = gc.startingMap(Planet.Earth);
	   
	   // make our own map from the API specific map
	   BattleMap earthBattleMap = new BattleMap(earthPlanetMap);
	   // get the list of units
   	VecUnit vecUnit = gc.myUnits();
   	for (int i = 0; i < vecUnit.size(); i++) {
   		Unit unit = vecUnit.get(0);
   		
   		if (unit.unitType() == UnitType.Worker) {
   			worker = new Worker(unit.id(), gc, earthBattleMap, 
   		   		unit.location().mapLocation());
   		}
   	}
	   
	   while (true) {
		System.out.println("Current Round: " + gc.round());
	   	/* if an exception occurs we don't want the program to crash because
	   	 * we will lose the game
	   	 */
	   	try {		
		   	vecUnit = gc.myUnits();
	      		for (int i = 0; i < vecUnit.size(); i++) {
	      			Unit unit = vecUnit.get(i);
	      			if (unit.unitType() == UnitType.Worker) {
	      				fbp.build(worker);
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
	 * A strategy for building
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
					worker.setBlueprint(Direction.West, UnitType.Factory);
				}
				break;
			default:
				return;
		}
	}
}
