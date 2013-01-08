/**
 * %HEADER%
 */
package net.sf.genomeview.gui.menu.edit;

import java.awt.event.ActionEvent;
import java.util.Observable;

import net.sf.genomeview.data.Model;
import net.sf.genomeview.gui.menu.AbstractModelAction;
import net.sf.jannot.Feature;

public class CloneFeatureAction extends AbstractModelAction {

	private static final long serialVersionUID = 4521376746707912717L;

	public CloneFeatureAction(Model model) {
		super("Clone selected feature", model);
		
	}

	public void actionPerformed(ActionEvent e) {
		assert (model.selectionModel().getFeatureSelection() != null);
		assert (model.selectionModel().getFeatureSelection().size() == 1);
		Feature rf = model.selectionModel().getFeatureSelection().iterator().next();

		// SimpleFeature rf = (SimpleFeature)
		// model.getFeatureSelection().iterator().next();
		Feature copy = rf.copy();
		model.vlm.getSelectedEntry().getMemoryAnnotation(copy.type()).add(copy);
		model.selectionModel().setLocationSelection(copy);
		// SimpleFeature srf = new SimpleFeature(rf.getSequence(), rf
		// .makeTemplate());
		// srf.setName("clone." + rf.getName());
		// Feature newly = model.addFeature(srf.getName(), (Location) srf
		// .getLocation(), srf.getTypeTerm(), srf.getSourceTerm());
		// model.setFeatureSelection(newly);
		// StaticUtils.getEditStructure(model).setVisible(true);

	}

	@Override
	public void update(Observable o, Object arg) {
		setEnabled(model.selectionModel().getFeatureSelection() != null
				&& model.selectionModel().getFeatureSelection().size() == 1);

	}

}
