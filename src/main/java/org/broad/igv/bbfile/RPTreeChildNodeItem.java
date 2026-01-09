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
    Container class for R+ Tree Child format
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
public class RPTreeChildNodeItem implements RPTreeNodeItem {

    private static Logger log = Logger.getLogger(RPTreeChildNodeItem.class.getCanonicalName());
    private final boolean isLeafItem = false;

    // R+ child (non-leaf) node item entries: BBFile Table N
    private RPChromosomeRegion chromosomeBounds; // chromosome bounds for item
    private RPTreeNode childNode;  // child node assigned to node item

    /*  Constructor for child node items.
    *
    *   Parameters:
    *       itemIndex - index of item belonging to a child node
    *       startChromID - starting chromosome/contig for item
    *       startBase - starting base for item
    *       endChromID - ending chromosome/contig for item
    *       endBase - ending base for item
    *       childNode - child node item assigned to child node
    *
    * */
    public RPTreeChildNodeItem(int startChromID, int startBase,
                               int endChromID, int endBase, RPTreeNode childNode){


        chromosomeBounds = new RPChromosomeRegion(startChromID, startBase, endChromID, endBase);
        this.childNode = childNode;
    }

    public RPChromosomeRegion getChromosomeBounds() {
        return chromosomeBounds;
    }

    public RPTreeNode getChildNode() {
        return childNode;
    }

    public int compareRegions(RPChromosomeRegion chromosomeRegion){

        int value = chromosomeBounds.compareRegions(chromosomeRegion);
        return value;
    }

    public void print(){

        log.info("Child node item :\n");
        log.info(" StartChromID = " + chromosomeBounds.getStartChromID() + "\n");
        log.info(" StartBase = " + chromosomeBounds.getStartBase() + "\n");
        log.info(" EndChromID = " + chromosomeBounds.getEndChromID() + "\n");
        log.info(" EndBase = " + chromosomeBounds.getEndBase() + "\n");

        // child node specific entries
        childNode.printItems();
    }

}

