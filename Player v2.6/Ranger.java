import bc.*;

import java.util.HashMap;


/**
 * Represents a ranger robot in the game
 * @author virsain
 */
public class Ranger extends AbstractRobot {
	public Ranger(int i, GameController g, Map map, MapLocation location,
			HashMap<Integer, AbstractUnit> hashMap) {
		
		super(i, g, map, location, UnitType.Ranger, hashMap);
		previousState = State.Idle;
		state = State.Idle;
	}
	
	public void randomMovementAndAttack(){
		attack();
		randomMovement();
	}
}
