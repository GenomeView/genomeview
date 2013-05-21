/**
 * %HEADER%
 */
package net.sf.genomeview.gui;

import java.util.Observable;
import java.util.Observer;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JToolBar;

import net.sf.genomeview.core.Configuration;
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

	
	private static final long serialVersionUID = -3468536836400268783L;

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
		add(new JLabel(MessageManager.getString("toolbar.chromosome")));
		final JComboBox cb=new JComboBox(new EntryListModel(model));
		model.addObserver(new Observer() {
			
			@Override
			public void update(Observable o, Object arg) {
				cb.repaint();
				
			}
		});
		add(cb);
		
		if (Configuration.getBoolean("geneticCodeSelection")) {
			add(new JLabel(MessageManager.getString("toolbar.code")));
			add(new AAMappingChooser(model));
		}
		
	}
}
