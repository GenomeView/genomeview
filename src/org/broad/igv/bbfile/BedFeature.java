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
*   Container class for BigBed features.
*
*   Note: required BigBed data items are:
*       mChromosome (name)
*       mChromosome mStartBase (starting base)
*       mChromosome mEndBase (ending base)
*       plus String "rest of fields" for custom fileds
*
*   Custom fields can follow any of the predefined fields which normally
*   follow the three required fields. Predefined fileds must be maintained
*   up to the point of customization.  (See BBFile Table A)
*
*   The predefined fields are:
*       name - name of feature
*       score - value betwenn 0 and 1000 defining viewing darkness
*       strand - "+" or "-", or "." for unknown
*       thickstart - base where item thickens, used for CDS mStartBase of genes
*       thickEnd - base where thick item ends
*       itemRGB - comma seperated R,G,B valuse from 0 to 255
*       blockCount - number of multi-part blocks; number of exons for genes
*       blockSizes - blockCount comma seperated list of blocks
*       blockStarts - blockCount comma seperated mStartBase locations (relative to mChromosome mStartBase)
*
*       Custom field dimensions are defined by the following fileds in BBFile Tab;e C:
 *          field count - number of fields in Bed format
 *          defined field count - number of fields that are of predefied type as shown above
*
*   Custom fields:
*       restOfFields (String contains the predefined and custom fields)
*
*   The custom fields are described by  .as dictionary terms which are
*   provided by the autoSQL section of the BigBed file. (See BBFile Table B example)
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
public class BedFeature {

    private static Logger log = Logger.getLogger(BedFeature.class.getCanonicalName());

    private int itemIndex;     // data record index

    // BBFile Table I - BigBed data format
    private String chromosome;      // mChromosome/contig name
    private int startBase;         // starting base for item
    private int endBase;           // ending base for item
    private String[] restOfFields;    // string containing custom fields

    public BedFeature(int itemIndex, String chromosome, int startBase, int endBase, String restOfFieldsString){

       this.itemIndex = itemIndex;
       this.chromosome =  chromosome;
       this.startBase =  startBase;
       this.endBase = endBase;
       restOfFields = restOfFieldsString == null ? null : restOfFieldsString.split("\t");
   }

   // returns the data record index
   public int getItemIndex() {
       return itemIndex;
   }

   // returns the mChromosome ID (0, 1, etc.)
   public String getChromosome() {
       return chromosome;
   }

   // returns the mChromosome mStartBase base position
   public int getStartBase(){
       return startBase;
   }

   // returns the mChromosome mEndBase base position
   public int getEndBase() {
       return endBase;
   }

    public String[] getRestOfFields(){
        return restOfFields;
    }

   public void print(){

       System.out.println("BigBed feature item " + itemIndex);
       System.out.println("mChromosome name: " + chromosome);
       System.out.println("mChromosome start base= " + startBase);
       log.info("mChromosome end base = " + endBase);
       System.out.println("Rest of fields: \n" + restOfFields);
   }
}
