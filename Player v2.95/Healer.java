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
	
	/**
	 * Attempts to heal a target
	 * 
	 * @return
	 * 	Returns 1 if successfully healed
	 * 	Returns 2 if heal cooldown still up
	 * 	Returns 3 if units detected but unable to heal 
	 * 		(they all are at max health)
	 * 	Returns 4 if no units detected
	 */
	public int attack() {
		// if the healer can't heal
		if (!gc.isHealReady(id)) {
			return 2;
		}
		
		VecUnit vecUnit = gc.senseNearbyUnitsByTeam(currentLocation, 
				gc.unit(id).attackRange(), battleMap.getAllyTeamColor());
		
		// if there are no units
		if (vecUnit.size() == 0) {
			return 4;
		}
		
		// iterate through the units and try to heal ally
		for (int i = 0; i < vecUnit.size(); i++) {
			Unit allyUnit = vecUnit.get(i);
			
			// if they are already at max health then do nothing
			if (allyUnit.health() == allyUnit.maxHealth()) continue;
			
			if (gc.canHeal(id, allyUnit.id())) {
				gc.heal(id, allyUnit.id());
				return 1;
			}
		}
		
		return 3;
	}
	
	/**
	 * Overrides the enemyDirection in Abstract Robot and walks towards allies
	 * instead of enemies
	 */
	public Direction enemyDirection() {
		// get the enemies in your vision radius
		VecUnit vecUnit1 = gc.senseNearbyUnitsByTeam(currentLocation, 
				gc.unit(id).visionRange(), battleMap.getAllyTeamColor());
		Unit lowHealthRobot;
		
		lowHealthRobot = getLowHealthRobot(vecUnit1);
		
		if (lowHealthRobot == null) return null;
		
		return currentLocation.directionTo(lowHealthRobot.location().
				mapLocation());
	}

	private Unit getLowHealthRobot(VecUnit vecUnit) {
		
		for (int i = 0; i < vecUnit.size(); i++) {
			Unit unit = vecUnit.get(i);
			
			if (unit.unitType() == UnitType.Factory || unit.unitType() == 
					UnitType.Rocket || unit.unitType() == UnitType.Healer) {
				continue;
			}
			
			if (unit.health() < unit.maxHealth()) return unit;
		}
		
		return null;
	}
}
