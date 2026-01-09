/**
 * %HEADER%
 */
package net.sf.jannot.utils;

import java.util.Iterator;
import java.util.SortedSet;

import net.sf.jannot.AminoAcidMapping;
import net.sf.jannot.Feature;
import net.sf.jannot.Location;
import net.sf.jannot.Strand;
import net.sf.jannot.refseq.MemorySequence;
import net.sf.jannot.refseq.Sequence;

public class SequenceTools {

	public static char complement(char nucleotide) {

		switch (nucleotide) {
		case 'a':
			return 't';
		case 't':
			return 'a';
		case 'g':
			return 'c';
		case 'c':
			return 'g';
		case 'A':
			return 'T';
		case 'T':
			return 'A';
		case 'G':
			return 'C';
		case 'C':
			return 'G';
		case '-':
			return '-';
		default:
			return 'N';

		}
	}



	public static String translate(Sequence dna, AminoAcidMapping mapping) {
		char[] output = new char[(int)Math.ceil(dna.size() / 3.0)];
		Iterator<Character>it=dna.get().iterator();
		int index=0;
		while(it.hasNext()){
			String codon=nextCodon(it);
			output[index++] = mapping.get(codon);
		}
		return new String(output);
	}




	/**
	 * @param it
	 * @return
	 */
	private static String nextCodon(Iterator<Character> it) {
		char[]tmp=new char[]{'n','n','n'};
		if(it.hasNext())
			tmp[0]=it.next();
		if(it.hasNext())
			tmp[1]=it.next();
		if(it.hasNext())
			tmp[2]=it.next();
		return new String(tmp);
	}



	public static Sequence extractSequence(Sequence seq, Feature feat) {
		//System.out.println("SQ:"+seq);
		StringBuffer out = new StringBuffer();
		Location[] arr = feat.location();

		
		for (int j = 0; j < arr.length; j++) {
			StringBuffer x=new StringBuffer();
			Iterable<Character> cc = seq.get(arr[j].start, arr[j].end+1);
			for (Character c : cc) {
				out.append(c);
				x.append(c);
				
			}
			//System.out.println("exon: "+x.toString());
			

		}
		Sequence sq = new MemorySequence(out);
		if (feat.strand() == Strand.REVERSE)
			sq = SequenceTools.reverseComplement(new MemorySequence(out));

		return sq;
	}

		

	/**
	 * Create a reverse complement sequence to the supplied sequence
	 * 
	 * @param sequence
	 * @return
	 */
	public static Sequence reverseComplement(Sequence sequence) {
		StringBuffer buffer = new StringBuffer(sequence.size());
		for(char c:sequence.get()){
			buffer.append(SequenceTools.complement(c));
		}
		buffer.reverse();
		return new MemorySequence(buffer);
	}

}
