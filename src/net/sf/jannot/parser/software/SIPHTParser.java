/**
 * %HEADER%
 */
package net.sf.jannot.parser.software;

import java.io.InputStream;
import java.util.ArrayList;

import cern.colt.Arrays;

import be.abeel.io.LineIterator;
import net.sf.jannot.Entry;
import net.sf.jannot.EntrySet;
import net.sf.jannot.Feature;
import net.sf.jannot.Location;
import net.sf.jannot.MemoryFeatureAnnotation;
import net.sf.jannot.Strand;
import net.sf.jannot.StringKey;
import net.sf.jannot.Type;
import net.sf.jannot.parser.Parser;

/**
 * @author Thomas Abeel
 * 
 */
public class SIPHTParser extends Parser {
	/**
	 * @param stringKey
	 */
	public SIPHTParser(StringKey stringKey) {
		super(stringKey);
	}

	@Override
	public EntrySet parse(InputStream is, EntrySet set) {
		if (set == null)
			set = new EntrySet();
		LineIterator it = new LineIterator(is);
		it.setSkipBlanks(true);
		String id = null;
		Type t = Type.get("SIPHT");
		int count = 0;
		while (it.hasNext() && count < 1) {
			if (it.next().startsWith("~"))
				count++;
		}
		ArrayList<String> header = new ArrayList<String>();
		for (String line : it) {
			if (line.startsWith("~") || line.startsWith("**")) {
				count++;
				continue;
			}
			String[] arr = line.split("\t+",header.size()>0?header.size():0);
			if (count == 1) {
				//System.out.println("header: " + Arrays.toString(arr));
				for (String s : arr) {
					header.add(s.trim());
				}
			}

			if (count == 2 || count == 3) {
				System.out.println("putative: " + Arrays.toString(arr));
				Feature f = new Feature();
				f.setType(t);
				int start = Integer.parseInt(arr[8]);
				int end = Integer.parseInt(arr[9]);
				if (arr[10].equals("<<<"))
					f.setStrand(Strand.REVERSE);
				else
					f.setStrand(Strand.FORWARD);
				f.addLocation(new Location(start,end));
				for(int i=0;i<arr.length;i++){
					if(!header.get(i).equals("|")){
						f.addQualifier(header.get(i), arr[i]);
					}
				}
				
				Entry e=set.getOrCreateEntry(arr[2].split("_")[0]);
				e.getMemoryAnnotation(t).add(f);
				
			}

		}

		return set;
	}
}
