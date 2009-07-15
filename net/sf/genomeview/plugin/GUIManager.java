/**
 * %HEADER%
 */
package net.sf.genomeview.plugin;

import java.awt.Component;
import java.util.ArrayList;
import java.util.List;

import javax.swing.Action;
import javax.swing.JMenu;

/**
 * Manages GUI components that are accessible for plugins.
 * 
 * @author Thomas Abeel
 * 
 */
public class GUIManager {

	/* There can be multiple menus as ther wil be one for each screen */
    private List<JMenu> pluginMenu=new ArrayList<JMenu>();
	private List<JMenu> pluginDoc=new ArrayList<JMenu>();

    public void registerPluginMenu(JMenu plugin) {
        this.pluginMenu.add(plugin);
        
    }
    
    public void registerPluginDocumentationMenu(JMenu pluginDoc){
    	this.pluginDoc.add(pluginDoc);
    }
    

    public void addPluginAction(Action a,String pathMenu){
    	for(JMenu menu:pluginMenu)
    		getMenu(menu, pathMenu).add(a);
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
		for(JMenu menu:pluginDoc)
    		menu.add(a);
		
	}

}
