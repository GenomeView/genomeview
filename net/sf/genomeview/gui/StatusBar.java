package net.sf.genomeview.gui;

import java.util.Observable;
import java.util.Observer;

import javax.swing.JLabel;

import net.sf.genomeview.data.Model;

public class StatusBar extends JLabel implements Observer{


	private static final long serialVersionUID = -4850784549623078528L;
	private Model model;

	private String message = new String("status");
	
	public StatusBar(Model model) {
		this.model=model;
		model.getGUIManager().registerStatusBar(this);
		setText(message);
		model.addObserver(this);
	}

	@Override
	public void update(Observable o, Object arg) {
		String selected = new String();
		String coords = new String();

		int currentCoord = model.getCurrentCoord();
		if (currentCoord == -1){
			coords = "--";
		} else {
			coords = "x: "+currentCoord;
		}
		
		if (model.getNumberOfSelectedNucs()!=0){
			selected = "         Selected : ";
			selected+= model.getNumberOfSelectedNucs()+" nt / "+model.getNumberOfSelectedProts()+" aa";
		} else {
			selected = new String();
		}
		
		this.message = coords + selected;
		setText(message);
	}

}
