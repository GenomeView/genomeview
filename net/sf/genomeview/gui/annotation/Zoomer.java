package net.sf.genomeview.gui.annotation;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.geom.CubicCurve2D;
import java.text.NumberFormat;
import java.util.Observable;
import java.util.Observer;

import javax.swing.JLabel;

import net.sf.genomeview.data.Model;
import net.sf.genomeview.gui.Convert;
import net.sf.jannot.Location;

public class Zoomer extends JLabel implements Observer, MouseMotionListener, MouseListener {

	private static final long serialVersionUID = -6554830611969076297L;

	private Model model;

	private MouseEvent pressed = null;
	private boolean left = false;
	private boolean right = false;
	private boolean inside = false;

	private Location pressLocation;
	private int closeness = 2;

	@Override
	public void mouseMoved(MouseEvent e) {
		if (e.getY() > visibleRec.y && e.getY() < visibleRec.y + visibleRec.height) {
			if (Math.abs(e.getX() - visibleRec.x) < closeness) {
				model.getParent().setCursor(Cursor.getPredefinedCursor(Cursor.E_RESIZE_CURSOR));
				left = true;
			} else if (Math.abs(e.getX() - (visibleRec.x + visibleRec.width)) < closeness) {
				model.getParent().setCursor(Cursor.getPredefinedCursor(Cursor.E_RESIZE_CURSOR));
				right = true;
			} else {
				model.getParent().setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
				left = false;
				right = false;

			}
		}

		if (!(left || right) && dragRec.contains(e.getX(), e.getY())) {
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

	private Location currentLocation = null;

	@Override
	public void mouseDragged(MouseEvent e) {
		Location full = new Location(1, model.getSelectedEntry().size());
		int pressedX = Convert.translateScreenToGenome(pressed.getX(), full, this.getWidth());
		if (left) {
			int mouseX = Convert.translateScreenToGenome(e.getX(), full, this.getWidth());
			int diff = mouseX - pressedX;
			if (pressLocation.start() + diff > 0)
				currentLocation = (new Location(pressLocation.start() + diff, pressLocation.end()));
		}
		if (right) {
			int mouseX = Convert.translateScreenToGenome(e.getX(), full, this.getWidth());
			int diff = mouseX - pressedX;
			if (pressLocation.end() + diff <= full.end())
				currentLocation = (new Location(pressLocation.start(), pressLocation.end() + diff));
		}
		if (inside) {
			int mouseX = Convert.translateScreenToGenome(e.getX(), full, this.getWidth());
			int diff = mouseX - pressedX;
			if (pressLocation.start() + diff > 0 && pressLocation.end() + diff <= full.end())
				currentLocation = (new Location(pressLocation.start() + diff, pressLocation.end() + diff));
			else if (pressLocation.start() + diff <= 0) {
				currentLocation = (new Location(1, model.getAnnotationLocationVisible().length()));
			} else if (pressLocation.end() + diff > full.end())
				currentLocation = (new Location(full.end() - model.getAnnotationLocationVisible().length() - 1, full.end()));
		}
		repaint();
	}

	public Zoomer(final Model model) {
		this.model = model;
		this.setPreferredSize(new Dimension(250, 40));
		this.setBackground(Color.WHITE);
		this.setOpaque(true);
		model.addObserver(this);
		addMouseMotionListener(this);
		addMouseListener(this);

	}

	private Rectangle visibleRec = null;
	private Rectangle dragRec = null;

	@Override
	public void paintComponent(Graphics g1) {
		super.paintComponent(g1);
		Graphics2D g = (Graphics2D) g1;
		/* Paint tick marks */
		Location r = new Location(1, model.getSelectedEntry().size());
		g.setColor(Color.BLACK);
		g.drawLine(0, 15, g.getClipBounds().width, 15);

		// determine the tickDistance, we aim for 10 ticks on screen.
		int length = r.length();
		int scale = (int) Math.log10(length / 10.0);
		int multiplier = (int) (length / Math.pow(10, scale + 1));
		int tickDistance = (int) (Math.pow(10, scale) * multiplier);
		if (tickDistance == 0)
			tickDistance = 1;
		// paint the ticks
		int currentTick = (r.start() - r.start() % tickDistance) + 1;
		boolean up = true;
		while (currentTick < r.end()) {
			int xpos = Convert.translateGenomeToScreen(currentTick, r, this.getWidth());
			String s = "" + currentTick;

			// if (up) {
			// g.drawLine(xpos, 2, xpos, 28);
			// g.drawString(s, xpos + 2, 14);
			// } else {
			g.drawLine(xpos, 2, xpos, 28);
			g.drawString(s, xpos + 2, 26);
			// }
			// up = !up;

			currentTick += tickDistance;

		}

		
		Location current = model.getAnnotationLocationVisible();
		
		loc(g,current,Color.CYAN,Color.BLUE);
		if (currentLocation != null)
			loc(g,currentLocation,new Color(200,200,200,75),new Color(100,100,100,75));

		String size = format(current);
		g.setColor(Color.BLACK);
		g.drawString(size, this.getSize().width / 2 - 50, this.getSize().height - 1);
	}

	private void loc(Graphics2D g,Location current,Color light,Color dark) {
		Dimension dim = this.getSize();
		Location full = new Location(1, model.getSelectedEntry().size());
		int start = Convert.translateGenomeToScreen(current.start(), full, dim.width);
		int end = Convert.translateGenomeToScreen(current.end(), full, dim.width);
	
		// g.drawLine(0, 7, dim.width, 7);
		visibleRec = new Rectangle(start, 0, end - start + 1, 14);
		dragRec = new Rectangle(start - 15, 0, end - start + 31, 14);

		/* Drag rectangle */
		g.setColor(light);
		g.fillArc(dragRec.x + 5, dragRec.y, 14, 14, 90, 180);
		g.fillArc(dragRec.x + dragRec.width - 18, dragRec.y, 14, 14, -90, 180);
		// g.fillRoundRect(dragRec.x, dragRec.y, dragRec.width, dragRec.height,
		// 3, 3);

		/* Visible rectangle */
		g.setColor(light);
		g.fill(visibleRec);
		g.setColor(dark);
		g.draw(visibleRec);

		CubicCurve2D.Double curveLeft = new CubicCurve2D.Double(0, this.getSize().height - 1,
		// rec.x/2.0,this.getSize().height-1,
				0, this.getSize().height - 10, visibleRec.x, visibleRec.y + visibleRec.height + 10, visibleRec.x, visibleRec.y + visibleRec.height);
		g.draw(curveLeft);

		CubicCurve2D.Double curveRight = new CubicCurve2D.Double(visibleRec.x + visibleRec.width, visibleRec.y + visibleRec.height, visibleRec.x + visibleRec.width, visibleRec.y + visibleRec.height + 10,
		// rec.x+rec.width+(this.getSize().width-rec.x-rec.width)/5.0,this.getSize().height-1,
				this.getSize().width, this.getSize().height - 10, this.getSize().width, this.getSize().height - 1);
		g.draw(curveRight);
		
	}

	private NumberFormat nf = NumberFormat.getInstance();

	private String format(Location current) {
		double size = current.length();
		if (size > 1000000) {
			size /= 1000000;
			nf.setMaximumFractionDigits(1);
			nf.setMinimumFractionDigits(1);
			return nf.format(size) + " Mb";
		}
		if (size > 1000) {
			size /= 1000;
			nf.setMaximumFractionDigits(1);
			nf.setMinimumFractionDigits(1);
			return nf.format(size) + " Kb";
		}
		nf.setMaximumFractionDigits(0);
		nf.setMinimumFractionDigits(0);
		return nf.format(size) + " b";
	}

	@Override
	public void update(Observable o, Object arg) {
		repaint();

	}

	@Override
	public void mouseClicked(MouseEvent e) {
		Location full = new Location(1, model.getSelectedEntry().size());
		int x = Convert.translateScreenToGenome(pressed.getX(), full, this.getWidth());
		model.center(x);
	}

	@Override
	public void mouseEntered(MouseEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public void mouseExited(MouseEvent e) {
		model.getParent().setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));

	}

	@Override
	public void mouseReleased(MouseEvent e) {
		if (currentLocation != null)
			model.setAnnotationLocationVisible(currentLocation);
		currentLocation = null;

	}

}
