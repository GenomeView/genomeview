/**
 * %HEADER%
 */
package net.sf.genomeview.data;

import java.net.MalformedURLException;
import java.net.URISyntaxException;

import net.sf.genomeview.gui.StaticUtils;
import be.abeel.net.URIFactory;

/**
 * 
 * @author Thomas Abeel
 * 
 */
public class Blast {

	public static void blastn(String header, String seq) {
		go("http://www.ncbi.nlm.nih.gov/blast/Blast.cgi?PROGRAM=blastn&BLAST_PROGRAMS=megaBlast&PAGE_TYPE=BlastSearch&SHOW_DEFAULTS=on&BLAST_SPEC=&LINK_LOC=blasttab&QUERY=>"
				+ header + "\n" + seq);
	}

	public static void blastp(String header, String seq) {
		go("http://www.ncbi.nlm.nih.gov/blast/Blast.cgi?PROGRAM=blastp&BLAST_PROGRAMS=blastp&PAGE_TYPE=BlastSearch&SHOW_DEFAULTS=on&BLAST_SPEC=&LINK_LOC=blasttab&QUERY=>"
				+ header + "\n" + seq);
	}

	public static void blastx(String header, String seq) {
		go("http://www.ncbi.nlm.nih.gov/blast/Blast.cgi?PROGRAM=blastx&BLAST_PROGRAMS=blastx&PAGE_TYPE=BlastSearch&SHOW_DEFAULTS=on&BLAST_SPEC=&LINK_LOC=blasttab&QUERY=>"
				+ header + "\n" + seq);
	}

	private static void go(String url) {
		try {
			StaticUtils.browse(URIFactory.uri(url));
		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
