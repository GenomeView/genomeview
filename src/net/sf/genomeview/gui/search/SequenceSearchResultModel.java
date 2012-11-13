/**
 * %HEADER%
 */
package net.sf.genomeview.gui.search;

import java.awt.Color;
import java.util.ArrayList;

import javax.swing.JOptionPane;
import javax.swing.table.AbstractTableModel;

import net.sf.genomeview.BufferSeq;
import net.sf.genomeview.data.Model;
import net.sf.genomeview.gui.search.SearchDialog.SequenceType;
import net.sf.jannot.AminoAcidMapping;
import net.sf.jannot.Location;
import net.sf.jannot.Strand;
import net.sf.jannot.utils.SequenceTools;

import com.eaio.stringsearch.BoyerMooreHorspoolRaita;
import com.eaio.stringsearch.ShiftOrMismatches;

/**
 * 
 * @author Thomas Abeel
 * 
 */
class SequenceSearchResultModel extends AbstractSearchResultModel {
	public SequenceSearchResultModel(Model model) {
		super(model);
		
	}

	class StrandedLocation {
		public StrandedLocation(Strand s, Location l) {
			this.l = l;
			this.s = s;
		}

		Location l;

		Strand s;
	}

	/**
         * 
         */
	private static final long serialVersionUID = 5974192617901951628L;

	private ArrayList<StrandedLocation> locations = new ArrayList<StrandedLocation>();

	@Override
	public Class<?> getColumnClass(int columnIndex) {
		return String.class;
	}

	@Override
	public int getColumnCount() {
		return 3;
	}

	@Override
	public String getColumnName(int columnIndex) {
		switch (columnIndex) {
		case 0:
			return "Strand";
		case 1:
			return "Start";
		case 2:
			return "End";
		}
		return null;
	}

	@Override
	public int getRowCount() {
		return locations.size();
	}

	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		switch (columnIndex) {
		case 0:
			return locations.get(rowIndex).s;
		case 1:
			return locations.get(rowIndex).l.start();
		case 2:
			return locations.get(rowIndex).l.end();
		}
		return null;
	}

	@Override
	public boolean isCellEditable(int rowIndex, int columnIndex) {
		return false;
	}

	/**
	 * Search this particular sequence and populate the model with the results.
	 * 
	 * @param model
	 * @param mismatch
	 * @param text
	 */
	void search(final Model model, String pattern, final int mismatch, final SequenceType type) {
		locations.clear();
		model.clearHighlights();
		final byte[] bytePattern = pattern.toUpperCase().getBytes();

		new Thread(new Runnable() {

			@Override
			public void run() {
				try {
					switch (type) {
					case Nucleotide:
						performNucleotideSearch();
						break;
					case AminoAcid:
						performAminoAcidSearch();
					}
				} catch (IllegalArgumentException ie) {
					JOptionPane
							.showMessageDialog(
									model.getGUIManager().getParent(),
									"<html>Mismatch search has some limitations:<br/>"
											+ "<table><tr><td>Editing distance</td><td>maximum pattern length</td></tr><tr><td>"
											+ "1</td><td>15</td></tr><tr><td>" + "2-3</td><td>10</td></tr><tr><td>"
											+ "4-5</td><td>7</td></tr></table></html>", "Too many mismatches",
									JOptionPane.WARNING_MESSAGE);
				}

			}

			private void performAminoAcidSearch() {
				// byte[] byteSequence =
				// model.getSelectedEntry().sequence().getSequence().toUpperCase().getBytes();
				byte[] byteSequence = new BufferSeq(model.getSelectedEntry().sequence()).toString().toUpperCase()
						.getBytes();
				byte[] translation = translate(byteSequence, 0);
				forwardSearch(translation, 0);
				translation = translate(byteSequence, 1);
				forwardSearch(translation, 1);
				translation = translate(byteSequence, 2);
				forwardSearch(translation, 2);

				reverseArray(byteSequence);
				for (int i = 0; i < byteSequence.length; i++) {
					byteSequence[i] = (byte) SequenceTools.complement((char) byteSequence[i]);
				}

				translation = translate(byteSequence, 0);
				reverseSearch(translation, 0);
				translation = translate(byteSequence, 1);
				reverseSearch(translation, 1);
				translation = translate(byteSequence, 2);
				reverseSearch(translation, 2);
			}

			private byte[] translate(byte[] seq, int offset) {
				byte[] out = new byte[seq.length / 3];
				for (int i = 0; i < out.length && 3 * i + offset + 2 < seq.length; i++) {
					AminoAcidMapping aamap = model.getAAMapping(model.getSelectedEntry());
					out[i] = (byte) aamap.get("" + (char) seq[3 * i + offset] + (char) seq[3 * i + offset + 1]
							+ (char) seq[3 * i + offset + 2]);
					// System.out.println((char)out[i]);
				}
				return out;
			}

			private void forwardSearch(byte[] byteSequence, int offset) {

				BoyerMooreHorspoolRaita bm = new BoyerMooreHorspoolRaita();

				ShiftOrMismatches som = new ShiftOrMismatches();

				/* Search forward strand */
				int[] lastPos = { 0, 0 };
				do {
					if (mismatch == 0)
						lastPos[0] = bm.searchBytes(byteSequence, lastPos[0], byteSequence.length, bytePattern);
					else
						lastPos = som.searchBytes(byteSequence, lastPos[0], byteSequence.length, bytePattern, mismatch);

					if (lastPos[0] >= 0) {

						lastPos[0]++;
						Location l = null;
						switch (type) {
						case Nucleotide:
							l = new Location(lastPos[0], lastPos[0] + bytePattern.length);
							break;
						case AminoAcid:
							l = new Location(lastPos[0] * 3 + offset - 2, lastPos[0] * 3 + bytePattern.length * 3
									+ offset - 2);
							break;
						}

						locations.add(new StrandedLocation(Strand.FORWARD, l));
						model.addHighlight(l, Color.cyan, Strand.FORWARD);
						fireTableDataChanged();

					}

				} while (lastPos[0] >= 0);
			}

			private void performNucleotideSearch() {
				// byte[] byteSequence =
				// model.getSelectedEntry().sequence().getSequence().toUpperCase().getBytes();
				byte[] byteSequence = new BufferSeq(model.getSelectedEntry().sequence()).toString().toUpperCase()
						.getBytes();

				forwardSearch(byteSequence, 0);
				reverseArray(byteSequence);
				for (int i = 0; i < byteSequence.length; i++) {
					byteSequence[i] = (byte) SequenceTools.complement((char) byteSequence[i]);
				}
				reverseSearch(byteSequence, 0);

			}

			private void reverseArray(byte[] array) {
				if (array == null) {
					return;
				}
				int i = 0;
				int j = array.length - 1;
				byte tmp;
				while (j > i) {
					tmp = array[j];
					array[j] = array[i];
					array[i] = tmp;
					j--;
					i++;
				}
			}

			private void reverseSearch(byte[] byteSequence, int offset) {
				BoyerMooreHorspoolRaita bm = new BoyerMooreHorspoolRaita();

				ShiftOrMismatches som = new ShiftOrMismatches();
				/* Search reverse strand */
				int lastPos[] = { 0, 0 };
				do {
					if (mismatch == 0)
						lastPos[0] = bm.searchBytes(byteSequence, lastPos[0], byteSequence.length, bytePattern);
					else
						lastPos = som.searchBytes(byteSequence, lastPos[0], byteSequence.length, bytePattern, mismatch);

					if (lastPos[0] >= 0) {
						Location l = null;
						switch (type) {
						case Nucleotide:
							l = new Location(byteSequence.length - lastPos[0] + 1, byteSequence.length
									- (lastPos[0] + bytePattern.length) + 1);
							break;
						case AminoAcid:
							l = new Location(byteSequence.length * 3 - lastPos[0] * 3 + 2 - offset, byteSequence.length
									* 3 - (lastPos[0] * 3 + bytePattern.length * 3) + 2 - offset);
							break;

						}

						locations.add(new StrandedLocation(Strand.REVERSE, l));
						model.addHighlight(l, Color.magenta, Strand.REVERSE);
						lastPos[0]++;
						fireTableDataChanged();

					}

				} while (lastPos[0] >= 0);

			}

		}).start();
	}

	Location getLocation(int index) {
		return locations.get(index).l;
	}

	@Override
	void clear() {
		locations.clear();
		fireTableDataChanged();

	}
}