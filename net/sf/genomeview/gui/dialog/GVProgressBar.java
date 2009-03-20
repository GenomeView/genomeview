/**
 * %HEADER%
 */
package net.sf.genomeview.gui.dialog;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JProgressBar;

import net.sf.jannot.source.ProgressListener;

/**
 * A popup dialog containing a progress bar and progress messages. It is
 * designed to be passed as a PropertyChangeListener to SwingWorkers.
 * 
 * @author thpar
 * @author Thomas Abeel
 */
public class GVProgressBar implements ProgressListener {

	private JFrame parent = null;

	private GVProgressBarDialog dialog;

	/**
	 * Contructs a JDialog with given title and an initial message. And
	 * positions it relative to the parent window.
	 * 
	 * @param title
	 * @param initText
	 */
	public GVProgressBar(String title, String initText, JFrame parent) {
		this.parent = parent;
		dialog = new GVProgressBarDialog(title, initText);
	}

	private class GVProgressBarDialog extends JDialog {

		private static final long serialVersionUID = 1L;

		private JLabel commentLabel = new JLabel();

		private JProgressBar pb = new JProgressBar(0, 100);

		public GVProgressBarDialog(String title, String initText) {
			super(parent, title);
			setAlwaysOnTop(true);
			requestFocus();
			setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
			setLayout(new GridBagLayout());
			setPreferredSize(new Dimension(280, 80));
			pb.setValue(0);
			GridBagConstraints gbc = new GridBagConstraints();
			gbc.fill = GridBagConstraints.NONE;
			gbc.anchor = GridBagConstraints.CENTER;
			gbc.gridx = 0;
			gbc.gridy = 0;
			gbc.gridy = GridBagConstraints.RELATIVE;
			commentLabel.setText(initText + "...");
			add(commentLabel, gbc);
			add(pb, gbc);
			pack();
			setLocationRelativeTo(parent);

			setVisible(true);

		}

		private void setComment(String comment) {
			commentLabel.setText(comment + "...");
			repaint();
		}

		public void setProgressValue(Integer newValue) {
			pb.setValue(newValue);
			repaint();
		}
	}

	@Override
	public void setDone() {

		dialog.setVisible(false);
		dialog.dispose();
	}

	@Override
	public void setComment(String comment) {
		dialog.setComment(comment);

	}

	@Override
	public void setProgress(int progress) {
		dialog.setProgressValue(progress);

	}

}
