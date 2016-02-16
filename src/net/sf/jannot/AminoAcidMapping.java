/**
 * %HEADER%
 */
package net.sf.jannot;

import java.util.HashMap;

import be.abeel.io.LineIterator;
import be.abeel.util.DefaultHashMap;

/**
 * Enumerates different genetic codes.
 * 
 * Source of genetic codes:
 * http://www.ncbi.nlm.nih.gov/Taxonomy/Utils/wprintgc.cgi
 * 
 * @author Thomas Abeel
 * 
 */
public enum AminoAcidMapping {

	STANDARDCODE("standard"), YEASTMITOCHONDRIAL("yeastMitochondrial"), INVERTEBRATEMITOCHONDRIAL(
			"invertebrateMitochondrial"), EUPLOTIDNUCLEAR("euplotidNuclear"), TABLE11(
			"table11"), TABLE12("table12"), TABLE13("table13"), TABLE14(
			"table14"), TABLE15("table15"), TABLE16("table16"), TABLE21(
			"table21"), TABLE22("table22"), TABLE23("table23"), TABLE4("table4"), TABLE6(
			"table6"), TABLE9("table9"), VERTEBRATEMITOCHONDRIAL(
			"vertebrateMitochondrial");

	/*
	 * Maps codons to amino acids.
	 */
	private HashMap<String, Character> mapping = new HashMap<String, Character>();
	/* Maps codons to whether they are valid starts */
	private HashMap<String, Boolean> startmap = new DefaultHashMap<String, Boolean>(
			Boolean.FALSE);
	/*
	 * The full name of a genetic code. This is the first line in the
	 * description file, if one is present.
	 */
	private String fullName = null;
	/*
	 * Contains a complete description of the genetic code
	 */
	private String description = null;

	private AminoAcidMapping(String resource) {
		LineIterator it = new LineIterator(AminoAcidMapping.class
				.getResourceAsStream("aamapping/" + resource));
		it.setSkipBlanks(true);
		char[] aa = it.next().split("=")[1].trim().toCharArray();
		char[] start = it.next().split("=")[1].trim().toCharArray();
		char[] base1 = it.next().split("=")[1].trim().toLowerCase()
				.toCharArray();
		char[] base2 = it.next().split("=")[1].trim().toLowerCase()
				.toCharArray();
		char[] base3 = it.next().split("=")[1].trim().toLowerCase()
				.toCharArray();
		for (int i = 0; i < aa.length; i++) {
			mapping.put("" + base1[i] + base2[i] + base3[i], aa[i]);
			if (start[i] != '-')
				startmap.put("" + base1[i] + base2[i] + base3[i], true);
		}
		try {
			it = new LineIterator(AminoAcidMapping.class
					.getResourceAsStream("aamapping/" + resource
							+ ".description"));
			String line = it.next();
			fullName = line;
			StringBuffer buf = new StringBuffer();
			while (it.hasNext()) {
				line = it.next();
				if (line.startsWith("\\*")) {
					line = "<b>" + line + "</b>";
				}
				buf.append(line + "\n");

			}
			description = buf.toString();
		} catch (NullPointerException e) {
			/* There may not be a description for this particular encoding */
			// XXX Once all genetic codes have a description, this can be
			// removed
		}
	}

	@Override
	public String toString() {
		if (fullName != null)
			return fullName;
		else
			return super.toString();
	}

	public String getDescription() {
		return description;
	}

	public char get(String codon) {
		Character c = mapping.get(codon.toLowerCase());
		if (c == null)
			return 'X';
		else
			return c;
	}

	public boolean isStart(String codon) {
		return startmap.get(codon.toLowerCase());
	}

	public boolean isStop(char aa) {
		return aa=='*';
	}

}
