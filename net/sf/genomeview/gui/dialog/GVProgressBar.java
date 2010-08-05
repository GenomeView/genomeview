/**
 * %HEADER%
 */
package net.sf.genomeview.gui.dialog;

import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JProgressBar;

import net.sf.genomeview.gui.StaticUtils;

/**
 * A popup dialog containing a progress bar and progress messages. It is
 * designed to be passed as a PropertyChangeListener to SwingWorkers.
 * 
 * @author thpar
 * @author Thomas Abeel
 */
public class GVProgressBar extends JDialog {

	private static final long serialVersionUID = -84766714924209282L;

	private JLabel commentLabel = new JLabel();

	private JProgressBar pb = new JProgressBar();

	
	public GVProgressBar(String title, String initText, Frame parent) {
		super(parent, title);
		setAlwaysOnTop(true);
		requestFocus();
		setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
		setLayout(new GridBagLayout());
		setPreferredSize(new Dimension(280, 80));
		pb.setIndeterminate(true);
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.fill = GridBagConstraints.BOTH;
		gbc.anchor = GridBagConstraints.CENTER;
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.gridy = GridBagConstraints.RELATIVE;
		commentLabel.setText(initText);
		add(commentLabel, gbc);
		add(pb, gbc);
		pack();
		StaticUtils.upperRight(this);

		setVisible(true);
	}

	

	public void done() {

		setVisible(false);
		dispose();
	}

	public void setComment(String comment) {
		commentLabel.setText(comment);

	}

	
	public void setMax(int max){
		pb.setIndeterminate(false);
		pb.setMaximum(max);
	}
	public void setProgress(int progress) {
		pb.setValue(progress);

	}



	public void inc() {
		pb.setValue(pb.getValue()+1);
		
	}
	
}
