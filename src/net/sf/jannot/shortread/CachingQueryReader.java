/**
 * %HEADER%
 */
/*
 * The Broad Institute
 * SOFTWARE COPYRIGHT NOTICE AGREEMENT
 * This is copyright (2007-2009) by the Broad Institute/Massachusetts Institute
 * of Technology.  It is licensed to You under the Gnu Public License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *    http://www.opensource.org/licenses/gpl-2.0.php
 *
 * This software is supplied without any warranty or guaranteed support
 * whatsoever. Neither the Broad Institute nor MIT can be responsible for its
 * use, misuse, or functionality.
 */
/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.sf.jannot.shortread;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.WeakHashMap;
import java.util.logging.Logger;

import net.sf.jannot.source.SAMDataSource;
import net.sf.samtools.SAMRecord;
import net.sf.samtools.util.CloseableIterator;
import be.abeel.util.LRUCache;

/**
 * A wrapper for {@link SAMDataSource} that supports query by interval and is
 * cached
 * 
 * @author jrobinso
 * @author Thomas Abeel
 */
public class CachingQueryReader {

	private static Logger log = Logger.getLogger(CachingQueryReader.class.getCanonicalName());
	private String cachedChr = "";
	private static int maxTileCount = 30;
	private int tileSize = 8000;
	private SAMDataSource ds;
	private LRUCache<Integer, Tile> cache;

	private static WeakHashMap<SAMDataSource, CachingQueryReader> wmap = new WeakHashMap<SAMDataSource, CachingQueryReader>();

	public static CachingQueryReader create(SAMDataSource ds) {
		if (wmap.get(ds) == null)
			wmap.put(ds, new CachingQueryReader(ds));
		return wmap.get(ds);
	}

	private CachingQueryReader(SAMDataSource ds) {
		this.ds = ds;
		cache = new LRUCache<Integer, Tile>(maxTileCount);
	}

	//

	public CloseableIterator<SAMRecord> iterator() {
		return ds.getReader().iterator();
	}

	private EmptyIterator<SAMRecord> empty = new EmptyIterator<SAMRecord>();

	public CloseableIterator<SAMRecord> query(String sequence, int start, int end, boolean contained) {

		int startTile = (start + 1) / getTileSize(sequence);
		int endTile = end / getTileSize(sequence); // <= inclusive
		List<Tile> tiles = getTiles(sequence, startTile, endTile);

		if (tiles.size() == 0) {
			return empty;
		}

		// Count total # of records
		int recordCount = tiles.get(0).getOverlappingRecords().size();
		for (Tile t : tiles) {
			recordCount += t.getContainedRecords().size();
		}

		List<SAMRecord> alignments = new ArrayList<SAMRecord>(recordCount);
		alignments.addAll(tiles.get(0).getOverlappingRecords());
		for (Tile t : tiles) {
			alignments.addAll(t.getContainedRecords());
		}
		return new TiledIterator(start, end, alignments);
	}

	private List<Tile> getTiles(String seq, int startTile, int endTile) {

		if (!seq.equals(cachedChr)) {
			cache = new LRUCache<Integer, Tile>(maxTileCount);
			cachedChr = seq;
		}

		List<Tile> tiles = new ArrayList<Tile>(endTile - startTile + 1);
		List<Tile> tilesToLoad = new ArrayList<Tile>(endTile - startTile + 1);

		int tileSize = getTileSize(seq);
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
					loadTiles(seq, tilesToLoad);
				}
				tilesToLoad.clear();
			} else {
				tilesToLoad.add(tile);
			}
		}

		if (tilesToLoad.size() > 0) {
			loadTiles(seq, tilesToLoad);
		}

		return tiles;
	}

	private void loadTiles(String seq, List<Tile> tiles) {

		int start = tiles.get(0).start;
		int end = tiles.get(tiles.size() - 1).end;
		CloseableIterator<SAMRecord> iter = null;

		// log.debug("Loading : " + start + " - " + end);
		int alignmentCount = 0;
		// long t0 = System.currentTimeMillis();
		try {
			iter = ds.getReader().query(seq, start, end, false);

			int tileSize = getTileSize(seq);
			while (iter.hasNext()) {
				SAMRecord record = iter.next();

				// Range of tile indices that this alignment contributes to.
				int aStart = record.getAlignmentStart();
				int aEnd = record.getAlignmentEnd();// record.getEnd();
				int idx0 = Math.max(0, (aStart - start) / tileSize);
				int idx1 = Math.min(tiles.size() - 1, (record.getAlignmentEnd() - start) / tileSize);

				// Loop over tiles this read overlaps
				for (int i = idx0; i <= idx1; i++) {
					Tile t = tiles.get(i);
					// FIXME aEnd???
					if ((aStart >= t.start) && (aStart < t.end)) {
						t.containedRecords.add(record);
					} else if ((aEnd >= t.start) && (aStart < t.start)) {
						t.overlappingRecords.add(record);
					}
				}

				alignmentCount++;
//				if (alignmentCount % 5000 == 0) {
//					// IGVMainFrame.getInstance().setStatusBarMessage("Reads loaded: "
//					// + alignmentCount);
//				}
			}

			for (Tile t : tiles) {
				t.setLoaded(true);
			}

			// double dt = (System.currentTimeMillis() - t0) / 1000.0;
			// log.debug("Loaded " + alignmentCount + " in " + dt);

		} finally {
			if (iter != null) {
				iter.close();
			}
			// IGVMainFrame.getInstance().resetStatusMessage();
		}
	}

	/**
	 * @return the tileSize
	 */
	public int getTileSize(String chr) {
		if (chr.equals("M") || chr.equals("chrM") || chr.equals("MT") || chr.equals("chrMT")) {
			return 100;
		} else {
			return tileSize;
		}
	}

	static class Tile {

		private boolean loaded = false;
		private List<SAMRecord> containedRecords;
		private int end;
		private List<SAMRecord> overlappingRecords;
		private int start;
		private int tileNumber;

		Tile(int tileNumber, int start, int end) {
			this.tileNumber = tileNumber;
			this.start = start;
			this.end = end;
			containedRecords = new ArrayList<SAMRecord>(16000);
			overlappingRecords = new ArrayList<SAMRecord>();
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

		/**
		 * @return the containedRecords
		 */
		public List<SAMRecord> getContainedRecords() {
			return containedRecords;
		}

		/**
		 * @param containedRecords
		 *            the containedRecords to set
		 */
		public void setContainedRecords(List<SAMRecord> containedRecords) {
			this.containedRecords = containedRecords;
		}

		/**
		 * @return the overlappingRecords
		 */
		public List<SAMRecord> getOverlappingRecords() {
			return overlappingRecords;
		}

		/**
		 * @param overlappingRecords
		 *            the overlappingRecords to set
		 */
		public void setOverlappingRecords(List<SAMRecord> overlappingRecords) {
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

	public class TiledIterator implements CloseableIterator<SAMRecord> {

		int tileIdx = 0;
		Iterator<SAMRecord> currentSamIterator;
		int end;
		SAMRecord nextRecord;
		int start;
		List<SAMRecord> alignments;

		TiledIterator(int start, int end, List<SAMRecord> alignments) {
			this.alignments = alignments;
			this.start = start;
			this.end = end;
			currentSamIterator = alignments.iterator();
			advanceToFirstRecord();
		}

		public void close() {
			// No-op
		}

		public boolean hasNext() {
			return nextRecord != null;
		}

		public SAMRecord next() {
			SAMRecord ret = nextRecord;

			advanceToNextRecord();

			return ret;
		}

		public void remove() {
			// ignored
		}

		private void advanceToFirstRecord() {
			advanceToNextRecord();
		}

		private void advanceToNextRecord() {
			advance();

			while ((nextRecord != null) && (nextRecord.getAlignmentEnd() < start)) {
				advance();
			}
		}

		private void advance() {
			if (currentSamIterator.hasNext()) {
				nextRecord = currentSamIterator.next();
				if (nextRecord.getAlignmentStart() > end) {
					nextRecord = null;
				}
			} else {
				nextRecord = null;
			}
		}
	}
}
// ~ Formatted by Jindent --- http://www.jindent.com

