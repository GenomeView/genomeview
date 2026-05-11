/**
 * %HEADER%
 */
package net.sf.genomeview.gui.search;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;

import net.sf.genomeview.data.Model;
import net.sf.jannot.DataKey;
import net.sf.jannot.Entry;
import net.sf.jannot.Feature;
import net.sf.jannot.FeatureAnnotation;

/**
 * A Model to show search results from a keyword search. Keyword search searches
 * the {@link FeatureAnnotation}s in all entries in the model.
 * 
 * @author Thomas Abeel
 * 
 */
@SuppressWarnings("serial")
class KeywordSearchResultModel extends AbstractSearchResultModel {

	// the set of features found in the current search.
	private final Set<Feature> featuresSet = new HashSet<Feature>();
	// features found, the search result.
	private final ArrayList<Feature> features = new ArrayList<Feature>();
	private final ArrayList<Entry> entries = new ArrayList<Entry>();

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

	/**
	 * Update this model with new search results. Called when a search action is
	 * initated. From the GUI, this happens when the user presses enter or
	 * clicks search. The search is done case insensitive. All search terms must
	 * match with one of the feature values. Special characters like {}[](),.
	 * are ignored
	 * 
	 * @param text the text the user typed in the search text area.
	 */
	void search(final String text) {

		final SearchPhrase phrase = new SearchPhrase(text);
		model.getLog().log(Level.INFO, "searching " + phrase);
		clear();
		for (final Entry e : model.entries()) {
			for (final DataKey d : e) {
				if (e.get(d) instanceof FeatureAnnotation) {
					try {
						for (final Feature f : ((FeatureAnnotation) e.get(d))
								.get()) {
							searchFeature(phrase, e, f);
						}
					} catch (IOException e1) {
						model.getLog().log(Level.WARNING,
								"Failed to get annotations", e1);
					}
				}
			}
		}

		fireTableDataChanged();

	}

	/**
	 * Search single feature. Adds feature to {@link #featuresSet} and
	 * {@link #features} if at least one of the feature qualifiers matches the
	 * search term
	 * 
	 * @param lowerCaseText {@link String} with the search term.
	 * @param e             the {@link Entry}
	 * @param f             the {@link Feature}
	 */
	private void searchFeature(final SearchPhrase phrase, final Entry e,
			final Feature f) {
		if (featuresSet.contains(f)) {
			return;
			// FIXME Feature does not implement equals, hashCode ??
		}

		for (final String key : f.getQualifiersKeys()) {
			final String value = f.qualifier(key);
			if (key == null || value == null) {
				continue;
			}

			if (phrase.matches(value)) {
				features.add(f);
				entries.add(e);
				featuresSet.add(f);
				return;
			}

		}
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
