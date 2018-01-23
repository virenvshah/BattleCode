import bc.*;
import java.util.ArrayList;
import java.util.Random;

/**
 * Contains useful utility methods 
 * 
 * @author virsain
 */
public class Utility {
	/**
	 * Returns the opposite direction to a direction
	 * @param dir
	 * 	The specified direction
	 * @return
	 * 	The opposite direction
	 */
	public static Direction getOppositeDirection(Direction dir) {
		switch (dir) {
			case South:
				return Direction.North;
			case Southwest:
				return Direction.Northeast;
			case Southeast:
				return Direction.Northwest;
			case North:
				return Direction.South;
			case Northwest:
				return Direction.Southeast;
			case Northeast:
				return Direction.Southwest;
			case West:
				return Direction.East;
			default:
				return Direction.West;
		}	
	}
	
	/**
	 * Returns the adjacent direction to a direction (one ahead)
	 * @param dir
	 * 	The specified direction
	 */
	public static Direction nextDirection(Direction dir) {
		switch (dir) {
		case North:
			return Direction.Northeast;
		case Northeast:
			return Direction.East;
		case East:
			return Direction.Southeast;
		case Southeast:
			return Direction.South;
		case South:
			return Direction.Southwest;
		case Southwest:
			return Direction.West;
		case West:
			return Direction.Northwest;
		default:
			return Direction.North;
		}	
	}
	
	/**
	 * Returns the adjacent direction to a direction (one behind)
	 * @param dir
	 * 	The specified direction
	 */
	public static Direction previousDirection(Direction dir) {
		switch (dir) {
		case North:
			return Direction.Northwest;
		case Northwest:
			return Direction.West;
		case West:
			return Direction.Southwest;
		case Southwest:
			return Direction.South;
		case South:
			return Direction.Southeast;
		case Southeast:
			return Direction.East;
		case East:
			return Direction.Northeast;
		default:
			return Direction.North;
		}	
	}

	
	/**
	 * Returns a random direction from an ArrayList of directions
	 * 
	 * 	An ArrayList of directions
	 * @return
	 * 	A random direction
	 */
	public static Direction getRandomDirection(ArrayList<Direction> dirs) {
		Random r = new Random();

		return dirs.get(r.nextInt(dirs.size()));
	}
}
