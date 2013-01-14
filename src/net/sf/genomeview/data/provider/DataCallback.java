package net.sf.genomeview.data.provider;

import java.util.List;

import net.sf.jannot.Location;

/**
 * 
 * @author Thomas Abeel
 *
 * @param <T>
 */
public interface DataCallback<T> {

	
	public void dataReady(Location l,List<T>it);
}
