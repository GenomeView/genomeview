/**
 * %HEADER%
 */
package net.sf.genomeview.data.provider;

import org.broad.igv.track.WindowFunction;

import net.sf.jannot.pileup.Pile;
/**
 * 
 * @author Thomas Abeel
 *
 */
public abstract class PileProvider extends AbstractDataProvider<Pile> {
	protected void notifyListeners(){
		setChanged();
		notifyObservers();
	}
	public abstract double getMaxPile();

	public abstract WindowFunction[] getWindowFunctions();

	public abstract void requestWindowFunction(WindowFunction wf) ;

	public abstract boolean isCurrentWindowFunction(WindowFunction wf);

//	public abstract double getMaxSummary();

}
