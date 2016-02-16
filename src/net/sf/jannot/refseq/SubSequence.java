/**
 * %HEADER%
 */
package net.sf.jannot.refseq;

/**
 * @author Thomas Abeel
 * 
 */
public class SubSequence extends Sequence {

	private int end;
	private int start;
	private Sequence seq;

	/**
	 * @param start
	 * @param end
	 */
	public SubSequence(Sequence s, int start, int end) {
		this.seq = s;
		this.start = start;
		this.end = end;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.sf.jannot.refseq.Sequence#get(int, int)
	 */
	@Override
	public Iterable<Character> get(int start, int end) {
		return seq.get(start + this.start, end);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.sf.jannot.refseq.Sequence#get()
	 */
	@Override
	public Iterable<Character> get() {
		return seq.get(this.start, this.end);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.sf.jannot.refseq.Sequence#size()
	 */
	@Override
	public int size() {
		return end-start;
	}

}
