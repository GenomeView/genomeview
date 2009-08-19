/**
 * %HEADER%
 */
package net.sf.genomeview.data.cache;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;

import be.abeel.io.ExtensionFileFilter;
import be.abeel.security.MD5Tools;

import net.sf.genomeview.core.Configuration;
import net.sf.jannot.source.DataSource;
import net.sf.jannot.source.FileSource;


public class SourceCache {

	private static File cacheDir;

	public static boolean contains(URL url) {
		
		cacheDir=new File(Configuration.getDirectory(),"cache");
		System.out.println("URL cache: "+cacheDir);
		if(!cacheDir.exists())
			cacheDir.mkdir();
		File[]files=cacheDir.listFiles(new ExtensionFileFilter("url"));
		Set<String>names=new HashSet<String>();
		String md5=MD5Tools.md5(url.toString());
		for(File file:files){
			names.add(file.getName());
		
		}
		return names.contains(md5+".url");
		
	}

	public static DataSource get(URL url) throws IOException {
		System.out.println("Retrieving from cache: "+url);
		return  new FileSource(new File(cacheDir,MD5Tools.md5(url.toString())+".url"));
	}

	public static OutputStream startCaching(URL url) throws FileNotFoundException {
		return  new FileOutputStream(new File(cacheDir,MD5Tools.md5(url.toString())+".tmp"));
	}

	/**
	 * This method should only be called when the data is completely written.
	 * 
	 * @param url
	 */
	public static void finish(URL url) {
		File f=new File(cacheDir,MD5Tools.md5(url.toString())+".tmp");
		f.renameTo(new File(cacheDir,MD5Tools.md5(url.toString())+".url"));
		
	}

}
