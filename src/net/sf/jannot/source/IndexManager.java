/**
 * %HEADER%
 */
package net.sf.jannot.source;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.util.logging.Logger;

import net.sf.jannot.indexing.Faidx;
import net.sf.jannot.mafix.MafixFactory;
import net.sf.jannot.tabix.TabixWriter;
import net.sf.jannot.tabix.TabixWriter.Conf;
import net.sf.samtools.BAMIndexer;
import net.sf.samtools.SAMFileHeader;
import net.sf.samtools.SAMFileReader;
import net.sf.samtools.SAMRecord;
import net.sf.samtools.SAMRecordIterator;
import net.sf.samtools.seekablestream.SeekableFileStream;
import be.abeel.security.MD5Tools;

/**
 * @author Thomas Abeel
 * 
 */
public class IndexManager {
	private static Logger log = Logger.getLogger(IndexManager.class.getCanonicalName());
	public static File cacheDir = new File(System.getProperty("user.home")+"/.genomeview/index");

	public static Locator getIndex(Locator locator) {
		String postfix = locator.getPostfix();
		if (postfix == null) {
			log.info("JAnnot does not know the index format for this locator: " + locator);
		}
		log.info("Trying to find local index");
		Locator out = findLocalIndex(locator);
		if (out == null) {
			log.info("Trying to find remote index");
			out = findRemoteIndex(locator);
		}

		return out;

	}

	/**
	 * @param locator
	 * @param postfix
	 * @return
	 */
	private static Locator findRemoteIndex(Locator locator) {
		Locator index = new Locator(locator + "." + locator.getPostfix());

		if (!index.exists()||index.length()==0)
			index = null;

		/* Special case handling of bam files */
		if (index == null) {
			index = new Locator(locator.toString().substring(0, locator.toString().length() - 4) + "." + locator.getPostfix());
			if (!index.exists()||index.length()==0)
				index = null;
		}
		return index;
	}

	/**
	 * @param locator
	 * @param postfix
	 * @return
	 */
	private static Locator findLocalIndex(Locator locator) {
		if (!cacheDir.exists())
			cacheDir.mkdir();
		Locator idx = cacheIndex(locator);
		if (!idx.exists()||idx.length()==0)
			idx = null;
		return idx;
	}

	private static Locator cacheIndex(Locator locator) {
		return new Locator(cacheDir + "/" + MD5Tools.md5(locator.toString()) + "." + locator.getPostfix());
	}

	/**
	 * A potentially long-running method that will create an index for the
	 * provided locator
	 * 
	 * @param locator
	 * @throws URISyntaxException
	 * @throws IOException
	 * @throws MalformedURLException
	 */
	public static boolean createIndex(Locator locator) throws MalformedURLException, IOException, URISyntaxException {
		log.info("Creating index for "+locator);
		if (!cacheDir.exists())
			cacheDir.mkdir();

		Locator idx = cacheIndex(locator);

		if(!idx.isURL()){
			Locator tmp=new Locator(locator.toString() + "." + locator.getPostfix());
			File f=tmp.file();
			try{
			f.createNewFile();
			}catch(IOException e){
				log.warning("Tried to make index file, failed: "+e);
			}
			if(f.exists()&&f.canWrite())
				idx=tmp;
					
		}
		
		if (idx.exists())
			log.info("Index already exists and will be overwritten!!!");

		if(locator.isMaf()){
			MafixFactory.generateIndex(locator.stream(), idx.file());
			
		}
		
		if (locator.isBAM()) {

			InputStream ios = locator.stream();

			SAMFileReader sfr = new SAMFileReader(ios);
			sfr.enableFileSource(true);

			SAMFileHeader head = sfr.getFileHeader();

			File tmpOutput= new File(idx+".tmp");
			BAMIndexer bix = new BAMIndexer(tmpOutput, head);

			SAMRecordIterator sir = sfr.iterator();
			while (sir.hasNext()) {
				SAMRecord n = sir.next();
				bix.processAlignment(n);

			}
			bix.finish();
			idx.file().delete();
			boolean rename=tmpOutput.renameTo(idx.file());
			log.info("Did rename succeed? "+rename);
			System.out.println(rename);
			return rename;
//			return true;
			// public BAMIndexer(final File output, SAMFileHeader fileHeader) {
			//
			// numReferences = fileHeader.getSequenceDictionary().size();
			// indexBuilder = new BAMIndexBuilder(fileHeader);
			// outputWriter = new BinaryBAMIndexWriter(numReferences, output);
			// }

		}
		
		if (locator.isTabix()) {
			Conf c = locator.getTabixConfiguration();
			try {
				TabixWriter tw = new TabixWriter(locator, c);

				tw.createIndex(idx);
				return true;
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}

		if (locator.isFasta()) {

			Faidx.index(locator, idx);
			return true;
		}

		return false;

	}

	/**
	 * @param data
	 * @return
	 */
	public static boolean canBuildIndex(Locator data) {
		boolean tbx = data.isTabix() && data.isBlockCompressed();
		return data.isBAM() || tbx || (data.isFasta() && !data.isAnyCompressed()) || (data.isMaf() && data.isBlockCompressed());
	}

}
