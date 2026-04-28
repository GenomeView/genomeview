/**
 * %HEADER%
 */
package net.sf.genomeview.data;

import net.sf.genomeview.gui.MessageManager;
import net.sf.jannot.Entry;
import net.sf.jannot.Global;

/**
 * An Entry indicating nothing is loaded
 * 
 * @author Thomas Abeel
 * 
 */
final public class DummyEntry extends Entry {

	public DummyEntry(Global global) {
		super(MessageManager.getString("dummyentry.nothing_loaded"), global);
	}

}