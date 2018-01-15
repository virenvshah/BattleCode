import bc.*;
import java.util.*;

/**
 * A player that mines
 * @author vineet
 *
 */
public class MiningPlayer {
	public static void main(String args[]) {
		MiningPlayer fbp = new MiningPlayer();
		GameController gc = new GameController();
		Worker worker = null;

		// get the API specific planet Map
	   PlanetMap earthPlanetMap = gc.startingMap(Planet.Earth);

	   // make our own map from the API specific map
	   BattleMap earthBattleMap = new BattleMap(earthPlanetMap);
		 ArrayDeque<MapLocation> mineLocs = earthBattleMap.getKarboniteLocations();
		 HashSet<MapLocation> minedOut = new HashSet();

	   // get the list of units
   	VecUnit vecUnit = gc.myUnits();
   	for (int i = 0; i < vecUnit.size(); i++) {
   		Unit unit = vecUnit.get(0);

   		if (unit.unitType() == UnitType.Worker) {
   			worker = new Worker(unit.id(), gc, earthBattleMap,
   		   		unit.location().mapLocation(), mineLocs, minedOut);
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
	      				fbp.mine(worker);
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
	 * A strategy for mining
	 */
	public void mine(Worker worker) {
		switch(worker.state){
			case Idle:
				if (worker.previousState == Worker.State.Move){
					System.out.println("Starting to mine");
					worker.mineArea(); // start mining
				} else {
					// put deque selection on this thing: we went from mining to
					// a standstill
					while(!(worker.mineLocs.isEmpty())){ //while it's not empty
						MapLocation temp = worker.mineLocs.remove();
						if(!(worker.minedOut.contains(temp))){
							worker.setPath(worker.currentLocation,temp);
							// update the state
							worker.previousState=worker.state;
							worker.state=Worker.State.Move;
							System.out.println("Picked a spot, let's move");
							break;
						}
					}
				}
				break;
			case Mine:
				System.out.println("Mining");
				worker.mineArea();
				break;
			case Move:
				System.out.println("Moving");
			 	if (worker.move()==1){
					// update state when you're here
					worker.previousState=worker.state;
					worker.state=Worker.State.Idle;
				}
				break;
			default:
		 		return;
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
