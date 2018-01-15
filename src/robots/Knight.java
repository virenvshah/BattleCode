import bc.*;

/**
 * Represents a knight robot in the game
 * @author virsain
 */
public class Knight extends AbstractRobot {
	// range in square units
	private int range[];
	
	public Knight(int i, GameController g, Map map, MapLocation location) {
		super(i, g, map, location, UnitType.Knight);
		state = State.Idle;
		previousState = State.Idle;
		
		range = new int[4];
		// 4 tiles radius
		range[0] = 32;
		// 3 tiles radius
		range[1] = 18;
		// 2 tiles radius 
		range[2] = 8;
		range[3] = (int) gc.unit(id).attackRange();
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
	public int ability() {
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
		
		if (vecUnit1.size() == 0) return null;
	
		// keep shrinking the range to find the enemy in the closest range
		for (int i = 0; i < range.length; i++) {
			vecUnit2 = gc.senseNearbyUnitsByTeam(currentLocation, 
					range[i], battleMap.getEnemyTeamColor());
			
			if (vecUnit2.size() == 0) {
				System.out.println("Stopped sensing at range" + range[i]);
				Direction dir = currentLocation.directionTo(vecUnit1.get(0).
						location().mapLocation());
				System.out.println(currentLocation.directionTo(vecUnit1.get(0).
						location().mapLocation()));
				System.out.println("Ready to move? " + gc.isMoveReady(id));
				System.out.println("Can move to location? " + gc.canMove(id, dir));
				return currentLocation.directionTo(vecUnit1.get(0).
						location().mapLocation());
			}
			
			vecUnit1 = vecUnit2;
		}
		
		// if vecUnit2.size() is still not zero then that means that the enemy
		// is within attacking radius.  Hence don't move.
		System.out.println("Still sensing");
		return Direction.Center;
	}
	
	/**
	 * Tries to move the robot in a particular direction
	 * 
	 * @param dir
	 * 	The direction in which the robot must move
	 * @return
	 * 	Returns 1 if successfully moved
	 * 	Returns 2 if not ready to move (still on cooldown)
	 * 	Returns 3 if path is blocked
	 */
	public int move(Direction dir) {
		previousState = state;
		int returnValue = super.move(dir);

		// robot finished moving, worker's state is null
		if (returnValue == 1) {
			state = State.Idle;
		}
		// if robot is still must move along direction, worker's state is MOVE
		else 
			state = State.Move;
		
		return returnValue;
	}
}
