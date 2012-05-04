/**
 * This file is part of GenomeView, a genome browser and annotation curator
 * 
 * Copyright (C) 2012 Thomas Abeel
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see http://www.gnu.org/licenses/.
 *
 * Project: http://genomeview.org/
 */
package net.sf.genomeview.gui.viztracks;

import java.awt.Point;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Observable;
import java.util.Observer;

import javax.swing.JDialog;

import net.sf.genomeview.data.Model;

/**
 * 
 * @author Thomas Abeel
 * 
 */
public class TrackConfigWindow implements Observer {

	private JDialog window;
	private TrackConfig config;
	
	public TrackConfigWindow(Model model, final TrackConfig config) {
		window = new JDialog(model.getGUIManager().getParent());
		
		window.setModal(false);
		window.setTitle("Configure track");
		this.config = config;
		System.out.println(config);
		window.add(config.getGUIContainer());
		window.pack();
		config.addObserver(this);
		window.addWindowListener(new WindowAdapter() {

			@Override
			public void windowClosing(WindowEvent e) {
				config.setConfigVisible(false);

			}

		});
		

	}

	@Override
	public void update(Observable o, Object arg) {
		Point nl=new Point(config.getConfigOffset().x-window.getWidth(),config.getConfigOffset().y);
		if (!nl.equals(window.getLocation()))
		 window.setLocation(nl);
//		if(config.getConfigOffset()!=window.getY())
//			window.setY
		if (config.isConfigVisible() != window.isVisible())
			window.setVisible(config.isConfigVisible());
		
	}

}
