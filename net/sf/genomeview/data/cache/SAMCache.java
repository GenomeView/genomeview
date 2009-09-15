/**
 * %HEADER%
 */
package net.sf.genomeview.data.cache;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashSet;
import java.util.Set;

import be.abeel.io.ExtensionFileFilter;
import be.abeel.security.MD5Tools;

import net.sf.genomeview.core.Configuration;
import net.sf.jannot.source.DataSource;
import net.sf.jannot.source.FileSource;
import net.sf.jannot.source.SAMDataSource;

public class SAMCache {

	private static File cacheDir = new File(Configuration.getDirectory(), "cache");;

	public static boolean contains(String md5) {

		System.out.println("URL cache: " + cacheDir);
		if (!cacheDir.exists())
			cacheDir.mkdir();
		File[] files = cacheDir.listFiles(new ExtensionFileFilter("bam"));
		Set<String> names = new HashSet<String>();

		for (File file : files) {
			names.add(file.getName());

		}
		return names.contains(md5);

	}

	public static SAMDataSource getSAM(URL url) throws IOException {
		String md5 = MD5Tools.md5(url.toString())+".bam" ;
		if (!contains(md5)) {
			File tmpBAM = new File(cacheDir, md5+".tmp");
			File tmpBAI = new File(cacheDir, md5 + ".bai"+".tmp");
			URLConnection conn = url.openConnection();
			System.out.println("Downloading: " + conn.getContentLength() + " bytes");
			copy(conn.getInputStream(), tmpBAM);
			copy(new URL(url.toString() + ".bai").openStream(), tmpBAI);
			tmpBAM.renameTo(new File(cacheDir, md5));
			tmpBAI.renameTo(new File(cacheDir, md5 + ".bai"));

		}
		return new SAMDataSource(new File(cacheDir, md5));
	}

	private static void copy(InputStream in, File file) throws IOException {
		OutputStream out = new FileOutputStream(file);

		byte[] buffer = new byte[100000];
		while (true) {
			int amountRead = in.read(buffer);
			if (amountRead == -1) {
				break;
			}
			out.write(buffer, 0, amountRead);

		}
		out.close();

	}
}
