/**
 * %HEADER%
 */
package net.sf.genomeview.gui.viztracks.hts;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Observable;
import java.util.Observer;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.AbstractAction;
import javax.swing.ButtonGroup;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JViewport;

import net.sf.genomeview.core.Configuration;
import net.sf.genomeview.data.Model;
import net.sf.genomeview.data.provider.PileProvider;
import net.sf.genomeview.data.provider.Status;
import net.sf.genomeview.gui.viztracks.Track;
import net.sf.genomeview.gui.viztracks.TrackCommunicationModel;
import net.sf.jannot.DataKey;
import net.sf.jannot.Location;

import org.broad.igv.track.WindowFunction;

/**
 * 
 * @author Thomas Abeel
 * 
 */
public class PileupTrack extends Track {

	private NumberFormat nf = NumberFormat.getInstance(Locale.US);
	private PileProvider provider;

	// private String label;

	public PileupTrack(DataKey key, PileProvider provider, final Model model) {
		super(key, model, true, false);

		ptm = new PileupTrackModel(model);
		tooltip = new PileupTooltip(ptm);
		this.provider = provider;

		this.provider.addObserver(new Observer() {

			@Override
			public void update(Observable o, Object arg) {
				// System.out.println("\tInvalidating track vizbuffers");
				ptm.lastQuery = null;
				/* Force repaint */
				model.refresh();

			}
		});

		nf.setMaximumFractionDigits(0);

	}

	private PileupTrackModel ptm;

	private PileupTooltip tooltip;

	@Override
	public boolean mouseExited(int x, int y, MouseEvent source) {
		tooltip.setVisible(false);
		return false;
	}

	@Override
	public boolean mouseMoved(int x, int y, MouseEvent source) {
		tooltip.set(source);
		return false;
	}

	private Logger log = Logger.getLogger(PileupTrack.class.toString());

	@Override
	public int paintTrack(Graphics2D g, int yOffset, double screenWidth, JViewport view, TrackCommunicationModel tcm) {

		ptm.setScreenWidth(screenWidth);
		ptm.setTrackCommunication(tcm);
		// System.out.println("- drawing track "+this);
		Location visible = model.getAnnotationLocationVisible();
		/* Status messages for data queuing an retrieval */
		// Iterable<Status> status = provider.getStatus(visible.start,
		// visible.end);
		Iterable<Status> status = provider.getStatus(visible.start, visible.end);
		/* Only retrieve data when location changed */
		if (ptm.lastQuery == null || !ptm.lastQuery.equals(visible)) {
			// System.out.println("--Using fresh data from provider in track");
			/* The actual data */
			// Iterable<Pile> piles = provider.get(visible.start, visible.end);

			if (model.getAnnotationLocationVisible().length() < Configuration.getInt("pileup:switchBarLine")) {
				// System.out.println("Track: "+this+"\t"+provider);
				ptm.setVizBuffer(new BarChartBuffer(visible, provider, ptm));
			} else
				ptm.setVizBuffer(new LineChartBuffer(visible, provider, ptm));

			ptm.lastQuery = model.getAnnotationLocationVisible();
		}

		/* Do the actual painting */
		int graphLineHeigh = ptm.getVizBuffer().draw(g, yOffset, screenWidth);

		g.setColor(Color.BLACK);

		g.drawString(displayName(), 10, yOffset + 24 - 2);

		return graphLineHeigh;

	}

	/* User settable maximum value for charts, use negative for unlimited */

	@Override
	public List<JMenuItem> getMenuItems() {
		ArrayList<JMenuItem> out = new ArrayList<JMenuItem>();

		
		/* Use global settings */
		final JCheckBoxMenuItem itemCrossTrack = new JCheckBoxMenuItem();
		itemCrossTrack.setSelected(ptm.isCrossTrackScaling());
		itemCrossTrack.setAction(new AbstractAction("Scale across tracks") {
			@Override
			public void actionPerformed(ActionEvent e) {
				ptm.setCrossTrackScaling(itemCrossTrack.isSelected());

			}

		});
		out.add(itemCrossTrack);
		
		/* Use global settings */
		final JCheckBoxMenuItem itemGlobal = new JCheckBoxMenuItem();
		itemGlobal.setSelected(ptm.isGlobalSettings());
		itemGlobal.setAction(new AbstractAction("Track uses defaults") {
			@Override
			public void actionPerformed(ActionEvent e) {
				ptm.setGlobalSettings(itemGlobal.isSelected());
			}

		});
		out.add(itemGlobal);
	
		

		if (!ptm.isGlobalSettings()) {
			

			/* Log scaling of line graph */
			final JCheckBoxMenuItem item = new JCheckBoxMenuItem();
			item.setSelected(ptm.isLogscaling());
			item.setAction(new AbstractAction("Use log scaling for line graph") {
				@Override
				public void actionPerformed(ActionEvent e) {
					ptm.setLogscaling(item.isSelected());

				}

			});
			out.add(item);

			/* Dynamic scaling for both plots */
			final JCheckBoxMenuItem item3 = new JCheckBoxMenuItem();
			item3.setSelected(ptm.isDynamicScaling());
			item3.setAction(new AbstractAction("Use dynamic scaling for plots") {
				@Override
				public void actionPerformed(ActionEvent e) {
					ptm.setDynamicScaling(item3.isSelected());

				}

			});
			out.add(item3);

			/* Maximum value */
			final JMenuItem item2 = new JMenuItem(new AbstractAction("Set maximum value") {

				@Override
				public void actionPerformed(ActionEvent e) {
					String in = JOptionPane.showInputDialog(model.getGUIManager().getParent(),
							"Input the maximum value, choose a negative number for unlimited", "Input maximum value",
							JOptionPane.QUESTION_MESSAGE);
					if (in != null) {
						try {
							Double d = Double.parseDouble(in);
							ptm.setMaxValue(d);
						} catch (Exception ex) {
							log.log(Level.WARNING, "Unparseble value for maximum in PileupTrack: " + in, ex);
						}
					}

				}
			});
			out.add(item2);

			ButtonGroup bg = new ButtonGroup();
			for (final WindowFunction wf : provider.getWindowFunctions()) {
				final JRadioButtonMenuItem jbm = new JRadioButtonMenuItem();
				jbm.setAction(new AbstractAction(wf.toString()) {

					@Override
					public void actionPerformed(ActionEvent e) {
						jbm.setSelected(true);
						// System.out.println("Requesting "+wf.toString());
						provider.requestWindowFunction(wf);
					}
				});
				if (provider.isCurrentWindowFunction(wf))
					jbm.setSelected(true);
				bg.add(jbm);
				out.add(jbm);
			}
		}
		return out;
	}

	@Override
	public String displayName() {
		return provider.label();
	}

}
