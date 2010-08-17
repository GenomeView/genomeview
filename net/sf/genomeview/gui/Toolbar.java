/**
 * %HEADER%
 */
package net.sf.genomeview.gui;

import javax.swing.ImageIcon;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JToolBar;

import net.sf.genomeview.core.Configuration;
import net.sf.genomeview.core.Icons;
import net.sf.genomeview.data.Model;
import net.sf.genomeview.gui.components.AAMappingChooser;
import net.sf.genomeview.gui.menu.edit.RedoAction;
import net.sf.genomeview.gui.menu.edit.UndoAction;
import net.sf.genomeview.gui.menu.navigation.AnnotationMoveLeftAction;
import net.sf.genomeview.gui.menu.navigation.AnnotationMoveRightAction;
import net.sf.genomeview.gui.menu.navigation.AnnotationZoomInAction;
import net.sf.genomeview.gui.menu.navigation.AnnotationZoomOutAction;
/**
 * 
 * @author Thomas Abeel
 *
 */
public class Toolbar extends JToolBar {

	public Toolbar(Model model) {

		setFloatable(false);

		add(new UndoAction(model));
		add(new RedoAction(model));
		addSeparator();
		add(new AnnotationZoomInAction(model));
		add(new AnnotationZoomOutAction(model));
		add(new AnnotationMoveLeftAction(model));
		add(new AnnotationMoveRightAction(model));
		addSeparator();
		add(new JLabel("Entry:"));
		add(new JComboBox(new EntryListModel(model)));
		if (Configuration.getBoolean("geneticCodeSelection")) {
			add(new JLabel("Code:"));
			add(new AAMappingChooser(model));
		}
		add(new JLabel(new ImageIcon(Icons.class.getResource("/images/vib.png"))));

	}
}
