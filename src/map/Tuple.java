package algorithms;

/**
 * Represents a point
 * 
 * @author virsain
 */
public class Tuple {
	int x;  // the x-coordinate
	int y;  // the y-coordinate
	
	/**
	 * Creates a new Tuple
	 * 
	 * @param x
	 * 	the x-coordinate
	 * 
	 * @param y
	 * 	the y-coordinate
	 */
	public Tuple(int x, int y) {
		this.x = x;
		this.y = y;
	}
	
	/**
	 * Tests whether another Tuple is equal to this Tuple
	 * 
	 * @param other
	 * 	The other Tuple
	 * 
	 * @return
	 * 	True if equal, false otherwise
	 */
	public boolean equals(Tuple other) {
		if (this.x == other.x && this.y == other.y)
			return true;

		return false;
	}
	
	/**
	 * Returns a String representation of a Tuple
	 */
	public String toString() {
		return "(" + x + ", " + y + ")";
	}
}
