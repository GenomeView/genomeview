/*
 * Copyright (c) 2007-2011 by The Broad Institute of MIT and Harvard.
 * All Rights Reserved.
 *
 * This software is licensed under the terms of the GNU Lesser General Public License (LGPL), Version 2.1 which
 * is available at http://www.opensource.org/licenses/lgpl-2.1.php.
 *
 * THE SOFTWARE IS PROVIDED "AS IS." THE BROAD AND MIT MAKE NO REPRESENTATIONS OR WARRANTIES OF
 * ANY KIND CONCERNING THE SOFTWARE, EXPRESS OR IMPLIED, INCLUDING, WITHOUT LIMITATION, WARRANTIES
 * OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE, NONINFRINGEMENT, OR THE ABSENCE OF LATENT
 * OR OTHER DEFECTS, WHETHER OR NOT DISCOVERABLE.  IN NO EVENT SHALL THE BROAD OR MIT, OR THEIR
 * RESPECTIVE TRUSTEES, DIRECTORS, OFFICERS, EMPLOYEES, AND AFFILIATES BE LIABLE FOR ANY DAMAGES OF
 * ANY KIND, INCLUDING, WITHOUT LIMITATION, INCIDENTAL OR CONSEQUENTIAL DAMAGES, ECONOMIC
 * DAMAGES OR INJURY TO PROPERTY AND LOST PROFITS, REGARDLESS OF WHETHER THE BROAD OR MIT SHALL
 * BE ADVISED, SHALL HAVE OTHER REASON TO KNOW, OR IN FACT SHALL KNOW OF THE POSSIBILITY OF THE
 * FOREGOING.
 */

package org.broad.igv.tdf;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.DataFormatException;

import net.sf.samtools.seekablestream.SeekableStream;

import org.broad.LRUCache;
import org.broad.igv.track.WindowFunction;
import org.broad.igv.util.CompressionUtils;
import org.broad.igv.util.StringUtils;

/**
 * @author jrobinso
 * @author Thomas Abeel
 */
public class TDFReader {

	static final Logger log = Logger.getLogger(TDFReader.class.getCanonicalName());
	public static final int GZIP_FLAG = 0x1;

	// Cache to insure there is only 1 reader per file
	static Map<String, TDFReader> readerCache = new HashMap();

	private SeekableStream seekableStream = null;
	private int version;
	private int maxZoom = -1;
	private Map<String, IndexEntry> datasetIndex;
	private Map<String, IndexEntry> groupIndex;
	private String trackType;
	private String trackLine;
	private String[] trackNames;
	private LRUCache<String, TDFGroup> groupCache = new LRUCache(20);
	private LRUCache<String, TDFDataset> datasetCache = new LRUCache(20);

	private Map<WindowFunction, Double> valueCache = new HashMap();
	private List<WindowFunction> windowFunctions;
	private String locator;

	boolean compressed = false;

	// private String path;

	public static TDFReader getReader(SeekableStream locator) throws URISyntaxException {

		TDFReader reader = readerCache.get(locator.getSource());
		if (reader == null) {
			reader = new TDFReader(locator);
			readerCache.put(locator.getSource(), reader);
		}
		return reader;
	}

	private TDFReader(SeekableStream locator) throws URISyntaxException {
		this.seekableStream = locator;
		this.locator = locator.getSource();
		try {
			readHeader();

		} catch (IOException ex) {
			log.log(Level.SEVERE, "Error loading file: " + locator, ex);
			throw new RuntimeException("Error loading file: " + ex.toString() + locator);
		}
	}

	public void close() {
		try {
			seekableStream.close();
			readerCache.remove(locator);
			datasetCache.clear();
			datasetCache = null;
			groupCache.clear();
			groupCache = null;
			valueCache.clear();
			valueCache = null;
		} catch (IOException e) {
			log.log(Level.SEVERE, "Error closing reader for: " + locator, e);
		}
	}

	// public String getPath() {
	// return locator.getPath();
	// }

	private void readHeader() throws IOException {

		// Buffer for the magic number, version, index position, and index
		// byte count + header byte count (4 + 4 + 8 + 4 + 4)
		// byte[] buffer = new byte[24];
		// readFully(buffer);
		byte[] buffer = readBytes(0, 24);
		ByteBuffer byteBuffer = ByteBuffer.wrap(buffer);
		byteBuffer.order(ByteOrder.LITTLE_ENDIAN);

		int magicNumber = byteBuffer.getInt();

		byte[] magicBytes = new byte[4];
		System.arraycopy(buffer, 0, magicBytes, 0, 4);
		String magicString = new String(magicBytes);

		if (!(magicString.startsWith("TDF") || !magicString.startsWith("IBF"))) {
			String msg = "Error reading header: bad magic number.";
			// throw new DataLoadException(msg, locator.getPath());
		}

		version = byteBuffer.getInt();
		long idxPosition = byteBuffer.getLong();
		log.info("TDF-Reading master: " + idxPosition);
		int idxByteCount = byteBuffer.getInt();
		int nHeaderBytes = byteBuffer.getInt();

		byte[] bytes = readBytes(24, nHeaderBytes);
		byteBuffer = ByteBuffer.wrap(bytes);
		byteBuffer.order(ByteOrder.LITTLE_ENDIAN);

		if (version >= 2) {
			int nWFs = byteBuffer.getInt();
			this.windowFunctions = new ArrayList(nWFs);
			for (int i = 0; i < nWFs; i++) {
				String wfName = StringUtils.readString(byteBuffer);
				try {
					windowFunctions.add(WindowFunction.valueOf(wfName));
				} catch (Exception e) {
					log.log(Level.SEVERE, "Error creating window function: " + wfName, e);
				}
			}
		} else {
			windowFunctions = Arrays.asList(WindowFunction.mean);
		}

		// Track type
		try {
			// trackType = TrackType.valueOf();
			trackType = StringUtils.readString(byteBuffer);
			log.info("TDF TRACK TYPE: " + trackType);

		} catch (Exception e) {
			// trackType = TrackType.OTHER;
			log.info("TDF TRACK TYPE: no clue");
		}

		// Track line
		trackLine = StringUtils.readString(byteBuffer).trim();

		int nTracks = byteBuffer.getInt();
		trackNames = new String[nTracks];
		for (int i = 0; i < nTracks; i++) {
			trackNames[i] = StringUtils.readString(byteBuffer);
		}

		// Flags
		if (version > 2) {
			String genomeId = StringUtils.readString(byteBuffer);
			int flags = byteBuffer.getInt();
			compressed = (flags & GZIP_FLAG) != 0;
		} else {
			compressed = false;
		}

		readMasterIndex(idxPosition, idxByteCount);

	}

	private void readMasterIndex(long idxPosition, int nBytes) throws IOException {

		// fis.seek(idxPosition);
		// byte[] bytes = new byte[nBytes];
		// readFully(bytes);
		byte[] bytes = readBytes(idxPosition, nBytes);
		ByteBuffer byteBuffer = ByteBuffer.wrap(bytes);
		byteBuffer.order(ByteOrder.LITTLE_ENDIAN);

		int nDatasets = byteBuffer.getInt();

		datasetIndex = new LinkedHashMap(nDatasets);
		for (int i = 0; i < nDatasets; i++) {
			String name = StringUtils.readString(byteBuffer);
			String[] arr = name.split("/");
			if (arr[2].charAt(0) == 'z') {
				int cZoom = Integer.parseInt(arr[2].substring(1));
				if (cZoom > maxZoom)
					maxZoom = cZoom;
			}
			long fPosition = byteBuffer.getLong();
			int n = byteBuffer.getInt();
			datasetIndex.put(name, new IndexEntry(fPosition, n));
		}

		int nGroups = byteBuffer.getInt();
		groupIndex = new LinkedHashMap(nGroups);
		for (int i = 0; i < nGroups; i++) {
			String name = StringUtils.readString(byteBuffer);
			long fPosition = byteBuffer.getLong();
			int n = byteBuffer.getInt();
			groupIndex.put(name, new IndexEntry(fPosition, n));
		}
	}

	public TDFDataset getDataset(String chr, int zoom, WindowFunction windowFunction) {
		if (windowFunction == null)
			windowFunction = WindowFunction.mean;
		// Version 1 only had mean
		String wf = getVersion() < 2 ? "" : "/" + windowFunction.toString();

		String dsName = null;
		if (zoom >= 0)
			dsName = "/" + chr + "/z" + zoom + wf;
		else
			dsName = "/" + chr + "/raw";

		log.info("TDF-DSQUERY: " + dsName);
		TDFDataset ds = getDataset(dsName);

		return ds;
	}

	public synchronized TDFDataset getDataset(String name) {

		if (datasetCache.containsKey(name)) {
			return datasetCache.get(name);
		}

		try {
			if (datasetIndex.containsKey(name)) {
				IndexEntry ie = datasetIndex.get(name);
				long position = ie.position;
				int nBytes = ie.nBytes;

				// fis.seek(position);
				// byte[] buffer = new byte[nBytes];
				// readFully(buffer);
				byte[] buffer = readBytes(position, nBytes);
				ByteBuffer byteBuffer = ByteBuffer.wrap(buffer);
				byteBuffer.order(ByteOrder.LITTLE_ENDIAN);

				TDFDataset ds = new TDFDataset(name, byteBuffer, this);
				datasetCache.put(name, ds);
				return ds;
			} else {
				return null;
			}

		} catch (IOException ex) {
			log.log(Level.SEVERE, "Error reading dataset: " + name, ex);
			throw new RuntimeException("System error occured while reading dataset: " + name);
		}
	}

	public Collection<String> getDatasetNames() {
		return datasetIndex.keySet();
	}

	public Collection<String> getGroupNames() {
		return groupIndex.keySet();
	}

	public synchronized TDFGroup getGroup(String name) {
		if (groupCache.containsKey(name)) {
			return groupCache.get(name);
		}

		try {
			IndexEntry ie = groupIndex.get(name);
			long position = ie.position;
			int nBytes = ie.nBytes;

			// fis.seek(position);
			// byte[] buffer = new byte[nBytes];
			// readFully(buffer);
			byte[] buffer = readBytes(position, nBytes);
			ByteBuffer byteBuffer = ByteBuffer.wrap(buffer);
			byteBuffer.order(ByteOrder.LITTLE_ENDIAN);

			TDFGroup group = new TDFGroup(name, byteBuffer);

			groupCache.put(name, group);

			return group;

		} catch (IOException ex) {
			log.log(Level.SEVERE, "Error reading group: " + name, ex);
			throw new RuntimeException("System error occured while reading group: " + name);
		}
	}

	// TODO -- move to dataset class

	public synchronized TDFTile readTile(TDFDataset ds, int tileNumber) {

		try {
			if (tileNumber >= ds.tilePositions.length) {
				// TODO - return empty tile
				return null;
			}

			long position = ds.tilePositions[tileNumber];
			if (position < 0) {

				// Indicates empty tile
				// TODO -- return an empty tile?
				return null;
			}

			int nBytes = ds.tileSizes[tileNumber];
			// fis.seek(position);
			// byte[] buffer = new byte[nBytes];
			// readFully(buffer);
			byte[] buffer = readBytes(position, nBytes);
			if (compressed) {
				buffer = CompressionUtils.decompress(buffer);

			}

			return TileFactory.createTile(buffer, trackNames.length);
		} catch (Exception ex) {
			String tileName = ds.getName() + "[" + tileNumber + "]";
			log.log(Level.SEVERE, "Error reading data tile: " + tileName, ex);
			throw new RuntimeException("System error occured while reading tile: " + tileName);
		} 
	}

	/**
	 * @return the version
	 */
	public int getVersion() {
		return version;
	}

	/**
	 * @return the trackType
	 */
	public String getTrackType() {
		return trackType;
	}

	/**
	 * @return the trackLine
	 */
	public String getTrackLine() {
		return trackLine;
	}

	/**
	 * @return the trackNames
	 */
	public String[] getTrackNames() {
		return trackNames;
	}

	private Double getValue(WindowFunction wf) {
		if (!valueCache.containsKey(wf)) {
			TDFGroup rootGroup = getGroup("/");
			String maxString = rootGroup.getAttribute(wf.getDisplayName());
			try {
				valueCache.put(wf, Double.parseDouble(maxString));
			} catch (Exception e) {
				log.info("Warning: value '" + wf.toString() + "' not found in tdf value " + locator);
				valueCache.put(wf, null);
			}
		}
		return valueCache.get(wf);
	}

	/**
	 * Return the default upper value for the data range. A check is made to see
	 * if both upper and lower limits are equal to zero, within a tolerance. If
	 * they are the upper limit is arbitrarily set to "100". This is protection
	 * against the pathological case that can occur with datasets consisting of
	 * largely zero values.
	 * 
	 * @return
	 */
	public double getUpperLimit() {
		Double val = getValue(WindowFunction.percentile98);
		double upperLimit = val == null ? getDataMax() : val;
		if (upperLimit < 1.0e-30 && getLowerLimit() < 1.0e-30) {
			upperLimit = 100;
		}
		return upperLimit;
	}

	public double getLowerLimit() {
		Double val = getValue(WindowFunction.percentile2);
		return val == null ? getDataMin() : val;
	}

	public double getDataMax() {
		Double val = getValue(WindowFunction.max);
		return val == null ? 100 : val;
	}

	public double getDataMin() {
		Double val = getValue(WindowFunction.min);
		return val == null ? 0 : val;
	}

	public byte[] readBytes(long position, int nBytes) throws IOException {
		// System.out.println("Seeking: " + position);
		seekableStream.seek(position);
		byte[] buffer = new byte[nBytes];
		seekableStream.read(buffer, 0, nBytes);
		return buffer;
	}

	/**
	 * @return the windowFunctions
	 */
	public List<WindowFunction> getWindowFunctions() {
		return windowFunctions;
	}

	class IndexEntry {

		long position;
		int nBytes;

		public IndexEntry(long position, int nBytes) {
			this.position = position;
			this.nBytes = nBytes;
		}
	}

	/**
	 * @return
	 */
	public String getLocator() {
		return locator;
	}

	public int getMaxZoom() {
		return maxZoom;
	}
}
