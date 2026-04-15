/**
 * %HEADER%
 */
package net.sf.genomeview.gui.config;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JComboBox;
import javax.swing.JLabel;

import be.abeel.gui.GridBagPanel;
import net.sf.genomeview.core.Configuration;

/**
 * 
 * @author Thomas Abeel
 * 
 */
public class ComboBoxConfig extends GridBagPanel {

	private static final long serialVersionUID = 5793902272456842701L;

	public ComboBoxConfig(String[] list, final String selectedKey, String msg) {
		gc.weightx = 0;
		gc.weighty = 0;
		add(new JLabel(msg), gc);
		gc.gridx++;

		final JComboBox jbc = new JComboBox(list);
		jbc.setSelectedItem(Configuration.instance().get(selectedKey));

		jbc.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				Object o = jbc.getSelectedItem();
				System.out.println("Selected item: " + o);
				Configuration.instance().set(selectedKey, o.toString());

			}

		});
		add(jbc, gc);
	}

}
