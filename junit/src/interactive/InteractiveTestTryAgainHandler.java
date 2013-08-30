package interactive;

import net.sf.genomeview.data.DataSourceHelper;
import net.sf.genomeview.data.Model;
import net.sf.genomeview.gui.dialog.TryAgainHandler;
import net.sf.jannot.source.Locator;

public class InteractiveTestTryAgainHandler {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		final Model model=new Model(null);
		TryAgainHandler.ask(model, "Let's try again", new Runnable() {
			public void run() {
				try {
					DataSourceHelper.load(model, new Locator("http://www.broadinstitute.org/software/genomeview/demo/c_elegans//IV.gff.gz"));
				} catch (Exception e) {
					throw new RuntimeException(e);
				}
			}
		});

	}

}
