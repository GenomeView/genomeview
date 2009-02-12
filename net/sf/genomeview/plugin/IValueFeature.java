/**
 * %HEADER%
 */
package net.sf.genomeview.plugin;


public interface IValueFeature extends Iterable<IValue> {

    
    /**
     * Returns the maximum value that will be returned by the IValue features.
     * 
     * @return maximum value
     */
    public double max();
    /**
     * Returns the minimum value that will be returned by the IValue features.
     * 
     * @return minimum value
     */
    public double min();
        
    public String getName();
  
}
