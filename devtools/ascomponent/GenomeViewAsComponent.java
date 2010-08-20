package devtools.ascomponent;

import javax.swing.JFrame;
import javax.swing.JPanel;

import net.sf.genomeview.data.Model;
import net.sf.genomeview.gui.MainContent;

public class GenomeViewAsComponent {

	public static void main(String[] args) {
		JFrame frame = new JFrame("GenomeView as component demo");
		Model model=new Model(frame);
		JPanel[] content=MainContent.createContent(model,1);
		frame.setContentPane(content[0]);
		frame.pack();
		frame.setVisible(true);
		model.addData(f)
	    * http://www.broadinstitute.org/software/genomeview/demo_c_elegans/IV.fasta.gz
	        * http://www.broadinstitute.org/software/genomeview/demo_c_elegans/IV.gff.gz
	        * http://www.broadinstitute.org/software/genomeview/demo_c_elegans/uwgs-rw_L2_FC6218_3.CHROMOSOME_IV.sorted.bam.bai

	}
}
