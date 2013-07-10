package net.sf.genomeview.gui.dialog;

import java.io.IOException;

import junit.framework.Assert;
import net.sf.genomeview.core.Configuration;
import net.sf.genomeview.data.Model;
import net.sf.genomeview.data.Session;

import org.junit.Test;

public class TestSaveDialog {

	@Test
	public void testSaveDialog1(){
		
		Model model=new Model("test");
		try {
			Session.loadSession(model, Configuration.getDirectory()+"/previous.gvs");
		} catch (IOException e) {
			Assert.fail();
			e.printStackTrace();
		}
		try {
			Thread.sleep(5000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		SaveDialog sd=new SaveDialog(model);
		
		Configuration.set("save:defaultLocation", "c:/test.bam");
		
		sd=new SaveDialog(model);
		
		
		Configuration.set("save:defaultLocation", "c:/test2.bam");
		Configuration.set("save:defaultParser","EMBL");
		Configuration.set("save:enableIncludeSequence","false");
		Configuration.set("save:enableEntrySelection", "false");
		Configuration.set("save:enableTypeSelection", "false");
		sd=new SaveDialog(model);
		
		
	}
	
}
