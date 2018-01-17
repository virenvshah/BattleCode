import bc.*;

/**
 * Super class for all Robots
 * @author virsain
 *
 */
public abstract class AbstractRobot extends AbstractUnit {
	public enum State { Build, Move, Mine, Idle; };
	private int range[] = {72, 50, 32, 18, 8, 1};
	
	GameController gc;  // the game controller for the game
	public MapLocation[] movePath;  // the path along which the robot must move
	public Direction moveDirection;  // the direction along which the robot must move
	int moveIndex;  // the index of movePath, the number of steps the robot
						 // has already taken along the path
	MapLocation currentLocation; // the robot's current location
	Map battleMap;  // A grid of TileNodes representing the full map
	public State state; // The robot's present state
	public State previousState;  // The robot's previous state

	public AbstractRobot(int i, GameController g, Map map, MapLocation location,
			UnitType t) {
		id = i;
		gc = g;
		battleMap = map;
		moveIndex = 0;
		currentLocation = location;
		type = t;
		
	}
	
	/**
	 * Tries to move the robot along a path.
	 * @return
	 * 	Returns -1 if no path is set
	 * 	Returns  0 if successfully moved
	 * 	Returns  1 if successfully moved and reached destination
	 * 	Returns  2 if not ready to move (still on cooldown)
	 * 	Returns  3 if path is blocked
	 */
	public int movePath() {
		// no path is set
		if (movePath == null) {
			state = State.Idle;
			return -1;
		}
			
		previousState = State.Move;
		
		// movement cooldown still up
		if (!gc.isMoveReady(id)) {
			// worker is still moving
			state = State.Move;
			return 2; 
		}
		
		// get the next step's direction
		Direction dir = currentLocation.directionTo(movePath[moveIndex]);
		
		// check if the robot can move in that direction
		if (!gc.canMove(id, dir)) {
			state = State.Move;
			return 3;
		}
		
		gc.moveRobot(id, dir);
		// set previous location's occupant to 0
		battleMap.updateOccupant(currentLocation, 0);
		// update the current location
		currentLocation = movePath[moveIndex];
		// update the map to the new location of the robot
		battleMap.updateOccupant(currentLocation, id);
		
		moveIndex++;
		
		// if reached destination then reset path
		if (moveIndex == movePath.length) {
			movePath = null;
			moveIndex = 0;
			state = State.Idle;
			return 1;
		}
		
		state = State.Move;
		return 0;
	}
	
	/**
	 * Tries to move the robot in a particular direction
	 * 
	 * @param dir
	 * 	The direction in which the robot must move
	 * @return
	 * 	Returns -1 if no direction set
	 * 	Returns 1 if successfully moved
	 * 	Returns 2 if not ready to move (still on cooldown)
	 * 	Returns 3 if path is blocked
	 */
	public int moveDir() {
		if (moveDirection == null) return -1;
		
		previousState = State.Move;
		
		// movement cooldown still up
		if (!gc.isMoveReady(id)) {
			// worker is still trying to moves
			state = State.Move;
			return 2; 
		}
		
		// check if the robot can move in that direction
		if (!gc.canMove(id, moveDirection)) {
			state = State.Move;
			return 3;
		}
		
		gc.moveRobot(id, moveDirection);
		// set previous location's occupantId to 0
		battleMap.updateOccupant(currentLocation, 0);
		// update the current location
		currentLocation = currentLocation.add(moveDirection);
		// update the map to the new location of the robot
		battleMap.updateOccupant(currentLocation, id);
		
		state = State.Idle;
		return 1;
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
	
	public void setDirection(Direction dir) {
		moveDirection = dir;
	}
	
	/**
	 * Attempts to attack a target
	 * 
	 * @return
	 * 	Returns 1 if successfully attacked
	 * 	Returns 2 if attack cooldown still up
	 * 	Returns 3 if units detected but too far to attack
	 * 	Returns 4 if no units detected
	 */
	public int attack() {
		// make sure attack isn't on cooldown
		if (gc.unit(id).attackHeat() >= 10) {
			System.out.println("attack on cooldown");
			return 2; 
		}
			
		// get the units within attackign range
		VecUnit vecUnit = gc.senseNearbyUnitsByTeam(currentLocation, 
				gc.unit(id).attackRange(), battleMap.getEnemyTeamColor());
		
		// if there are no units
		if (vecUnit.size() == 0) {
			return 4;
		}
		
		// iterate through the units and attack first one that can be attacked
		for (int i = 0; i < vecUnit.size(); i++) {
			Unit enemyUnit = vecUnit.get(i);
			
			if (gc.canAttack(id, enemyUnit.id())) {
				gc.attack(id, enemyUnit.id());
				System.out.println("SUCCESSFULLY ATTACKED");
				return 1;
			}
		}
		
		return 3;
	}
	
	/**
	 * Gets the direction the robot must move to reach the closest enemy
	 */
	public Direction enemyDirection() {
		// get the enemies in your vision radius
		VecUnit vecUnit1 = gc.senseNearbyUnitsByTeam(currentLocation, 
				gc.unit(id).visionRange(), battleMap.getEnemyTeamColor());
		VecUnit vecUnit2;
		
		int i = 0;
		
		if (vecUnit1.size() == 0) return null;
		
		for (; range[i] > gc.unit(id).visionRange(); i++) ;
	
		// keep shrinking the range to find the enemy in the closest range
		for (; i < range.length; i++) {
			if (range[i] < gc.unit(id).attackRange()) break;
			
			vecUnit2 = gc.senseNearbyUnitsByTeam(currentLocation, 
					range[i], battleMap.getEnemyTeamColor());
			
			if (vecUnit2.size() == 0) {
				return currentLocation.directionTo(vecUnit1.get(0).
						location().mapLocation());
			}
			
			vecUnit1 = vecUnit2;
		}
		
		// if vecUnit2.size() is still not zero then that means that the enemy
		// is within attacking radius.  Hence don't move.
		return Direction.Center;
	}
	
	public MapLocation getLocation() {
		return currentLocation;
	}

	public void idle() {
		previousState = state;
		state = State.Idle;
	}
}
