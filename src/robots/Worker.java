package robots;

import api.*;
import map.Map;
//import bc.*;

/**
 * Represents a worker robot in the game
 * @author virsain
 */
public class Worker extends AbstractRobot {
	public enum State { Build, Move, Mine; };
	
	public Worker(int i, GameController g, Map map, MapLocation location) {
		super(i, g, map, location);
		occupantType = UnitType.Worker;
	}
}