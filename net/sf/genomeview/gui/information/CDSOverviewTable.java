/**
 * %HEADER%
 */
package net.sf.genomeview.gui.information;

import java.awt.Rectangle;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.util.Observable;
import java.util.Observer;
import java.util.SortedSet;

import javax.swing.JTable;
import javax.swing.JViewport;
import javax.swing.ToolTipManager;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumnModel;

import net.sf.genomeview.data.Model;
import net.sf.jannot.Feature;
import net.sf.jannot.Location;
import net.sf.jannot.Type;

/**
 * Provides an overview of all gene structures.
 * 
 * @author Thomas Abeel
 * 
 */
public class CDSOverviewTable extends JTable implements Observer {

    private static final long serialVersionUID = 8956245030328303086L;

    final CDSTrackListModel listModel;

    public CDSOverviewTable(final Model model) {
        super(new CDSTrackListModel(model));
        model.addObserver(this);
        listModel = (CDSTrackListModel) this.getModel();

        // setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        // getColumnModel().getColumn(0).setPreferredWidth(200);
        for (int i = 1; i < this.getColumnCount(); i++) {
            getColumnModel().getColumn(i).setPreferredWidth(30);
            getColumnModel().getColumn(i).setMaxWidth(50);
        }
        getTableHeader().addMouseMotionListener(new ColumnHeaderToolTips(listModel));
        ToolTipManager.sharedInstance().setInitialDelay(0);

        // setRowSelectionAllowed(true);
        // setColumnSelectionAllowed(false);
        // setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int column = getSelectedColumn();

                if (e.getClickCount() > 0) {
                    Feature rf = listModel.getFeature(getSelectedRow());

                    switch (column) {
                    case 5:

                        model.setFeatureVisible(rf, !model.isFeatureVisible(rf));
                        break;
                    default:
                        model.setLocationSelection(rf);
                        break;
                    }

                    if (e.getClickCount() > 1) {
                        int min = rf.start();
                        int max = rf.end();
                        double border = 0.05 * (max - min);
                        model.setAnnotationLocationVisible(new Location((int) (min - border), (int) (max + border)));
                    }
                }
                /* Keep selection */
                SortedSet<Feature> fs = model.getFeatureSelection();
                int row = listModel.getRow(fs.first(), Type.get("CDS"));
                getSelectionModel().setSelectionInterval(row, row);

            }
        });
    }

    class ColumnHeaderToolTips extends MouseMotionAdapter {
        private int index = -1;

        private CDSTrackListModel listModel;

        public ColumnHeaderToolTips(CDSTrackListModel listModel) {
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

    @Override
    public void update(Observable o, Object arg) {
        SortedSet<Feature> fs = ((Model) o).getFeatureSelection();

        if (fs.size() == 1) {
            // FIXME for multiple structure types
            // if
            // (Configuration.getTypeSet("geneStructures").contains(fs.first().type()))
            // {
            if (fs.first().type() == Type.get("CDS")) {
                int row = listModel.getRow(fs.first(), Type.get("CDS"));
                getSelectionModel().setSelectionInterval(row, row);
                if (!(getParent() instanceof JViewport)) {
                    return;
                }
                JViewport viewport = (JViewport) getParent();

                // This rectangle is relative to the table where the
                // northwest corner of cell (0,0) is always (0,0).
                Rectangle rect = getCellRect(row, 0, true);

                // The location of the view relative to the table
                Rectangle viewRect = viewport.getViewRect();

                int topVisible = viewport.getViewRect().y;
                int bottomVisible = viewport.getViewRect().height + topVisible;

                /* When the cell is visible, don't do anything */
                if (rect.y > topVisible && rect.y + rect.height < bottomVisible) {
                    return;
                }
                // Translate the cell location so that it is relative
                // to the view, assuming the northwest corner of the
                // view is (0,0).
                rect.setLocation(rect.x - viewRect.x, rect.y - viewRect.y);

                // Calculate location of rect if it were at the center of view
                int centerX = (viewRect.width - rect.width) / 2;
                int centerY = (viewRect.height - rect.height) / 2;

                // Fake the location of the cell so that scrollRectToVisible
                // will move the cell to the center
                if (rect.x < centerX) {
                    centerX = -centerX;
                }
                if (rect.y < centerY) {
                    centerY = -centerY;
                }
                rect.translate(centerX, centerY);

                // Scroll the area into view.
                viewport.scrollRectToVisible(rect);
            }
        }

    }
}
