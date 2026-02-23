package net.sf.genomeview.gui.menu.file;

import java.awt.event.ActionEvent;

import net.sf.genomeview.data.Model;
import net.sf.genomeview.gui.MessageManager;
import net.sf.genomeview.gui.dialog.ExportPdfDialog;
import net.sf.genomeview.gui.menu.AbstractModelAction;

@SuppressWarnings("serial")
public class ExportPDFAction extends AbstractModelAction {

	public ExportPDFAction(Model model) {
		super(MessageManager.getString("filemenu.export_pdf"), model);

	}

	public void actionPerformed(ActionEvent arg0) {
		new ExportPdfDialog(model).display();
	}

}
