/**
 * %HEADER%
 */
package net.sf.jannot.parser;

import java.io.InputStream;
import java.util.Arrays;

import net.sf.jannot.DataKey;
import net.sf.jannot.Entry;
import net.sf.jannot.EntrySet;
import net.sf.jannot.Strand;
import net.sf.jannot.alignment.maf.AbstractAlignmentSequence;
import net.sf.jannot.alignment.maf.MAFMemoryMultipleAlignment;
import net.sf.jannot.alignment.maf.MemoryAlignmentBlock;
import net.sf.jannot.alignment.maf.MemoryAlignmentSequence;
import net.sf.jannot.refseq.MemorySequence;
import be.abeel.io.LineIterator;

/**
 * @author Thomas Abeel
 * 
 */
public class MAFParser extends Parser {

	/**
	 * @param dataKey
	 */
	public MAFParser(DataKey dataKey) {
		super(dataKey);
		// TODO Auto-generated constructor stub
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.sf.jannot.parser.Parser#parse(java.io.InputStream,
	 * net.sf.jannot.source.DataSource, net.sf.jannot.EntrySet)
	 */
	@Override
	public EntrySet parse(InputStream is, EntrySet set) {
		if (set == null)
			set = new EntrySet();
		LineIterator it = new LineIterator(is);
		it.setCommentIdentifier("#");
		it.setSkipBlanks(true);
		MemoryAlignmentBlock a = null;
		Entry entry = null;
		MAFMemoryMultipleAlignment ma =null;
		boolean first = true;
		for (String line : it) {
			if (line.charAt(0) == 'a') {
				first = true;
			} else if (line.charAt(0) == 's') {

				String[] arr = line.split("[ \t]+");

				assert arr.length == 7;
				// if (entry == null) {
				String[] name = arr[1].split("\\.");
				
				if(first){
					ma= new MAFMemoryMultipleAlignment();
					if (set.getEntry(name[name.length - 1]) != null) {
						entry = set.getOrCreateEntry(name[name.length - 1]);
					} else{
						entry = set.getOrCreateEntry(arr[1]);
					}
					if(entry.get(dataKey)!=null)
						ma=(MAFMemoryMultipleAlignment) entry.get(dataKey);
					else
						entry.add(dataKey, ma);
					
				}
				

				// }
				MemorySequence seq = new MemorySequence(arr[6]);
				AbstractAlignmentSequence s = new MemoryAlignmentSequence(arr[1], Integer.parseInt(arr[2]),
						Integer.parseInt(arr[3]), Integer.parseInt(arr[5]), Strand.fromSymbol(arr[4].charAt(0)), seq);
				if (first) {
					first = false;
					a = new MemoryAlignmentBlock(s.start(), s.end());
					ma.add(a);
				}
				a.add(s);
				ma.addSpecies(arr[1]);

			}
		}
		return set;
	}
}
