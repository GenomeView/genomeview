/**
 * %HEADER%
 */
package net.sf.jannot.parser;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.logging.Logger;

import net.sf.jannot.DataKey;
import net.sf.jannot.Entry;
import net.sf.jannot.EntrySet;
import net.sf.jannot.StringKey;
import net.sf.jannot.Type;
import net.sf.jannot.parser.software.BlastM8Parser;
import net.sf.jannot.parser.software.BroadSolexa;
import net.sf.jannot.parser.software.FindPeaksParser;
import net.sf.jannot.parser.software.GeneMarkParser;
import net.sf.jannot.parser.software.MapViewParser;
import net.sf.jannot.parser.software.MaqSNPParser;
import net.sf.jannot.parser.software.MauveParser;
import net.sf.jannot.parser.software.SIPHTParser;
import net.sf.jannot.parser.software.TRNAscanParser;
import net.sf.jannot.parser.software.TransTermHPParser;
import be.abeel.io.LineIterator;

public abstract class Parser {

	public static final Parser GFF3 = new GFF3Parser();

	public static final Parser EMBL = new EMBLParser();

	private static Logger log = Logger.getLogger(Parser.class.toString());

	// FIXME this should be dynamically determined
	public static Parser[] parsers(Object source) {
		return new Parser[] { GFF3, new BEDParser(source.toString()), EMBL, new GTFParser(), new BlastM8Parser(), new FindPeaksParser(), new GeneMarkParser(),
				new MaqSNPParser(), new TransTermHPParser(), new TRNAscanParser(), new EMBLParser(), new FastaParser(), new GenbankParser(), new PTTParser(),
				new TBLParser(),new VCFParser(source.toString()),new WiggleParser() };
	}

	public Parser(DataKey dataKey) {
		this.dataKey = dataKey;
	}

	@Override
	public String toString() {
		return this.getClass().getName().replaceAll("net.sf.jannot.parser.", "");
	}

	/**
	 * Read all data from an input stream. Set the data source for each item to
	 * the supplied source. If and EntrySet is supplied the data will be added
	 * to this set, otherwise a new set will be created. Either the supplied or
	 * the new EntrySet is returned.
	 * 
	 * @param is
	 *            inputStream
	 * @param source
	 *            source to set to features
	 * @param set
	 *            TODO
	 * @param entrySet
	 *            the EntrySet to which all stuff will be added
	 * @return either the supplied EntrySet or a new one containing the loaded
	 *         stuff
	 */
	public abstract EntrySet parse(InputStream is, EntrySet set);

	/**
	 * Output everything from the provided entry to the output stream.
	 * 
	 * 
	 * @param os
	 *            output stream to write data to
	 * @param e
	 *            the entry to save
	 * @param source
	 *            the source to filter on, or null when no filtering is
	 *            required.
	 */
	public void write(OutputStream os, Entry entry) {
		write(os, entry, Type.values());
	}

	public void write(OutputStream os, Entry entry, DataKey[] dk) {
		// Do nothing by default, parser can choose to implement the write
		// method.

	}

	public static Parser detectParser(InputStream is, Object source) throws IOException {

		Parser p = findParser(is, source);
		log.info("parser: " + p);
		return p;

	}

	/**
	 * Method to automagically detect parsers.
	 * 
	 * @param is
	 * @param source
	 * @return
	 * @throws IOException
	 */
	private static Parser findParser(InputStream is, Object source) throws IOException {
		LineIterator it = new LineIterator(is);
		// it.setSkipComments(true);
		it.setSkipBlanks(true);
		String firstLine = it.next();
		String nonCommentLine = firstLine;

		// Skip comments and UCSC browser information lines
		while (nonCommentLine.startsWith("#") || nonCommentLine.startsWith("browser")) {
			nonCommentLine = it.next();

		}
		if(firstLine.contains("fileformat=VCF"))
			return new VCFParser(source.toString());
		
		if (firstLine.contains("Mauve1"))
			return new MauveParser(new StringKey(source.toString()));

		if (nonCommentLine.equals("id	chrom	start	end	max_coord"))
			return new FindPeaksParser();

		log.info("firstLine: " + firstLine);
		log.info("nonCommentLine: " + nonCommentLine);
		if (firstLine.startsWith("Guide for interpreting SIPHT output"))
			return new SIPHTParser(new StringKey(source.toString()));
		if (firstLine.startsWith("##maf"))
			return new MAFParser(new StringKey(source.toString()));
		if (nonCommentLine.startsWith("GeneMark"))
			return new GeneMarkParser();
		if (nonCommentLine.startsWith("TransTermHP"))
			return new TransTermHPParser();
		if (nonCommentLine.startsWith("gvheader:syntenic")) {
			return new SyntenicParser(new StringKey(source.toString()));
		}

		// System.out.println("Detect: " + line);
		if (nonCommentLine.startsWith("track")) {
			if (nonCommentLine.startsWith("track type=wiggle_0")) {
				return new WiggleParser();
			} else if (nonCommentLine.startsWith("track type=bedGraph")) {
				return new BedGraphParser(new StringKey(source.toString()));
			} else {
				nonCommentLine = it.next();
			}

		}

		if (nonCommentLine.startsWith("LOCUS"))
			return new GenbankParser();
		log.info("tab split nonCommentLine: " + nonCommentLine.split("\t").length);
		String[] nonCommentArr = nonCommentLine.split("\t");
		if (nonCommentArr.length == 9) {
			if (nonCommentArr[0].contains(".."))
				return new PTTParser();
			else {
				boolean no1 = nonCommentArr[1].matches("[0-9]+");
				boolean no2 = nonCommentArr[2].matches("[0-9]+");

				if (no1 && no2) {
					return new BEDParser(source.toString());
				} else if (nonCommentArr[8].contains("="))
					return new GFF3Parser();
				else
					return new GTFParser();

			}

		}

		if (nonCommentLine.split("[ \t]+").length == 8) {
			String[] head = new String[] { "Sequence", "tRNA", "Bounds", "tRNA", "Anti", "Intron", "Bounds", "Cove" };
			if (Arrays.equals(nonCommentLine.split("[ \t]+"), head)) {
				return new TRNAscanParser();
			}
		}

		/* Can either be BlastM8 or BED */
		if (nonCommentLine.split("\t").length == 12) {
			String[] arr = nonCommentLine.split("\t");
			try {
				Double.parseDouble(arr[4]);
			} catch (NumberFormatException ne) {
				return new BlastM8Parser();
			}
			char c = arr[5].charAt(0);
			if (c == '+' || c == '-' || c == '.')
				return new BEDParser(source.toString());

			try {
				Double.parseDouble(arr[9]);
				Double.parseDouble(arr[11]);
			} catch (NumberFormatException ne) {
				return new MaqSNPParser();
			}

			return new BlastM8Parser();
		}

		if (nonCommentLine.split("\t").length == 16) {
			return new MapViewParser(new StringKey(source.toString()));
		}
		if (nonCommentLine.startsWith("ID") || nonCommentLine.startsWith("FT") || nonCommentLine.startsWith("FH"))
			return new EMBLParser();

		if (nonCommentLine.startsWith(">")) {

			if (nonCommentLine.startsWith(">Feature "))
				return new TBLParser();
			else
				return specifyFastaType(nonCommentLine, source);

		}

		return null;
		// if (nonCommentLine.split("\t").length == 1) {
		// return new ALNParser(new StringKey(source.toString()));
		// }
		//
		// if (nonCommentLine.split("\t").length != 9)
		// return new BEDParser();

	}

	/*
	 * Data key for data types that require some external information to
	 * determine the name of the data
	 */
	protected DataKey dataKey = null;

	public void setDataKey(DataKey dk){
		this.dataKey=dk;
	}
	
	@Deprecated
	public void setDataKey(String s) {
		setDataKey(new StringKey(s));
	}

	private static Parser specifyFastaType(String line, Object source) {
		boolean broadShortRead = true;
		try {
			String[] arr = line.split(" ");
			/* The final thing on the header line should be the word 'mismatches */
			if (!arr[arr.length - 1].equals("mismatches)")) {
				broadShortRead = false;
			}
			/* Try to parse the number of mismatches */
			Integer.parseInt(arr[arr.length - 2].substring(1));
			/* Strand should be either 'fw' or 'rc' */
			if (!arr[2].equals("fw") && !arr[2].equals("rc"))
				broadShortRead = false;

		} catch (NumberFormatException e) {
			broadShortRead = false;
		} catch (IndexOutOfBoundsException e) {
			broadShortRead = false;
		}
		if (broadShortRead)
			return new BroadSolexa(new StringKey(source.toString()));
		else
			return new FastaParser(new StringKey(source.toString()));
	}

}
