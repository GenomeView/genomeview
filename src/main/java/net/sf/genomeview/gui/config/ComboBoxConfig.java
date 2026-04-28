/**
 * %HEADER%
 */
package net.sf.genomeview.gui.config;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.logging.Level;

import javax.swing.JComboBox;
import javax.swing.JLabel;

import be.abeel.gui.GridBagPanel;
import net.sf.genomeview.data.Model;

/**
 * 
 * @author Thomas Abeel
 * 
 */
public class ComboBoxConfig extends GridBagPanel {

	public ComboBoxConfig(String[] list, final String selectedKey, String msg,
			Model model) {
		gc.weightx = 0;
		gc.weighty = 0;
		add(new JLabel(msg), gc);
		gc.gridx++;

		final JComboBox<String> jbc = new JComboBox<>(list);
		jbc.setSelectedItem(model.getConfiguration().get(selectedKey));

		jbc.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				Object o = jbc.getSelectedItem();
				model.getLog().log(Level.INFO, "Selected item: " + o);
				model.getConfiguration().set(selectedKey, o.toString());

			}

		});
		add(jbc, gc);
	}

}
