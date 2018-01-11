import bc.*;

/**
 * Represents a worker robot in the game
 * @author virsain
 */
public class Worker {
	int id;  // the worker's id
	GameController gc;  // the game controller for the game
	MapLocation[] movePath;  // the path along which the worker must move
	int moveIndex;  // the index of movePath, the number of steps the worker
						 // has already taken along the path
	MapLocation currentLocation; // the worker's current location
	Map battleMap;  // A grid of TileNodes representing the full map
	int occupantType;
	
	public Worker(int i, GameController g, Map map, MapLocation location) {
		id = i;
		gc = g;
		battleMap = map;
		occupantType = 2;
		moveIndex = 0;
		currentLocation = location;
	}
	
	/**
	 * Tries to move the worker along a path.
	 * @return
	 * 	Returns -1 if no path is set
	 * 	Returns  0 if successfully moved
	 * 	Returns  1 if successfully moved and reached destination
	 * 	Returns  2 if not ready to move (still on cooldown)
	 * 	Returns  3 if path is blocked
	 */
	public int move() {
		// no path is set
		if (movePath == null) return -1;
		
		// movement cooldown still up
		if (!gc.isMoveReady(id)) return 1; 
		
		// get the next step's direction
		Direction dir = currentLocation.directionTo(movePath[moveIndex]);
		
		// check if the robot can move in that direction
		if (!gc.canMove(id, dir)) return 3;
		
		gc.moveRobot(id, dir);
		// set previous location's occupant to 0
		battleMap.updateOccupant(currentLocation, 0);
		// update the current location
		currentLocation = movePath[moveIndex];
		// update the map to the new location of the robot
		battleMap.updateOccupant(currentLocation, occupantType);
		
		moveIndex++;
		
		// if reached destination then reset path
		if (moveIndex == movePath.length) {
			movePath = null;
			moveIndex = 0;
			return 1;
		}
		
		return 0;
	}
	
	/**
	 * Finds the shortestPath between the start and end locations and sets
	 * the robot's movePath to that path
	 * 
	 * @param start
	 * 	The start location
	 * @param end
	 * 	The end location
	 * 
	 * @return 
	 * 	True if there is a path between the start and end locations, false
	 * 	if no path exists.
	 */
	public boolean setPath(MapLocation start, MapLocation end) {
		MapLocation[] path = battleMap.shortestPath(start, end);
		
		// if path is empty, then no path exists
		if (path.length == 0) return false;
		
		movePath = path;
		return true;
	}
	
	public MapLocation getLocation() {
		return currentLocation;
	}
}
