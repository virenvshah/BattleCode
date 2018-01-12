package api;

public class Unit {
	public int id() {
		return 0;
	}
	
	public UnitType unitType() {
		return UnitType.Worker;
	}
	
	public Location location() {
		return new Location();
	}
}
