/**
 * %HEADER%
 */
package net.sf.genomeview.gui.menu;

import javax.swing.JMenu;
import javax.swing.JMenuBar;

import net.sf.genomeview.data.Model;
import net.sf.genomeview.gui.MainWindow;
import net.sf.genomeview.gui.menu.edit.CloneFeatureAction;
import net.sf.genomeview.gui.menu.edit.CopySequenceAction;
import net.sf.genomeview.gui.menu.edit.CreateNewFeatureAction;
import net.sf.genomeview.gui.menu.edit.CreateNewFeatureFromCoordinatesAction;
import net.sf.genomeview.gui.menu.edit.EditStructureAction;
import net.sf.genomeview.gui.menu.edit.ExtendToStopCodonAction;
import net.sf.genomeview.gui.menu.edit.MergeFeatureAction;
import net.sf.genomeview.gui.menu.edit.RemoveAction;
import net.sf.genomeview.gui.menu.edit.RemoveLocationAction;
import net.sf.genomeview.gui.menu.edit.SplitFeatureAction;
import net.sf.genomeview.gui.menu.file.ExitAction;
import net.sf.genomeview.gui.menu.file.LoadDirectoryAction;
import net.sf.genomeview.gui.menu.file.LoadEntriesAction;
import net.sf.genomeview.gui.menu.file.LoadFeaturesAction;
import net.sf.genomeview.gui.menu.file.NewInstanceAction;
import net.sf.genomeview.gui.menu.file.SaveAction;
import net.sf.genomeview.gui.menu.file.SaveAsAction;
import net.sf.genomeview.gui.menu.help.ShowAboutDialogAction;
import net.sf.genomeview.gui.menu.help.ShowConfigurationAction;
import net.sf.genomeview.gui.menu.help.ShowInstalledModulesAction;
import net.sf.genomeview.gui.menu.navigation.CenterOnPositionAction;
import net.sf.genomeview.gui.menu.navigation.SearchAction;
import net.sf.genomeview.gui.menu.selection.ClearFeatureSelectionAction;
import net.sf.genomeview.gui.menu.selection.ClearRegionSelectionAction;
import net.sf.genomeview.gui.menu.selection.HideFeatureSelectionAction;
import net.sf.genomeview.gui.menu.selection.NCBIdnaBlastAction;
import net.sf.genomeview.gui.menu.selection.NCBIproteinBlastAction;
import net.sf.genomeview.gui.menu.selection.NCBIselectedDnaBlastAction;
import net.sf.genomeview.gui.menu.selection.ShowFeatureSelectionAction;
import net.sf.genomeview.gui.menu.selection.ShowSequenceWindowAction;
import net.sf.genomeview.gui.menu.selection.ZoomToSelectedFeaturesAction;
import net.sf.genomeview.gui.menu.selection.ZoomToSelectedLocationAction;
import net.sf.genomeview.gui.menu.selection.ZoomToSelectionAction;
import net.sf.genomeview.gui.menu.view.ChromosomeToggle;
import net.sf.genomeview.gui.menu.view.EvidenceToggle;
import net.sf.genomeview.gui.menu.view.StructureToggle;

public class MainMenu extends JMenuBar {

	private static final long serialVersionUID = 6478474621947392346L;

	public MainMenu(Model model, MainWindow mainWindow) {
		JMenu file = new JMenu("File");
		file.add(new NewInstanceAction(model));
		file.addSeparator();
		file.add(new LoadEntriesAction(model));
		file.add(new LoadFeaturesAction(model));
		file.add(new LoadDirectoryAction(model));

		file.addSeparator();
		file.add(new SaveAction(model));
		file.add(new SaveAsAction(model));

		file.addSeparator();
		file.add(new ShowConfigurationAction(model));
		file.addSeparator();
		file.add(new ExitAction(model));
		add(file);

		JMenu edit = new JMenu("Edit");

		edit.add(new CopySequenceAction(model));
		edit.add(new CloneFeatureAction(model));
		edit.add(new RemoveAction(model));
		edit.add(new RemoveLocationAction(model));
		edit.add(new EditStructureAction(model));
		edit.addSeparator();
		edit.add(new CreateNewFeatureFromCoordinatesAction(model));
		edit.add(new CreateNewFeatureAction(model));
		edit.add(new MergeFeatureAction(model));
		edit.add(new SplitFeatureAction(model));
		edit.add(new ExtendToStopCodonAction(model));
		add(edit);

		JMenu navigation = new JMenu("Navigation");
		navigation.add(new CenterOnPositionAction(model));
		navigation.add(new SearchAction(model));
		add(navigation);

		JMenu select = new JMenu("Selection");
		select.add(new ShowSequenceWindowAction(model));
		select.add(new NCBIdnaBlastAction(model));
		select.add(new NCBIproteinBlastAction(model));
		select.add(new NCBIselectedDnaBlastAction(model));
		select.addSeparator();
		select.add(new ClearFeatureSelectionAction(model));
		select.add(new ClearRegionSelectionAction(model));
		select.add(new HideFeatureSelectionAction(model));
		select.add(new ShowFeatureSelectionAction(model));
		select.addSeparator();
		select.add(new ZoomToSelectionAction(model));
		select.add(new ZoomToSelectedFeaturesAction(model));
		select.add(new ZoomToSelectedLocationAction(model));
		add(select);

		// Toggle buttons to show or hide the separate frames.
		JMenu view = new JMenu("View");

		// view.addSeparator();
		view.add(new ChromosomeToggle(model));
		view.add(new StructureToggle(model));
		view.add(new EvidenceToggle(model));
		// view.add(nfBox);
		add(view);

		JMenu plugin = new JMenu("Plugins");
		model.getGUIManager().registerPluginMenu(plugin);
		add(plugin);

		// Loader.loadAllModules(model, plugin);

		JMenu help = new JMenu("Help");
		help
				.add(new OpenURLAction("User documentation",
						"http://genomeview.sourceforge.net/content/user-documentation"));
		help.add(new OpenURLAction("Post bug report or feature request",
				"http://sourceforge.net/tracker/?group_id=208107"));
		// help.add(new OpenURLAction("Open project website",
		// "http://sf.net/projects/genomeview"));
		help.addSeparator();
		help.add(new ShowInstalledModulesAction(model));
		help.add(new OpenURLAction("Official list plugins",
				"http://genomeview.sourceforge.net/category/post-type/module"));
		// help.add(new OpenURLAction("Available modules",
		// "http://www.psb.ugent.be/~thabe/gvmodules/"));
		help.addSeparator();
		help.add(new ShowAboutDialogAction(model));
		add(help);

	}
}
