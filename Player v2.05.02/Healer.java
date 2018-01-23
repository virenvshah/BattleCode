import bc.*;

import java.util.HashMap;

/**
 * Represents a healer robot in the game
 * @author virsain
 */
public class Healer extends AbstractRobot {
	
	public Healer(int i, GameController g, Map map, MapLocation location,
			HashMap<Integer, AbstractUnit> hashMap) {

		super(i, g, map, location, UnitType.Healer, hashMap);
		
		previousState = State.Idle;
		state = State.Idle;
	}
}
