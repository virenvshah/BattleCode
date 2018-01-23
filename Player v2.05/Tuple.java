/**
 * Represents a point
 * 
 * @author virsain
 */
public class Tuple {
	public int x;  // the x-coordinate
	public int y;  // the y-coordinate
	
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
	@Override
	public boolean equals(Object o) {
		try {
			Tuple other = (Tuple) o;
		
			if (this.x == other.x && this.y == other.y)
				return true;

			return false;
		} catch (Exception e) {
			return false;
		}
	}
	
	/**
	 * Returns a String representation of a Tuple
	 */
	@Override
	public String toString() {
		return "(" + x + ", " + y + ")";
	}
	
	/**
	 * Returns the hashCode of this object
	 */
	@Override
	public int hashCode() {
		return toString().hashCode();
	}
}
