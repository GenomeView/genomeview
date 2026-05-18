package net.sf.genomeview.data;

import java.io.IOException;
import java.util.ArrayList;

import org.junit.Test;

import net.sf.genomeview.core.Globals;
import net.sf.jannot.Entry;
import net.sf.jannot.StringKey;
import net.sf.jannot.exception.ReadFailedException;
import net.sf.jannot.syntenic.SyntenicData;

public class TrackListTest {

	private final Globals globals;

	public TrackListTest() throws IOException, ReadFailedException {
		globals = new Globals();
	}

	@Test
	public void testThreadSafeIterator() {
		Model model = new Model("id", globals);

		TrackList tracklist = new TrackList(model);
		// add an extra entry
		tracklist.update(getEntry());
	}

	/**
	 * @return a test entry
	 */
	private Entry getEntry() {
		Entry e = new Entry("entry", globals.getGlobal());
		SyntenicData syntenic = new SyntenicData(new ArrayList<>(),
				globals.getGlobal());
		e.add(new StringKey("data1"), syntenic);
		return e;
	}
}