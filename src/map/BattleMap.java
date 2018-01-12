import bc.*;

import java.util.ArrayList;
import java.util.PriorityQueue;
import java.lang.Math;

/**
 * Represents a map on a planet
 * 
 * @author virsain
 */
public class BattleMap implements Map {
	
	// the map represented as a grid of TileNodes
	public TileNode tileNodeMap[][];
	
	int height; // height of the map
	int width;  // width of the map
	Planet planet;  // Earth or Mars (Enum)
		
	// contains all Karbonite locations
	ArrayList<MapLocation> karboniteLocations;  
	
	/**
	 * Creates a new Map
	 */
	public BattleMap(PlanetMap planetMap) {
		planet = planetMap.getPlanet();
		height = (int) planetMap.getHeight();
		width = (int) planetMap.getWidth();
		tileNodeMap = new TileNode[height][width];
		karboniteLocations = new ArrayList<MapLocation>();
		
      makeGrid(planetMap);
	}
	
	/**
	 * Initializes the TileNodes and creates edges to their
	 * neighboring TileNodes.
	 */
	private void makeGrid(PlanetMap planetMap) {
		// iterate through each tile and initialize it
		for (int i = 0; i < height; i++) {
			for (int j = 0; j < width; j++) {
				tileNodeMap[i][j] = new TileNode();
				
				// something at [i][j] has coordinates j, i
				tileNodeMap[i][j].location = new Tuple(j, i);
				
				// The API requres MapLocation types instead of Tuples
				MapLocation mapLocation = new MapLocation(planet, j, i);
				
				
				// determines the tileType based on the API
				// if the terrain is passable then it is either karbonite or empty
				if (planetMap.isPassableTerrainAt(mapLocation) == 1) {
					int karbonite = (int) planetMap.initialKarboniteAt(mapLocation);

					if (karbonite > 0) {
						tileNodeMap[i][j].tileType = karbonite;
						karboniteLocations.add(mapLocation);
					} else {
						tileNodeMap[i][j].tileType = 0;
					}
				// otherwise the tile is water	
				} else {
					tileNodeMap[i][j].tileType = -1;
				}
					
				
				/* checks if it is possible to move to a neighbor
				 * if it is possible, add the neighbor's location to the list of 
				 * neighbors 
				 */
				
				int index = 0;
				
				// south
				if (i-1 >= 0)
					tileNodeMap[i][j].neighbors[index++] = new Tuple(j, i-1);
				
				// north
				if (i+1 < height)
					tileNodeMap[i][j].neighbors[index++] = new Tuple(j, i+1);
				
				// east
				if (j-1 >= 0)
					tileNodeMap[i][j].neighbors[index++] = new Tuple(j-1, i);
				
				// west
				if (j+1 < width)
					tileNodeMap[i][j].neighbors[index++] = new Tuple(j+1, i);
				
				// south-east
				if (i-1 >= 0 && j-1 >= 0)
					tileNodeMap[i][j].neighbors[index++] = new Tuple(j-1, i-1);
				
				// south-west
				if (i-1 >= 0 && j+1 < width)
					tileNodeMap[i][j].neighbors[index++] = new Tuple(j+1, i-1);
				
				// north-east
				if (i+1 < height && j-1 >= 0)
					tileNodeMap[i][j].neighbors[index++] = new Tuple(j-1, i+1);
				
				// north-west
				if (i+1 < height && j+1 < width)
					tileNodeMap[i][j].neighbors[index++] = new Tuple(j+1, i+1);
			
				tileNodeMap[i][j].numOfNeighbors = index;
			}
		}
	}
	
	/**
	 * Returns the list of Karbonite locations on the map
	 */
	public ArrayList<MapLocation> getKarboniteLocations() {
		// we don't want the internal ArrayList to get modified so we clone
		return (ArrayList<MapLocation>) karboniteLocations.clone();
	}
	
	/**
	 * Returns the tile type as an integer at the given location. If the tile 
	 * type is Karbonite, returns the integer amount of Karbonite.
	 * 
	 * @param location
	 * 	The given MapLocation
	 */
	public int getTileType(MapLocation location) {
		return tileNodeMap[location.getY()][location.getX()].tileType;
	}
	
	/**
	 * Updates the occupant of a TileNode at a specific location
	 * 
	 * @param location
	 * 	The location of the TileNode 
	 * @param occupant
	 * 	The occupant represented as an integer
	 */
	public void updateOccupant(MapLocation location, UnitType occupant) {
		tileNodeMap[location.getY()][location.getX()].occupant = occupant;
	}
	
	/**
	 * Finds the shortest path between two points
	 * 
	 * @param startingPoint
	 * 	The point from which the path begins
	 * 
	 * @param endingPoint
	 * 	The point at which the path ends
	 * 
	 * @return
	 * 	An array of Tuples which represents the 
	 * 	points that the path visits in order
	 */
	public MapLocation[] shortestPath(MapLocation start, MapLocation end) {
		reset();
		
		Tuple startingPoint = new Tuple(start.getX(), start.getY());
		Tuple endingPoint = new Tuple(end.getX(), end.getY());
		
		// the goal is a water tile
		if (tileNodeMap[endingPoint.y][endingPoint.x].tileType < 0)
			return new MapLocation[0];
		
		// the start and end points are the same
		if (startingPoint.equals(endingPoint))
			return new MapLocation[0];
		
		// create a minPriorityQueue
		PriorityQueue<TileNode> frontier = 
				new PriorityQueue<TileNode>(1000, new TileNodeComparator());
		
		// set the distance to be 0 and predecessor to null
		// because in a previous iteration it may have been a different value
		tileNodeMap[startingPoint.y][startingPoint.x].distance = 0;
		tileNodeMap[startingPoint.y][startingPoint.x].predecessor = null;
		
		// update visited status
		tileNodeMap[startingPoint.y][startingPoint.x].wasVisited = true;
		
		// set the weight for the starting node
		tileNodeMap[startingPoint.y][startingPoint.x].weight = 
				diagonalDistance(startingPoint, endingPoint);
		
		// add the starting node to the priority queue
		frontier.add(tileNodeMap[startingPoint.y][startingPoint.x]);
		
		while (frontier.size() > 0) {
			TileNode tile = frontier.poll();  // tile with smallest weight
			
			// get each neighboring tile
			for (int i = 0; i < tile.numOfNeighbors; i++) {
				TileNode successor = 
						tileNodeMap[tile.neighbors[i].y][tile.neighbors[i].x];
					
				// if the goal has been reached
				if (successor.location.equals(endingPoint)) {
					successor.predecessor = tile;
					successor.distance = tile.distance + 1;
					return getPath(successor);
				}
				
				// if the tile is occupied or was already visited skip the tile
				if (successor.tileType < 0 || successor.wasVisited) continue;
				
				// update successor's distance, weight, visited status
				successor.distance = tile.distance + 1;
				successor.weight = successor.distance + 
						diagonalDistance(successor.location, endingPoint);
				successor.wasVisited = true;
				
				// update successor's parent
				successor.predecessor = tile;
				
				// add successor to priority queue
				frontier.add(successor);
			}
		}
		
		return new MapLocation[0];
	}
	
	/**
	 * Obtains the path from the startingNode to the endingNode
	 * after A* search is done.
	 * 
	 * @param lastNode
	 * 	The endingNode
	 * @return
	 * 	The path
	 */
	private MapLocation[] getPath(TileNode lastNode) {
		MapLocation[] path = new MapLocation[lastNode.distance];
		int index = lastNode.distance - 1;
		
		TileNode current = lastNode;
		TileNode previous = lastNode.predecessor;
		
		// the previous node is the node that came before this node in the path
		// if the previous node is null it means we reached the startNode
		while (previous != null) {
			path[index--] = new MapLocation(planet, current.location.x, 
					current.location.y);
			current = previous;
			previous = current.predecessor;
		}
		
		return path;
	}
	
	/**
	 * Resets the values of wasVisited to false for each TileNode before
	 * the A* heuristic search
	 */
	private void reset() {
		for (int i = 0; i < height; i++) {
			for (int j = 0; j < width; j++) {
				tileNodeMap[i][j].wasVisited = false;
			}
		}
	}
	
	/**
	 * Estimates smallest distance between two points in terms of robot movement
	 * This distance is the diagonal distance heuristic function.
	 * 
	 * @return
	 * 	The estimated distance
	 */
	private int diagonalDistance(Tuple a, Tuple b) {
		// the difference in x values of the two points
		int xDiff = Math.abs(a.x - b.x);
		
		// the difference in y values of the two points
		int yDiff = Math.abs(a.y - b.y);

		// return the larger of the two values
		if (xDiff > yDiff) return xDiff;
		else return yDiff;
	}
	
	/**
	 * Returns a String representation of the map
	 */
	public String toString() {
		StringBuilder sb = new StringBuilder();
		
		for (int i = (int) height - 1; i >= 0; i--) {
			for (int j = 0; j < width; j++) {
				sb.append(tileNodeMap[i][j].tileType + " ");
			}
			
			sb.append("\n");
		}
		
		return sb.toString();
	}
	
	/**
	 * Returns a String representation of the map
	 * with the shortest path represented as stars
	 * 
	 * @param
	 * 	The path as a series of Tuples
	 */
	public String toString(Tuple[] path) {
		StringBuilder sb = new StringBuilder();
		
		for (int i = (int) height - 1; i >= 0; i--) {
			for (int j = 0; j < width; j++) {
				Tuple point = new Tuple(j, i);
				boolean flag = false;
				
				for (int k = 0; k < path.length; k++) {
					if (point.equals(path[k])) {
						flag = true;
						break;
					}
				}
				
				if (flag) sb.append("* ");
				else sb.append(tileNodeMap[i][j].tileType + " ");
			}
			
			sb.append("\n");
		}
		
		return sb.toString();
	}
}
