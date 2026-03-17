/**
 * %HEADER%
 * 
 * Copyright 2004, Generation5. All Rights Reserved.
 */
package net.sf.genomeview.core;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * A gradient going through given colour points.
 */
public class ColorGradient1 {

	public static ColorGradient1 DEFAULT = new ColorGradient1(
			Arrays.asList(Color.red, Color.yellow, Color.green, Color.blue));

	private List<Color> colors = new ArrayList<>();

	/**
	 * 
	 * @param cols a list of colors through which the gradient will go.
	 */
	public ColorGradient1(List<Color> cols) {
		if (cols == null || cols.size() < 2)
			throw new IllegalArgumentException("At least 2 colors required ");
		this.colors.addAll(cols);
	}

	/**
	 * 
	 * @param x the position in [0,1].
	 * @return the interpolated color at that position. Color is interpolated
	 *         linearly in r,g,b.
	 */
	public Color get(float x) {
		if (x < 0)
			x = 0;
		else if (x > 1)
			x = 1;
		float indexfraction = x * (colors.size() - 1);
		// 2.3 means at 0.3 between color 2 and 3
		int i = (int) indexfraction;
		float frac = indexfraction - i;
		if (frac == 0)
			return colors.get(i);
		return interpolate(colors.get(i), colors.get(i + 1), frac);
	}

	private Color interpolate(Color cA, Color cB, float frac) {
		float a = frac;
		float b = 1f - a;
		a = a / 255;
		b = b / 255;
		return new Color(a * cA.getRed() + b * cB.getRed(),
				a * cA.getGreen() + b * cB.getGreen(),
				a * cA.getBlue() + b * cB.getBlue());
	}

}
