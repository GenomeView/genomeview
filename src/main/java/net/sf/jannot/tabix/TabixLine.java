/**
 * %HEADER%
 */
package net.sf.jannot.tabix;

import java.util.ArrayList;

/**
 * 
 * @author Thomas Abeel
 * 
 */
public class TabixLine {
	// /* Buffer for individual columns */
	private ArrayList<String> buffer = new ArrayList<String>();
	// /* Buffer for current column */
	// private StringBuffer current = new StringBuffer();
	int tid;
	public int beg;
	public int end;
	// private int bin;
	/* Indicates comment lines or other useless lines, like empty ones */
	boolean meta = false;
	private String line;

	// private String[] payload;

	public String line() {
		return line;
	}

	public String get(int idx) {
		return buffer.get(idx);
	}

	public int getInt(int idx) {
		return Integer.parseInt(get(idx));
	}

	public double getDouble(int idx) {
		return Double.parseDouble(get(idx));
	}

	// private int ti_reg2bin(int beg, int end) {
	// --end;
	// if (beg >>> 14 == end >>> 14)
	// return 4681 + (beg >>> 14);
	// if (beg >>> 17 == end >>> 17)
	// return 585 + (beg >>> 17);
	// if (beg >>> 20 == end >>> 20)
	// return 73 + (beg >>> 20);
	// if (beg >>> 23 == end >>> 23)
	// return 9 + (beg >>> 23);
	// if (beg >>> 26 == end >>> 26)
	// return 1 + (beg >>> 26);
	// return 0;
	// }

	// /**
	// * Add one single character to the column that's currently being read.
	// *
	// * @param c
	// */
	// public void add(char c) {
	// current.append(c);
	// }

	// /**
	// * Set the current column to the given string and jump to next column.
	// *
	// * @param col
	// */
	// public void setColumn(String col){
	// buffer.add(col);
	// current = new StringBuffer();
	// }

	// /**
	// * Go to the next column
	// *
	// */
	// public void next() {
	// buffer.add(current.toString());
	// current = new StringBuffer();
	//
	// }

	public void parse(TabIndex idx, char split) {
		// /* Make sure to add last column */
		// buffer.add(current.toString());
		// current=null;
		/* Handle empty lines */
		if (line.length() == 0) {
			this.meta = true;
			return;
		}
		StringBuffer tmp = new StringBuffer();
		for (int i = 0; i < line.length(); i++) {
			char c = line.charAt(i);
			if (c == split) {
				buffer.add(tmp.toString());
				tmp = new StringBuffer();
			} else {
				tmp.append(c);

			}

		}

		buffer.add(tmp.toString());
		
		this.tid = idx.names.indexOf(buffer.get((int) idx.sc - 1));
		this.beg = Integer.parseInt(buffer.get((int) idx.bc - 1).toString());
		if(idx.ec>0)
			this.end = Integer.parseInt(buffer.get((int) idx.ec - 1).toString());
		else
			this.end=beg;
		// this.bin = ti_reg2bin(this.beg, this.end);
		// payload=buffer.toArray(new String[0]);
		// buffer=null;
	}

	/**
	 * @return
	 */
	public int length() {
		return buffer.size();
	}

	/**
	 * @param line
	 */
	public void setLine(String line) {
		this.line = line;

	}

	// /**
	// * @return
	// */
	// public String[] getPayload() {
	// return payload;
	// }
}