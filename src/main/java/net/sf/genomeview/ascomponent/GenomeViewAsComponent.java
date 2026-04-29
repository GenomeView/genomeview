package net.sf.genomeview.ascomponent;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;

import javax.swing.JFrame;
import javax.swing.JPanel;

import net.sf.genomeview.core.Globals;
import net.sf.genomeview.data.DataSourceHelper;
import net.sf.genomeview.data.Model;
import net.sf.genomeview.gui.MainContent;
import net.sf.jannot.exception.ReadFailedException;
import net.sf.jannot.source.Locator;
import tudelft.utilities.logging.Reporter;

/**
 * 
 * @author Thomas Abeel
 *
 */
public class GenomeViewAsComponent {

	// example files to load
	private static final String BAM_EX = " http://www.broadinstitute.org/software/genomeview/demo_c_elegans/uwgs-rw_L2_FC6218_3.CHROMOSOME_IV.sorted.bam";
	private static final String GFF_EX = "http://www.broadinstitute.org/software/genomeview/demo_c_elegans/IV.gff.gz ";
	private static final String FASTA_EX = "http://www.broadinstitute.org/software/genomeview/demo_c_elegans/IV.fasta.gz";

	public static void main(String[] args) throws MalformedURLException,
			IOException, ReadFailedException, URISyntaxException {
		// quick workaround, logger needs to be redirected to a console
		Globals globals = new Globals();

		JFrame frame = new JFrame("GenomeView as component demo");
		Model model = new Model(null, globals);
		model.getGUIManager().registerMainWindow(frame);
		JPanel[] content = MainContent.createContent(model, 1);
		frame.setContentPane(content[0]);
		frame.pack();
		frame.setVisible(true);

		Reporter log = globals.getLog();
		DataSourceHelper.load(model, new Locator(FASTA_EX, log));
		DataSourceHelper.load(model, new Locator(GFF_EX, log));
		DataSourceHelper.load(model, new Locator(BAM_EX, log));

	}
}
