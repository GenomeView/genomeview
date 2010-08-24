/**
 * %HEADER%
 */
package net.sf.genomeview.gui.dialog;

import java.awt.Container;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Map.Entry;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;

import be.abeel.gui.TitledComponent;

import net.sf.genomeview.data.Model;
import net.sf.genomeview.gui.StaticUtils;
import net.sf.jannot.Data;
import net.sf.jannot.DataKey;

/**
 * 
 * @author Thomas Abeel
 * 
 */
public class SaveDialog extends JDialog {

	private static final long serialVersionUID = -5209291628487502687L;

	class DataSourceCheckbox extends JCheckBox {

		private static final long serialVersionUID = -208816638301437642L;

		private Data<?> data;

		public DataSourceCheckbox(Data<?> data) {
			super(data.toString());
			this.data = data;
		}

	}

	private SaveDialog(final Model model) {
		super(model.getGUIManager().getParent(), "Save dialog", true);
		setLayout(new GridBagLayout());

		GridBagConstraints gc = new GridBagConstraints();
		gc.insets = new Insets(3, 3, 3, 3);
		gc.gridwidth = 2;
		gc.gridheight = 1;
		gc.gridx = 0;
		gc.gridy = 0;

		final ArrayList<DataSourceCheckbox> dss = new ArrayList<DataSourceCheckbox>();
		add(new JLabel("Select sources to save"), gc);
		gc.gridy++;
		for (net.sf.jannot.Entry e : model.entries()) {
			Container cp = new Container();
			cp.setLayout(new GridLayout(0, 1));
			int count = 0;
			for (DataKey ds : e) {
				Data s = e.get(ds);
				if (s.canSave()) {
					DataSourceCheckbox dsb = new DataSourceCheckbox(s);
					dss.add(dsb);
					cp.add(dsb);
					count++;

				}
			}
			if (count > 0) {
				add(new TitledComponent(e.toString(), cp),gc);
				gc.gridy++;
			}
		}

		JButton save = new JButton("Save");
		JButton close = new JButton("Close");

		gc.gridwidth = 1;
		gc.gridy++;
		add(save, gc);
		gc.gridx++;
		add(close, gc);

		// save.addActionListener(new ActionListener() {
		//
		// @Override
		// public void actionPerformed(ActionEvent e) {
		//
		// for (DataSourceCheckbox dsb : dss) {
		// if (dsb.isSelected()) {
		// boolean continueSave = true;
		// if (dsb.data.isDestructiveSave()) {
		// int result =
		// JOptionPane.showConfirmDialog(model.getGUIManager().getParent(),
		// "Overwrite existing file?\n"
		// + dsb.data, "Overwrite?", JOptionPane.YES_NO_OPTION);
		// if (result != JOptionPane.YES_OPTION)
		// continueSave = false;
		// }
		//
		// if (continueSave) {
		// boolean seqSaved = false;
		// for (Entry en : model.entries()) {
		// if (en.defaultSource.equals(dsb.data))
		// seqSaved = true;
		//
		// }
		// if (!seqSaved) {
		// int result =
		// JOptionPane.showConfirmDialog(model.getGUIManager().getParent(),
		// "Save source without sequence?", "No sequence!",
		// JOptionPane.YES_NO_OPTION);
		// if (result != JOptionPane.YES_OPTION)
		// continueSave = false;
		// }
		// }
		// setVisible(false);
		// if (continueSave) {
		// WriteEntriesWorker rw = new WriteEntriesWorker(dsb.data, model);
		// rw.execute();
		// } else {
		// JOptionPane.showMessageDialog(model.getGUIManager().getParent(),
		// "Save aborted!");
		// }
		//
		// }
		// }
		//
		// }
		//
		// });

		close.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				setVisible(false);

			}

		});

		StaticUtils.center(this);
		pack();
		setVisible(true);
	}

	public static void display(Model model) {
		new SaveDialog(model);

	}

}
