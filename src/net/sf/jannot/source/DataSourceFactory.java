/**
 * %HEADER%
 */
package net.sf.jannot.source;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.logging.Logger;

import net.sf.jannot.bigwig.BigWigDataSource;
import net.sf.jannot.exception.ReadFailedException;
import net.sf.jannot.source.cache.CachedURLSource;
import net.sf.jannot.tabix.IndexedFeatureFile;
import net.sf.jannot.tdf.TDFDataSource;

/**
 * 
 * @author Thomas Abeel
 * 
 */
public class DataSourceFactory {
	private static Logger log = Logger.getLogger(DataSourceFactory.class.getCanonicalName());

	public enum Sources {
		LOCALFILE, URL;// , DAS;
		@Override
		public String toString() {
			switch (this) {
			case URL:
				return "URL";
			case LOCALFILE:
				return "Local file";
				// case DIRECTORY:
				// return "Directory";
				// case DAS:
				// return "DAS server";
			}
			return null;
		}
	}

	public static boolean disableURLCaching = true;
	
	public static DataSource create(Locator locator) throws URISyntaxException, IOException, ReadFailedException {
		return create(locator,null);
		
	}

	public static DataSource create(Locator data, Locator index) throws URISyntaxException, IOException,
			ReadFailedException {
		log.info("Data: " + data);
		log.info("Index: " + index);
		if (data.isURL()) {
			SSL.certify(data.url());
		}

		if (data.isTDF()) {
			return new TDFDataSource(data);
		}
		
		if(data.isBigWig())
			return new BigWigDataSource(data);

		if (index == null) {
			log.info("Could not find index");
			if (data.isURL()) {
				if (disableURLCaching) {
					log.info("Loading as regular URLSource");
					return new URLSource(data.url());
				} else {
					log.info("Loading as CachedURLSource");
					return new CachedURLSource(data.url());
				}
			} else {
				log.info("Loading as FileSource");
				return new FileSource(data.file());
			}
		} else {

			if (data.isBAM()) {
				return new SAMDataSource(data, index);
			}
			if (data.isFasta())
				return new IndexedFastaDataSource(data, index);

			if (data.isTabix())
				return new IndexedFeatureFile(data, index);

			if (data.isMaf())
				return new IndexedMAFDataSource(data, index);
		}
		log.severe("Could not construct data source for \n\t" + data + "\n\t" + index);
		return null;

	}


	

}
