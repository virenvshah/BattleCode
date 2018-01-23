import bc.*;

import java.util.HashMap;


/**
 * Represents a ranger robot in the game
 * @author virsain
 */
public class Ranger extends AbstractRobot {
	boolean sniping;
	public Ranger(int i, GameController g, Map map, MapLocation location,
			HashMap<Integer, AbstractUnit> hashMap) {
		
		super(i, g, map, location, UnitType.Ranger, hashMap);
		previousState = State.Idle;
		state = State.Idle;
		sniping = false;
	}
	
	public void randomMovementAndAttack(){
		attack();
		randomMovement();
	}
	public int snipe(MapLocation snipe_location){
		sniping = true;
		if(canBeginSnipe(id, snipe_location) && isBeginSnipeReady(id)){
			beginSnipe(id, snipe_location);
		}
		else {return -1;}

	}
}
