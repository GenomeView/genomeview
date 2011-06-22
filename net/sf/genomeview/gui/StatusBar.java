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
	private MouseModel mouse;
	private SelectionModel select;
	private String message = new String("status");
	private Model model;

	private JLabel messages=new JLabel();
	public StatusBar(Model model) {
		this.gc.insets=new Insets(0,0,0,0);
		gc.weightx=1;
		gc.weighty=1;
		this.add(messages,gc);
		this.mouse = model.mouseModel();
		this.model=model;
		this.select = model.selectionModel();
		model.getGUIManager().registerStatusBar(this);
		messages.setText(message);
		this.mouse.addObserver(this);
	}

	@Override
	public void update(Observable o, Object arg) {
		StringBuffer msg=new StringBuffer();
		Location viz=model.getAnnotationLocationVisible();
		msg.append("Visible: "+viz.start+":"+viz.end+" ");
		
		int currentCoord = mouse.getCurrentCoord();
		if (currentCoord == -1) {
			msg.append("mouse: -- ");
		} else {
			msg.append("mouse: " + currentCoord+" ");
		}

		if (select.getNumberOfSelectedNucs() != 0) {
			msg.append("Selected: ");
			msg.append(select.getNumberOfSelectedNucs() + " nt / " + select.getNumberOfSelectedProts() + " aa ");
		}

		messages.setText(msg.toString());
	}

}
