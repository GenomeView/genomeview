/**
 * %HEADER%
 */
package net.sf.jannot.shortread;

import net.sf.jannot.Strand;
/**
 * 
 * @author Thomas Abeel
 *
 */
public class BasicShortRead extends ShortRead {

	
	/*
	 * This contains the start coordinate of the short read. Also the
	 * orientation is encoded in this value.
	 * 
	 * The coordinate is one based
	 * 
	 * Negative values indicate reverse strand, positive ones forward strand
	 */
	private int mapStart;
	/* Length of the short read */
	private short length;

	private byte[] seq;

	public BasicShortRead(char[] seq, int mapStart, boolean forward) {
		byte[] tmp = new byte[seq.length];
		for (int i = 0; i < seq.length; i++)
			tmp[i] = (byte) seq[i];
		init(tmp, mapStart, forward);
	}

	public BasicShortRead(byte[] seq, int mapStart, boolean forward) {
		init(seq, mapStart, forward);
	}

	private void init(byte[] seq, int mapStart, boolean forward) {
		if (forward)
			this.mapStart = mapStart;
		else
			this.mapStart = -mapStart;

		if (seq == null) {
			this.seq = null;
			this.length = 0;
			return;
		}
		if (seq.length > Short.MAX_VALUE)
			throw new RuntimeException("Short reads should be shorter than "+Short.MAX_VALUE+" nucleotides");
		length = (short) seq.length;
		/* Convert sequence to byte[] */
		this.seq = new byte[(int) Math.ceil(seq.length / 4.0)];
		for (int i = 0; i < seq.length; i++) {
			byte c = seq[i];
			byte bc = encode(c);
			byte bc2 = (byte) (bc << (i % 4) * 2);
			this.seq[i / 4] |= bc2;
			// System.out.println("adding: " + c + "\t" +
			// Integer.toBinaryString(0xFF & bc) + "\t" +
			// Integer.toBinaryString(0xFF & bc2) + "\t" +
			// Integer.toBinaryString(0xFF & this.seq[i / 4]));
		}
	}

	/*
	 * These should be one-based coordinates
	 */
	public char getNucleotide(int pos) {
		return nuc(pos);
	}

	final private char nuc(int pos) {
		if (pos > length) {
			throw new RuntimeException("Position outside this read length: pos=" + pos + "; length=" + length);
		} else {
			/* One based coordinate, correct for it! */
			pos--;
			byte b = this.seq[pos / 4];
			int mask = (3 << (pos % 4) * 2);
			int b2 = (b & mask);
			int b3 = (b2 >>> (pos % 4) * 2);
			// System.out.println("decoding: " + (pos+1) + "\t" +
			// Integer.toBinaryString(0xFF & b) + "\t" +
			// Integer.toBinaryString(0xFF & mask) + "\t" +
			// Integer.toBinaryString(0xFF & b2) + "\t" +
			// Integer.toBinaryString(0xFF & b3));
			return decode((byte) b3);
		}
	}

	private char decode(byte b) {
		switch (b) {
		case 0:
			return 'A';
		case 1:
			return 'C';

		case 2:
			return 'G';
		case 3:
			return 'T';
		}
		throw new RuntimeException("Unknown byte value for decoding: " + b);
	}

	private byte encode(byte c) {
		switch (c) {
		case 'a':
		case 'A':
			return 0;
		case 'c':
		case 'C':
			return 1;
		case 'g':
		case 'G':
			return 2;
		case 't':
		case 'T':
			return 3;
		}
		throw new RuntimeException("Unknown character for encoding: " + (char)c);
	}

	

	
	public int start() {
		return Math.abs(mapStart);
	}

	public int end() {
		return start() + length - 1;
	}

	public Strand strand() {
		if (mapStart < 0) {
			return Strand.REVERSE;
		} else
			return Strand.FORWARD;
	}

	public int length() {
		return length;
	}

	// @Override
	// public String toString() {
	//
	// }

}

