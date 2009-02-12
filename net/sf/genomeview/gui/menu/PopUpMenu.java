/**
 * %HEADER%
 */
package net.sf.genomeview.gui.menu;

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
import net.sf.genomeview.gui.menu.selection.ZoomToSelectedFeaturesAction;
import net.sf.genomeview.gui.menu.selection.ZoomToSelectedLocationAction;
import net.sf.genomeview.gui.menu.selection.ZoomToSelectionAction;


public class PopUpMenu extends JPopupMenu {

    private static final long serialVersionUID = 2573433669184123608L;

    public PopUpMenu(Model model) {
        add(new RemoveAction(model));
        add(new EditStructureAction(model));
        add(new ClearFeatureSelectionAction(model));
        add(new ShowSequenceWindowAction(model));
        addSeparator();

        add(new ClearRegionSelectionAction(model));
        add(new ZoomToSelectionAction(model));
        add(new ZoomToSelectedFeaturesAction(model));
        add(new ZoomToSelectedLocationAction(model));
        add(new CreateNewFeatureAction(model));
        add(new CloneFeatureAction(model));
        add(new MergeFeatureAction(model));
        add(new SplitFeatureAction(model));

    }

}
