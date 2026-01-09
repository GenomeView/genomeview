/**
 * %HEADER%
 */
package net.sf.jannot.mafix;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import net.sf.samtools.util.BlockCompressedInputStream;
import net.sf.samtools.util.BlockCompressedOutputStream;
import net.sf.samtools.seekablestream.SeekableStream;
import be.abeel.io.LineIterator;

/**
 * @author thpar 
 * @author Thomas Abeel
 * 
 */
public class MafixFactory {


	/**
	 * Read the original MAF file and write it to the block zipped stream. Add
	 * every new Alignment Block to the index.
	 * @throws URISyntaxException 
	 */
	public static void generateBlockZippedFile(InputStream is, File out) throws FileNotFoundException, IOException, URISyntaxException {
		BufferedInputStream bis=new BufferedInputStream(is, 1024*1024);
		LineIterator it=new LineIterator(bis);

		BlockCompressedOutputStream bcos = new BlockCompressedOutputStream(out);

		for(String line:it){
			bcos.write(line.getBytes());
			bcos.write(System.getProperty("line.separator").getBytes());
		
		}

		bcos.close();

	}
	/**
	 * Reads a line from the inputstream until a \n or \n\r is encountered. The
	 * file pointer will be positioned at the beginning of the next line after
	 * this read.
	 * 
	 * @param bcos
	 * 
	 * @return a string with the read characters. Null when no more characters
	 *         are being read.
	 */
	private static String readLine(BlockCompressedInputStream bcos) throws IOException {

		int readChar = bcos.read();
		if (readChar < 0)
			return null; // EOF

		StringBuffer lineBuffer = new StringBuffer();
		long readAheadCharPos = 0;
		int readAheadChar = 0;

		readAheadCharPos = bcos.getFilePointer();
		readAheadChar = bcos.read();
		
		while (readAheadChar >= 0 && readChar != '\n' && readChar != '\r') {
			lineBuffer.append((char)readChar);

			readChar = readAheadChar;

			readAheadCharPos = bcos.getFilePointer();
			// int available =bcos.available();
			readAheadChar = bcos.read();
			
		}
		if (readChar >= 0 && readAheadChar >= 0) {
			if (readAheadCharPos >= 0 && !(readChar == '\r' && readAheadChar == '\n')) {
				// the readAhead ate a char from the next line.
				// cough it up...
				bcos.seek(readAheadCharPos);
			}
		}
		
		return lineBuffer.toString();
	}

	public static void generateIndex(SeekableStream is, File out) throws IOException {
		System.out.println("Generating index");
		MAFIndex idx = new MAFIndex();

		/**
		 * position of a block in the zipped file
		 */
		long blockStart = 0;
		/**
		 * Does the next line contain the nuc location for human?
		 */
		boolean nextLineHuman = false;

		/**
		 * Name of the reference chromosome we're reading
		 */
		String thisChrom = new String();

		/**
		 * Are we currently inside a MA block?
		 */
		boolean inBlock = false;

		/**
		 * Start position of this MA
		 */
		int nucPosition = 0;
		int alignmentLength = 0;
		int srcSize = -1;
		BlockCompressedInputStream bcos = new BlockCompressedInputStream(is);

		bcos.seek(0);
		long beginLinePointer = bcos.getFilePointer();
//		long lastLinePointer = beginLinePointer;

		List<String> blockSpecies = new ArrayList<String>();
		StringBuffer blockStrands = new StringBuffer();

		String line = readLine(bcos);
		while (line != null) {

			// ignore comments
			if (!line.startsWith("#")) {

				// split on whitespace
				String[] columns = line.split("\\s+");

				// when encountering an a-line, save position and mark next line
				// as
				// containing human coordinates
				if (columns[0].equals("a")) {

					inBlock = true;
					blockStart = beginLinePointer;
					blockSpecies = new ArrayList<String>();
					blockStrands = new StringBuffer();
					nextLineHuman = true;
				} else if (columns[0].equals("s")) {
					if (nextLineHuman) {
						nucPosition = Integer.parseInt(columns[2]);
						alignmentLength = Integer.parseInt(columns[3]);
						srcSize = Integer.parseInt(columns[5]);
						nextLineHuman = false;
						// add the human chromosome name
						thisChrom = columns[1];
					} else {
						blockSpecies.add(columns[1]);
					}
					idx.addSpecies(thisChrom, columns[1]);
					blockStrands.append(columns[4]);

				}
//				System.out.println("Strands: "+blockStrands.toString());
				if (inBlock && line.isEmpty()) {
					MAFEntry mafEntry = new MAFEntry();
					nucPosition = fixPosition(nucPosition, blockStrands.charAt(0), srcSize, alignmentLength);
					mafEntry.setNucStart(nucPosition);
					mafEntry.setAlignmentLength(alignmentLength);
					mafEntry.setOffsetPair(blockStart);
					mafEntry.setSpecies(idx.encodeSpecies(thisChrom,blockSpecies,blockStrands.toString()));

					idx.addEntry(thisChrom, mafEntry);
					inBlock = false;
				}
			}

//			lastLinePointer = beginLinePointer;
			beginLinePointer = bcos.getFilePointer();
			line = readLine(bcos);
		}
		if (inBlock) {
			MAFEntry mafEntry = new MAFEntry();
			mafEntry.setNucStart(nucPosition);
			mafEntry.setAlignmentLength(alignmentLength);
			mafEntry.setOffsetPair(blockStart);
			mafEntry.setSpecies(idx.encodeSpecies(thisChrom,blockSpecies,blockStrands.toString()));
			idx.addEntry(thisChrom, mafEntry);
			inBlock = false;
		}
		idx.writeToFile(out);
	}

	/**
	 * @param nucPosition
	 * @param srcSize
	 * @param charAt
	 * @return
	 */
	private static int fixPosition(int start, char strand, int srcSize, int noNucleotides) {
		if (strand == '+')
			return start;
		else if (strand == '-')
			return srcSize - start - noNucleotides;
		throw new RuntimeException("Could not fix position...");
	}

//	/**
//	 * Creates a block zipped file from the original MAF, generates an mfi index
//	 * file and writes this index to a zipfile in the same directory.
//	 * @param seekableStream 
//	 * 
//	 * @param mafFile
//	 * @throws FileNotFoundException
//	 * @throws IOException
//	 * @throws URISyntaxException 
//	 */
//	public static void compressAndIndex(InputStream seekableStream, File file) throws FileNotFoundException, IOException, URISyntaxException {
//		new MafixFactory(seekableStream,file);
//
//	}

}
