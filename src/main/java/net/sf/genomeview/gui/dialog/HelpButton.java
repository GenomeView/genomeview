/**
 * %HEADER%
 */
package net.sf.genomeview.gui.dialog;

import java.awt.Window;

import javax.swing.JLabel;

import net.sf.genomeview.core.Icons;
import net.sf.genomeview.gui.components.OverlayListener;
/**
 * 
 * @author Thomas Abeel
 *
 */
public class HelpButton extends JLabel {

	private static final long serialVersionUID = -7184331819811186958L;

	public HelpButton(final Window parent, final String message) {
		super(Icons.HELP);
		super.addMouseListener(new OverlayListener(message));
	}

}
