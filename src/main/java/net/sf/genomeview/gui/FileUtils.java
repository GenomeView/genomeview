package net.sf.genomeview.gui;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;

public class FileUtils implements Serializable {
	private static final long serialVersionUID = -3475463874481230522L;
	
	/**
	 * Returns if the file is a directory
	 * @param filename File name it is wanted to know about
	 * @return If the file is a directory
	 */
	public static boolean isDirectory(String filename){
		File f = new File(filename);
		return f.isDirectory();
	}
	/**
	 * Returns if the file is a file
	 * @param filename File name it is wanted to know about
	 * @return If the file is a file
	 */
	public static boolean isFile(String filename){
		File f = new File(filename);
		return f.isFile();
	}
	/**
	 * Returns if the file exists
	 * @param filename File name it is wanted to know about
	 * @return If the file exists
	 */
	public static boolean exists(String filename){
		File f = new File(filename);
		return f.exists();
	}
	/**
	 * Returns if the file has a valid name
	 * @param filename File name it is wanted to know about
	 * @return If the file has a valid name
	 */
	public static boolean isFilenameValid(String filename){
	    File f = new File(filename);
	    try {
	       f.getCanonicalPath();
	       return true;
	    }
	    catch (IOException e) {
	       return false;
	    }
	}
}
