package net.sf.genomeview;

import net.sf.jannot.AminoAcidMapping;
import net.sf.jannot.Location;
import net.sf.jannot.refseq.Sequence;
import net.sf.jannot.utils.SequenceTools;

@Deprecated
public class BufferSeq {

	private char[] buffer = null;
	private Location l;

	public BufferSeq(Sequence seq, Location l) {
		this.l = l;
		Iterable<Character> bufferedSeq = seq.get(l.start, l.end);

		buffer = new char[l.length()];
		int idx = 0;
		for (char c : bufferedSeq) {
			buffer[idx++] = c;
		}
	}

	public BufferSeq(Sequence sequence) {
		this(sequence,new Location(1,sequence.size()));
	}

	public char getNucleotide(int i) {
		return buffer[i - l.start];
	}

	public char getReverseNucleotide(int i) {
		return SequenceTools.complement(buffer[i - l.start]);
	}
	
	public String toString(){
		return new String(buffer);
	}

	public char getAminoAcid(int pos, AminoAcidMapping mapping) {
		String codon = "" + getNucleotide(pos) + getNucleotide(pos + 1)
				+ getNucleotide(pos + 2);
		return mapping.get(codon);
	}

	public char getReverseAminoAcid(int pos, AminoAcidMapping mapping) {
		String codon = "" + getReverseNucleotide(pos + 2)
				+ getReverseNucleotide(pos + 1) + getReverseNucleotide(pos);
		return mapping.get(codon);
	}

}
