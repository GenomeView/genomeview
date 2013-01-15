/**
 * %HEADER%
 */
package net.sf.genomeview.gui.viztracks.hts;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JViewport;
import javax.swing.JWindow;
import javax.swing.border.Border;

import net.sf.genomeview.core.Configuration;
import net.sf.genomeview.data.Model;
import net.sf.genomeview.data.provider.ShortReadProvider;
import net.sf.genomeview.gui.Convert;
import net.sf.genomeview.gui.viztracks.Track;
import net.sf.genomeview.gui.viztracks.TrackCommunicationModel;
import net.sf.jannot.DataKey;
import net.sf.jannot.Location;
import net.sf.samtools.SAMRecord;

/**
 * 
 * @author Thomas Abeel
 * 
 */
public class ShortReadTrack extends Track {

	
	private srtRender render;
	
//	private ShortReadProvider provider;
	private ShortReadTrackConfig srtc;

	public ShortReadTrack(DataKey key, ShortReadProvider provider, Model model) {
		super(key, model, true, new ShortReadTrackConfig(model, key));
		this.srtc = (ShortReadTrackConfig) config;
//		this.provider = provider;
		render=new srtRender(model,provider,srtc, key);
	}

	private InsertionTooltip tooltip = new InsertionTooltip();
	private ReadInfo readinfo = new ReadInfo();

	private static class InsertionTooltip extends JWindow {

		private static final long serialVersionUID = -7416732151483650659L;

		private JLabel floater = new JLabel();

		public InsertionTooltip() {
			floater.setBackground(Color.GRAY);
			floater.setForeground(Color.BLACK);
			Border emptyBorder = BorderFactory.createEmptyBorder(5, 5, 5, 5);
			Border colorBorder = BorderFactory.createLineBorder(Color.BLACK);
			floater.setBorder(BorderFactory.createCompoundBorder(colorBorder, emptyBorder));
			add(floater);
			pack();
		}

		public void set(MouseEvent e, ShortReadInsertion sri) {
			if (sri == null)
				return;
			StringBuffer text = new StringBuffer();
			text.append("<html>");

			if (sri != null) {
				text.append("Insertion: ");
				byte[] bases = sri.esr.getReadBases();
				for (int i = sri.start; i < sri.start + sri.len; i++) {
					text.append((char) bases[i]);
				}
				text.append("<br/>");
			}
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

	private static class ReadInfo extends JWindow {

		private static final long serialVersionUID = -7416732151483650659L;

		private JLabel floater = new JLabel();

		public ReadInfo() {
			floater.setBackground(Color.GRAY);
			floater.setForeground(Color.BLACK);
			Border emptyBorder = BorderFactory.createEmptyBorder(5, 5, 5, 5);
			Border colorBorder = BorderFactory.createLineBorder(Color.BLACK);
			floater.setBorder(BorderFactory.createCompoundBorder(colorBorder, emptyBorder));
			add(floater);
			pack();
		}

		public void set(MouseEvent e, SAMRecord sr) {
			if (sr == null)
				return;
			StringBuffer text = new StringBuffer();
			text.append("<html>");

			if (sr != null) {
				text.append("Name: " + sr.getReadName() + "<br/>");
				text.append("Len: " + sr.getReadLength() + "<br/>");
				text.append("Cigar: " + sr.getCigarString() + "<br/>");
				text.append("Sequence: " + rerun(sr.getReadString()) + "<br/>");
				text.append("Paired: " + sr.getReadPairedFlag() + "<br/>");
				if (sr.getReadPairedFlag()) {
					if (!sr.getMateUnmappedFlag())
						text.append("Mate: " + sr.getMateReferenceName() + ":" + sr.getMateAlignmentStart() + "<br/>");
					else
						text.append("Mate missing" + "<br/>");
					text.append("Second: " + sr.getFirstOfPairFlag());
				}
				// text.append("<br/>");
			}
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

	private static String rerun(String arg) {
		StringBuffer out = new StringBuffer();
		int i = 0;
		for (; i < arg.length() - 80; i += 80)
			out.append(arg.substring(i, i + 80) + "<br/>");
		out.append(arg.substring(i, arg.length()));
		return out.toString();

	}

	@Override
	public boolean mouseExited(int x, int y, MouseEvent source) {
		tooltip.setVisible(false);
		readinfo.setVisible(false);
		return false;
	}

	@Override
	public boolean mouseClicked(int x, int y, MouseEvent source) {
		super.mouseClicked(x, y, source);
		if (source.isConsumed())
			return true;
		// System.out.println("Click: " + x + " " + y);
		if (source.getClickCount() > 1) {
			for (java.util.Map.Entry<Rectangle, SAMRecord> e : render.hitMap.entrySet()) {
				if (e.getKey().contains(x, y)) {
					System.out.println("2*Click: " + e.getValue());
					if (e.getValue().getReadPairedFlag() && !e.getValue().getMateUnmappedFlag())
						model.vlm.center(e.getValue().getMateAlignmentStart());
				}
			}
		}

		return false;
	}

	public boolean mouseDragged(int x, int y, MouseEvent source) {
		tooltip.setVisible(false);
		readinfo.setVisible(false);
		return false;

	}

	@Override
	public boolean mouseMoved(int x, int y, MouseEvent source) {
		if (model.vlm.getAnnotationLocationVisible().length() < Configuration.getInt("geneStructureNucleotideWindow")) {
			ShortReadInsertion sri = null;
			for (java.util.Map.Entry<Rectangle, ShortReadInsertion> e : render.paintedBlocks.entrySet()) {
				if (e.getKey().contains(x, y)) {
					sri = e.getValue();
					break;
				}
			}

			if (sri != null) {
				if (!tooltip.isVisible())
					tooltip.setVisible(true);
				tooltip.set(source, sri);
			} else {
				if (tooltip.isVisible())
					tooltip.setVisible(false);
			}
			//
			// System.out.println("Moved: " + x + " " + y);
			for (java.util.Map.Entry<Rectangle, SAMRecord> e : render.hitMap.entrySet()) {
				if (e.getKey().contains(x, y)) {
					// System.out.println("Prijs: " + e.getValue());
					readinfo.set(source, e.getValue());
				}
			}
			//
			return false;

		} else {
			if (tooltip.isVisible())
				tooltip.setVisible(false);
		}
		return false;
	}

	@Override
	public int paintTrack(Graphics2D gGlobal, int yOffset, double screenWidth, JViewport view, TrackCommunicationModel tcm) {
		Location bufferedLocation=render.location();
		Location visible=model.vlm.getVisibleLocation();
		int x=0;
		if(bufferedLocation!=null)
			x=Convert.translateGenomeToScreen(bufferedLocation.start,visible, screenWidth);
		gGlobal.drawImage(render.buffer(), x, yOffset, null);
		return render.buffer().getHeight();
	}

	

	



}
