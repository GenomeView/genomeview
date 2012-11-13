/**
 * %HEADER%
 */
package net.sf.genomeview.core;

public enum DisplayType {
    /*
     * All information of a track should be displayed as a single line of
     * blocks.
     * 
     */
    OneLineBlocks,
    /*
     * All information is displayed in tiled blocks.
     * 
     */
    MultiLineBlocks,
    /*
     * The information is displayed as a graph
     * 
     * 
     */
    LineProfile,

    /*
     * The information is displayed as a bar chart.
     * 
     */
    BarchartProfile,

    /*
     * Display the information color coded.
     * 
     */
    ColorCodingProfile;
}
