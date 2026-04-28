/**
 * %HEADER%
 */
package net.sf.genomeview.gui.config;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

import javax.swing.JLabel;
import javax.swing.JTextField;

import net.sf.genomeview.core.Configuration;

@SuppressWarnings("serial")
class DoubleConfig extends Container {

	private JLabel label = new JLabel();

	private JTextField valueField = new JTextField("");

	DoubleConfig(final String key, final String title, Configuration config) {
		valueField.setText(config.get(key));
		label.setText(title);
		setLayout(new BorderLayout());
		add(label, BorderLayout.WEST);
		add(valueField, BorderLayout.CENTER);
		valueField.addKeyListener(new KeyAdapter() {

			@Override
			public void keyReleased(KeyEvent e) {
				super.keyReleased(e);
				config.set(key, valueField.getText());

			}

		});

	}

}