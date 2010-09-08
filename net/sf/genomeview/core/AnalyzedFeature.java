/**
 * %HEADER%
 * 
 */
package net.sf.genomeview.core;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import net.sf.jannot.AminoAcidMapping;
import net.sf.jannot.Feature;
import net.sf.jannot.Location;
import net.sf.jannot.Strand;
import net.sf.jannot.refseq.Sequence;
import net.sf.jannot.utils.SequenceTools;

/**
 * 
 * @author Thomas Abeel
 * 
 */
public class AnalyzedFeature {

	private Sequence seq;
	private Feature f;
	private AminoAcidMapping aa;
	private String translation;
	private Sequence dna;
	private String startCodon;

	public AnalyzedFeature(Sequence sequence, Feature rf, AminoAcidMapping aminoAcidMapping) {
		this.seq = sequence;
		this.f = rf;
		this.aa = aminoAcidMapping;
		dna = SequenceTools.extractSequence(seq, f);
		startCodon = dna.subsequence(1, 4).stringRepresentation();
		translation = SequenceTools.translate(dna, aa);

	}

	/**
	 * Checks whether the provided location misses an acceptor (AG).
	 * 
	 * @param seq
	 * @param f
	 * @param l
	 * @return
	 */
	public boolean missingAcceptor(Location l) {
		if (!l.equals(f.location().first()) && f.strand() == Strand.FORWARD) {
			String s = seq.subsequence(l.start - 2, l.start).stringRepresentation();
			return !(s.equalsIgnoreCase("ag"));
		}
		if (!l.equals(f.location().last()) && f.strand() == Strand.REVERSE) {
			String s = seq.subsequence(l.end + 1, l.end + 3).stringRepresentation();
			return !(s.equalsIgnoreCase("ct"));
		}
		return false;

	}

	/**
	 * Checks whether the provided location misses a donor (GT).
	 * 
	 * @param seq
	 *            sequence to which the feature maps
	 * @param f
	 *            feature to which the location belongs
	 * @param l
	 *            location to check for missing donor
	 * @return
	 */
	public boolean missingDonor(Location l) {

		if (!l.equals(f.location().last()) && f.strand() == Strand.FORWARD) {
			String s = seq.subsequence(l.end + 1, l.end + 3).stringRepresentation();
			return !(s.equalsIgnoreCase("gt") || s.equalsIgnoreCase("gc"));
		}
		if (!l.equals(f.location().first()) && f.strand() == Strand.REVERSE) {
			String s = seq.subsequence(l.start - 2, l.start).stringRepresentation();
			return !(s.equalsIgnoreCase("ac") || s.equalsIgnoreCase("gc"));
		}
		return false;
	}

	public boolean hasMissingStartCodon() {
		return !aa.isStart(startCodon);

	}

	public boolean hasMissingStopCodon() {
		return !translation.endsWith("*");

	}

	/**
	 * Checks the feature for internal stop codons.
	 * 
	 * @param sequence
	 * @param rf
	 * @return
	 */
	public List<Location> getIternalStopCodons() {
		ArrayList<Location> out = new ArrayList<Location>();
		for (int i = 0; i < translation.length() - 1; i++) {
			if (translation.charAt(i) == '*') {
				out.add(getntpos(i));
			}
		}
		return out;
	}

	private Location getntpos(int aapos) {
		ArrayList<Integer> list = new ArrayList<Integer>();
		for (Location l : f.location()) {
			for (int i = l.start; i <= l.end; i++)
				list.add(i);
		}
		if (f.strand() == Strand.REVERSE)
			Collections.reverse(list);
		return new Location(list.get(aapos * 3), list.get(aapos * 3 + 2));

	}

}