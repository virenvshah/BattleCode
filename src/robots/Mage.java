package robots;

import api.*;
import map.Map;
//import bc.*;

/**
 * Represents a mage robot in the game
 * @author virsain
 */
public class Mage extends AbstractRobot {
	public Mage(int i, GameController g, Map map, MapLocation location) {
		super(i, g, map, location);
		occupantType = UnitType.Mage;
	}
}