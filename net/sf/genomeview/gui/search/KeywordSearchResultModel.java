/**
 * %HEADER%
 */
package net.sf.genomeview.gui.search;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import net.sf.genomeview.data.Model;
import net.sf.jannot.DataKey;
import net.sf.jannot.Entry;
import net.sf.jannot.Feature;
import net.sf.jannot.FeatureAnnotation;
import net.sf.jannot.Qualifier;

/**
 * 
 * @author Thomas Abeel
 * 
 */
class KeywordSearchResultModel extends AbstractSearchResultModel {

	private static final long serialVersionUID = -6980331160054705283L;

	private ArrayList<Feature> features = new ArrayList<Feature>();
	private ArrayList<Entry> entries = new ArrayList<Entry>();

	private Set<Feature> featuresSet = new HashSet<Feature>();

	KeywordSearchResultModel(Model model) {
		super(model);
	}

	private String[] columns = new String[] { "Entry", "Feature" };

	@Override
	public String getColumnName(int col) {

		return columns[col];

	}

	@Override
	public int getColumnCount() {
		return 2;
	}

	@Override
	public int getRowCount() {
		return features.size();
	}

	@Override
	public Object getValueAt(int row, int col) {
		switch (col) {
		case 0:
			return entries.get(row);
		case 1:
			return features.get(row);
		}
		return null;

	}

	Entry getEntry(int row) {
		return entries.get(row);

	}

	void search(String text) {
		String lowerCaseText = text.toLowerCase();
		clear();
		for (Entry e : model.entries()) {
			for (DataKey d : e) {
				if (e.get(d) instanceof FeatureAnnotation) {
					for (Feature f : ((FeatureAnnotation) e.get(d)).get()) {
						if (!featuresSet.contains(f)) {
							for (String key : f.getQualifiersKeys()) {
								String value=f.qualifier(key);
								
									if ((key!=null&&key.toLowerCase().contains(lowerCaseText))
											|| (value!=null&&value.toLowerCase().contains(lowerCaseText))) {
										if (!featuresSet.contains(f)) {
											features.add(f);
											entries.add(e);
											featuresSet.add(f);
										}
									}

							}
						}
					}
				}
			}
		}
		
		fireTableDataChanged();

	}

	Feature getFeature(int row) {
		return features.get(row);
	}
	@Override
	void clear() {
		features.clear();
		entries.clear();
		featuresSet.clear();
		fireTableDataChanged();
	}
}