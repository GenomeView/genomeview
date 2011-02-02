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
import java.util.Iterator;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.TransferHandler;

import net.sf.genomeview.data.Model;
import net.sf.genomeview.scheduler.ReadWorker;
import net.sf.jannot.exception.ReadFailedException;
import net.sf.jannot.source.DataSource;
import net.sf.jannot.source.DataSourceFactory;

/**
 * 
 * @author Thomas Abeel
 * 
 */
class DropTransferHandler extends TransferHandler {

	private Model model;

	public DropTransferHandler(Model model) {
		this.model = model;
	}

	private static final long serialVersionUID = -661823132133744933L;

	@Override
	public boolean canImport(JComponent arg0, DataFlavor[] flavors) {
		for (int i = 0; i < flavors.length; i++) {
			DataFlavor flavor = flavors[i];
//			System.out.println("Flavor: " + flavor);
			if (flavor.equals(DataFlavor.javaFileListFlavor)) {
				return true;
			}
		}
		return false;
	}

	@Override
	public boolean importData(JComponent comp, Transferable t) {
		DataFlavor[] flavors = t.getTransferDataFlavors();
		for (int i = 0; i < flavors.length; i++) {
			DataFlavor flavor = flavors[i];
			try {
				if (flavor.equals(DataFlavor.javaFileListFlavor)) {
					System.out.println("importData: FileListFlavor");

					List<File> l = (List<File>) t.getTransferData(DataFlavor.javaFileListFlavor);
					Iterator<File> iter = l.iterator();
					while (iter.hasNext()) {
						File file = (File) iter.next();
						System.out.println("File dropped: " + file.getCanonicalPath());

						DataSource ds = DataSourceFactory.createFile(file);
						ReadWorker rf = new ReadWorker(ds, model);
						rf.execute();

					
					}
					return true;
				} else {
					System.out.println("Data rejected: " + flavor);
					// Don't return; try next flavor.
				}
			} catch (IOException ex) {
				System.err.println("IOError getting data: " + ex);
			} catch (UnsupportedFlavorException e) {
				System.err.println("Unsupported Flavor: " + e);
			} catch (ReadFailedException ex) {
				System.err.println("Readfailed exception getting data: " + ex);
			} catch (URISyntaxException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return false;
	}
}