/**
 * %HEADER%
 */
package net.sf.jannot.pileup;

/**
 * @author Thomas Abeel
 * 
 */
public class DoublePile extends SinglePile {
	public DoublePile(int pos, float val0, float val1) {
		super(pos, val0);
		this.val1 = val1;

	}

	private float val1;

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.sf.jannot.pileup.SinglePile#getValueCount()
	 */
	@Override
	public int getValueCount() {
		return 2;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.sf.jannot.pileup.SinglePile#getValue(int)
	 */
	@Override
	public float getValue(int i) {
		if (i == 0)
			return super.getValue(0);
		else if (i == 1)
			return val1;
		else
			throw new IndexOutOfBoundsException(
					"DoublePile only supports two value, you requested the value with index " + i);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.sf.jannot.pileup.SinglePile#getTotal()
	 */
	@Override
	public float getTotal() {
		return getValue(0) + getValue(1);
	}

}
