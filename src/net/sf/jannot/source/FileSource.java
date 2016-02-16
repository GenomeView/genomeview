/**
 * %HEADER%
 */
package net.sf.jannot.source;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import net.sf.jannot.parser.Parser;

/**
 * Extends AbstractStreamDataSource. It prepares the data to be read.  
 * 
 * @author Thomas Abeel
 *
 */
public class FileSource extends AbstractStreamDataSource {

	private File file;


	

	public FileSource(String file) throws IOException {
		this(new File(file));
	}
	public File getFile(){
		return file;
	}
	public FileSource(File file) throws IOException {
		super(new Locator(file.toString()));
		InputStream ios1, ios2;
		ios1 = new FileInputStream(file);
		ios2 = new FileInputStream(file);
		Parser p = Parser.detectParser(ios1,file);
		ios1.close();
		super.setParser(p);
		super.setIos(ios2);
		this.file = file;
	}

//	@Override
//	public void saveOwn(EntrySet entries) {
//		try {
//			int count = 0;
//			File bak = new File(file + ".bak." + count++);
//			while (infiniteBackups && bak.exists()) {
//				bak = new File(file + ".bak." + count);
//			}
//			file.renameTo(bak);
//
//			OutputStream os = new FileOutputStream(file);
//			for (Entry e : entries) {
//				super.getParser().write(os, e, this);
//			}
//			os.close();
//		} catch (FileNotFoundException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		
//
//	}

	@Override
	public String toString() {
		if (file.getParentFile() != null) {
			return ".../" + file.getParentFile().getName() + "/"
					+ file.getName().toString();
		} else
			return file.getName().toString();

	}

	/* (non-Javadoc)
	 * @see net.sf.jannot.source.DataSource#isIndexed()
	 */
	@Override
	public boolean isIndexed() {
		return false;
	}

	/* (non-Javadoc)
	 * @see net.sf.jannot.source.DataSource#size()
	 */
	@Override
	public long size() {
		return file.length();
	}


}
