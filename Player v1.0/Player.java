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
	   
	   while (true) {
	   	System.out.println("Current Round: " + gc.round());
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
	      			System.out.println(u.unitType() + " is null");
	      			if (u.unitType() != UnitType.Worker) continue;
	      			
	      			tm.addUnit(u);
	      			unit = tm.getUnit(u.id());
	      		}
	      		
	      		// check the time of unit
	      		if (unit.type == UnitType.Worker) {
	      			tm.build((Worker) unit);
	      		} else if (unit.type == UnitType.Factory) {
	      			tm.produce((Factory) unit, UnitType.Ranger);
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


