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
import net.sf.genomeview.data.provider.Status;
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

	private ShortReadProvider provider;
	private ShortReadTrackConfig srtc;

	public ShortReadTrack(DataKey key, ShortReadProvider provider, Model model) {
		super(key, model, true, new ShortReadTrackConfig(model, key));
		this.srtc = (ShortReadTrackConfig) config;
		this.provider = provider;
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
						model.center(e.getValue().getMateAlignmentStart());
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
		if (model.getAnnotationLocationVisible().length() < Configuration.getInt("geneStructureNucleotideWindow")) {
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

//	private Location currentVisible;

	// private Color pairingColor;

//	private Rectangle viewRectangle;

	

	

	

//	private Rectangle prevView = null;
	private Location prevVisible = null;
	private boolean prevReady=false;
	private srtRender render=null;

	@Override
	public int paintTrack(Graphics2D gGlobal, int yOffset, double screenWidth, JViewport view, TrackCommunicationModel tcm) {

		// this.view = view;
//		this.viewRectangle = view.getViewRect();
		// /* Store information to be used in other methods */
		// currentEntry = entry;
		// currentScreenWidth = screenWidth;
	
		// this.currentYOffset = yOffset;
		/* Configuration options */
		

		if (provider == null)
			return 0;

		/* Also check that config options remained the same */
		Location currentVisible=model.getAnnotationLocationVisible();
		Iterable<Status> status = provider.getStatus(currentVisible.start, currentVisible.end);
		boolean ready=true;
		for(Status s:status){
			ready &=s.isReady();
		}
		if (!same(currentVisible,ready)) {
			render=new srtRender(entry,dataKey,provider, currentVisible,srtc, screenWidth);
			
			

		

		} else {
			//System.out.println("Using bufferedimage!");
		}

		gGlobal.drawImage(render.buffer(), 0, yOffset, null);
		// return yOffset - originalYOffset;
		return render.buffer().getHeight();
	}

	private boolean same(Location loc,  boolean ready) {
		boolean same =  loc.equals(prevVisible)&&prevReady==ready;
//		prevView = view;
		prevReady=ready;
		prevVisible = loc;
		return same;
	}

	


	public void clear() {
//		paintedBlocks.clear();
//		hitMap.clear();
		provider = null;

	}

}
