
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

/**
 * Created by IntelliJ IDEA.
 * User: martind
 * Date: Dec 20, 2009
 * Time: 10:37:43 PM
 * To change this template use File | Settings | File Templates.
 */
 package org.broad.igv.bbfile;

import java.util.logging.Logger;

/*
 *   Container class for B+ tree leaf node.
* */
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
public class BPTreeLeafNodeItem implements BPTreeNodeItem {

    private static Logger log = Logger.getLogger(BPTreeLeafNodeItem.class.getCanonicalName());
    private final boolean isLeafItem = true;
    private long leafIndex;    // leaf index in B+ tree item list

    // B+ Tree Leaf Node Item entities - BBFile Table G
    private String chromKey; // B+ tree node item is associated by key
    private int chromID;      // numeric mChromosome/contig ID
    private int chromSize;    // number of bases in mChromosome/contig

    /*
    *   Constructs a B+ tree leaf node item with the supplied information.
    *
    *   Parameters:
    *       leafIndex - leaf item index
    *       chromKey - chromosome/contig name key
    *       chromID - chromosome ID assigned to the chromosome name key
    *       chromsize - number of bases in the chromosome/contig
    * */
    public BPTreeLeafNodeItem(long leafIndex, String chromKey, int chromID, int chromSize){

        this.leafIndex = leafIndex;
        this.chromKey = chromKey;
        this.chromID = chromID;
        this.chromSize = chromSize;
    }

    /*
    *   Method returns the index assigned to this node item.
    *
    *   Returns:
    *       index assigned to this node item
    * */
    public long getItemIndex(){
        return leafIndex;
    }

    /*
    *   Method returns if this node is a leaf item.
    *
    *   Returns:
    *       true because node is a leaf item
    * */
    public boolean isLeafItem() {
        return isLeafItem;
    }

    /*
    *   Method returns the chromosome name key  assigned to this node item.
    *
    *   Returns:
    *       chromosome name key assigned to this node item
    * */
    public String getChromKey() {
        return chromKey;
    }

   /*
    *   Method compares supplied chromosome key with leaf node key.
    *
    *   Parameters:
    *       chromKey - chromosome name ley to compare
    *
    *   Returns:
    *       true, if keys are equal; false if keys are different
    * */
   
    public boolean chromKeysMatch(String chromKey) {
        String thisKey = this.chromKey;
        String thatKey = chromKey;

        if (thisKey.compareTo(thatKey) == 0)
            return true;
        else
            return false;
   }

    public void print() {

       log.info("B+ tree leaf node item number " + leafIndex);
       log.info("Key value = " + chromKey);
       log.info("ChromID = " + chromID);
       log.info("Chromsize = " + chromSize);
   }

    // *** BPTreeLeafNodeItem specific methods ***
    public int getChromID() {
       return chromID;
   }

   public int getChromSize() {
       return chromSize;
   }

}
