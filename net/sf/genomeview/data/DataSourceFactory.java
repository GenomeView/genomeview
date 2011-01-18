/**
 * %HEADER%
 */
package net.sf.genomeview.data;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileFilter;

import net.sf.genomeview.core.Configuration;
import net.sf.genomeview.data.cache.CachedURLSource;
import net.sf.genomeview.data.das.DAS;
import net.sf.genomeview.data.das.DAS.EntryPoint;
import net.sf.jannot.exception.ReadFailedException;
import net.sf.jannot.source.DataSource;
import net.sf.jannot.source.FileSource;
import net.sf.jannot.source.IndexedFastaDataSource;
import net.sf.jannot.source.IndexedMAFDataSource;
import net.sf.jannot.source.MultiFileSource;
import net.sf.jannot.source.SAMDataSource;
import net.sf.jannot.source.SSL;
import net.sf.jannot.source.URLSource;
import net.sf.jannot.tabix.IndexedFeatureFile;
import be.abeel.io.LineIterator;

/**
 * 
 * @author Thomas Abeel
 * 
 */
public class DataSourceFactory {
	private static Logger log = Logger.getLogger(DataSourceFactory.class.getCanonicalName());

	public enum Sources {
		LOCALFILE, URL, DIRECTORY, DAS;
		@Override
		public String toString() {
			switch (this) {
			case URL:
				return "URL";
			case LOCALFILE:
				return "Local file";
			case DIRECTORY:
				return "Directory";
			case DAS:
				return "DAS server";
			}
			return null;
		}
	}

	// private static SAMDataSource constructSAM(URL url) throws IOException {
	// SSL.certify(url);
	// return new SAMDataSource(url);
	//
	// }

	public static DataSource createURL(URL url) throws IOException, ReadFailedException {
		DataSource indexedSource = tryIndexedStuff(url);
		if (indexedSource != null) {
			return indexedSource;
		} else {
			if (Configuration.getBoolean("general:disableURLCaching")) {
				return new URLSource(url);
			} else {
				return new CachedURLSource(url);
			}

		}
	}

	public static DataSource[] create(Sources source, Model model, final String[] extensions) {
		switch (source) {
		case DIRECTORY:
			try {
				JFileChooser chooser = new JFileChooser(Configuration.getFile("lastDirectory"));
				chooser.setMultiSelectionEnabled(false);
				chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
				chooser.setFileFilter(new FileFilter() {

					@Override
					public boolean accept(File f) {
						return f.isDirectory();
					}

					@Override
					public String getDescription() {
						return "Directories";
					}

				});
				int returnVal = chooser.showOpenDialog(model.getGUIManager().getParent());
				if (returnVal == JFileChooser.APPROVE_OPTION) {

					File file = chooser.getSelectedFile();

					DataSource[] out = new DataSource[1];
					out[0] = new MultiFileSource(file);
					Configuration.set("lastDirectory", file.getParentFile());
					return out;
				} else {
					return null;
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			break;
		case URL:

			try {
				String input = JOptionPane.showInputDialog(model.getGUIManager().getParent(),
						"Give the URL of the data");
				if (input != null && input.trim().length() > 0) {
					URL url = new URL(input.trim());
					return new DataSource[] { createURL(url) };
				} else
					return null;

			} catch (ReadFailedException re) {
				JOptionPane.showMessageDialog(model.getGUIManager().getParent(), "Could not read data from source: "
						+ re.getMessage(),"Error!",JOptionPane.ERROR_MESSAGE);
			} catch (Exception e) {
				e.printStackTrace();
			}
			break;
		case DAS:
			try {
				String url = JOptionPane.showInputDialog(model.getGUIManager().getParent(), "Give the URL of the data")
						.trim();
				DAS das = new DAS(url);
				List<String> refs = das.getReferences();
				Collections.sort(refs);
				String ref = (String) JOptionPane.showInputDialog(model.getGUIManager().getParent(),
						"Select reference genome", "Reference selection", JOptionPane.INFORMATION_MESSAGE, null, refs
								.toArray(), refs.get(0));
				List<EntryPoint> eps = das.getEntryPoints(ref);
				EntryPoint ep = (EntryPoint) JOptionPane.showInputDialog(model.getGUIManager().getParent(),
						"Select entry point", "Entry point selection", JOptionPane.INFORMATION_MESSAGE, null, eps
								.toArray(), eps.get(0));
				das.setEntryPoint(ep);
				das.setReference(ref);
				return new DataSource[] { das };
			} catch (Exception e) {
				e.printStackTrace();
			}
			break;
		case LOCALFILE:
			try {
				JFileChooser chooser = new JFileChooser(Configuration.getFile("lastDirectory"));
				chooser.resetChoosableFileFilters();
				if (extensions != null) {

					for (final String ext : extensions) {
						chooser.addChoosableFileFilter(new FileFilter() {

							@Override
							public boolean accept(File f) {
								if (f.isDirectory())
									return true;
								return f.getName().toLowerCase().endsWith(ext)
										|| f.getName().toLowerCase().endsWith(ext + ".gz")
										|| f.getName().toLowerCase().endsWith(ext + ".bgz");
							}

							@Override
							public String getDescription() {
								return ext + " files";
							}

						});
					}
					chooser.addChoosableFileFilter(new FileFilter() {

						@Override
						public boolean accept(File f) {
							if (f.isDirectory())
								return true;
							for (String ext : extensions) {

								if (f.getName().toLowerCase().endsWith(ext)
										|| f.getName().toLowerCase().endsWith(ext + ".gz")
										|| f.getName().toLowerCase().endsWith(ext + ".bgz")) {
									return true;
								}
							}
							return false;
						}

						@Override
						public String getDescription() {
							return Arrays.toString(extensions);
						}

					});
				}

				chooser.setMultiSelectionEnabled(true);
				int returnVal = chooser.showOpenDialog(model.getGUIManager().getParent());
				if (returnVal == JFileChooser.APPROVE_OPTION) {
					File[] files = chooser.getSelectedFiles();
					DataSource[] out = new DataSource[files.length];
					for (int i = 0; i < files.length; i++) {
						out[i] = createFile(files[i]);

					}
					Configuration.set("lastDirectory", files[0].getParentFile());
					return out;
				} else {
					return null;
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			break;
		}
		return null;
	}

	public static DataSource createFile(File file) throws IOException, ReadFailedException {

		DataSource indexedSource = tryIndexedStuff(file);
		if (indexedSource != null)
			return indexedSource;
		else
			return new FileSource(file);
	}

	/**
	 * Method will return <code>null</code> when the input object does not allow
	 * indexed access. The caller should handle this by defaulting to the memory
	 * based parsers.
	 * 
	 * @param in
	 * @return
	 * @throws IOException
	 * @throws ReadFailedException
	 */
	private static DataSource tryIndexedStuff(Object in) throws IOException, ReadFailedException {
		String fileName = in.toString();
		/* Don't try for webservices, that's not going to work */
		if (fileName.indexOf('&') >= 0 || fileName.indexOf('?') >= 0) {
			log.info("Not trying indexing, this looks like a webservice: " + fileName);
			return null;
		}
		if (fileName.toLowerCase().endsWith("bai")) {
			if (in instanceof File)
				return new SAMDataSource(new File(fileName.substring(0, fileName.length() - 4)));
			if (in instanceof URL) {
				SSL.certify((URL) in);
				return new SAMDataSource(new URL(fileName.substring(0, fileName.length() - 4)));

			}
		} else {
			String indexName = fileName + ".fai";
			if (checkExist(in, indexName)) {
				log.fine("Reading faidx file...");
				if (in instanceof File)
					return new IndexedFastaDataSource((File) in);
				if (in instanceof URL)
					return new IndexedFastaDataSource((URL) in);
			}

			indexName = fileName + ".tbi";
			if (checkExist(in, indexName)) {
				log.fine("Reading tabix file...");
				if (in instanceof File)
					return new IndexedFeatureFile((File) in, 8000, 50);
				if (in instanceof URL)
					return new IndexedFeatureFile((URL) in, 8000, 50);
			}
			
			indexName = fileName + ".mfi";
			if (checkExist(in, indexName)) {
				log.fine("Reading mafix file...");
				if (in instanceof File)
					return new IndexedMAFDataSource((File) in);
				if (in instanceof URL)
					return new IndexedMAFDataSource((URL) in);
			}

		}
		/* No indexing scheme found */
		log.fine("No index file found for "+fileName);
		return null;
	}

	private static boolean checkExist(Object in, String string) {
		if (in instanceof File)
			return new File(string).exists();
		if (in instanceof URL)
			try {
				log.fine("Checking: " + string);
				URLConnection conn = new URL(string).openConnection();
				conn.setUseCaches(false);
				log.fine(conn.getHeaderFields().toString());
				LineIterator it = new LineIterator(conn.getInputStream());
				it.setSkipBlanks(true);
				String line = it.next().trim();
				while (line.length() < 1)
					line = it.next().trim();
				log.fine("Checkline: " + line);
				it.close();

				/*
				 * This is not supposed to happend, except with badly configured
				 * CMS that take over
				 */
				if (line.startsWith("<!DOCTYPE"))
					return false;
				;

				return true;
			} catch (IOException ioe) {
				System.err.println(ioe);
				return false;
			}
		return false;
	}

}
