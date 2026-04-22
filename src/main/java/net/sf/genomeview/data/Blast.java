/**
 * %HEADER%
 */
package net.sf.genomeview.data;

import net.sf.genomeview.gui.StaticUtils;
import tudelft.utilities.logging.Reporter;

/**
 * Returns query strings that can be used in
 * {@link StaticUtils#browse(java.net.URI, Reporter)}. By returning strings, not
 * URI or URL, we avoid all kind of exceptions that
 * {@link StaticUtils#browse(String, Reporter)} can handle for us.
 * 
 * @author Thomas Abeel
 * 
 */
public class Blast {

	public static String blastn(String header, String seq) {
		return ("http://www.ncbi.nlm.nih.gov/blast/Blast.cgi?PROGRAM=blastn&BLAST_PROGRAMS=megaBlast&PAGE_TYPE=BlastSearch&SHOW_DEFAULTS=on&BLAST_SPEC=&LINK_LOC=blasttab&QUERY=>"
				+ header + "\n" + seq);
	}

	public static String blastp(String header, String seq) {
		return ("http://www.ncbi.nlm.nih.gov/blast/Blast.cgi?PROGRAM=blastp&BLAST_PROGRAMS=blastp&PAGE_TYPE=BlastSearch&SHOW_DEFAULTS=on&BLAST_SPEC=&LINK_LOC=blasttab&QUERY=>"
				+ header + "\n" + seq);
	}

	public static String blastx(String header, String seq) {
		return ("http://www.ncbi.nlm.nih.gov/blast/Blast.cgi?PROGRAM=blastx&BLAST_PROGRAMS=blastx&PAGE_TYPE=BlastSearch&SHOW_DEFAULTS=on&BLAST_SPEC=&LINK_LOC=blasttab&QUERY=>"
				+ header + "\n" + seq);
	}

}
