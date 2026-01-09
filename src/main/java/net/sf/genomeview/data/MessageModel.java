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
package net.sf.genomeview.data;

import java.util.Observable;

import net.sf.jannot.Location;
/**
 * 
 * @author Thomas Abeel
 *
 */
public class MessageModel extends Observable {

	private Model model;

	MessageModel(Model model) {
		this.model = model;
	}

	public void setStatusBarMessage(String s){
		this.message=s;
		setChanged();
		notifyObservers();
	}
	
	private String message = null;

	public String getStatusBarMessage() {
		StringBuffer msg = new StringBuffer();
		Location viz = model.vlm.getAnnotationLocationVisible();
		msg.append("  "+model.vlm.getSelectedEntry()+":" + viz.start + ":" + viz.end + " ");

		int currentCoord = model.mouseModel().getCurrentCoord();
		if (currentCoord == -1) {
			// msg.append("mouse: -- ");
		} else {
			msg.append("(" + currentCoord + ") ");
		}

		if (model.selectionModel().getNumberOfSelectedNucs() != 0) {
			msg.append("Selected: ");
			msg.append(model.selectionModel().getNumberOfSelectedNucs()
					+ " nt / "
					+ model.selectionModel().getNumberOfSelectedProts()
					+ " aa ");
		}

		if (message != null)
			msg.append("-- " + message);
		
		return msg.toString();
	}

}
