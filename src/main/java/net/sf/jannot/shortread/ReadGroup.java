/**
 * %HEADER%
 */
package net.sf.jannot.shortread;

import net.sf.jannot.Data;
import net.sf.samtools.SAMRecord;

/**
 * 
 * @author Thomas Abeel
 * 
 */
public abstract class ReadGroup implements Data<SAMRecord> {

	/**
	 * Returns the maximum length of a read. In case this information is not
	 * available, it is sufficient to return the maximum length so far
	 * encountered in the get() or get(start,end) queries.
	 */
	public abstract int readLength();

	/**
	 * 
	 * Get the second read in a mate-pair.
	 * 
	 * @param one
	 *            the read for which to find the mate
	 * @return the mate
	 */
	public abstract SAMRecord getSecondRead(SAMRecord one);

	/**
	 * Returns the maximum length of a pair of reads.
	 * 
	 * The length of a pair is defined as the distance between the start of the
	 * first read and the end of the second.
	 * 
	 * In case this information is not available, it is sufficient to return the
	 * maximum pair length so far encountered in the get() or get(start,end)
	 * queries.
	 */
	public abstract int getPairLength();

	public boolean canSave() {
		return false;
	}

	/**
	 * @param second
	 * @return
	 */
	public abstract SAMRecord getFirstRead(SAMRecord second) ;

}
