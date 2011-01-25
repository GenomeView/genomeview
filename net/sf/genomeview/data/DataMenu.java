package net.sf.genomeview.data;

import java.io.File;
import java.net.URL;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileFilter;

import net.sf.genomeview.core.Configuration;
import net.sf.jannot.exception.ReadFailedException;
import net.sf.jannot.source.DataSource;
import net.sf.jannot.source.DataSourceFactory;
import net.sf.jannot.source.DataSourceFactory.Sources;
import net.sf.jannot.source.MultiFileSource;
import net.sf.jannot.source.das.DAS;
import net.sf.jannot.source.das.DAS.EntryPoint;

public class DataMenu {
	
	
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
					return new DataSource[] { DataSourceFactory.createURL(url) };
				} else
					return null;

			} catch (ReadFailedException re) {
				JOptionPane.showMessageDialog(model.getGUIManager().getParent(), "Could not read data from source: "
						+ re.getMessage(), "Error!", JOptionPane.ERROR_MESSAGE);
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
						"Select reference genome", "Reference selection", JOptionPane.INFORMATION_MESSAGE, null,
						refs.toArray(), refs.get(0));
				List<EntryPoint> eps = das.getEntryPoints(ref);
				EntryPoint ep = (EntryPoint) JOptionPane.showInputDialog(model.getGUIManager().getParent(),
						"Select entry point", "Entry point selection", JOptionPane.INFORMATION_MESSAGE, null,
						eps.toArray(), eps.get(0));
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
						out[i] = DataSourceFactory.createFile(files[i]);

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
}
