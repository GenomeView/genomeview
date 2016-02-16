/**
 * %HEADER%
 */
package net.sf.jannot.wiggle;

import net.sf.jannot.utils.ArrayIterable;

/**
 * 
 * @author Thomas Abeel
 *
 */
public abstract class AbstractWiggle implements Graph, Query {
	
	public String label(){
		return "wiggle";
	}
	
	private FloatCache buffer5 = null;

	private int lastStart=-1,lastEnd=-1,lastRes=-1;
	private float[]last=null;
	@Override
	public float[] get(int start, int end, int resolutionIndex) {
		if(buffer5==null)
			throw new RuntimeException("Wiggle needs to be initialized!");
		if(lastStart==start&&lastEnd==end&&lastRes==resolutionIndex)
			return last;
		if (resolutionIndex < 5) {
			last = getRawRange(start, end);

		} else {
			last = buffer5.getRawRange(start, end);
			resolutionIndex -= 5;

		}
		while (resolutionIndex > 0) {
			last = merge(last);
			resolutionIndex--;
		}
		return last;

	}

	private float[] merge(float[] ds) {
		float[] out = new float[(ds.length + 1) / 2];
		double max = 0;
		for (int i = 0; i < ds.length - 1; i += 2) {
			out[i / 2] = (ds[i] + ds[i + 1]) / 2;
			if (out[i / 2] > max)
				max = out[i / 2];
		}
		if (ds.length % 2 == 1)
			out[out.length - 1] = ds[ds.length - 1];
		// for (int i = 0; i < out.length; i++)
		// out[i] /= max;
		return out;
	}

//	@Override
//	public abstract String getName();

	@Override
	public abstract float max();

	@Override
	public abstract float min();

	@Override
	public abstract float[] getRawRange(int start, int end);

	@Override
	public abstract long size();

	public void init(Query source) {
		buffer5=new FloatCache(source);
		
	}
	
	

	/* (non-Javadoc)
	 * @see net.sf.jannot.Data#get(int, int)
	 */
	@Override
	public Iterable<Float> get(int start, int end) {
		float[]out=getRawRange(start, end);
		return new ArrayIterable<Float>(out);
	}
	
	
	
}
