/**
 * %HEADER%
 */
package net.sf.genomeview.gui.search;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import net.sf.genomeview.data.Model;
import net.sf.jannot.Feature;
import net.sf.jannot.FeatureAnnotation;
import net.sf.jannot.Type;

/**
 * 
 * @author Thomas Abeel
 * 
 */
class OverlapSearchResultModel extends AbstractSearchResultModel {

	private static final long serialVersionUID = 5850270831525342543L;

	private ArrayList<Feature> features = new ArrayList<Feature>();

	private Set<Feature> featuresSet = new HashSet<Feature>();

	OverlapSearchResultModel(Model model) {
		super(model);
	}

	void search(Type source, Type target) {
		for (Feature f : ((FeatureAnnotation) model.vlm.getSelectedEntry().get(source)).get()) {
			for (Feature g : ((FeatureAnnotation) model.vlm.getSelectedEntry().get(target)).get()) {
				if (f.overlaps(g)) {
					features.add(f);
					break;
				}

			}

		}
		fireTableDataChanged();

	}

	@Override
	public String getColumnName(int col) {
		return "Feature";

	}

	@Override
	public int getColumnCount() {
		return 1;
	}

	@Override
	public int getRowCount() {
		return features.size();
	}

	@Override
	public Object getValueAt(int row, int col) {
		return features.get(row);
	}

	Feature getFeature(int row) {
		return features.get(row);
	}
	@Override
	void clear() {
		features.clear();
		featuresSet.clear();
		fireTableDataChanged();
	}
}