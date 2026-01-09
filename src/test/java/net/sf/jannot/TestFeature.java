package net.sf.jannot;

import java.util.ArrayList;
import java.util.List;



import org.junit.Assert;
import org.junit.Test;
import static  org.junit.Assert.*;

/**
 * 
 * @author Thomas Abeel
 * 
 */
public class TestFeature {

	
	@Test
	public void testEmptyCopy(){
		Feature f=new Feature();
		Feature g=f.copy();
	}
	@Test
	public void testFeatures() {

		Feature f=new Feature();
		f.addLocation(new Location(1,100));
		f.copy();
		
	}
	
	@Test
	public void testFeatureSetLocation(){
		Feature f=new Feature();
		f.setLocation(new Location(1,10));
		Assert.assertEquals(1, f.start());
		Assert.assertEquals(10, f.end());
		
		f.setLocation(new Location(5,15));
		Assert.assertEquals(5, f.start());
		Assert.assertEquals(15, f.end());
		
		f.setLocation(new Location[]{new Location(3,13)});
		Assert.assertEquals(3, f.start());
		Assert.assertEquals(13, f.end());
		
		f.setLocation(new Location[]{new Location(4,8),new Location(12,16)});
		Assert.assertEquals(4, f.start());
		Assert.assertEquals(16, f.end());
		
		f.addLocation(new Location(17,22));
		Assert.assertEquals(4, f.start());
		Assert.assertEquals(22, f.end());
		
		List<Location> list=new ArrayList<Location>();
		list.add(new Location(5,7));
		list.add(new Location(8,11));
		f.setLocation(list);
		
		Assert.assertEquals(5, f.start());
		Assert.assertEquals(11, f.end());
		
		list=new ArrayList<Location>();
		list.add(new Location(17,31));
		f.setLocation(list);
		
		Assert.assertEquals(17, f.start());
		Assert.assertEquals(31, f.end());
		
		
	}
	
	@Test
	public void testQualifier() {
		Feature f=new Feature();
		
		assertTrue(f.getQualifiersKeys().size()==0);
		
		f.addQualifier("protein", "test");
		assertTrue(f.getQualifiersKeys().size()==1);
		assertEquals("test", f.qualifier("protein"));
		
		f.addQualifier("protein", "more");
		assertTrue(f.getQualifiersKeys().size()==1);
		assertEquals("test,more", f.qualifier("protein"));
		
		f.removeQualifier("protein");
		assertTrue(f.getQualifiersKeys().size()==0);
		assertEquals(null, f.qualifier("protein"));
		
	}

}
