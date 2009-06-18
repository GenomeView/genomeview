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
			return "Type";
		case 3:
			return "Annotation visibility";
		case 1:
			return "Chromosome visibility";
		case 2:
			return "Structure visibility";

		case 4:
			return "ChromText";
		case 5:
			return "Collapsed";
		case 6:
			return "";
		case 7:
			return "";
		default:
			return "no name";

		}
	}

	private static final long serialVersionUID = 1999579168557221399L;

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
		return 8;
	}

	@Override
	public int getRowCount() {
		return model.getTrackList().size();
	}

	@Override
	public Class<?> getColumnClass(int arg0) {
		switch (arg0) {
		case 0:
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
			return track.displayName();
		case 3:
			if (model.getTrackList().get(row).isVisible()) {
				return Icons.YES;
			} else {
				return Icons.NO;
			}
		case 1:
			if (track instanceof FeatureTrack) {
				Type ct = ((FeatureTrack) track).getType();
				if (model.isVisibleOnChromosome(ct)) {
					return Icons.YES;
				} else {
					return Icons.NO;
				}
			} else {
				return Icons.BDASH;
			}
		case 2:
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

		case 4:
			if (track instanceof FeatureTrack) {
				Type ct = ((FeatureTrack) track).getType();
				if (model.isShowChromText(ct)) {
					return Icons.YES;
				} else {
					return Icons.NO;
				}
			} else {
				return Icons.BDASH;
			}

		case 5:
			if (track.isCollapsible()) {
				if (track.isCollapsed()) {
					return Icons.YES;
				} else {
					return Icons.NO;
				}
			} else {
				return Icons.BDASH;
			}

		case 6:
			return Icons.UP_ARROW;
		case 7:
			return Icons.DOWN_ARROW;
		}
		return null;

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
		if (column == 3) {
			model.getTrackList().get(row).setVisible(
					!model.getTrackList().get(row).isVisible());

		}
		if (column == 1) {
			if (track instanceof FeatureTrack) {
				Type type = ((FeatureTrack) track).getType();
				model.setVisibleOnChromosome(type, !model
						.isVisibleOnChromosome(type));
			}
		}
		if (column == 2) {
			StructureTrack strack = getStructureTrack();
			if (track instanceof FeatureTrack) {
				Type type = ((FeatureTrack) track).getType();
				strack.setTypeVisible(type, !strack.isTypeVisible(type));

			}

		}

		if (column == 4) {
			if (track instanceof FeatureTrack) {
				Type type = ((FeatureTrack) track).getType();
				model.setShowChromText(type, !model.isShowChromText(type));
			}
		}
		if (column == 5) {
			if (track.isCollapsible()) {
				track.setCollapsed(!track.isCollapsed());
			}
		}
		if (column == 6) {
			model.getTrackList().up(row);
		}
		if (column == 7) {
			model.getTrackList().down(row);
		}

	}
}
