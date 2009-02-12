/**
 * %HEADER%
 */
package net.sf.genomeview.plugin;

import java.util.Iterator;
import java.util.Vector;

public class SimpleValueFeature implements IValueFeature {
    private double min;

    private double max;

    private String name = null;

    public SimpleValueFeature(String name) {
        values = new Vector<IValue>();
        this.name = name;
    }

    public void add(IValue e) {
        if (values.size() == 0) {
            min = e.getValue();
            max = e.getValue();
        } else {
            if (min > e.getValue())
                min = e.getValue();
            if (max < e.getValue())
                max = e.getValue();
        }

        values.add(e);
    }

    private Vector<IValue> values;

    public Iterator<IValue> iterator() {
        return values.iterator();
    }

    public String getName() {
        return name;
    }

    @Override
    public double max() {
        return max;
    }

    @Override
    public double min() {
        return min;
    }

  

}
