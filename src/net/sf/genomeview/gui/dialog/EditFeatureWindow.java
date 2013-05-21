/**
 * %HEADER%
 */
package net.sf.genomeview.gui.dialog;

import java.awt.GridBagConstraints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.StringReader;
import java.util.Arrays;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import net.sf.genomeview.data.Model;
import net.sf.genomeview.gui.MessageManager;
import net.sf.genomeview.gui.components.StrandCombo;
import net.sf.genomeview.gui.components.TypeCombo;
import net.sf.jannot.Feature;
import net.sf.jannot.Location;
import net.sf.jannot.MemoryFeatureAnnotation;
import be.abeel.gui.GridBagPanel;
import be.abeel.gui.TitledComponent;
import be.abeel.io.LineIterator;

/**
 * JFrame that allows to edit a feature
 * 
 * @author Thomas Abeel
 * 
 */
public class EditFeatureWindow extends JDialog {

	/**
	 * 
	 */
	private static final long serialVersionUID = -790390435947336529L;

	private final Model model;

	private Feature feature;

	private EditFeatureWindow _self;
	private JTextArea notes, location;
	private StrandCombo strandSelection;
	private TypeCombo typeSelection;

	private class EditFeatureWindowContent extends GridBagPanel {
		/**
		 * 
		 */
		private static final long serialVersionUID = 3211302042434915395L;

		public EditFeatureWindowContent() {
			gc.gridwidth = 3;
			gc.fill = GridBagConstraints.BOTH;
			gc.weightx = 1;
			gc.weighty = 0;
			notes = new JTextArea(10, 50);
			location = new JTextArea(10, 50);
			location.setWrapStyleWord(true);
			location.setLineWrap(true);
			strandSelection = new StrandCombo();
			typeSelection = new TypeCombo(model);
			this.add(new TitledComponent(MessageManager.getString("editfeature.type"), typeSelection), gc);
			gc.gridy++;
			this.add(new TitledComponent(MessageManager.getString("editfeature.strand"), strandSelection), gc);

			/* Notes legend */
			gc.gridy++;
			gc.gridwidth = 1;
			this.add(new JLabel(MessageManager.getString("editfeature.notes")), gc);
			gc.gridx++;
			this.add(new HelpButton(_self, MessageManager.getString("editfeature.help_one_qualifier_line")), gc);

			/* Notes text area */
			gc.gridy++;
			gc.gridx = 0;
			;
			gc.weighty = 1;
			gc.gridwidth = 3;
			this.add(new JScrollPane(notes), gc);

			/* Location legend */
			gc.weighty = 0;
			gc.gridy++;
			gc.gridwidth = 1;
			this.add(new JLabel(MessageManager.getString("editfeature.location")), gc);
			gc.gridx++;
			this.add(new HelpButton(_self, MessageManager.getString("editfeature.location_separator")), gc);

			/* Location text area */
			gc.gridy++;
			gc.gridx = 0;
			;
			gc.weighty = 1;
			gc.gridwidth = 3;
			this.add(new JScrollPane(location), gc);

			gc.gridy++;
			gc.weighty = 0;
			JButton ok = new JButton(MessageManager.getString("button.save_close"));
			ok.addActionListener(new ActionListener() {

				public void actionPerformed(ActionEvent arg0) {
					boolean warning = false;
					try {
						SortedSet<Location> loc = new TreeSet<Location>();
						StringBuffer text = new StringBuffer();
						for (String line : new LineIterator(new StringReader(location.getText()))) {
							text.append(line.trim());
						}
						String[] arr = text.toString().split(",");
						for (String s : arr) {
							String[] as = s.split("\\.\\.");
							int start = Integer.parseInt(as[0].trim());
							int end = Integer.parseInt(as[1].trim());
							if(start<1)
								start=1;
							if(end>model.vlm.getSelectedEntry().getMaximumLength())
								end=model.vlm.getSelectedEntry().getMaximumLength();
							loc.add(new Location(start, end));
						}
						feature.setLocation(loc);
					} catch (Exception e) {
						e.printStackTrace();
						JOptionPane.showMessageDialog(_self, MessageManager.getString("editfeature.location_failed_warn"), MessageManager.getString("editfeature.location_failed"), JOptionPane.WARNING_MESSAGE);
						warning = true;
					}
					feature.setStrand(strandSelection.getStrand());
					
					/* Update qualifiers */
					try {
//						feature.setMute(true);
						feature.clearQualifiers();
						
						/* Construct new qualifiers */
//						List<Qualifier> list = new ArrayList<Qualifier>();
						for (String line : new LineIterator(new StringReader(notes.getText()))) {
							if (line.trim().length() > 0) {
								String[] arr = line.split("=");
								feature.addQualifier(arr[0].trim(), arr[1].trim());
							}
						}
//						/* Remove all qualifiers */
//						List<Qualifier> remove = new ArrayList<Qualifier>();
//						for (String key : feature.getQualifiersKeys()) {
//							List<Qualifier> qs = feature.qualifier(key);
//							for (Qualifier q : qs) {
//								remove.add(q);
//							}
//						}
//						for (Qualifier q : remove) {
//							feature.removeQualifier(q);
//						}
//						for (Qualifier q : list) {
//							feature.addQualifier(q);
//						}
//						feature.setMute(false);
					} catch (Exception e) {
						e.printStackTrace();
						JOptionPane.showMessageDialog(_self, MessageManager.getString("editfeature.notes_failed_warn"), MessageManager.getString("editfeature.notes_failed"), JOptionPane.WARNING_MESSAGE);
						warning = true;
					}
					
					/* Update type if needed and notify annotation model */
					if(feature.type()!=typeSelection.getTerm()){
						MemoryFeatureAnnotation mf=model.vlm.getSelectedEntry().getMemoryAnnotation(feature.type());
						mf.remove(feature);
						feature.setType(typeSelection.getTerm());
						mf=model.vlm.getSelectedEntry().getMemoryAnnotation(typeSelection.getTerm());
						mf.add(feature);
						model.updateTracks();
						
						model.annotationModel().typeUpdated(feature.type());
						model.annotationModel().typeUpdated(typeSelection.getTerm());
					}else{
						model.annotationModel().typeUpdated(feature.type());
					}

					
					if (!warning)
						_self.setVisible(false);

				}

			});
			JButton cancel = new JButton("Close");
			cancel.addActionListener(new ActionListener() {

				public void actionPerformed(ActionEvent arg0) {
					_self.setVisible(false);

				}

			});

			gc.gridwidth = 1;
			gc.gridy++;
			add(ok, gc);
			gc.gridx++;

			add(cancel, gc);
		}
	}

	public EditFeatureWindow(Model model) {
		super(model.getGUIManager().getParent(), MessageManager.getString("editfeature.edit_structure"));
		_self = this;
		setModal(true);
		this.model = model;
		this.setContentPane(new EditFeatureWindowContent());

		this.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);

		pack();

	}

	public void setVisible(boolean b) {
		if (b) {
			this.feature = model.selectionModel().getFeatureSelection().first();
			/* Fill notes text area */
			StringBuffer text = new StringBuffer();
			notes.setText("");
			for (String key : feature.getQualifiersKeys()) {
				
				text.append(key + "=" + feature.qualifier(key) + "\n");
				
				
			}
			notes.setText(text.toString());
			/* Fill in combo boxes */
			strandSelection.setSelectedItem(feature.strand());
			typeSelection.setSelectedItem(feature.type());
			location.setText(format(feature.location()));

		}

		super.setVisible(b);
	}

	private String format(Location[] loc) {
		StringBuffer tmp = new StringBuffer(Arrays.toString(loc));
		return tmp.substring(1, tmp.length() - 1);
	}

}
