package algorithms;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

/**
 * Test cases for shortest path
 * 
 * @author virsain
 */
public class PlanetMapTest {
	long startTime, endTime;

	@Test
	public void testShortestPathSmallMap() {
		PlanetMap map = new PlanetMap();
		
		map.map = new int[4][5];
		
		// create some obstructions
		map.map[1][0] = 1;
		map.map[1][1] = 1;
		map.map[2][2] = 1;
		
		map.tileNodeMap = new TileNode[map.map.length][map.map[0].length];
		map.makeGraph();
		
		// first test case
		Tuple start = new Tuple(0, 0);
		Tuple end = new Tuple(3, 3);
		printShortestPath(map, start, end);
		
		// second test case
		start = new Tuple(0, 0);
		end = new Tuple(4, 3);
		printShortestPath(map, start, end);
	}
	
	@Test
	public void testShortestPathLargeMap() {
		PlanetMap map = new PlanetMap();
		
		map.map = new int[50][50];
		map.tileNodeMap = new TileNode[map.map.length][map.map[0].length];
		map.makeGraph();
		
		// first test case
		Tuple start = new Tuple(0, 0);
		Tuple end = new Tuple(48, 48);
		printShortestPath(map, start, end);
		
		// second test case
		map.map[49][48] = 1;
		map.map[48][48] = 1;
		map.map[48][49] = 1;
		start = new Tuple(0, 0);
		end = new Tuple(49, 49);
		printShortestPath(map, start, end);
		
		map.map[20][20] = 1;
		map.map[20][21] = 1;
		map.map[21][20] = 1;
		map.map[20][22] = 1;
		map.map[22][20] = 1;
		map.map[1][1] = 1;
		start = new Tuple(0, 0);
		end = new Tuple(25, 25);
		printShortestPath(map, start, end);
		
		map.map[25][25] = 1;
		start = new Tuple(2, 45);
		end = new Tuple(26, 26);
		printShortestPath(map, start, end);
		
		map.map[25][25] = 1;
		start = new Tuple(0, 0);
		end = new Tuple(25, 25);
		printShortestPath(map, start, end);
	}
	
	public void printShortestPath(PlanetMap map, Tuple start, Tuple end) {
		long startTime, endTime;
		
		System.out.println("The map:\n");
		System.out.println(map);
		
		startTime = System.nanoTime();
		Tuple[] path = map.shortestPath(start, end);
		endTime = System.nanoTime();
		
		System.out.println("The shortest path from " + start + " to " + end + 
				" is + (" + ((endTime-startTime)/1000000.0) + " ms)" + ": \n");
		
		System.out.println(map.toString(path));
		
		for (int i = 0; i < path.length; i++) {
			System.out.print(path[i] + " ");
		}
		
		System.out.println("\n");
	}
}
