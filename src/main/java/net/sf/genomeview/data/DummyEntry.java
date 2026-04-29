/**
 * %HEADER%
 */
package net.sf.genomeview.data;

import net.sf.genomeview.core.Globals;
import net.sf.jannot.Entry;

/**
 * An Entry indicating nothing is loaded
 * 
 * @author Thomas Abeel
 * 
 */
final public class DummyEntry extends Entry {

	public DummyEntry(Globals global) {
		super(global.getMessageManager().getString("dummyentry.nothing_loaded"),
				global.getGlobal());
	}

}