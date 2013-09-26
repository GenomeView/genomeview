package net.sf.genomeview.data;

import java.io.File;
import java.io.FileNotFoundException;

import net.sf.nameservice.NameService;

import org.junit.Assert;
import org.junit.Test;

/**
 * 
 * @author Thomas Abeel
 *
 */
public class TestSession {

	@Test
	public void testSessionParser() throws FileNotFoundException{
		Model model=new Model("TestModel");
		Thread t=Session.loadSession(model, new File("junit/resource/brokensession1.gvs"));
		try {
			t.join();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Assert.fail();
		}
		
	}
	
	@Test
	public void testAlias() throws FileNotFoundException{
		Model model=new Model("TestModel");
		Thread t=Session.loadSession(model, new File("junit/resource/testsession.gvs"));
		try {
			t.join();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Assert.fail();
		}
		Assert.assertEquals("Display name",NameService.getPrimaryName("genome"));
	}
}
