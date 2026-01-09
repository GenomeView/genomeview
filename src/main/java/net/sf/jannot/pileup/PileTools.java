/**
 * %HEADER%
 */
package net.sf.jannot.pileup;

/**
 * 
 * @author Thomas Abeel
 * 
 */
public class PileTools {
	/**
	 * Creates the most memory efficient pile implementation for the given
	 * values.
	 * 
	 * @param s
	 * @param arr
	 * @return
	 */
	public static Pile create(int s, float ... arr) {
		switch (arr.length) {
		case 0:
			return null;
		case 1:
			return new SinglePile(s, arr[0]);
		case 2:
			return new DoublePile(s, arr[0], arr[1]);
		default:
			return new MultiPile(s, arr);

		}

	}
}
