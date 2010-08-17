/**
 * %HEADER%
 */
package net.sf.genomeview.gui.information;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Observable;
import java.util.Observer;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JToolBar;

import net.sf.genomeview.data.Model;
import net.sf.genomeview.gui.components.TypeCombo;
import net.sf.jannot.Type;
import be.abeel.gui.GridBagPanel;

/**
 * Panel with an overview of all available tracks and where they should be
 * visible. Also provides additional information regarding selected items.
 * 
 * This frame spans the right side of the application.
 * 
 * 
 * @author thabe
 * 
 */
public class InformationFrame extends GridBagPanel {

	private static final long serialVersionUID = -8504854026653847566L;
	private Model model;

	public InformationFrame(final Model model) {
		this.model = model;
		gc.fill = GridBagConstraints.BOTH;
		gc.weightx = 1;
		gc.weighty = 1;
		gc.gridwidth = 2;
//
//		final SourceTable sourceTrackList = new SourceTable(model);
//		gc.gridy++;
//		gc.weighty = 0;
//		add(new JLabel("Data sources"), gc);
//		gc.gridy++;
//		gc.weighty = 0.5;
//		add(new JScrollPane(sourceTrackList), gc);
//		gc.gridy++;

		final TrackTable featureTrackList = new TrackTable(model);

		gc.gridy++;
		gc.weighty = 0;
		add(new JLabel("Track list"), gc);
		gc.gridy++;
		gc.weighty = 1;
		add(new JScrollPane(featureTrackList), gc);
		gc.gridy++;

		FeatureTable annotationTrackList = new FeatureTable(model);

		gc.weighty = 0;
		gc.gridwidth = 1;
		add(new JLabel("Features"), gc);
		TypeCombo type = new TypeCombo(model, false);
		type.setSelectedItem(Type.get("CDS"));
		type.addActionListener(annotationTrackList);
		gc.gridx++;
		add(type, gc);

		gc.gridwidth = 2;
		gc.gridx--;
		gc.gridy++;
		gc.weighty = 1;
		add(new JScrollPane(annotationTrackList), gc);
		gc.gridy++;
		gc.weighty = 0;
		add(new JLabel("Details on selected items:"), gc);
		gc.gridy++;
		gc.weightx = 0;
		add(new SearchButtons(), gc);
		gc.weightx = 1;
		gc.gridy++;

		gc.weighty = 1;
		JPanel detail = new FeatureDetailPanel(model);
		add(new JScrollPane(detail), gc);

		gc.gridy++;
		gc.weighty = 0.3;

		add(new GeneStructureView(model), gc);

		setMinimumSize(new Dimension(200,200));


	}

	class SearchButtons extends JToolBar {

		public SearchButtons() {
			super.setFloatable(false);
			add(buttonate(Query.google));
			add(buttonate(Query.ncbiQuery));
			add(buttonate(Query.ensemblQuery));
			add(buttonate(Query.plaza));
			super.addSeparator();

		}

		private JButton buttonate(final Query q) {
			final JButton out;

			if (q.getIcon() != null)
				out = new JButton(q.getIcon());
			else
				out = new JButton(q.getLabel());
			model.addObserver(new Observer() {

				@Override
				public void update(Observable o, Object arg) {
					out.setEnabled(model.selectionModel().isFeatureSelected());

				}
			});

			out.setToolTipText(q.getLabel());
			out.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent e) {
					q.query(model.selectionModel().getFeatureSelection().first().toString());

				}
			});
			return out;
		}
	}

}
