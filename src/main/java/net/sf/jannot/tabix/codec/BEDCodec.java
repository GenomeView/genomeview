/**
 * %HEADER%
 */
package net.sf.jannot.tabix.codec;

import net.sf.jannot.Feature;
import net.sf.jannot.parser.BEDTools;
import net.sf.jannot.tabix.FeatureWrapper;
import net.sf.jannot.tabix.TabixLine;

/**
 * @author Thomas Abeel
 * 
 */
public class BEDCodec extends Codec<Feature> {

	
	private FeatureWrapper wrapper;

	/**
	 * @param in
	 */
	public BEDCodec(FeatureWrapper wrapper,Iterable<TabixLine> in) {
		super(in,1024);
		this.wrapper=wrapper;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.sf.jannot.tabix.codec.Codec#parse(java.lang.String)
	 */
	@Override
	public Feature parse(TabixLine line) {
		Feature f = BEDTools.parseLine(line.line(), null,null);
//		wrapper.update(f);
		return f;
	}

}
