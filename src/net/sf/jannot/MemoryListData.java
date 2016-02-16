/**
 * %HEADER%
 */
package net.sf.jannot;

import java.util.ArrayList;


/**
 * Data that is stored in memory in a list of a particular type
 */
public abstract class MemoryListData<T> extends ArrayList<T> implements Data<T>  {

	public void addAll(MemoryListData<T> t) {
		Iterable<T> list = t.get();
		addAll(list);
	}

	public void addAll(Iterable<T> list) {
		for (T t : list)
			this.add(t);
	}



	

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.sf.jannot.Data#get(int, int)
	 */
	@Override
	public Iterable<T> get(int start, int end) {
		return new LocatedListIterable(this, new Location(start,end));
		
				
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.sf.jannot.Data#get()
	 */
	@Override
	public Iterable<T> get() {
		return new ListIterable<T>(this);
	}

	/* (non-Javadoc)
	 * @see net.sf.jannot.Data#canSave()
	 */
	@Override
	public boolean canSave() {
		return false;
	}

//	/**
//	 * @return
//	 */
//	public int size() {
//		return list.size();
//	}
//
//	/**
//	 * @param row
//	 * @return
//	 */
//	public T get(int row) {
//		return list.get(row);
//	}
//
//	/**
//	 * @param first
//	 * @return
//	 */
//	public int indexOf(T o) {
//		return list.indexOf(o);
//	}

}
