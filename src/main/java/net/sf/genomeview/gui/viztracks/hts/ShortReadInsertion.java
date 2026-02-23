/**
 * %HEADER%
 */
package net.sf.genomeview.gui.viztracks.hts;

import htsjdk.samtools.SAMRecord;

/**
 * 
 * @author Thomas Abeel
 *
 */
class ShortReadInsertion {
	SAMRecord esr;
	int start, len;
}