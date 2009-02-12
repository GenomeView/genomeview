/**
 * %HEADER%
 */
package net.sf.genomeview.gui.information;

import java.awt.Color;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;

import javax.swing.JColorChooser;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.ToolTipManager;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumnModel;

import net.sf.genomeview.core.Configuration;
import net.sf.genomeview.core.DisplayType;
import net.sf.genomeview.data.Model;
import net.sf.jannot.Type;

public class FeatureTrackTable extends JTable {

    private static final long serialVersionUID = 2680194422003453639L;

    public FeatureTrackTable(final Model model) {
        super(new FeatureTrackListModel(model));
        FeatureTrackListModel listModel = (FeatureTrackListModel) this.getModel();
        getTableHeader().addMouseMotionListener(new ColumnHeaderToolTips(listModel));
        /* Set column widths */
//        setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
//        getColumnModel().getColumn(0).setPreferredWidth(150);
        for (int i = 1; i < this.getColumnCount(); i++){
            getColumnModel().getColumn(i).setPreferredWidth(20);
            getColumnModel().getColumn(i).setMaxWidth(50);
        }

        ToolTipManager.sharedInstance().setInitialDelay(0);
        setCellSelectionEnabled(false);
        setRowSelectionAllowed(false);
        setColumnSelectionAllowed(false);
        setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        addMouseListener(new MouseAdapter() {
            public void mouseReleased(MouseEvent e) {
                int column = getSelectedColumn();
                int row = getSelectedRow();
                Type type = (Type) getValueAt(row, 0);
                if (column == 3) {
                    model.setVisibleOnAnnotation(type, !model.isVisibleOnAnnotation(type));
                }
                if (column == 1) {
                    model.setVisibleOnChromosome(type, !model.isVisibleOnChromosome(type));
                }
                if(column==2)
                    model.setVisibleOnStructure(type, !model.isVisibleOnStructure(type));
                if (column == 4) {
                    Color newColor = JColorChooser.showDialog(
                            model.getParent(),
                            "Choose track color",
                            Configuration.getColor(type));
                    if(newColor!=null)
                        Configuration.setColor(type,newColor);
                    model.refresh();
                           
                }
                if (column == 5) {
                    // System.out.println("Setting for "+type);
                    model.setShowText(type, !model.isShowTextOnStructure(type));
                }
                if (column == 6) {
                    // System.out.println("Setting for "+type);
                    model.setShowChromText(type, !model.isShowChromText(type));
                }
                if (column == 7) {
                    switch (model.getDisplayType(type)) {
                    case MultiLineBlocks:
                        model.setDisplayType(type, DisplayType.OneLineBlocks);
                        break;
                    case LineProfile:
                        model.setDisplayType(type, DisplayType.ColorCodingProfile);
                        break;
                    case OneLineBlocks:
                        model.setDisplayType(type, DisplayType.MultiLineBlocks);
                        break;
                    case ColorCodingProfile:
                        model.setDisplayType(type, DisplayType.LineProfile);
                        break;
                    }
                }
                if (column == 8) {
                    Type.moveUp(type);
                    model.refresh();
                }
                if (column == 9) {
                    Type.moveDown(type);
                    model.refresh();
                }
            }
        });
    }

    class ColumnHeaderToolTips extends MouseMotionAdapter {
        private int index = -1;

        private FeatureTrackListModel listModel;

        public ColumnHeaderToolTips(FeatureTrackListModel listModel) {
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
