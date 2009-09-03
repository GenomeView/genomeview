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
import net.sf.genomeview.gui.menu.file.ClearEntriesAction;
import net.sf.genomeview.gui.menu.file.ExitAction;
import net.sf.genomeview.gui.menu.file.LoadDirectoryAction;
import net.sf.genomeview.gui.menu.file.LoadFeaturesAction;
import net.sf.genomeview.gui.menu.file.LoadSessionAction;
import net.sf.genomeview.gui.menu.file.NewInstanceAction;
import net.sf.genomeview.gui.menu.file.SaveAction;
import net.sf.genomeview.gui.menu.file.SaveAsAction;
import net.sf.genomeview.gui.menu.file.SaveSessionAction;
import net.sf.genomeview.gui.menu.file.ShowConfigurationAction;
import net.sf.genomeview.gui.menu.help.ShowAboutDialogAction;
import net.sf.genomeview.gui.menu.help.ShowInstalledModulesAction;
import net.sf.genomeview.gui.menu.navigation.GotoPosition;
import net.sf.genomeview.gui.menu.navigation.SearchAction;
import net.sf.genomeview.gui.menu.selection.ClearFeatureSelectionAction;
import net.sf.genomeview.gui.menu.selection.ClearRegionSelectionAction;
import net.sf.genomeview.gui.menu.selection.NCBIdnaBlastAction;
import net.sf.genomeview.gui.menu.selection.NCBIproteinBlastAction;
import net.sf.genomeview.gui.menu.selection.NCBIselectedDnaBlastAction;
import net.sf.genomeview.gui.menu.selection.ShowSequenceWindowAction;
import net.sf.genomeview.gui.menu.selection.ZoomToSelectedFeaturesAction;
import net.sf.genomeview.gui.menu.selection.ZoomToSelectedLocationAction;
import net.sf.genomeview.gui.menu.selection.ZoomToSelectionAction;

public class MainMenu extends JMenuBar {

	private static final long serialVersionUID = 6478474621947392346L;

	public MainMenu(Model model, MainWindow mainWindow) {
		JMenu file = new JMenu("File");
		file.add(new NewInstanceAction(model));
		file.add(new ClearEntriesAction(model));
		file.add(new SaveSessionAction(model));
		file.add(new LoadSessionAction(model));
		file.addSeparator();
		file.add(new LoadFeaturesAction(model));
//		file.add(new LoadDirectoryAction(model));

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
		navigation.add(new GotoPosition(model));
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
		select.addSeparator();
		select.add(new ZoomToSelectionAction(model));
		select.add(new ZoomToSelectedFeaturesAction(model));
		select.add(new ZoomToSelectedLocationAction(model));
		add(select);


		JMenu plugin = new JMenu("Plugins");
		model.getGUIManager().registerPluginMenu(plugin);
		add(plugin);

		JMenu help = new JMenu("Help");
		help
				.add(new OpenURLAction("User documentation",
						"http://genomeview.sourceforge.net/content/user-documentation"));
		help.add(new OpenURLAction("Post bug report or feature request",
				"http://sourceforge.net/tracker/?group_id=208107"));
		
		help.addSeparator();
		help.add(new ShowInstalledModulesAction(model));
		help.add(new OpenURLAction("Official list plugins",
				"http://genomeview.sourceforge.net/plugins"));
		JMenu pluginDoc=new JMenu("Plugin documentation");
		model.getGUIManager().registerPluginDocumentationMenu(pluginDoc);
		help.add(pluginDoc);
		help.addSeparator();
		help.add(new ShowAboutDialogAction(model));
		add(help);

	}
}
