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
	 * 	An array of Tuples which represents the 
	 * 	points that the path visits in order
	 */
	public Tuple[] shortestPath(Tuple startingPoint, Tuple endingPoint);
	
	/**
	 * Updates the occupant of a TileNode at a specific location
	 * 
	 * @param location
	 * 	The location of the TileNode 
	 * @param occupant
	 * 	The occupant represented as an integer
	 */
	public void updateLocation(Tuple location, int occupant);
}
