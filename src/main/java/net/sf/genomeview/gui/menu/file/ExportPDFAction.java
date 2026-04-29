package net.sf.genomeview.gui.menu.file;

import java.awt.event.ActionEvent;
import java.util.Observable;

import net.sf.genomeview.data.Model;
import net.sf.genomeview.gui.dialog.ExportPdfDialog;
import net.sf.genomeview.gui.menu.AbstractModelAction;

@SuppressWarnings("serial")
public class ExportPDFAction extends AbstractModelAction {

	public ExportPDFAction(Model model) {
		super(model.getMessageMgr().getString("filemenu.export_pdf"), model);

	}

	@Override
	public void actionPerformedSafe(ActionEvent arg0) {
		new ExportPdfDialog(model).display();
	}

	@Override
	public void updateSafe(Observable o, Object obj) {
		// ignore
	}

}
