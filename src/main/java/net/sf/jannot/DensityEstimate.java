/**
 * %HEADER%
 */
package net.sf.jannot;

/**
 * @author Thomas Abeel
 *
 */
public interface DensityEstimate {

	public int getEstimateCount(Location l);
	public int getMaximumCoordinate();
}
