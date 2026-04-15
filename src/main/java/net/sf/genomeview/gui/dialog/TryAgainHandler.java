package net.sf.genomeview.gui.dialog;

import java.util.logging.Level;

import javax.swing.JOptionPane;

import net.sf.genomeview.data.Model;

public class TryAgainHandler {

	public static void ask(Model model, String message, Runnable job) {

		int result = JOptionPane.showConfirmDialog(
				model.getGUIManager().getMainWindow(), message);
		if (result == JOptionPane.YES_NO_OPTION) {
			try {
				job.run();
			} catch (RuntimeException re) {
				model.getLog().log(Level.WARNING,
						"Failed to retry this instruction", re);

			}

		}

	}
}
