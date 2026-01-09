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

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.util.logging.Logger;

import org.broad.tribble.SeekableStreamUtils;

import org.broad.tribble.LittleEndianInputStream;
import net.sf.samtools.seekablestream.SeekableStream;

/*
 *  Container class for BBFile B+ Tree header.
 *  B+ Tree Header can be constructed by reading values in from a BBFile
  *  (  Table E), or by assigning the values in a constructor.
 *
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
public class  BPTreeHeader {

    private static Logger log = Logger.getLogger(BPTreeHeader.class.getCanonicalName());

    static public final int BPTREE_HEADER_SIZE = 32;

    static public final int BPTREE_MAGIC_LTH = 0x78CA8C91;
    static public final int BPTREE_MAGIC_HTL = 0x918CCA78;

    private long headerOffset;     // BBFile file offset for mChromosome tree
    private boolean headerOK;      // B+ Tree header OK?
    
    // Chromosome B+ Tree Header - Table E
    private int magic;        // magic number identifies it as B+ header
    private int blockSize;    // number of children per block
    private int keySize;      // min # of charcter bytes for mChromosome name
    private int valSize;      // size of (bigWig) values - currently 8
    private long itemCount;   // number of chromosomes/contigs in B+ tree
    private long reserved;    // Currently 0

   /*
   *    Constructor for reading in a B+ tree header a from a file input stream.
   *
   *    Parameters:
   *        fis - file input handle
   *        fileOffset - file offset to the B+ tree header
   *        isLowToHigh - indicates byte order is low to high, else is high to low
   * */
    public BPTreeHeader(SeekableStream fis, long fileOffset, boolean isLowToHigh) {

        long itemsCount;

       // save the seekable file handle  and B+ Tree file offset
       headerOffset = fileOffset;

       // Note: a bad B+ Tree header will result in false returned
       headerOK =  readHeader(fis, headerOffset, isLowToHigh);
    }



    public static int getHeaderSize() {
        return BPTREE_HEADER_SIZE;
    }

    public long getHeaderOffset() {
        return headerOffset;
    }

     public boolean isHeaderOK() {
        return headerOK;
    }

    public int getMagic() {
        return magic;
    }

    public int getBlockSize() {
        return blockSize;
    }

    public int getKeySize() {
        return keySize;
    }

    public int getValSize() {
        return valSize;
    }

    public long getItemCount() {
        return itemCount;
    }

    public long getReserved() {
        return reserved;
    }

    // prints out the B+ Tree Header
     public void print() {

        // Chromosome B+ Tree  Header - BBFile Table E
        if(headerOK)
            log.info("B+ Tree Header was read from file location " + headerOffset);
        log.info(" Magic ID =" + magic);
        log.info(" Block size = " + blockSize);
        log.info(" Key size = " + keySize);
        log.info(" Indexed value size = " + valSize);
        log.info(" Item Count = " + itemCount);
        log.info(" Reserved = " + reserved);
    }
    
   /*
   * Reads in the B+ Tree Header.
   * Returns status of B+ tree header read; true if read, false if not.
   * */
    private boolean readHeader(SeekableStream fis, long fileOffset, boolean isLowToHigh) {

        LittleEndianInputStream lbdis;
        DataInputStream bdis;

         byte[] buffer = new byte[BPTREE_HEADER_SIZE];
         int bytesRead;
    
        try {
            // Read B+ tree header into a buffer
            fis.seek(fileOffset);
//            fis.readFully(buffer);
            SeekableStreamUtils.readFully(buffer, fis);
        
            // decode header
            if(isLowToHigh){
                lbdis = new LittleEndianInputStream(new ByteArrayInputStream(buffer));

                // check for a valid B+ Tree Header
                magic = lbdis.readInt();

                if(magic != BPTREE_MAGIC_LTH)
                    return false;

                // Get mChromosome B+ header information
                blockSize = lbdis.readInt();
                keySize = lbdis.readInt();
                valSize = lbdis.readInt();
                itemCount = lbdis.readLong();
                reserved = lbdis.readLong();
            }
            else {
                bdis = new DataInputStream(new ByteArrayInputStream(buffer));

                // check for a valid B+ Tree Header
                magic = bdis.readInt();

                if(magic != BPTREE_MAGIC_HTL)
                    return false;

                // Get mChromosome B+ header information
                blockSize = bdis.readInt();
                keySize = bdis.readInt();
                valSize = bdis.readInt();
                itemCount = bdis.readLong();
                reserved = bdis.readLong();

            }

        }catch(IOException ex) {
           log.severe("Error reading B+ tree header " + ex);
           throw new RuntimeException("Error reading B+ tree header \n", ex);
            }

        // success
         return true;
    }

}

