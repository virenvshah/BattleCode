package api;

public class PlanetMap {
	/**
	 * Returns the height of the planet map
	 */
	public long getHeight() {
		return 50;
	}

	/**
	 * Returns the width of the planet map
	 */
	public long getWidth() {
		return 50;
	}
	
	/**
	 * Returns the planet represented by this planet map
	 */
	public Planet getPlanet() {
		return Planet.Earth;
	}
	
	public short isPassableTerrainAt(MapLocation location) {
		return 1;
	}
	
	public long initialKarboniteAt(MapLocation location) {
		return 1;
	}
}
