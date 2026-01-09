/**
 * %HEADER%
 */
package net.sf.jannot.picard;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.logging.Logger;

import net.sf.jannot.Cleaner;

/**
 * @author Thomas Abeel
 * 
 */
public class BinaryBlob {
	/* Files are each 32 Mb */
	private static final int BLOCKSIZE = 32 * 1024 * 1024;
	/* Each file contains n number of blocks */
	// private static final int BLOCKSPERFILE = 40;
	// private ArrayList<Integer> blocks = new ArrayList<Integer>();

	// private void updateBlocks(long pos, int length) {
	// blocks.clear();
	// long startBlock = pos / BLOCKSIZE;
	// while (startBlock * BLOCKSIZE < pos + length) {
	// blocks.add((int) startBlock);
	// startBlock++;
	//
	// }
	// }

	private File[] bufferFiles;
	private RandomAccessFile[] rafs;
	// private URL url;
	// private SeekableHTTPStream urlstream;
	private Logger log = Logger.getLogger(BinaryBlob.class.getCanonicalName());

	public BinaryBlob(long size) throws IOException {
		bufferFiles = new File[1 + (int) (size / (BLOCKSIZE))];
		rafs = new RandomAccessFile[bufferFiles.length];

		log.fine("Creating BinaryBlob cache files");
		for (int i = 0; i < bufferFiles.length; i++) {
			bufferFiles[i] = File.createTempFile("GenomeView.binaryblob", ".tmp." + i);
			bufferFiles[i].deleteOnExit();
			rafs[i] = new RandomAccessFile(bufferFiles[i], "rwd");
			Cleaner.register(rafs[i], bufferFiles[i]);
		}

		// retrievedBlocks = new BitSet();

	}

	/**
	 * @param i
	 * @param value
	 * @throws IOException
	 */
	public synchronized void putFloat(long pos, float value) throws IOException {
		long fIndex = pos / BLOCKSIZE;
		long fPos = pos % BLOCKSIZE;
		rafs[(int) fIndex].seek(fPos);
		rafs[(int) fIndex].writeFloat(value);

	}

	// /**
	// * @param out
	// * @param start
	// * @param len
	// */
	// public void getFloat(float[] out, int start, int len) {
	// // TODO Auto-generated method stub
	//		
	// }

	// /**
	// * @return
	// */
	// public int size() {
	// return size;
	// }

	/**
	 * @param i
	 * @return
	 * @throws IOException
	 */
	public synchronized float getFloat(int pos) throws IOException {
		int fIndex=(int)(pos/BLOCKSIZE);
		long fPos=pos%BLOCKSIZE;
		if(fPos>=rafs[fIndex].length())
			return 0;
			
		rafs[fIndex].seek(fPos);
		return rafs[fIndex].readFloat();
	}
	// private synchronized void retrieve(long block) throws IOException {
	// try {
	// System.out.println("getting block " + block);
	// /* Download block data */
	// urlstream.seek(block * BLOCKSIZE);
	// byte[] buffer = new byte[BLOCKSIZE];
	// urlstream.read(buffer, 0, BLOCKSIZE);
	//
	// /* Cache */
	// rafs[(int) (block / BLOCKSPERFILE)].seek((block % BLOCKSPERFILE) *
	// BLOCKSIZE);
	// rafs[(int) (block / BLOCKSPERFILE)].write(buffer);
	// retrievedBlocks.set((int) block);
	//
	// } catch (Exception e) {
	// log.log(Level.SEVERE, "Exception during retrieval", e);
	//
	// }
	// }

	// private BitSet retrievedBlocks = null;
	//
	// // private RandomAccessFile raf;

	// public synchronized int read(long position, byte[] buffer, int offset,
	// int length) throws IOException {
	// if (offset < 0 || length < 0 || (offset + length) > buffer.length) {
	// throw new IndexOutOfBoundsException();
	// }
	// updateBlocks(position, length);
	// /* Check whether all blocks we need are cached */
	// for (int block : blocks) {
	// if (!retrievedBlocks.get(block)) {
	// retrieve(block);
	// }
	// }
	//
	// /* Total bytes read */
	// int n = 0;
	//
	// /* First file in which we need to read something */
	// long startFile = position / (BLOCKSIZE * BLOCKSPERFILE);
	//
	// /* Last file in which we need to read something */
	// long endFile = (position + length) / (BLOCKSIZE * BLOCKSPERFILE);
	//
	// /* First file */
	// RandomAccessFile workingRaf = rafs[(int) startFile];
	// position = position - (startFile * BLOCKSIZE * BLOCKSPERFILE);
	// workingRaf.seek(position);
	//
	// /* Available bytes in first file */
	// long avail = BLOCKSIZE * BLOCKSPERFILE - position;
	// if (avail > length)
	// avail = length;
	//
	// // /--> read first chunk
	// while (n < avail) {
	// int count = workingRaf.read(buffer, offset + n, (int) (avail - n));
	// if (count < 0) {
	// throw new EOFException();
	// }
	// n += count;
	// }
	//
	// /* Middle files */
	// for (long i = startFile + 1; i < endFile; i++) {
	// avail = BLOCKSIZE * BLOCKSPERFILE;
	// workingRaf = rafs[(int) i];
	// workingRaf.seek(0);
	// while (n < avail) {
	// int count = workingRaf.read(buffer, offset + n, (int) (avail - n));
	// if (count < 0) {
	// throw new EOFException();
	// }
	// n += count;
	// }
	// }
	//
	// /* Last file */
	// if (startFile < endFile) {
	// workingRaf = rafs[(int) endFile];
	// workingRaf.seek(0);
	// while (n < length) {
	// workingRaf = rafs[(int) endFile];
	// workingRaf.seek(0);
	// int count = workingRaf.read(buffer, offset + n, length - n);
	// if (count < 0) {
	// throw new EOFException();
	// }
	// n += count;
	// }
	// }
	//
	// return n;
	// }

}
