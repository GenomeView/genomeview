/**
 * %HEADER%
 */
package net.sf.genomeview.gui;

import java.awt.event.MouseEvent;

public class Mouse {
	public static boolean modifier(MouseEvent e) {
		return e.isAltDown() || e.isShiftDown() || e.isControlDown();
	}

	public static  boolean button1(MouseEvent e) {
		return e.getButton() == MouseEvent.BUTTON1;
	}

	public static  boolean button2(MouseEvent e) {
		return e.getButton() == MouseEvent.BUTTON2;
	}

	public static  boolean button3(MouseEvent e) {
		return e.getButton() == MouseEvent.BUTTON3;
	}
}
