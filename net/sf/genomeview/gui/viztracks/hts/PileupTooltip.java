/**
 * %HEADER%
 */
package net.sf.genomeview.gui.viztracks.hts;

import java.awt.Color;
import java.awt.event.MouseEvent;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JWindow;
import javax.swing.border.Border;

/**
 * 
 * @author Thomas Abeel
 * 
 */
class PileupTooltip extends JWindow {

	private static final long serialVersionUID = -7416732151483650659L;

	private JLabel floater = new JLabel();

	private PileupTrackModel track;

	public PileupTooltip(PileupTrackModel ptm) {
		this.track = ptm;

		floater.setBackground(Color.GRAY);
		floater.setForeground(Color.BLACK);
		Border emptyBorder = BorderFactory.createEmptyBorder(5, 5, 5, 5);
		Border colorBorder = BorderFactory.createLineBorder(Color.BLACK);
		floater.setBorder(BorderFactory.createCompoundBorder(colorBorder, emptyBorder));
		add(floater);
		pack();
	}

	public void set(MouseEvent e) {
		
		VizBuffer vb= track.getVizBuffer();
		String text = vb.getTooltip(e.getX());
		if(text==null){
			setVisible(false);
			return;
		}
		if (text!=null&&!text.toString().equals(floater.getText())) {
			floater.setText(text.toString());
			this.pack();
		}
		setLocation(e.getXOnScreen() + 5, e.getYOnScreen() + 5);

		if (!isVisible()) {
			setVisible(true);
		}
	}
}
