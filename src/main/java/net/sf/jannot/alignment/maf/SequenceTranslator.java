package net.sf.jannot.alignment.maf;

import java.io.FileNotFoundException;
import java.io.PrintWriter;

import cern.colt.Arrays;
import net.sf.jannot.Strand;
import net.sf.jannot.alignment.maf.AbstractAlignmentSequence;
import net.sf.jannot.refseq.Sequence;

/**
 * 
 * @author Thomas Abeel
 * 
 */
public class SequenceTranslator {
	private int[] position;
	private int maxCur = 0;

	public SequenceTranslator(AbstractAlignmentSequence alignmentSequence) {
		initPosition(alignmentSequence);
	}

	private void initPosition(AbstractAlignmentSequence as) {

		int len = -1;
		len = as.end() - as.start();
		position = new int[len + 1];
		int cur = 0;
		int ix = 0;
		// FIXME easy to speed this up!
		Sequence seq = as.seq();
		for (int i = 1; i <= seq.size(); i++) {
			char nt;
			if (as.strand() == Strand.FORWARD)
				nt = as.seq().get(i, i + 1).iterator().next();
			else
				nt = as.seq().get(as.seq().size() - i + 1, as.seq().size() - i + 2).iterator().next();
			if (nt == '-') {
				cur++;
			} else {
				position[ix++] = cur++;
			}
		}
		while (ix < position.length) {
			position[ix++] = cur++;
		}
		maxCur = cur;
	}

	private int[] revtable = null;

	public synchronized int[] getReverseTranslationTable() {
		if (revtable == null) {
			revtable = new int[maxCur];
			for (int i = 0; i < position.length; i++) {
				revtable[position[i]] = i;
			}
			for (int i = 1; i < revtable.length; i++) {
				if (revtable[i] == 0) {
					revtable[i] = revtable[i - 1];
				}
			}

		}
		return revtable;

	}

	public int translate(int pos) {
		return position[pos];
	}
}
