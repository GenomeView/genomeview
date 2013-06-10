/**
 * %HEADER%
 */
package net.sf.genomeview.data.provider;

import java.util.ArrayList;

import net.sf.genomeview.core.Configuration;
import net.sf.genomeview.core.NoFailIterable;
import net.sf.genomeview.data.GenomeViewScheduler;
import net.sf.genomeview.data.Model;
import net.sf.genomeview.data.Task;
import net.sf.jannot.Data;
import net.sf.jannot.Entry;
import net.sf.jannot.Location;
import net.sf.jannot.pileup.DoublePile;
import net.sf.jannot.shortread.ReadGroup;
import net.sf.jannot.tdf.TDFData;
import net.sf.samtools.SAMRecord;

import org.broad.igv.track.WindowFunction;

/**
 * 
 * @author Thomas Abeel
 * 
 */
public class ShortReadProvider implements DataProvider<SAMRecord> {

	private ReadGroup source;
	private int lastStart;
	private int lastEnd;

	public ShortReadProvider(Entry e, ReadGroup source, Model model) {
		this.source = source;

	}

	@Override
	public void get(final int start, final int end, final DataCallback<SAMRecord> cb) {

		/* New request */
		lastStart = start;
		lastEnd = end;

		/* Queue up retrieval */
		Task t = new Task(new Location(start, end)) {

			@Override
			public void run() {
				// When actually running, check again whether we still need
				// this data
				if (start != lastStart && end != lastEnd)
					return;

				Iterable<SAMRecord> fresh = source.get(start, end);
				ArrayList<SAMRecord> tmp = new ArrayList<SAMRecord>();
				for (SAMRecord p : fresh) {
					tmp.add(p);
				}
				/* Notify rendered that the data is ready */
				cb.dataReady(new Location(start, end), tmp);
			}

		};
		GenomeViewScheduler.submit(t);

	}

	public int readLength() {
		return source.readLength();
	}

	public SAMRecord getSecondRead(SAMRecord one) {
		return source.getSecondRead(one);
	}

	public SAMRecord getFirstRead(SAMRecord one) {
		return source.getFirstRead(one);
	}

}
