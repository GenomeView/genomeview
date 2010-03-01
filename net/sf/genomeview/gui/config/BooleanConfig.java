/**
 * %HEADER%
 */
package net.sf.genomeview.gui.config;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JCheckBox;

import net.sf.genomeview.core.Configuration;

class BooleanConfig extends JCheckBox {

	private static final long serialVersionUID = 9081788377933556296L;

	BooleanConfig(final String key, final String title) {
		super(title);
		this.setSelected(Configuration.getBoolean(key));

		this.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				Configuration.set(key, isSelected());

			}

		});
	}
}