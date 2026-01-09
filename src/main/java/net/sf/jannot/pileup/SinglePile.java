/**
 * %HEADER%
 */
package net.sf.jannot.pileup;

import net.sf.jannot.Located;

/**
 * 
 * Pile representing a single value.
 * 
 * @author Thomas Abeel
 * 
 */
public class SinglePile implements Located,Pile {
	/* 1-based location of this pile */
	private int pos = 0;

	private int len = 1;

	private float val0 = 0;

//	private float fcoverage = 0;

//	private byte[] bases = null;

//	public int getPos() {
//		return pos;
//	}

	public int getValueCount(){
		return 1;
	}
	
	public float getValue(int i) {
		if(i==0)
			return val0;
		else
			throw new IndexOutOfBoundsException("Single pile only has one value");
	}

//	public float getFCoverage() {
//		return fcoverage;
//	}

	public float getTotal() {
		return val0;
	}

	/**
	 * Gives you the actual bases that are present in this pile. This method may
	 * return <code>null</code> if the summary format does not have base
	 * information.
	 * 
	 * @return
	 */
	public byte[] getBases() {
		return null;
	}

	public void setLength(int len) {
		this.len = len;
	}

	public int getLength() {
		return len;
	}

	public SinglePile(int pos, float val) {
		this.pos = pos;
		this.val0=val;
		
	}

//	/**
//	 * Creates a pile up for read data from a pile up file
//	 * 
//	 * @param pos
//	 *            position of the pile
//	 * @param reads
//	 *            read data
//	 */
//	public SinglePile(int pos, byte[] reads) {
//
//		this.pos = pos;
//		this.rcoverage = 0;
//		this.fcoverage = 0;
//		for (int i = 0; i < reads.length; i++) {
//			byte c = reads[i];
//			if (c == '^')
//				i++;
//			else if (c == '-' || c == '+') {
//				int jump = reads[++i];
//				try {
//					i += Integer.parseInt("" + (char) jump);
//				} catch (NumberFormatException ne) {
//					System.err.println("NFE: " + pos);
//				}
//			} else if (c == '.' || c == 'A' || c == 'C' || c == 'G' || c == 'T' || c == 'N') {
//				fcoverage++;
//			} else if (c == ',' || c == 'a' || c == 'c' || c == 'g' || c == 't' || c == 'n') {
//				rcoverage++;
//			} else {
//				// System.err.println("PILE: unknown char: "+(char)c);
//			}
//
//		}
//
//		this.bases = reads;
//	}

	
	/*
	 * (non-Javadoc)
	 * 
	 * @see net.sf.jannot.Located#start()
	 */
	@Override
	public int start() {
		return pos;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.sf.jannot.Located#end()
	 */
	@Override
	public int end() {
		return pos + len;
	}

}
