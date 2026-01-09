/**
 * %HEADER%
 */
package net.sf.jannot.parser.software;

import java.io.InputStream;

import net.sf.jannot.Entry;
import net.sf.jannot.EntrySet;
import net.sf.jannot.Feature;
import net.sf.jannot.Location;
import net.sf.jannot.MemoryFeatureAnnotation;
import net.sf.jannot.Type;
import net.sf.jannot.parser.Parser;
import be.abeel.io.LineIterator;

/**
 * 
 * Parser for the Maq SNP format.
 * 
 * Maq: Mapping and Assembly with Qualities
 * http://maq.sourceforge.net/maq-man.shtml
 * 
 * This parser handles the output of the cns2snp program of MAQ.
 * 
 * @author Thomas Abeel
 * 
 */
public class MaqSNPParser extends Parser {

	/**
	 * @param dataKey
	 */
	public MaqSNPParser() {
		super(null);
		// TODO Auto-generated constructor stub
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.sf.jannot.parser.Parser#parse(java.io.InputStream,
	 * net.sf.jannot.source.DataSource, net.sf.jannot.EntrySet)
	 */
	@Override
	public EntrySet parse(InputStream is, EntrySet set) {
		if (set == null)
			set = new EntrySet();
		LineIterator it = new LineIterator(is);
		it.setSkipBlanks(true);
		it.setSkipComments(true);
		Type t=Type.get("SNP");
		
		for(String line:it){
			String[]arr=line.split("\t");
			Entry e=set.getOrCreateEntry(arr[0]);
			MemoryFeatureAnnotation fa=e.getMemoryAnnotation(t);
			Feature f=new Feature();
			int pos=Integer.parseInt(arr[1]);
			f.addLocation(new Location(pos,pos));
			f.addQualifier("reference",arr[2]);
			f.addQualifier("consensus",arr[3]);
			f.addQualifier("phred-like consensus quality",arr[4]);
			f.addQualifier("read depth",arr[5]);
			f.addQualifier("average coverage",arr[6]);
			f.addQualifier("highests mapping quality",arr[7]);
			f.addQualifier("minimum consensus quality",arr[8]);
			f.addQualifier("second best call",arr[9]);
			f.addQualifier("log likelihood ratio second and third best call",arr[10]);
			f.addQualifier("third best call",arr[11]);
			f.setType(t);
			fa.add(f);
			
		}
		return set;
		
		
	}

	

}
