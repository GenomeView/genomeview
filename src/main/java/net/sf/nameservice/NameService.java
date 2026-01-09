package net.sf.nameservice;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;

import net.sf.jannot.exception.ReadFailedException;
import be.abeel.io.LineIterator;

public class NameService {
	public static final HashMap<String, String> map = new HashMap<String, String>();

	static {
		try {
			resetDefault();

		} catch (Exception e) {
			e.printStackTrace();
			System.err
					.println("Failed to load naming service, synonyms won't work...");
		}
	}

	void printMapping() {
		System.out.println(map);
	}

	public static String getPrimaryName(String key) {
		key = key.trim();
		if (map.containsKey(key.toUpperCase()))
			return map.get(key.toUpperCase());
		else
			return key;
	}

	public static void resetDefault() throws ReadFailedException {
		map.clear();
		addSynonyms(NameService.class.getResourceAsStream("synonyms.txt"));
	}

	public static void addSynonym(String primary, String alt) {
		map.put(primary.trim().replace(' ', '_').toUpperCase(), primary.trim());
		String[] arr = alt.split(",");
		for (String s : arr) {
			map.put(s.trim().toUpperCase(),primary.trim());
			map.put(s.trim().replace(' ', '_').toUpperCase(), primary.trim());
		}

	}

	public static void addSynonyms(InputStream is) throws ReadFailedException {
		for (String line : new LineIterator(is, true, true)) {
			String[] prim = line.split("=");
			addSynonym(prim[0],prim[1]);
			
		}
		try {
			is.close();
		} catch (IOException ioe) {
			ioe.printStackTrace();
			System.err
					.println("Failed to close the file, probably synonyms will work anyway...");
		}
	}
}
