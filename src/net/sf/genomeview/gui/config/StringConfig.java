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

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

import javax.swing.JLabel;
import javax.swing.JTextField;

import net.sf.genomeview.core.Configuration;
import net.sf.genomeview.data.Model;

/**
 * 
 * @author Thomas Abeel
 * 
 */
public class StringConfig extends Container {

	/**
         * 
         */
	private static final long serialVersionUID = -1460525692768168573L;

	private JLabel label = new JLabel();

	private JTextField valueField = new JTextField("");

	@Deprecated
	public StringConfig(final String key, final String title) {
		this(key,title,null);
	}
	
	
	public StringConfig(final String key, final String title,final Model model) {
		
			
		valueField.setText(Configuration.get(key));
		label.setText(title);
		setLayout(new BorderLayout());
		add(label, BorderLayout.WEST);
		add(valueField, BorderLayout.CENTER);
		valueField.addKeyListener(new KeyAdapter() {

			@Override
			public void keyReleased(KeyEvent e) {
				super.keyReleased(e);
				Configuration.set(key, valueField.getText());
				if(model!=null)
					model.refresh();
			}

		});

	}

}