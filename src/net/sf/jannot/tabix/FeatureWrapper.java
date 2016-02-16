/**
 * %HEADER%
 */
package net.sf.jannot.tabix;

import net.sf.jannot.Feature;
import net.sf.jannot.FeatureAnnotation;

/**
 * @author Thomas Abeel
 * 
 */
public abstract class FeatureWrapper extends TabixWrapper<Feature> implements FeatureAnnotation {

	
	
//	private HashSet<String>qualifierKeys=new HashSet<String>();
	
	/**
	 * @param key
	 * @param data
	 * @param idx
	 */
	public FeatureWrapper(String key, IndexedFeatureFile data, TabIndex idx) {
		super(key, data, idx);
	}

//	public void update(Feature f) {
////		if (f.getScore() > maxScore)
////			maxScore = f.getScore();
////		if (f.getScore() < minScore)
////			minScore = f.getScore();
////		qualifierKeys.addAll(f.getQualifiersKeys());
//	}

//	private double minScore = Double.POSITIVE_INFINITY, maxScore = Double.NEGATIVE_INFINITY;

//	/*
//	 * (non-Javadoc)
//	 * 
//	 * @see net.sf.jannot.FeatureAnnotation#getMaxScore()
//	 */
//	@Override
//	public double getMaxScore() {
//		return maxScore;
//	}

//	/*
//	 * (non-Javadoc)
//	 * 
//	 * @see net.sf.jannot.FeatureAnnotation#getMinScore()
//	 */
//	@Override
//	public double getMinScore() {
//		return minScore;
//	}
	/* (non-Javadoc)
	 * @see net.sf.jannot.FeatureAnnotation#qualifierKeys()
	 */
//	@Override
//	public Set<String> qualifierKeys() {
//		return qualifierKeys;
//	}

}
