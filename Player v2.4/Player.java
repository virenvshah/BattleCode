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

	      		if (player.gc.planet() == Planet.Earth) {
   	      		if (unit == null) {
   	      			continue;
   	      		}

      	     		if (player.gc.round() < 500) {
      	     			player.earthBattle(unit);
      	     		} else {
      	     			player.evacuate(unit);
      	     		}
   	      	} else {
   	      		if (unit == null) {
   	      			if (u.unitType() == UnitType.Rocket) {
   	      				player.tm.addUnit(u);
   	      				unit = player.tm.getUnit(u.id());
   	      			} else {
   	      				continue;
   	      			}
   	      		}

   	      		player.marsBattle(unit);
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
			tm.work(worker, UnitType.Factory);
		} else if (unit.type == UnitType.Factory) {
			tm.updateWorkerAndStructure(unit);
			tm.produceDifferentUnits((Factory) unit);
		} else if (unit.type == UnitType.Healer) {
			tm.goToEnemy((Healer) unit);
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
			tm.work(worker, UnitType.Rocket);
		} else if (unit.type == UnitType.Factory) {
			tm.updateWorkerAndStructure(unit);
			tm.produceLess((Factory) unit, UnitType.Ranger);
		} else if (unit.type == UnitType.Healer) {
			tm.evacuate((Healer) unit);
		} else if (unit.type == UnitType.Ranger) {
			tm.evacuate((Ranger) unit);
		} else if (unit.type == UnitType.Rocket) {
			tm.updateWorkerAndStructure(unit);
			System.out.println("Garrisoned units " + ((Rocket) unit).numOfGarrisonUnits());
			if(((Rocket)unit).atMaxCap()){ // launch at maximum capacity
				tm.launchRocket((Rocket) unit);
			}
		}
	}

	public void marsBattle(AbstractUnit unit){
		System.out.println("Hi I'm in Mars Battle");
		// check unit type
		if (unit.type == UnitType.Worker){
			tm.updateWorkerAndStructure(unit);
			Worker worker = (Worker) unit;
			try{
				tm.replicateWorkerMars((Worker) unit);
			}
			catch (Exception e){
				System.out.println("Mars replication error!");
			}
			tm.mineMars(worker);
		} else if (unit.type == UnitType.Rocket){
			tm.updateWorkerAndStructure(unit);
			Rocket rocket = (Rocket) unit;
			System.out.println("Trying to unload from rocket");
			tm.rocketUnload(rocket);
			System.out.println("Unloaded from rocket");
		} else if (unit.type == UnitType.Ranger) {
			((Ranger) unit).randomMovementAndAttack();
		} else {
			((AbstractRobot) unit).randomMovement();
		}
	}
}
