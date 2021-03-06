/*
 * Copyright (c) 2007-2010 by The Broad Institute, Inc. and the Massachusetts Institute of Technology.
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

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.broad.igv.track;

/**
 * @author jrobinso
 */
public enum WindowFunction {

    mean("Mean"),
    median("Median"),
    min("Minimum"),
    max("Maximum"),
    percentile2("2nd Percentile"),
    percentile10("10th Percentile"),
    percentile90("90th Percentile"),
    percentile98("98th Percentile"),
//    stddev("Standard Deviation"),
    count("Count");
//    density("Density");

    private String displayName = "";

    WindowFunction(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    static public WindowFunction getWindowFunction(String name) {

        WindowFunction windowFunction = null;
        if (WindowFunction.mean.name().equalsIgnoreCase(name)) {
            windowFunction = WindowFunction.mean;
        } else if (WindowFunction.median.name().equalsIgnoreCase(name)) {
            windowFunction = WindowFunction.median;
        } else if (WindowFunction.min.name().equalsIgnoreCase(name)) {
            windowFunction = WindowFunction.min;
        } else if (WindowFunction.max.name().equalsIgnoreCase(name)) {
            windowFunction = WindowFunction.max;
        } else if (WindowFunction.percentile10.name().equalsIgnoreCase(name)) {
            windowFunction = WindowFunction.percentile10;
        } else if (WindowFunction.percentile90.name().equalsIgnoreCase(name)) {
            windowFunction = WindowFunction.percentile90;
        } else if (WindowFunction.percentile98.name().equalsIgnoreCase(name)) {
            windowFunction = WindowFunction.percentile98;
//        } else if (WindowFunction.stddev.name().equals(name)) {
//            windowFunction = WindowFunction.stddev;
        } else if (WindowFunction.count.name().equalsIgnoreCase(name)) {
            windowFunction = WindowFunction.count;
//        } else if (WindowFunction.density.name().equals(name)) {
//            windowFunction = WindowFunction.density;
        }
        return windowFunction;
    }
}
