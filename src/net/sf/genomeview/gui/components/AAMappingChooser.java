/**
 * %HEADER%
 */
package net.sf.genomeview.gui.components;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Observable;
import java.util.Observer;

import javax.swing.JComboBox;

import net.sf.genomeview.data.Model;
import net.sf.jannot.AminoAcidMapping;

public class AAMappingChooser extends JComboBox implements ActionListener,
		Observer {

	
	private static final long serialVersionUID = 8572963039431236614L;
	private Model model;

	public AAMappingChooser(Model model) {
		this.model = model;
		model.addObserver(this);
		setEditable(false);
		for (AminoAcidMapping aamap : AminoAcidMapping.values())
			this.addItem(aamap);
		this.addActionListener(this);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (!model.getAAMapping().equals(this.getSelectedItem()))
			model.setAAMapping(model.vlm.getSelectedEntry(),
					(AminoAcidMapping) this.getSelectedItem());
	}

	@Override
	public void update(Observable arg0, Object arg1) {
		this.removeActionListener(this);
		this.setSelectedItem(model.getAAMapping());
		this.addActionListener(this);

	}

}
