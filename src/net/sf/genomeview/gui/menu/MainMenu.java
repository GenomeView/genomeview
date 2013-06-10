/**
 * %HEADER%
 */
package net.sf.genomeview.gui.menu;

import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;

import net.sf.genomeview.data.Model;
import net.sf.genomeview.gui.MessageManager;
import net.sf.genomeview.gui.components.OverlayListener;
import net.sf.genomeview.gui.menu.edit.CloneFeatureAction;
import net.sf.genomeview.gui.menu.edit.CopySequenceAction;
import net.sf.genomeview.gui.menu.edit.CreateNewFeatureAction;
import net.sf.genomeview.gui.menu.edit.CreateNewFeatureFromCoordinatesAction;
import net.sf.genomeview.gui.menu.edit.EditStructureAction;
import net.sf.genomeview.gui.menu.edit.ExtendToStartCodonAction;
import net.sf.genomeview.gui.menu.edit.ExtendToStopCodonAction;
import net.sf.genomeview.gui.menu.edit.MergeFeatureAction;
import net.sf.genomeview.gui.menu.edit.RemoveAction;
import net.sf.genomeview.gui.menu.edit.RemoveLocationAction;
import net.sf.genomeview.gui.menu.edit.SplitFeatureAction;
import net.sf.genomeview.gui.menu.file.ClearEntriesAction;
import net.sf.genomeview.gui.menu.file.ExitAction;
import net.sf.genomeview.gui.menu.file.ExportDataAction;
import net.sf.genomeview.gui.menu.file.LoadFeaturesAction;
import net.sf.genomeview.gui.menu.file.LoadSessionAction;
import net.sf.genomeview.gui.menu.file.SaveAction;
import net.sf.genomeview.gui.menu.file.SaveImage;
import net.sf.genomeview.gui.menu.file.SaveSessionAction;
import net.sf.genomeview.gui.menu.file.ShowConfigurationAction;
import net.sf.genomeview.gui.menu.file.ShowGenomeExplorerAction;
import net.sf.genomeview.gui.menu.help.ShowAboutDialogAction;
import net.sf.genomeview.gui.menu.help.ShowInstalledModulesAction;
import net.sf.genomeview.gui.menu.navigation.GotoPosition;
import net.sf.genomeview.gui.menu.navigation.GotoTrack;
import net.sf.genomeview.gui.menu.navigation.SearchAction;
import net.sf.genomeview.gui.menu.selection.ClearFeatureSelectionAction;
import net.sf.genomeview.gui.menu.selection.ClearRegionSelectionAction;
import net.sf.genomeview.gui.menu.selection.ShowSequenceWindowAction;
import net.sf.genomeview.gui.menu.selection.ZoomToSelectedFeaturesAction;
import net.sf.genomeview.gui.menu.selection.ZoomToSelectedLocationAction;
import net.sf.genomeview.gui.menu.selection.ZoomToSelectionAction;

public class MainMenu extends JMenuBar {

	private static final long serialVersionUID = 6478474621947392346L;

	public MainMenu(Model model) {
		JMenu file = new JMenu(MessageManager.getString("mainmenu.file"));
		
		JMenuItem i=new JMenuItem(new LoadFeaturesAction(model));
		OverlayListener ol=new OverlayListener(MessageManager.getString("mainmenu.load_info"));
		i.addMouseListener(ol);
		i.addActionListener(ol);
		file.add(i);
		
		file.addSeparator();
		file.add(new ShowGenomeExplorerAction(model));
		file.add(new SaveSessionAction(model));
		file.add(new LoadSessionAction(model));
		
		file.addSeparator();
		file.add(new SaveAction(model));
		file.add(new ExportDataAction(model));
		file.add(new SaveImage(model));

		file.addSeparator();
		file.add(new ShowConfigurationAction(model));
		file.add(new ClearEntriesAction(model));
		file.addSeparator();
		file.add(new ExitAction(model));
		add(file);

		JMenu edit = new JMenu(MessageManager.getString("mainmenu.edit"));

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
		edit.add(new ExtendToStartCodonAction(model));
		edit.add(new ExtendToStopCodonAction(model));
		
		add(edit);

		JMenu navigation = new JMenu(MessageManager.getString("mainmenu.navigation"));
		navigation.add(new GotoPosition(model));
		navigation.add(new GotoTrack(model));
		navigation.add(new SearchAction(model));
		add(navigation);

		JMenu select = new JMenu(MessageManager.getString("mainmenu.selection"));
		select.add(new ShowSequenceWindowAction(model));
//		select.add(new NCBIdnaBlastAction(model));
//		select.add(new NCBIproteinBlastAction(model));
//		select.add(new NCBIselectedDnaBlastAction(model));
		select.addSeparator();
		select.add(new ClearFeatureSelectionAction(model));
		select.add(new ClearRegionSelectionAction(model));
		select.addSeparator();
		select.add(new ZoomToSelectionAction(model));
		select.add(new ZoomToSelectedFeaturesAction(model));
		select.add(new ZoomToSelectedLocationAction(model));
		add(select);


		JMenu plugin = new JMenu(MessageManager.getString("mainmenu.plugins"));
		model.getGUIManager().registerPluginMenu(plugin);
		add(plugin);

		JMenu help = new JMenu(MessageManager.getString("mainmenu.help"));
		help.add(new OpenURLAction(MessageManager.getString("mainmenu.user_documentation"),
						"http://genomeview.org/content/user-documentation"));
		help.add(new OpenURLAction(MessageManager.getString("mainmenu.post_bug_request"),
				"http://sourceforge.net/tracker/?group_id=208107"));
		help.add(new OpenURLAction(MessageManager.getString("mainmenu.mailing_list"), "https://lists.sourceforge.net/lists/listinfo/genomeview-support"));
		help.addSeparator();
		help.add(new ShowInstalledModulesAction(model));
//		help.add(new OpenURLAction("Official list plugins",
//				"http://genomeview.sourceforge.net/plugins"));
		JMenu pluginDoc=new JMenu(MessageManager.getString("mainmenu.plugin"));
		model.getGUIManager().registerPluginDocumentationMenu(pluginDoc);
		help.add(pluginDoc);
		help.addSeparator();
		help.add(new ShowAboutDialogAction(model));
		
		add(help);
	}
}
