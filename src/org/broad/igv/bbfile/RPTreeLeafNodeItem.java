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


/*
    Container class for R+ tree leaf node data locator.
*
*   Note: Determination of data item as  BigWig data or BigBed data
*           depends on whether the file is BigWig of Table J format
*           or BigBed of Tble I format.
 */
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
public class RPTreeLeafNodeItem extends RPChromosomeRegion implements RPTreeNodeItem {

    private static Logger log = Logger.getLogger(RPTreeLeafNodeItem.class.getCanonicalName());

    private long dataOffset;      // file offset to data item
    private long dataSize;        // size of data item

    /*  Constructor for leaf node items.
    *
    *   Parameters:
    *       itemIndex - index of item belonging to a leaf node
    *       startChromID - starting chromosome/contig for item
    *       startBase - starting base for item
    *       endChromID - ending chromosome/contig for item
    *       endBase - ending base for item
    *       dataOffset - file location for leaf chromosome/contig data
    *       dataSize - size of (compressed) leaf data region in bytes
    *
    * */

    public RPTreeLeafNodeItem(int startChromID, int startBase,
                              int endChromID, int endBase, long dataOffset, long dataSize) {
        super(startChromID, startBase, endChromID, endBase);
        this.dataOffset = dataOffset;
        this.dataSize = dataSize;
    }

 
    public RPChromosomeRegion getChromosomeBounds() {
        return this;
    }

    public void print() {

        log.info("R+ tree leaf node data item ");
        log.info("StartChromID = " + getStartChromID());
        log.info("StartBase = " + getStartBase());
        log.info("EndChromID = " + getEndChromID());
        log.info("EndBase = " + getEndBase());

        // leaf node specific entries
        log.info("DataOffset = " + dataOffset);
        log.info("DataSize = " + dataSize);
    }

    // *** RPTreeLeafNodeItem specific methods ***

    public long getDataOffset() {
        return dataOffset;
    }

    public long geDataSize() {
        return dataSize;
    }

}
