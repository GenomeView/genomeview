/*
 * Copyright (c) 2011 by The Broad Institute of MIT and Harvard
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
package org.broad.tools;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collection;

import net.sf.samtools.SAMFileReader;
import net.sf.samtools.SAMFileReader.ValidationStringency;

import org.broad.igv.track.WindowFunction;

/**
 * Program to create tdf files from bam files.
 * 
 * @author Thomas Abeel
 * 
 */
public class ConvertBAM2TDF {

	public static void main(String[] args) throws Exception {
		if (args.length == 0) {
			printUsage();
		}
		for (String s : args) {
			if (!new File(s + ".bai").exists()) {
				System.err.println("WARNING: Could not find BAI file for " + s);
				System.err
						.println("\ttdformat needs a BAI file for each BAM file.");
			} else {
				try {
					createFile(s);
				} catch (Exception e) {
					System.err.println("ERROR: Failed to create TDF file for "
							+ s);
				}
			}
		}
	}

	private static void printUsage() {
		System.out
				.println("Usage: java -jar tdformat-<version>.jar <bam file 1> [<bam file 2> ...]");
		System.out.println("\ttdformat needs a BAI file for each BAM file.");

	}

	private static void createFile(String ifile) throws IOException,
			URISyntaxException {
		Collection<WindowFunction> wfs = new ArrayList<WindowFunction>();
		for (WindowFunction wf : WindowFunction.values())
			wfs.add(wf);
		SAMFileReader
				.setDefaultValidationStringency(ValidationStringency.SILENT);
		TDFTools igvTools = new TDFTools();
		igvTools.doCount(ifile, ifile + ".tdf", wfs);

	}

}
