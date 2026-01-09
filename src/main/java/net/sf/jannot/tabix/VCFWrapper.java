/**
 * %HEADER%
 */
package net.sf.jannot.tabix;

import java.io.IOException;
import java.net.URISyntaxException;

import net.sf.jannot.Feature;
import net.sf.jannot.tabix.codec.BEDCodec;
import net.sf.jannot.variation.VCFCodec;
import net.sf.jannot.variation.Variation;

/**
 * 
 * @author Thomas Abeel
 * 
 */
public class VCFWrapper extends TabixWrapper<Variation> {

	VCFWrapper(String key, IndexedFeatureFile data, TabIndex idx) {
		super(key, data, idx);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.sf.jannot.Data#get(int, int)
	 */
	@Override
	public Iterable<Variation> get(int start, int end) {
		try {
			return new VCFCodec(this, data.query(key, start, end));
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