/**
 * %HEADER%
 */
package net.sf.genomeview.data;

import java.util.concurrent.ExecutionException;

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
public class TestModel {

	class TModel extends Model{
		public TModel(String id) {
			super(id);
	
		}
	}
	
	

	@Test
	public void testClearEntries() {
		Model model = new Model(null);
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

		protected TestDataSource() {
			super(null);
			// TODO Auto-generated constructor stub
		}

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

		@Override
		public boolean isIndexed() {
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public long size() {
			// TODO Auto-generated method stub
			return 0;
		}

	

	}
}
