package net.sf.genomeview.gui.search;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.logging.Level;

import org.junit.Test;

import net.sf.genomeview.core.Configuration;
import net.sf.genomeview.core.Globals;
import net.sf.genomeview.core.MessageManager;
import net.sf.genomeview.data.Model;
import net.sf.jannot.DataKey;
import net.sf.jannot.DistributingReporter;
import net.sf.jannot.Entry;
import net.sf.jannot.Feature;
import net.sf.jannot.FeatureAnnotation;
import net.sf.jannot.Global;
import net.sf.jannot.JavaLogInterceptor;
import net.sf.jannot.StringKey;
import net.sf.jannot.exception.ReadFailedException;
import net.sf.nameservice.NameService;

public class KeywordSearchResultModelTest {
	private static final String SENTENCE1 = "conserved hypothetical protein {Bacillus anthracis Ames}";
	private final DataKey key1 = new StringKey("feature1");
	private static final String SENTENCE2 = "sensor histidine kinase SrrB, putative {Bacillus anthracis Ames}";
	private final DataKey key2 = new StringKey("feature2");
	private static final String SENTENCE3 = "MTA-SAH nucleosidase - phosphatase, putative {Bacillus anthracis Ames}";
	private final DataKey key3 = new StringKey("feature3");

	private final Model model;
	private final KeywordSearchResultModel keywordmodel;

	private final FeatureAnnotation fanno1, fanno2, fanno3;
	private final Feature feature1 = mock(Feature.class),
			feature2 = mock(Feature.class), feature3 = mock(Feature.class);
	private DistributingReporter log = mock(DistributingReporter.class);

	public KeywordSearchResultModelTest()
			throws IOException, ReadFailedException {
		// initializing model is quite involved, need to
		// either mock or make real objects.
		// init reads all kind of config files ......
		Global global = new Global(log, mock(JavaLogInterceptor.class),
				new NameService(log));
		Globals globals = new Globals(global, new Configuration(global),
				new MessageManager(null));
		model = new Model("id", globals);

		keywordmodel = new KeywordSearchResultModel(model);

		// mock the annotations.... lot of work.
		// but creating features is also quite involved...
		fanno1 = mock(FeatureAnnotation.class);
		when(fanno1.get()).thenReturn(Arrays.asList(feature1));
		when(feature1.getQualifiersKeys())
				.thenReturn(new HashSet<>(Arrays.asList("")));
		when(feature1.qualifier(any())).thenReturn(SENTENCE1);

		fanno2 = mock(FeatureAnnotation.class);
		when(fanno2.get()).thenReturn(Arrays.asList(feature2));
		when(feature2.getQualifiersKeys())
				.thenReturn(new HashSet<>(Arrays.asList("")));
		when(feature2.qualifier(any())).thenReturn(SENTENCE2);

		fanno3 = mock(FeatureAnnotation.class);
		when(fanno3.get()).thenReturn(Arrays.asList(feature3));
		when(feature3.getQualifiersKeys())
				.thenReturn(new HashSet<>(Arrays.asList("")));
		when(feature3.qualifier(any())).thenReturn(SENTENCE3);

	}

	@Test
	public void smokeTest() {
	}

	@Test
	public void search0Test() {
		// check that the search finds the proper features
		// and that they are added to the model
		keywordmodel.search("histidine putative");
		// check no warnings were logged
		verify(log, times(0)).log(eq(Level.WARNING), any());
		// this should yield no results as entryset is empty now
		assertEquals(0, keywordmodel.getRowCount());
	}

	@Test
	public void search3Test() {
		Entry entry = model.entries().getOrCreateEntry("1");
		entry.add(key1, fanno1);
		entry.add(key2, fanno2);
		entry.add(key3, fanno3);

		// check that the search finds the proper features
		// and that they are added to the model
		keywordmodel.search("histidine, putative");
		// check no warnings were logged
		verify(log, times(0)).log(eq(Level.WARNING), any());
		// this should give feature2 as the result
		assertEquals(1, keywordmodel.getRowCount());
		assertEquals(feature2, keywordmodel.getFeature(0));
	}

}
