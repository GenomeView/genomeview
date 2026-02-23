/**
 *    This file is part of GenomeView.
 *
 *    GenomeView is free software: you can redistribute it and/or modify
 *    it under the terms of the GNU General Public License as published by
 *    the Free Software Foundation, either version 3 of the License, or
 *    (at your option) any later version.
 *
 *    JAnnot is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *    GNU General Public License for more details.
 *
 *    You should have received a copy of the GNU General Public License
 *    along with GenomeView.  If not, see <http://www.gnu.org/licenses/>.
 */
package net.sf.genomeview.gui.components;

import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;

import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JRootPane;
import javax.swing.KeyStroke;

/**
 * 
 * @author Thomas Abeel
 * 
 */
public class EscapeDialog extends JDialog {

	/**
	 * 
	 */
	private static final long serialVersionUID = 207105337366199788L;

	public EscapeDialog(Frame parent, String string) {
		super(parent, string);
		init();
	}

	public EscapeDialog(Frame parent, String string, ModalityType applicationModal) {
		super(parent,string,applicationModal);
		init();
	}
	private void init(){
		ActionListener actionListener = new ActionListener() {
			public void actionPerformed(ActionEvent actionEvent) {
				setVisible(false);
			}
		};
		KeyStroke stroke = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0);
		getRootPane().registerKeyboardAction(actionListener, stroke, JComponent.WHEN_IN_FOCUSED_WINDOW);
	}

}
