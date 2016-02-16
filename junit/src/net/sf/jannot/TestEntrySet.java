package net.sf.jannot;

import junit.framework.Assert;
import net.sf.jannot.Entry;
import net.sf.jannot.EntrySet;

import org.junit.Test;
/**
 * 
 * @author Thomas Abeel
 *
 */
public class TestEntrySet {
	@Test
	public void testGet() {
		EntrySet es=new EntrySet();
		Entry x=es.getOrCreateEntry("chr1");
		Entry y=es.getOrCreateEntry("2");
		Assert.assertNotNull(x);
		Assert.assertNotNull(y);
		System.out.println(es.getEntry("1"));
		Assert.assertNotNull(es.getEntry("1"));
		Assert.assertNotNull(es.getEntry("CHR1"));
		Assert.assertNotNull(es.getEntry("chr1"));
		
		System.out.println(es.getEntry("chr2"));
		Assert.assertNotNull(es.getEntry("chr2"));
		Assert.assertNotNull(es.getEntry("Chr2"));
		Assert.assertNotNull(es.getEntry("chR2"));
		
		
	}
}