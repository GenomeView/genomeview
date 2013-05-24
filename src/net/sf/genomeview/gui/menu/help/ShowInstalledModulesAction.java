/**
 * %HEADER%
 */
package net.sf.genomeview.gui.menu.help;

import java.awt.event.ActionEvent;
import java.util.Collection;

import javax.swing.AbstractAction;
import javax.swing.JOptionPane;

import net.sf.genomeview.data.Model;
import net.sf.genomeview.gui.MessageManager;
import net.sf.genomeview.plugin.PluginLoader;

import org.java.plugin.registry.PluginDescriptor;


public class ShowInstalledModulesAction extends AbstractAction {

    private Model model;

    private static final long serialVersionUID = 4182067300462615334L;

    public ShowInstalledModulesAction(Model model) {
        super(MessageManager.getString("helpmenu.installed_modules"));
        this.model = model;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        Collection<PluginDescriptor> col = PluginLoader.pluginManager.getRegistry().getPluginDescriptors();
        String message = "<html>";
        for (PluginDescriptor p : col) {
            // PluginLoader.pluginManager.
            if (p.getId().equals("genomeview.core")) {
                message += "Plugin manager (" + p.getVersion() + ")<br/>";
            } else {
                message += p.getExtensions().iterator().next().getParameter("name").rawValue();
                message += " (" + p.getVersion() + ")<br/>";
                if (p.getDocumentation() != null)
                    message += "&nbsp;&nbsp;" + p.getDocumentation();
            }
            if (p.getExtensions().size() > 0) {
                System.out.println(p.getExtensions().iterator().next().getParameter("name").rawValue());
            }

        }
        message += "</html>";

        JOptionPane.showMessageDialog(model.getGUIManager().getParent(), message, "Installed modules", JOptionPane.INFORMATION_MESSAGE);
    }

}
