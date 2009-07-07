/**
 * %HEADER%
 */
package net.sf.genomeview.gui.information;

import java.util.Observable;
import java.util.Observer;

import javax.swing.ImageIcon;
import javax.swing.table.AbstractTableModel;

import net.sf.genomeview.core.Icons;
import net.sf.genomeview.data.Model;
import net.sf.genomeview.gui.annotation.track.FeatureTrack;
import net.sf.genomeview.gui.annotation.track.StructureTrack;
import net.sf.genomeview.gui.annotation.track.Track;
import net.sf.jannot.Type;

/**
 * Wraps an instance of the Model interface and let it act like a tablemodel.
 * 
 * @author Thomas Abeel
 * 
 */
public class TrackTableModel extends AbstractTableModel implements Observer {

	@Override
	public String getColumnName(int column) {
		switch (column) {
		
		case 0:
			return "Structure visibility";
		case 1:
			return "Annotation visibility";
		case 2:
			return "Collapsed";
		case 3:
			return "up";
		case 4:
			return "down";
		default:
			return "Track name";

		}
	}

	private static final long serialVersionUID = 19995791685221399L;

	private Model model;

	public TrackTableModel(Model model) {
		this.model = model;
		model.addObserver(this);

	}

	public void update(Observable o, Object arg) {

		fireTableDataChanged();

	}

	@Override
	public int getColumnCount() {
		return 6;
	}

	@Override
	public int getRowCount() {
		return model.getTrackList().size();
	}

	@Override
	public Class<?> getColumnClass(int arg0) {
		switch (arg0) {
		case 5:
			return Track.class;

		default:
			return ImageIcon.class;
		}
	}

	@Override
	public Object getValueAt(int row, int col) {
		Track track = model.getTrackList().get(row);
		switch (col) {
		
		
		
		case 0:
			if (track instanceof FeatureTrack) {
				Type ct = ((FeatureTrack) track).getType();
				if (getStructureTrack().isTypeVisible(ct)) {
					return Icons.YES;
				} else {
					return Icons.NO;
				}
			} else {
				return Icons.BDASH;
			}
		case 1:
			if (model.getTrackList().get(row).isVisible()) {
				return Icons.YES;
			} else {
				return Icons.NO;
			}
		case 2:
			if (track.isCollapsible()) {
				if (track.isCollapsed()) {
					return Icons.YES;
				} else {
					return Icons.NO;
				}
			} else {
				return Icons.BDASH;
			}

		case 3:
			return Icons.UP_ARROW;
		case 4:
			return Icons.DOWN_ARROW;
		default: return track.displayName();
		}
		

	}

	private StructureTrack getStructureTrack() {
		for (Track track : model.getTrackList()) {
			if (track instanceof StructureTrack)
				return (StructureTrack) track;
		}
		return null;
	}

	public void mouse(int column, int row) {
		Track track = model.getTrackList().get(row);
		
		
		if (column == 0) {
			StructureTrack strack = getStructureTrack();
			if (track instanceof FeatureTrack) {
				Type type = ((FeatureTrack) track).getType();
				strack.setTypeVisible(type, !strack.isTypeVisible(type));

			}

		}

		if (column == 1) {
			model.getTrackList().get(row).setVisible(
					!model.getTrackList().get(row).isVisible());

		}
		
		if (column == 2) {
			if (track.isCollapsible()) {
				track.setCollapsed(!track.isCollapsed());
			}
		}
		if (column == 3) {
			model.getTrackList().up(row);
		}
		if (column == 4) {
			model.getTrackList().down(row);
		}

	}
}
