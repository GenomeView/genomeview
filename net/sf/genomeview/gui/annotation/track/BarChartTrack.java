/**
 * %HEADER%
 */
package net.sf.genomeview.gui.annotation.track;

import java.awt.Color;
import java.awt.Rectangle;
import java.util.List;

import net.sf.genomeview.core.Configuration;
import net.sf.genomeview.gui.Convert;
import net.sf.jannot.Feature;

/**
 * This class will become a bar chart track. This code was extracted from the GeneEvidenceLabel.
 * @author tabeel
 *
 */
public class BarChartTrack {
//case BarchartProfile:
//	int line = 75;
//	/* Determine maximum score */
//	List<Feature> allfeatures = model.getSelectedEntry().annotation
//			.getByType(type);
//	double maxScore = 0;
//	for (Feature rf : allfeatures) {
//		if (rf.getScore() > maxScore)
//			maxScore = rf.getScore();
//
//	}
//	/* Paint all visible features */
//	for (Feature rf : types) {
//		assert (rf.location().size() == 1);
//		// the line on which to paint this feature
//		int thisLine = 0;
//		double heightScale = 1;
//
//		Color c = Configuration.getColor("TYPE_" + rf.type());
//
//		double score = rf.getScore() / maxScore;
//		heightScale = score;
//		c = Color.green;
//		String background = rf.singleQualifierValue("background");
//		if (background != null) {
//			double backgroundScore = Double.parseDouble(background);
//			if (score > backgroundScore)
//				c = Color.GREEN;
//			else
//				c = Color.red;
//		}
//
//		g.setColor(c);
//		int x1 = Convert.translateGenomeToScreen(rf.start(), model
//				.getAnnotationLocationVisible(), screenWidth);
//		int x2 = Convert.translateGenomeToScreen(rf.end() + 1,
//				model.getAnnotationLocationVisible(), screenWidth);
//		if (x2 == x1)
//			x2++;
//		if (x2 > 0) {
//			assert (rf.location().size() == 1);
//			super.collisionMap.addLocation(new Rectangle(x1,
//					thisLine * line + yOffset, x2 - x1, line), rf
//					.location().first());
//			g
//					.fillRect(
//							x1,
//							(int) (thisLine * line + yOffset + (1 - heightScale)
//									* line), x2 - x1,
//							(int) (line * heightScale));
//			// Set<Feature> selected = model.getFeatureSelection();
//			// if (selected != null && selected.contains(rf)) {
//			if (model.getLocationSelection().contains(
//					rf.location().first())) {
//				g.setColor(Color.BLACK);
//				g
//						.drawRect(x1, (int) (thisLine * line
//								+ yOffset + (1 - heightScale)
//								* line), x2 - x1,
//								(int) (line * heightScale));
//
//			}
//		}
//	}
//	if (Configuration.getBoolean("showTrackName")) {
//		g.setColor(Color.black);
//		g.drawString(type.toString(), 10, yOffset + line);
//	}
//	yOffset += line;
//	break;
}
