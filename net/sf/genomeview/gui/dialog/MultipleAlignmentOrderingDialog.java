/**
 * %HEADER%
 */
package net.sf.genomeview.gui.dialog;

import java.awt.BorderLayout;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.util.Observable;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.event.MouseInputListener;
import javax.swing.plaf.basic.BasicTableUI;
import javax.swing.table.AbstractTableModel;

import net.sf.genomeview.data.Model;

import com.google.common.collect.BiMap;

/**
 * 
 * @author Thomas Abeel
 * 
 */
public class MultipleAlignmentOrderingDialog extends JDialog {
	public MultipleAlignmentOrderingDialog(Model model,BiMap<String, Integer> ordering) {
		setModal(true);
		final JDialog _self=this;
		final OrderingTableModel listModel = new OrderingTableModel(ordering);
		final JTable table=new JTable(listModel);
		table.setUI(new DragDropRowTableUI(model, ordering.inverse()));
		table.setCellSelectionEnabled(false);
		table.setRowSelectionAllowed(false);
		table.setColumnSelectionAllowed(false);
		table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

		setLayout(new BorderLayout());
		add(table,BorderLayout.CENTER);
		JButton close=new JButton("Close");
		close.addActionListener(new ActionListener(){

			@Override
			public void actionPerformed(ActionEvent e) {
				_self.dispose();
				
			}
			
		});
		add(close,BorderLayout.SOUTH);

	}

	public class DragDropRowTableUI extends BasicTableUI {

		private boolean draggingRow = false;
		private int startDragPoint;
		private int dyOffset;
		private BiMap<Integer, String> ordering;
		private Model model;

		public DragDropRowTableUI(Model model,BiMap<Integer, String>ordering){
			this.ordering=ordering;
			this.model=model;
		}
		
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
				startDragPoint = (int) e.getPoint().getY();
			}

			public void mouseDragged(MouseEvent e) {
				int fromRow = table.getSelectedRow();

				if (fromRow >= 0) {
					draggingRow = true;

					int rowHeight = table.getRowHeight();
					int middleOfSelectedRow = (rowHeight * fromRow) + (rowHeight / 2);

					int toRow = -1;
					int yMousePoint = (int) e.getPoint().getY();

					/* Up */
					if (yMousePoint < (middleOfSelectedRow - rowHeight)) {
						// Move row up
						toRow = fromRow - 1;
//						up = true;
					} /* Down */else if (yMousePoint > (middleOfSelectedRow + rowHeight)) {
						// Move row down
						toRow = fromRow + 1;
//						down = true;
					}

					if (toRow >= 0 && toRow < table.getRowCount()) {
						String a=ordering.get(fromRow);
						String b=ordering.get(toRow);
						ordering.forcePut(fromRow, b);
						ordering.forcePut(toRow, a);
						table.setRowSelectionInterval(toRow, toRow);
						startDragPoint = yMousePoint;
						model.refresh();
					}

					dyOffset = (startDragPoint - yMousePoint) * -1;
					table.repaint();
				}
			}

			public void mouseReleased(MouseEvent e) {
				super.mouseReleased(e);

				draggingRow = false;
				table.repaint();
			}
		}
	}

	class OrderingTableModel extends AbstractTableModel {
		private BiMap<Integer, String> ordering;

		@Override
		public String getColumnName(int column) {
			return "Name";
		}

		

		public OrderingTableModel(BiMap<String, Integer> ordering2) {
			this.ordering=ordering2.inverse();
		}

		public void update(Observable o, Object arg) {

			fireTableDataChanged();

		}

		@Override
		public int getColumnCount() {
			return 1;
		}

		@Override
		public int getRowCount() {
			return ordering.size();
		}

		@Override
		public Object getValueAt(int row, int col) {
			return ordering.get(row);
			

		}
	}

}
