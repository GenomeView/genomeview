/**
 * %HEADER%
 */
package net.sf.jannot.event;

import net.sf.jannot.Feature;

public abstract class FeatureEvent implements ChangeEvent {

    private Feature feature;
    
    private String msg = new String();

    public final Feature getFeature() {
        return feature;
    }

    public FeatureEvent(Feature f) {
        this.feature = f;
    }

    public FeatureEvent(Feature f, String msg) {
        this.feature = f;
        this.msg = msg;
    }
    
    @Override
    public String toString(){
    	return new String("Edit feature "+feature+" ("+msg+")");
    }
}
