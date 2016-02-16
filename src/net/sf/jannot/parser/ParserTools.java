/**
 * %HEADER%
 */
package net.sf.jannot.parser;

import java.util.SortedSet;
import java.util.TreeSet;

import net.sf.jannot.Location;
import net.sf.jannot.Strand;

public class ParserTools {
	public static Strand getStrand(String location) {
		if (location.toString().contains("complement"))
			return Strand.REVERSE;
		else
			return Strand.FORWARD;
	}

	public static SortedSet<Location> parseLocation(String location) {
		SortedSet<Location> out = new TreeSet<Location>();
		String t = location.toString().replaceAll("complement\\((.*?)\\)", "$1");
		String y = t.replaceAll("order\\((.*?)\\)", "$1");
		String x = y.replaceAll("join\\((.*?)\\)", "$1");
		
		String[] arr = x.split(",");
		for (String s : arr) {
			String[] pos = s.split("\\.\\.");
			int start, end;
			boolean fuzzyStart = false, fuzzyEnd = false;
			if (pos[0].startsWith("<")) {

				start = Integer.parseInt(pos[0].substring(1));
				fuzzyStart = true;
			} else
				start = Integer.parseInt(pos[0]);

			if (pos.length == 1) {
				end=start;
					
			} else if (pos[1].startsWith(">")) {

				end = Integer.parseInt(pos[1].substring(1));
				fuzzyEnd = true;
			} else
				end = Integer.parseInt(pos[1]);
			out.add(new Location(start, end, fuzzyStart, fuzzyEnd));

		}

		return out;
	}

}
