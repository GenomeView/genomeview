/**
 * %HEADER%
 */
package net.sf.jannot.tabix.codec;

import java.util.SortedSet;
import java.util.TreeSet;

import net.sf.jannot.Feature;
import net.sf.jannot.Location;
import net.sf.jannot.Strand;
import net.sf.jannot.Type;
import net.sf.jannot.tabix.FeatureWrapper;
import net.sf.jannot.tabix.TabixLine;

/**
 * @author Thomas Abeel
 * 
 */
public class GFFCodec extends Codec<Feature> {

	private FeatureWrapper wrapper;

	/**
	 * @param wrapper 
	 * @param in
	 */
	public GFFCodec(FeatureWrapper wrapper, Iterable<TabixLine> in) {
		super(in,1024);
		this.wrapper=wrapper;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.sf.jannot.tabix.codec.Codec#parse(java.lang.String)
	 */
	@Override
	public Feature parse(TabixLine line) {
		Feature f = lru.get(line);
		if (f != null)
			return f;
		else {

			try {
				// if (arr.length < 9) {
				// arr = GFF3Parser.padGff(arr);
				// }
				Location l = new Location(line.getInt(3), line.getInt(4));
				//String parent = GFF3Parser.extractParent(line.get(8));

				// /* Add to existing feature */
				// if (parent != null && parentMap.containsKey(parent) &&
				// arr[2].equals("CDS")) {
				//
				// parentMap.get(parent).addLocation(l);
				//
				// } else {/* Add as a new feature */
				f = new Feature();
				SortedSet<Location> tmp = new TreeSet<Location>();
				tmp.add(l);
				f.setLocation(tmp);
				char strand = line.get(6).charAt(0);
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
				// f.addQualifier(new Qualifier("seqid", arr[0]));
				f.addQualifier("source", line.get(1));
				f.setType(Type.get(line.get(2)));
				String five = line.get(5);
				if (!(five.length() == 1 && five.charAt(0) == '.') && five.length() != 0)
					f.setScore(Double.parseDouble(five));
				if (line.length() > 8) {
					String[] attributes = line.get(8).split(";");
					for (String s : attributes) {
						String[] pair = s.trim().split("=");
						if (pair.length == 2) {
							String[] values = pair[1].split(",");
							for (String v : values) {
								f.addQualifier(pair[0], v);
							}
						} else
							f.addQualifier("note", pair[0]);
					}
				}
//				wrapper.update(f);
				// if (parent != null && f.type() == Type.get("CDS"))
				// parentMap.put(parent, f);
				// String id = f.singleQualifierValue("id");
				// assert(id!=null);
				// idMap.put(id, f);
				// set.getOrCreateEntry(arr[0]).annotation.add(f);
				// FeatureAnnotation fa =
				// set.getOrCreateEntry(arr[0]).getAnnotation(f.type());
				// fa.add(f);
				return f;
				// }

			} catch (Exception e) {
				e.printStackTrace();
				System.err.println("Could not parse line: " + line);
				return null;
			}
		}
	}

}
