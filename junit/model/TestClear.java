/**
 * %HEADER%
 */
package junit.model;

import java.util.concurrent.ExecutionException;

import net.sf.genomeview.data.Model;
import net.sf.genomeview.data.ReadWorker;
import net.sf.jannot.EntrySet;
import net.sf.jannot.exception.ReadFailedException;
import net.sf.jannot.source.DataSource;

import org.junit.Assert;
import org.junit.Test;

/**
 * 
 * @author Thomas Abeel
 * 
 */
public class TestClear extends Model {

	public TestClear(String id, String config) {
		super(id, config);
		// TODO Auto-generated constructor stub
	}

	@Test
	public void testClearEntries() {
		Model model = new Model(null,null);
		try {
			TestDataSource ds = new TestDataSource();
			ReadWorker rw = new ReadWorker(ds, model);
			rw.execute();
			rw.get();
		} catch (InterruptedException e) {
			Assert.fail();
			e.printStackTrace();
		} catch (ExecutionException e) {
			Assert.fail();
			e.printStackTrace();
		}
		Assert.assertEquals(5, model.noEntries());
		model.clearEntries();
		Assert.assertEquals(0, model.noEntries());

	}

	class TestDataSource extends DataSource {

		@Override
		public EntrySet read(EntrySet add) throws ReadFailedException {
			for (int i = 0; i < 5; i++)
				add.getOrCreateEntry("entry" + i);
			return add;
		}

		@Override
		public void finalize() {
			// TODO Auto-generated method stub

		}

	}
}
