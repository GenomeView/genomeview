/**
 * %HEADER%
 */
package net.sf.jannot.parser;

import java.io.InputStream;
import java.util.Arrays;

import net.sf.jannot.Entry;
import net.sf.jannot.EntrySet;
import net.sf.jannot.Feature;
import net.sf.jannot.Location;
import net.sf.jannot.MemoryFeatureAnnotation;
import net.sf.jannot.Type;
import be.abeel.io.LineIterator;

/**
 * 
 * @author Thomas Abeel
 * 
 */
public class VCFParser extends Parser {

	enum Variation {
		Match, SingleSubstitution, LongSubstitution, SingleDeletion, LongDeletion, SingleInsertion, LongInsertion;

	}

	/**
	 * @param dataKey
	 */
	VCFParser(String fileName) {
		super(null);
		String[] arr = fileName.replace('\\', '/').split("/");
		setDataKey(Type.get(arr[arr.length - 1]));
	}

	/**
	 * Will return an entry for each unique seq_id
	 */
	@Override
	public EntrySet parse(InputStream is, EntrySet set) {
		if (set == null)
			set = new EntrySet();

		LineIterator it = new LineIterator(is, true, true);

		for (String line : it) {
			String[] arr = line.trim().split("\\s+");
			Entry e = set.getOrCreateEntry(arr[0]);
			MemoryFeatureAnnotation annot = e.getMemoryAnnotation(dataKey);
			Feature newFeature = new Feature();
			if (arr.length < 7)
				throw new RuntimeException("Not sufficient columns " + Arrays.toString(arr));

			/*
			 * Only parse lines that pass the filters
			 */
			String filter = arr[6];
			if (!(filter.equalsIgnoreCase("PASS") || filter.equals(".")))
				continue;

			int pos = Integer.parseInt(arr[1]);

			String id = arr[2];

			newFeature.setQualifier("id", id);

			String ref = arr[3];

			newFeature.setQualifier("ref", ref);

			String multiAlt = arr[4];

			for (String alt : multiAlt.split(",")) {

				Feature f = newFeature.copy();
				f.setQualifier("alt", alt);
				double score = 0;
				if (arr[5].charAt(0) != '.')
					score = Double.parseDouble(arr[5]);
				f.setQualifier("score", "" + score);

				int refLength = ref.length();

				int altLength = alt.length();
				Variation variation = null;

				if (ref.length() == alt.length()) {
					if (alt.equals(".") || ref.equals(alt))
						variation = Variation.Match;
					else {
						if (ref.length() == 1)
							variation = Variation.SingleSubstitution;
						else
							variation = Variation.LongSubstitution;
					}
				} else {
					// assume(ref.length() > 0 && alt.length() > 0)
					if (ref.length() == 1 || alt.length() == 1) {
						int diff = ref.length() - alt.length();
						// if (ref.length() > alt.length())
						if (diff > 1)
							variation = Variation.LongDeletion;
						else if (diff > 0)
							variation = Variation.SingleDeletion;
						else if (diff < -1)
							variation = Variation.LongInsertion;
						else if (diff < 0)
							variation = Variation.SingleInsertion;
						else
							throw new RuntimeException("This is not supposed to happen!");
					} else {
						variation = Variation.LongSubstitution;
					}

				}
				/*
				 * Only include differences, don't load matches
				 */
				if (variation != Variation.Match) {
					int end = pos + refLength - 1;
					int start = pos;
					// if(variation==Variation.SingleSubstitution||variation==Variation.SingleInsertion||variation==Variation.LongInsertion)
					// end=pos;
					if (variation == Variation.SingleDeletion || variation == Variation.LongDeletion) {
						start = pos + 1;

						// end=pos+refLength;

					}

					f.setLocation(new Location(start, end));

					/*
					 * Add winglets for large events
					 */
					if (variation == Variation.LongInsertion) {
						f.addLocation(new Location(pos - altLength / 2, pos - altLength / 2));
						f.addLocation(new Location(pos + altLength / 2, pos + altLength / 2));
					}
					/*
					 * Add additional winglets for unclear calls 
					 */
					if (variation == Variation.LongInsertion && alt.contains("N")) {
						f.addLocation(new Location(pos - (int)((altLength / 2)*1.1), pos - (int)((altLength / 2)*1.1)));
						f.addLocation(new Location(pos + (int)((altLength / 2)*1.1), pos +(int)((altLength / 2)*1.1)));
					}
					
					int delta = altLength - refLength;
					if (variation == Variation.LongSubstitution && refLength < altLength) {
						f.addLocation(new Location(start - delta / 2, start - delta / 2));
						f.addLocation(new Location(end + delta / 2, end + delta / 2));
						if (alt.contains("N")) {
							f.addLocation(new Location(start - (int)((delta / 2)*1.1),start- (int)((delta / 2)*1.1)));
							f.addLocation(new Location(end + (int)((delta / 2)*1.1),end+ (int)((delta / 2)*1.1)));
						}
					}

					f.addQualifier("delta", ""+delta);
					f.setType(Type.get(variation.toString()));
					annot.add(f);
				}
			}
			// lazy val blankFilter=filter.equals(".")

		}
		return set;
	}
}
