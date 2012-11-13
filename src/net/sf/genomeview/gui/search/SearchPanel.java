/**
 * %HEADER%
 */
package net.sf.genomeview.gui.search;

import javax.swing.JComponent;

import be.abeel.gui.GridBagPanel;

/**
 * 
 * @author Thomas Abeel
 * 
 */
public class SearchPanel extends GridBagPanel {

	private static final long serialVersionUID = -8948245093455638571L;
	private JComponent focusField;

	void setFocusField(JComponent text) {
		this.focusField = text;

	}

	JComponent getFocusComponent() {
		return focusField;
	}

}
