/**
 * %HEADER%
 */
package net.sf.jannot.parser;

import java.io.InputStream;
import java.util.Arrays;
import java.util.SortedSet;
import java.util.TreeSet;

import net.sf.jannot.EntrySet;
import net.sf.jannot.Feature;
import net.sf.jannot.Location;
import net.sf.jannot.MemoryFeatureAnnotation;
import net.sf.jannot.Strand;
import net.sf.jannot.Type;
import be.abeel.io.LineIterator;

public class PTTParser extends Parser {

	/**
	 * @param dataKey
	 */
	public PTTParser() {
		super(null);
		// TODO Auto-generated constructor stub
	}

	/**
	 * Will return an entry for each unique seq_id
	 */
	@Override
	public EntrySet parse(InputStream is, EntrySet set) {
		if (set == null)
			set = new EntrySet();
		LineIterator it = new LineIterator(is);
		it.setSkipBlanks(true);
		it.setSkipComments(true);
		Type t = Type.get("protein");
		for (String line : it) {
			String[] arr = line.trim().split("\t");
			try {

				String[] loc = arr[0].trim().split("\\.\\.");
				Location l = new Location(Integer.parseInt(loc[0]), Integer.parseInt(loc[1]));
				Feature f = new Feature();
				SortedSet<Location> tmp = new TreeSet<Location>();
				tmp.add(l);
				f.setLocation(tmp);
				char strand = arr[1].charAt(0);
				switch (strand) {
				case '-':
					f.setStrand(Strand.REVERSE);
					break;
				case '+':
					f.setStrand(Strand.FORWARD);
					break;
				case '.':
				case '?':
					f.setStrand(Strand.UNKNOWN);
					break;
				}
				f.addQualifier("length", arr[2]);
				f.addQualifier("PID", arr[3]);
				f.addQualifier("Gene", arr[4]);
				f.addQualifier("Synonym", arr[5]);
				f.addQualifier("Code", arr[6]);
				f.addQualifier("COG", arr[7]);
				f.addQualifier("Product", arr[8]);

				f.setType(t);

				// set.getEntry().annotation.add(f);
				MemoryFeatureAnnotation fa = set.iterator().next().getMemoryAnnotation(t);
				fa.add(f);

			} catch (Exception e) {
				e.printStackTrace();
				System.err.println("Could not parse line: " + Arrays.toString(arr));
			}

		}
		return set;
	}



}
