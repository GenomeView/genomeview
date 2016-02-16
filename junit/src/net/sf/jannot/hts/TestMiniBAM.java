package net.sf.jannot.hts;

import junit.framework.Assert;
import net.sf.jannot.Data;
import net.sf.jannot.DataKey;
import net.sf.jannot.Entry;
import net.sf.jannot.EntrySet;
import net.sf.jannot.source.DataSource;
import net.sf.jannot.source.DataSourceFactory;
import net.sf.jannot.source.Locator;

import org.junit.Test;

import support.DataManager;

/**
 * 
 * @author Thomas Abeel
 * 
 */
public class TestMiniBAM {
	@Test
	public void testShortRead() {
		
		Locator fData = new Locator(DataManager.file("tworead.bam"));
		Locator fIndex = new Locator(DataManager.file("tworead.bam.bai"));
		
		try {
			DataSource ds = DataSourceFactory.create(fData, fIndex);
			Assert.assertNotNull(ds);
			EntrySet entries = ds.read();
			Entry  e=entries.getEntry("chr4");
			int dkCount=0;
			int readCount=0;
			for(DataKey dk:e){
				dkCount++;
				Data d=e.get(dk);
				for(Object o:d.get(73151000, 73152000)){
					readCount++;
					System.out.println(o);
				}
			}
			Assert.assertEquals(1, dkCount);
			Assert.assertEquals(2, readCount);
		} catch (Exception e) {
			e.printStackTrace();
			Assert.fail();
		}
	}
}
