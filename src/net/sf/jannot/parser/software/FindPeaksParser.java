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
public class FindPeaksParser extends Parser {

	/**
	 * @param dataKey
	 */
	public FindPeaksParser() {
		super(null);
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
		/* Skip first line */
		it.next();
		Type t = Type.get("peak");
		for (String line : it) {
			String[] arr = line.split("[ \t]+");
			Entry e = set.getOrCreateEntry(arr[1]);
			Feature f = new Feature();
			int start = Integer.parseInt(arr[2]);
			int end = Integer.parseInt(arr[3]);
			f.setType(t);
			f.addQualifier("max_coord", arr[4]);
			f.addQualifier("Name", "Peak "+arr[0]);
			//f.addQualifier("t-score", arr[5]);
			f.setScore(Double.parseDouble(arr[5]));
			f.addLocation(new Location(start, end));
			f.setStrand(Strand.UNKNOWN);
			MemoryFeatureAnnotation fa = e.getMemoryAnnotation(t);
			fa.add(f);

		}
		return set;
	}

}
