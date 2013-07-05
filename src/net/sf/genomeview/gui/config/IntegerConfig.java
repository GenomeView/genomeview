package net.sf.genomeview.gui.config;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

import javax.swing.JLabel;

import net.sf.genomeview.core.Configuration;
import be.abeel.gui.JIntegerField;

class IntegerConfig extends Container {

		/**
         * 
         */
		private static final long serialVersionUID = -1460525692768168573L;

		private JLabel label = new JLabel();

		private JIntegerField valueField = new JIntegerField("");

		IntegerConfig(final String key, final String title) {
			valueField.setText(Configuration.get(key));
			label.setText(title);
			setLayout(new BorderLayout());
			add(label, BorderLayout.WEST);
			add(valueField, BorderLayout.CENTER);
			valueField.addKeyListener(new KeyAdapter() {

				@Override
				public void keyReleased(KeyEvent e) {
					super.keyReleased(e);
					Configuration.set(key, valueField.getValue());

				}

			});

		}

	}