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
	}
}
