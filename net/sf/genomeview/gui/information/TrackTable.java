/**
 * %HEADER%
 */
package net.sf.genomeview.gui.information;

import java.awt.Component;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.util.logging.Logger;

import javax.swing.JComponent;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.ToolTipManager;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumnModel;

import net.sf.genomeview.data.Model;
import net.sf.genomeview.data.TrackList;
import net.sf.genomeview.gui.viztracks.Track;

/**
 * 
 * @author Thomas Abeel
 * 
 */
public class TrackTable extends JTable {
	private Logger log = Logger.getLogger(TrackTable.class.getCanonicalName());
	private Model model;
	private static final long serialVersionUID = 2680194422003453639L;

	public TrackTable(final Model model) {
		super(new TrackTableModel(model));
		this.model=model;
		setSelectionModel(new TrackSelectionModel());
		// final TrackTableModel listModel = ;
		// getTableHeader().addMouseMotionListener(new
		// ColumnHeaderToolTips(listModel));
		// getTableHeader().setReorderingAllowed(false);
		setTableHeader(null);
		setUI(new DragDropRowTableUI(model));
		/* Set column widths */
		// setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		// getColumnModel().getColumn(0).setPreferredWidth(150);
		getColumnModel().getColumn(0).setPreferredWidth(20);
		getColumnModel().getColumn(0).setMaxWidth(50);
		getColumnModel().getColumn(2).setPreferredWidth(20);
		getColumnModel().getColumn(2).setMaxWidth(50);

		ToolTipManager.sharedInstance().setInitialDelay(0);
		// setCellSelectionEnabled(false);
		// setRowSelectionAllowed(true);
		// setColumnSelectionAllowed(false);
		//
		// setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

	}

	public Component prepareRenderer(TableCellRenderer renderer, int rowIndex, int vColIndex) {
		Component c = super.prepareRenderer(renderer, rowIndex, vColIndex);
		if (c instanceof JComponent) {
			JComponent jc = (JComponent) c;
			String longDisplay= model.getTrackList().get(rowIndex).config().displayName();
			jc.setToolTipText(longDisplay);
		}
		return c;
	}

	// class ColumnHeaderToolTips extends MouseMotionAdapter {
	// private int index = -1;
	//
	// private TrackTableModel listModel;
	//
	// public ColumnHeaderToolTips(TrackTableModel listModel) {
	// this.listModel = listModel;
	//
	// }
	//
	// public void mouseMoved(MouseEvent evt) {
	//
	// JTableHeader header = (JTableHeader) evt.getSource();
	// JTable table = header.getTable();
	// TableColumnModel colModel = table.getColumnModel();
	// int vColIndex = colModel.getColumnIndexAtX(evt.getX());
	// if (vColIndex != this.index && vColIndex >= 0) {
	// header.setToolTipText(listModel.getColumnName(vColIndex));
	// }
	// this.index = vColIndex;
	//
	// }
	// }

}
