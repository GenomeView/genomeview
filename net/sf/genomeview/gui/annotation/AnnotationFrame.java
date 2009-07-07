/**
 * %HEADER%
 */
package net.sf.genomeview.gui.annotation;

import java.awt.BorderLayout;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;

import net.sf.genomeview.data.Model;

/**
 * Frame for annotation. This contains the gene structure panel and the evidence
 * panel.
 * 
 * @author Thomas Abeel
 * 
 */
public class AnnotationFrame extends JPanel {

    

	private static final long serialVersionUID = 2042233559397818212L;
	private GeneEvidenceLabel evidenceLabel;

    /* The supplied index is the index of the window that this panel is located. */
    public AnnotationFrame(int index, Model model) {
    	setLayout(new BorderLayout());
    	add(new Zoomer(model), BorderLayout.NORTH);
        /* gene structure and evidence panels */
    	evidenceLabel = new GeneEvidenceLabel(model);
        JScrollPane tmp = new JScrollPane(evidenceLabel);
        tmp.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        add(tmp, BorderLayout.CENTER);
    }
}
