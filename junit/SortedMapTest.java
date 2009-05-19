package junit;

import java.util.Collections;
import java.util.SortedMap;
import java.util.TreeMap;

public class SortedMapTest {

	public static void main(String[]args){
		SortedMap<Integer, String>map=new TreeMap<Integer, String>(Collections.reverseOrder());
		
		map.put(1, "A");
		map.put(2, "C");
		map.put(3, "G");
		map.put(4, "T");
		System.out.println(map);
		
	}
}
