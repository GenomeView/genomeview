/**
 * %HEADER%
 */
package net.sf.genomeview.gui.config;

import java.awt.Color;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JColorChooser;
import javax.swing.JLabel;

import be.abeel.gui.GridBagPanel;
import net.sf.genomeview.core.ColorIcon;
import net.sf.genomeview.core.Configuration;
import net.sf.genomeview.data.Model;

public class ColorConfig extends GridBagPanel {

	private static final long serialVersionUID = -2242613993844951737L;

	public ColorConfig(Model model, String key, String msg) {
		gc.weightx = 0;
		gc.weighty = 0;
		add(new JLabel(msg), gc);
		gc.gridx++;
		add(new ColorLabel(model, key), gc);
	}

	private class ColorLabel extends JLabel {

		private static final long serialVersionUID = -290128964356729238L;

		private ColorLabel(final Model model, final String configKey) {
			super(new ColorIcon(model.getConfiguration().getColor(configKey),
					16));
			this.addMouseListener(new MouseAdapter() {
				@Override
				public void mouseClicked(MouseEvent e) {
					Configuration conf = model.getConfiguration();
					Color newColor = JColorChooser.showDialog(
							model.getGUIManager().getMainWindow(),
							"Choose color", conf.getColor(configKey));

					if (newColor != null) {
						conf.setColor(configKey, newColor);
						setIcon(new ColorIcon(conf.getColor(configKey), 16));
					}
				}
			});

		}
	}

}
