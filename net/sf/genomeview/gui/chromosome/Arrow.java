/**
 * %HEADER%
 */
package net.sf.genomeview.gui.chromosome;

import java.awt.Polygon;

import net.sf.jannot.Strand;

/**
 * A directed arrow implementation.
 * 
 * @author thpar
 * 
 */
public class Arrow extends Polygon {
    private static final long serialVersionUID = 9101015401050447141L;

    public Arrow(int x, int y, int w, int h) {
        this(x, y, w, h, Strand.UNKNOWN);
    }

    public Arrow(int x, int y, int w, int h, Strand strand) {
        int headw = Math.min(20, w / 2);
        int edge = Math.min(15, h / 4);
        int headh = h;
        int lineh = headh - edge * 2;
        int linew = w - headw;

        if (strand == Strand.UNKNOWN || strand == Strand.FORWARD) {
            this.addPoint(x, y + edge);
            this.addPoint(x + linew, y + edge);
            this.addPoint(x + linew, y);
            this.addPoint(x + w, y + h / 2);
            this.addPoint(x + linew, y + h);
            this.addPoint(x + linew, y + edge + lineh);
            this.addPoint(x, y + edge + lineh);
            this.addPoint(x, y + edge);
        } else {
            this.addPoint(x, y + headh / 2);
            this.addPoint(x + headw, y);
            this.addPoint(x + headw, y + edge);
            this.addPoint(x + w, y + edge);
            this.addPoint(x + w, y + edge + lineh);
            this.addPoint(x + headw, y + edge + lineh);
            this.addPoint(x + headw, y + h);
            this.addPoint(x, y + headh / 2);
        }
    }

}
