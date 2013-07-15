/**
 * %HEADER%
 */
package net.sf.genomeview.plugin;

import java.awt.Component;
import java.awt.Frame;
import java.awt.dnd.DropTarget;
import java.util.ArrayList;
import java.util.List;

import javax.swing.Action;
import javax.swing.JMenu;
import javax.swing.JMenuItem;

import net.sf.genomeview.gui.StatusBar;
import net.sf.genomeview.gui.explorer.GenomeExplorerManager;
import net.sf.genomeview.gui.information.InformationFrame;
import net.sf.genomeview.gui.viztracks.GeneEvidenceLabel;

/**
 * Manages GUI components that are accessible for plugins.
 * 
 * @author Thomas Abeel
 * @author thpar
 * 
 */
public class GUIManager {

	/**
	 * The main window of the GUI belonging to this model.
	 */
	private Frame parent;

	public GUIManager() {

	}

	public Frame getParent() {
		return parent;
	}

	public void startPluginLoading(){
		togglePluginLoadingMessage(true);
	}
	
	public void finishPluginLoading(){
		togglePluginLoadingMessage(false);
	}
	
	/* There can be multiple menus as there will be one for each screen */
	private List<JMenu> pluginMenu = new ArrayList<JMenu>();
	private List<JMenu> pluginDoc = new ArrayList<JMenu>();

	public void registerPluginMenu(JMenu plugin) {
		this.pluginMenu.add(plugin);

	}

	public void registerPluginDocumentationMenu(JMenu pluginDoc) {
		this.pluginDoc.add(pluginDoc);
	}

	public void addPluginAction(Action a, String pathMenu) {
		for (JMenu menu : pluginMenu)
			getMenu(menu, pathMenu).add(a);
	}
	
	public void addPluginMenuItem(JMenuItem i, String pathMenu){
		for (JMenu menu : pluginMenu)
			getMenu(menu, pathMenu).add(i);
	}

	/**
	 * Recursive method to find the actual menu.
	 * 
	 * @param moduleMenu
	 * @param menu
	 * @return
	 */
	private static JMenu getMenu(JMenu moduleMenu, String menu) {
		// System.out.println("MM:"+moduleMenu);
		String[] arr = menu.split("::");
		if (arr.length > 1) {// still more submenus
			JMenu check = null;
			Component[] comps = moduleMenu.getMenuComponents();
			// System.out.println("LM:" + moduleMenu);
			// System.out.println(comps.length);
			for (Component c : comps) {
				// System.out.println("C::" + c);
				if (c instanceof JMenu) {
					JMenu cMenu = (JMenu) c;
					if (cMenu.getText().equals(arr[0]))
						check = cMenu;
				}
			}
			if (check == null) {
				check = new JMenu(arr[0]);
				moduleMenu.add(check);
			}
			String newMenu = arr[1];
			for (int i = 2; i < arr.length; i++) {
				newMenu += "::" + arr[i];
			}
			return getMenu(check, newMenu);
		} else {
			// System.out.println("MM:" + moduleMenu);
			Component[] comps = moduleMenu.getMenuComponents();
			// System.out.println("\t" + comps.length);
			for (Component c : comps) {
				// System.out.println("C::" + c);
				if (c instanceof JMenu) {
					JMenu cMenu = (JMenu) c;
					if (cMenu.getText().equals(arr[0]))
						return cMenu;
				}
			}
			JMenu fresh = new JMenu(arr[0]);
			moduleMenu.add(fresh);
			return fresh;
		}

	}

	public void addPluginDocumentation(Action a) {
		for (JMenu menu : pluginDoc)
			menu.add(a);

	}

	private GeneEvidenceLabel gel = null;
	private StatusBar statusBar;
	private GenomeExplorerManager genomeExplorerManager;
	private InformationFrame infoFrame;
	private JMenuItem pluginLoadingMessage;


	public StatusBar getStatusBar() {
		return statusBar;
	}

	public void registerEvidenceLabel(GeneEvidenceLabel gel) {
		this.gel = gel;
	}

	public GeneEvidenceLabel getEvidenceLabel() {
		return gel;
	}

	public void registerStatusBar(StatusBar statusBar) {
		this.statusBar = statusBar;

	}

	public void registerMainWindow(Frame parentFrame) {
		this.parent = parentFrame;

	}

	public void registerGenomeExplorer(GenomeExplorerManager genomeExplorerManager) {
		this.genomeExplorerManager = genomeExplorerManager;

	}

	public GenomeExplorerManager getGenomeExplorer() {
		return genomeExplorerManager;
	}

	public void registerInformationFrame(InformationFrame infoFrame){
		this.infoFrame = infoFrame;
	}
	
	public InformationFrame getInformationFrame(){
		return infoFrame;
	}

	
	/**
	 * Give the {@link GUIManager} a reference to the "Plugin Loading" message 
	 * in the plugin menu.
	 * 
	 * @param tempItem
	 */
	public void registerPluginMenuToggle(JMenuItem tempItem) {
		pluginLoadingMessage = tempItem;
		
	}
	
	/**
	 * Toggle the "Plugin loading" message (in)visible.
	 * 
	 * @param visible
	 */
	public void togglePluginLoadingMessage(boolean visible){
		this.pluginLoadingMessage.setVisible(visible);
	}

}
