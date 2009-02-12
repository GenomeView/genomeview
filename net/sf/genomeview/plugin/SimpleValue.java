/**
 * %HEADER%
 */
package net.sf.genomeview.plugin;

import java.awt.Color;

import net.sf.jannot.Strand;



public class SimpleValue implements IValue {

    private int position;

    private double value;

    public SimpleValue(int position, double value) {
        super();
        this.position = position;
        this.value = value;
    }

    /**
     * Return valued should be between 0 and 1
     * 
     * @return
     */
    public double getValue() {
        return value;
    }

    public int getEnd() {
        return position;
    }

    public String getNote() {

        return null;
    }

    public int getStart() {
        return position;
    }

    public Strand getStrand() {
        return Strand.UNKNOWN;
    }

    @Override
    public Color color() {
       return Color.black;
    }

}
