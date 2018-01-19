import bc.*;

/**
 * A player that builds factories
 * @author virsain
 *
 */
public class Player {
	// The hashMap where all units are stored, the key is their id

	public static void main(String args[]) {
		GameController gc = new GameController();
		TroopManagement tm = new TroopManagement(gc);


		gc.queueResearch(UnitType.Ranger);
		gc.queueResearch(UnitType.Ranger);
		gc.queueResearch(UnitType.Rocket);

		ResearchInfo techCapability = gc.researchInfo();

		try {
			tm.initializeWorkers(gc);
		} catch (Exception e) {
   		System.out.println("Exception was thrown :(");
   		e.printStackTrace();
   	}

		gc.nextTurn();

	   while (true) {
	   	System.out.println("Current Round: " + gc.round() + " Time left " +
	   			gc.getTimeLeftMs());
	   	/* if an exception occurs we don't want the program to crash because
	   	 * we will lose the game
	   	 */
	   	try {
	   		// get the list of units every turn
	   		VecUnit vecUnit = gc.myUnits();

	   		// iterate through the list of units
	      	for (int i = 0; i < vecUnit.size(); i++) {
	      		Unit u = vecUnit.get(i);

	      		AbstractUnit unit = tm.getUnit(u.id());

	      		if (unit == null) {
	      			continue;
	      		}

	      		// check the time of unit
	      		if (unit.type == UnitType.Worker) {
	      			Worker worker = (Worker) unit;
	      			switch (worker.occupation) {
	      				case Builder:
	      					tm.build(worker);
	      					break;
	      				case Miner:
									if (techCapability.getLevel(UnitType.Rocket)>=1.0){
										// rockets can be built
										tm.buildRocket(worker);
									} else {
										tm.mine(worker);
									}
	      					break;
	      			}
	      		} else if (unit.type == UnitType.Factory) {
	      			tm.produce((Factory) unit, UnitType.Ranger);
	      		} else if (unit.type == UnitType.Rocket) {
							Rocket rock = (Rocket) unit;
							if (rock.state == AbstractStructure.State.Ready){
								tm.launchRocket(rock);
							} else if (rock.state == AbstractStructure.State.Idle)
						} else if (unit.type == UnitType.Knight) {
	      			tm.manageTroop((Knight) unit);
	      		} else if (unit.type == UnitType.Ranger) {
	      			tm.goToEnemy((Ranger) unit);
	      		}
	      	}
	   	} catch (Exception e) {
	   		System.out.println("Exception was thrown :(");
	   		e.printStackTrace();
	   	}

	   	gc.nextTurn();
	   }
	}
}
