import bc.*;

/**
 * A player that builds factories
 * @author virsain
 *
 */
public class Player {
	GameController gc;
	TroopManagement tm;
	
	public static void main(String args[]) {
		Player player= new Player();
		player.gc = new GameController();
		player.tm = new TroopManagement(player.gc);
		
		player.gc.queueResearch(UnitType.Ranger);
		player.gc.queueResearch(UnitType.Ranger);
		player.gc.queueResearch(UnitType.Rocket);
		
		try {
			player.tm.initializeWorkers(player.gc);
		} catch (Exception e) {
   		System.out.println("Exception was thrown :(");
   		e.printStackTrace();
   	}
		
		player.gc.nextTurn();
	   
	   while (true) {
	   	System.out.println("Current Round: " + player.gc.round() + " Time left " + 
	   			player.gc.getTimeLeftMs());
	   	/* if an exception occurs we don't want the program to crash because
	   	 * we will lose the game
	   	 */
	   	try {
	   		// get the list of units every turn
	   		VecUnit vecUnit = player.gc.myUnits();
	   		
	   		// iterate through the list of units
	      	for (int i = 0; i < vecUnit.size(); i++) {
	      		Unit u = vecUnit.get(i);
	      		
	      		AbstractUnit unit = player.tm.getUnit(u.id());
	      		
	      		if (unit == null) {
	      			continue;
	      		}
	      		
	      		if (player.gc.round() < 500) {
	      			player.earthBattle(unit);
	      		} else {
	      			player.evacuate(unit);
	      		}
	      	}
	      	
	      	player.tm.removeDeadWorkersAndStructures();
	   	} catch (Exception e) {
	   		System.out.println("Exception was thrown :(");
	   		e.printStackTrace();
	   	}
	   	
	   	player.gc.nextTurn();
	   }
	}
	
	/**
	 * Troop management for the initial stages of the game
	 * @param unit
	 */
	public void earthBattle(AbstractUnit unit) {
		// check the time of unit
		if (unit.type == UnitType.Worker) {
			tm.updateWorkerAndStructure(unit);
			Worker worker = (Worker) unit;
			switch (worker.occupation) {
				case Builder:
					tm.build(worker, UnitType.Factory);
					break;
				case Miner:
					tm.mine(worker);
					break;
			}
		} else if (unit.type == UnitType.Factory) {
			tm.updateWorkerAndStructure(unit);
			tm.produce((Factory) unit, UnitType.Ranger);
		} else if (unit.type == UnitType.Knight) {
			tm.goToEnemy((Knight) unit);
		} else if (unit.type == UnitType.Ranger) {
			tm.goToEnemy((Ranger) unit);
		}
	}
	
	/**
	 * Troop management to leave Earth
	 * @param unit
	 */
	public void evacuate(AbstractUnit unit) {
		if (unit.type == UnitType.Worker) {
			tm.updateWorkerAndStructure(unit);
			Worker worker = (Worker) unit;
			switch (worker.occupation) {
				case Builder:
					tm.build(worker, UnitType.Rocket);
					break;
				case Miner:
					tm.mine(worker);
					break;
			}
		} else if (unit.type == UnitType.Factory) {
			tm.updateWorkerAndStructure(unit);
			tm.produceLess((Factory) unit, UnitType.Ranger);
		} else if (unit.type == UnitType.Knight) {
			tm.goToEnemy((Knight) unit);
		} else if (unit.type == UnitType.Ranger) {
			tm.evacuate((Ranger) unit);
		} else if (unit.type == UnitType.Rocket) {
			tm.updateWorkerAndStructure(unit);
			System.out.println("Garrisoned units " + ((Rocket) unit).numOfGarrisonUnits());
		}
	}
}


