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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import org.junit.Assert;
import org.junit.Test;

import support.DataManager;

/**
 * 
 * @author Thomas Abeel
 * 
 */
public class TestParserDetection {

	@Test
	public void testBED() {
		File f = DataManager.file("minibed.bed");
		try {
			Parser p = Parser.detectParser(new FileInputStream(f), "file");
			Assert.assertTrue("Wrong parser: "+p.getClass(),p instanceof BEDParser);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Assert.fail();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Assert.fail();
		}

	}

	@Test
	public void testVCF() {
		File f = DataManager.file("tiny.vcf");
		try {
			Parser p = Parser.detectParser(new FileInputStream(f), "file");
			Assert.assertTrue("Wrong parser: "+p.getClass(),p instanceof VCFParser);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Assert.fail();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Assert.fail();
		}

	}
	
}
