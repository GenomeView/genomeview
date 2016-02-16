/**
 * %HEADER%
 */
package net.sf.jannot.parser;

import java.io.InputStream;

import net.sf.jannot.DataKey;
import net.sf.jannot.Entry;
import net.sf.jannot.EntrySet;
import net.sf.jannot.StringKey;
import net.sf.jannot.wiggle.FloatArrayWiggle;
import be.abeel.io.LineIterator;
import cern.colt.list.FloatArrayList;

public class BedGraphParser extends Parser {

	/**
	 * @param dataKey
	 */
	public BedGraphParser(DataKey datakey) {
		super(datakey);

	}

	@Override
	public EntrySet parse(InputStream is, EntrySet set) {
		if (set == null)
			set = new EntrySet();
		LineIterator it = new LineIterator(is);
		it.setSkipComments(true);
		it.setCommentIdentifier("#");
		it.addCommentIdentifier("browser");
		it.addCommentIdentifier("track");

		FloatArrayList values = new FloatArrayList();
		String last = "";
		Entry e = null;
		for (String line : it) {
			String[] arr = line.split("\t");
			int start = Integer.parseInt(arr[1]);
			int end = Integer.parseInt(arr[2]);
			if (!last.equals(arr[0])) {
				last = arr[0];
				
				if (e != null){
					e.add(dataKey, new FloatArrayWiggle(values.elements()));
					System.out.println("Adding: "+e+"\t"+values.size());
					values=new FloatArrayList();
				}
				e = set.getOrCreateEntry(arr[0]);
			}
			float val = Float.parseFloat(arr[3]);
			/* Make sure the array is big enough */
			if(end>values.size())
				values.setSize(end);
			for (int i = start; i < end; i++) {
				values.set(i, val);
			}

		}
		
		return set;
	}


}
