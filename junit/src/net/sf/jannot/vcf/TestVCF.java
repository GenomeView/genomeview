package net.sf.jannot.vcf;

import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;

import net.sf.jannot.Data;
import net.sf.jannot.DataKey;
import net.sf.jannot.Entry;
import net.sf.jannot.EntrySet;
import net.sf.jannot.source.DataSource;
import net.sf.jannot.source.DataSourceFactory;
import net.sf.jannot.source.Locator;
import net.sf.jannot.variation.Variation;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import support.DataManager;

import java.io.File;

/**
 * IMPORTANT!!!: This test requires the data in test/resource to be prepared.
 * 
 * See the readme.txt file in test/resource/ to see download instructions.
 * 
 * 
 * @author Thomas Abeel
 * @author David Roldan Martinez
 * 
 */
public class TestVCF {

	// private String[] files =DataManager.vcfFiles();

	private static Logger log = Logger.getLogger(TestVCF.class.toString());

	@Before
	public void init() {
	}

	@After
	public void destroy() {

	}

	@Test
	public void test_dummyTest() {
		log.info("BEGIN test_dummyTest");
		assertTrue(true);
		log.info("END test_dummyTest");
	}

	@Test
	public void test_loadEntries() {
		log.info("BEGIN test_loadEntries");
		try {
			int i = 0;

			String dataIdentifier = "CEU.trio.2010_03.genotypes.vcf.gz";
			String indexIdentifier = dataIdentifier + ".tbi";

			EntrySet entries = new EntrySet();
			Locator fIndex = new Locator(DataManager.file(indexIdentifier));
			Locator fData = new Locator(DataManager.file(dataIdentifier));
			DataSource ds = DataSourceFactory.create(fData, fIndex);

			// File fileData = new File(dataFile);
			// File indexData = new File(indexFile);

			log.info("------ Sample # " + i + " ---------");
			log.info("	> fileData: " + fData.file() + "( " + fData.file().length() + " KB)");
			log.info("	> indexData: " + fIndex.file() + "( " + fIndex.file().length() + " KB)");
			i++;
			log.info("	> Reading source:" + ds);
			try {
				long t1 = System.currentTimeMillis();
				ds.read(entries);
				long t2 = System.currentTimeMillis();
				log.info("		> Time consumed loading files: " + Math.abs(t2 - t1));
				log.info("		> Number of entries: " + entries.size());
				// assertTrue(entries.size() > 0);
				// We build an ArrayList to access randonmly to a entry
				List<Entry> list = new ArrayList<Entry>();
				for (Entry entry : entries) {
					list.add(entry);
				}
				int selectedIndex = Math.max(0, (int) Math.round(Math.random() * list.size()) - 1);
				Entry selectedEntry = list.get(selectedIndex);

				Iterator<DataKey> it = selectedEntry.iterator();
				List<DataKey> list2 = new ArrayList<DataKey>();
				while (it.hasNext()) {
					list2.add(it.next());
				}
				int keyIndex = Math.max(0, (int) Math.round(Math.random() * list2.size()) - 1);

				/* retrieve some data from some entry */
				long t3 = System.currentTimeMillis();
				DataKey dataKey = list2.get(keyIndex);
				Data<?> data = selectedEntry.get(dataKey);
				// System.out.println("DD: " + data);
				Iterable<?> resultSet = data.get(500000, 2000000);
				long t4 = System.currentTimeMillis();
				log.info("		> Time consumed fetching data: " + Math.abs(t4 - t3));
				for (Object o : resultSet) {
					// System.out.println(o);
					Variation v = (Variation) o;
					// System.out.println(o+"\t"+v.start()+"\t"+v.alleles());
				}
			} catch (Exception e) {
				e.printStackTrace();
				assertTrue(false);
			}
			// }
			// }
		} catch (Exception e) {
			e.printStackTrace();
			assertTrue(false);
		}
		log.info("END test_loadEntries");
	}

}