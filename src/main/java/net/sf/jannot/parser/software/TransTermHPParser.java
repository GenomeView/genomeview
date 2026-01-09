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
 * Parser for the output of the TransTermHP program as described in
 * 
 * <cite> C. Kingsford, K. Ayanbule and S.L. Salzberg. Rapid, accurate,
 * computational discovery of Rho-independent transcription terminators
 * illuminates their relationship to DNA uptake. Genome Biology 8:R22 (2007).
 * </cite>
 * 
 * Data format description: http://transterm.cbcb.umd.edu/description.html
 * 
 * 
 * @author Thomas Abeel
 * 
 */
public class TransTermHPParser extends Parser {

	/**
	 * @param dataKey
	 */
	public TransTermHPParser() {
		super(null);
		// TODO Auto-generated constructor stub
	}

	@Override
	public EntrySet parse(InputStream is, EntrySet set) {
		if (set == null)
			set = new EntrySet();
		LineIterator it = new LineIterator(is);
		String id = null;
		Type t = Type.get("TransTermHP");
		for (String line : it) {
			line = line.trim();
			if (line.startsWith("SEQUENCE"))
				id = line.substring(9);
			if (line.startsWith("TERM")) {
				String[] arr = line.split("[ \t\n\f\r]+");
				Feature f = new Feature();
				f.setType(t);
				f.addLocation(new Location(Integer.parseInt(arr[2]), Integer.parseInt(arr[4])));
				f.setStrand(Strand.fromSymbol(arr[5].charAt(0)));
				f.addQualifier("source", "TransTermHP");
				f.addQualifier("location", loc(arr[6].charAt(0)));
				f.setScore(Integer.parseInt(arr[7]) / 100.0);
				f.addQualifier("hairpin score", arr[8]);
				f.addQualifier("tail score", arr[9]);
				f.addQualifier("note", arr[10]);
				// System.out.println(f);
				Entry e = set.getOrCreateEntry(id);
				MemoryFeatureAnnotation fa = e.getMemoryAnnotation(t);
				fa.add(f);
			}
		}

		return set;
	}

	private String loc(char charAt) {
		switch (charAt) {
		case 'g':
		case 'G':
			return "In the interior of a gene (at least 50bp from an end)";
		case 'f':
		case 'F':
			return "Between two +strand genes";
		case 'R':
		case 'r':
			return "Between two -strand genes";
		case 'T':
		case 't':
			return "Between a +strand gene and a -strand gene";
		default:
			return "Not defined";
		}

	}

}
