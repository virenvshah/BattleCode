import bc.*;
import java.util.*;
/**
 * Represents a worker robot in the game
 * @author virsain
 */
public class Worker extends AbstractRobot {
	Set<MapLocation> minedLocs;
	public Worker(int i, GameController g, Map map, MapLocation location, Set<MapLocation> mls) {
		id = i;
		gc = g;
		battleMap = map;
		occupantType = UnitType.Worker;
		moveIndex = 0;
		currentLocation = location;
		minedLocs = mls;
	}
	public int mine(){
		ArrayDeque mineLocs = battleMap.getKarboniteLocations();
		MapLocation consideration;
		while(!(mineLocs.isEmpty())){
			consideration = mineLocs.remove();
			if (!(minedLocs.contains(consideration))){
				setPath(currentLocation,consideration);

			}
		}
	}
}
