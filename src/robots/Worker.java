import bc.*;

/**
 * Represents a worker robot in the game
 * @author virsain
 */
public class Worker extends AbstractRobot {
	public Worker(int i, GameController g, Map map, MapLocation location) {
		id = i;
		gc = g;
		battleMap = map;
		occupantType = UnitType.Worker;
		moveIndex = 0;
		currentLocation = location;
	}
}
