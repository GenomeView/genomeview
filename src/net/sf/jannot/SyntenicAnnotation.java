/**
 * %HEADER%
 */
package net.sf.jannot;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
/**
 * 
 * @author Thomas Abeel
 *
 */
public class SyntenicAnnotation extends EntrySetAnnotation<SyntenicBlock> {

	private List<SyntenicBlock>syntenicBlocks=new ArrayList<SyntenicBlock>();
	@Override
	public List<SyntenicBlock> get(Entry e,Location l) {
		return syntenicBlocks;
	}
	private HashSet<String>targets=new HashSet<String>();
	@Override
	public void add(SyntenicBlock t) {
		syntenicBlocks.add(t);
		targets.add(t.target());
//		setChanged();
//		notifyObservers();
		
	}
	@Override
	public List<SyntenicBlock> getAll(Entry e) {
		return syntenicBlocks;
	}
	public Set<String> getTargets() {
		return targets;
	}
	@Override
	public Iterator<SyntenicBlock> iterator() {
		return syntenicBlocks.iterator();
	}
	/* (non-Javadoc)
	 * @see net.sf.jannot.EntrySetAnnotation#contains(java.lang.Object)
	 */
	@Override
	public boolean contains(SyntenicBlock t) {
		return syntenicBlocks.contains(t);
	}
	/**
	 * 
	 */
	public void clear() {
		syntenicBlocks.clear();
		
	}
}
