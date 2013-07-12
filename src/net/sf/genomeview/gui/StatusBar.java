/**
 * %HEADER%
 */
package net.sf.genomeview.gui;

import java.awt.Insets;
import java.util.Observable;
import java.util.Observer;

import javax.swing.JLabel;

import net.sf.genomeview.data.Model;
import net.sf.genomeview.data.MouseModel;
import net.sf.genomeview.data.SelectionModel;
import net.sf.jannot.Location;
import be.abeel.gui.GridBagPanel;

public class StatusBar extends GridBagPanel implements Observer {

	private static final long serialVersionUID = -4850784549623078528L;
//	private String message = new String("status");
	private Model model;

	private JLabel messages=new JLabel();
	public StatusBar(Model model) {
		this.gc.insets=new Insets(0,0,0,0);
		gc.weightx=1;
		gc.weighty=1;
		this.add(messages,gc);
		this.model=model;
		model.addObserver(this);
		model.mouseModel().addObserver(this);
		model.getGUIManager().registerStatusBar(this);
//		messages.setText(message);
		

	}

	
	@Override
	public void update(Observable o, Object arg) {
		messages.setText(model.messageModel().getStatusBarMessage());
	}

}
