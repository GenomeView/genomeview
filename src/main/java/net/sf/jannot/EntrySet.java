/**
 * %HEADER%
 */
package net.sf.jannot;

import java.util.HashMap;
import java.util.Iterator;
import java.util.concurrent.ConcurrentSkipListSet;

import net.sf.nameservice.NameService;

/**
 * Top level class for JAnnot, this class represents a set of {@link Entry}.
 *   
 * 
 * @author Thomas Abeel
 *
 */
public class EntrySet implements Iterable<Entry>{
	
	/* EntrySet level annotation, typically annotation types spanning multiple entries like comparative data */
	final public SyntenicAnnotation syntenic = new SyntenicAnnotation();
	
	final public Description description = new Description();
	
	private ConcurrentSkipListSet<Entry> entries = new ConcurrentSkipListSet<Entry>();
	private HashMap<String,Entry> map = new HashMap<String,Entry>();
	
	/*
	 * This should be the only way to access the map as it does some key mapping
	 */
	private Entry mapGet(String key){
		Entry out=map.get(key);
		if(out==null)
			out=map.get(key.toLowerCase());
		if(out==null)
			out=map.get("chr"+key);
		if(out==null&&key.toLowerCase().startsWith("chr"))
			out=map.get(key.substring(3));
		
		return out;
	}
	

	@Override
	public Iterator<Entry> iterator() {
		return entries.iterator();
	}

	public synchronized Entry getOrCreateEntry(String string) {
		string=NameService.getPrimaryName(string);
		if(mapGet(string)==null){
			Entry e=new Entry(string);
			map.put(string, e);
			entries.add(e);
		}
		return mapGet(string);
	}
	
	public synchronized Entry firstEntry(){
		return entries.first();
	}
	public synchronized Entry getEntry(String string) {
		return mapGet(NameService.getPrimaryName(string));
		
	}

	public int size() {
		return map.size();
	}

	public void clear() {
		entries.clear();
		map.clear();
		syntenic.clear();
		
		
	}

	
}
