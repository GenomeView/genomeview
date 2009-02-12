/**
 * %HEADER%
 */
package net.sf.genomeview.gui.information;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.util.Observable;
import java.util.Observer;

import javax.swing.ImageIcon;
import javax.swing.table.AbstractTableModel;

import net.sf.genomeview.core.Configuration;
import net.sf.genomeview.core.DisplayType;
import net.sf.genomeview.core.Icons;
import net.sf.genomeview.data.Model;
import net.sf.jannot.Type;

/**
 * Wraps an instance of the Model interface and let it act like a tablemodel.
 * 
 * @author Thomas Abeel
 * 
 */
public class FeatureTrackListModel extends AbstractTableModel implements Observer {

    @Override
    public String getColumnName(int column) {
        switch (column) {
        case 0:
            return "Type";
        case 3:
            return "Annotation visibility";
        case 1:
            return "Chromosome visibility";
        case 2:
            return "Structure visibility";
        case 4:
            return "Color";
        case 5:
            return "Text";
        case 6:
            return "ChromText";
        case 7:
            return "Collapsed";
        case 8:
            return "";
        case 9:
            return "";
        default:
            return "no name";

        }
    }

    private static final long serialVersionUID = 1999579168557221399L;

    private Model model;

    public FeatureTrackListModel(Model model) {
        this.model = model;
        model.addObserver(this);

    }

    public void update(Observable o, Object arg) {

        fireTableDataChanged();

    }

    @Override
    public int getColumnCount() {
        return 10;
    }

    @Override
    public int getRowCount() {
        return Type.values().length;
    }

    @Override
    public Class<?> getColumnClass(int arg0) {
        switch (arg0) {
        case 0:
            return Type.class;

        default:
            return ImageIcon.class;
        }
    }

    @Override
    public Object getValueAt(int row, int col) {
        Type ct = Type.values()[row];
        switch (col) {
        case 0:
            return ct;
        case 3:
            if (model.isVisibleOnAnnotation(ct)) {
                return Icons.YES;
            } else {
                return Icons.NO;
            }
        case 1:
            if (model.isVisibleOnChromosome(ct)) {
                return Icons.YES;
            } else {
                return Icons.NO;
            }
        case 2:
            if (model.isVisibleOnStructure(ct)) {
                return Icons.YES;
            } else {
                return Icons.NO;
            }
        case 4:
            return new ColorIcon(Configuration.getColor("TYPE_" + ct));
        case 5:
            if (model.isShowTextOnStructure(ct)) {
                return Icons.YES;
            } else {
                return Icons.NO;
            }
        case 6:
            if (model.isShowChromText(ct)) {
                return Icons.YES;
            } else {
                return Icons.NO;
            }
        case 7:
            DisplayType dt = model.getDisplayType(ct);

            if (dt == DisplayType.OneLineBlocks || dt == DisplayType.ColorCodingProfile) {
                return Icons.YES;
            } else {
                return Icons.NO;
            }

        case 8:
            return Icons.UP_ARROW;
        case 9:
            return Icons.DOWN_ARROW;
        }
        return null;

    }

    class ColorIcon extends ImageIcon {

        private static final long serialVersionUID = 1L;

        private Color c;

        public ColorIcon(Color c) {
            this.c = c;
        }

        @Override
        public int getIconHeight() {
            return 16;
        }

        @Override
        public int getIconWidth() {
            return 16;
        }

        @Override
        public synchronized void paintIcon(Component comp, Graphics g, int x, int y) {
            g.setColor(c);
            g.fillRect(0, 0, 16, 16);
        }

    }
}
