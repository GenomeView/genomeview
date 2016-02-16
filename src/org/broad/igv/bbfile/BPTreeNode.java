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

 /*
 *  Interface defining the B+ Tree Node behavior
*
*   Note: Key is a property of node items and BPTreeNode methods
*       getLowestChromKey() and getHighestChromKey() can be used
*       to check node key range.
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
public interface BPTreeNode {

    /*
    *   Method returns the node index in the B+ tree organization.
    *
    *   Returns:
    *       node index in B+ tree
    * */
    public long getNodeIndex();


    /*
    *   Method identifies the node as a leaf node or a child (non-leaf) node.
    *
    *   Returns:
    *       true, if leaf node; false if child node
    * */
    public boolean isLeaf();

    /*
    *   Method inserts the node item appropriate to the item's key value.
    *
    *   Returns:
    *       Node item inserted successfully.
    * */
    public boolean insertItem(BPTreeNodeItem item);

    /*
    *   Method deletes the node item appropriate to the item's index.
    *
    *   Returns:
    *       Node item deleted successfully.
    * */
    public boolean deleteItem(int index);

    /*
    *   Method returns the number of items assigned to the node.
    *
    *   Returns:
    *       Count of node items contained in the node
    * */
    public int getItemCount();

    /*
    *   Method returns the indexed node item.
    *
    *   Returns:
    *       Indexed node item.
    * */
    public  BPTreeNodeItem getItem(int index);

    /*
    *   Method returns the lowest chromosome name key belonging to the node.
    *
    *   Returns:
    *       Lowest contig/chromosome name key; or null for no node items.
    * */
    public  String getLowestChromKey();

    /*
    *   Method returns the highest chromosome name key belonging to the node.
    *
    *   Returns:
    *       Highest contig/chromosome name key; or null for no node items.
    * */
    public  String getHighestChromKey();

    /*
    *   Method returns the lowest chromosome ID belonging to the node.
    *
    *   Returns:
    *       Lowest contig/chromosome ID; or -1 for no node items.
    * */
    public  int getLowestChromID();

    /*
    *   Method returns the highest chromosome ID  belonging to the node.
    *
    *   Returns:
    *       Highest contig/chromosome ID; or -1 for no node items.
    * */
    public  int getHighestChromID();

    /*
    *   Method prints the nodes items and sub-node items.
    *       Node item deleted successfully.
    * */
    public void printItems();

}
