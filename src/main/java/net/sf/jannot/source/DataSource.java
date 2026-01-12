/**
 * %HEADER%
 */
package net.sf.jannot.source;

import java.io.File;
import java.io.IOException;
import java.net.URL;

import net.sf.jannot.EntrySet;
import net.sf.jannot.exception.ReadFailedException;

/**
 * 
 * @author Thomas Abeel
 * 
 */
public abstract class DataSource implements Comparable<DataSource> {

	protected Locator locator = null;

	protected DataSource(Locator l) {
		this.locator = l;
	}

	@Override
	public int compareTo(DataSource o) {
		return this.toString().compareTo(o.toString());
	}

	public EntrySet read() throws ReadFailedException {
		return read(null);
	}

	public abstract EntrySet read(EntrySet add) throws ReadFailedException;

	public abstract boolean isIndexed();

	public abstract long size();

	/**
	 * try to read from given sources
	 * 
	 * @param url  a {@link URL}
	 * @param file a {@link File}
	 * @return the file length, or if null, the url content length, or 0 if if the
	 *         url can also not be read
	 */
	protected static long size(URL url, File file) {
		long size = 0;
		if (url != null)
			try {
				size = url.openConnection().getContentLength();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		if (file != null)
			size = file.length();

		return size;

	}

	public Locator getLocator() {
		return locator;
	}
}
