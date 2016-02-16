/**
 * %HEADER%
 */
package net.sf.jannot.picard;

import java.io.EOFException;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.sf.jannot.Cleaner;
import net.sf.jannot.exception.ReadFailedException;

import net.sf.samtools.seekablestream.SeekableHTTPStream;
import net.sf.samtools.seekablestream.SeekableStream;
import be.abeel.net.URIFactory;

/**
 * @author Thomas Abeel
 */
public class SeekableFileCachedHTTPStream extends net.sf.samtools.seekablestream.SeekableHTTPStream {

	private Logger log = Logger.getLogger(SeekableFileCachedHTTPStream.class.getCanonicalName());

	class Cache {
		private ArrayList<Integer> blocks = new ArrayList<Integer>();

		private void updateBlocks(long pos, int length) {
			blocks.clear();
			long startBlock = pos / BLOCKSIZE;
			while (startBlock * BLOCKSIZE < pos + length) {
				blocks.add((int) startBlock);
				startBlock++;

			}
		}

		private File[] bufferFiles;
		private RandomAccessFile[] rafs;
		// private URL url;
		private SeekableHTTPStream urlstream;

		private Cache(URL url) throws IOException, ReadFailedException {
			urlstream = new SeekableHTTPStream(url);
			long len=urlstream.length();
			log.fine("Reported size "+len +" for "+url);
			bufferFiles = new File[1+(int) (len / (BLOCKSIZE * BLOCKSPERFILE))];
			rafs = new RandomAccessFile[bufferFiles.length];
			
			log.fine("Creating "+bufferFiles.length+" HTTP cache files for "+url);
			for (int i = 0; i < bufferFiles.length; i++) {
				bufferFiles[i] = File.createTempFile("GenomeView.urlbuffer", ".tmp." + i);
				bufferFiles[i].deleteOnExit();
				rafs[i] = new RandomAccessFile(bufferFiles[i], "rwd");
				Cleaner.register(rafs[i], bufferFiles[i]);
			}

			retrievedBlocks = new BitSet();

		}

		private synchronized void retrieve(long block) throws IOException {
			try {
				log.log(Level.FINE,"getting block " + block);
				/* Download block data */
				urlstream.seek(block * BLOCKSIZE);
				byte[] buffer = new byte[BLOCKSIZE];
				urlstream.read(buffer, 0, BLOCKSIZE);

				/* Cache */
				rafs[(int) (block / BLOCKSPERFILE)].seek((block % BLOCKSPERFILE) * BLOCKSIZE);
				rafs[(int) (block / BLOCKSPERFILE)].write(buffer);
				retrievedBlocks.set((int) block);

			} catch (Exception e) {
				log.log(Level.SEVERE, "Exception during retrieval rafs.len="+rafs.length+", url="+urlstream.getSource(), e);

			}
		}

		private BitSet retrievedBlocks = null;

		// private RandomAccessFile raf;

		public synchronized int read(long position, byte[] buffer, int offset, int length) throws IOException {
			if (offset < 0 || length < 0 || (offset + length) > buffer.length) {
				throw new IndexOutOfBoundsException();
			}
			updateBlocks(position, length);
			/* Check whether all blocks we need are cached */
			for (int block : blocks) {
				if (!retrievedBlocks.get(block)) {
					retrieve(block);
				}
			}

			/* Total bytes read */
			int n = 0;

			/* First file in which we need to read something */
			long startFile = position / (BLOCKSIZE * BLOCKSPERFILE);

			/* Last file in which we need to read something */
			long endFile = (position + length) / (BLOCKSIZE * BLOCKSPERFILE);

			/* First file */
			RandomAccessFile workingRaf = rafs[(int) startFile];
			position = position - (startFile * BLOCKSIZE * BLOCKSPERFILE);
			workingRaf.seek(position);

			/* Available bytes in first file */
			long avail = BLOCKSIZE * BLOCKSPERFILE - position;
			if (avail > length)
				avail = length;

			// /--> read first chunk
			while (n < avail) {
				int count = workingRaf.read(buffer, offset + n, (int) (avail - n));
				if (count < 0) {
					throw new EOFException();
				}
				n += count;
			}

			/* Middle files */
			for (long i = startFile + 1; i < endFile; i++) {
				avail=BLOCKSIZE*BLOCKSPERFILE;
				workingRaf = rafs[(int) i];
				workingRaf.seek(0);
				while (n < avail ) {
					int count = workingRaf.read(buffer, offset + n, (int) (avail - n));
					if (count < 0) {
						throw new EOFException();
					}
					n += count;
				}
			}

			/* Last file */
			if (startFile < endFile) {
				workingRaf = rafs[(int) endFile];
				workingRaf.seek(0);
				while (n < length) {
					workingRaf = rafs[(int) endFile];
					workingRaf.seek(0);
					int count = workingRaf.read(buffer, offset + n, length - n);
					if (count < 0) {
						throw new EOFException();
					}
					n += count;
				}
			}

			return n;
		}

	}

	private long position = 0;

	private Cache cache;
	private static final int BLOCKSIZE = 256 * 1024;
	/* Each file contains n number of blocks */
	private static final int BLOCKSPERFILE = 40;

	public SeekableFileCachedHTTPStream(URL url) throws IOException, ReadFailedException {
		super(url);
		cache = new Cache(url);

	}

	private SeekableFileCachedHTTPStream(URL url, Cache c) {
		super(url);
		this.cache = c;
	}

	public void seek(long position) {
		this.position = position;
	}

	public synchronized int read(byte[] buffer, int offset, int length) throws IOException {
		int n = cache.read(position, buffer, offset, length);
		position += n;
		return n;

	}

	/**
	 * Closes this SeekableFileCachedHTTPStream and its entire lineage.
	 * 
	 * @throws IOException
	 */
	public void closeAll() {
		try {
			for (RandomAccessFile raf : cache.rafs)
				raf.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public SeekableStream stream() {
		try {
			return new SeekableFileCachedHTTPStream(URIFactory.url(super.getSource()), cache);
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}
}
