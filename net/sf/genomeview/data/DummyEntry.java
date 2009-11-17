/**
 * %HEADER%
 */
package net.sf.genomeview.data;

import net.sf.jannot.Entry;

/**
 * 
 * @author Thomas Abeel
 * 
 */
final public class DummyEntry extends Entry {
	
	public static Entry dummy=new DummyEntry();
	private DummyEntry() {
		super("Nothing loaded", null);
	}

}