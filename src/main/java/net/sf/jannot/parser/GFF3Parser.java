/**
 * %HEADER%
 */
package net.sf.jannot.parser;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import org.apache.commons.collections.map.Flat3Map;

import net.sf.jannot.DataKey;
import net.sf.jannot.Entry;
import net.sf.jannot.EntrySet;
import net.sf.jannot.Feature;
import net.sf.jannot.FeatureAnnotation;
import net.sf.jannot.Location;
import net.sf.jannot.MemoryFeatureAnnotation;
import net.sf.jannot.Strand;
import net.sf.jannot.Type;
import be.abeel.io.LineIterator;

/**
 * 
 * @author Thomas Abeel
 * 
 */
public class GFF3Parser extends Parser {

	/**
	 * @param dataKey
	 */
	GFF3Parser() {
		super(null);
	
	}

	/**
	 * Will return an entry for each unique seq_id
	 */
	@Override
	public EntrySet parse(InputStream is, EntrySet set) {
		if (set == null)
			set = new EntrySet();

		/* Keeps track of which features have the same ID */
		Map<String, Feature> parentMap = new HashMap<String, Feature>();
		LineIterator it = new LineIterator(is, true, true);

		Map<String, String> quals = new Flat3Map();
		for (String line : it) {
			String[] arr = line.trim().split("\t");

			try {
				if (arr.length < 9) {
					arr = padGff(arr);
				}

				quals.clear();
				parseQualifiers(arr[8], quals);

				Location l = new Location(Integer.parseInt(arr[3]), Integer.parseInt(arr[4]));
				String parent = extractParent(quals, arr[2], arr[0]);

				/* Add to existing feature */
				if (parent != null && parentMap.containsKey(parent)) {

					parentMap.get(parent).addLocation(l);

				} else {/* Add as a new feature */
					Feature f = new Feature();
					f.setLocation(l);
					char strand = arr[6].charAt(0);
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
					f.addQualifier("source", arr[1]);
					f.setType(Type.get(arr[2]));
					if (!(arr[5].length() == 1 && arr[5].charAt(0) == '.') && arr[5].length() != 0)
						f.setScore(Double.parseDouble(arr[5]));
					for (java.util.Map.Entry<String, String> me : quals.entrySet()) {
						f.addQualifier(me.getKey(), me.getValue());
					}
					// String[] attributes = arr[8].split(";");
					// for (String s : attributes) {
					// String[] pair = s.trim().split("=");
					// if (pair.length == 2) {
					// String[] values = pair[1].split(",");
					// for (String v : values) {
					// f.addQualifier(new Qualifier(pair[0], v));
					// }
					// } else
					// f.addQualifier(new Qualifier("note", pair[0]));
					// }
					if (parent != null)
						parentMap.put(parent, f);
					// String id = f.singleQualifierValue("id");
					// assert(id!=null);
					// idMap.put(id, f);
					// set.getOrCreateEntry(arr[0]).annotation.add(f);
					MemoryFeatureAnnotation fa = set.getOrCreateEntry(arr[0]).getMemoryAnnotation(f.type());
					fa.add(f);
				}

			} catch (Exception e) {
				e.printStackTrace();
				System.err.println("Could not parse line: " + Arrays.toString(arr));
			}

		}
		parentMap = null;
		return set;
	}

	/**
	 * @param string
	 * @param quals
	 */
	private void parseQualifiers(String qq, Map<String, String> quals) {
		String[] arr = qq.split(";");
		for (String s : arr) {
			int i = s.indexOf('=');
			String key = "note";
			if (i >= 0)
				key = s.substring(0, i);
			key = key.trim();

			String value = s.substring(i + 1, s.length());
			value = value.trim();

			if (quals.containsKey(key))
				quals.put(key, quals.get(key) + "," + value);
			else
				quals.put(key, value);
		}

	}

	public static String[] padGff(String[] arr) {
		String[] newArray = new String[9];
		int fullToken = 0;
		for (String token : arr) {
			newArray[fullToken++] = token;
		}
		for (int emptyToken = fullToken; emptyToken < newArray.length; emptyToken++) {
			newArray[emptyToken] = ".";
		}
		return newArray;
	}

	public static String extractParent(Map<String, String> quals, String type, String chr) {
		String out = quals.get("ID");
		if (out == null)
			out = quals.get("Parent");
		if (out != null)
			out = chr + "$$" + type + "$$" + out;
		return out;

	}

	// private static String tryParent(String line, String key) {
	// if (!line.contains(key))
	// return null;
	// String[] arr = line.split(";");
	// for (String s : arr) {
	// if (s.trim().startsWith(key))
	// return s.trim().split("=")[1].trim();
	// }
	// return null;
	// }

	@Override
	public void write(OutputStream os, Entry entry, DataKey[] dks) {

		PrintWriter out = new PrintWriter(os);
		for (DataKey dk : dks) {
			if (entry.get(dk) instanceof FeatureAnnotation) {
				FeatureAnnotation fa = (FeatureAnnotation) entry.get(dk);
				for (Feature f : fa.get()) {

					this.fixID(f);
					for (int i = 0; i < f.location().length; i++)
						out.println(line(entry, f, entry.getID(), i));
				}
			}
		}
		out.flush();
	}

	private String line(Entry e, Feature f, String acc, int locIdx) {
		StringBuffer out = new StringBuffer();
		out.append(e.getID() + "\t");
		out.append(f.qualifier("source") + "\t");
		out.append(f.type() + "\t");
		out.append(f.location()[locIdx].start() + "\t");
		out.append(f.location()[locIdx].end() + "\t");
		out.append(f.getScore() + "\t");
		out.append(f.strand().symbol() + "\t.\t");
		StringBuffer qualifiers = new StringBuffer();
		for (String s : f.getQualifiersKeys()) {
			if (!s.equals("source") && !s.equals("seqid")) {
				qualifiers.append(";" + s + "=");
				qualifiers.append(f.qualifier(s));

			}

		}
		if (qualifiers.length() > 0)
			out.append(qualifiers.substring(1));
		else
			out.append("no qualifiers");
		return out.toString();
	}

	/**
	 * value to be used for assigning random IDs. Initialized randomly and then
	 * incremented with one with every next ID.
	 */
	private int randomID=new Random(System.currentTimeMillis()).nextInt();

	
	/**
	 * Set a temporary ID if needed, to make sure multi-exon CDS's have a common
	 * ID to refer to.
	 * 
	 * @param feat
	 */
	private void fixID(Feature feat) {
		Set<String> quals = feat.getQualifiersKeys();
		if (!quals.contains("ID")) {
			if (quals.contains("Parent")) {
				feat.setQualifier("ID", "Child_of_" + feat.qualifier("Parent"));
			} else {
				String rand = String.format("%08d", randomID++);
				feat.setQualifier("ID", "Random_ID_" + rand);
			}
		}
	}

}
