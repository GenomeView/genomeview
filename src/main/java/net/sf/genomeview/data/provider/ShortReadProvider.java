/**
 * %HEADER%
 */
package net.sf.genomeview.data.provider;

import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.Level;

import htsjdk.samtools.SAMRecord;
import net.sf.genomeview.data.GenomeViewScheduler;
import net.sf.genomeview.data.Model;
import net.sf.genomeview.data.Task;
import net.sf.jannot.Entry;
import net.sf.jannot.Location;
import net.sf.jannot.shortread.ReadGroup;

/**
 * 
 * @author Thomas Abeel
 * 
 */
public class ShortReadProvider implements DataProvider<SAMRecord> {

	private ReadGroup source;
	private int lastStart;
	private int lastEnd;
	private Model model;

	public ShortReadProvider(Entry e, ReadGroup source, Model model) {
		this.source = source;
		this.model = model;

	}

	@Override
	public void get(final int start, final int end,
			final DataCallback<SAMRecord> cb) throws IOException {

		/* New request */
		lastStart = start;
		lastEnd = end;

		/* Queue up retrieval */
		Task t = new Task(new Location(start, end)) {

			@Override
			public void run() {
				try {
					// When actually running, check again whether we still need
					// this data
					if (start != lastStart && end != lastEnd)
						return;

					ArrayList<SAMRecord> tmp = new ArrayList<SAMRecord>();
					for (SAMRecord p : source.get(start, end)) {
						tmp.add(p);
					}
					/* Notify rendered that the data is ready */
					cb.dataReady(new Location(start, end), tmp);
				} catch (Throwable e) {
					model.getLog().log(Level.SEVERE, "failed to load SAM data",
							e);
				}
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
