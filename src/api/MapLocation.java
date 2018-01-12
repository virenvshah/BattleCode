package api;

public class MapLocation {
	Planet planet;
	int x;
	int y;
	
	public MapLocation(Planet planet, int x, int y) {
		this.planet = planet;
		this.x = x;
		this.y = y;
	}
	
	public int getX() {
		return x;
	}
	
	public int getY() {
		return y;
	}
	
	public Direction directionTo(MapLocation to) {
		int xDiff = to.x - this.x;
		int yDiff = to.y - this.y;
		
		if (xDiff > 0 && yDiff > 0) return Direction.Northwest; 
		else if (xDiff > 0 && yDiff < 0) return Direction.Southwest;
		else if (xDiff > 0 && yDiff == 0) return Direction.West;
		else if (xDiff < 0 && yDiff > 0) return Direction.Northeast;
		else if (xDiff < 0 && yDiff < 0) return Direction.Southeast;
		else if (xDiff < 0 && yDiff == 0) return Direction.East;
		else if (xDiff == 0 && yDiff > 0) return Direction.North;
		else if (xDiff == 0 && yDiff < 0) return Direction.South;
		else return Direction.Center;
	
	}
}
