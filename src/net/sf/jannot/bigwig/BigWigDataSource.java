/**
 * %HEADER%
 */
package net.sf.jannot.bigwig;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;

import net.sf.jannot.Entry;
import net.sf.jannot.EntrySet;
import net.sf.jannot.StringKey;
import net.sf.jannot.exception.ReadFailedException;
import net.sf.jannot.picard.SeekableFileCachedHTTPStream;
import net.sf.jannot.source.DataSource;
import net.sf.jannot.source.Locator;
import net.sf.jannot.tabix.PileupWrapper;
import net.sf.samtools.seekablestream.SeekableFileStream;
import net.sf.samtools.seekablestream.SeekableStream;

import org.broad.igv.bbfile.BBFileReader;
import org.broad.igv.tdf.TDFReader;

import cern.colt.Arrays;

/**
 * 
 * Data source for BigWig files.
 * 
 * @author Thomas Abeel
 * 
 */
public class BigWigDataSource extends DataSource {

	private SeekableStream s = null;

	/**
	 * @param file
	 * @throws URISyntaxException
	 * @throws ReadFailedException
	 * @throws IOException 
	 * @throws MalformedURLException 
	 */
	public BigWigDataSource(Locator l) throws ReadFailedException, URISyntaxException, MalformedURLException, IOException {
		super(l);
		if (!l.isURL())
			s = new SeekableFileStream(l.file());
		else
			s = new SeekableFileCachedHTTPStream(l.url());
		tr = new BBFileReader(l,s);

	}

	private BBFileReader tr = null;

	// /**
	// * @param url
	// * @throws URISyntaxException
	// * @throws ReadFailedException
	// * @throws IOException
	// */
	// public TDFDataSource(URL url) throws ReadFailedException,
	// URISyntaxException, IOException {
	// super(new Locator(url.toString()));
	// s=new SeekableFileCachedHTTPStream(url);
	// tr=TDFReader.getReader(s);
	// }

	// /**
	// * @param string
	// * @throws URISyntaxException
	// * @throws ReadFailedException
	// */
	// private TDFDataSource(String string) throws ReadFailedException,
	// URISyntaxException {
	// tr = TDFReader.getReader(string);
	//
	// }

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.sf.jannot.source.DataSource#read(net.sf.jannot.EntrySet)
	 */
	@Override
	public EntrySet read(EntrySet set) throws ReadFailedException {

		if (set == null)
			set = new EntrySet();
		// SAMFileReader inputSam = getReader();
//		System.out.println(tr.getDatasetNames());
//		System.out.println(tr.getDataMin());
//		System.out.println(tr.getDataMax());
//		System.out.println(tr.getGroupNames());
//		System.out.println(tr.getWindowFunctions());
//		System.out.println(Arrays.toString(tr.getTrackNames()));

		Set<String> chrs = new HashSet<String>();

		for (String s : tr.getChromosomeNames()) {
//			String[] arr = s.split("/");
			chrs.add(s);
			Entry e = set.getOrCreateEntry(s);
			e.add(new StringKey(tr.getLocator().toString()), new BigWigData(s, tr));
		}
//		chrs.remove("All");
//		for (String chr : chrs) {
//			
//
//		}

		// for (String name : index.names()) {
		// Entry e = set.getOrCreateEntry(name);
		// // try {
		// try {
		// e.setSequence(new FaidxData(index, content, name));
		// } catch (Exception ex) {
		// System.err.println("Faidx error URL: " + url);
		// System.err.println("Faidx error file: " + file);
		// throw new ReadFailedException(ex);
		//
		// }
		// }
		return set;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.sf.jannot.source.DataSource#isIndexed()
	 */
	@Override
	public boolean isIndexed() {
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.sf.jannot.source.DataSource#size()
	 */
	@Override
	public long size() {
		return s.length();
	}
}
