package algorithms;

import java.util.PriorityQueue;
import java.lang.Math;

/**
 * Represents a map on a planet
 * 
 * @author virsain
 */
public class PlanetMap {
	
	/* A grid of integers representing a planet
	 * 0 represents an empty tile
	 * 1 represents a water tile
	 * 2 represents a Karbonite site
	 */
	public int map[][];
	
	// the map represented as a grid of TileNodes
	public TileNode tileNodeMap[][];
	
	/**
	 * Creates a new Map
	 */
	public PlanetMap() {
		
	}
	
	/**
	 * Initializes the TileNodes and creates edges to their
	 * neighboring TileNodes.
	 */
	public void makeGraph() {
		int height = tileNodeMap.length;
		int width = tileNodeMap[0].length;
		
		// iterate through each tile and initialize it
		for (int i = 0; i < height; i++) {
			for (int j = 0; j < width; j++) {
				tileNodeMap[i][j] = new TileNode();
				
				// something at [i][j] has coordinates j, i
				tileNodeMap[i][j].location = new Tuple(j, i);
				tileNodeMap[i][j].occupant = map[i][j];
				
				
				
				/* checks if it is possible to move to a neighbor
				 * if it is possible, add the neighbor's location to the list of 
				 * neighbors 
				 */
				
				int index = 0;
				
				// north
				if (i-1 >= 0)
					tileNodeMap[i][j].neighbors[index++] = new Tuple(i-1, j);
				
				// south
				if (i+1 < height)
					tileNodeMap[i][j].neighbors[index++] = new Tuple(i+1, j);
				
				// east
				if (j-1 >= 0)
					tileNodeMap[i][j].neighbors[index++] = new Tuple(i, j-1);
				
				// west
				if (j+1 < width)
					tileNodeMap[i][j].neighbors[index++] = new Tuple(i, j+1);
				
				// north-east
				if (i-1 >= 0 && j-1 >= 0)
					tileNodeMap[i][j].neighbors[index++] = new Tuple(i-1, j-1);
				
				// north-west
				if (i-1 >= 0 && j+1 < width)
					tileNodeMap[i][j].neighbors[index++] = new Tuple(i-1, j+1);
				
				// south-east
				if (i+1 < height && j-1 >= 0)
					tileNodeMap[i][j].neighbors[index++] = new Tuple(i+1, j-1);
				
				// south-west
				if (i+1 < height && j+1 < width)
					tileNodeMap[i][j].neighbors[index++] = new Tuple(i+1, j+1);
			
				tileNodeMap[i][j].numOfNeighbors = index;
			}
		}
	}
	
	/**
	 * Updates the occupants based on the integer grid
	 */
	public void updateOccupants() {
		int height = tileNodeMap.length;
		int width = tileNodeMap[0].length;
		
		for (int i = 0; i < height; i++) {
			for (int j = 0; j < width; j++) {
				tileNodeMap[i][j].occupant = map[i][j];
				tileNodeMap[i][j].wasVisited = false;
			}
		}
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
	public Tuple[] shortestPath(Tuple startingPoint, Tuple endingPoint) {
		updateOccupants();
		
		// create a minPriorityQueue
		PriorityQueue<TileNode> frontier = 
				new PriorityQueue<TileNode>(1000, new TileNodeComparator());
		
		// set the distance to be 0 and predecessor to null
		// because in a previous iteration it may have been a different value
		tileNodeMap[startingPoint.x][startingPoint.y].distance = 0;
		tileNodeMap[startingPoint.x][startingPoint.y].predecessor = null;
		
		// update visited status
		tileNodeMap[startingPoint.x][startingPoint.y].wasVisited = true;
		
		// set the weight for the starting node
		tileNodeMap[startingPoint.x][startingPoint.y].weight = 
				diagonalDistance(startingPoint, endingPoint);
		
		// add the starting node to the priority queue
		frontier.add(tileNodeMap[startingPoint.x][startingPoint.y]);
		
		while (frontier.size() > 0) {
			TileNode tile = frontier.poll();  // tile with smallest weight
			
			// get each neighboring tile
			for (int i = 0; i < tile.numOfNeighbors; i++) {
				TileNode successor = 
						tileNodeMap[tile.neighbors[i].x][tile.neighbors[i].y];
					
				// if the goal has been reached
				if (successor.location.equals(endingPoint)) {
					// if goal is occupied return empty path (path does not exist)
					if (successor.occupant > 0) return new Tuple[0];
						
					successor.predecessor = tile;
					successor.distance = tile.distance + 1;
					return getPath(successor);
				}
				
				// if the tile is occupied or was already visited skip the tile
				if (successor.occupant > 0 || successor.wasVisited) continue;
				
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
		
		return new Tuple[0];
	}
	
	public Tuple[] getPath(TileNode lastNode) {
		Tuple[] path = new Tuple[lastNode.distance];
		int index = lastNode.distance - 1;
		
		TileNode current = lastNode;
		TileNode previous = lastNode.predecessor;
		
		while (previous != null) {
			path[index--] = current.location;
			current = previous;
			previous = current.predecessor;
		}
		
		return path;
	}
	
	/**
	 * Estimates smallest distance between two points in terms of robot movement
	 * This distance is the diagonal distance heuristic function.
	 * 
	 * @return
	 * 	The estimated distance
	 */
	public int diagonalDistance(Tuple a, Tuple b) {
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
		
		for (int i = map.length - 1; i >= 0; i--) {
			for (int j = 0; j < map[0].length; j++) {
				sb.append(map[i][j] + " ");
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
		
		for (int i = map.length - 1; i >= 0; i--) {
			for (int j = 0; j < map[0].length; j++) {
				Tuple point = new Tuple(j, i);
				boolean flag = false;
				
				for (int k = 0; k < path.length; k++) {
					if (point.equals(path[k])) {
						flag = true;
						break;
					}
				}
				
				if (flag) sb.append("* ");
				else sb.append(map[i][j] + " ");
			}
			
			sb.append("\n");
		}
		
		return sb.toString();
	}
}
