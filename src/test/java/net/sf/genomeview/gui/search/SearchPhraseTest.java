package net.sf.genomeview.gui.search;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Test;

public class SearchPhraseTest {
	private static final String SENTENCE1 = "conserved hypothetical protein {Bacillus anthracis Ames}";
	private static final String SENTENCE2 = "sensor histidine kinase SrrB, putative {Bacillus anthracis Ames}";
	private final SearchPhrase phrase;

	public SearchPhraseTest() {
		phrase = new SearchPhrase("histidine");

	}

	@Test
	public void smoke() {
	}

	@Test
	public void testSplit() {
		List<String> split = phrase.split(SENTENCE1);
		assertEquals(6, split.size());
	}

	@Test
	public void testScore1() {
		// none of the words in sentence should score well with "histidine"
		int score = phrase.score(phrase.split(SENTENCE1));
		assertTrue(score > 5);
	}

	@Test
	public void testScore2() {
		// sentence 2 contains histidine literally
		int score = phrase.score(phrase.split(SENTENCE2));
		assertEquals(0, score);
	}

	@Test
	public void testOneCharOff() {
		// phrase with 1 typo
		SearchPhrase phrase1 = new SearchPhrase("histdine");
		int score = phrase1.score(phrase.split(SENTENCE2));
		assertEquals(1, score);
	}

	@Test
	public void testTwoWordMatch() {
		SearchPhrase phrase2 = new SearchPhrase("histidine, putative");
		int score = phrase2.score(phrase.split(SENTENCE2));
		assertEquals(0, score);
	}

	@Test
	public void testTwoWordMatchTwoCharsOff() {
		SearchPhrase phrase2 = new SearchPhrase("histdine, putativ");
		int score = phrase2.score(phrase.split(SENTENCE2));
		assertEquals(2, score);
	}

}
