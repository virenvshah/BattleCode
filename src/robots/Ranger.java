package robots;

import api.*;
import map.Map;
//import bc.*;

/**
 * Represents a ranger robot in the game
 * @author virsain
 */
public class Ranger extends AbstractRobot {
	public Ranger(int i, GameController g, Map map, MapLocation location) {
		super(i, g, map, location);
		occupantType = UnitType.Ranger;
	}
}
