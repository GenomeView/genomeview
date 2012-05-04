/**
 * %HEADER%
 */
package net.sf.genomeview.gui.information;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.util.logging.Logger;

import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.ToolTipManager;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumnModel;

import net.sf.genomeview.data.Model;
import net.sf.genomeview.data.TrackList;
/**
 * 
 * @author Thomas Abeel
 *
 */
public class TrackTable extends JTable {
	private Logger log = Logger.getLogger(TrackTable.class.getCanonicalName());
    private static final long serialVersionUID = 2680194422003453639L;

    public TrackTable(final Model model) {
        super(new TrackTableModel(model));
        final TrackTableModel listModel = (TrackTableModel) this.getModel();
        //getTableHeader().addMouseMotionListener(new ColumnHeaderToolTips(listModel));
        //getTableHeader().setReorderingAllowed(false);
        setTableHeader(null);
        setUI(new DragDropRowTableUI(model));
        /* Set column widths */
//        setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
//        getColumnModel().getColumn(0).setPreferredWidth(150);
        getColumnModel().getColumn(0).setPreferredWidth(20);
        getColumnModel().getColumn(0).setMaxWidth(50);
        getColumnModel().getColumn(2).setPreferredWidth(20);
        getColumnModel().getColumn(2).setMaxWidth(50);

        ToolTipManager.sharedInstance().setInitialDelay(0);
        setCellSelectionEnabled(false);
        setRowSelectionAllowed(false);
        setColumnSelectionAllowed(false);
        setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        addMouseListener(new MouseAdapter() {
            public void mouseReleased(MouseEvent e) {
            	int column=columnAtPoint(e.getPoint());
            	int row=rowAtPoint(e.getPoint());
            	listModel.mouse(column,row);
                
            }
        });
    }

    class ColumnHeaderToolTips extends MouseMotionAdapter {
        private int index = -1;

        private TrackTableModel listModel;

        public ColumnHeaderToolTips(TrackTableModel listModel) {
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
