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
	public int heal() {
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
		
		// iterate through the units and try to attack factory
		for (int i = 0; i < vecUnit.size(); i++) {
			Unit allyUnit = vecUnit.get(i);
			
			if (allyUnit.health() == allyUnit.maxHealth()) continue;
			
			if (gc.canHeal(id, allyUnit.id())) {
				gc.heal(id, allyUnit.id());
				System.out.println("SUCCESSFULLY HEALED " + allyUnit.unitType());
				return 1;
			}
		}
		
		return 3;
	}
}
