package net.sf.genomeview.gui;

import javax.swing.JLabel;
import javax.swing.JPanel;

import net.sf.genomeview.data.Model;

public class StatusBar extends JLabel{

	private Model model;

	public StatusBar(Model model) {
		this.model=model;
		model.getGUIManager().registerStatusBar(this);
		setText("status");
	}

}
