package net.sf.genomeview.gui.search;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.text.similarity.LevenshteinDistance;

/**
 * a phrase (word or bunch of words etc) that is being searched for
 */
public class SearchPhrase {

	private final List<String> terms = new ArrayList<>();
	private final LevenshteinDistance levenshtein = LevenshteinDistance
			.getDefaultInstance();

	/**
	 * 
	 * @param searchTerm a term that will be {@link #split(String)} into words.
	 *                   The search target is to match ALL the words (logical
	 *                   and). See {@link #matches(String)}
	 */
	public SearchPhrase(final String searchTerm) {
		terms.addAll(split(searchTerm));
	}

	/**
	 * 
	 * @param value a value to be cleaned
	 * @return cleaned value, with brackets, comma, dots etc replaced with
	 *         whitespace " "
	 * 
	 */
	private String clean(String value) {
		return value.toLowerCase().trim()
				.replaceAll("[\\s\\{\\}\\(\\)\\[\\].,]", " ")
				.replaceAll("  ", " ");
	}

	/**
	 * 
	 * @param term a string with multiple words, separated with whitespaces. See
	 *             {@link #clean(String)}
	 * @return list of words in the term
	 */
	public List<String> split(String term) {
		return Arrays.asList(clean(term).split(" "));
	}

	@Override
	public String toString() {
		return terms.toString();
	}

	/**
	 * 
	 * @param value a String - possible candidate matching this. The value is
	 *              assumed to contain a number of words. Those words are
	 *              separated {@link #clean(String)} and compared with the
	 *              {@link #terms}.
	 * @return true if value scores high enough to be assumed a match
	 */
	public boolean matches(String value) {
		return score(Arrays.asList(clean(value).split(" "))) <= 1;
	}

	/**
	 * @param values a List of words to be matched with the search phrase
	 * @return score how well value matches to the searchphrase. LOWER is BETTER
	 */
	public int score(final List<String> values) {
		// each search term must score well with one of the values
		int score = 0;
		for (String term : terms) {
			score += score(term, values);
		}
		return score;
	}

	/**
	 * 
	 * @param term   one of the {@link #terms}
	 * @param values the values to score
	 * @return lower value if term matches better with a value in values.
	 */
	private int score(String term, List<String> values) {
//		return values.contains(term) ? 0 : 1;
		return values.stream().map(value -> levenshtein.apply(value, term))
				.min(Integer::compareTo).orElse(1);
	}

}