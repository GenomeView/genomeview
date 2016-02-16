/**
 * %HEADER%
 */
package net.sf.jannot.mafix;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.zip.GZIPOutputStream;

import cern.colt.Arrays;

import be.abeel.io.LineIterator;

/**
 * The index of a MAF file.
 * 
 * @author thpar
 * @author Thomas Abeel
 * 
 */
class MAFIndex {
	// number of nucs per tile
	private int TILE_SIZE = 100;

	/**
	 * Pairs an index (nucleotide position to position in alignment file) with a
	 * tiling (nucleotide range to nucleotide positions with associated
	 * entries).
	 * 
	 * @author thpar
	 * 
	 */
	private class ChrIndex {
		/**
		 * The index keeps a reference from the nucleotide position to a
		 * {@link MAFEntry}
		 */
		public Map<Integer, MAFEntry> index = new HashMap<Integer, MAFEntry>();
		/**
		 * Mapping of tile number to a list of alignment starts A tile is mapped
		 * to the alignment start if it somewhere overlaps with the alignment.
		 */
		public Map<Integer, List<Integer>> tiling = new HashMap<Integer, List<Integer>>();
	}

	/**
	 * Maps chromosome names to their indexes
	 */
	private Map<String, ChrIndex> chromIndexes = new HashMap<String, ChrIndex>();

	/**
	 * Reference chromosome names mapped to the species they're aligned with.
	 * The list of species contains ALL species, including the reference
	 * chromosome itself, that's on place 0.
	 */
	private Map<String, List<String>> chromSpecies = new HashMap<String, List<String>>();

	/**
	 * Start a new empty index object
	 */
	MAFIndex() {
	}

	/**
	 * create an index object streamed from an index file
	 * 
	 * @param indexFile
	 * @throws IOException
	 */
	MAFIndex(InputStream indexFile) throws IOException {
		this.loadFromStream(indexFile);
	}

	/**
	 * Write the index to a file, compressed with GZIP.
	 * 
	 * @param indexFile
	 */
	void writeToFile(File indexFile) throws IOException {
		System.out.println("Writing index to file");
		GZIPOutputStream gzo = new GZIPOutputStream(new FileOutputStream(indexFile));

		PrintWriter pw = new PrintWriter(gzo);

		for (Entry<String, ChrIndex> chrEntry : chromIndexes.entrySet()) {
			String refChrom = chrEntry.getKey();
			// get all species that are aligned to this chromosome
			List<String> species = chromSpecies.get(refChrom);

			// make species mapping
			// output species at the same time
			Map<String, Integer> speciesLocationMap = new HashMap<String, Integer>();
			pw.print(":");
			for (int specCount = 0; specCount < species.size(); specCount++) {
				String spec = species.get(specCount);
				pw.print(spec);
				speciesLocationMap.put(spec, specCount);
				if (specCount < species.size() - 1) {
					pw.print(",");
				}
			}
			pw.println();
			Map<Integer, MAFEntry> index = chrEntry.getValue().index;
			for (Entry<Integer, MAFEntry> entry : index.entrySet()) {
				int nuc = entry.getKey();
				MAFEntry mafEntry = entry.getValue();
				int alignmentLength = mafEntry.getAlignmentLength();
				long fileStart = mafEntry.getStart();
				// long fileEnd = mafEntry.getEnd();

				// String strands = mafEntry.getEncodedStrands();
				String speciesCode = mafEntry.getEncodedSpecies();

				pw.println(nuc + "\t" + alignmentLength + "\t" + fileStart + "\t" + "\t" + "\t" + speciesCode);

			}
		}

		pw.close();
	}

	

	void addEntry(String chr, MAFEntry mafEntry) {
		ChrIndex chrIndex = chromIndexes.get(chr);
		if (chrIndex == null) {
			chrIndex = new ChrIndex();
			chromIndexes.put(chr, chrIndex);
		}

		Map<Integer, MAFEntry> index = chrIndex.index;

		index.put(mafEntry.getNucStart(), mafEntry);
		int endNucPosition = mafEntry.getNucStart() + mafEntry.getAlignmentLength() - 1;

		int startTileNumber = getTileNumber(mafEntry.getNucStart());
		int endTileNumber = getTileNumber(endNucPosition);
		for (int i = startTileNumber; i <= endTileNumber; i++) {
			List<Integer> tile = getTile(chr, i);
			tile.add(mafEntry.getNucStart());
		}
	}

	/**
	 * Translate a nucleotide position to the tile number
	 * 
	 * @param nucPosition
	 * @return
	 */
	private int getTileNumber(int nucPosition) {
		return nucPosition / TILE_SIZE;
	}

	/**
	 * Tiles are counted zero based
	 * 
	 * @param chr
	 *            name of the chromosome
	 * @param tileNumber
	 * @return list with all alignment starts within this tile
	 */
	private List<Integer> getTile(String chr, int tileNumber) {
		ChrIndex chrIndex = chromIndexes.get(chr);
		if (chrIndex == null) {
			chrIndex = new ChrIndex();
			chromIndexes.put(chr, chrIndex);
		}

		Map<Integer, List<Integer>> tiling = chrIndex.tiling;

		List<Integer> tile;
		if (!tiling.containsKey(tileNumber)) {
			tile = new ArrayList<Integer>();
			tiling.put(tileNumber, tile);
		} else {
			tile = tiling.get(tileNumber);
		}
		return tile;
	}

	/**
	 * 
	 * Returns all the alignments that overlap with [nucFrom, nucTo[
	 * 
	 * @param nucFrom
	 * @param nucTo
	 * @return
	 */
	SortedSet<MAFEntry> getMAFEntries(String chr, int nucFrom, int nucTo) {
		int startTile = getTileNumber(nucFrom);
		int endTile = getTileNumber(nucTo - 1);

		SortedSet<MAFEntry> mafEntries = new TreeSet<MAFEntry>();

		ChrIndex chrIndex = chromIndexes.get(chr);
		if (chrIndex == null)
			return mafEntries;

		Map<Integer, MAFEntry> index = chrIndex.index;
		Map<Integer, List<Integer>> tiling = chrIndex.tiling;

		for (int i = startTile; i <= endTile; i++) {
			if (tiling.containsKey(i)) {
				List<Integer> alignmentStarts = tiling.get(i);
				// iterate the starting points and retrieve the entry in the
				// index
				// entries overlapping with the requested range are returned
				for (int alStart : alignmentStarts) {
					MAFEntry entry = index.get(alStart);
					if (entry.overlaps(nucFrom, nucTo)) {
						mafEntries.add(entry);
					}

				}
			}
		}
		return mafEntries;
	}

	void loadFromStream(InputStream indexFile) throws FileNotFoundException, IOException {
		LineIterator lit = new LineIterator(indexFile);

		String currentChrom = new String();

		for (String line : lit) {
			if (line.startsWith(":")) {
				// started on a new chromosome
				String[] species = line.substring(1).split(",");
				currentChrom = species[0];
				for (int i = 0; i < species.length; i++) {
					addSpecies(currentChrom, species[i]);
				}

			} else {
				String[] cols = line.split("\t");
				MAFEntry mafEnt = new MAFEntry();
				mafEnt.setNucStart(Integer.parseInt(cols[0]) + 1);
				mafEnt.setAlignmentLength(Integer.parseInt(cols[1]));
				mafEnt.setOffsetPair(Long.parseLong(cols[2]));
				// mafEnt.setStrandEncoding(cols[4]);
				mafEnt.setSpeciesEncoding(cols[5]);

				this.addEntry(currentChrom, mafEnt);
			}
		}
	}

	MAFEntry getMAFEntry(String chr, int nucPosition) {
		if (!chromIndexes.containsKey(chr))
			return null;
		return chromIndexes.get(chr).index.get(nucPosition);
	}

	/**
	 * @return The names of the stored chromosomes
	 */
	Set<String> getNames() {
		return chromIndexes.keySet();
	}

	/**
	 * Returns the list of species/chromosome the reference chromosomse is
	 * mapped to, including the reference chromosome itself on place 0.
	 * 
	 * @param chr
	 *            the reference chromosome
	 * @return
	 */
	List<String> getSpecies(String chr) {
		return chromSpecies.get(chr);
	}

	void addSpecies(String refChrom, String spec) {
		List<String> species;
		species = this.chromSpecies.get(refChrom);
		if (species == null) {
			species = new ArrayList<String>();
			chromSpecies.put(refChrom, species);
		}
		if (!species.contains(spec)) {
			species.add(spec);
		}
	}

	/**
	 * @param blockSpecies
	 * @param blockStrands
	 * @return
	 */
	int[] encodeSpecies(String refChrom, List<String> blockSpecies, String blockStrands) {
		int[] out = new int[blockSpecies.size()];
		List<String> list = chromSpecies.get(refChrom);
		for (int i = 0; i < blockSpecies.size(); i++) {
			int sign = 1;
			if (blockStrands.charAt(i+1) == '-')
				sign = -1;
			out[i] = sign * list.indexOf(blockSpecies.get(i));
		}
		return out;
	}

	/**
	 * @param selectedChrom
	 * @param spec
	 * @return
	 */
	String getSpeciesName(String selectedChrom, int spec) {
		return chromSpecies.get(selectedChrom).get(spec);
	}

	public int size(String selectedChrom) {
		return chromIndexes.get(selectedChrom).index.size();
	}

}
