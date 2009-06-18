package net.sf.genomeview.gui.annotation;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionAdapter;
import java.awt.event.MouseMotionListener;
import java.util.Observable;
import java.util.Observer;

import javax.swing.JLabel;

import net.sf.genomeview.data.Model;
import net.sf.genomeview.gui.Convert;
import net.sf.jannot.Location;

public class Zoomer extends JLabel implements Observer, MouseMotionListener, MouseListener {

	private Model model;

	private MouseEvent pressed = null;
	private boolean left = false;
	private boolean right = false;
	private boolean inside = false;

	private Location pressLocation;
	private int closeness = 5;

	@Override
	public void mouseMoved(MouseEvent e) {
		if (e.getY() > rec.y && e.getY() < rec.y + rec.height) {
			if (Math.abs(e.getX() - rec.x) < closeness) {
				model.getParent().setCursor(Cursor.getPredefinedCursor(Cursor.E_RESIZE_CURSOR));
				left = true;
			} else if (Math.abs(e.getX() - (rec.x + rec.width)) < closeness) {
				model.getParent().setCursor(Cursor.getPredefinedCursor(Cursor.E_RESIZE_CURSOR));
				right = true;
			} else {
				model.getParent().setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
				left = false;
				right = false;

			}
		}

		if (!(left || right) && rec.contains(e.getX(), e.getY())) {
			inside = true;
		} else {
			inside = false;
		}
	}

	@Override
	public void mousePressed(MouseEvent e) {
		pressed = e;
		pressLocation = model.getAnnotationLocationVisible();
	}

	@Override
	public void mouseDragged(MouseEvent e) {
		Location full = new Location(1, model.getSelectedEntry().sequence.size());
		int pressedX = Convert.translateScreenToGenome(pressed.getX(), full, this.getWidth());
		if (left) {
			int mouseX = Convert.translateScreenToGenome(e.getX(), full, this.getWidth());
			int diff = mouseX - pressedX;
			model.setAnnotationLocationVisible(new Location(pressLocation.start() + diff, pressLocation.end()));
		}
		if (right) {
			int mouseX = Convert.translateScreenToGenome(e.getX(), full, this.getWidth());
			int diff = mouseX - pressedX;
			model.setAnnotationLocationVisible(new Location(pressLocation.start(), pressLocation.end() + diff));
		}
		if (inside) {
			int mouseX = Convert.translateScreenToGenome(e.getX(), full, this.getWidth());
			int diff = mouseX - pressedX;
			model.setAnnotationLocationVisible(new Location(pressLocation.start() + diff, pressLocation.end() + diff));
		}
	}

	public Zoomer(final Model model) {
		this.model = model;
		model.addObserver(this);
		addMouseMotionListener(this);
		addMouseListener(this);

	}

	private Rectangle rec = null;

	@Override
	public void paintComponent(Graphics g1) {
		super.paintComponent(g1);
		Graphics2D g = (Graphics2D) g1;
		Dimension dim = this.getSize();
		Location full = new Location(1, model.getSelectedEntry().sequence.size());
		Location current = model.getAnnotationLocationVisible();
		int start = Convert.translateGenomeToScreen(current.start(), full, dim.width);
		int end = Convert.translateGenomeToScreen(current.end(), full, dim.width);
		g.setColor(Color.BLACK);
		g.drawLine(0, dim.height / 2, dim.width, dim.height / 2);
		rec = new Rectangle(start, dim.height / 2 - 10, end - start + 1, 20);
		g.setColor(Color.BLUE);
		g.fill(rec);
		g.setColor(Color.blue.darker());
		g.draw(rec);
	}

	@Override
	public void update(Observable o, Object arg) {
		repaint();

	}

	@Override
	public void mouseClicked(MouseEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public void mouseEntered(MouseEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public void mouseExited(MouseEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public void mouseReleased(MouseEvent e) {
		// TODO Auto-generated method stub

	}

}
