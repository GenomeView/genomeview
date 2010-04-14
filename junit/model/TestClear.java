package junit.model;

import net.sf.genomeview.data.Model;
import net.sf.jannot.Entry;
import net.sf.jannot.EntrySet;
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
			model.addData(new TestDataSource());
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
		public EntrySet read(EntrySet add) throws ReadFailedException {
			for(int i=0;i<5;i++)
				add.getOrCreateEntry("entry"+i);
			return add;
		}



		@Override
		public void finalize() {
			// TODO Auto-generated method stub
			
		}

	}
}
