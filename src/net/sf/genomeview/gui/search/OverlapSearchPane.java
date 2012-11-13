/**
 * %HEADER%
 */
package net.sf.genomeview.gui.search;

import java.awt.GridBagConstraints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JButton;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;

import net.sf.genomeview.data.Model;
import net.sf.genomeview.gui.components.TypeCombo;
import net.sf.jannot.Feature;
import net.sf.jannot.Location;
import be.abeel.gui.TitledComponent;

/**
 * 
 * @author Thomas Abeel
 *
 */
class OverlapSearchPane extends SearchPanel {
		
	private static final long serialVersionUID = 2986333484335063190L;

		public OverlapSearchPane(final Model model) {
			gc.weightx = 1;
			gc.weighty = 0;
			gc.fill = GridBagConstraints.BOTH;
			final JTextArea seq = new JTextArea(7, 30);
			super.setFocusField(seq);
			final TypeCombo sourceType = new TypeCombo(model);
			final TypeCombo targetType = new TypeCombo(model);
			final OverlapSearchResultModel srm = new OverlapSearchResultModel(model);
			final JTable results = new JTable(srm);
			results.addMouseListener(new MouseAdapter() {
				@Override
				public void mouseClicked(MouseEvent e) {
					int row = results.getSelectedRow();
					Feature f = srm.getFeature(row);
					model.selectionModel().setLocationSelection(f);
					double border = 0.05 * (f.end() - f.start());
					model.setAnnotationLocationVisible(new Location((int) (f.start() - border),
							(int) (f.end() + border)));

				}
			});
			JButton searchButton = new JButton("Search");
			searchButton.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent e) {
					srm.clear();
					srm.search(sourceType.getTerm(), targetType.getTerm());

				}

			});
			
			gc.gridwidth = 2;
			add(new TitledComponent("Overlap between type ", sourceType), gc);
			gc.gridy++;
			add(new TitledComponent("and type", targetType), gc);
			gc.gridy++;
			
			add(searchButton, gc);
			
			gc.gridwidth = 2;

			gc.gridy++;
			gc.weighty = 1;
			add(new JScrollPane(results), gc);

		}
	}