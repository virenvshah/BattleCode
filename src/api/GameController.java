package api;

public class GameController {
	public void nextTurn() {}
	
	public int round() {
		return 0;
	}
	
	public PlanetMap startingMap(Planet planet) {
		return new PlanetMap();
	}

	public boolean isMoveReady(int id) {
		return true;
	}

	public boolean canMove(int id, Direction dir) {
		return true;
	}

	public void moveRobot(int id, Direction dir) {
		
	}

	public VecUnit myUnits() {
		return new VecUnit();
	}
}