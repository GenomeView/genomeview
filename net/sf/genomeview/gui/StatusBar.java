/**
 * %HEADER%
 */
package net.sf.genomeview.gui;

import java.util.Observable;
import java.util.Observer;

import javax.swing.JLabel;

import net.sf.genomeview.data.Model;
import net.sf.genomeview.data.MouseModel;
import net.sf.genomeview.data.SelectionModel;

public class StatusBar extends JLabel implements Observer {

	private static final long serialVersionUID = -4850784549623078528L;
	private MouseModel model;
	private SelectionModel select;
	private String message = new String("status");

	public StatusBar(Model model) {
		this.model = model.mouseModel();
		this.select = model.selectionModel();
		model.getGUIManager().registerStatusBar(this);
		setText(message);
		this.model.addObserver(this);
	}

	@Override
	public void update(Observable o, Object arg) {
		String selected = new String();
		String coords = new String();

		int currentCoord = model.getCurrentCoord();
		if (currentCoord == -1) {
			coords = "--";
		} else {
			coords = "x: " + currentCoord;
		}

		if (select.getNumberOfSelectedNucs() != 0) {
			selected = "         Selected : ";
			selected += select.getNumberOfSelectedNucs() + " nt / " + select.getNumberOfSelectedProts() + " aa";
		} else {
			selected = new String();
		}

		this.message = coords + selected;
		setText(message);
	}

}
