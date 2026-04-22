package net.sf.genomeview.data;

import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;

import org.junit.Test;

import net.sf.genomeview.core.DistributingReporter;
import net.sf.genomeview.gui.viztracks.TickmarkTrack;
import net.sf.genomeview.gui.viztracks.Track;
import net.sf.genomeview.gui.viztracks.annotation.StructureTrack;
import net.sf.jannot.Entry;
import net.sf.jannot.StringKey;
import net.sf.jannot.SyntenicData;
import net.sf.jannot.exception.ReadFailedException;
import net.sf.nameservice.NameService;

public class TrackListTest {

	@Test
	public void testThreadSafeIterator()
			throws IOException, ReadFailedException {
		DistributingReporter log = new DistributingReporter();
		NameService.init(log);
		Model model = new Model("id", log);

		TrackList tracklist = new TrackList(model);
		// add an extra entry
		tracklist.update(getEntry());

		// to test thread safety we get an iterator,
		// and then injedt a new Entry
		Iterator<Track> it = tracklist.iterator();
		assertTrue(it.next() instanceof TickmarkTrack);
		assertTrue(it.next() instanceof StructureTrack);
		tracklist.clear();
		it.next();
	}

	/**
	 * @return a test entry
	 */
	private Entry getEntry() {
		Entry e = new Entry("entry");
		SyntenicData syntenic = new SyntenicData(new ArrayList<>());
		e.add(new StringKey("data1"), syntenic);
		return e;
	}
}