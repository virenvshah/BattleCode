import bc.*;

import java.util.HashMap;

/**
 * Represents a knight robot in the game
 * @author virsain
 */
public class Knight extends AbstractRobot {
	
	public Knight(int i, GameController g, Map map, MapLocation location,
			HashMap<Integer, AbstractUnit> hashMap) {
		
		super(i, g, map, location, UnitType.Knight, hashMap);
		state = State.Spawn;
		previousState = State.Idle;
	}
}
