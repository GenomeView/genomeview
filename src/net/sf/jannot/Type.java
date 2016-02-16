/**
 * %HEADER%
 */
package net.sf.jannot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 
 * I must improve this documentation
 * 
 * Class 'Type' contains list of group names which features can be classified.
 * e.g. CDS, intergenic.
 * 
 * Constructor : Type (string name);
 * 
 * Methods: get, toString, moveUp, moveDown
 * 
 * Obs: All previous declared Type names are uniquely stored in a static Type
 * array variable.
 * 
 * @author Thomas Abeel
 * 
 */

public class Type implements DataKey {

	private static ConcurrentHashMap<String, Type> map = new ConcurrentHashMap<String, Type>();

	private static List<Type> order = new ArrayList<Type>();
	static {

	}

	private String name;

	/**
	 * 
	 * Return array of declared types.
	 * 
	 * @return
	 */
	public static Type[] values() {
		return order.toArray(new Type[0]);

	}

	/**
	 * This method is deprecated. Use get(String key) instead.
	 * 
	 */
	@Deprecated
	public static Type valueOf(String s) {
		return get(s);
	}

	/**
	 * 
	 * Return key
	 * 
	 * @param key
	 * @return
	 */
	public synchronized static Type get(String key) {
		if (!map.containsKey(key)) {
			map.put(key, new Type(key));
			order.add(map.get(key));
		}
		return map.get(key);
	}

	private Type(String string) {
		this.name = string;

	}

	/**
 *     
 */
	public String toString() {
		return name;
	}

	/**
	 * Class Type stores all Types defined in a array. moveUp swap 'type' index
	 * with its previous one. If 'type' is the first index, nothing happens.
	 * 
	 * @param type
	 */
	public synchronized static void moveUp(Type type) {
		int index = order.indexOf(type);
		if (index > 0)
			Collections.swap(order, index - 1, index);

	}

	/**
	 * Class Type stores all Types defined in a array. moveDown swap 'type'
	 * index with its next one. If 'type' is the last index, nothing happens.
	 * 
	 * @param type
	 */
	public synchronized static void moveDown(Type type) {
		int index = order.indexOf(type);
		if (index < order.size() - 1)
			Collections.swap(order, index, index + 1);

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(DataKey o) {
		return o.toString().compareTo(this.toString());
	}
}
