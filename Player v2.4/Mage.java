import bc.*;

import java.util.HashMap;


/**
 * Represents a mage robot in the game
 * @author virsain
 */
public class Mage extends AbstractRobot {
	
	public Mage(int i, GameController g, Map map, MapLocation location,
			HashMap<Integer, AbstractUnit> hashMap) {
		
		super(i, g, map, location, UnitType.Mage, hashMap);
		previousState = State.Idle;
		state = State.Idle;
	}
}
