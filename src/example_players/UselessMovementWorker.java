import java.util.ArrayList;
import bc.*;

/**
 * 
 * @author virsain
 *
 */
public class UselessMovementWorker {
	public static void main(String[] args) {
		long time1 = System.nanoTime();
		long time2;
		
		System.out.println("Starting");
		GameController gc = new GameController();
		
		// get the API specific planet Map
	   PlanetMap earthPlanetMap = gc.startingMap(Planet.Earth);
	   
	   // make our own map from the API specific map
	   BattleMap earthBattleMap = new BattleMap(earthPlanetMap);
	   ArrayList<Worker> workers = new ArrayList<Worker>();

	   // get the list of units
	   VecUnit vecUnit;
	   
	   while (true) {
	   	vecUnit = gc.myUnits();
	   	if (vecUnit.size() > 0) {
	   		break;
	   	}
	   	gc.nextTurn();
	   }
	    
	   
	   // get the first unit which will be a worker
	   Unit w = vecUnit.get(0);
	   
	   // create a worker class
	   Worker worker = new Worker(w.id(), gc, earthBattleMap, 
	   		w.location().mapLocation());
			
	   MapLocation[] endPoints = new MapLocation[2];
	   endPoints[0] = new MapLocation(Planet.Earth, (int) earthPlanetMap.getWidth() - 1, 0);
	   endPoints[1] = new MapLocation(Planet.Earth, 0, (int) earthPlanetMap.getHeight() - 1);
	   int flag = 0;
	    
	   while(true) {
	   	time1 = System.nanoTime();
	      System.out.println("Current Round: " + gc.round());
	      System.out.println("(" + worker.getLocation().getX() + ", " + 
	      		worker.getLocation().getY() + ")");
	      
	      if (worker.move() == -1) {
	         MapLocation start = new MapLocation(Planet.Earth,
	             worker.getLocation().getX(), worker.getLocation().getY());
	         flag = (flag+1) % 2;
	         MapLocation end = endPoints[flag];
	         worker.setPath(start, end);
	         worker.move();
	      }

	      // keep checking time after each turn
	      time2 = System.nanoTime();
	      System.out.println((time2-time1) / 1000000.0);
	      gc.nextTurn();
	   }
	}
}
