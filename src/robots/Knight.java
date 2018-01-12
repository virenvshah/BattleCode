package robots;

import api.*;
import map.Map;
//import bc.*;

/**
 * Represents a knight robot in the game
 * @author virsain
 */
public class Knight extends AbstractRobot {
	public Knight(int i, GameController g, Map map, MapLocation location) {
		super(i, g, map, location);
		occupantType = UnitType.Knight;
	}
}