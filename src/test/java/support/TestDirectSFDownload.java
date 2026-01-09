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
package support;

import java.io.File;

import org.junit.Assert;
import org.junit.Test;


/**
 * 
 * @author Thomas Abeel
 *
 */
public class TestDirectSFDownload {

	@Test
	public void testDownload(){
		String id="minibed.bed";
		DataManager.file(id);
		Assert.assertTrue(new File(".sf-testing-cache").exists());
		Assert.assertTrue(new File(".sf-testing-cache/minibed.bed").exists());
		Assert.assertTrue(new File(".sf-testing-cache/minibed.bed").length()>0);
		
	}
	
	@Test
	public void testLargeDownload(){
		String id="CEU.trio.2010_03.genotypes.vcf.gz";
		DataManager.file(id);
		Assert.assertTrue(new File(".sf-testing-cache").exists());
		Assert.assertTrue(new File(".sf-testing-cache/"+id).exists());
		Assert.assertTrue(new File(".sf-testing-cache/"+id).length()>0);
		
	}
}
