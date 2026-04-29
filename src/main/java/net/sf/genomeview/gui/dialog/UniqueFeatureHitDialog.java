/**
 * %HEADER%
 */
package net.sf.genomeview.gui.dialog;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;

import net.sf.genomeview.core.MessageManager;
import net.sf.genomeview.data.Model;
import net.sf.jannot.Feature;

public class UniqueFeatureHitDialog extends JDialog {

	private static final long serialVersionUID = 1L;

	static Feature selectedValue;

	private Model model;

	public UniqueFeatureHitDialog(java.util.Set<Feature> list, Model model) {

		super(model.getGUIManager().getMainWindow(), model.getMessageMgr()
				.getString("uniquefeaturehitdialog.title"));
		this.model = model;
		final MessageManager mm = model.getMessageMgr();
		this.setModal(true);
		this.setLayout(new BorderLayout());
		this.setLocationRelativeTo(model.getGUIManager().getMainWindow());

		this.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);

		final JComboBox box = new JComboBox();
		for (Feature p : list) {
			box.addItem(p);
		}
		JButton ok = new JButton(mm.getString("button.ok"));
		this.getRootPane().setDefaultButton(ok);
		ok.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				selectedValue = (Feature) box.getSelectedItem();
				dispose();
			}

		});
		this.add(new JLabel(mm.getString("uniquefeaturehitdialog.select")),
				BorderLayout.NORTH);
		this.add(box, BorderLayout.CENTER);
		this.add(ok, BorderLayout.SOUTH);
		this.pack();

		this.setVisible(true);
	}

	public Feature value() {
		return selectedValue;
	}

}
