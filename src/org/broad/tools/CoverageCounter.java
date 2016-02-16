/*
 * Copyright (c) 2007-2010 by The Broad Institute, Inc. and the Massachusetts Institute of Technology.
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

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.broad.tools;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import net.sf.samtools.AlignmentBlock;
import net.sf.samtools.SAMFileReader;
import net.sf.samtools.SAMRecord;
import net.sf.samtools.SAMRecordIterator;
import net.sf.samtools.SAMSequenceDictionary;
import net.sf.samtools.SAMSequenceRecord;

/**
 *   TODO -- normalize option
 *
 -n           Normalize the count by the total number of reads. This option
 multiplies each count by (1,000,000 / total # of reads). It
 is useful when comparing multiple chip-seq experiments when
 the absolute coverage depth is not important.
 */

/**
 * @author jrobinso
 * @author Thomas Abeel
 */
class CoverageCounter {

	private String alignmentFile;
	private Preprocessor consumer;
	private float[] buffer;
	private int windowSize = 1;
	// TODO -- make mapping qulaity a parameter
	private int minMappingQuality = 0;
	// FIXME What's this supposed to do?
	// private int strandOption = -1;
	private int extFactor;
	private int totalCount = 0;

	private SAMSequenceDictionary genome;

	CoverageCounter(String alignmentFile, Preprocessor consumer, int windowSize, int extFactor, File wigFile,
			SAMSequenceDictionary genome2) {
		/* This should be a BAM file */
		this.alignmentFile = alignmentFile;
		this.consumer = consumer;
		this.windowSize = windowSize;
		this.extFactor = extFactor;
		// this.wigFile = wigFile;
		this.genome = genome2;
		// this.strandOption = strandOption;
		buffer = new float[2];// strandOption < 0 ? new float[1] : new float[2];
	}

	private boolean passFilter(SAMRecord alignment) {

		return !alignment.getReadUnmappedFlag() && !alignment.getDuplicateReadFlag()
				&& alignment.getMappingQuality() >= minMappingQuality;
	}

	void parse() throws IOException, URISyntaxException {

		int tolerance = (int) (windowSize * (Math.floor(extFactor / windowSize) + 2));

		SAMFileReader sfr = new SAMFileReader(new File(alignmentFile));

		// DataSourceFactory.createFile(new File(alignmentFile +
		// ".bai")).read(genome);

		String lastChr = "";
		ReadCounter counter = null;

		for (SAMSequenceRecord e : genome.getSequences()) {
			SAMRecordIterator it = sfr.queryOverlapping(e.getSequenceName(), 1, e.getSequenceLength());
			// for (DataKey dk : e) {
			// Data<?> data = e.get(dk);
			// if (data instanceof ReadGroup) {
			// for (SAMRecord alignment : ) {
			while (it.hasNext()) {
				SAMRecord alignment = it.next();
				if (passFilter(alignment)) {

					totalCount++;

					String alignmentChr = e.getSequenceName();

					if (alignmentChr.equals(lastChr)) {
						if (counter != null) {
							counter.closeBucketsBefore(alignment.getAlignmentStart() - tolerance);
						}
					} else {
						if (counter != null) {
							counter.closeBucketsBefore(Integer.MAX_VALUE);
						}
						counter = new ReadCounter(alignmentChr);
						lastChr = alignmentChr;
					}

					AlignmentBlock[] blocks = alignment.getAlignmentBlocks().toArray(new AlignmentBlock[0]);
					if (blocks != null) {
						for (AlignmentBlock block : blocks) {

							int adjustedStart = block.getReferenceStart();// block.getStart();
							// FIXME Is this the correct coordinate?
							int adjustedEnd = block.getReferenceStart() + block.getLength();// block.getEnd();
							if (alignment.getReadNegativeStrandFlag()) {
								adjustedStart = Math.max(0, adjustedStart - extFactor);
							} else {
								adjustedEnd += extFactor;
							}

							for (int pos = adjustedStart; pos < adjustedEnd; pos++) {

								if (!alignment.getReadNegativeStrandFlag())
									counter.incrementCount(pos);
								else
									counter.incrementNegCount(pos);
							}
						}
					} else {
						int adjustedStart = alignment.getAlignmentStart();
						// FIXME is this correct?
						int adjustedEnd = alignment.getAlignmentEnd();
						if (alignment.getReadNegativeStrandFlag()) {
							adjustedStart = Math.max(0, adjustedStart - extFactor);
						} else {
							adjustedEnd += extFactor;
						}

						for (int pos = adjustedStart; pos < adjustedEnd; pos++) {
							if (!alignment.getReadNegativeStrandFlag())
								counter.incrementCount(pos);
							else
								counter.incrementNegCount(pos);
						}
					}
				}

				// }
				// }

			}
			it.close();
		}

		if (counter != null) {
			counter.closeBucketsBefore(Integer.MAX_VALUE);
		}

		consumer.setAttribute("totalCount", String.valueOf(totalCount));
		// FIXME??consumer.parsingComplete();
		// FIXME?? consumer.finish();//

	}

	class ReadCounter {

		String chr;
		TreeMap<Integer, Counter> counts = new TreeMap<Integer, Counter>();

		ReadCounter(String chr) {
			this.chr = chr;
		}

		void incrementCount(int position) {
			Integer bucket = position / windowSize;
			if (!counts.containsKey(bucket)) {
				counts.put(bucket, new Counter());
			}
			counts.get(bucket).increment();
		}

		void incrementNegCount(int position) {
			Integer bucket = position / windowSize;
			if (!counts.containsKey(bucket)) {
				counts.put(bucket, new Counter());
			}
			counts.get(bucket).incrementNeg();
		}

		void closeBucketsBefore(int position) {
			List<Integer> bucketsToClose = new ArrayList<Integer>();

			Integer bucket = position / windowSize;
			for (Map.Entry<Integer, Counter> entry : counts.entrySet()) {
				if (entry.getKey() < bucket) {

					// Divide total count by window size. This is the average
					// count per
					// base over the window, so 30x coverage remains 30x
					// irrespective of window size.
					int bucketStartPosition = entry.getKey() * windowSize;
					int bucketEndPosition = bucketStartPosition + windowSize;
					if (genome != null) {
						SAMSequenceRecord chromosome = genome.getSequence(chr);
						if (chromosome != null) {
							bucketEndPosition = Math.min(bucketEndPosition, chromosome.getSequenceLength());
						}
					}
					int bucketSize = bucketEndPosition - bucketStartPosition;

					buffer[0] = ((float) entry.getValue().getCount()) / bucketSize;
					buffer[1] = ((float) entry.getValue().getNegCount()) / bucketSize;

					consumer.addData(chr, bucketStartPosition, bucketEndPosition, buffer, null);

					bucketsToClose.add(entry.getKey());
				}
			}

			for (Integer key : bucketsToClose) {
				counts.remove(key);
			}

		}
	}

	private class Counter {

		int count = 0;
		int negCount = 0;

		void increment() {
			count++;
		}

		void incrementNeg() {
			negCount++;
		}

		int getCount() {
			return count;
		}

		int getNegCount() {
			return negCount;
		}
	}

}
