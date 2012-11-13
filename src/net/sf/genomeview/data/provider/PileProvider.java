/**
 * %HEADER%
 */
package net.sf.genomeview.data.provider;

import org.broad.igv.track.WindowFunction;

import net.sf.genomeview.data.Model;
import net.sf.jannot.pileup.DoublePile;
import net.sf.jannot.pileup.Pile;
/**
 * 
 * @author Thomas Abeel
 *
 */
public abstract class PileProvider extends AbstractDataProvider<Pile> {
	

	public PileProvider(Model model) {
		super(model);
	}

	public abstract double getMaxPile();

	public abstract WindowFunction[] getWindowFunctions();

	public abstract void requestWindowFunction(WindowFunction wf) ;

	public abstract boolean isCurrentWindowFunction(WindowFunction wf);

	
//	public abstract double getMaxSummary();

}
