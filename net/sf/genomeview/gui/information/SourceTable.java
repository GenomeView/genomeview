/**
 * %HEADER%
 */
package net.sf.genomeview.gui.information;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;

import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.ToolTipManager;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumnModel;

import net.sf.genomeview.data.Model;
import net.sf.jannot.source.DataSource;

public class SourceTable extends JTable {
	private static final long serialVersionUID = 2680194422003453639L;


	public SourceTable(final Model model) {
		super(new SourceTableModel(model));
	
		final SourceTableModel listModel = (SourceTableModel) this.getModel();
		getTableHeader().addMouseMotionListener(new ColumnHeaderToolTips(listModel));
		getTableHeader().setReorderingAllowed(false);

		/* Set column widths */
		setAutoResizeMode(JTable.AUTO_RESIZE_SUBSEQUENT_COLUMNS);
		// getColumnModel().getColumn(0).setPreferredWidth(150);
		for (int i = 1; i < this.getColumnCount(); i++) {
			getColumnModel().getColumn(i).setMaxWidth(50);
			getColumnModel().getColumn(i).setPreferredWidth(20);
		}
		ToolTipManager.sharedInstance().setInitialDelay(0);
		setCellSelectionEnabled(false);
		setRowSelectionAllowed(false);
		setColumnSelectionAllowed(false);
		setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		addMouseListener(new MouseAdapter() {
			public void mouseReleased(MouseEvent e) {
				if(e.getButton()==1){
					int column = getSelectedColumn();
					int row = getSelectedRow();
					DataSource type = listModel.sources().get(row);
					if (column == 1) {
						System.out.println("Setting visibility " + type + " " + !model.isSourceVisible(type));
					model.setSourceVisibility(type, !model.isSourceVisible(type));
					}
				}

			}
		});
	}

	public String getToolTipText(MouseEvent e) {
		String tip = null;
		java.awt.Point p = e.getPoint();
		int rowIndex = rowAtPoint(p);
		int colIndex = columnAtPoint(p);
		int realColumnIndex = convertColumnIndexToModel(colIndex);

		if (realColumnIndex == 0) { // Sport column
			SourceTableModel model = (SourceTableModel) this.getModel();
			String source = model.sources().get(rowIndex).toString();

			return source;

		} else { 
			tip = super.getToolTipText(e);
		}
		return tip;
	}

	class ColumnHeaderToolTips extends MouseMotionAdapter {
		private int index = -1;

		private SourceTableModel listModel;

		public ColumnHeaderToolTips(SourceTableModel listModel) {
			this.listModel = listModel;

		}

		public void mouseMoved(MouseEvent evt) {

			JTableHeader header = (JTableHeader) evt.getSource();
			JTable table = header.getTable();
			TableColumnModel colModel = table.getColumnModel();
			int vColIndex = colModel.getColumnIndexAtX(evt.getX());
			if (vColIndex != this.index && vColIndex >= 0) {
				header.setToolTipText(listModel.getColumnName(vColIndex));
			}
			this.index = vColIndex;

		}
	}
}
