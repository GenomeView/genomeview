/**
 * %HEADER%
 */
package net.sf.jannot.parser;

import java.io.InputStream;

import net.sf.jannot.DataKey;
import net.sf.jannot.EntrySet;
import net.sf.jannot.Location;
import net.sf.jannot.Strand;
import net.sf.jannot.SyntenicBlock;
import be.abeel.io.LineIterator;

/**
 * This parser read syntenic blocks from an 8 column file that has the following
 * formatting.
 * 
 * 
 * Synthenic block:
 * 
 * Starts with the line
 * 
 * <pre>
 * gvheader:syntenic
 * </pre>
 * 
 * 8 columns
 * 
 * 0,1,2,3 name,start,end,strand of reference
 * 
 * 4,5,6,7 name,start,end,strand of informant
 * 
 * 
 * @author Thomas Abeel
 * 
 */
public class SyntenicParser extends Parser {

	/**
	 * @param dataKey
	 */
	public SyntenicParser(DataKey dataKey) {
		super(dataKey);
		// TODO Auto-generated constructor stub
	}

	@Override
	public EntrySet parse(InputStream is, EntrySet set) {
		// List<Entry>list=new ArrayList<Entry>();
		if (set == null)
			set = new EntrySet();
		// Map<String,Entry>mapping=new HashMap<String, Entry>();

		LineIterator it = new LineIterator(is);
		it.setSkipBlanks(true);
		it.setSkipComments(true);
		it.addCommentIdentifier("gvheader");
		for (String line : it) {
			String[] arr = line.split("\t");
			Location refLoc = new Location(Integer.parseInt(arr[1]), Integer.parseInt(arr[2]));
			Strand refStrand = Strand.fromSymbol(arr[3].charAt(0));
			Location informantLoc = new Location(Integer.parseInt(arr[5]), Integer.parseInt(arr[6]));
			Strand informantStrand = Strand.fromSymbol(arr[7].charAt(0));

			SyntenicBlock sb = new SyntenicBlock(arr[0], arr[4], refLoc, informantLoc, refStrand, informantStrand);
			set.syntenic.add(sb);
			// FIXME set.getOrCreateEntry(arr[0], source);
			SyntenicBlock sbf = sb.flip();
			set.syntenic.add(sbf);
			// FIXME set.getOrCreateEntry(arr[4], source);

		}
		return set;
	}

}
