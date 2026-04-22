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
package net.sf.genomeview.gui.config;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.logging.Level;

import javax.swing.JCheckBox;

import net.sf.genomeview.core.Configuration;
import net.sf.genomeview.data.Model;

/**
 * Editor for a boolean field in the {@link Configuration}
 * 
 * @author Thomas Abeel
 * 
 */
@SuppressWarnings("serial")
public class BooleanConfig extends JCheckBox {

	public BooleanConfig(final String key, final String title,
			final Model model) {
		super(title);
		this.setSelected(Configuration.instance().getBoolean(key));

		this.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				model.getLog().log(Level.INFO,
						"Setting: " + key + "\t" + isSelected());
				Configuration.instance().set(key, isSelected());
				model.refresh();
				for (ConfigListener cl : listenerList) {
					cl.configurationChanged();
				}

			}

		});
	}

	private ArrayList<ConfigListener> listenerList = new ArrayList<ConfigListener>();

	public void addConfigListener(ConfigListener listener) {
		listenerList.add(listener);

	}
}