package intermittent;

import java.io.File;
import java.io.FileNotFoundException;

import junit.framework.Assert;
import net.sf.genomeview.data.Model;
import net.sf.genomeview.data.Session;

import org.junit.Test;

import support.DataManager;

/**
 * 
 * Hunting for
 * 
 * Aug 28, 2013 6:54:33 PM net.sf.genomeview.data.Session$1 run SEVERE:
 * Something went wrong while loading line: U
 * http://www.broadinstitute.org/software /genomeview/demo/c_elegans//IV.gff.gz
 * from the session file. To recover GenomeView skipped this file.
 * 
 * java.lang.RuntimeException: java.io.EOFException: Unexpected end of ZLIB
 * input stream at be.abeel.io.LineIterator.<init>(LineIterator.java:118) at
 * be.abeel.io.LineIterator.<init>(LineIterator.java:133) at
 * be.abeel.io.LineIterator.<init>(LineIterator.java:90) at
 * net.sf.jannot.parser.Parser.findParser(Parser.java:111) at
 * net.sf.jannot.parser.Parser.detectParser(Parser.java:96) at
 * net.sf.jannot.source.URLSource.init(URLSource.java:35) at
 * net.sf.jannot.source.URLSource.<init>(URLSource.java:44) at
 * net.sf.jannot.source.DataSourceFactory.create(DataSourceFactory.java:69) at
 * net.sf.genomeview.data.DataSourceHelper.load(DataSourceHelper.java:143) at
 * net.sf.genomeview.data.DataSourceHelper.load(DataSourceHelper.java:46) at
 * net.sf.genomeview.data.Session$1.run(Session.java:124) at
 * java.lang.Thread.run(Thread.java:724)
 * 
 * Caused by: java.io.EOFException: Unexpected end of ZLIB input stream at
 * java.util.zip.InflaterInputStream.fill(InflaterInputStream.java:240) at
 * java.util.zip.InflaterInputStream.read(InflaterInputStream.java:158) at
 * java.util.zip.GZIPInputStream.read(GZIPInputStream.java:116) at
 * sun.nio.cs.StreamDecoder.readBytes(StreamDecoder.java:283) at
 * sun.nio.cs.StreamDecoder.implRead(StreamDecoder.java:325) at
 * sun.nio.cs.StreamDecoder.read(StreamDecoder.java:177) at
 * java.io.InputStreamReader.read(InputStreamReader.java:184) at
 * java.io.BufferedReader.fill(BufferedReader.java:154) at
 * java.io.BufferedReader.readLine(BufferedReader.java:317) at
 * java.io.BufferedReader.readLine(BufferedReader.java:382) at
 * be.abeel.io.LineIterator.<init>(LineIterator.java:114)
 */
public class InteractiveTestSessionURL {

	@Test
	public void testSessionURL() {
		File f = DataManager.file("singlegff.gvs");
		Model model = new Model(null);
		try {
			Thread t=Session.loadSession(model, f);
			try {
				t.join();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				Assert.fail();
			}
			try {
				Thread.sleep(10000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				Assert.fail();
			}
			System.out.println(model.entries().firstEntry());
			Assert.assertEquals("IV", ""+model.entries().firstEntry());
		} catch (FileNotFoundException e) {
			Assert.fail();
			e.printStackTrace();
		}
	}

}
