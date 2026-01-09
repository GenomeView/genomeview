/**
 * %HEADER%
 */
package net.sf.jannot.tabix;

import java.io.IOException;
import java.net.URISyntaxException;

import net.sf.jannot.Feature;
import net.sf.jannot.tabix.codec.BEDCodec;

/**
 * 
 * @author Thomas Abeel
 * 
 */
public class BEDWrapper extends FeatureWrapper {

	BEDWrapper(String key, IndexedFeatureFile data, TabIndex idx) {
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
			return new BEDCodec(this, data.query(key, start, end));
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