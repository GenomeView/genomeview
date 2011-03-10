/**
 * %HEADER%
 */
package net.sf.genomeview.data.provider;

import net.sf.jannot.pileup.Pile;
/**
 * 
 * @author Thomas Abeel
 *
 */
public abstract class PileProvider extends AbstractDataProvider<Pile> {

	public abstract double getMaxPile();

	public abstract double getMaxSummary();

}
