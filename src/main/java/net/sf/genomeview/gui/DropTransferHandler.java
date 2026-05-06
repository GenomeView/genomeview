/**
 * %HEADER%
 */
package net.sf.genomeview.gui;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;

import javax.swing.JComponent;
import javax.swing.TransferHandler;

import net.sf.genomeview.data.DataSourceHelper;
import net.sf.genomeview.data.Model;
import net.sf.jannot.exception.ReadFailedException;
import net.sf.jannot.source.Locator;
import tudelft.utilities.logging.Reporter;

/**
 * Probably a handler for drag&drop actions
 * 
 * @author Thomas Abeel
 * 
 */
class DropTransferHandler extends TransferHandler {

	private final Model model;
	private DataFlavor urlFlavor;
	private DataFlavor uriFlavor;

	public DropTransferHandler(Model model) {
		this.model = model;
		try {
			urlFlavor = new DataFlavor(
					"application/x-java-url;class=java.net.URL");
			uriFlavor = new DataFlavor("text/uri-list;class=java.lang.String");
		} catch (ClassNotFoundException cnfe) {
			model.getLog().log(Level.SEVERE, "Can't load drop transfer handler",
					cnfe);
		}
	}

	private static final long serialVersionUID = -661823132133744933L;

	@Override
	public boolean canImport(JComponent arg0, DataFlavor[] flavors) {
		Set<DataFlavor> accepted = new HashSet<DataFlavor>();
		accepted.add(uriFlavor); // custom uri (string) flavor
		accepted.add(urlFlavor); // custom url flavor
		accepted.add(DataFlavor.javaFileListFlavor); // data files
		accepted.add(DataFlavor.stringFlavor); // urls or even files on some
												// systems

		for (int i = 0; i < flavors.length; i++) {
			if (accepted.contains(flavors[i])) {
				return true;
			}
		}
		return false;
	}

	@Override
	public boolean importData(JComponent comp, Transferable t) {
		final Reporter log = model.getLog();
		DataFlavor[] flavors = t.getTransferDataFlavors();
		for (int i = 0; i < flavors.length; i++) {
			DataFlavor flavor = flavors[i];
			try {
				if (flavor.equals(urlFlavor)) {
					URL url = (URL) t.getTransferData(urlFlavor);
					log.log(Level.INFO, "URL dropped: " + url);
					new DataSourceHelper(model)
							.load(new Locator(url.toString(), log));
					return true;
				} else if (flavor.equals(uriFlavor)) {
					String uriString = (String) t.getTransferData(uriFlavor);
					log.log(Level.INFO, "URI String dropped: " + uriString);
					new DataSourceHelper(model)
							.load(new Locator(uriString, log));
					return true;
				} else if (flavor.equals(DataFlavor.stringFlavor)) {
					String initString = (String) t
							.getTransferData(DataFlavor.stringFlavor);
					log.log(Level.INFO, "String dropped: " + initString);
					String[] lines = initString
							.split(System.getProperty("line.separator"));
					for (String s : lines) {
						log.log(Level.INFO, "String '" + s + "'");
						new DataSourceHelper(model).load(new Locator(s, log));
					}
					return true;
				} else if (flavor.equals(DataFlavor.javaFileListFlavor)) {
					log.log(Level.INFO, "importData: FileListFlavor");

					List<File> l = (List<File>) t
							.getTransferData(DataFlavor.javaFileListFlavor);

					Iterator<File> iter = l.iterator();
					while (iter.hasNext()) {
						File file = iter.next();
						log.log(Level.INFO,
								"File dropped: " + file.getCanonicalPath());
						new DataSourceHelper(model)
								.load(new Locator(file.toString(), log));
					}
					if (l.size() != 0) {
						return true;
					} else {
						log.log(Level.INFO,
								"FileList was empty... (trying next flavor)");
					}
				} else {
					log.log(Level.WARNING, "Data rejected: " + flavor);
					// Don't return; try next flavor.
				}
			} catch (IOException | URISyntaxException | ReadFailedException
					| UnsupportedFlavorException ex) {
				log.log(Level.INFO, "Problem handling drop" + ex);
			}
		}
		return false;
	}
}