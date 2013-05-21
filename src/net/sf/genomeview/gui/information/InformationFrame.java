/**
 * %HEADER%
 */
package net.sf.genomeview.gui.information;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.Insets;
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
import net.sf.genomeview.gui.MessageManager;
import net.sf.genomeview.gui.components.ConnectionMonitor;
import net.sf.genomeview.gui.components.TypeCombo;
import net.sf.genomeview.gui.dialog.HelpButton;
import net.sf.jannot.Type;
import be.abeel.gui.GridBagPanel;
import be.abeel.gui.MemoryWidget;

/**
 * Panel with an overview of all available tracks and where they should be
 * visible. Also provides additional information regarding selected items.
 * 
 * This frame spans the right side of the application.
 * 
 * 
 * @author Thomas Abeel
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
		gc.gridwidth = 5;
		
		final TrackTable featureTrackList = new TrackTable(model);

		gc.gridy++;
		gc.gridwidth=1;
		gc.weighty = 0;
		gc.weightx = 0;
		add(new HelpButton(model.getGUIManager().getParent(), MessageManager.getString("infoframe.track_list_help")),gc);
		gc.gridx++;
		gc.gridwidth=4;
		gc.weightx =1;
		add(new JLabel(MessageManager.getString("infoframe.track_list")), gc);
		
	
		gc.gridwidth=5;
		gc.gridx=0;
		gc.gridy++;
		gc.weighty = 1;
		add(new JScrollPane(featureTrackList), gc);
		gc.gridy++;

		FeatureTable annotationTrackList = new FeatureTable(model);

		gc.weighty = 0;
		gc.gridwidth = 1;
		gc.weightx=0;
		add(new HelpButton(model.getGUIManager().getParent(), MessageManager.getString("infoframe.features_help")),gc);
		gc.gridx++;
		add(new JLabel(MessageManager.getString("infoframe.features")), gc);
		TypeCombo type = new TypeCombo(model, false);
		type.setSelectedItem(Type.get("CDS"));
		type.addActionListener(annotationTrackList);
		gc.gridx++;
		gc.gridwidth = 3;
		gc.weightx=1;
		add(type, gc);

		gc.gridwidth = 5;
		gc.gridx=0;
		gc.gridy++;
		gc.weighty = 1;
		add(new JScrollPane(annotationTrackList), gc);
		
		
		
		gc.gridy++;
		gc.weighty = 0;
		gc.gridwidth=1;
		add(new HelpButton(model.getGUIManager().getParent(), MessageManager.getString("infoframe.details_item_help")),gc);
		gc.gridx++;
		gc.gridwidth=4;
		add(new JLabel(MessageManager.getString("infoframe.details_item")), gc);
		gc.gridy++;
		gc.weightx = 0;
		gc.gridx=0;
		gc.gridwidth=5;
		
		add(new SearchButtons(), gc);
		gc.weightx = 1;
		gc.gridy++;

		gc.weighty = 1;
		JPanel detail = new FeatureDetailPanel(model);
		add(new JScrollPane(detail), gc);

		gc.gridy++;
		gc.weighty = 0.3;

		add(new GeneStructureView(model), gc);

		gc.gridy++;
		gc.gridwidth=5;
		gc.weighty = 0.01;
		add(new MonitorPanel(),gc);
		
		
		
		setPreferredSize(new Dimension(150,50));


	}

	class MonitorPanel extends GridBagPanel{
		
		private static final long serialVersionUID = 1226803679142353777L;

		public MonitorPanel(){
			gc.insets=new Insets(0,0,0,0);
			gc.weightx=1;
			this.add(new MemoryWidget(),gc);
			gc.gridx++;
			gc.weightx=0;
			this.add(ConnectionMonitor.instance.networkLabel,gc);
			gc.gridx++;
			this.add(ConnectionMonitor.instance.reposLabel,gc);
			gc.gridx++;
			this.add(ConnectionMonitor.instance.webLabel,gc);
		}
	}
	
	class SearchButtons extends JToolBar {

		/**
		 * 
		 */
		private static final long serialVersionUID = -1886328161085622722L;

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
