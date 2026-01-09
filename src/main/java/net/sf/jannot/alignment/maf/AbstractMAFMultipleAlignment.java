/**
 * %HEADER%
 */
package net.sf.jannot.alignment.maf;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import net.sf.jannot.Data;

/**
 * @author Thomas Abeel
 * 
 */
public abstract class AbstractMAFMultipleAlignment implements Data<AbstractAlignmentBlock> {

	public String label() {
		return "Multiple alignment";
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = 6205720325158215670L;
	private Set<String> species = new HashSet<String>();

	/**
	 * @param string
	 */
	public void addSpecies(String string) {
		species.add(string);

	}

	public Collection<String> species() {
		return Collections.unmodifiableCollection(species);
	}

	@Override
	public abstract Iterable<AbstractAlignmentBlock> get(int start, int end);

	@Override
	public abstract Iterable<AbstractAlignmentBlock> get();

	@Override
	public boolean canSave() {
		return false;
	}

	public abstract int noAlignmentBlocks();

}
