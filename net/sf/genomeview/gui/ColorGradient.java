/**
 * %HEADER%
 * 
 * Copyright 2004, Generation5. All Rights Reserved.
 */
package net.sf.genomeview.gui;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;

/**
 * This class provides a method of creating a smooth gradient evenly distributed
 * colour points. To create a gradient, add as many colors as you like and call
 * <code>createGradient</code>. <code>getColour</code> or
 * <code>getGradient</code> can be used to retrieve the resultant colors.
 * 
 * @author James Matthews
 * @author Thomas Abeel
 */
public class ColorGradient {

	private List<Color> crGradientPoints = new ArrayList<Color>();
	private List<Color> crGradient;
	
	/**
	 * Add a color to the gradient list. 
	 * 
	 * @param gradientColour
	 *            the next color in the gradient.
	 */
	public void addPoint(Color gradientColour) {
		crGradientPoints.add(gradientColour);
	}

	public static ColorGradient getSimple(Color start,Color end){
		ColorGradient out=new ColorGradient();
		out.addPoint(start);
		out.addPoint(end);
		out.createGradient(512);
		return out;
	}

	/**
	 * Calculate the RGB deltas between two different color values and over a
	 * given number of steps. <code>createGradient</code> uses this function
	 * to connect the gradient points together.
	 * 
	 * @param start
	 *            the starting color.
	 * @param end
	 *            the ending color.
	 * @param steps
	 *            the number of steps required to get from <code>start</code> to
	 *            <code>end</code.
	 * @return a <code>double[3]</code> array returning the red, green and blue
	 *         delta values.
	 */
	private double[] getRGBDeltas(Color start, Color end, int steps) {
		double[] delta = new double[3];

		delta[0] = (end.getRed() - start.getRed()) / (double) steps;
		delta[1] = (end.getGreen() - start.getGreen()) / (double) steps;
		delta[2] = (end.getBlue() - start.getBlue()) / (double) steps;

		return delta;
	}

	/**
	 * Create the gradient using the current gradient list. This method defaults
	 * to using 256 steps.
	 */
	public void createGradient() {
		createGradient(256);
	}

	/**
	 * Create the gradient using the current gradient list.
	 * 
	 * @param numSteps
	 *            the total number of steps to take from the first color to the last.
	 */
	public void createGradient(int numSteps) {
		int steps = numSteps / (crGradientPoints.size() - 1);
		double[] crColours = new double[3];
		crColours[0] = crGradientPoints.get(0).getRed();
		crColours[1] = crGradientPoints.get(0).getGreen();
		crColours[2] = crGradientPoints.get(0).getBlue();

		crGradient = new ArrayList<Color>();//new Color[numSteps];

		// For each of the gradient points
		for (int i = 0; i < crGradientPoints.size() - 1; i++) {
			double[] delta = getRGBDeltas(crGradientPoints.get(i),
					crGradientPoints.get(i + 1), steps);

			for (int s = 0; s < steps; s++) {
				crColours[0] += delta[0];
				crColours[1] += delta[1];
				crColours[2] += delta[2];
				if (crColours[0] > 255)
					crColours[0] = 255;
				if (crColours[1] > 255)
					crColours[1] = 255;
				if (crColours[2] > 255)
					crColours[2] = 255;
				crGradient.add(new Color((int) Math.round(crColours[0]),
						(int) Math.round(crColours[1]), (int) Math
								.round(crColours[2])));
				
			}
		}
	}

	/**
	 * Return the <i>ith</i> colour in the gradient.
	 * 
	 * @param i
	 *            the index of gradient array to return.
	 * @return the <code>Color</code> value in the gradient.
	 */
	public Color getColor(int i) {
		return crGradient.get(i);
	}

	/**
	 * Return the entire gradient as a Color list.
	 * 
	 * @return list containing the gradient colors.
	 */
	public List<Color> getGradient() {
		return crGradient;
	}

	/**
	 * Test function.
	 * 
	 * @param args
	 *            command-line arguments (ignored).
	 */
	public static void main(String args[]) {
		ColorGradient gradient = new ColorGradient();

		gradient.addPoint(new Color(64, 0, 128));
		gradient.addPoint(new Color(255, 0, 128));
		gradient.addPoint(new Color(255, 255, 128));
		gradient.addPoint(Color.WHITE);
		gradient.createGradient(512);

		BufferedImage buffer = new BufferedImage(512, 100, 1);
		Graphics graphics = buffer.createGraphics();

		for (int i = 0; i < 512; i += 2) {
			graphics.setColor(gradient.getColor(i / 2));
			graphics.fillRect(i, 0, 2, 100);
		}

		java.awt.image.RenderedImage rendered = buffer;
		try {
			File file = new File("gradtest.png");
			ImageIO.write(rendered, "png", file);
		} catch (IOException e) {
			System.err.println("An error occurred.");
		}
	}

}
