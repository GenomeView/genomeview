/**
 * %HEADER%
 */
package net.sf.jannot.variation;

import java.util.HashMap;

import net.sf.jannot.Feature;
import net.sf.jannot.Location;
import net.sf.jannot.Type;
import net.sf.jannot.parser.BEDTools;
import net.sf.jannot.tabix.FeatureWrapper;
import net.sf.jannot.tabix.TabixLine;
import net.sf.jannot.tabix.VCFWrapper;
import net.sf.jannot.tabix.codec.Codec;


/**
 * @author Thomas Abeel
 * 
 */
public class VCFCodec extends Codec<Variation> {

	
	private VCFWrapper wrapper;

	/**
	 * @param in
	 */
	public VCFCodec(VCFWrapper vcfWrapper,Iterable<TabixLine> in) {
		super(in,1024);
		this.wrapper=vcfWrapper;
	}
	
	

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.sf.jannot.tabix.codec.Codec#parse(java.lang.String)
	 */
	@Override
	public Variation parse(TabixLine line) {
		Variation f = new VCFVariation(line);
//		f.setType(Type.get("SNP"));
//		f.setLocation(new Location(line.beg,line.end));
//		f.addQualifier("ref", line.get(3));
//		if(line.get(4).charAt(0)!='.')
//			f.addQualifier("alt", line.get(4));
		
		
//		wrapper.update(f);
		return f;
	}

}
