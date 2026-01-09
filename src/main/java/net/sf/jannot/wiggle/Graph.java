/**
 * %HEADER%
 */
package net.sf.jannot.wiggle;

import net.sf.jannot.Data;

/**
 * 
 * @author Thomas Abeel
 * 
 */
public interface Graph extends Data<Float>{

	/**
	 * Returns float array with float[i] containing a value
	 * 
	 * Resolutions should be powers of two: 1,2,4,8,16,32,64,128,256,...
	 * 
	 * Their corresponding indices are: 0,1,2,3,4,5,6,7,8,...
	 * 
	 * @param start
	 *            zero based coordinate of the start
	 * @param end
	 *            zero based coordinate of the end, non-inclusive
	 * @param resolutionIndex
	 *            index of the desired resolution
	 */
	public float[] get(int start, int end, int resolutionIndex);

	public float min();

	public float max();

	//public String getName();

	/**
	 * @param pos one based coordinate
	 * @return
	 */
	public float value(int pos);

}
