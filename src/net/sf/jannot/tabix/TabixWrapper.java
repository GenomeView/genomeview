/**
 * %HEADER%
 */
package net.sf.jannot.tabix;

import net.sf.jannot.Data;
import net.sf.jannot.DensityEstimate;
import net.sf.jannot.Location;

public abstract class TabixWrapper<T> implements Data<T>, DensityEstimate {

	public boolean canSave(){
		return false;
	}
	private static final int RECORDSIZE = 12;
	protected String key;
	protected IndexedFeatureFile data;
	protected TabIndex idx;

	public String label(){
		String s=data.source();
		int bIndx=s.lastIndexOf('\\');
		int fIdx=s.lastIndexOf('/');
		int idx=Math.max(bIndx, fIdx);
		return s.substring(idx+1);
		
	}
	
	TabixWrapper(String key, IndexedFeatureFile data, TabIndex idx) {
		this.data = data;
		this.key = key;
		this.idx = idx;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.sf.jannot.Data#get()
	 */
	@Override
	public Iterable<T> get() {
		return get(1, getMaximumCoordinate());

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * net.sf.jannot.DensityEstimate#getEstimateCount(net.sf.jannot.Location)
	 */
	@Override
	public int getEstimateCount(Location l) {
		int tid = idx.names.indexOf(key);
		long max = 0;
		long min = Long.MAX_VALUE;
		for (int i = 0; i < idx.linIndex[tid].size(); i++) {
			long val = idx.linIndex[tid].get(i) >> 16;
			if (val > 0 && val < min)
				min = val;
			if (val > max)
				max = val;
		}

		double fraction = l.length() / (double) getMaximumCoordinate();
		int estimate = (int) (fraction * (max - min) / RECORDSIZE);
		return estimate;

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.sf.jannot.DensityEstimate#getMaximumCoordinate()
	 */
	@Override
	public int getMaximumCoordinate() {
		int tid = idx.names.indexOf(key);
		return idx.linIndex[tid].size() * 16 * 1024;
	}

}