package net.sf.genomeview.gui.dialog;

import javax.swing.JOptionPane;

import net.sf.genomeview.data.Model;
import net.sf.genomeview.gui.CrashHandler;

public class TryAgainHandler {

	public static void ask(Model model, String message, Runnable job) {

		int result = JOptionPane.showConfirmDialog(model.getGUIManager().getParent(), message);
		if (result == JOptionPane.YES_NO_OPTION) {
			try {
				job.run();
			} catch (RuntimeException re) {

				CrashHandler.showErrorMessage("Failed to retry this instruction", re);

			}

		}

	}
}
