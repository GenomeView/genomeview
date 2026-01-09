/**
 * %HEADER%
 */
package net.sf.jannot.parser.software;

import java.io.InputStream;

import net.sf.jannot.DataKey;
import net.sf.jannot.Entry;
import net.sf.jannot.EntrySet;
import net.sf.jannot.exception.ReadFailedException;
import net.sf.jannot.parser.Parser;
import net.sf.jannot.refseq.MemorySequence;
import net.sf.jannot.shortread.BasicShortRead;
import net.sf.jannot.shortread.MemoryReadSet;
import net.sf.jannot.utils.SequenceTools;
import be.abeel.io.LineIterator;

/**
 * A short read parser for Broad data
 * 
 * @author Thomas Abeel
 * 
 */
public class BroadSolexa extends Parser {

	/**
	 * @param dataKey
	 */
	public BroadSolexa(DataKey dataKey) {
		super(dataKey);
		
	}

	@Override
	public EntrySet parse(InputStream is, EntrySet set) {
		if (set == null)
			set = new EntrySet();
		LineIterator it = new LineIterator(is);

		long time=System.currentTimeMillis();
		
		/* This parser assumes that header and sequences lines alternate */
		int mapStart = -1;
		boolean forward = false;
		Entry entry = null;
//		set.setMute(true);
		
		for (String line : it) {
			
			if (line.startsWith(">")) {
				try {
					String[] arr = line.split(" ");
					/* Mapping start in Broad short read format is zero based, correct for it*/
					String[]arr3=arr[3].split("\\.");
					mapStart = Integer.parseInt(arr3[1]);
					forward = arr[2].equals("fw");
					
					entry = set.getOrCreateEntry(arr3[0]);
//					if(entry.shortReads.getReadGroup(source)==null){
//						entry.shortReads.add(source, new MemoryReadSet());
//					}
					if(entry==null)
					 {
						throw new ReadFailedException("There is no reference sequence loaded for this short read: "+arr[3]);
					// current.description.setPrimaryAccessionNumber(line.substring(1).split(" ")[0].split("\t")[0]);
					}
					if(!entry.contains(dataKey)){
						entry.add(dataKey, new MemoryReadSet());
					}
				} catch (Exception e) {
					e.printStackTrace();
					System.out.println("Offending line: " + line);
					return set;
				} finally {
				}
			} else {
				MemoryReadSet mrs=(MemoryReadSet) entry.get(dataKey);
				//if(forward)
					//FIXME mrs.add(new BasicShortRead(line.toCharArray(), mapStart, forward));
			//	else
					//FIXME mrs.add(new BasicShortRead(SequenceTools.reverseComplement(new MemorySequence(new StringBuffer(line))).getSequence().toCharArray(), mapStart, forward));
			}
		}
//		set.setMute(false);
		
		return set;
	}

	

}
