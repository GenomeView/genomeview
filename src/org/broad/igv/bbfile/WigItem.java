/*
 * Copyright (c) 2007-2011 by The Broad Institute of MIT and Harvard.  All Rights Reserved.
 *
 * This software is licensed under the terms of the GNU Lesser General Public License (LGPL),
 * Version 2.1 which is available at http://www.opensource.org/licenses/lgpl-2.1.php.
 *
 * THE SOFTWARE IS PROVIDED "AS IS." THE BROAD AND MIT MAKE NO REPRESENTATIONS OR
 * WARRANTES OF ANY KIND CONCERNING THE SOFTWARE, EXPRESS OR IMPLIED, INCLUDING,
 * WITHOUT LIMITATION, WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR
 * PURPOSE, NONINFRINGEMENT, OR THE ABSENCE OF LATENT OR OTHER DEFECTS, WHETHER
 * OR NOT DISCOVERABLE.  IN NO EVENT SHALL THE BROAD OR MIT, OR THEIR RESPECTIVE
 * TRUSTEES, DIRECTORS, OFFICERS, EMPLOYEES, AND AFFILIATES BE LIABLE FOR ANY DAMAGES
 * OF ANY KIND, INCLUDING, WITHOUT LIMITATION, INCIDENTAL OR CONSEQUENTIAL DAMAGES,
 * ECONOMIC DAMAGES OR INJURY TO PROPERTY AND LOST PROFITS, REGARDLESS OF WHETHER
 * THE BROAD OR MIT SHALL BE ADVISED, SHALL HAVE OTHER REASON TO KNOW, OR IN FACT
 * SHALL KNOW OF THE POSSIBILITY OF THE FOREGOING.
 */

package org.broad.igv.bbfile;

import java.util.logging.Logger;

/**
 * File retrieved from BigWig project at Google code on July 26, 2011, revision
 * 36
 * 
 * http://code.google.com/p/bigwig/
 * 
 * This project provides java readers for the UCSC's BigWig and BigBed formats.
 * It was originally developed by Martin Decautis and Jim Robinson for the
 * Integrative Genomics Viewer (http://www.broadinstitute.org/igv). Thanks to
 * Jim Kent and Ann Zweig and from UCSC for their assistance.
 * 
 * 
 * Modification to work with GenomeView by Thomas Abeel.
 * 
 * @author Martin Decautis
 * @author Jim Robinson
 * @author Thomas Abeel
 * 
 */
public class WigItem {

    private static Logger log = Logger.getLogger(WigItem.class.getCanonicalName());

    private int itemIndex;         // wig section item index number
    private String chromosome;     // mChromosome name
    private int startBase;         // mStartBase base position for feature
    private int endBase;           // mEndBase base position for feature
    private float wigValue;        // wig value

    public WigItem(int itemIndex, String chromosome, int startBase, int endBase, float wigValue){

        this.itemIndex = itemIndex;
        this.chromosome = chromosome;
        this.startBase = startBase;
        this.endBase = endBase;
        this.wigValue = wigValue;
    }

    public int getItemNumber(){
        return itemIndex;
    }

    public String getChromosome() {
        return chromosome;
    }

    public int getStartBase() {
        return startBase;
    }

    public int getEndBase() {
        return endBase;
    }

    public float getWigValue() {
        return wigValue;
    }

     public void print(){
       log.info("Wig item index " + itemIndex);
       log.info("mChromosome name: " + chromosome);
       log.info("mChromosome start base = " + startBase);
       log.info("mChromosome end base = " + endBase);
       log.info("Wig value: \n" + wigValue);
   }
}
