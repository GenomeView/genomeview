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
package net.sf.jannot;

import java.io.IOException;
import java.net.URISyntaxException;

import net.sf.jannot.source.Locator;

import org.junit.Ignore;
import org.junit.Test;

import be.abeel.io.LineIterator;

/**
 * 
 * @author Thomas Abeel
 * 
 */
public class TestLocator {

	private static final String SRC_TEST_RESOURCES_JUNIT_TXT = "src/test/resources/junit.txt";
	private static final String TEST_RESOURCES_JUNIT_TXT = "/"+SRC_TEST_RESOURCES_JUNIT_TXT;
	private static final String JUNIT_TXT = "file://"+SRC_TEST_RESOURCES_JUNIT_TXT;

	@Test
	public void testLocalRelativeFile() throws URISyntaxException, IOException {

		Locator l = new Locator(SRC_TEST_RESOURCES_JUNIT_TXT);
		for (String line : new LineIterator(l.stream())) {
			System.out.println(line);
		}

	}

	@Test
	public void testLocalAbsoluteFile() throws URISyntaxException, IOException {
		String path = new java.io.File(".").getCanonicalPath() + TEST_RESOURCES_JUNIT_TXT;
		Locator l = new Locator(path);
		for (String line : new LineIterator(l.stream())) {
			System.out.println(line);
		}

	}

	@Test
	public void testURLLocalAbsoluteFile() throws URISyntaxException, IOException {
		String path = "file://" + new java.io.File(".").getCanonicalPath() + TEST_RESOURCES_JUNIT_TXT;
		Locator l = new Locator(path);
		for (String line : new LineIterator(l.stream())) {
			System.out.println(line);
		}

	}

	@Test
	public void testURLLocalFile() throws URISyntaxException, IOException {

		Locator l = new Locator(JUNIT_TXT);
		for (String line : new LineIterator(l.stream())) {
			System.out.println(line);
		}

	}

	@Ignore
	@Test
	public void testURL() throws URISyntaxException, IOException {
		Locator l = new Locator("http://genomeview.org/junit.php");
		for (String line : new LineIterator(l.stream())) {
			System.out.println(line);
		}

	}

}
