package junit.model;

import net.sf.genomeview.data.Model;
import net.sf.jannot.Entry;
import net.sf.jannot.exception.ReadFailedException;
import net.sf.jannot.exception.SaveFailedException;
import net.sf.jannot.source.DataSource;

import org.junit.Assert;
import org.junit.Test;

public class TestClear {

	@Test
	public void testClearEntries() {
		Model model = new Model(null);
		try {
			model.addEntries(new TestDataSource());
		} catch (ReadFailedException e) {

			e.printStackTrace();
			Assert.fail();
		}
		Assert.assertEquals(5, model.noEntries());
		model.clearEntries();
		Assert.assertEquals(0, model.noEntries());
		
		
	}

	class TestDataSource extends DataSource {

		@Override
		public boolean isDestructiveSave() {
			return false;
		}

		@Override
		public Entry[] read() throws ReadFailedException {
			Entry[] out = new Entry[5];
			for (int i = 0; i < out.length; i++)
				out[i] = new Entry(null);
			return out;
		}

		@Override
		public void saveOwn(Entry[] entries) throws SaveFailedException {
			// TODO Auto-generated method stub

		}

	}
}
