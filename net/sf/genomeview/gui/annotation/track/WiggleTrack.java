/**
 * %HEADER%
 */
package net.sf.genomeview.gui.annotation.track;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.GeneralPath;
import java.util.List;

import net.sf.genomeview.core.ColorFactory;
import net.sf.genomeview.core.Configuration;
import net.sf.genomeview.core.DisplayType;
import net.sf.genomeview.data.Model;
import net.sf.genomeview.gui.Convert;
import net.sf.genomeview.plugin.IValue;
import net.sf.genomeview.plugin.IValueFeature;
import net.sf.jannot.Entry;

// A SINGLE WIGGLE TRACK CAN CONTAIN MULTIPLE GRAPHS
public class WiggleTrack extends Track {
	private String name;

	public WiggleTrack(String name, Model model, boolean b) {
		super(model, b);
		this.name = name;
	}

	// private void renderValueFeatures(Graphics2D g) {
	// // System.out.println("Render line: " + key);
	// List<IValueFeature> vfList = model.getValueFeatures(model
	// .getSelectedEntry());
	//
	// for (IValueFeature vf : vfList) {
	// /*
	// * Keeps track of the number of pixels that is used vertically to
	// * paint this feature.
	// */
	// int pixelsUsed = 0;
	//
	// if (model.isValueFeatureVisible(vf)) {
	// DisplayType dt = model.getValueFeatureDisplayType(vf);
	//
	// // TODO only paint new color coding tick if is visible
	// int lastX = -1;
	// int lastY = -1;
	// if (dt == DisplayType.LineProfile) {
	// boolean firstValue = true;
	// boolean addLastValue = true;
	// GeneralPath gp = new GeneralPath();
	// for (IValue value : vf) {
	// g.setColor(Color.BLACK);
	// double val = (value.getValue() - vf.min())
	// / (vf.max() - vf.min());
	// // require inverse of value because we paint
	// // top-down.
	// int y = (int) (-val * 50) + framePixelsUsed + 50;
	// int x = Convert.translateGenomeToScreen(value
	// .getStart(), model
	// .getAnnotationLocationVisible(), screenWidth);
	//
	// if (x >= 0 && x <= screenWidth) {
	// if (firstValue) {// fix first value
	// gp.moveTo(lastX, lastY);
	// gp.lineTo(x, y);
	// firstValue = false;
	//
	// } else {
	// // if (x != lastX || y != lastY) {
	// if (x != lastX) {
	// gp.lineTo(x, y);
	// }
	// }
	//
	// } else if (!firstValue && addLastValue) {// fix
	// // ending
	// gp.lineTo(x, y);
	// addLastValue = false;
	// }
	// lastX = x;
	// lastY = y;
	//
	// }
	// // System.out.println();
	//
	// g.draw(gp);
	// // framePixelsUsed += 50;
	// pixelsUsed = 50;
	// } else if (dt == DisplayType.ColorCodingProfile) {// color
	// // coding
	// // scheme
	//
	// for (IValue value : vf) {
	// double val = (value.getValue() - vf.min())
	// / (vf.max() - vf.min());
	// g.setColor(ColorFactory.getColorCoding(val));
	// int x = Convert.translateGenomeToScreen(value
	// .getStart(), model
	// .getAnnotationLocationVisible(), screenWidth);
	// g.fillRect(lastX, framePixelsUsed, x - lastX, 10);
	// lastX = x;
	//
	// }
	// // framePixelsUsed += 10;
	// pixelsUsed = 10;
	// } else {// Barcharts
	// // System.out.println("\tBarchart rendering:
	// // "+vf.getName());
	// if (vf.max() == vf.min()) {
	// for (IValue value : vf) {
	//
	// g.setColor(value.color());
	// int startX = Convert.translateGenomeToScreen(value
	// .getStart(), model
	// .getAnnotationLocationVisible(),
	// screenWidth);
	// int endX = Convert.translateGenomeToScreen(value
	// .getEnd(), model
	// .getAnnotationLocationVisible(),
	// screenWidth);
	// g.fillRect(startX, framePixelsUsed, endX - startX
	// + 1, 10);
	//
	// }
	// framePixelsUsed += 10;
	// pixelsUsed = 10;
	// } else {
	// for (IValue value : vf) {
	//
	// g.setColor(value.color());
	// int startX = Convert.translateGenomeToScreen(value
	// .getStart(), model
	// .getAnnotationLocationVisible(),
	// screenWidth);
	// int endX = Convert.translateGenomeToScreen(value
	// .getEnd(), model
	// .getAnnotationLocationVisible(),
	// screenWidth);
	//
	// /* only positive values */
	// if (vf.min() >= 0) {
	// int bottomY = framePixelsUsed + 30;
	// double rescaledValue = (value.getValue() - vf
	// .min())
	// / (vf.max() - vf.min());
	// assert (rescaledValue >= 0);
	// assert (rescaledValue <= 1);
	// int topY = (int) (bottomY - (rescaledValue * 30));
	// // FIXME scaling is not yet perfect
	// g.fillRect(startX, topY, endX - startX + 1,
	// bottomY - topY);
	// } else { /* also negative values */
	// int bottomY, topY;
	// double rescaledValue = (value.getValue() - vf
	// .min())
	// / (vf.max() - vf.min());
	// rescaledValue *= 2;
	// rescaledValue--;
	// if (rescaledValue >= 0) {
	// bottomY = framePixelsUsed + 15;
	// topY = (int) (bottomY - (rescaledValue * 15));
	// } else {
	// topY = framePixelsUsed + 15;
	// bottomY = (int) (topY - (rescaledValue * 15));
	// }
	// g.fillRect(startX, topY, endX - startX + 1,
	// bottomY - topY);
	// }
	//
	// }
	// pixelsUsed = 30;
	// // framePixelsUsed += 30;
	// }
	// }
	// paintDisplayToggle(g, vf);
	// }
	// framePixelsUsed += pixelsUsed;
	// if (Configuration.getBoolean("showTrackName")) {
	// g.setColor(Color.black);
	// g.drawString(vf.getName(), 10, framePixelsUsed);
	// }
	// }
	// }

	@Override
	public String displayName() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int paint(Graphics g, Entry e, int offset, double width) {
		// TODO Auto-generated method stub
		return 0;
	}
}
