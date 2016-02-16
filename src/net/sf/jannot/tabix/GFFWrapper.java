/**
 * %HEADER%
 */
package net.sf.jannot.tabix;

import java.io.IOException;
import java.net.URISyntaxException;

import net.sf.jannot.Feature;
import net.sf.jannot.FeatureAnnotation;
import net.sf.jannot.Location;
import net.sf.jannot.tabix.codec.GFFCodec;
/**
 */
public class GFFWrapper extends FeatureWrapper  {

	public GFFWrapper(String key, IndexedFeatureFile data, TabIndex idx) {
		super(key, data, idx);
	}


	
	/*
	 * (non-Javadoc)
	 * 
	 * @see net.sf.jannot.Data#get(int, int)
	 */
	@Override
	public Iterable<Feature> get(int start, int end) {
		try {
			return new GFFCodec(this,data.query(key, start, end));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}



	



}