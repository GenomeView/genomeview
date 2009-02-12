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

import net.sf.genomeview.core.DisplayType;
import net.sf.genomeview.data.Model;
import net.sf.genomeview.plugin.IValueFeature;


/**
 * Wraps an instance of the Model interface and let it act like a
 * ListModel.
 * 
 * @author Thomas Abeel
 * 
 */
public class ValueTrackListModel extends AbstractTableModel implements Observer {

    /**
     * 
     */
    private static final long serialVersionUID = -4176724854372480953L;

    @Override
    public String getColumnName(int column) {
        switch (column) {
        case 0:
            return "name";
        case 1:
            return "Collapsed";
        case 2:
            return "Visible";
        default:
            return null;
        }
    }

    private Model model;

    public ValueTrackListModel(Model model) {
        this.model = model;
        model.addObserver(this);

    }

    // public Object getElementAt(int arg0) {
    // return model.getTypeTerms().get(arg0);
    // }
    //
    // public int getSize() {
    // return model.getTypeTerms().size();
    // }

    public void update(Observable o, Object arg) {
        // fireContentsChanged(this, 0, getSize());
        // fireIntervalAdded(this, 0, getSize());
        // fireIntervalRemoved(this, 0, getSize());
        fireTableDataChanged();

    }

    @Override
    public int getColumnCount() {
        return 3;
    }

    @Override
    public int getRowCount() {
        return model.getValueFeatures(model.getSelectedEntry()).size();
    }

    @Override
    public Class<?> getColumnClass(int arg0) {
        switch (arg0) {
        case 0:
            return String.class;

        default:
            return ImageIcon.class;
        }
    }

    public IValueFeature getValueFeature(int row){
        return model.getValueFeatures(model.getSelectedEntry()).get(row);
    }
    @Override
    public Object getValueAt(int row, int col) {
        IValueFeature f = getValueFeature(row);
        switch (col) {
        case 0:
            return f.getName();
        case 1:
            DisplayType dt = model.getValueFeatureDisplayType(f);

            if (dt == DisplayType.OneLineBlocks || dt == DisplayType.ColorCodingProfile) {
                return new ImageIcon(this.getClass().getResource("/images/yes.png"));
            } else {
                return new ImageIcon(this.getClass().getResource("/images/no.png"));
            }
        case 2:
            boolean visible = model.isValueFeatureVisible(f);

            if (visible) {
                return new ImageIcon(this.getClass().getResource("/images/yes.png"));
            } else {
                return new ImageIcon(this.getClass().getResource("/images/no.png"));
            }

        }
        return null;

        // if (col == 0)
        //        
        // else if (col == 1) {
        //            
        //
        // } else if (col == 2) {
        // if (model.isVisibleOnChromosome(ct)) {
        // return new ImageIcon(this.getClass().getResource("/images/yes.png"));
        // } else {
        // return new ImageIcon(this.getClass().getResource("/images/no.png"));
        // }
        // }
        // return null;
    }

    class ColorIcon extends ImageIcon {

        /**
		 * 
		 */
		private static final long serialVersionUID = 320457653320639364L;
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
