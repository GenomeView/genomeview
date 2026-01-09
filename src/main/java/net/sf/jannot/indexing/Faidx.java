/**
 * %HEADER%
 */
package net.sf.jannot.indexing;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URISyntaxException;

import net.sf.jannot.source.Locator;

/**
 * @author Thomas Abeel
 * 
 */
public class Faidx {

	public static void index(Locator data, Locator index) throws URISyntaxException, IOException {

		PrintWriter out = new PrintWriter(index.file());
		BufferedInputStream bis = new BufferedInputStream(data.stream());
		byte[] buffer = new byte[8 * 1024];
		int read = bis.read(buffer);
		long filepos = 0;
		long entryLen = 0;
		// boolean newLine = false;
		boolean firstLine = false;
		boolean firstSpacer = false;
		boolean newEntry = true;
		long lineLen = 0;
		int spacer = 0;
		boolean firstEntry = true;
		boolean lineSet = false;
		// boolean firstChar = false;
		long oStart = 0;
		int oSpacer = 0;
		long oLen = 0;
		long oLine = 0;
		String oName = null;

		StringBuffer name = new StringBuffer();
		while (read > 0) {
			for (int i = 0; i < read; i++) {

				char c = (char) buffer[i];
				if (c == '>'&&!firstLine) {
					spacer = 0;
					// firstChar = true;
					firstSpacer = true;
					firstLine = true;
					lineSet = false;
					newEntry = true;
					name.setLength(0);
					// System.out.println("ELen=" + entryLen);
					if (!firstEntry) {
						oLen = entryLen;
						output(oSpacer, oLen, oLine, oName, oStart, out);

					}
					firstEntry = false;
					entryLen = 0;

				} else if (c == '\n' || c == '\r') {

					if (newEntry) {
						newEntry = false;
						oName = name.toString().trim();

					} else if (!firstLine && !lineSet) {
						oLine = lineLen;
						lineSet = true;
					}
					spacer++;
					lineLen = 0;

				} else {

					if (newEntry)
						name.append(c);
					else {
						if (lineSet&&firstSpacer) {
							oSpacer = spacer;
							firstSpacer = false;
						}
						if (firstLine) {
							oStart = filepos;
							firstLine = false;
						}

						lineLen++;
						entryLen++;
					}
					spacer = 0;
				}

				filepos++;

			}
			read = bis.read(buffer);
		}
		oLen = entryLen;
		output(oSpacer, oLen, oLine, oName, oStart, out);
		out.close();
	}

	/**
	 * @param oName
	 * @param oLine
	 * @param oLen
	 * @param oSpacer
	 * @param oStart
	 * @param out
	 * 
	 */
	private static void output(int oSpacer, long oLen, long oLine, String oName, long oStart, PrintWriter out) {
		out.println(oName + "\t" + oLen + "\t" + (oStart) + "\t" + oLine + "\t" + (oLine + oSpacer));

	}

}
