/**
 * %HEADER%
 */
package net.sf.genomeview.gui.search;

import java.awt.Color;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JOptionPane;
import javax.swing.table.AbstractTableModel;

import net.sf.genomeview.BufferSeq;
import net.sf.genomeview.data.Model;
import net.sf.genomeview.gui.search.SearchDialog.SequenceType;
import net.sf.jannot.AminoAcidMapping;
import net.sf.jannot.Entry;
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
class MotifSearchResultModel extends AbstractSearchResultModel {
	public MotifSearchResultModel(Model model) {
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
	 * @param text
	 */
	void search(final Model model, String inPattern, final SequenceType type) {
		locations.clear();
		model.clearHighlights();
		// final byte[] bytePattern = pattern.toUpperCase().getBytes();

		final String pattern =inPattern.replace('*', '.').replace('?', '.').replace('-', '.');
		new Thread(new Runnable() {

			@Override
			public void run() {

				switch (type) {
				case Nucleotide:

					performNucleotideSearch();
					break;
				case AminoAcid:

					performAminoAcidSearch();
				}

			}

			private void search(String input, Pattern p, boolean forward, Entry selected, int modifier) {
				Matcher m = p.matcher(input.toUpperCase());
				
				while (m.find()) {
					
			
					int multiplier=1;
					if(type==SequenceType.AminoAcid)
						multiplier=3;
					if (forward) {
						Location l = new Location(m.start()*multiplier+modifier, m.end()*multiplier+modifier);
						locations.add(new StrandedLocation(Strand.FORWARD, l));
						model.addHighlight(l, Color.cyan, Strand.FORWARD);

					} else {

						Location l = new Location(selected.getMaximumLength() - (m.end()*multiplier)+modifier, selected.getMaximumLength() - (m.start()*multiplier)+modifier);
						locations.add(new StrandedLocation(Strand.REVERSE, l));
						model.addHighlight(l, Color.magenta, Strand.REVERSE);
					}
					fireTableDataChanged();

				}
			}

			private void performAminoAcidSearch() {
			
				Pattern p = Pattern.compile(pattern.toUpperCase().replace('X', '.'));

				Entry selected = model.vlm.getVisibleEntry();

				String seq=model.vlm.getSelectedEntry().sequence().stringRepresentation().toUpperCase();
		
				String translation = new String(translate(seq.getBytes(), 0));
				search(translation,p,true,selected,1);
				translation = new String(translate(seq.getBytes(), 1));
				search(translation,p,true,selected,2);
				translation = new String(translate(seq.getBytes(), 2));
				search(translation,p,true,selected,3);


				seq=SequenceTools.reverseComplement(selected.sequence()).stringRepresentation().toUpperCase();

				translation = new String(translate(seq.getBytes(), 0));

				search(translation,p,false,selected,1);
				translation = new String(translate(seq.getBytes(), 1));

				search(translation,p,false,selected,0);
				translation = new String(translate(seq.getBytes(), 2));
				search(translation,p,false,selected,-1);

			}

			private byte[] translate(byte[] seq, int offset) {
				byte[] out = new byte[seq.length / 3];
				for (int i = 0; i < out.length && 3 * i + offset + 2 < seq.length; i++) {
					AminoAcidMapping aamap = model.getAAMapping(model.vlm.getSelectedEntry());
					out[i] = (byte) aamap.get("" + (char) seq[3 * i + offset] + (char) seq[3 * i + offset + 1]
							+ (char) seq[3 * i + offset + 2]);
					// System.out.println((char)out[i]);
				}
				return out;
			}


			private void performNucleotideSearch() {
				Pattern p = Pattern.compile(pattern.toUpperCase().replace('N', '.'));

				Entry selected = model.vlm.getVisibleEntry();

				search(model.vlm.getSelectedEntry().sequence().stringRepresentation().toUpperCase(), p, true, selected,1);
				search(SequenceTools.reverseComplement(selected.sequence()).stringRepresentation().toUpperCase(), p, false, selected,1);


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