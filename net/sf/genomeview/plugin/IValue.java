/**
 * %HEADER%
 */
package net.sf.genomeview.plugin;

import java.awt.Color;

import net.sf.jannot.Strand;

public interface IValue {

    /**
     * Return valued should be between 0 and 1
     * 
     * @return
     */
    public double getValue();

    public int getStart();

    public int getEnd();

    public Strand getStrand();

    public String getNote();
    
    public Color color();

}
