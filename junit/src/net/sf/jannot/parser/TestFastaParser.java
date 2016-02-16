package net.sf.jannot.parser;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URISyntaxException;

import net.sf.jannot.Entry;
import net.sf.jannot.EntrySet;
import net.sf.jannot.exception.ReadFailedException;
import net.sf.jannot.refseq.Sequence;
import net.sf.jannot.source.DataSource;
import net.sf.jannot.source.DataSourceFactory;
import net.sf.jannot.source.Locator;

import org.junit.Assert;
import org.junit.Test;

import support.DataManager;

/**
 * 
 * @author Thomas Abeel
 * 
 */
public class TestFastaParser {

	private void testFile(File file) {
		try {

			DataSource ds = DataSourceFactory.create(new Locator(file));
			EntrySet es = ds.read();
			// System.out.println(es.firstEntry());
			Assert.assertEquals("TestFasta", es.firstEntry().getID());
			int count = 0;
			for (Entry e : es)
				count++;
			Assert.assertEquals(1, count);

			Sequence d = es.firstEntry().sequence();
			Assert.assertEquals(38, d.size());

			Assert.assertEquals("ACGTACGTAACCGGTTTTGGCCAATGCATGCAAGTTGA", d.stringRepresentation());

		} catch (URISyntaxException e) {
			e.printStackTrace();
			Assert.fail();
		} catch (IOException e) {
			e.printStackTrace();
			Assert.fail();
		} catch (ReadFailedException e) {

			e.printStackTrace();
			Assert.fail();
		}
	}

	@Test
	public void testMiniFasta() {
		File f = DataManager.file("mini.fasta");
		testFile(f);
	}

	@Test
	public void testWrite() {
		File f = DataManager.file("mini.fasta");
		try {
			DataSource ds = DataSourceFactory.create(new Locator(f));
			EntrySet es = ds.read();

			File out = File.createTempFile("unittesting.", ".fasta");
			out.deleteOnExit();

			FileOutputStream fos = new FileOutputStream(out);
			for (Entry e : es)
				new FastaParser().write(fos, e);
			fos.close();

			testFile(out);
			
		

		} catch (URISyntaxException e) {
			e.printStackTrace();
			Assert.fail();
		} catch (IOException e) {
			e.printStackTrace();
			Assert.fail();
		} catch (ReadFailedException e) {

			e.printStackTrace();
			Assert.fail();
		}
	}

}
