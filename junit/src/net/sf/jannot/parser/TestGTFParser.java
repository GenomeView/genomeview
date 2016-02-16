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
public class TestGTFParser {
	private static Logger log = Logger.getLogger(TestGTFParser.class.toString());

	@Test
	public void testParserMini() {
		File f = DataManager.file("doubleScore.gtf");
		try {
			DataSource ds = DataSourceFactory.create(new Locator(f));
			EntrySet es = ds.read();
			double score=es.firstEntry().getMemoryAnnotation(Type.get("gene")).get(0).getScore();
			Assert.assertEquals(0, score,0.0001);
			

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

	

}
