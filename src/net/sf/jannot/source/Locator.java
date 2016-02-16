/**
 * %HEADER%
 */
package net.sf.jannot.source;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.util.logging.Logger;

import net.sf.jannot.tabix.TabixWriter;
import net.sf.jannot.tabix.TabixWriter.Conf;

import net.sf.samtools.seekablestream.SeekableStream;
import net.sf.samtools.seekablestream.SeekableStreamFactory;
import be.abeel.net.URIFactory;

/**
 * 
 * Intelligent descriptor of a file. Can either be local or remote on a server.
 * Can be indexed or plain. This locator can describe the type of data that is
 * in the file.
 * 
 * @author Thomas Abeel
 * 
 */
public class Locator {
	private static Logger log = Logger.getLogger(Locator.class.getCanonicalName());
	private String locator;
	private long length = -1;
	private boolean exists = false;
	private boolean streamCompressed = false;
	private boolean blockCompressed = false;
	private String ext;
	private long lastModified=-1;

	@Override
	public String toString() {
		return locator;
	}

	/**
	 * Removes the index extension from the file name
	 */
	public void stripIndex() {
		if (locator.endsWith(".mfi") || locator.endsWith(".tbi") 
				|| locator.endsWith(".fai")) {
			locator = locator.substring(0, locator.length() - 4);
			init();
		}
		if(locator.endsWith(".bai")){
			locator = locator.substring(0, locator.length() - 4);
			if(!locator.endsWith(".bam"))
				locator+=".bam";
			init();
		}
		
	}

	public Locator(File f){
		this(f.toString());
	}
	public Locator(String l) {
		if (l.startsWith("file://")){
			l = l.substring(7);
		}
		this.locator = l.trim();
		init();
	}

	/**
	 * 
	 */
	private void init() {
		String[] arr = locator.toString().toLowerCase().split("\\.");
		initExt(arr);

		if (isURL()) {
			initURL();
		} else
			initFile();

	}

	public boolean isStreamCompressed() {
		return streamCompressed;
	}

	public boolean isBlockCompressed() {
		return blockCompressed;
	}

	/**
	 * @param arr
	 */
	private void initExt(String[] arr) {
		streamCompressed = false;
		blockCompressed = false;
		ext = arr[arr.length - 1];
		if (arr[arr.length - 1].equals("bgz")) {
			ext = arr[arr.length - 2];
			blockCompressed = true;
		}
		if (arr[arr.length - 1].equals("gz")) {
			ext = arr[arr.length - 2];
			streamCompressed = true;
		}

	}

	/**
	 * 
	 */
	private void initURL() {
		try {
			log.fine("Checking: " + locator);
			URLConnection conn = URIFactory.url(locator).openConnection();
			conn.setUseCaches(false);
			log.info(conn.getHeaderFields().toString());
			String header = conn.getHeaderField(null);
			if (header.contains("404")) {
				log.info("404 file not found: " + locator);
				return;
			}
			if (header.contains("500")) {
				log.info("500 server error: " + locator);
				return;
			}

			if (conn.getContentLength() > 0) {
				byte[] buffer = new byte[50];

				conn.getInputStream().read(buffer);
				/*
				 * This is not supposed to happen, except with badly configured
				 * CMS that take over
				 */
				if (new String(buffer).trim().startsWith("<!DOCTYPE"))
					return;
			} else if (conn.getContentLength() == 0) {
				exists = true;
				return;
			}

			exists = true;
			length = conn.getContentLength();
			lastModified=conn.getLastModified();
		} catch (Exception ioe) {
			System.err.println(ioe);
			// ioe.printStackTrace();
		}

	}

	/**
	 * 
	 */
	private void initFile() {
		File tmp=new File(locator);
		exists = tmp.exists();
		if (exists){
			length =tmp.length();
			lastModified=tmp.lastModified();
		}

	}

	/**
	 * @param locator
	 * @return
	 */
	public String getPostfix() {

		if (isTabix())
			return "tbi";
		if (isFasta())
			return "fai";
		if (isBAM())
			return "bai";
		if (isMaf())
			return "mfi";
		return null;
	}

	public boolean isURL() {
		return locator.startsWith("http://") || locator.startsWith("https://");
	}

	public long length() {
		return length;
	}

	public boolean exists() {
		return exists;
	}

	/**
	 * @return
	 * @throws URISyntaxException
	 * @throws MalformedURLException
	 */
	public URL url() throws MalformedURLException, URISyntaxException {
		return URIFactory.url(locator);

	}

	public File file() {
		return new File(locator);
	}

	/**
	 * @return
	 */
	public boolean isWebservice() {

		return locator.indexOf('&') >= 0 || locator.indexOf('?') >= 0;

	}

	/**
	 * @return
	 */
	public boolean isTDF() {

		return ext.equals("tdf");
	}

	public boolean isWig() {
		return ext.equals("wig");
	}

	public boolean isBigWig() {
		return ext.equals("bw") || ext.equals("bigwig");
	}

	public boolean isTabix() {
		return ext.equals("vcf")||ext.equals("gff") || ext.equals("gff3") || ext.equals("bed") || ext.equals("tsv")
				|| ext.equals("pileup") || ext.equals("swig") || ext.equals("tab");

	}

	public boolean requiresIndex() {
		return (isMaf() && isBlockCompressed()) || isBAM()|| ext.equals("tsv") || ext.equals("pileup")
				|| ext.equals("swig") || ext.equals("tab");
	}

	public boolean recommendedIndex() {
		return requiresIndex() || isFasta() || isMaf();
	}

	public boolean supportsIndex() {
		return recommendedIndex() || requiresIndex()  ||isVCF()|| ext.equals("gff") || ext.equals("gff3") || ext.equals("bed");
	}

	public boolean isBAM() {
		return ext.equals("bam");

	}

	public boolean isFasta() {
		return ext.equals("fasta") || ext.equals("fa") || ext.equals("fas") || ext.equals("con") || ext.equals("fna")|| ext.equals("tfa");
	}

	public boolean isMaf() {
		return ext.equals("maf");

	}
	
	public boolean isVCF() {
		return ext.equals("vcf");

	}

	public boolean isPileup() {
		return ext.equals("pileup");
	}
	
	
	public long lastModified(){
		return lastModified;
	}
	/**
	 * @return
	 */
	public Conf getTabixConfiguration() {
		if (!isTabix())
			return null;
		if (ext.equals("gff") || ext.equals("gff3")) {
			return TabixWriter.GFF_CONF;
		}
		if (ext.equals("bed")) {
			return TabixWriter.BED_CONF;
		}

		if(ext.equals("vcf")){
			return TabixWriter.VCF_CONF;
		}
		Conf out = new Conf(0, 0, 0, 0, '#', 0);

		if (ext.equals("pileup") || ext.equals("swig") || ext.equals("tab") || ext.equals("tsv")) {
			out.chrColumn = 1;
			out.startColumn = 2;
			out.endColumn = 2;
		}

		return out;

	}

	/**
	 * @return
	 * @throws IOException 
	 * @throws FileNotFoundException
	 * @throws URISyntaxException
	 * @throws MalformedURLException
	 */
	public SeekableStream stream() throws IOException, URISyntaxException {
		if (!isURL())
			return SeekableStreamFactory.getInstance().getStreamFor(this.file().toString());
		else
			return SeekableStreamFactory.getInstance().getStreamFor(this.url());
	}

	public boolean isAnyCompressed() {
		return streamCompressed || blockCompressed;
	}

	public String getName() throws MalformedURLException, URISyntaxException {
		if (isURL()) {
			int slashIndex = url().getPath().lastIndexOf('/');
			return url().getPath().substring(slashIndex + 1);

		} else
			return file().getName();

	}

	
}
