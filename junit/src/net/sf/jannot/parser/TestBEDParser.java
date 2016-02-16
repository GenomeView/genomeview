/**
 *    This file is part of JAnnot.
 *
 *    JAnnot is free software: you can redistribute it and/or modify
 *    it under the terms of the GNU General Public License as published by
 *    the Free Software Foundation, either version 3 of the License, or
 *    (at your option) any later version.
 *
 *    JAnnot is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *    GNU General Public License for more details.
 *
 *    You should have received a copy of the GNU General Public License
 *    along with JAnnot.  If not, see <http://www.gnu.org/licenses/>.
 */
package net.sf.jannot.parser;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;

import net.sf.jannot.Data;
import net.sf.jannot.DataKey;
import net.sf.jannot.Entry;
import net.sf.jannot.EntrySet;
import net.sf.jannot.StringKey;
import net.sf.jannot.Type;
import net.sf.jannot.exception.ReadFailedException;
import net.sf.jannot.source.DataSource;
import net.sf.jannot.source.DataSourceFactory;
import net.sf.jannot.source.Locator;

import org.junit.Assert;
import org.junit.Test;

import be.abeel.io.LineIterator;

import support.DataManager;

/**
 * 
 * @author Thomas Abeel
 * 
 */
public class TestBEDParser {
	private static Logger log = Logger.getLogger(TestBEDParser.class.toString());

	@Test
	public void testParserMini() {
		File f = DataManager.file("minibed.bed");
		try {
			DataSource ds = DataSourceFactory.create(new Locator(f));
			EntrySet es = ds.read();
			// System.out.println(es.firstEntry());
			Assert.assertEquals("chr7", es.firstEntry().getID());
			int count = 0;
			for (Entry e : es)
				count++;
			Assert.assertEquals(1, count);
			Data d = es.firstEntry().get(Type.get("ItemRGBDemo"));
			for (DataKey dk : es.firstEntry()) {
				System.out.println("Datakey=" + dk);
			}
			Assert.assertNotNull(d);

		} catch (URISyntaxException e) {
			e.printStackTrace();
			Assert.fail();
		} catch (IOException e) {
			e.printStackTrace();
			Assert.fail();
		} catch (ReadFailedException e) {

			e.printStackTrace();
			Assert.fail();
		}
	}

	@Test
	public void testParserBare() {
		File f = DataManager.file("barebed.bed");
		try {
			DataSource ds = DataSourceFactory.create(new Locator(f));
			EntrySet es = ds.read();
			// System.out.println(es.firstEntry());
			Assert.assertEquals("chr7", es.firstEntry().getID());
			int count = 0;
			for (Entry e : es)
				count++;
			Assert.assertEquals(1, count);
			Data d = es.firstEntry().get(Type.get("barebed.bed"));
			for (DataKey dk : es.firstEntry()) {
				System.out.println("Datakey=" + dk);
			}
			Assert.assertNotNull(d);

		} catch (URISyntaxException e) {
			e.printStackTrace();
			Assert.fail();
		} catch (IOException e) {
			e.printStackTrace();
			Assert.fail();
		} catch (ReadFailedException e) {

			e.printStackTrace();
			Assert.fail();
		}
	}

	@Test
	public void testSave() {
		File f = DataManager.file("barebed.bed");
		try{
			DataSource ds = DataSourceFactory.create(new Locator(f));
			EntrySet es = ds.read();
			BEDParser output=new BEDParser("save.bed");
			FileOutputStream fos = new FileOutputStream("save.bed");
			for(Entry e:es){
				output.write(fos, e);
			}
			fos.close();
			LineIterator it=new LineIterator(new File("save.bed"));
			Assert.assertEquals("track name=\"barebed.bed\"", it.next());
			LineIterator expected=new LineIterator(f);
			for(String line:it){
				Assert.assertEquals(expected.next(),line.replaceAll("0\\.0", "0"));
			}
			it.close();
			expected.close();
		}catch(Exception e){
			e.printStackTrace();
			Assert.fail();
		}
	}

	@Test
	public void test_loadEntries() {
		log.info("BEGIN test_loadEntries");
		try {
			File fileData = DataManager.file("ItemRGBDemo.txt");
			InputStream is = new FileInputStream(fileData);

			EntrySet entries = new EntrySet();
			BEDParser parser = new BEDParser(fileData.getName());
			log.info("	> fileData: " + fileData + "( " + fileData.length() + " KB)");
			try {
				// We parse the sample file
				entries = parser.parse(is, null);
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
				System.out.println("DD: " + data);

				long t4 = System.currentTimeMillis();
				log.info("		> Time consumed fetching data: " + Math.abs(t4 - t3));

			} catch (Exception e) {
				e.printStackTrace();
				assertTrue(false);
			}
		} catch (Exception e) {
			e.printStackTrace();
			assertTrue(false);
		}
		log.info("END test_loadEntries");
	}

}
