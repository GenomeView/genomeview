package net.sf.jannot.parser.software;

import java.io.InputStream;
import java.util.ArrayList;

import cern.colt.Arrays;

import be.abeel.io.LineIterator;

import net.sf.jannot.DataKey;
import net.sf.jannot.Entry;
import net.sf.jannot.EntrySet;
import net.sf.jannot.Strand;
import net.sf.jannot.alignment.maf.AbstractAlignmentBlock;
import net.sf.jannot.alignment.maf.AbstractAlignmentSequence;
import net.sf.jannot.alignment.maf.MAFMemoryMultipleAlignment;
import net.sf.jannot.alignment.maf.MemoryAlignmentBlock;
import net.sf.jannot.alignment.maf.MemoryAlignmentSequence;
import net.sf.jannot.parser.Parser;
import net.sf.jannot.refseq.MemorySequence;

public class MauveParser extends Parser {

	/**
	 * @param dataKey
	 */
	public MauveParser(DataKey dataKey) {
		super(dataKey);

	}

	@Override
	public EntrySet parse(InputStream is, EntrySet set) {
		if (set == null)
			set = new EntrySet();
		LineIterator it = new LineIterator(is);
		it.setCommentIdentifier("#");
		it.setSkipBlanks(true);
		it.setSkipComments(true);
		MemoryAlignmentBlock a = null;
		Entry entry = null;
		MAFMemoryMultipleAlignment ma = null;

		StringBuffer buffer = null;
		boolean marker = true;
		// boolean next = false;
		String headerLine = null;

		// ArrayList<AbstractAlignmentBlock> blocks = new
		// ArrayList<AbstractAlignmentBlock>();
		// ArrayList<AbstractAlignmentBlock> seqs = new
		// ArrayList<AbstractAlignmentBlock>();

		for (String line : it) {

			if (line.charAt(0) == '=') {
				marker = true;
				// System.out.println("Close old block: " + line);

				/* Start new block */
			} else if (line.charAt(0) == '>') {
				System.out.println("Parsing: " + line);
				String[] arr = line.split("[ ]+");
				String[] locArr = arr[1].split(":")[1].split("-");

				if (ma == null) {
					String name = arr[3].substring(arr[3].lastIndexOf('/') + 1).split("\\.")[0];
					entry = set.getOrCreateEntry(name);
					ma = new MAFMemoryMultipleAlignment();
					entry.add(dataKey, ma);
				}

				if (buffer != null) {

					addSequence(buffer, headerLine, a, entry,ma);

				}
				if (marker) {
					marker = false;
					System.out.println("->Make block " + Arrays.toString(arr) + "\t" + java.util.Arrays.toString(locArr));

					a = new MemoryAlignmentBlock(Integer.parseInt(locArr[0]), Integer.parseInt(locArr[1]));
					ma.add(a);
				}
				buffer = new StringBuffer();
				headerLine = line;

			} else {
				buffer.append(line);

			}
		}
		addSequence(buffer, headerLine, a, entry,ma);
		
		for (AbstractAlignmentBlock ab : ma.get()) {
			System.out.println("AB: " + ab.start() + "\t" + ab.end());
			for (AbstractAlignmentSequence as : ab) {
				System.out.println("\tAS: " + as.getName() + "\t" + as.start() + "\t" + as.end());
			}
		}
		return set;
	}

	private void addSequence(StringBuffer buffer, String headerLine, MemoryAlignmentBlock a, Entry entry, MAFMemoryMultipleAlignment ma) {
		String[] prevArr = headerLine.split("[ ]+");
		System.out.println("-->Add sequence " + prevArr[3]);

		String[] prevLocArr = prevArr[1].split(":")[1].split("-");

		MemorySequence seq = new MemorySequence(buffer.toString());
		AbstractAlignmentSequence s = new MemoryAlignmentSequence(prevArr[3], Integer.parseInt(prevLocArr[0]),
				Integer.parseInt(prevLocArr[1]) - Integer.parseInt(prevLocArr[0]) + 1, entry.getMaximumLength(),
				Strand.fromSymbol(prevArr[2].charAt(0)), seq);
		a.add(s);
		ma.addSpecies(prevArr[3]);

	}
}
