/**
 * %HEADER%
 */
package net.sf.genomeview.gui.annotation;

import java.awt.BorderLayout;
import java.awt.event.MouseWheelListener;
import java.util.Observable;
import java.util.Observer;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;

import net.sf.genomeview.data.Model;

/**
 * Frame for annotation. This contains the navigatoin panel and the annotation
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
        final JScrollPane tmp = new JScrollPane(evidenceLabel);
        evidenceLabel.setViewport(tmp.getViewport());
        tmp.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        tmp.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        MouseWheelListener listener = tmp.getMouseWheelListeners()[0];
        evidenceLabel.setScrollPaneListener(listener);
        add(tmp, BorderLayout.CENTER);
    }
}
