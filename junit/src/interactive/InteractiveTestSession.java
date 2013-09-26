package interactive;

import java.io.File;
import java.io.FileNotFoundException;

import net.sf.genomeview.data.Model;
import net.sf.genomeview.data.Session;

import org.junit.Assert;
import org.junit.Test;

public class InteractiveTestSession {
	@Test
	public void testSessionParser() throws FileNotFoundException {
		Model model = new Model("TestModel");
		Thread t = Session.loadSession(model, new File("junit/resource/brokensession1.gvs"));
		try {
			t.join();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Assert.fail();
		}

	}
}
