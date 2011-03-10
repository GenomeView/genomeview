/**
 * %HEADER%
 */
package net.sf.genomeview.data.provider;

import java.util.ArrayList;
/**
 * 
 * @author Thomas Abeel
 *
 * @param <T>
 */
public abstract class AbstractDataProvider<T> implements DataProvider<T>{

	public Iterable<Status> getStatus(int start, int end){
		return new ArrayList<Status>();
	}
}
