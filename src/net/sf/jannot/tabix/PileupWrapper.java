/**
 * %HEADER%
 */
package net.sf.jannot.tabix;

import java.io.IOException;
import java.net.URISyntaxException;

import net.sf.jannot.pileup.DoublePile;
import net.sf.jannot.pileup.PileNormalization;
import net.sf.jannot.tabix.codec.PileupCodec;

public class PileupWrapper extends TabixWrapper<DoublePile> implements PileNormalization {

	PileupWrapper(String key, IndexedFeatureFile data, TabIndex idx) {
		super(key, data, idx);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.sf.jannot.Data#get(int, int)
	 */
	@Override
	public Iterable<DoublePile> get(int start, int end) {
		try {
			return new PileupCodec(data.query(key, start, end));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public boolean supportsNormalization() {
		return false;
	}

}