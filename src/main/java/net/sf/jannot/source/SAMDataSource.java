/**
 * %HEADER%
 */
package net.sf.jannot.source;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;

import net.sf.jannot.Cleaner;
import net.sf.jannot.DataKey;
import net.sf.jannot.Entry;
import net.sf.jannot.EntrySet;
import net.sf.jannot.exception.ReadFailedException;
import net.sf.jannot.picard.SeekableFileCachedHTTPStream;
import net.sf.jannot.shortread.BAMreads;
import net.sf.samtools.SAMFileReader;
import net.sf.samtools.SAMFileReader.ValidationStringency;
import net.sf.samtools.SAMSequenceDictionary;
import net.sf.samtools.SAMSequenceRecord;
import net.sf.samtools.seekablestream.SeekableFileStream;
import net.sf.samtools.seekablestream.SeekableStream;
import be.abeel.net.URIFactory;

/**
 * 
 * @author Thomas Abeel
 * 
 */
public class SAMDataSource extends DataSource {

	class SAMKey implements DataKey {

		private String string;

		SAMKey(String file) {
			this.string = file;
		}

		@Override
		public String toString() {
			return this.string;
		}

		public int compareTo(DataKey o) {
			return toString().compareTo(toString());
		}

		@Override
		public boolean equals(Object o) {
			return toString().equals(o.toString());
		}

	}

	private SeekableStream content;
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((content == null) ? 0 : content.hashCode());
		result = prime * result + ((index == null) ? 0 : index.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		SAMDataSource other = (SAMDataSource) obj;
		if (content == null) {
			if (other.content != null)
				return false;
		} else if (!content.equals(other.content))
			return false;
		if (index == null) {
			if (other.index != null)
				return false;
		} else if (!index.equals(other.index))
			return false;
		return true;
	}

	/* File containing the BAM index */
	private File index;
	/* Display name for this data */
	private DataKey sourceKey;

	private SAMFileReader sfr = null;
	private long size;

	public SAMFileReader getReader() {
		// System.out.println(content);
		// System.out.println(content.getSource());
		if (sfr == null) {
			SAMFileReader.setDefaultValidationStringency(ValidationStringency.SILENT);
			System.out.println("SDS: "+content+"\t"+index);
			sfr = new SAMFileReader(content, index, false);
			Cleaner.register(sfr, content, deleteIndex ? index : null);

		}
		return sfr;
		// return new SAMFileReader(content, index, false);

	}

	/**
	 * BAM file URL
	 * 
	 * @param url
	 * @throws IOException
	 * @throws ReadFailedException
	 * @throws URISyntaxException
	 */
	private void init(URL url, URL idx) throws IOException, ReadFailedException, URISyntaxException {
		setSourceKey(new SAMKey(url.toString()));
		/* BAM file */
		// content =new SeekableHTTPStream(url);
		content = new SeekableFileCachedHTTPStream(url);
		size = url.openConnection().getContentLength();

		/* Index file */
		File tmpBAI = File.createTempFile("urlbam", ".bai");
		tmpBAI.deleteOnExit();
//		url = URIFactory.url(idx);
		copy(idx.openStream(), tmpBAI);
		index = tmpBAI;
		deleteIndex = true;

	}

	/**
	 * BAM file URL, index is local
	 * 
	 * @param url
	 * @throws IOException
	 * @throws ReadFailedException
	 * @throws URISyntaxException
	 */
	private void init(URL url, File idx) throws IOException, ReadFailedException, URISyntaxException {
		setSourceKey(new SAMKey(url.toString()));
		/* BAM file */
		// content =new SeekableHTTPStream(url);
		content = new SeekableFileCachedHTTPStream(url);
		size = url.openConnection().getContentLength();

		index = idx;


	}
	
	private boolean deleteIndex = false;

	private static void copy(InputStream in, File file) throws IOException {
		OutputStream out = new FileOutputStream(file);

		byte[] buffer = new byte[100000];
		while (true) {
			int amountRead = in.read(buffer);
			if (amountRead == -1) {
				break;
			}
			out.write(buffer, 0, amountRead);

		}
		out.close();

	}

	/**
	 * BAM file
	 * 
	 * @param file
	 * @throws IOException
	 */
	private void init(File file,File index) throws IOException {
		setSourceKey(new SAMKey(file.toString()));
		size = file.length();
		content = new SeekableFileStream(file);
		this.index =index;
	}

	/**
	 * @param data
	 * @param index2
	 */
	public SAMDataSource(Locator data, Locator index) {
		super(data);
		if (data == null || index == null)
			throw new RuntimeException("Either data or index are not provided: " + data + "; " + index);
		if (data.isURL()) {
			try {
				if(index.isURL())
					init(data.url(), index.url());
				else 
					init(data.url(), index.file());
			} catch (MalformedURLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ReadFailedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (URISyntaxException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else {
			try {
				init(data.file(),index.file());
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

	}

	@Override
	public EntrySet read(EntrySet set) throws ReadFailedException {
		if (set == null)
			set = new EntrySet();
		SAMFileReader inputSam = getReader();

		SAMSequenceDictionary tmpDic = inputSam.getFileHeader().getSequenceDictionary();
		for (int i = 0; i < tmpDic.size(); i++) {
			SAMSequenceRecord org = inputSam.getFileHeader().getSequence(i);
			Entry e = set.getOrCreateEntry(org.getSequenceName());
			e.add(getSourceKey(), new BAMreads(this, org.getSequenceName()));
			// e.shortReads.add(this, new BAMreads(this, e));

		}
		return set;
	}

	@Override
	public String toString() {
		return content.toString();
	}

	@Override
	public void finalize() {
		sfr.close();
		if (content instanceof SeekableFileCachedHTTPStream)
			((SeekableFileCachedHTTPStream) content).closeAll();

	}

	private void setSourceKey(DataKey sourceKey) {
		this.sourceKey = sourceKey;
	}

	public DataKey getSourceKey() {
		return sourceKey;
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
		return size;
	}

}
