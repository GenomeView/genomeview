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
package net.sf.genomeview.gui.viztracks.annotation;

import java.awt.event.ActionEvent;
import java.util.Set;

import javax.swing.AbstractAction;
import javax.swing.JButton;

import net.sf.genomeview.core.Configuration;
import net.sf.genomeview.data.Model;
import net.sf.genomeview.gui.MessageManager;
import net.sf.genomeview.gui.dialog.StructureTrackConfig;
import net.sf.genomeview.gui.viztracks.TrackConfig;
import net.sf.jannot.StringKey;
import net.sf.jannot.Type;
import be.abeel.gui.GridBagPanel;
import be.abeel.util.DefaultHashMap;

/**
 * 
 * @author Thomas Abeel
 * 
 */
public class StructureTrackModel extends TrackConfig {
	private DefaultHashMap<Type, Boolean> visibleTypes = new DefaultHashMap<Type, Boolean>(Boolean.FALSE);

	public StructureTrackModel(Model model, StringKey key) {
		super(model, key);

		Set<Type> tmp1 = Configuration.getTypeSet("visibleTypesStructure");
		for (Type t : tmp1)
			setTypeVisible(t, true);
	}

	public boolean isTypeVisible(Type ct) {
		return visibleTypes.get(ct);
	}

	public void setTypeVisible(Type t, boolean b) {
		visibleTypes.put(t, b);
		// setChanged();
		// notifyObservers();
		model.refresh();

	}

	@Override
	protected GridBagPanel getGUIContainer() {
		GridBagPanel out = super.getGUIContainer();
		final StructureTrackModel _self = this;
		out.gc.gridy++;
		out.add(new JButton(new AbstractAction(MessageManager.getString("structuretrack.configure")) {

			@Override
			public void actionPerformed(ActionEvent e) {
				StructureTrackConfig.display(model, _self);

			}

		}), out.gc);

		return out;
	}

	

}