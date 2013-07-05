/**
 * %HEADER%
 */
package net.sf.genomeview.gui.components;

import javax.swing.*;

/**
 * 
 * JSlider class for double values
 * 
 * @author Andreas Gohr
 * @author Frank Schubert
 * @author Thomas Abeel
 * 
 * 
 */
public class DoubleJSlider extends JSlider {

	private static final long serialVersionUID = 2458665505148606599L;
	private final double step;

	/**
	 * Constructor - initializes with 0.0,100.0,50.0
	 */
	public DoubleJSlider() {
		super();
		setDoubleMinimum(0.0);
		setDoubleMaximum(1.0);
		setDoubleValue(0.0);
		step = 0.01;
	}

	/**
	 * Constructor
	 */
	public DoubleJSlider(double min, double max, double val, double step) {
		super();
		this.step = step;
		setDoubleMinimum(min);
		setDoubleMaximum(max);
		setDoubleValue(val);

	}

	/**
	 * returns Maximum in double precision
	 */
	public double getDoubleMaximum() {
		return (getMaximum() * step);
	}

	/**
	 * returns Minimum in double precision
	 */
	public double getDoubleMinimum() {
		return (getMinimum() * step);
	}

	/**
	 * returns Value in double precision
	 */
	public double getDoubleValue() {
		return (getValue() * step);
	}

	/**
	 * sets Maximum in double precision
	 */
	public void setDoubleMaximum(double max) {
		setMaximum((int) (max / step));
	}

	/**
	 * sets Minimum in double precision
	 */
	public void setDoubleMinimum(double min) {
		setMinimum((int) (min / step));
	}

	/**
	 * sets Value in double precision
	 */
	public void setDoubleValue(double val) {
		setValue((int) (val / step));
		setToolTipText(Double.toString(val));
	}

}
