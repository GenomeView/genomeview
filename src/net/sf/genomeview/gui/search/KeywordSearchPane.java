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
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;

import net.sf.genomeview.data.Model;
import net.sf.jannot.Entry;
import net.sf.jannot.Feature;
import net.sf.jannot.Location;
import be.abeel.gui.TitledComponent;

/**
 * 
 * @author Thomas Abeel
 * 
 */
class KeywordSearchPane extends SearchPanel {

	private static final long serialVersionUID = -7531967816569386730L;

	KeywordSearchPane(final Model model) {
		gc.weightx = 1;
		gc.weighty = 0;
		gc.fill = GridBagConstraints.BOTH;
		final JTextField text = new JTextField(40);
		setFocusField(text);
		JButton searchButton = new JButton("Search");

		final KeywordSearchResultModel srm = new KeywordSearchResultModel(model);
		final JTable resultTable = new JTable(srm);
		resultTable.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				int row = resultTable.getSelectedRow();
				Feature f = srm.getFeature(row);
				Entry entry = srm.getEntry(row);
				if (model.getSelectedEntry() != entry)
					model.setSelectedEntry(entry);
				model.selectionModel().setLocationSelection(f);
				double border = 0.05 * (f.end() - f.start());
				model.setAnnotationLocationVisible(new Location((int) (f.start() - border), (int) (f.end() + border)),
						true);

			}
		});
		text.addKeyListener(new KeyAdapter() {

			/*
			 * (non-Javadoc)
			 * 
			 * @see java.awt.event.KeyAdapter#keyTyped(java.awt.event.KeyEvent)
			 */
			@Override
			public void keyReleased(KeyEvent e) {

				int key = e.getKeyCode();
				if (key == KeyEvent.VK_ENTER) {
					srm.clear();
					srm.search(text.getText().trim());
				} else {
					super.keyTyped(e);
				}

			}

		});
		searchButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				srm.clear();
				srm.search(text.getText().trim());

			}

		});

		gc.gridwidth = 2;
		add(new TitledComponent("Keyword", text), gc);
		gc.gridy++;

		add(searchButton, gc);

		gc.gridwidth = 2;

		gc.gridy++;
		gc.weighty = 1;
		add(new JScrollPane(resultTable), gc);

	}

}