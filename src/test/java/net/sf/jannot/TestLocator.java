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

import org.junit.Test;

import be.abeel.io.LineIterator;

/**
 * 
 * @author Thomas Abeel
 * 
 */
public class TestLocator {

	@Test
	public void testLocalRelativeFile() throws URISyntaxException, IOException {

		Locator l = new Locator("test/resource/junit.txt");
		for (String line : new LineIterator(l.stream())) {
			System.out.println(line);
		}

	}

	@Test
	public void testLocalAbsoluteFile() throws URISyntaxException, IOException {
		String path = new java.io.File(".").getCanonicalPath() + "/test/resource/junit.txt";
		Locator l = new Locator(path);
		for (String line : new LineIterator(l.stream())) {
			System.out.println(line);
		}

	}

	@Test
	public void testURLLocalAbsoluteFile() throws URISyntaxException, IOException {
		String path = "file://" + new java.io.File(".").getCanonicalPath() + "/test/resource/junit.txt";
		Locator l = new Locator(path);
		for (String line : new LineIterator(l.stream())) {
			System.out.println(line);
		}

	}

	@Test
	public void testURLLocalFile() throws URISyntaxException, IOException {

		Locator l = new Locator("file://test/resource/junit.txt");
		for (String line : new LineIterator(l.stream())) {
			System.out.println(line);
		}

	}

	@Test
	public void testURL() throws URISyntaxException, IOException {
		Locator l = new Locator("http://genomeview.org/junit.php");
		for (String line : new LineIterator(l.stream())) {
			System.out.println(line);
		}

	}

}
