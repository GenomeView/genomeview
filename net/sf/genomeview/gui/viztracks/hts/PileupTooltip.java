/**
 * %HEADER%
 */
package net.sf.genomeview.gui.viztracks.hts;

import java.awt.Color;
import java.awt.event.MouseEvent;
import java.text.NumberFormat;
import java.util.Locale;

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
	private NumberFormat nf = NumberFormat.getPercentInstance(Locale.US);

	private static final long serialVersionUID = -7416732151483650659L;

	private JLabel floater = new JLabel();

	private PileupTrackModel track;

	public PileupTooltip(PileupTrackModel track) {
		this.track = track;
		nf.setMaximumFractionDigits(1);
		floater.setBackground(Color.GRAY);
		floater.setForeground(Color.BLACK);
		Border emptyBorder = BorderFactory.createEmptyBorder(5, 5, 5, 5);
		Border colorBorder = BorderFactory.createLineBorder(Color.BLACK);
		floater.setBorder(BorderFactory.createCompoundBorder(colorBorder, emptyBorder));
		add(floater);
		pack();
	}

	public void set(MouseEvent e) {
		if (track.isDetailed() && track.nc != null) {
			StringBuffer text = new StringBuffer();
			text.append("<html>");
			int effectivePosition = track.translateFromMouse(e.getX());

			int total = track.nc.getTotalCount(effectivePosition);

			if (track.nc.hasData()) {
				text.append("<strong>Matches:</strong> " + format(track.nc.getCount('.', effectivePosition), total)
						+ "<br/>");
				text.append("<strong>Mismatches:</strong><br/>");
				text.append("A: " + format(track.nc.getCount('A', effectivePosition), total));
				text.append("<br/>");
				text.append("T: " + format(track.nc.getCount('T', effectivePosition), total));
				text.append("<br/>");
				text.append("G: " + format(track.nc.getCount('G', effectivePosition), total));
				text.append("<br/>");
				text.append("C: " + format(track.nc.getCount('C', effectivePosition), total));
				text.append("<br/>");
			}
			text.append("<strong>Coverage:</strong> "
					+ (track.detailedRects[0][effectivePosition] + track.detailedRects[1][effectivePosition]) + "<br/>");
			text.append("Forward: " + track.detailedRects[0][effectivePosition] + "<br/>");
			text.append("Reverse: " + track.detailedRects[1][effectivePosition] + "<br/>");

			text.append("</html>");
			if (!text.toString().equals(floater.getText())) {
				floater.setText(text.toString());
				this.pack();
			}
			setLocation(e.getXOnScreen() + 5, e.getYOnScreen() + 5);

			if (!isVisible()) {
				setVisible(true);
			}
		}
	}

	private String format(int count, int total) {
		if (total > 0)
			return count + " (" + nf.format(count / (double) total) + ")";
		else
			return "" + count;
	}
}