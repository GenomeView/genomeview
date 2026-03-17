/**
 * %HEADER%
 * 
 * Copyright 2004, Generation5. All Rights Reserved.
 */
package net.sf.genomeview.core;

import static org.junit.Assert.assertEquals;

import java.awt.Color;
import java.util.Arrays;

import org.junit.Test;

/**
 * A gradient going through given colour points.
 */
public class ColorGradient1Test {

	@Test
	public void testSmoke() {
		ColorGradient1 a = ColorGradient1.DEFAULT;
	}

	@Test
	public void blackwhite() {
		ColorGradient1 bw = new ColorGradient1(
				Arrays.asList(Color.black, Color.white));
		for (float r = 0; r < 1; r += 0.01) {
			Color color = bw.get(r);
			assertEquals(r, color.getRed() / 255f, .002);
			assertEquals(r, color.getGreen() / 255f, .002);
			assertEquals(r, color.getBlue() / 255f, .002);
		}
	}

}
