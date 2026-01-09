/**
 * %HEADER%
 */
package net.sf.jannot.parser.software;

import java.io.InputStream;

import net.sf.jannot.Entry;
import net.sf.jannot.EntrySet;
import net.sf.jannot.Feature;
import net.sf.jannot.Location;
import net.sf.jannot.MemoryFeatureAnnotation;
import net.sf.jannot.Strand;
import net.sf.jannot.Type;
import net.sf.jannot.parser.Parser;
import be.abeel.io.LineIterator;

/**
 * @author Thomas Abeel
 * 
 */
public class TRNAscanParser extends Parser {

	/**
	 * @param dataKey
	 */
	public TRNAscanParser() {
		super(null);
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
		it.setSkipBlanks(true);
		it.setSkipComments(true);
		/* Skip first three lines */
		it.next();
		it.next();
		it.next();
		Type t = Type.get("tRNA");
		for (String line : it) {
			String[] arr = line.split("[ \t]+");
			Entry e = set.getOrCreateEntry(arr[0]);
			Feature f = new Feature();
			int start = Integer.parseInt(arr[2]);
			int end = Integer.parseInt(arr[3]);
			f.setType(t);
			f.addQualifier("type", arr[4]);
			f.addQualifier("anti-codon", arr[5]);
			f.setScore(Double.parseDouble(arr[8]));
			f.addLocation(new Location(start, end));
			if (start > end) {
				f.setStrand(Strand.REVERSE);
			} else {
				f.setStrand(Strand.FORWARD);
			}
			MemoryFeatureAnnotation fa = e.getMemoryAnnotation(t);
			fa.add(f);

		}
		return set;
	}

}
