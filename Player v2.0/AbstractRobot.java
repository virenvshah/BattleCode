import bc.*;

import java.util.Random;
import java.util.ArrayList;
import java.util.HashMap;

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
	public MapLocation destination;
	public Direction moveDirection;  // the direction along which the robot must move
	public int moveIndex;  // the index of movePath, the number of steps the robot
						 // has already taken along the path
	Map battleMap;  // A grid of TileNodes representing the full map
	public State state; // The robot's present state
	public State previousState;  // The robot's previous state
	Random r; // a random number generator
	HashMap<Integer, AbstractUnit> unitHashMap; // the unit hashMap
	int attempts; // attempts to move along a path

	public AbstractRobot(int i, GameController g, Map map, MapLocation location,
			UnitType t, HashMap<Integer, AbstractUnit> hashMap) {
		id = i;
		gc = g;
		battleMap = map;
		moveIndex = 0;
		currentLocation = location;
		type = t;
		unitHashMap = hashMap;
		r = new Random();
		
		battleMap.updateOccupant(location, i);	
	}
	
	/**
	 * Tries to move the robot along a path.
	 * 
	 * @param moveToEnemy
	 * 	Decides whether to call setPath or setPathToEnemy
	 * @return
	 * 	Returns -1 if no path is set
	 * 	Returns  0 if successfully moved
	 * 	Returns  1 if successfully moved and reached destination
	 * 	Returns  2 if not ready to move (still on cooldown)
	 * 	Returns  3 if path is blocked
	 * 	Returns  4 if destination is blocked
	 * 	Returns  5 if randomly moved
	 */
	public int movePath(boolean moveToEnemy) {
		// no path is set
		if (movePath == null) {
			state = State.Idle;
			return -1;
		}
			
		previousState = state;
		// change state to Idle in if conditions
		state = State.Move;
		
		// movement cooldown still up
		if (!gc.isMoveReady(id)) {
			// worker is still moving
			return 2; 
		}
		
		// get the next step's direction
		Direction dir = currentLocation.directionTo(movePath[moveIndex]);
		moveDirection = dir;
	
		// check if the robot can move in that direction
		if (!gc.canMove(id, dir)) {
			attempts++;
			
			// tried too many times to move but couldn't move
			if (attempts > 6) {
				resetPath();
				randomMovement();
				return 5;
			}
			
			// otherwise, get the unit obstructing it
			
			Unit unit = gc.senseUnitAtLocation(movePath[moveIndex]);
			// if the obstruction is a worker or a factory
			if (unit.unitType() == UnitType.Worker || unit.unitType() == 
					UnitType.Factory) {
				 
				// destination is blocked
				if (moveIndex == movePath.length-1) {
					
					// you've basically reached
					// the enemy location has been reached so remove it from
					// the enemy locations ArrayList
					if (moveToEnemy) battleMap.removeEnemyLocation(destination);
					resetPath();
					return 4;
				}
				 
				// if the obstruction is the enemy then ditch the path
				// and move randomly hoping to move somewhere smart
				if (unit.team() == battleMap.getEnemyTeamColor()) {
					resetPath();
					randomMovement();
				   return 5;
				}
				
								
				if (unit.unitType() == UnitType.Worker) {
					
					Worker worker = (Worker) unitHashMap.get(unit.id());
					 
					// if the worker's state isn't either of these then the worker will 
					// eventually move away.  Therefore just wait for it to move.
					if (worker.state != State.Build && worker.state != State.Mine) {
						return 3;
					}
				}
				 
				// otherwise if its an ally factory or an ally worker, which is building or mining 
				// a new path must be set
				 
				// if the new path can be set:
				if (moveToEnemy && setPathToEnemy()) {
					// try moving along the new path
					movePath(true);
				} else if (!moveToEnemy && setPath(currentLocation, movePath[movePath.length-1])) {
					// try moving along the new path
					movePath(false);
				}
			}
			 
			return 3;
		}
		
		previousState = State.Move;
		state = State.Move;
		gc.moveRobot(id, dir);
		attempts = 0;
		// set previous location's occupant to 0
		battleMap.updateOccupant(currentLocation, -1);
		// update the current location
		currentLocation = movePath[moveIndex];

		// update the map to the new location of the robot
		battleMap.updateOccupant(currentLocation, id);
		
		// increment for next step
		moveIndex++;
		
		// if reached destination then reset path
		if (moveIndex == movePath.length) {
			if (moveToEnemy) battleMap.removeEnemyLocation(destination);
			resetPath();
			return 1;
		}
		
		return 0;
	}
	
	/**
	 * Tries to move the robot in a particular direction
	 * 
	 * @param dir
	 * 	The direction in which the robot must move
	 * @return
	 * 	Returns -1 if no direction set or if not ready to move (still on cooldown)
	 * 	Returns 1 if successfully moved
	 * 	Returns 2 if path is blocked
	 */
	public int moveDir() {
		previousState = state;
		
		if (moveDirection == null) return -1;
		
		// movement cooldown still up
		if (!gc.isMoveReady(id)) {
			// worker is still trying to moves
			state = State.Move;
			return -1; 
		}
		
		// check if the robot can move in that direction
		if (!gc.canMove(id, moveDirection)) {
			state = State.Move;
			return 2;
		}
		
		gc.moveRobot(id, moveDirection);
		// if there is a path, reset it because you're moving off the path
		resetPath();
		
		// set previous location's occupantId to 0
		battleMap.updateOccupant(currentLocation, -1);
		// update the current location
		currentLocation = currentLocation.add(moveDirection);
		
		// update the map to the new location of the robot
		battleMap.updateOccupant(currentLocation, id);
		
		previousState = State.Move;
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
		if (path.length == 0) {
			resetPath();
			return false;
		}
		
		movePath = path;
		destination = movePath[movePath.length-1];
		moveIndex = 0;
		attempts = 0;
		return true;
	}
	
	/**
	 * Sets path based on an existing path
	 * @param path
	 * 	The existing path
	 */
	public void setPath(MapLocation[] path) {
		movePath = path;
		destination = movePath[movePath.length-1];
		moveIndex = 0;
		attempts = 0;
	}
	
	/**
	 * Sets the robot's path to one of the enemy spawning locations
	 */
	public boolean setPathToEnemy() {
		// get the starting locations of the enemy
		ArrayList<MapLocation> enemyLocations = 
				battleMap.getEnemyLocations();
		
		if (enemyLocations.size() == 0) {
			return false;
		}
		
		MapLocation[] path;
		
		// the destination
		MapLocation dest = getClosestLocation(currentLocation, 
				enemyLocations);
		
		// check if there is an existing path
		Tuple locTuple = new Tuple(currentLocation.getX(), currentLocation.
				getY());
			
		// Tuples must be passed because their hashCode and equals method
		// was Override
		path = battleMap.getExistingPath(locTuple);
			
		if (path != null) {
			
			// make sure the path isn't the same path that the robot already tried
			if (movePath != null) {
				if (movePath[0] != path[0]) {
					setPath(path);
					return true;
				}
				
			// otherwise if movePath is null	
			} else {
				setPath(path);
				return true;
			}
		}
			
		// if no existing path try forming a path
		if (setPath(currentLocation, dest)) {
			// add that path to the existing paths so other robots can use it
			// later on if they try forming a path from that location
			battleMap.addExistingPath(locTuple, movePath);
			return true;
		}
			
		return false;
	}
	
	/**
	 * Resets the path
	 */
	public void resetPath() {
		movePath = null;
		destination = null;
		moveIndex = 0;
		state = State.Idle;
		attempts = 0;
	}
	
	public MapLocation getLocation() {
		return currentLocation;
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
	 * 	Returns 3 if units detected within attack range, but couldn't attack
	 * 	Returns 4 if no units detected
	 */
	public int attack() {
		// make sure attack isn't on cooldown
		if (!gc.isAttackReady(id)) {
			return 2; 
		}
			
		// get the units within attacking range
		VecUnit vecUnit = gc.senseNearbyUnitsByTeam(currentLocation, 
				gc.unit(id).attackRange(), battleMap.getEnemyTeamColor());
		
		// if there are no units
		if (vecUnit.size() == 0) {
			return 4;
		}
		
		// iterate through the units and try to attack factory
		for (int i = 0; i < vecUnit.size(); i++) {
			Unit enemyUnit = vecUnit.get(i);
			
			if (enemyUnit.unitType() != UnitType.Factory) continue;
			
			if (gc.canAttack(id, enemyUnit.id())) {
				gc.attack(id, enemyUnit.id());
				System.out.println("SUCCESSFULLY ATTACKED FACTORY");
				return 1;
			}
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
	 * Looks for enemies and moves towards them.  If no enemies found, 
	 * it moves randomly
	 * @return
	 * 	1 if moved towards enemy, -1 if no enemy found
	 */
	public int moveTowardsEnemy() {
		int returnValue;
		Direction dir = enemyDirection();
		
		if (dir == null) {
			return -1;
		} else if (dir == Direction.Center) {
			return 1;
		} else {
			// try moving in the specified direction
			setDirection(dir);
			returnValue = moveDir();
			
			// check if there was an obstruction, otherwise return
			if (returnValue != 2) return returnValue;
			
			// try an adjacent direction
			setDirection(Utility.nextDirection(dir));
			returnValue = moveDir();
			
			if (returnValue != 2) return returnValue;
			
			// try an adjacent direction
			setDirection(Utility.previousDirection(dir));
			returnValue = moveDir();
			
			// if robot was able to move return 1, otherwise -1
			if (returnValue == 1) return returnValue;
			else return -1;
		}
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
	
	/**
	 * Returns the nearby structures at a location for a certain radius
	 * @param location
	 * 	The specified locatoin
	 * @param radius
	 * 	The radius within which to look for structures
	 * @return
	 * 	A structure Unit if found, null if none found 
	 */
	public Unit getNearbyStruct(MapLocation location, long radius) {
		// only get the units that are within a unit box of the robot
		VecUnit unitList = gc.senseNearbyUnitsByTeam(location, 
				radius, battleMap.getAllyTeamColor());
			
		for (int i = 0; i < unitList.size(); i++) {
			Unit unit = unitList.get(i);
				
			if (unit.unitType() == UnitType.Factory) {
				// return the direction to the factory from the robot
				return unit;
			}
		}
			
			// no unbuilt factories :(
			return null;
	}
	
	/**
	 * Returns the destination in the ArrayList which is closest to a specified location
	 * 
	 * @param location
	 * 	The specified location
	 * @param destinations
	 * 	The ArrayList of destinations
	 * @return
	 * 	The closest destination
	 */
	public MapLocation getClosestLocation(MapLocation location, ArrayList<MapLocation> destinations) {
		MapLocation dest;
		int xDiff, yDiff;
		int distance;
		int minDist = 999999999;
		int minIndex = 0;
		
		// get the closest destination
		for (int i = 0; i < destinations.size(); i++) {
			dest = destinations.get(i);
			xDiff = Math.abs(location.getX() - dest.getY());
		
			// the difference in y values of the two points
		   yDiff = Math.abs(location.getY() - dest.getY());

		   // equate distance to the larger of the two values
		   if (xDiff > yDiff) distance = xDiff;
		   else distance = yDiff;
		   
		   // check if distance is smaller than minimum distance
		   if (distance < minDist) {
		   	minDist = distance;
		   	minIndex = i;
		   }
		}  
		
		return destinations.get(minIndex);
	}
	
	/**
	 * Causes the unit to move in a random direction
	 * 
	 * @return
	 * 	-1 if no direction is set
	 * 	otherwise return value of moveDir()
	 */
	public int randomMovement() {
		// get all the directions in which the robot can move
		ArrayList<Direction> dirs = getPassableDirections();
		
		if (dirs.size() > 0) {
			setDirection(Utility.getRandomDirection(dirs));
			return moveDir();
		}
		
		// no direction could be set
		return -1;
	}
	
	public ArrayList<Direction> getPassableDirections() {
		return battleMap.getPassableDirections(currentLocation);
	}
	
	// debugging purposes
	public void printString() {
		System.out.println("CurrentLocation " + currentLocation);
		System.out.println("Real Location " + gc.unit(id).location().mapLocation());
		System.out.println();
				
		if (movePath == null) {
			System.out.println(moveDirection);
		} else {
			for (int i = 0; i < movePath.length; i++) {
				System.out.println(movePath[i]);
			}
		}
	
		System.out.println();
		System.out.println();
		System.out.println();
	}
}
