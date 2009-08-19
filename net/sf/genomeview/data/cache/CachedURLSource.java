/**
 * %HEADER%
 */
package net.sf.genomeview.data.cache;

import java.io.IOException;
import java.net.URL;

import net.sf.jannot.EntrySet;
import net.sf.jannot.exception.ReadFailedException;
import net.sf.jannot.source.URLSource;

public class CachedURLSource extends URLSource{

	public CachedURLSource(URL url) throws IOException {
		super(url, null);
		
	}

	@Override
	public EntrySet read(EntrySet set) throws ReadFailedException {
		if (!SourceCache.contains(url)) {
			try {
				init();
			} catch (Exception e) {
				throw new ReadFailedException(e);
				
			} 
			//Cache the stuff while reading
			return super.read(set);
		}else{
			return SourceCache.get(url).read(set);
		}
	}

	

}
