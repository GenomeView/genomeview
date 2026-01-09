/**
 * %HEADER%
 */
package net.sf.jannot.tabix;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Logger;

import net.sf.jannot.Entry;
import net.sf.jannot.EntrySet;
import net.sf.jannot.StringKey;
import net.sf.jannot.Type;
import net.sf.jannot.exception.ReadFailedException;
import net.sf.jannot.picard.LineBlockCompressedInputStream;
import net.sf.jannot.source.DataSource;
import net.sf.jannot.source.Locator;
import net.sf.samtools.util.BlockCompressedInputStream;
import be.abeel.util.LRUCache;

/**
 * 
 * @author Thomas Abeel
 * 
 */
public class IndexedFeatureFile extends DataSource {
	// FIXME a lot of duplicated stuff from CachingQueryReader with respect to
	// the tiling caching...

	public String source() {
		return data.toString();
	}

	@Override
	public String toString() {
		return data.toString();
	}

	/**
	 * Index constructed from a binary index file
	 */
	private TabIndex idx;

	/**
	 * Compressed data file.
	 */
	private Locator data;
	// private Locator index;

	private long size;

	public TabIndex getIndex() {
		return idx;
	}

	// /**
	// * Reads an index file and constructs an index object.
	// *
	// * @param indexFile
	// * the compressed index file
	// *
	// * @throws IOException
	// */
	// public IndexedFeatureFile(File compressedFile) throws IOException {
	// this(compressedFile, 8000, 50);
	// }
	//
	// public IndexedFeatureFile(URL compressedURL) throws IOException,
	// URISyntaxException {
	// this(compressedURL, 8000, 50);
	// }

	// /**
	// * Reads an index file and constructs an index object.
	// *
	// *
	// * @param compressedFile
	// * the compressed, indexed data file
	// * @throws IOException
	// */
	// private IndexedFeatureFile(File compressedFile, int tileSize, int tiles)
	// throws IOException {
	// File indexFile = new File(compressedFile.toString() + ".tbi");
	// this.compFile = compressedFile;
	// this.size = compressedFile.length();
	// BlockCompressedInputStream in = new
	// BlockCompressedInputStream(indexFile);
	// setup(in, tileSize, tiles);
	// }

	// /**
	// * Reads an index file and constructs an index object.
	// *
	// * @param indexFile
	// * the compressed index file
	// * @param compressedFile
	// * the compressed, indexed data file
	// * @throws IOException
	// * @throws URISyntaxException
	// */
	// private IndexedFeatureFile(URL compressedFile, int tileSize, int tiles)
	// throws IOException, URISyntaxException {
	// URL indexFile = URIFactory.url(compressedFile.toString() + ".tbi");
	// this.size = compressedFile.openConnection().getContentLength();
	// this.compFile = compressedFile;
	// BlockCompressedInputStream in = new
	// BlockCompressedInputStream(indexFile);
	// setup(in, tileSize, tiles);
	// }

	/**
	 * @param data
	 * @param index
	 * @throws IOException
	 * @throws URISyntaxException
	 */
	public IndexedFeatureFile(Locator data, Locator index) throws IOException, URISyntaxException {
		super(data);
		this.data = data;
		// this.index = index;
		this.size = data.length();
		BlockCompressedInputStream in = null;
		if (index.isURL()) {
			in = new BlockCompressedInputStream(index.url());
		} else
			in = new BlockCompressedInputStream(index.file());
		setup(in, 8000, 50);
	}

	private void setup(BlockCompressedInputStream in, int tileSize, int tiles) throws IOException {
		BinaryCodec bc = new BinaryCodec(in);
		this.tileSize = tileSize;
		this.maxTileCount = tiles;
		// read magic
		byte[] buffer = new byte[4];
		bc.readBytes(buffer);

		// read number of sequences
		int n = bc.readInt();

		// create new index object
		idx = new TabIndex(n);
		idx.magic = new String(buffer);

		// read header info and write to idx
		idx.preset = bc.readUInt();
		idx.sc = bc.readUInt();
		idx.bc = bc.readUInt();
		idx.ec = bc.readUInt();
		// System.out.println("Read EC: "+idx.ec)
		idx.meta = (char) bc.readUInt();

		// skip line
		bc.readUInt();

		// create buffer to contain sequence names
		int namesLen = bc.readInt();
		buffer = new byte[namesLen];
		bc.readBytes(buffer);

		// parse names and add to index
		StringBuffer word = new StringBuffer();
		for (byte letter : buffer) {
			if (letter != 0) {
				word.append((char) letter);
			} else {
				idx.names.add(new String(word));
				word = new StringBuffer();
			}
		}

		for (int i = 0; i < idx.size(); i++) {
			long binSize = bc.readUInt();

			for (int j = 0; j < binSize; j++) {
				long key = bc.readUInt();
				ArrayList<Pair64> p = new ArrayList<Pair64>();
				idx.index[i].put((int) key, p);

				int numberOfPairs = bc.readInt();
				p.ensureCapacity(numberOfPairs);
				for (int k = 0; k < numberOfPairs; k++) {
					p.add(new Pair64(bc.readLong(), bc.readLong()));
				}
			}

			int numberOfOffsets = bc.readInt();

			idx.linIndex[i].ensureCapacity(numberOfOffsets);
			for (int j = 0; j < numberOfOffsets; j++) {
				idx.linIndex[i].add(bc.readLong());
			}
			// System.out.println("Linidex: "+idx.names.get(i)+"\t"+idx.linIndex[i]);
		}

		in.close();

	}

	/**
	 * Translate REG to BINS.
	 * 
	 * @param beg
	 * @param end
	 * @param list
	 * @return
	 */
	private ArrayList<Short> reg2bins(int beg, int end) {
		/*
		 * Recap:
		 * 
		 * From C manual: When shifting to the right for unsigned int, bits fall
		 * off the least significant end, and 0's are shifted in from the most
		 * significant end. This is also known as logical right shift (logical
		 * shifts shift in 0's). If this occurs, then you divide by 2^K (if
		 * you're shifting K bits)
		 * 
		 * Note that in the C version, beg and end are UNSIGNED ints. So if in
		 * Java we want to bitshift to the right, we explicitly have to say to
		 * add zeros to the left by using ">>>".
		 */

		// warning, k is an int, assigned to a short...
		ArrayList<Short> list = new ArrayList<Short>();
		list.ensureCapacity(MAX_BIN);
		int k;
		--end;
		list.add((short) 0);
		for (k = 1 + (beg >>> 26); k <= 1 + (end >>> 26); ++k)
			list.add((short) k);
		for (k = 9 + (beg >>> 23); k <= 9 + (end >>> 23); ++k)
			list.add((short) k);
		for (k = 73 + (beg >>> 20); k <= 73 + (end >>> 20); ++k)
			list.add((short) k);
		for (k = 585 + (beg >>> 17); k <= 585 + (end >>> 17); ++k)
			list.add((short) k);
		for (k = 4681 + (beg >>> 14); k <= 4681 + (end >>> 14); ++k)
			list.add((short) k);
		list.trimToSize();
		return list;
	}

	private boolean is_overlap(int beg, int end, int rbeg, int rend) {
		return (rend >= beg && rbeg <= end);
	}

	final int MAX_BIN = 37450;
	final int TAD_LIDX_SHIFT = 14;

	// preset masks
	final int TI_PRESET_GENERIC = 0;
	final int TI_PRESET_SAM = 1;
	final int TI_PRESET_VCF = 2;
	final int TI_FLAG_UCSC = 0x10000;

	// private String sequence;
	//
	// private int start;
	//
	// private int end;

	// private int entriesFound = 0;

	private ArrayList<Pair64> get_chunk_coordinates(int tid, int beg, int end) {

		ArrayList<Short> bins = reg2bins(beg, end);

		HashMap<Integer, ArrayList<Pair64>> index = idx.index[tid];

		long min_off = (beg >> TAD_LIDX_SHIFT >= idx.linIndex[tid].size()) ? 0 : idx.linIndex[tid]
				.get(beg >> TAD_LIDX_SHIFT);

		ArrayList<Pair64> off = new ArrayList<Pair64>();

		for (int bin : bins) {
			if (index.containsKey(bin)) {
				ArrayList<Pair64> p = index.get(bin);
				for (Pair64 pair : p) {
					if (pair.end > min_off)
						off.add(pair);
				}
			}
		}
		// if not a single bin matches... don't bother any further
		if (off.size() == 0)
			return off;

		Collections.sort(off);
		// resolve completely contained adjacent blocks
		int i, l;
		for (i = 1, l = 0; i < off.size(); ++i) {
			if (off.get(l).end < off.get(i).end) {
				off.set(++l, off.get(i));
			}
		}
		// the position of l is the last entry that matters
		// so the number of entries is l+1
		int n_off = l + 1;

		// resolve overlaps between adjacent blocks; this may happen due to the
		// merge in indexing
		for (i = 1; i < n_off; ++i) {
			if (off.get(i - 1).end >= off.get(i).start) {
				off.get(i - 1).end = off.get(i).start;
			}
		}

		// merge adjacent blocks
		for (i = 1, l = 0; i < n_off; ++i) {
			if (off.get(l).end >>> 16 == off.get(i).start >>> 16) {
				off.get(l).end = off.get(i).end;
			} else {
				off.set(++l, off.get(i));
			}
		}
		n_off = l + 1;

		// we possibly have some leftover blocks now at the end of the arraylist
		// only the first n_off blocks should be used, so copy these to a new
		// ArrayList.
		ArrayList<Pair64> new_off = new ArrayList<Pair64>();
		new_off.ensureCapacity(n_off);
		for (i = 0; i < n_off; i++) {
			new_off.add(off.get(i));
		}

		return new_off;
	}

	private ArrayList<TabixLine> empty = new ArrayList<TabixLine>();
	private Logger log = Logger.getLogger(IndexedFeatureFile.class.toString());
	private int tileSize;

	static class Tile {

		private boolean loaded = false;
		// private List<TabixLine> containedRecords;
		private int end;
		private List<TabixLine> overlappingRecords;
		private int start;
		private int tileNumber;

		Tile(int tileNumber, int start, int end) {
			this.tileNumber = tileNumber;
			this.start = start;
			this.end = end;
			// containedRecords = new ArrayList<TabixLine>(16000);
			overlappingRecords = new ArrayList<TabixLine>();
		}

		/**
		 * @return the tileNumber
		 */
		public int getTileNumber() {
			return tileNumber;
		}

		/**
		 * @param tileNumber
		 *            the tileNumber to set
		 */
		public void setTileNumber(int tileNumber) {
			this.tileNumber = tileNumber;
		}

		/**
		 * @return the start
		 */
		public int getStart() {
			return start;
		}

		/**
		 * @param start
		 *            the start to set
		 */
		public void setStart(int start) {
			this.start = start;
		}

		// /**
		// * @return the containedRecords
		// */
		// public List<TabixLine> getContainedRecords() {
		// return containedRecords;
		// }
		//
		// /**
		// * @param containedRecords
		// * the containedRecords to set
		// */
		// public void setContainedRecords(List<TabixLine> containedRecords) {
		// this.containedRecords = containedRecords;
		// }

		/**
		 * @return the overlappingRecords
		 */
		public List<TabixLine> getOverlappingRecords() {
			return overlappingRecords;
		}

		/**
		 * @param overlappingRecords
		 *            the overlappingRecords to set
		 */
		public void setOverlappingRecords(List<TabixLine> overlappingRecords) {
			this.overlappingRecords = overlappingRecords;
		}

		/**
		 * @return the loaded
		 */
		public boolean isLoaded() {
			return loaded;
		}

		/**
		 * @param loaded
		 *            the loaded to set
		 */
		public void setLoaded(boolean loaded) {
			this.loaded = loaded;
		}
	}

	private int cachedChr = -1;
	private int maxTileCount;
	private LRUCache<Integer, Tile> cache;

	private List<Tile> getTiles(int tid, int startTile, int endTile) throws IOException, URISyntaxException {

		if (tid != cachedChr) {
			cache = new LRUCache<Integer, Tile>(maxTileCount);
			cachedChr = tid;
		}

		List<Tile> tiles = new ArrayList<Tile>(endTile - startTile + 1);
		List<Tile> tilesToLoad = new ArrayList<Tile>(endTile - startTile + 1);

		for (int t = startTile; t <= endTile; t++) {
			Tile tile = cache.get(t);

			if (tile == null) {
				int start = t * tileSize;
				int end = start + tileSize;

				tile = new Tile(t, start, end);
				cache.put(t, tile);
			}

			tiles.add(tile);
			// The current tile is loaded, load any preceding tiles we have
			// pending
			if (tile.isLoaded()) {
				if (tilesToLoad.size() > 0) {
					loadTiles(tid, tilesToLoad);
				}
				tilesToLoad.clear();
			} else {
				tilesToLoad.add(tile);
			}
		}

		if (tilesToLoad.size() > 0) {
			loadTiles(tid, tilesToLoad);
		}

		return tiles;
	}

	private void loadTiles(int tid, List<Tile> tiles) throws IOException, URISyntaxException {

		int start = tiles.get(0).start;
		int end = tiles.get(tiles.size() - 1).end;
		ArrayList<TabixLine> iter;

		// log.info("Loading tiles in range : " + start + " - " + end);
		// int alignmentCount = 0;
		// long t0 = System.currentTimeMillis();

		// iter = ds.getReader().query(seq, start, end, false);
		iter = readRawRange(tid, start, end);

		// while (iter.hasNext()) {
		for (TabixLine record : iter) {
			// SAMRecord record = iter.next();
			// System.out.println("Processing: " + record.line());
			// Range of tile indices that this alignment contributes to.
			int aStart = record.beg;// record.getAlignmentStart();
			int aEnd = record.end;// getAlignmentEnd();// record.getEnd();
			int idx0 = Math.max(0, (aStart - start) / tileSize);
			int idx1 = Math.min(tiles.size() - 1, (record.end - start) / tileSize);

			// Loop over tiles this read overlaps
			for (int i = idx0; i <= idx1; i++) {
				Tile t = tiles.get(i);

				// if ((aStart >= t.start) && (aEnd < t.end)) {
				// t.containedRecords.add(record);
				// } else
				if ((aEnd >= t.start) && (aStart < t.end)) {
					t.overlappingRecords.add(record);
				}
			}

			// alignmentCount++;
		}
		// log.info("Read count="+SeekableFileStream.readCount);

		for (Tile t : tiles) {
			t.setLoaded(true);
		}
	}

	private ArrayList<TabixLine> readRange(int tid, int beg, int end) throws IOException, URISyntaxException {
		int startTile = (beg + 1) / tileSize;
		int endTile = end / tileSize; // <= inclusive
		List<Tile> tiles = getTiles(tid, startTile, endTile);

		if (tiles.size() == 0) {
			return empty;
		}
		// System.out.println("ReadRange: " + beg + "\t" + end + "\t" +
		// startTile + "\t" + endTile + "\t" + tileSize);
		// Count total # of records
		int recordCount = tiles.get(0).getOverlappingRecords().size();
		for (Tile t : tiles) {
			recordCount += t.getOverlappingRecords().size();
		}

		ArrayList<TabixLine> alignments = new ArrayList<TabixLine>(recordCount);
		// alignments.addAll(tiles.get(0).getOverlappingRecords());
		for (Tile t : tiles) {
			// System.out.println("Tile: " + t.getContainedRecords());
			// FIXME may introduce duplicates, although it shouldn't
			alignments.addAll(t.getOverlappingRecords());
		}

		// for (TabixLine tl : alignments)
		// System.out.println("\t" + tl.line());

		return alignments;// new TiledIterator(start, end, alignments);
	}

	/**
	 * Read a range from the compressed GFF-file.
	 * 
	 * @param tid
	 *            the sequence number (0 based)
	 * @param beg
	 *            begin of range
	 * @param end
	 *            end of range
	 * @return A list of Strings, one GFF entry a line.
	 * @throws URISyntaxException
	 */
	private ArrayList<TabixLine> readRawRange(int tid, int beg, int end) throws IOException, URISyntaxException {

		ArrayList<TabixLine> output = new ArrayList<TabixLine>();

		ArrayList<Pair64> off = get_chunk_coordinates(tid, beg, end);

		if (off.size() == 0)
			return output;

		LineBlockCompressedInputStream in = new LineBlockCompressedInputStream(data.stream());

		// System.out.println("Reading raw: " + tid + " " + beg + " " + end +
		// " " + off);

		for (Pair64 bin : off) {

			// seek to beginning of block
			in.seek(bin.start);
			// keep reading until end of block

			/*
			 * Checking the file pointer is not a good thing to check here as it is wrapped
			 * in a buffered reader that will put the pointer way forward.
			 */
			while (in.getFilePointer() >= 0 /* && in.getFilePointer() < bin.end */) {
				// System.out.println("FP1: " + in.getFilePointer() + "\t" +
				// bin.end);
				TabixLine intv = this.readParsedLine(in, tid, beg, end);// Line(in);
				if (intv == null)
					break;
				if (intv.end > end)
					break;
				// System.out.println("Raw line: " + intv.line());
				// System.out.println("\t" + intv.line().length());
				// System.out.println("\ttid: " + tid + "\t" + intv.tid);
				// System.out.println("\tmeta: " + intv.meta);
				// System.out.println("\tFP2: " + in.getFilePointer() + "\t" +
				// bin.end);
				// if it's not a comment, the sequence id is correct, we are not
				// seeking beyond the requested end yet
				// and the found entry overlaps with our request: we found a GFF
				// entry.
				if (intv.meta)
					continue;
				else
				// if (line.charAt(0) != idx.meta) {
				// try {
				// //ParsedLine intv = get_intv(line);
				// // log.info("$"+intv.beg+" "+intv.end+"$-"+line);
				if (intv.tid == tid && intv.beg < end) {
					if (is_overlap(beg, end, intv.beg, intv.end)) {
						output.add(intv);
					}
				}
				// } catch (NumberFormatException nf) {
				// throw new IOException("Problems while parsing: " + line+
				// "\n\tReadRange: " + beg + "\t" + end);
				// }
				// }
			}
		}
		in.close();
		// this.entriesFound = output.size();
		return output;
	}

	/**
	 * Reads a parsed line from a BlockCompressedInputStream without using a
	 * buffer. This enables us to control the filePointer on every line read.
	 * 
	 * 
	 * @param in
	 * @param end
	 * @param beg
	 * @param tid
	 * @return
	 * @throws IOException
	 */
	private TabixLine readParsedLine(LineBlockCompressedInputStream in, int tid, int beg, int end) throws IOException {

		TabixLine p = new TabixLine();

		String line = in.readLine();
		if (line == null)
			return null;
		p.setLine(line);
		p.parse(idx, '\t');

		return p;
	}

	// /**
	// * Take a line from the indexed file and retrieve the essential fields,
	// * according to the column numbers read at the beginning of the index
	// file.
	// *
	// * This could be extended with file specific exceptions (SAM preset, UCSC
	// * flag, etc...). See original C-code. annotated_c_project/index.c
	// starting
	// * line 149
	// *
	// * @param str
	// * GFF entry
	// * @return a parsed GFF line with begin, end, sequence id and bin
	// */
	// private ParsedLine get_intv(String str) {
	// ParsedLine intv = new ParsedLine();
	//
	// String[] arr = str.split("\t");
	// intv.tid = idx.names.indexOf(arr[(int) idx.sc - 1]);
	// intv.beg = Integer.parseInt(arr[(int) idx.bc - 1]);
	// intv.end = Integer.parseInt(arr[(int) idx.ec - 1]);
	//
	// // Scanner parseLine = new Scanner(str);
	// // parseLine.useDelimiter("\t");
	// // int col = 1;
	// // while (parseLine.hasNext()) {
	// // String content = parseLine.next();
	// // if (col == idx.sc) {
	// // // sequence
	// // intv.tid = idx.names.indexOf(content);
	// // }
	// // if (col == idx.bc) {
	// // // start
	// // intv.beg = Integer.parseInt(content);
	// // }
	// // if (col == idx.ec) {
	// // // end
	// // intv.end = Integer.parseInt(content);
	// // }
	// // col++;
	// // }
	// intv.payload = str;
	//
	// intv.bin = ti_reg2bin(intv.beg, intv.end);
	// return intv;
	// }

	private String lastSeq = null;
	private int lastStart = -1;
	private int lastEnd = -1;
	private ArrayList<TabixLine> lastList = null;

	// @Override
	public synchronized Iterable<TabixLine> query(String sequence, int start, int end) throws IOException,
			URISyntaxException {
		if (!idx.names.contains(sequence))
			return null;
		if (sequence.equals(lastSeq) && start >= lastStart && end <= lastEnd)
			return lastList;

		int tid = idx.names.indexOf(sequence);
		ArrayList<TabixLine> entryList = this.readRange(tid, start, end);
		lastSeq = sequence;
		lastStart = start;
		lastEnd = end;
		lastList = entryList;

		return entryList;
	}

	// /**
	// * @return the number of entries found in the last query
	// */
	// public int entriesFound() {
	// return this.entriesFound;
	// }

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.sf.jannot.source.DataSource#read(net.sf.jannot.EntrySet)
	 */
	@Override
	public EntrySet read(EntrySet add) throws ReadFailedException {
		if (add == null)
			add = new EntrySet();
		// System.out.println("Tabix names: " + idx.names);

		for (String name : idx.names) {
			Entry e = add.getOrCreateEntry(name);
			// System.out.println("Adding e: " + e);
			/* This should be done better, but for now I have no idea how */
			if (data.isPileup())
				e.add(new StringKey(data.toString()), new PileupWrapper(name, this, idx));
			else if (data.toString().toLowerCase().contains(".swig") || data.toString().toLowerCase().contains(".tab")
					|| data.toString().toLowerCase().contains(".tsv"))
				e.add(new StringKey(data.toString()), new SWigWrapper(name, this, idx));
			else if (data.toString().toLowerCase().contains(".bed"))
				e.add(Type.get(data.toString()), new BEDWrapper(name, this, idx));
			else if (data.toString().toLowerCase().contains(".gff"))
				e.add(Type.get(data.toString()), new GFFWrapper(name, this, idx));
			else if (data.isVCF()) {
				e.add(Type.get(data.toString()), new VCFWrapper(name, this, idx));
			} else
				log.severe("Don't now how to read this file, can't figure out the type: " + data);
		}
		return add;
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

/**
 * 
 * Organized content of an index file
 */
class TabIndex {

	/**
	 * File identifier magic. Should be \1
	 */
	String magic;

	/**
	 * Preset mask (for db specific settings?)
	 */
	long preset;

	/**
	 * Column number with sequence name
	 */
	long sc;
	/**
	 * Column number with starting nt
	 */
	long bc;
	/**
	 * Column number with ending nt
	 */
	long ec;

	/**
	 * Defines the character for meta information. Comment lines, normally set
	 * to '#'
	 */
	char meta;

	/**
	 * List of mappings (key to binlist), one per sequence.
	 */
	HashMap<Integer, ArrayList<Pair64>>[] index;

	/**
	 * Linear indexes, one per sequence
	 */
	ArrayList<Long>[] linIndex;

	/**
	 * Sequence list
	 */
	List<String> names = new ArrayList<String>();

	/**
	 * Create a new TabIndex
	 * 
	 * @param size
	 *            the number of sequences in the index
	 */
	public TabIndex(int size) {
		index = new HashMap[size];
		linIndex = new ArrayList[size];
		for (int i = 0; i < size; i++) {
			index[i] = new HashMap<Integer, ArrayList<Pair64>>();
			linIndex[i] = new ArrayList<Long>();
		}
	}

	public int size() {
		return index.length;
	}

}
