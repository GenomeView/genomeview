/**
 * %HEADER%
 */
package net.sf.genomeview.gui.dialog;

import java.awt.GridBagConstraints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
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
import net.sf.genomeview.gui.components.StrandCombo;
import net.sf.genomeview.gui.components.TypeCombo;
import net.sf.jannot.Feature;
import net.sf.jannot.Location;
import net.sf.jannot.Qualifier;
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
			this.add(new TitledComponent("Type", typeSelection), gc);
			gc.gridy++;
			this.add(new TitledComponent("Strand", strandSelection), gc);

			/* Notes legend */
			gc.gridy++;
			gc.gridwidth = 1;
			this.add(new JLabel("Notes"), gc);
			gc.gridx++;
			this.add(new HelpButton(_self, "One qualifier per line, key=value"), gc);

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
			this.add(new JLabel("Location"), gc);
			gc.gridx++;
			this.add(new HelpButton(_self, "Locations separated with a comma, start and stop coordinate separated with two dots. White space and new lines are ignored."), gc);

			/* Location text area */
			gc.gridy++;
			gc.gridx = 0;
			;
			gc.weighty = 1;
			gc.gridwidth = 3;
			this.add(new JScrollPane(location), gc);

			gc.gridy++;
			gc.weighty = 0;
			JButton ok = new JButton("Save & Close");
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
							loc.add(new Location(start, end));
						}
						feature.setLocation(loc);
					} catch (Exception e) {
						e.printStackTrace();
						JOptionPane.showMessageDialog(_self, "Could not parse the location, please double check!", "Location failed", JOptionPane.WARNING_MESSAGE);
						warning = true;
					}
					feature.setStrand(strandSelection.getStrand());
					feature.setType(typeSelection.getTerm());

					try {
//						feature.setMute(true);
						/* Construct new qualifiers */
						List<Qualifier> list = new ArrayList<Qualifier>();
						for (String line : new LineIterator(new StringReader(notes.getText()))) {
							if (line.trim().length() > 0) {
								String[] arr = line.split("=");
								list.add(new Qualifier(arr[0].trim(), arr[1].trim()));
							}
						}
						/* Remove all qualifiers */
						List<Qualifier> remove = new ArrayList<Qualifier>();
						for (String key : feature.getQualifiersKeys()) {
							List<Qualifier> qs = feature.qualifier(key);
							for (Qualifier q : qs) {
								remove.add(q);
							}
						}
						for (Qualifier q : remove) {
							feature.removeQualifier(q);
						}
						for (Qualifier q : list) {
							feature.addQualifier(q);
						}
//						feature.setMute(false);
					} catch (Exception e) {
						e.printStackTrace();
						JOptionPane.showMessageDialog(_self, "Failed to parse the notes, please double check!", "Notes failed", JOptionPane.WARNING_MESSAGE);
						warning = true;
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
		super(model.getGUIManager().getParent(), "Edit structure");
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
				List<Qualifier> qs = feature.qualifier(key);
				for (Qualifier q : qs) {
					text.append(q.getKey() + "=" + q.getValue() + "\n");
				}
				notes.setText(text.toString());
			}
			/* Fill in combo boxes */
			strandSelection.setSelectedItem(feature.strand());
			typeSelection.setSelectedItem(feature.type());
			location.setText(format(feature.location()));

		}

		super.setVisible(b);
	}

	private String format(SortedSet<Location> loc) {
		StringBuffer tmp = new StringBuffer(loc.toString());
		return tmp.substring(1, tmp.length() - 1);
	}

}
