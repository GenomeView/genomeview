package net.sf.genomeview.ascomponent;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;

import javax.swing.JFrame;
import javax.swing.JPanel;

import net.sf.genomeview.data.DataSourceHelper;
import net.sf.genomeview.data.Model;
import net.sf.genomeview.gui.MainContent;
import net.sf.jannot.exception.ReadFailedException;
import net.sf.jannot.source.Locator;
import tudelft.utilities.logging.ReportToLogger;
import tudelft.utilities.logging.Reporter;

/**
 * 
 * @author Thomas Abeel
 *
 */
public class GenomeViewAsComponent {

	public static void main(String[] args) throws MalformedURLException,
			IOException, ReadFailedException, URISyntaxException {
		JFrame frame = new JFrame("GenomeView as component demo");
		Model model = new Model(null);
		model.getGUIManager().registerMainWindow(frame);
		JPanel[] content = MainContent.createContent(model, 1);
		frame.setContentPane(content[0]);
		frame.pack();
		frame.setVisible(true);

		// quick workaround, logger needs to be redirected to a console
		final Reporter log = new ReportToLogger(
				GenomeViewAsComponent.class.toString());

		DataSourceHelper.load(model, new Locator(
				"http://www.broadinstitute.org/software/genomeview/demo_c_elegans/IV.fasta.gz"),
				log);

		DataSourceHelper.load(model, new Locator(
				"http://www.broadinstitute.org/software/genomeview/demo_c_elegans/IV.gff.gz "),
				log);

		DataSourceHelper.load(model, new Locator(
				" http://www.broadinstitute.org/software/genomeview/demo_c_elegans/uwgs-rw_L2_FC6218_3.CHROMOSOME_IV.sorted.bam"),
				log);

	}
}
