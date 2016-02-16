/**
 * %HEADER%
 */
package net.sf.jannot.parser;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.StringReader;
import java.util.SortedSet;
import java.util.Vector;

import net.sf.jannot.DataKey;
import net.sf.jannot.Entry;
import net.sf.jannot.EntrySet;
import net.sf.jannot.Feature;
import net.sf.jannot.FeatureAnnotation;
import net.sf.jannot.Location;
import net.sf.jannot.MemoryFeatureAnnotation;
import net.sf.jannot.Strand;
import net.sf.jannot.Type;
import net.sf.jannot.refseq.MemorySequence;
import be.abeel.io.LineIterator;

/**
 * Parser for EMBL files.
 * 
 * EMBL Specifications:
 * ftp://ftp.ebi.ac.uk/pub/databases/embl/doc/usrman.txt
 * 
 * @author thabe, thpar
 *
 */
public class EMBLParser extends Parser {

	EMBLParser() {
		super(null);
	}

	@Override
	public EntrySet parse(InputStream is, EntrySet set) {
		if (set == null)
			set = new EntrySet();

		LineIterator it = new LineIterator(is);
		it.setSkipBlanks(true);
		int currentLine = 1;
		Entry entry = null;
		boolean seqMode = false;
		StringBuffer seqBuffer = new StringBuffer();
		for (String line : it) {
			if (line.startsWith("ID")) {
				seqMode = false;
				/* output last feature of previous entry */
				constructFeature(entry);
				storeSequence(entry, seqBuffer);
				entry = createNewEntry(line, set);
			} else if (currentLine == 1) {
				// No ID line in this file
				entry = set.iterator().next();
			}
			if (line.startsWith("XX") || line.startsWith("FH") || line.startsWith("AH") || line.startsWith("//")) {
				// do nothing, contains no data
			} else if (line.startsWith("FT")) {
				processFeatureLine(line, entry);
			} else if (line.startsWith("SQ")) {
				seqMode = true;
				// do nothing, this line is ignored
			} else {
				if (!seqMode)
					processLine(line, entry);
				else {
					String cline = condense(line);
					// assert(cline.length()==60);
					seqBuffer.append(cline);
				}

			}

			currentLine++;
		}
		/* output last feature of the file */
		constructFeature(entry);
		storeSequence(entry, seqBuffer);
		// /* Light mode, never encountered an ID line */
		// if (entry.getID().equals("defaultEMBLentry")) {
		// set.add(entry);
		// }

		return set;
	}

	private void storeSequence(Entry entry, StringBuffer seqBuffer) {
		if (entry != null && seqBuffer.length() > 0)
			entry.setSequence(new MemorySequence(seqBuffer.toString()));
		seqBuffer.setLength(0);

	}

	private String condense(String line) {
		return line.trim().substring(0, 65).replaceAll(" ", "");
	}

	private Vector<String> featureBuffer = new Vector<String>();

	private void processFeatureLine(String line, Entry entry) {

		String key = line.substring(5, 20).trim();
		if (!key.equals("")) {
			constructFeature(entry);
			featureBuffer.add(line);
		} else {
			featureBuffer.add(line);
		}

	}

	/* Create a feature from the data available in the feature buffer */
	private void constructFeature(Entry entry) {
		if (featureBuffer.size() > 0) {
			boolean buildingLocation = true;
			String type = featureBuffer.get(0).substring(5, 21).trim();
			StringBuffer location = new StringBuffer();
			Vector<StringBuffer> qualifiers = new Vector<StringBuffer>();
			for (int i = 0; i < featureBuffer.size(); i++) {
				String lc = featureBuffer.get(i).substring(21).trim();
				boolean slash = lc.startsWith("/");
				if (slash)
					buildingLocation = false;
				if (buildingLocation)
					location.append(lc);
				else {
					if (slash)
						qualifiers.add(new StringBuffer(lc));
					else
						qualifiers.lastElement().append("\n" + lc);
				}

			}
			featureBuffer.clear();
			try {
				SortedSet<Location> l = ParserTools.parseLocation(location.toString());
				Strand s = ParserTools.getStrand(location.toString());

				Feature f = new Feature();
				addQualifiers(qualifiers, f);
				f.setLocation(l);
				f.setStrand(s);

				f.setType(Type.get(type));
				MemoryFeatureAnnotation fa = entry.getMemoryAnnotation(f.type());
				fa.add(f);
				// entry.annotation.add(f);
			} catch (Exception e) {
				e.printStackTrace();
				System.err.println("Parser error! " + e);
				System.err.println("Location=" + location);
				System.err.println("Qualifiers=" + qualifiers);
				// System.err.println("Source=" + source);

			}

		}

	}

	private void addQualifiers(Vector<StringBuffer> qualifiers, Feature f) {
		// List<Qualifier> out = new Vector<Qualifier>();
		for (StringBuffer s : qualifiers) {
			String[] arr = s.toString().split("=");
			try {

				f.addQualifier(arr[0].substring(1).trim(), stripQuotes(arr[1].trim()));
			} catch (Exception e) {
				f.addQualifier("note", arr[0]);

			}
		}
		// return out;
	}

	private String stripQuotes(String trim) {
		return trim.replaceAll("\"", "");
	}

	private void processLine(String line, Entry entry) {

		if (line.startsWith("AC")) {
			String[] arr = line.substring(5).split(";");
			for (String s : arr) {
				entry.description.add("acc", s.trim());
			}

		} else if (line.startsWith("PR")) {
			entry.description.put("project identifier", line.substring(5));

		} else if (line.startsWith("DT")) {
			processDate(line, entry);
		} else if (line.startsWith("DE")) {
			entry.description.add("description", line.substring(5));
		} else if (line.startsWith("KW")) {
			entry.description.add("kw", line.substring(5));
		} else if (line.startsWith("OS")) {
			entry.description.add("os", line.substring(5));
		} else if (line.startsWith("OC")) {
			entry.description.add("oc", line.substring(5));
		} else if (line.startsWith("R")) {
			// TODO implement reference stuff
			// System.out.println("Ignoring reference line: " + line);
		} else if (line.startsWith("DR")) {
			// TODO implement database reference stuff
			// System.out.println("Ignoring database reference line: " + line);
		} else if (line.startsWith("CC")) {
			// TODO implement comments stuff
			System.out.println("Ignoring comments line: " + line);
		} else if (line.startsWith("AS")) {
			// TODO implement assembly stuff
			// System.out.println("Ignoring assembly line: " + line);
		} else if (line.startsWith("CO")) {
			// TODO implement construct stuff
			// System.out.println("Ignoring construct line: " + line);
		} else {
			System.err.println("Unrecognized line: " + line);
		}
	}

	/* Keeps track of the first data line, there should be two */
	private boolean firstDateLine = true;

	private void processDate(String line, Entry entry) {
		if (firstDateLine) {
			entry.description.put("first date", line.substring(5));
			firstDateLine = false;
		} else {
			entry.description.put("second date", line.substring(5));
		}

	}

	private Entry createNewEntry(String idLine, EntrySet set) {
		// Entry out = new Entry(source);
		String[] arr = idLine.substring(5).split(";");
		if (arr.length != 7) {
			System.err.println("The ID line is not conform the specifications. We can extract the ID, but other fields will be ignored.");
			System.err.println("\t" + idLine);
			String emergencyID = arr[0].split("\\s+")[0].trim();
			System.err.println("Extracted ID:" + emergencyID);
			return set.getOrCreateEntry(emergencyID);
		} else {
			Entry out = set.getOrCreateEntry(arr[0].trim());
			out.description.put("seqversion", arr[1].substring(3).trim());
			// if (!arr[2].trim().equals("linear"))
			// throw new
			// UnsupportedException("Only linear sequences are supported! Found "
			// + arr[2]);
			out.description.put("moleculeType", arr[3].trim());
			out.description.put("dataClass", arr[4].trim());
			out.description.put("taxDivision", arr[5].trim());
			return out;

		}

	}

	private static final String spacer = "   ";

	/* Indicates whether the sequence should be written to file */
	public boolean storeSequence = true;

	@Override
	public void write(OutputStream os, Entry e, DataKey[] dks) {

		PrintWriter out = new PrintWriter(new OutputStreamWriter(os));
		// if (source == null || source.equals(e.defaultSource)) {
		/* ID line */
		out.println("ID" + spacer + e.getID() + "; SV " + e.description.get("seqversion") + "; linear; "
				+ e.description.get("moleculeType") + "; " + e.description.get("dataClass") + "; " + e.description.get("taxDivision")
				+ "; " + e.sequence().size() + " BP.");
		out.println("XX");

		/* Accession line */
		String primaryAcc = e.getID();
		out.print("AC" + spacer + primaryAcc + "; ");
		if (e.description.get("acc") != null) {
			for (String acc : e.description.get("acc").split("\n")) {
				if (!acc.equals(primaryAcc))
					out.print(acc + "; ");
			}
		}
		out.println();
		out.println("XX");
		/* Date lines */
		out.println("DT" + spacer + e.description.get("first date"));
		out.println("DT" + spacer + e.description.get("second date"));
		out.println("XX");

		/* Description lines */
		if (e.description.get("description") != null) {
			for (String line : new LineIterator(new StringReader(e.description.get("description").toString()))) {
				out.println("DE" + spacer + line);
			}
			out.println("XX");
		}

		/* O lines */
		if (e.description.get("os") != null) {
			out.println("OS" + spacer + e.description.get("os"));
			out.println("XX");
		}
		if (e.description.get("oc") != null) {
			out.println("OC" + spacer + e.description.get("oc"));
			out.println("XX");
		}
		if (e.description.get("kw") != null) {
			out.println("KW" + spacer + e.description.get("kw"));
			out.println("XX");
		}
		// }
		/* Feature header */
		out.println("FH   Key             Location/Qualifiers");
		out.println("FH");
		// for (Type t : Type.values()) {
		// for (Feature f : e.annotation.getByType(t)) {
		// if (source == null | f.getSource().equals(source))
		// out.println(line(f));
		//
		// }
		//
		// }
		for (DataKey data : dks) {
			if (e.get(data) instanceof FeatureAnnotation) {
				MemoryFeatureAnnotation fa = e.getMemoryAnnotation((Type) data);
				for (Feature f : fa.get()) {
					out.println(line(f));
				}
			}

		}
		out.println("XX");
		if (storeSequence) {
			// if (source == null || source.equals(e.defaultSource)) {
			out.println("SQ   Sequence " + e.sequence().size() + " BP ; 0 A; 0 C; 0 G; 0 T; 0 other;");
			char[] line = new char[80];
			for (int i = 0; i < 80; i++)
				line[i] = ' ';
			int pos = 0;
			// for (int i = 1; i <= e.sequence().size(); i++) {
			int idx = 1;
			for (char c : e.sequence().get()) {
				line[pos++] = c;// e.sequence().getNucleotide(i);

				if (idx % 10 == 0)
					line[pos++] = ' ';
				if (idx % 60 == 0) {
					char[] number = new String("" + idx).toCharArray();
					for (int j = 0; j < number.length; j++)
						line[line.length - 1 - j] = number[number.length - 1 - j];
					pos = 0;
					out.println(line);
				}
				idx++;
			}
			for (int i = pos; i < 80; i++)
				line[i] = ' ';
			char[] number = new String("" + e.sequence().size()).toCharArray();
			for (int j = 0; j < number.length; j++)
				line[line.length - 1 - j] = number[number.length - 1 - j];
			out.println(line);
			// }
		}
		out.flush();

	}

	private String line(Feature f) {
		String ftstart = "FT";
		while (ftstart.length() < 21) {
			ftstart += " ";
		}
		String fType = f.type().toString().substring(0, Math.min(15, f.type().toString().length()));
		StringBuffer line = new StringBuffer("FT" + spacer + fType);
		while (line.length() < 21)
			line.append(" ");
		line.append(unparse(f.strand(), f.location()));
		StringBuffer out = new StringBuffer();
		out.append(line);
		for (String key : f.getQualifiersKeys()) {
			line = new StringBuffer("FT");
			while (line.length() < 21)
				line.append(" ");
			
			
			String qual = f.qualifier(key);
			if (qual == null)
				System.err.println("EMBLParse: " + f + "\t" + key + "\t" + qual);
			if (qual == null) {
				line.append("/" + key);
				out.append("\n" + line);
			} else {
				for (String q : qual.split(",")) {
					line.append("/" + key + "=" + q);
					String outline = line.toString().replaceAll("\n", "\n" + ftstart);
					out.append("\n" + outline);
				}
			}
		}

		return out.toString();

	}

	private String unparse(Strand s, Location[] location) {
		String ftstart = "FT";
		while (ftstart.length() < 21) {
			ftstart += " ";
		}
		StringBuffer loc = new StringBuffer();
		if (s == Strand.REVERSE) {
			loc.append("complement(");
		}
		if (location.length > 1) {
			loc.append("join(");
			for (Location l : location) {
				if (location[0].equals(l))
					loc.append(l);
				else
					loc.append(",\n" + ftstart + l);
			}
			loc.append(")");
		} else
			loc.append(location[0]);
		if (s == Strand.REVERSE)
			loc.append(")");
		return loc.toString();
	}

}
