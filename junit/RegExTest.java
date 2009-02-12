/**
 * %HEADER%
 */
package junit;

import org.junit.Test;

public class RegExTest {

	@Test
	public void testRegEX() {
		String s = "join(complement(41303..41324),complement(40265..40318),complement(17234..17346),complement(16715..16808),complement(16119..16226),complement(15596..15671),complement(12745..12840),complement(12382..12447),complement(11121..11255),complement(9724..9749),complement(9158..9626),complement(7061..8230))";
		String t=s.replaceAll("complement\\((.*?)\\)", "$1");
		String x=t.replaceAll("join\\((.*?)\\)", "$1");
		
		System.out.println(x);
	}
}
