/**
 * %HEADER%
 */
package net.sf.genomeview.gui.menu.edit;

import java.awt.event.ActionEvent;
import java.util.HashSet;
import java.util.Observable;
import java.util.Observer;
import java.util.Set;

import net.sf.genomeview.data.Model;
import net.sf.genomeview.data.NotificationTypes;
import net.sf.genomeview.gui.MessageManager;
import net.sf.genomeview.gui.menu.AbstractModelAction;
import net.sf.jannot.Feature;
/**
 * 
 * @author Thomas Abeel
 *
 */
public class RemoveAction extends AbstractModelAction implements Observer {

	private static final long serialVersionUID = -3409728329439144492L;

	public RemoveAction(Model model) {
		super(MessageManager.getString("editmenu.remove_feature"), model);
		model.addObserver(this);
		setEnabled(model.selectionModel().getFeatureSelection() != null&&model.selectionModel().getFeatureSelection().size()>0);
	}

	public void actionPerformed(ActionEvent arg0) {

		Set<Feature> toRemove = new HashSet<Feature>();
		toRemove.addAll(model.selectionModel().getFeatureSelection());
		for (Feature rf : toRemove)
			model.vlm.getSelectedEntry().getMemoryAnnotation(rf.type()).remove(rf);
		model.refresh(NotificationTypes.JANNOTCHANGE);
	}

	public void update(Observable o, Object arg) {
		setEnabled(model.selectionModel().getFeatureSelection() != null&&model.selectionModel().getFeatureSelection().size()>0);

	}

}
