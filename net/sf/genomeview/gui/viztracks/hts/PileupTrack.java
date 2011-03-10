/**
 * %HEADER%
 */
package net.sf.genomeview.gui.viztracks.hts;

import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.AbstractAction;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JViewport;

import net.sf.genomeview.data.Model;
import net.sf.genomeview.data.provider.PileProvider;
import net.sf.genomeview.gui.viztracks.Track;
import net.sf.jannot.Location;

/**
 * 
 * @author Thomas Abeel
 * 
 */
public class PileupTrack extends Track {

	private NumberFormat nf = NumberFormat.getInstance(Locale.US);
	private PileProvider provider;

	public PileupTrack(PileProvider provider, Model model) {
		super(null, model, true, false);
		ptm = new PileupTrackModel(model);
		tooltip = new PileupTooltip(ptm);
		this.provider = provider;
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
	public int paintTrack(Graphics2D g, int yOffset, double screenWidth, JViewport view) {

		ptm.setScreenWidth(screenWidth);

		Location visible = model.getAnnotationLocationVisible();
		/* Status messages for data queuing an retrieval */
//		Iterable<Status> status = provider.getStatus(visible.start, visible.end);

		/* Only retrieve data when location changed */
		if (ptm.lastQuery == null || !ptm.lastQuery.equals(model.getAnnotationLocationVisible())) {
			/* The actual data */
//			Iterable<Pile> piles = provider.get(visible.start, visible.end);

			if (model.getAnnotationLocationVisible().length() < 32000) {
		//		System.out.println("Track: "+this+"\t"+provider);
				ptm.setVizBuffer(new BarChartBuffer(visible, provider,ptm));
			} else
				ptm.setVizBuffer(new LineChartBuffer(visible, provider,ptm));

			ptm.lastQuery = model.getAnnotationLocationVisible();
		}

		/* Do the actual painting */
		int graphLineHeigh = ptm.getVizBuffer().draw(g, yOffset, screenWidth);
		return graphLineHeigh;

	}

	

	/* User settable maximum value for charts, use negative for unlimited */

	@Override
	public List<JMenuItem> getMenuItems() {
		ArrayList<JMenuItem> out = new ArrayList<JMenuItem>();

		/* Use global settings */
		final JCheckBoxMenuItem itemGlobal = new JCheckBoxMenuItem();
		itemGlobal.setSelected(ptm.isGlobalSettings());
		itemGlobal.setAction(new AbstractAction("Use global settings for scaling") {
			@Override
			public void actionPerformed(ActionEvent e) {
				ptm.setGlobalSettings(itemGlobal.isSelected());

			}

		});
		out.add(itemGlobal);

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
		return out;
	}

	@Override
	public String displayName() {
		return "Pileup: " + super.dataKey;
	}
}


