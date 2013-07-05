package net.sf.genomeview.plugin;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import org.junit.Assert;
import org.junit.Test;

public class TestDownloadPlugin {

	static public void start(){
		
	}
	
	@Test
	public void testDownloadJar(){
		String urlString = "http://bioinformatics.psb.ugent.be/img/splash/beg_logo.png";
		File toDir = new File("/home/thpar/temp_gv");
		
		File resultFile = new File(toDir, "beg_logo.png");
		
		Assert.assertFalse(resultFile.exists());
		
		try {
			URL url = new URL(urlString);
			PluginLoader.installPlugin(url, toDir);
			
			Assert.assertTrue(resultFile.exists());
			
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e){
			e.printStackTrace();
		}
		
	}
	
	@Test
	public void testCopyJar(){
		String origString = "/home/thpar/curationassistant-0.1.jar";
		File toDir = new File("/home/thpar/temp_gv");
		
		File resultFile = new File(toDir, "curationassistant-0.1.jar");
		
		Assert.assertFalse(resultFile.exists());
		
		try {
			File file = new File(origString);
			PluginLoader.installPlugin(file, toDir);
			
			Assert.assertTrue(resultFile.exists());
			
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e){
			e.printStackTrace();
		}
		
	}
}
