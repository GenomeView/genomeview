/**
 * %HEADER%
 */
package net.sf.genomeview.gui.information;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Observable;
import java.util.Observer;
import java.util.SortedSet;

import javax.swing.JLabel;

import net.sf.genomeview.core.Configuration;
import net.sf.genomeview.data.Model;
import net.sf.genomeview.gui.components.CollisionMap;
import net.sf.jannot.AminoAcidMapping;
import net.sf.jannot.Entry;
import net.sf.jannot.Feature;
import net.sf.jannot.Location;
import net.sf.jannot.Strand;
import net.sf.jannot.refseq.Sequence;
import net.sf.jannot.utils.SequenceTools;

/**
 * A label to paint the structure of the currently selected CDS. If there is one
 * selected.
 * 
 * @author Thomas Abeel
 * 
 */
public class GeneStructureView extends JLabel implements Observer {

	private Model model;

	private Feature rf = null;

	private Entry entry;

	private CollisionMap collisionMap;

	public GeneStructureView(Model model) {
		this.model = model;
		collisionMap = new CollisionMap(model);

		model.addObserver(this);
		this.setPreferredSize(new Dimension(200, 50));
		this.setBackground(Color.WHITE);
		this.setOpaque(true);
		final GeneStructureView _self = this;

		this.addMouseListener(new MouseAdapter() {

			public void mouseClicked(MouseEvent e) {
				Location l = collisionMap.uniqueLocation(e.getX(), e.getY());
				if (e.getClickCount() > 1 && l != null) {
					int gap = (int) (l.length() * 0.05);
					_self.model.setAnnotationLocationVisible(new Location(l.start() - gap, l.end() + gap));
				} else {
					int hGap = (int) (_self.getWidth() * 0.05);
					double posPixelRatio = (double) (_self.getWidth() * 0.90) / (double) rf.length();
					int pos = (int) ((e.getX() - hGap) / posPixelRatio);
					System.out.println("CDSView click:" + pos);
					if (rf != null)
						_self.model.center(rf.start() + pos);
				}
			}

			@Override
			public void mouseReleased(MouseEvent e) {

			}

		});

	}

	private static final long serialVersionUID = 4645397653645650758L;

	@Override
	public void paintComponent(Graphics g) {

		super.paintComponent(g);

		if (model.selectionModel().getFeatureSelection().size() == 1
				&& Configuration.getTypeSet("geneStructures").contains(
						model.selectionModel().getFeatureSelection().first().type())) {
			rf = model.selectionModel().getFeatureSelection().first();
			entry = model.getSelectedEntry();
		}
		if (rf != null) {
			renderCDS((Graphics2D) g, rf);

		}
	}

	/**
	 * Calculates the draw frame of the location. Can be 1, 2 or 3.
	 * 
	 * @param l
	 *            location to calculate frame for
	 * @param rf
	 *            the feature to which the location belongs
	 * @return the drawing frame index, can be 1, 2 or 3
	 */
	private int getDrawFrame(Location l, Feature rf) {
		int locFrame;
		if (rf.strand() == Strand.REVERSE)
			locFrame = (l.end() + 1) % 3;
		else
			locFrame = (l.start()) % 3;
		if (locFrame == 0)
			locFrame = 3;
		int phase = rf.getPhase(l);// 0,1 or 2

		int sum;
		if (rf.strand() == Strand.REVERSE)
			sum = locFrame + 3 - phase;
		else
			sum = locFrame + phase;
		int drawFrame = sum % 3;
		drawFrame = (drawFrame == 0 ? 3 : drawFrame);
		if (rf.strand() == Strand.FORWARD)
			return 4 - drawFrame;
		else
			return drawFrame;
	}

	class AnalyzedFeature {

		private Sequence seq;
		private Feature f;
		private AminoAcidMapping aa;
		private String translation;
		private Sequence dna;
		private String startCodon;

		public AnalyzedFeature(Sequence sequence, Feature rf, AminoAcidMapping aminoAcidMapping) {
			this.seq = sequence;
			this.f = rf;
			this.aa = aminoAcidMapping;
			dna = SequenceTools.extractSequence(seq, f);
			startCodon = dna.subsequence(1, 4).stringRepresentation();
			translation = SequenceTools.translate(dna, aa);

		}

		/**
		 * Checks whether the provided location misses an acceptor (AG).
		 * 
		 * @param seq
		 * @param f
		 * @param l
		 * @return
		 */
		public boolean missingAcceptor(Location l) {
			if (!l.equals(rf.location().first()) && f.strand() == Strand.FORWARD) {
				String s = seq.subsequence(l.start - 2, l.start).stringRepresentation();
				return !(s.equalsIgnoreCase("ag"));
			}
			if (!l.equals(rf.location().last()) && f.strand() == Strand.REVERSE) {
				String s = seq.subsequence(l.end + 1, l.end + 3).stringRepresentation();
				return !(s.equalsIgnoreCase("ct"));
			}
			return false;

		}

		/**
		 * Checks whether the provided location misses a donor (GT).
		 * 
		 * @param seq
		 *            sequence to which the feature maps
		 * @param f
		 *            feature to which the location belongs
		 * @param l
		 *            location to check for missing donor
		 * @return
		 */
		public boolean missingDonor(Location l) {

			if (!l.equals(rf.location().last()) && f.strand() == Strand.FORWARD) {
				String s = seq.subsequence(l.end + 1, l.end + 3).stringRepresentation();
				return !(s.equalsIgnoreCase("gt") || s.equalsIgnoreCase("gc"));
			}
			if (!l.equals(rf.location().first()) && f.strand() == Strand.REVERSE) {
				String s = seq.subsequence(l.start - 2, l.start).stringRepresentation();
				return !(s.equalsIgnoreCase("ac") || s.equalsIgnoreCase("gc"));
			}
			return false;
		}

		public boolean hasMissingStartCodon() {
			return !aa.isStart(startCodon);

		}

		public boolean hasMissingStopCodon() {
			return !translation.endsWith("*");

		}

		/**
		 * Checks the feature for internal stop codons.
		 * 
		 * @param sequence
		 * @param rf
		 * @return
		 */
		public List<Location> getIternalStopCodons() {
			ArrayList<Location> out = new ArrayList<Location>();
			for (int i = 0; i < translation.length() - 1; i++) {
				if (translation.charAt(i) == '*') {
					out.add(getntpos(i));
				}
			}
			return out;
		}

		private Location getntpos(int aapos) {
			ArrayList<Integer> list = new ArrayList<Integer>();
			for (Location l : rf.location()) {
				for (int i = l.start; i <= l.end; i++)
					list.add(i);
			}
			if (rf.strand() == Strand.REVERSE)
				Collections.reverse(list);
			return new Location(list.get(aapos * 3), list.get(aapos * 3 + 2));

		}

	}

	/**
	 * Will render a CDS based on a feature.
	 * 
	 * <code>
	 * +-----------------------+
	 * | forward frame 2       |
	 * +-----------------------+
	 * | forward frame 1       |
	 * +-----------------------+
	 * | forward frame 0       |
	 * +-----------------------+
	 * | forward nucleotides   |
	 * +=======================+
	 * | position tick marks   |
	 * +=======================+
	 * | reverse nucleotides   |
	 * +-----------------------+
	 * | reverse frame 0       |
	 * +-----------------------+
	 * | reverse frame 1       |
	 * +-----------------------+
	 * | reverse frame 2       |
	 * +-----------------------+
     * </code>
	 */
	private void renderCDS(Graphics2D g, Feature rf) {
		Color[] background = new Color[] { new Color(204, 238, 255, 100), new Color(255, 255, 204, 100),
				new Color(204, 238, 255, 100) };

		int lineHeight = (int) (0.9 * (this.getHeight() / 3.0));

		double posPixelRatio = (double) (this.getWidth() * 0.90) / (double) rf.length();
		int hGap = (int) (this.getWidth() * 0.05);
		int vGap = (int) (this.getHeight() * 0.05);

		g.setColor(background[0]);
		g.fillRect(0, 0, this.getWidth(), lineHeight + vGap);
		g.setColor(background[1]);
		g.fillRect(0, lineHeight + vGap, this.getWidth(), lineHeight);
		g.setColor(background[0]);
		g.fillRect(0, 2 * lineHeight + vGap, this.getWidth(), lineHeight + vGap);

		Location last = null;
		int lastY = 0;
		int arrowDrawFrame = -1;
		AnalyzedFeature af = new AnalyzedFeature(entry.sequence(), rf, model.getAAMapping());
		HashMap<Location, Integer> drawFrameMapping = new HashMap<Location, Integer>();
		for (Location l : rf.location()) {
			int drawFrame = getDrawFrame(l, rf);
			drawFrameMapping.put(l, drawFrame);
			/* Keep track of the frame we have to draw the arrow in */
			if (rf.strand() == Strand.REVERSE && last == null)
				arrowDrawFrame = drawFrame;
			else if (rf.strand() == Strand.FORWARD)
				arrowDrawFrame = drawFrame;

			/* Start of the block */
			int lmin = (int) ((l.start() - rf.start()) * posPixelRatio);
			/* End of the block */
			int lmax = (int) ((l.end() - rf.start() + 1) * posPixelRatio);
			/* Horizontal position */
			int hor;
			// if (rf.strand() == Strand.REVERSE)
			// hor = middle + (drawFrame * lineHeight) + tickHeight / 2 + gap;
			// else
			hor = ((drawFrame - 1) * lineHeight);

			/* Create box */
			Rectangle r = new Rectangle(lmin + hGap, hor + vGap, lmax - lmin, lineHeight);
			/* Draw box */
			Color cdsColor = Configuration.getColor("TYPE_CDS");
			g.setColor(cdsColor);
			g.fill(r);

			/* Draw darker box outline */
			g.setColor(cdsColor.darker());
			g.draw(r);

			/* Draw wrong splice site lines */
			g.setStroke(new BasicStroke(4.0f));
			if (af.missingDonor(l)) {
				g.setColor(Color.RED);
				if (rf.strand() == Strand.FORWARD)
					g.drawLine(r.x + r.width, r.y, r.x + r.width, r.y + r.height);
				else
					g.drawLine(r.x, r.y, r.x, r.y + r.height);
			}
			if (af.missingAcceptor(l)) {
				g.setColor(Color.RED);
				if (rf.strand() == Strand.REVERSE)
					g.drawLine(r.x + r.width, r.y, r.x + r.width, r.y + r.height);
				else
					g.drawLine(r.x, r.y, r.x, r.y + r.height);
			}
			g.setColor(Color.RED);
			/* Draw missing start codon */
			if (af.hasMissingStartCodon()) {
				if (rf.strand() == Strand.FORWARD && l.equals(rf.location().first())) {
					g.drawLine(r.x, r.y, r.x, r.y + r.height);
				}
				if (rf.strand() == Strand.REVERSE && l.equals(rf.location().last())) {
					g.drawLine(r.x + r.width, r.y, r.x + r.width, r.y + r.height);
				}
			}
			/* Draw missing stop codon */
			if (af.hasMissingStopCodon()) {
				if (rf.strand() == Strand.FORWARD && l.equals(rf.location().last())) {
					g.drawLine(r.x, r.y, r.x, r.y + r.height);
				}
				if (rf.strand() == Strand.REVERSE && l.equals(rf.location().first())) {
					g.drawLine(r.x + r.width, r.y, r.x + r.width, r.y + r.height);
				}
			}
			
			g.setStroke(new BasicStroke(1.0f));

			/* Draw line between boxes */
			// FIXME may have stopcodon overlapping, make sure to indicate it
			if (last != null) {
				int lastX = (int) ((last.end() - rf.start() + 1) * posPixelRatio);// translateGenomeToScreen(last.end()
				// + 1,
				// model.getAnnotationLocationVisible());
				int currentX = (int) ((l.start() - rf.start()) * posPixelRatio);// translateGenomeToScreen(l.start(),
				// model.getAnnotationLocationVisible());
				int currentY = hor + lineHeight / 2;
				int maxY = Math.min(currentY, lastY) - lineHeight / 2;
				int middleX = (lastX + currentX) / 2;
				g.setColor(Color.BLACK);
				g.drawLine(lastX + hGap, lastY + vGap, middleX + hGap, maxY + vGap);
				g.drawLine(middleX + hGap, maxY + vGap, currentX + hGap, currentY + vGap);
			}

			collisionMap.addLocation(r, l);
			last = l;
			lastY = hor + lineHeight / 2;

		}
		/* Draw arrow */
		g.setColor(Color.BLACK);
		int hor = ((arrowDrawFrame - 1) * lineHeight) + vGap;
		int arrowLenght = 10;
		if (rf.strand() == Strand.FORWARD) {
			int x = (int) (this.getWidth() * 0.95);
			g.drawLine(x, hor, x + arrowLenght, hor + (lineHeight / 2));
			g.drawLine(x + arrowLenght, hor + (lineHeight / 2), x, hor + lineHeight);
		} else {
			int x = (int) (this.getWidth() * 0.05);
			g.drawLine(x, hor, x - arrowLenght, hor + (lineHeight / 2));
			g.drawLine(x - arrowLenght, hor + (lineHeight / 2), x, hor + lineHeight);
		}
		/* Draw inframe stop codons */
		List<Location> list = af.getIternalStopCodons();
		for (Location l : list) {
			/* Start of the block */
			int lmin = (int) ((l.start() - rf.start()) * posPixelRatio);
			/* End of the block */
			int lmax = (int) ((l.end() - rf.start() + 1) * posPixelRatio);
			/* Horizontal position */

			// if (rf.strand() == Strand.REVERSE)
			// hor = middle + (drawFrame * lineHeight) + tickHeight / 2 + gap;
			// else
			for (java.util.Map.Entry<Location, Integer> en : drawFrameMapping.entrySet()) {
				if (en.getKey().overlaps(l))
					hor = ((en.getValue() - 1) * lineHeight);
			}

			/* Create box */
			Rectangle r = new Rectangle(lmin + hGap, hor + vGap, lmax - lmin, lineHeight);
			/* Draw box */
			g.setColor(Color.RED);

			g.fill(r);
		}

		/* --------------------- */
		/* Paint viewport */
		/* --------------------- */
		Location l = model.getAnnotationLocationVisible();
		/* Start of the block */
		int lmin = (int) ((l.start() - rf.start()) * posPixelRatio);
		/* End of the block */
		int lmax = (int) ((l.end() - rf.start() + 1) * posPixelRatio);

		/* Create box */
		Rectangle r = new Rectangle(lmin + hGap - 1, 0, lmax - lmin + 1, this.getHeight() - 1);
		g.setColor(Color.RED);
		g.draw(r);

	}

	@Override
	public void update(Observable o, Object arg) {
		if (!model.getSelectedEntry().equals(entry)) {
			rf = null;
			entry = null;
		}

		repaint();

	}

}
