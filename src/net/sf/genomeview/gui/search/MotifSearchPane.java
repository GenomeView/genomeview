/**
 * %HEADER%
 */
package net.sf.genomeview.gui.search;

import java.awt.GridBagConstraints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;

import net.sf.genomeview.data.Model;
import net.sf.genomeview.gui.MessageManager;
import net.sf.genomeview.gui.search.SearchDialog.SequenceType;
import net.sf.jannot.Location;
import be.abeel.gui.TitledComponent;

/**
 * 
 * @author Thomas Abeel
 * 
 */
class MotifSearchPane extends SearchPanel {

	private static final long serialVersionUID = -3270709193426284702L;

	MotifSearchPane(final Model model) {
		gc.fill = GridBagConstraints.BOTH;
	
		
		final JTextField seq = new JTextField(40);
		this.setFocusField(seq);
		
		JButton search = new JButton(MessageManager.getString("button.search"));

		final MotifSearchResultModel mrm = new MotifSearchResultModel(model);
		final JTable results = new JTable(mrm);
		results.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {
				int row = results.getSelectedRow();
				if (row >= 0) {
					Location l = mrm.getLocation(row);
					model.vlm.center((l.start() + l.end()) / 2);
				}
			}
		});
		final JComboBox type = new JComboBox(SequenceType.values());

		search.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				mrm.clear();
				model.clearHighlights();
				mrm.search(model, seq.getText().trim(), 
						(SequenceType) type.getSelectedItem());

			}

		});
		seq.addKeyListener(new KeyAdapter() {

			/*
			 * (non-Javadoc)
			 * 
			 * @see java.awt.event.KeyAdapter#keyTyped(java.awt.event.KeyEvent)
			 */
			@Override
			public void keyReleased(KeyEvent e) {

				int key = e.getKeyCode();
				if (key == KeyEvent.VK_ENTER) {
					mrm.clear();
					model.clearHighlights();
					mrm.search(model, seq.getText().trim(),
							(SequenceType) type.getSelectedItem());
				} else {
					super.keyTyped(e);
				}

			}

		});
		

		
		/* Query sequence box */
		gc.weightx = 1;
		gc.gridwidth = 4;
		gc.weighty = 0.1;
		add(new TitledComponent(MessageManager.getString("motifsearch.query_sequence"), seq), gc);
		gc.weighty = 0;
		gc.gridwidth = 1;
		gc.gridy++;
		add(type, gc);
		gc.weightx = 0;
		gc.gridx++;
		add(search, gc);
	

		/* Result table */
		gc.gridx = 0;
		gc.gridwidth = 4;
		gc.gridy++;
		gc.weighty = 1;
		gc.weightx = 1;
		add(new TitledComponent(MessageManager.getString("motifsearch.result_locations"), new JScrollPane(results)), gc);

	}

}