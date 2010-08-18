/**
 * %HEADER%
 */
package net.sf.genomeview.gui.menu;

import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.SortedSet;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;

import net.sf.genomeview.data.Model;
import net.sf.genomeview.gui.annotation.track.StructureTrack;
import net.sf.genomeview.gui.annotation.track.TickmarkTrack;
import net.sf.genomeview.gui.annotation.track.Track;
import net.sf.genomeview.gui.menu.edit.CloneFeatureAction;
import net.sf.genomeview.gui.menu.edit.CreateNewFeatureAction;
import net.sf.genomeview.gui.menu.edit.EditStructureAction;
import net.sf.genomeview.gui.menu.edit.MergeFeatureAction;
import net.sf.genomeview.gui.menu.edit.RemoveAction;
import net.sf.genomeview.gui.menu.edit.SplitFeatureAction;
import net.sf.genomeview.gui.menu.selection.ClearFeatureSelectionAction;
import net.sf.genomeview.gui.menu.selection.ClearRegionSelectionAction;
import net.sf.genomeview.gui.menu.selection.ShowSequenceWindowAction;
import net.sf.jannot.Feature;
import net.sf.jannot.Qualifier;

public class PopUpMenu extends JPopupMenu {

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
			List<Qualifier> lq = f.qualifier("url");
			for (Qualifier q : lq) {
				String name = q.getValue().split(":")[0];
				String url = q.getValue().substring(q.getValue().indexOf(':') + 1);
				actions.add(new OpenURLAction(name, url));
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
		/* Track specific actions, if there is a track */
		if (t != null) {
			List<JMenuItem> list = t.getMenuItems();
			for (JMenuItem a : list) {
				if (a.isEnabled()) {
					count++;
					add(a);
				}
			}
			if (count > 0)
				addSeparator();

			JMenuItem clear = new JMenuItem(new AbstractAction("Unload track") {

				@Override
				public void actionPerformed(ActionEvent e) {
					int result = JOptionPane.showConfirmDialog(model.getGUIManager().getParent(),
							"Are you sure you want to clear this track?");
					if (result == JOptionPane.YES_OPTION) {
						model.remove(t);
					}
				}

			});
			add(clear);

		}

		if (this.getComponentCount() == 0) {
			add(new AbstractAction("No actions available") {

				@Override
				public boolean isEnabled() {
					return false;
				}

				public void actionPerformed(ActionEvent e) {

				}

			});
		}

	}

	private void addC(Action a) {
		if (a.isEnabled()) {
			count++;
			add(a);
		}

	}

}
