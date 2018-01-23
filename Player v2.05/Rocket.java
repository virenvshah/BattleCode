import bc.*;

public class Rocket extends AbstractStructure {
	MapLocation destination;
	
	public Rocket(int id, GameController g, Map map, MapLocation location) {
		super(id, g, map, location, UnitType.Rocket);
		
		// factories can only be made by worker's in their blueprint stage
		state = State.Blueprint;
		previousState = State.Blueprint;
	}
	
	public int launch() {
		if (!gc.canLaunchRocket(id, destination)) {
			return 2;
		}
		
		gc.launchRocket(id, destination);
		System.out.println("Successfully Launched Rocket");
		
		return 1;
	}
	
	public void setDestination(MapLocation dest) {
		destination = dest;
	}
	
	public long flightDuration() {
		return gc.currentDurationOfFlight();
	}
	
	public int numOfGarrisonUnits() {
		return (int) gc.unit(id).structureGarrison().size();
	}

	public boolean atMaxCap(){return (gc.unit(id).structureGarrison().size()==gc.unit(id).structureMaxCapacity());}
}
