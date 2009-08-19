/**
 * %HEADER%
 */
package net.sf.genomeview.data.cache;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.net.URL;

import net.sf.jannot.EntrySet;
import net.sf.jannot.exception.ReadFailedException;
import net.sf.jannot.parser.Parser;
import net.sf.jannot.source.URLSource;

public class CachedURLSource extends URLSource {

	public CachedURLSource(URL url) throws IOException {
		super(url, null);

	}

	@Override
	public EntrySet read(EntrySet set) throws ReadFailedException {
		if (!SourceCache.contains(url)) {
			try {
				super.setParser(Parser.detectParser(url.openStream()));
				final PipedInputStream in = new PipedInputStream();
				final PipedOutputStream forParser = new PipedOutputStream(in);

				new Thread(new Runnable() {
					public void run() {
						try {
							OutputStream out = SourceCache.startCaching(url);
							InputStream is=url.openStream();
							byte[] buffer = new byte[100000];
							while (true) {
								
								int amountRead = is.read(buffer);
								if (amountRead == -1) {
									break;
								}
								forParser.write(buffer, 0, amountRead);
								out.write(buffer, 0, amountRead);

							}
							forParser.close();
							out.close();
							SourceCache.finish(url);
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}

					}
				}).start();

				super.setIos(in);
			} catch (Exception e) {
				throw new ReadFailedException(e);

			}
			return super.read(set);
		} else {
			try {
				return SourceCache.get(url).read(set);
			} catch (IOException e) {
				throw new ReadFailedException(e);
			}
		}
	}

}
