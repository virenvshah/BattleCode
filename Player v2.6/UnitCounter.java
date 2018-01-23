import bc.*;

public class UnitCounter {
	// the counts from the previous turn
	public int rangerCount;
	public int workerCount;
	public int healerCount;
	public int factoryCount;
	public int rocketCount;
	
	// the counts for the present turn
	public int rangeC;
	public int workC;
	public int healC;
	public int factC;
	public int rockC;
	
	// the unit caps
	public int rangerCap;
	public int workerCap;
	public int healerCap;
	public int factoryCap;
	public int rocketCap;
	
	
	public UnitCounter() {
		rangerCount = 0;
		workerCount = 0;
		healerCount = 0;
		factoryCount = 0;
		rocketCount = 0;
		
		rangeC = 0;
		workC = 0;
		healC = 0;
		factC = 0;
		rockC = 0;
		
		rangerCap = 100;
		workerCap = 0;
		healerCap = 30;
		factoryCap = 6;
		rocketCap = 10;		
	}
	
	public void resetCount() {
		rangerCount = rangeC;
		workerCount = workC;
		healerCount = healC;
		factoryCount = factC;
		rocketCount = rockC;
		
		rangeC = 0;
		workC = 0;
		healC = 0;
		factC = 0;
		rockC = 0;
	}
	
	public int getCap(UnitType type) {
		switch (type) {
			case Ranger:
				return rangerCap;
			case Healer:
				return healerCap;
			case Worker:
				return workerCap;
			case Factory:
				return factoryCap;
			case Rocket:
				return rocketCap;
			default:
				return 0;
		}
	}
	
	public int getCount(UnitType type) {
		switch (type) {
			case Ranger:
				return rangerCount;
			case Healer:
				return healerCount;
			case Worker:
				return workerCount;
			case Factory:
				return factoryCount;
			case Rocket:
				return rocketCount;
			default:
				return 0;
		}
	}
}

