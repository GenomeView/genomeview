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

import javax.swing.JComponent;
import javax.swing.TransferHandler;

import net.sf.genomeview.core.Configuration;
import net.sf.genomeview.data.DataSourceHelper;
import net.sf.genomeview.data.Model;
import net.sf.genomeview.plugin.PluginLoader;
import net.sf.jannot.exception.ReadFailedException;
import net.sf.jannot.source.Locator;

/**
 * 
 * @author Thomas Abeel
 * 
 */
class DropTransferHandler extends TransferHandler {

	private Model model;
	private DataFlavor urlFlavor;

	public DropTransferHandler(Model model) {
		this.model = model;
		try { 
			urlFlavor = 
			new DataFlavor ("application/x-java-url; class=java.net.URL"); 
		} catch (ClassNotFoundException cnfe) { 
			cnfe.printStackTrace( ); 
		}
	}

	private static final long serialVersionUID = -661823132133744933L;

	@Override
	public boolean canImport(JComponent arg0, DataFlavor[] flavors) {
		Set<DataFlavor> accepted = new HashSet<DataFlavor>();
		accepted.add(DataFlavor.javaFileListFlavor);   //data files
		accepted.add(DataFlavor.stringFlavor);         //urls or even files on some systems
		accepted.add(urlFlavor);                       //custom url flavor
		
		for (int i = 0; i < flavors.length; i++) {
			if (accepted.contains(flavors[i])){
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
						if (file.getName().endsWith(".jar")){
							PluginLoader.installPlugin(file, Configuration.getPluginDirectory());	
						} else {
							DataSourceHelper.load(model,new Locator(file.toString()));							
						}
					}
					return true;
				} else if (flavor.equals(DataFlavor.stringFlavor)){
					String s =(String) t.getTransferData(DataFlavor.stringFlavor);
	        		System.out.println("String dropped: "+s);
	        		if (s.endsWith(".jar")){
	        			PluginLoader.installPlugin(new URL(s), Configuration.getPluginDirectory());	        			
	        		} else {
	        			DataSourceHelper.load(model,new Locator(s));
	        		}
				} else if (flavor.equals(urlFlavor)){
					URL url = (URL) t.getTransferData (urlFlavor);
	        		System.out.println("URL dropped: "+url);
	        		if (url.toString().endsWith(".jar")){
	        			PluginLoader.installPlugin(url, Configuration.getPluginDirectory());	        			
	        		} else {
	        			DataSourceHelper.load(model,new Locator(url.toString()));
	        		}
				} else {
					System.out.println("Data rejected: " + flavor);
					// Don't return; try next flavor.
				}
			} catch (IOException ex) {
				System.err.println("IOError getting data: " + ex);
			} catch (UnsupportedFlavorException e) {
				System.err.println("Unsupported Flavor: " + e);
			} catch (URISyntaxException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ReadFailedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return false;
	}
}