/**
 * %HEADER%
 */
package net.sf.genomeview.gui.viztracks.variation;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import javax.swing.JViewport;

import net.sf.genomeview.core.Colors;
import net.sf.genomeview.core.Configuration;
import net.sf.genomeview.data.Model;
import net.sf.genomeview.gui.Convert;
import net.sf.genomeview.gui.components.CollisionMap;
import net.sf.genomeview.gui.viztracks.GeneEvidenceLabel.FillMode;
import net.sf.genomeview.gui.viztracks.Track;
import net.sf.genomeview.gui.viztracks.TrackCommunicationModel;
import net.sf.genomeview.gui.viztracks.TrackConfig;
import net.sf.jannot.Data;
import net.sf.jannot.DataKey;
import net.sf.jannot.Feature;
import net.sf.jannot.Location;
import net.sf.jannot.Type;
import net.sf.jannot.tabix.VCFWrapper;
import net.sf.jannot.variation.Allele;
import net.sf.jannot.variation.Variation;

/**
 * 
 * @author Thomas Abeel
 * 
 */
public class VariationTrack extends Track {

	static class VariationTrackConfig extends TrackConfig {

		protected VariationTrackConfig(Model model, DataKey dataKey) {
			super(model, dataKey);
		}

	}

	private VariationTrackConfig vtc;

	public VariationTrack(Model model, Type key) {
		super(key, model, true, new VariationTrackConfig(model, key));
		vtc = (VariationTrackConfig) config;
	}

	@Override
	protected int paintTrack(Graphics2D g, int yOffset, double width, JViewport view, TrackCommunicationModel tcm) {
		Location visible = model.vlm.getAnnotationLocationVisible();

		Iterable<Variation> data = (Iterable<Variation>) entry.get(dataKey).get(visible.start, visible.end);
		g.setColor(Color.BLUE);
		for (Variation v : data) {
			int coordinate = v.start();
			int x1 = Convert.translateGenomeToScreen(coordinate, model.vlm.getAnnotationLocationVisible(), width);
			int w = Convert.translateGenomeToScreen(coordinate + 1, model.vlm.getAnnotationLocationVisible(), width) - x1;
			if(w<1)
				w=1;
			for (Allele a : v.alleles()) {
				//float freq = a.alternativeFrequency();
				if(a.reference().length()>a.alternative().length())
					g.setColor(Color.RED);
				else if(a.reference().length()<a.alternative().length())
					g.setColor(Color.BLACK);
				else
					g.setColor(Color.CYAN);
					g.fillRect(x1, yOffset, w, (int)(40/*freq*/));
			}
		}

		return 40;

	}

}
