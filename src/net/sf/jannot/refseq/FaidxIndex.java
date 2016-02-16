/**
 * %HEADER%
 */
package net.sf.jannot.refseq;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;

import be.abeel.io.LineIterator;
/**
 * 
 * @author Thomas Abeel
 *
 */
public class FaidxIndex {

	
	class IndexEntry{
		long len,start,lineLen,byteLen;

		/**
		 * @param len
		 * @param start
		 * @param lineLen
		 * @param byteLen
		 */
		IndexEntry(long len, long start, long lineLen, long byteLen) {
			super();
			this.len = len;
			this.start = start;
			this.lineLen = lineLen;
			this.byteLen = byteLen;
		}
	}
	private HashMap<String,IndexEntry>mapping=new HashMap<String, IndexEntry>();
	private String[]names;


	public FaidxIndex(InputStream is) {
		LineIterator it = new LineIterator(is);
		ArrayList<String>lines=new ArrayList<String>();
		for (String line : it) {
			lines.add(line);
		}
		names=new String[lines.size()];
		
		int idx=0;
		for(String line:lines){
			String[] arr = line.split("\t");
			try {
				names[idx++]=arr[0];
				IndexEntry ie=new IndexEntry(Long.parseLong(arr[1]), Long.parseLong(arr[2]), Long.parseLong(arr[3]), Long.parseLong(arr[4]));
				mapping.put(arr[0],ie);

				
			} catch (ArrayIndexOutOfBoundsException e) {
				System.err.println("Could not parse: " + line);
				while(it.hasNext()){
					System.err.println(it.next());
				}
			}
		}

	}

	public String[] names() {
		return names;
	}
	
	public IndexEntry get(String name){
		return mapping.get(name);
	}

}
