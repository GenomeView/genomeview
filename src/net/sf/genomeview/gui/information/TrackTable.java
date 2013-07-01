/**
 * %HEADER%
 */
package net.sf.genomeview.gui.information;

import java.awt.Component;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
	private Logger log = LoggerFactory.getLogger(TrackTable.class.getCanonicalName());
	private Model model;
	private static final long serialVersionUID = 2680194422003453639L;

	public TrackTable(final Model model) {
		super(new TrackTableModel(model));
		this.model=model;
		setSelectionModel(new TrackSelectionModel());
		
		setTableHeader(null);
		setUI(new DragDropRowTableUI(model));
		/* Set column widths */
	
		getColumnModel().getColumn(0).setPreferredWidth(20);
		getColumnModel().getColumn(0).setMaxWidth(50);
		getColumnModel().getColumn(2).setPreferredWidth(20);
		getColumnModel().getColumn(2).setMaxWidth(50);

		ToolTipManager.sharedInstance().setInitialDelay(0);
		

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

	

}
