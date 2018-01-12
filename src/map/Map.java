import java.util.ArrayList;
import bc.*;

public interface Map {
	
	/**
	 * Finds the shortest path between two points
	 * 
	 * @param startingPoint
	 * 	The point from which the path begins
	 * @param endingPoint
	 * 	The point at which the path ends
	 * 
	 * @return
	 * 	An array of MapLocations which represents the 
	 * 	points that the path visits in order
	 */
	public MapLocation[] shortestPath(MapLocation startingPoint, MapLocation endingPoint);
	
	/**
	 * Updates the occupant of a TileNode at a specific location
	 * 
	 * @param location
	 * 	The location of the TileNode 
	 * @param occupant
	 * 	The occupant represented as an integer
	 */
	public void updateOccupant(MapLocation location, UnitType occupant);
	
	/**
	 * Returns the tile type as an integer at the given location. If the tile 
	 * type is Karbonite, returns the integer amount of Karbonite.
	 * 
	 * @param location
	 * 	The given MapLocation
	 */
	public int getTileType(MapLocation location);
	
	/**
	 * Returns the list of Karbonite locations on the map
	 */
	public ArrayList<MapLocation> getKarboniteLocations();
}
