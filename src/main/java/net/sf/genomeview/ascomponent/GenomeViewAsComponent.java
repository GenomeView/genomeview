package net.sf.genomeview.ascomponent;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;

import javax.swing.JFrame;
import javax.swing.JPanel;

import net.sf.genomeview.core.DistributingReporter;
import net.sf.genomeview.data.DataSourceHelper;
import net.sf.genomeview.data.Model;
import net.sf.genomeview.gui.MainContent;
import net.sf.jannot.exception.ReadFailedException;
import net.sf.jannot.source.Locator;

/**
 * 
 * @author Thomas Abeel
 *
 */
public class GenomeViewAsComponent {

	public static void main(String[] args) throws MalformedURLException,
			IOException, ReadFailedException, URISyntaxException {
		// quick workaround, logger needs to be redirected to a console
		final DistributingReporter log = new DistributingReporter();

		JFrame frame = new JFrame("GenomeView as component demo");
		Model model = new Model(null, log);
		model.getGUIManager().registerMainWindow(frame);
		JPanel[] content = MainContent.createContent(model, 1);
		frame.setContentPane(content[0]);
		frame.pack();
		frame.setVisible(true);

		DataSourceHelper.load(model, new Locator(
				"http://www.broadinstitute.org/software/genomeview/demo_c_elegans/IV.fasta.gz"));

		DataSourceHelper.load(model, new Locator(
				"http://www.broadinstitute.org/software/genomeview/demo_c_elegans/IV.gff.gz "));

		DataSourceHelper.load(model, new Locator(
				" http://www.broadinstitute.org/software/genomeview/demo_c_elegans/uwgs-rw_L2_FC6218_3.CHROMOSOME_IV.sorted.bam"));

	}
}
