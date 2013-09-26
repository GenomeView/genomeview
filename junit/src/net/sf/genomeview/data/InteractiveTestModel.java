/**
 * %HEADER%
 */
package net.sf.genomeview.data;


import java.util.concurrent.ExecutionException;

import net.sf.genomeview.core.Configuration;

import org.junit.Assert;
import org.junit.Test;

/**
 * 
 * @author Thomas Abeel
 * 
 */
public class InteractiveTestModel {

	String v=Configuration.get("version");
	
//	static class TModel extends Model{
//		public TModel(String id) {
//			super(id);
//	
//		}
//	}
//	
	

	@Test
	public void testClearEntries() {
		Model model = new Model(null);
		try {
			TDataSource ds = new TDataSource();
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

	
}
