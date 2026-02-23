/**
 * %HEADER%
 */
package net.sf.genomeview.gui.information;

import java.util.Observable;
import java.util.Observer;

import javax.swing.ImageIcon;
import javax.swing.table.AbstractTableModel;

import net.sf.genomeview.core.Configuration;
import net.sf.genomeview.core.Icons;
import net.sf.genomeview.data.Model;
import net.sf.genomeview.gui.viztracks.Track;

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
			return "Annotation visibility";
		case 1:
			return "Track name";
		default:
			return "Unload";

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
		return 3;
	}

	@Override
	public int getRowCount() {
		return model.getTrackList().size();
	}

	@Override
	public Class<?> getColumnClass(int arg0) {
		switch (arg0) {
		case 1:
			return Track.class;

		default:
			return ImageIcon.class;
		}
	}

	@Override
	public Object getValueAt(int row, int col) {
		Track track = model.getTrackList().get(row);
		if (track == null){
			System.err.println("Selected track is null, this shouldn't happen...");
			return null;
		}
		switch (col) {

		case 0:
			if (track.config().isVisible()) {
				return Icons.VISIBLE;
			} else {
				return Icons.INVISIBLE;
			}
		case 1:
			return track.config().shortDisplayName();
		default:
			return Icons.DELETE;
		}

	}

	// private StructureTrack getStructureTrack() {
	// for (Track track : model.getTrackList()) {
	// if (track instanceof StructureTrack)
	// return (StructureTrack) track;
	// }
	// return null;
	// }

	public void mouse(int column, int row) {
		Track track = model.getTrackList().get(row);
		

		if (column == 0) {
			model.getTrackList().get(row).config().setVisible(!model.getTrackList().get(row).config().isVisible());

		}

		if (column == 2)
			model.remove(track);

		

	}
}
