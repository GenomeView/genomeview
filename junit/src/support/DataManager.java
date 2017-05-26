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
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;

import org.apache.commons.io.IOUtils;
import org.junit.Assert;


import be.abeel.io.LineIterator;

/**
 * 
 * @author Thomas Abeel
 * 
 */
public class DataManager {

	public static File file(String identifier) {
		try {
			File folder = new File(".sf-testing-cache");
			if (!folder.exists()) {
				folder.mkdirs();
			}
			File file = new File(folder, identifier);
			if (!file.exists() || file.length() == 0) {
				retrieveSF(identifier, file);
			}
			return file;
		} catch (Exception e) {
			e.printStackTrace();
			Assert.fail();
			return null;
		}

	}

	private static void retrieveSF(String identifier, File out) throws Exception {
		URL url = new URL("http://genomeview.org/junit/" + identifier);
		HttpURLConnection conn = (HttpURLConnection) url.openConnection();
		int response = conn.getResponseCode();
		if (response == 302) {
			String location = conn.getHeaderField("Location");
			conn = (HttpURLConnection) new URL(location).openConnection();
			FileOutputStream fof=new FileOutputStream(out);
			IOUtils.copy(conn.getInputStream(),fof );
			fof.close();
			
		}
	}

//	public static String[] vcfFiles() {
//
//		/*
//		 * TODO
//		 * 
//		 * Before returning this list of files, make sure that those files and
//		 * their indices have been downloaded to the correct folder.
//		 * 
//		 * - Check internet connection - Check available disk-space - Check if
//		 * files are present
//		 * 
//		 * if not: Download files (with progress indication)
//		 * 
//		 * return list of files
//		 */
//		String[] out = new String[] { "test/resource/CEU.SRP000031.2010_03.genotypes.vcf.gz", "test/resource/CEU.SRP000032.2010_03.genotypes.vcf.gz",
//				"test/resource/CEU.SRP000033.2010_03.genotypes.vcf.gz", "test/resource/CEU.trio.2010_03.genotypes.vcf.gz",
//				"test/resource/CHB.SRP000033.2010_03.genotypes.vcf.gz", "test/resource/CHB+JPT.SRP000031.2010_03.genotypes.vcf.gz",
//				"test/resource/CHD.SRP000033.2010_03.genotypes.vcf.gz",
//				"test/resource/JPT.SRP000033.2010_03.genotypes.vcf.gz",
//				"test/resource/LWK.SRP000033.2010_03.genotypes.vcf.gz",
//				// "test/resource/trio.2010_06.ychr.genotypes.vcf.gz",
//				"test/resource/TSI.SRP000033.2010_03.genotypes.vcf.gz", "test/resource/YRI.SRP000031.2010_03.genotypes.vcf.gz",
//				"test/resource/YRI.SRP000032.2010_03.genotypes.vcf.gz", "test/resource/YRI.SRP000033.2010_03.genotypes.vcf.gz",
//				"test/resource/YRI.trio.2010_03.genotypes.vcf.gz" };
//		return out;
//	}
}
