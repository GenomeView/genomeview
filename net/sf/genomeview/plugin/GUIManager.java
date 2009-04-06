/**
 * %HEADER%
 */
package net.sf.genomeview.plugin;

import java.awt.Component;

import javax.swing.Action;
import javax.swing.JMenu;

/**
 * Manages GUI components that are accessible for plugins.
 * 
 * @author Thomas Abeel
 * 
 */
public class GUIManager {

    private JMenu pluginMenu;
	private JMenu pluginDoc;

    public void registerPluginMenu(JMenu plugin) {
        this.pluginMenu=plugin;
        
    }
    
    public void registerPluginDocumentationMenu(JMenu pluginDoc){
    	this.pluginDoc=pluginDoc;
    }
    

    public void addPluginAction(Action a,String pathMenu){
        getMenu(pluginMenu, pathMenu).add(a);
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
		pluginDoc.add(a);
	}

}
