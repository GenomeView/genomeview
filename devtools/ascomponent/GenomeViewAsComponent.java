package devtools.ascomponent;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;

import javax.swing.JFrame;
import javax.swing.JPanel;

import net.sf.genomeview.data.Model;
import net.sf.genomeview.gui.MainContent;
import net.sf.genomeview.scheduler.ReadWorker;
import net.sf.jannot.exception.ReadFailedException;
import net.sf.jannot.source.DataSource;
import net.sf.jannot.source.DataSourceFactory;
import net.sf.jannot.utils.URIFactory;

public class GenomeViewAsComponent {

	public static void main(String[] args) throws MalformedURLException, IOException, ReadFailedException, URISyntaxException {
		JFrame frame = new JFrame("GenomeView as component demo");
		Model model=new Model();
		model.getGUIManager().registerMainWindow(frame);
		JPanel[] content=MainContent.createContent(model,1);
		frame.setContentPane(content[0]);
		frame.pack();
		frame.setVisible(true);
		
		DataSource ds = DataSourceFactory.createURL(URIFactory.url("http://www.broadinstitute.org/software/genomeview/demo_c_elegans/IV.fasta.gz"));
		ReadWorker rf = new ReadWorker(ds, model);
		rf.execute();
		
		ds = DataSourceFactory.createURL(URIFactory.url("http://www.broadinstitute.org/software/genomeview/demo_c_elegans/IV.gff.gz"));
		rf = new ReadWorker(ds, model);
		rf.execute();
		
		ds = DataSourceFactory.createURL(URIFactory.url(" http://www.broadinstitute.org/software/genomeview/demo_c_elegans/uwgs-rw_L2_FC6218_3.CHROMOSOME_IV.sorted.bam.bai"));
		rf = new ReadWorker(ds, model);
		rf.execute();
	    

	}
}
