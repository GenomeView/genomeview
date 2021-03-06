/**
 * %HEADER%
 */
package net.sf.genomeview.gui.dialog;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JRadioButton;

import net.sf.genomeview.data.Model;
import net.sf.genomeview.gui.MessageManager;
import net.sf.genomeview.gui.StaticUtils;
import net.sf.jannot.Feature;
import net.sf.jannot.Location;
import be.abeel.gui.TitledComponent;
/**
 * 
 * @author Thomas Abeel
 *
 */
public class MergeFeatureDialog extends JDialog {
	private static final long serialVersionUID = -770863087750087961L;

	public MergeFeatureDialog(final Model model) {
		super(model.getGUIManager().getParent(), MessageManager.getString("mergefeatures.title"));
		final MergeFeatureDialog _self = this;
		setModal(true);
		setAlwaysOnTop(true);
	

		Container c = new Container();
		c.setLayout(new GridBagLayout());
		GridBagConstraints gc = new GridBagConstraints();
		gc.gridx = 0;
		gc.gridy = 0;
		gc.weightx = 1;
		gc.weighty = 1;
		gc.insets = new Insets(3, 3, 3, 3);
		gc.fill = GridBagConstraints.BOTH;

		ButtonGroup mergeType = new ButtonGroup();
		final JRadioButton intron = new JRadioButton(MessageManager.getString("mergefeatures.merge_space_intron"));
		intron.setSelected(true);
		final JRadioButton exon = new JRadioButton(MessageManager.getString("mergefeatures.merge_space_exon"));
		mergeType.add(intron);
		mergeType.add(exon);
		gc.gridwidth = 2;
		Container cp = new Container();
		cp.setLayout(new BorderLayout());
		cp.add(intron, BorderLayout.NORTH);
		cp.add(exon, BorderLayout.SOUTH);
		c.add(new TitledComponent(MessageManager.getString("mergefeatures.merge_type"), cp), gc);
		gc.gridy++;
		setBackground(exon.getBackground());
		// final TypeCombo select = new TypeCombo(model);
		// select.setSelectedItem(model.selectionModel().getFeatureSelection().first().type());
		final ButtonGroup bg = new ButtonGroup();
		final Set<Feature> set = model.selectionModel().getFeatureSelection();
		final Map<String, Feature> mapping = new HashMap<String, Feature>();
		cp = new Container();
		cp.setLayout(new GridLayout(0, 1));
		int index = 0;

		for (Feature f : set) {
			String label=f.toString();
			if (f.getQualifiersKeys().contains("Name")){
				label = f.qualifier("Name") +" ("+label+")";
			} else if (f.getQualifiersKeys().contains("ID")){
				label = f.qualifier("ID")  +" ("+label+")";
			}
			if(label.length()>50)
				label=label.substring(0, 50)+"...";
			JRadioButton button = new JRadioButton(label);
			button.setActionCommand(f.toString()+f.hashCode());
			if (index == 0)
				button.setSelected(true);
			bg.add(button);

			mapping.put(button.getActionCommand(), f);
			cp.add(button);
			// fs[index++] = f;
		}
		// System.out.println(mapping);
		c.add(new TitledComponent(MessageManager.getString("mergefeatures.master_feature"), cp), gc);
		gc.gridy++;

		// c.add(select, gc);
		final JCheckBox remove = new JCheckBox(MessageManager.getString("mergefeatures.remove_original_after_merger"));
		gc.gridy++;
		c.add(remove, gc);
		gc.gridwidth = 1;
		gc.gridy++;
		JButton ok = new JButton(MessageManager.getString("button.ok"));
		ok.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {

				List<Location> list = new ArrayList<Location>();
				List<Feature>parents=new ArrayList<Feature>();
				/* Create a deep copy of all locations */
				for (Feature rf : set) {
					for (Location l : rf.location()) {
						list.add(l.copy());
						parents.add(rf);
					}
				}
				SortedSet<Location> locations = new TreeSet<Location>();
				if (exon.isSelected()) {
					//System.out.println("EXON");
					for (int i = 0; i < list.size(); i++) {
						if (i == list.size() - 1) {
							locations.add(list.get(i));
						} else {
							Feature c = parents.get(i);//.getParent();
							Feature n = parents.get(i + 1);//.getParent();
							//System.out.println("Parents: "+c+"\t"+n);
							if (c != n) {
								locations.add(new Location(list.get(i).start, list.get(i + 1).end));
								i++;
							} else {
								locations.add(list.get(i));
							}
						}

					}

				} else {

					locations.addAll(list);
				}
				/*
				 * Construct a deep copy of the first feature, this one will be
				 * used as a frame for the new feature.
				 */
				Feature copy = mapping.get(bg.getSelection().getActionCommand()).copy();// model.selectionModel().getFeatureSelection().first().copy();

				copy.setLocation(locations);
				/* Add new feature to the annotation */
				model.vlm.getSelectedEntry().getMemoryAnnotation(copy.type()).add(copy);
				// ((MemoryFeatureAnnotation)
				// model.getSelectedEntry().get(copy.type())).add(copy);

				/* If requested, the original features are deleted */
				if (remove.isSelected()) {
					Set<Feature> toRemove = new HashSet<Feature>();
					toRemove.addAll(model.selectionModel().getFeatureSelection());
					for (Feature rf : toRemove)
						model.vlm.getSelectedEntry().getMemoryAnnotation(rf.type()).remove(rf);
				}
				model.selectionModel().clearLocationSelection();

				_self.dispose();

			}

		});
		JButton cancel = new JButton(MessageManager.getString("button.cancel"));
		cancel.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				_self.dispose();

			}

		});
		c.add(ok, gc);
		gc.gridx++;
		c.add(cancel, gc);
		setContentPane(c);
		pack();
		StaticUtils.center(model.getGUIManager().getParent(),this);
		setVisible(true);

	}
}
