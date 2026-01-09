package net.sf.jannot.refseq;

import java.io.FileInputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.logging.LogManager;
import java.util.logging.Logger;

import org.junit.Test;

import net.sf.jannot.Cleaner;
import net.sf.jannot.EntrySet;
import net.sf.jannot.exception.ReadFailedException;
import net.sf.jannot.source.IndexedFastaDataSource;
import net.sf.jannot.source.Locator;

/**
 * 
 * @author Thomas Abeel
 * 
 */
public class TestFirstNucleotide {

	@Test
	public void testNucleotide() throws MalformedURLException,
			IOException, ReadFailedException, URISyntaxException {
		
		Logger log = Logger.getLogger(TestFirstNucleotide.class
				.getCanonicalName());
		log.info("Loading source");
		Locator l = new Locator(
				"http://genomeview.org/frigg/genome.fasta");
		Locator i = new Locator(
				"http://genomeview.org/frigg/genome.fasta.fai");

		IndexedFastaDataSource ifd = new IndexedFastaDataSource(l, i);
		System.out.println("Reading entries");
		EntrySet es = ifd.read();

		System.out.println("Query");
		for (Character c : es.getEntry("chr1").sequence().get(1, 1000)) {
			System.out.print(c);
		}
		System.out.println();
		System.out.println();

		System.out.println("Query");
		for (Character c : es.getEntry("chr1").sequence().get(-10, 1000)) {
			System.out.print(c);
		}

		Cleaner.exit();

	

	}
}
