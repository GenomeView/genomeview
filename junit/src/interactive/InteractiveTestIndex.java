package interactive;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;

import javax.swing.JFrame;

import net.sf.genomeview.data.DataSourceHelper;
import net.sf.genomeview.data.Model;
import net.sf.jannot.exception.ReadFailedException;
import net.sf.jannot.source.Locator;

import org.junit.Test;

import support.DataManager;

public class InteractiveTestIndex {
	
	@Test
	public void testAutoIndex(){
		File f= DataManager.file("tworead.bam");
		try {
			JFrame frame=new JFrame("dummy");
			frame.pack();
			frame.setVisible(true);
			Model model=new Model("test");
			model.getGUIManager().registerMainWindow(frame);
			DataSourceHelper.load(model, new Locator(f));
		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ReadFailedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
	}
}
