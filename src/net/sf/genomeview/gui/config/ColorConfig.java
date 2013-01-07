/**
 * %HEADER%
 */
package net.sf.genomeview.gui.config;

import java.awt.Color;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JColorChooser;
import javax.swing.JLabel;

import net.sf.genomeview.core.ColorIcon;
import net.sf.genomeview.core.Configuration;
import net.sf.genomeview.data.Model;
import be.abeel.gui.GridBagPanel;

public class ColorConfig extends GridBagPanel {

		private static final long serialVersionUID = -2242613993844951737L;

		public ColorConfig(Model model, String key, String msg) {
			gc.weightx=0;
			gc.weighty=0;
			add(new JLabel(msg), gc);
			gc.gridx++;
			add(new ColorLabel(model, key), gc);
		}

		private class ColorLabel extends JLabel {

			private static final long serialVersionUID = -290128964356729238L;

			private ColorLabel(final Model model, final String configKey) {
				super(new ColorIcon(Configuration.getColor(configKey), 16));
				this.addMouseListener(new MouseAdapter() {
					public void mouseClicked(MouseEvent e) {
						Color newColor = JColorChooser.showDialog(model.getGUIManager().getParent(), "Choose color", Configuration.getColor(configKey));

						if (newColor != null) {
							Configuration.setColor(configKey, newColor);
							setIcon(new ColorIcon(Configuration.getColor(configKey), 16));
						}
					}
				});

			}
		}

	}
