/**
 * %HEADER%
 */
package net.sf.jannot.wiggle;
/**
 * 
 * @author Thomas Abeel
 *
 */
public interface Query {
	float[] getRawRange(int start, int end);

	long size();
}
