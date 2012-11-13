/**
 * %HEADER%
 */
package net.sf.genomeview.gui.menu;

import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.SortedSet;
import java.util.logging.Logger;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JPopupMenu;

import net.sf.genomeview.data.Model;
import net.sf.genomeview.gui.menu.edit.CloneFeatureAction;
import net.sf.genomeview.gui.menu.edit.CreateNewFeatureAction;
import net.sf.genomeview.gui.menu.edit.EditStructureAction;
import net.sf.genomeview.gui.menu.edit.MergeFeatureAction;
import net.sf.genomeview.gui.menu.edit.RemoveAction;
import net.sf.genomeview.gui.menu.edit.SplitFeatureAction;
import net.sf.genomeview.gui.menu.selection.ClearFeatureSelectionAction;
import net.sf.genomeview.gui.menu.selection.ClearRegionSelectionAction;
import net.sf.genomeview.gui.menu.selection.ShowSequenceWindowAction;
import net.sf.genomeview.gui.viztracks.Track;
import net.sf.jannot.Feature;

/**
 * 
 * @author Thomas Abeel
 * 
 */
public class PopUpMenu extends JPopupMenu {
	private Logger log = Logger.getLogger(PopUpMenu.class.toString());
	private static final long serialVersionUID = 2573433669184123608L;

	private int count = 0;

	public PopUpMenu(final Model model, final Track t) {

		count = 0;

		/* Selected feature actions */
		addC(new RemoveAction(model));
		addC(new EditStructureAction(model));
		addC(new ClearFeatureSelectionAction(model));
		addC(new ShowSequenceWindowAction(model));
		if (count > 0)
			addSeparator();
		count = 0;
		addC(new ClearRegionSelectionAction(model));

		addC(new CreateNewFeatureAction(model));
		addC(new CloneFeatureAction(model));
		addC(new MergeFeatureAction(model));
		addC(new SplitFeatureAction(model));

		if (count > 0)
			addSeparator();
		count = 0;

		SortedSet<Feature> sf = model.selectionModel().getFeatureSelection();
		List<Action> actions = new ArrayList<Action>();
		for (Feature f : sf) {
			if (f.qualifier("url") != null) {
				for (String q : f.qualifier("url").split(",")) {
					String name = q.split(":")[0];
					String url = q.substring(q.indexOf(':') + 1);
					if (name.startsWith("http")) {
						url = q;
						name = "Web query";
					}
					actions.add(new OpenURLAction(name, url));
				}
			}
		}
		if (actions.size() > 0)
			addSeparator();
		for (Action a : actions)
			add(a);
		count += actions.size();

		if (count > 0)
			addSeparator();
		count = 0;

		
		add(new AbstractAction("Configure track") {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				
				t.config().setConfigVisible(true);
				
			}
		});
		
		
		

	}

	private void addC(Action a) {
		if (a.isEnabled()) {
			count++;
			add(a);
		}

	}

}
