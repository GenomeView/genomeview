/**
 * %HEADER%
 */
package net.sf.jannot;


/* Represents the annotation of a single type */

public class MemoryFeatureAnnotation extends MemoryListData<Feature> implements FeatureAnnotation {

	/* All data that is kept in memory, these get added manually */
	// private MemoryListData<Feature> memoryData = new
	// MemoryListData<Feature>();

//	private HashSet<String> qualifierKeys = new HashSet<String>();

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.sf.jannot.Data#get(int, int)
	 */
	@Override
	public Iterable<Feature> get(int start, int end) {
		return new LocatedListIterable<Feature>(this, new Location(start, end));

		// return memoryData.get();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.sf.jannot.Data#get()
	 */
	@Override
	public Iterable<Feature> get() {
		return super.get();
	}

	private double minStart = Integer.MAX_VALUE;
	private double maxEnd = 0;

	private String label = null;

	/**
	 * @param f
	 * @return
	 */
	public synchronized boolean add(Feature f) {
		if (label == null)
			label = f.type().toString();
		super.add(f);
//		qualifierKeys.addAll(f.getQualifiersKeys());
		if (f.start() < minStart)
			minStart = f.start();
		if (f.end() > maxEnd)
			maxEnd = f.end();
//		if (f.getScore() > maxScore)
//			maxScore = f.getScore();
//		if (f.getScore() < minScore)
//			minScore = f.getScore();
		return true;
	}

	/**
	 * @param f
	 */
	public synchronized void remove(Feature f) {
		super.remove(f);

	}

	/**
	 * Returns the number of features that resides in memory.
	 * 
	 * @return
	 */
	public int cachedCount() {
		return super.size();
	}

	/**
	 * @param row
	 * @return
	 */
	public Feature getCached(int row) {
		return super.get(row);
	}

	/**
	 * @param first
	 * @return
	 */
	public int getCachedIndexOf(Feature first) {
		return super.indexOf(first);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * net.sf.jannot.DensityEstimate#getEstimateCount(net.sf.jannot.Location)
	 */
	@Override
	public int getEstimateCount(Location l) {
		if(cachedCount()<200)
			return 0;
		double d = cachedCount() / (maxEnd - minStart);
		
		int estMemory = (int) (l.length() * d);
	
		return estMemory;

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.sf.jannot.DensityEstimate#getMaximumCoordinate()
	 */
	@Override
	public int getMaximumCoordinate() {
		return (int) maxEnd;
	}

//	private double minScore = Double.POSITIVE_INFINITY;
//	private double maxScore = Double.NEGATIVE_INFINITY;

//	/*
//	 * (non-Javadoc)
//	 * 
//	 * @see net.sf.jannot.FeatureAnnotation#getMaxScore()
//	 */
//	@Override
//	public double getMaxScore() {
//		return maxScore;
//	}
//
//	/*
//	 * (non-Javadoc)
//	 * 
//	 * @see net.sf.jannot.FeatureAnnotation#getMinScore()
//	 */
//	@Override
//	public double getMinScore() {
//		return minScore;
//	}

	@Override
	public String toString() {
		if (super.size() > 0)
			return super.get(0).type().toString();
		else
			return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.sf.jannot.FeatureAnnotation#qualifierKeys()
	 */
//	@Override
//	public Set<String> qualifierKeys() {
//		return qualifierKeys;
//	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.sf.jannot.Data#canSave()
	 */
	@Override
	public boolean canSave() {
		if (super.size() > 0)
			return true;
		else
			return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.sf.jannot.Data#label()
	 */
	@Override
	public String label() {
		if (label == null)
			return "no data";
		else
			return label;
	}

}
