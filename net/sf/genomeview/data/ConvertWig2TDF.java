/*
 * Copyright (c) 2007-2011 by The Broad Institute, Inc. and the Massachusetts Institute of Technology.
 * All Rights Reserved.
 *
 * This software is licensed under the terms of the GNU Lesser General private License (LGPL), Version 2.1 which
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

package net.sf.genomeview.data;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.sf.jannot.Entry;
import net.sf.jannot.EntrySet;
import net.sf.jannot.exception.ReadFailedException;
import net.sf.jannot.refseq.Sequence;
import net.sf.jannot.source.DataSourceFactory;
import net.sf.jannot.source.Locator;

import org.broad.igv.tdf.TDFBedTile;
import org.broad.igv.tdf.TDFDataset;
import org.broad.igv.tdf.TDFFixedTile;
import org.broad.igv.tdf.TDFGroup;
import org.broad.igv.tdf.TDFTile;
import org.broad.igv.tdf.TDFVaryTile;
import org.broad.igv.tdf.TDFWriter;
import org.broad.igv.tools.Accumulator;
import org.broad.igv.track.WindowFunction;
import org.broad.igv.util.collections.FloatArrayList;
import org.broad.igv.util.collections.IntArrayList;

import be.abeel.io.LineIterator;

/**
 * 
 * @author jrobinso
 * @author Thomas Abeel
 */
class ConvertWig2TDF {

	private static Logger log = Logger.getLogger(ConvertWig2TDF.class.toString());
	private boolean compressed = true;
	private boolean skipZeroes = false;
	private int nZoom;
	private int maxExtFactor = 0;
	private Zoom[] zoomLevels;
	private int nTracks;

	private String currentChr = "";
	private int currentChrLength;
	private int nPtsProcessed = 0;
	private int lastStartPosition = 0;
	private HashSet<String> skippedChromosomes = new HashSet<String>();
	private TDFWriter writer;
	private Raw rawData;
	private File outputFile;
	private Accumulator allDataStats;
	private List<String> chromosomes = new ArrayList<String>();
	private Set<String> visitedChromosomes = new HashSet<String>();
	private Map<String, String> attributes = new HashMap<String, String>();
	private PrintStream out = System.out;

	private List<WindowFunction> windowFunctions = Arrays.asList(WindowFunction.mean,WindowFunction.min,WindowFunction.max);// ,
																						// WindowFunction.median,
	// WindowFunction.min, WindowFunction.max);
	private String genomeID;

	private ConvertWig2TDF(String gID, File output) {
		this.genomeID = gID;
		this.outputFile = output;
		// this.genome = genome;
		allDataStats = new Accumulator(windowFunctions);
	}

	

	public static void main(String[]args) throws IOException, URISyntaxException{
		ConvertWig2TDF.convertWig2TDF(new Locator("z:/workspace/NetworkReconstruction/info/hotregions.wig"), new File("z:/workspace/NetworkReconstruction/info/hotregions.wig.tdf"));
	}
	
	

	/**
	 * Called to set inital parameters. It is required that this be called prior
	 * to writing the file
	 */
	private void setTrackParameters(String trackType, String trackLine, String[] trackNames) {

		if (outputFile != null && writer == null) {
			writer = new TDFWriter(outputFile, genomeID, trackType, trackLine, trackNames, windowFunctions, compressed);
			nTracks = trackNames.length;

			TDFGroup rootGroup = writer.getRootGroup();
			rootGroup.setAttribute("genome", genomeID);
			rootGroup.setAttribute("maxZoom", String.valueOf(nZoom));

		}
	}

	/**
	 * Add an array of data for the given interval. The array contains a value
	 * for each sample/track in this dataset. The name is an optional probe or
	 * feature name.
	 */
	private void addData(String chr, int start, int end, float[] data, String name) {

		if (writer == null) {
			return;
		}

		if (skipZeroes) {
			boolean allZeroes = true;
			for (int i = 0; i < data.length; i++) {
				if (data[i] != 0) {
					allZeroes = false;
					break;
				}
			}
			if (allZeroes) {
				return;
			}
		}

		
		if (skippedChromosomes.contains(chr)) {
			return;
		}

		if (currentChr != null && chr.equals(currentChr)) {
			if (start < (lastStartPosition - maxExtFactor)) {
				String msg = "Error: Data is not sorted @ " + chr + " " + start + "  (last position = "
						+ lastStartPosition + "   max ext factor = " + maxExtFactor + ")";
				out.println(msg);
				throw new RuntimeException(msg);
			}
		} else {
			newChromosome(chr);
		}

		// Check a second time, in case it just got added
		if (skippedChromosomes.contains(chr)) {
			return;
		}

		// Is this data in range for the chromosome?
		int chrLength = mapLength.get(chr);//.getMaximumLength(); // genome.getChromosome(chr).getLength();

		if (start > chrLength) {
			log.info("Ignoring data from non-existent locus.  Probe = " + name + "  Locus = " + chr + ":" + start + "-"
					+ end + ". " + chr + " length = " + chrLength);
			return;
		}

		// Add to raw data
		rawData.addData(start, end, data, name);

		// Zoom levels
		for (Zoom zl : zoomLevels) {
			zl.addData(start, end, data);
		}

		lastStartPosition = start;

	}

	/**
	 * Start a new chromosome. Note that data is sorted by chromosome, then
	 * start position.
	 */
	private void newChromosome(String chr) {

		if (visitedChromosomes.contains(chr)) {
			String msg = "Error: Data is not ordered by start position. Chromosome " + chr
					+ " appears in multiple blocks";
			out.println(msg);
			throw new RuntimeException(msg);

		}
		visitedChromosomes.add(chr);
		

			chromosomes.add(chr);

			out.println();
			out.println("Processing chromosome " + chr);
			if (zoomLevels != null) {
				for (Zoom zl : zoomLevels) {
					zl.close();
				}
			}
			if (rawData != null) {
				rawData.close();
			}

			currentChr = chr;
			currentChrLength = mapLength.get(currentChr);//c.getMaximumLength();
			zoomLevels = new Zoom[nZoom + 1];
			for (int z = 0; z <= nZoom; z++) {
				zoomLevels[z] = new Zoom(chr, z, currentChrLength);
			}

			rawData = new Raw(chr, currentChrLength, 100000);

		lastStartPosition = 0;

	}

	private void finish() {
		if (writer == null) {
			return;
		}

		StringBuffer chrString = new StringBuffer();
		Iterator<String> iter = chromosomes.iterator();
		while (iter.hasNext()) {
			chrString.append(iter.next());
			if (iter.hasNext()) {
				chrString.append(",");
			}
		}
		writer.getRootGroup().setAttribute("chromosomes", chrString.toString());

		for (Map.Entry<String, String> entry : attributes.entrySet()) {
			writer.getRootGroup().setAttribute(entry.getKey(), entry.getValue());
		}

		if (zoomLevels != null) {
			for (Zoom zl : zoomLevels) {
				zl.close();
			}
		}
		
		if (rawData == null) {
			// TODO -- delete .tdf file?
			out.println("No features were found that matched chromosomes in genome: " + genomeID);

		} else {
			rawData.close();

			// Record max/min
			allDataStats.finish();
			TDFGroup group = writer.getGroup("/");
			group.setAttribute(TDFGroup.USE_PERCENTILE_AUTOSCALING, "true");
			for (WindowFunction wf : windowFunctions) {
				group.setAttribute(wf.getDisplayName(), String.valueOf(allDataStats.getValue(wf)));
			}
			writer.closeFile();
		}

	}

	/**
	 * Class representing a tile of raw (as opposed to summarized) data.
	 */
	private class RawTile {
		String dsName;
		int tileNumber;
		int tileStart;
		int tileEnd;
		IntArrayList startArray;
		IntArrayList endArray;
		ArrayList<String> nameList;
		FloatArrayList[] dataArray;

		RawTile(String dsName, int tileNumber, int start, int end) {
			this.dsName = dsName;
			this.tileNumber = tileNumber;
			this.tileStart = start;
			this.tileEnd = end;
			startArray = new IntArrayList();
			endArray = new IntArrayList();
			dataArray = new FloatArrayList[nTracks];
			for (int i = 0; i < nTracks; i++) {
				dataArray[i] = new FloatArrayList();
			}
		}

		void addData(int start, int end, float[] data, String name) {

			if (start > tileEnd) {
				log.info("Warning: start position > tile end");

			}

			if (end < tileStart) {
				log.info("Warning: end position > tile end");
			}

			if (name != null && nameList == null) {
				nameList = new ArrayList<String>();
			}

			int dataStart = Math.max(tileStart, start);
			int dataEnd = Math.min(tileEnd, end);
			startArray.add(dataStart);
			endArray.add(dataEnd);
			for (int i = 0; i < data.length; i++) {
				dataArray[i].add(data[i]);
			}
			if (name != null) {
				nameList.add(name);
			}
		}

		void close() {
			try {
				if (startArray.size() > 0) {
					int[] s = startArray.toArray();
					int[] e = endArray.toArray();
					float[][] d = new float[dataArray.length][dataArray[0].size()];
					for (int i = 0; i < dataArray.length; i++) {
						d[i] = dataArray[i].toArray();
					}

					String[] n = nameList == null ? null : nameList.toArray(new String[] {});
					TDFBedTile tile = new TDFBedTile(tileStart, s, e, d, n);
					writer.writeTile(dsName, tileNumber, tile);
					startArray.clear();
					endArray.clear();
					for (int i = 0; i < dataArray.length; i++) {
						dataArray[i].clear();
					}
				}
			} catch (IOException ex) {
				ex.printStackTrace();
			}
		}
	}

	/**
	 * Class representing the raw dataset
	 */
	private class Raw {

		private String dsName;
		private int tileWidth;
		private Map<Integer, RawTile> activeTiles = new HashMap<Integer, RawTile>();

		Raw(String chr, int chrLength, int tileWidth) {

			this.tileWidth = tileWidth;
			int nTiles = (int) (chrLength / tileWidth) + 1;
			dsName = "/" + chr + "/raw";
			writer.createDataset(dsName, TDFDataset.DataType.FLOAT, tileWidth, nTiles);

		}

		/**
		 * @param start
		 * @param end
		 * @param data
		 */
		private void addData(int start, int end, float[] data, String name) {

			int startTileNumber = (int) (start / tileWidth);
			int endTileNumber = (int) (end / tileWidth);

			// Check for closed tiles -- tiles we are guaranteed to not revisit
			int tmp = (start - maxExtFactor) / tileWidth;
			while (!activeTiles.isEmpty()) {
				Integer tileNumber = activeTiles.keySet().iterator().next();
				if (tileNumber < tmp) {
					RawTile t = activeTiles.get(tileNumber);
					t.close();
					activeTiles.remove(tileNumber);
					// out.println("(-) " + level + "_" + tileNumber);
				} else {
					break;
				}
			}

			// Add data to all tiles it spans. The data will be effectively
			// "cut" if it spans multiple tiles.
			for (int t = startTileNumber; t <= endTileNumber; t++) {
				RawTile tile = activeTiles.get(t);
				if (tile == null) {
					tile = new RawTile(dsName, t, t * tileWidth, (t + 1) * tileWidth);
					activeTiles.put(t, tile);
				}
				tile.addData(start, end, data, name);
			}

			nPtsProcessed++;
		}

		void close() {
			for (RawTile t : activeTiles.values()) {
				t.close();
			}
			activeTiles = null;
		}

	}

	/**
	 * Class representing all the data for a particular zoom level.
	 */
	private class Zoom {

		int level;
		int tileWidth;
		LinkedHashMap<Integer, Tile> activeTiles = new LinkedHashMap<Integer, Tile>();
		Map<WindowFunction, TDFDataset> datasets = new HashMap<WindowFunction, TDFDataset>();

		Zoom(String chr, int level, int chrLength) {
			int nTiles = (int) Math.pow(2, level);
			tileWidth = chrLength / nTiles + 1;
			this.level = level;

			// Create datasets -- one for each window function
			for (WindowFunction wf : windowFunctions) {
				String dsName = "/" + chr + "/z" + level + "/" + wf.toString();
				datasets.put(wf, writer.createDataset(dsName, TDFDataset.DataType.FLOAT, tileWidth, nTiles));
			}
		}

		private void addData(int start, int end, float[] data) {

			int startTile = start / tileWidth;
			int endTile = end / tileWidth;

			// Check for closed tiles
			int tmp = (start - maxExtFactor) / tileWidth;
			while (!activeTiles.isEmpty()) {
				Integer tileNumber = activeTiles.keySet().iterator().next();
				if (tileNumber < tmp) {
					Tile t = activeTiles.get(tileNumber);
					t.close();
					activeTiles.remove(tileNumber);
				} else {
					break;
				}
			}

			for (int i = startTile; i <= endTile; i++) {
				Tile t = activeTiles.get(i);
				if (t == null) {
					t = new Tile(datasets, level, i, 700, tileWidth);
					activeTiles.put(i, t);
				}
				t.addData(start, end, data);
			}
		}

		// Close all active tiles

		private void close() {
			for (Tile t : activeTiles.values()) {
				t.close();
			}
		}
	}

	/**
	 * A tile of summarized data
	 */
	private class Tile {
		int totalCount;
		// int zoomLevel;
		int tileNumber;
		int tileStart;
		int lastFinishedBin = 0;
		float binWidth;
		int nBins;
		int nonEmptyBins;
		Accumulator[][] accumulators;
		Map<WindowFunction, TDFDataset> datasets;

		Tile(Map<WindowFunction, TDFDataset> datasets, int zoomLevel, int tileNumber, int nBins, int tileWidth) {
			this.totalCount = 0;
			this.datasets = datasets;
			// this.zoomLevel = zoomLevel;
			this.tileNumber = tileNumber;
			this.tileStart = tileNumber * tileWidth;
			this.nBins = nBins;
			this.binWidth = ((float) tileWidth) / nBins;
			this.accumulators = new Accumulator[nTracks][nBins];
		}

		/**
		 * Add a data point. Positions are zero based exclusive (UCSC
		 * convention)
		 * 
		 * @param start
		 * @param end
		 * @param data
		 *            array of values at this position, 1 value per track
		 */
		void addData(int start, int end, float[] data) {
			totalCount++;

			int startBin = Math.max(0, (int) ((start - tileStart) / binWidth));
			int endBin = Math.min(nBins - 1, (int) ((end - tileStart) / binWidth));

			int tmp = (int) ((start - tileStart - maxExtFactor) / binWidth);

			for (int t = 0; t < nTracks; t++) {
				for (int b = lastFinishedBin; b < tmp; b++) {
					if (accumulators[t][b] != null) {
						accumulators[t][b].finish();
					}
				}
				lastFinishedBin = Math.max(0, tmp - 1);

				for (int b = startBin; b <= endBin; b++) {
					if (accumulators[t][b] == null) {
						accumulators[t][b] = new Accumulator(datasets.keySet());
					}
					accumulators[t][b].add(data[t]);
				}
			}
		}

		/**
         *
         */
		void close() {

			// Count non-empty bins. All tracks should be the same
			nonEmptyBins = 0;

			for (int t = 0; t < nTracks; t++) {
				for (int i = 0; i < nBins; i++) {
					if (accumulators[t][i] != null) {
						accumulators[t][i].finish();
						if (t == 0) {
							nonEmptyBins++;
						}
					}
				}
			}

			TDFTile tile = null;

			for (WindowFunction wf : datasets.keySet()) {

				// If < 50% of bins are empty use vary step tile, otherwise use
				// fixed step
				if (nonEmptyBins < 0.5 * nBins) {
					int[] starts = new int[nonEmptyBins];
					float[][] data = new float[nTracks][nonEmptyBins];
					int n = 0;
					for (int i = 0; i < nBins; i++) {
						for (int t = 0; t < nTracks; t++) {
							Accumulator acc = accumulators[t][i];
							if (acc != null) {
								data[t][n] = acc.getValue(wf);
								if (t == nTracks - 1) {
									starts[n] = (int) (tileStart + (i * binWidth));
									n++;
								}
							}
						}
					}
					tile = new TDFVaryTile((int) tileStart, binWidth, starts, data);

				} else {
					float[][] data = new float[nTracks][nBins];
					for (int t = 0; t < nTracks; t++) {
						for (int i = 0; i < nBins; i++) {
							data[t][i] = accumulators[t][i] == null ? Float.NaN : accumulators[t][i].getValue(wf);
						}
					}
					tile = new TDFFixedTile(tileStart, tileStart, binWidth, data);
				}

				String dsName = datasets.get(wf).getName();
				try {
					writer.writeTile(dsName, tileNumber, tile);
				} catch (IOException iOException) {
					log.log(Level.SEVERE, "Error writing tile: " + dsName + " [" + tileNumber + "]", iOException);
					// TODO -- replace with PreprocessorException
					throw new RuntimeException(iOException);
				}
			}
		}
	}

	private void count(int maxZoomValue, Locator data) throws IOException, URISyntaxException {
		// setNZoom(maxZoomValue);
		nZoom = maxZoomValue;
		setTrackParameters(genomeID, genomeID, new String[] { "value" });
		// this.setSkipZeroes(true);

		// int len = dnaproperty.length();
		// System.out.println(dnaproperty.map);
		// System.exit(-1);

		BufferedInputStream bis = new BufferedInputStream(data.stream(), 1024 * 1024);
		LineIterator it = new LineIterator(bis, true, true);
		String alignmentChr = null;
		for (String line : it) {
			if (line.startsWith("track")) {
				// Ignore for now
			} else if (line.startsWith("variableStep")) {
				alignmentChr = line.split("\\s+")[1].split("=")[1];
			} else {
				String[] arr = line.split("\t");
				int position = Integer.parseInt(arr[0]);
				double d = Double.parseDouble(arr[1]);
				addData(alignmentChr, position, position, new float[] { (float) d }, null);
			}

		}

	}

	private static HashMap<String, Integer> mapLength = new HashMap<String, Integer>();

	public static void convertWig2TDF(Locator data, File output) throws IOException, URISyntaxException {
		// this.dnaproperty = (ConversionMapDNAProperty) prop;
		BufferedInputStream bis = new BufferedInputStream(data.stream(), 1024 * 1024);
		LineIterator it = new LineIterator(bis);
		int max = 0;
		String gID = null;

		int pos = 0;
		String chr = null;
		for (String line : it) {
			if (line.startsWith("track")) {
				for (String a : line.split(" ")) {
					if (a.startsWith("name"))
						gID = a.split("=")[1];
				}
			} else if (line.startsWith("variableStep")) {
				if (chr != null) {
					mapLength.put(chr, pos);
					System.out.println("putting: "+chr+"\t"+pos);
					
				}
				chr = line.split("\\s+")[1].split("=")[1];
			} else {
				String[] arr = line.split("\t");
//				if (arr.length > 1)
//					chr = line.split("\\s+")[1].split("=")[1];
				try {
					pos = Integer.parseInt(arr[0]);
					if (pos > max)
						max = pos;
				} catch (NumberFormatException e) {
					System.err.println("boom!");
					// Seems we couldn't parse the number after all.
				}
			}

		}
		System.out.println("putting: "+chr+"\t"+pos);
		mapLength.put(chr, pos);

		int zoom = 0;
		while (max / 2 > 50000) {
			max /= 2;
			zoom++;
		}
		System.out.println("Zoom levels needed: " + zoom);

		ConvertWig2TDF cv = new ConvertWig2TDF(gID, output);
		cv.count(zoom, data);
		cv.finish();

	}

}
