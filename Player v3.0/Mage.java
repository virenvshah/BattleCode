import bc.*;

/**
 * Represents a mage robot in the game
 * @author virsain
 */
public class Mage extends AbstractRobot {
	public Mage(int i, GameController g, Map map, MapLocation location) {
		super(i, g, map, location, UnitType.Mage);
	}
	
	public int ability() {
		return 0;
	}
}
