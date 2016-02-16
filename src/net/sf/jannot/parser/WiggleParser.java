/**
 * %HEADER%
 */
package net.sf.jannot.parser;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import net.sf.jannot.Entry;
import net.sf.jannot.EntrySet;
import net.sf.jannot.StringKey;
import net.sf.jannot.wiggle.TroveArrayWiggle;
import be.abeel.io.LineIterator;
/**
 * 
 * @author Thomas
 *
 */
public class WiggleParser extends Parser {

	public WiggleParser() {
		super(null);
	}

	@Override
	public EntrySet parse(InputStream is, EntrySet set) {
		try {
			if (set == null)
				set = new EntrySet();
			LineIterator it = new LineIterator(is);
			it.setSkipComments(true);
			it.setCommentIdentifier("#");
			it.addCommentIdentifier("browser ");

			// FloatArrayList values = new FloatArrayList();
			TroveArrayWiggle daw = null;
			boolean variable = false;
			int step = 0;
			int span = 1;
			int start = 0;
			int stepOffset = 1;
			String name = "" + System.currentTimeMillis();
			Entry e = null;
			for (String line : it) {
				//System.out.println("Parsing: "+line);
				if (line.startsWith("track")) {
					Map<String,String>lineMap=BEDTools.parseTrack(line);
					name = lineMap.get("name");
					String chr=lineMap.get("chrom");
					e=set.getEntry(chr);

				} else if (line.startsWith("variableStep")) {
					if(e==null)
						e=set.iterator().next();
					add(e, name, daw);

					String[] arr = line.split("[ \t]+");
					span = 1;
					variable = true;

					for (String s : arr) {
						String[] kv = s.split("=");
						if (kv[0].equals("span")) {
							span = Integer.parseInt(kv[1].trim());
						}
						if (kv[0].equals("chrom")) {
							e = set.getOrCreateEntry(kv[1].trim());
							daw = new TroveArrayWiggle(e.getMaximumLength());

						}

					}
				} else if (line.startsWith("fixedStep")) {
					if(e==null)
						e=set.iterator().next();
					add(e, name, daw);

					String[] arr = line.split("[ \t]+");
					variable = false;

					stepOffset = 1;
					for (String s : arr) {
						String[] kv = s.split("=");
						if (kv[0].equals("span"))
							span = Integer.parseInt(kv[1]);
						if (kv[0].equals("step"))
							step = Integer.parseInt(kv[1]);
						if (kv[0].equals("start"))
							start = Integer.parseInt(kv[1]);
						if (kv[0].equals("chrom")) {
							e = set.getOrCreateEntry(kv[1].trim());
							daw = new TroveArrayWiggle(e.getMaximumLength());
						}
					}
				} else if (variable) {
					String[] arr = line.split("[ \t]+");
					int s = Integer.parseInt(arr[0]);
					double val = Double.parseDouble(arr[1]);
					
					for (int i = s; i < s + span; i++) {
						daw.set(i, (float) val);
					}
				} else {
					double val = Double.parseDouble(line);
					
					for (int i = start + stepOffset; i < start + stepOffset + span; i++) {
						daw.set(i, (float) val);
					}
					stepOffset += step;
				}
			}
			add(e, name, daw);
		} catch (IOException ioex) {
			throw new RuntimeException(ioex);
		}

		
		return set;
	}

	/**
	 * @param e
	 * @param name
	 * @param daw
	 */
	private void add(Entry e, String name, TroveArrayWiggle daw) {
		/* Add the previous one */
		if (daw != null) {
			daw.init();
			e.add(new StringKey(name), daw);
			
			daw = null;
		}

	}

	

}
