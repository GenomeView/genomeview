/**
 * %HEADER%
 */
package net.sf.jannot.pileup;

import cern.colt.Arrays;


/**
 * @author Thomas Abeel
 * 
 */
public class MultiPile extends DoublePile {

	private float[] val;

	private double sum=0;
	
	public String toString(){
		return super.start()+"="+Arrays.toString(val);
		
	}
	
	public MultiPile(int pos, float[] arr) {
		super(pos, arr.length>0?arr[0]:Float.NaN, arr.length>1?arr[1]:Float.NaN);
		this.val=arr;
		for(float f:val)
			sum+=f;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.sf.jannot.pileup.DoublePile#getValueCount()
	 */
	@Override
	public int getValueCount() {
		return val.length;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.sf.jannot.pileup.DoublePile#getValue(int)
	 */
	@Override
	public float getValue(int i) {
		return val[i];
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.sf.jannot.pileup.DoublePile#getTotal()
	 */
	@Override
	public float getTotal() {
		return (float)sum;
	}

}
