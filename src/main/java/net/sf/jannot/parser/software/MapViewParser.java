/**
 * %HEADER%
 */
package net.sf.jannot.parser.software;

import java.io.InputStream;
import java.io.OutputStream;

import be.abeel.io.LineIterator;
import be.abeel.util.TimeInterval;

import net.sf.jannot.DataKey;
import net.sf.jannot.Entry;
import net.sf.jannot.EntrySet;
import net.sf.jannot.exception.ReadFailedException;
import net.sf.jannot.parser.Parser;
import net.sf.jannot.shortread.BasicShortRead;
import net.sf.jannot.shortread.MemoryReadSet;
import net.sf.jannot.shortread.ShortRead;
import net.sf.jannot.source.DataSource;

public class MapViewParser extends Parser {

	/**
	 * @param dataKey
	 */
	public MapViewParser(DataKey dataKey) {
		super(dataKey);
		// TODO Auto-generated constructor stub
	}

	@Override
	public EntrySet parse(InputStream is, EntrySet set) {
		if (set == null)
			set = new EntrySet();
		LineIterator it = new LineIterator(is);

		long time = System.currentTimeMillis();
		

//		set.setMute(true);
		int count = 0;
		for (String line : it) {
			String[] arr = line.split("\t");
			Entry entry = set.getOrCreateEntry(arr[1]);
			
//			if(e.shortReads.getReadGroup(source)==null){
//				e.shortReads.add(source, new MemoryReadSet());
//			}
			if(!entry.contains(dataKey)){
				entry.add(dataKey, new MemoryReadSet());
			}
			if (!arr[14].matches(".*[nN].*")) {
				MemoryReadSet mrs=(MemoryReadSet) entry.get(dataKey);
				//mrs.add(new BasicShortRead(arr[14].toCharArray(), Integer.parseInt(arr[2]), arr[3].charAt(0) == '+'));
			} else {
				// System.out.println("discarding: "+arr[0]);
				count++;
			}

		}
		System.out.println("Discarded: " + count + " short reads because of ambiguity");
//		set.setMute(false);
		
		return set;
	}

	

}
