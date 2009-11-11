/**
 * %HEADER%
 */
package net.sf.genomeview.gui.information;

import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;

import javax.swing.JComponent;
import javax.swing.event.MouseInputListener;
import javax.swing.plaf.basic.BasicTableUI;

import net.sf.genomeview.data.Model;
/**
 * 
 * @author Thomas Abeel
 *
 */
public class DragDropRowTableUI extends BasicTableUI {
	
	public Model model;
	public DragDropRowTableUI(Model model){
		this.model=model;
	}
	private boolean draggingRow = false;
	private int startDragPoint;
	private int dyOffset;
	
    protected MouseInputListener createMouseInputListener() {
        return new DragDropRowMouseInputHandler();
    }
    
    public void paint(Graphics g, JComponent c) {
    	super.paint(g, c);
    	
    	if (draggingRow) {
    		g.setColor(table.getParent().getBackground());
			Rectangle cellRect = table.getCellRect(table.getSelectedRow(), 0, false);
    		g.copyArea(cellRect.x, cellRect.y, table.getWidth(), table.getRowHeight(), cellRect.x, dyOffset);
    		
    		if (dyOffset < 0) {
    			g.fillRect(cellRect.x, cellRect.y + (table.getRowHeight() + dyOffset), table.getWidth(), (dyOffset * -1));
    		} else {
    			g.fillRect(cellRect.x, cellRect.y, table.getWidth(), dyOffset);
    		}
    	}
    }
    
    class DragDropRowMouseInputHandler extends MouseInputHandler {
    	
        public void mousePressed(MouseEvent e) {
        	super.mousePressed(e);
        	startDragPoint = (int)e.getPoint().getY();
        }
        
        public void mouseDragged(MouseEvent e) {
        	int fromRow = table.getSelectedRow();
        	
        	if (fromRow >= 0) {
	        	draggingRow = true;
	        	        	
	        	int rowHeight = table.getRowHeight();
	        	int middleOfSelectedRow = (rowHeight * fromRow) + (rowHeight / 2);
	        	
	        	int toRow = -1;
	        	int yMousePoint = (int)e.getPoint().getY();
	        	
	        	boolean up=false;
	        	boolean down=false;
	        	/* Up*/
	        	if (yMousePoint < (middleOfSelectedRow - rowHeight)) {
	        		// Move row up
	        		toRow = fromRow - 1;
	        		up=true;
	        	} /*Down*/ else if (yMousePoint > (middleOfSelectedRow + rowHeight)) {
	        		// Move row down
	        		toRow = fromRow + 1;
	        		down=true;
	        	}
	        	
	        	if (toRow >= 0 && toRow < table.getRowCount()) {
	        		
	        		
	        		
	        		if(up)
	        			model.getTrackList().up(fromRow);
	        		if(down)
	        			model.getTrackList().down(fromRow);
		    		table.setRowSelectionInterval(toRow, toRow);
		    		startDragPoint = yMousePoint;
	        	}
	        	
	        	dyOffset = (startDragPoint - yMousePoint) * -1;
	        	table.repaint();
        	}
        }
        
        public void mouseReleased(MouseEvent e){
        	super.mouseReleased(e);
        	
        	draggingRow = false;
        	table.repaint();
        }
    }
}