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
		URL url = new URL("https://sourceforge.net/projects/genomeview/files/unit%20test%20files/" + identifier + "/download?use_mirror=autoselect");
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


}
