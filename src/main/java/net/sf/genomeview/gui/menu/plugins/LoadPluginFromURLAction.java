package net.sf.genomeview.gui.menu.plugins;

import java.awt.event.ActionEvent;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import javax.swing.AbstractAction;
import javax.swing.JOptionPane;

import net.sf.genomeview.core.Configuration;
import net.sf.genomeview.data.Model;
import net.sf.genomeview.gui.MessageManager;
import net.sf.genomeview.plugin.PluginLoader;

public class LoadPluginFromURLAction extends AbstractAction {

	private static final long serialVersionUID = 7646686007341915085L;
	private Model model;

	public LoadPluginFromURLAction(Model model) {
		super(MessageManager.getString("pluginmenu.enter_url"));
		this.model = model;
	}



	@Override
	public void actionPerformed(ActionEvent e) {
		String urlString = (String)JOptionPane.showInputDialog(
				model.getGUIManager().getParent(), 
				MessageManager.getString("plugindownloaddialog.enter_location"),
				MessageManager.getString("plugindownloaddialog.enter_location_title"),
				JOptionPane.QUESTION_MESSAGE,
				null,
				null,
				"http://"
				);
		try {
			URL url = new URL(urlString);
			PluginLoader.installPlugin(url, Configuration.getPluginDirectory());
		} catch (MalformedURLException e1) {
			JOptionPane.showMessageDialog(model.getGUIManager().getParent(), MessageManager.getString("plugindownloaddialog.incorrect_url"));
			e1.printStackTrace();
		} catch (IOException e1){
			JOptionPane.showMessageDialog(model.getGUIManager().getParent(), MessageManager.getString("plugindownloaddialog.download_problem"));
			e1.printStackTrace();
		}

	}


}
