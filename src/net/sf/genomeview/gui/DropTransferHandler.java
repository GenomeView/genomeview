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
import java.util.ArrayList;
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
	private DataFlavor uriFlavor;

	public DropTransferHandler(Model model) {
		this.model = model;
		try { 
			urlFlavor = new DataFlavor ("application/x-java-url;class=java.net.URL"); 
			uriFlavor = new DataFlavor ("text/uri-list;class=java.lang.String");
		} catch (ClassNotFoundException cnfe) { 
			cnfe.printStackTrace( ); 
		}
	}

	private static final long serialVersionUID = -661823132133744933L;

	@Override
	public boolean canImport(JComponent arg0, DataFlavor[] flavors) {
		Set<DataFlavor> accepted = new HashSet<DataFlavor>();
		accepted.add(uriFlavor);                       //custom uri (string) flavor
		accepted.add(urlFlavor);                       //custom url flavor
		accepted.add(DataFlavor.javaFileListFlavor);   //data files
		accepted.add(DataFlavor.stringFlavor);         //urls or even files on some systems
		
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
				if (flavor.equals(urlFlavor)){
					URL url = (URL) t.getTransferData (urlFlavor);
					System.out.println("URL dropped: "+url);
					if (url.toString().endsWith(".jar")){
						PluginLoader.installPlugin(url, Configuration.getPluginDirectory());	        			
					} else {
						DataSourceHelper.load(model,new Locator(url.toString()));
					}
					return true;
				} else if (flavor.equals(uriFlavor)){
					String uriString = (String) t.getTransferData (uriFlavor);
					System.out.println("URI String dropped: "+uriString);
					if (uriString.endsWith(".jar")){
						PluginLoader.installPlugin(new URL(uriString), Configuration.getPluginDirectory());	        			
					} else {
						DataSourceHelper.load(model,new Locator(uriString));
					}
					return true;
				} else if (flavor.equals(DataFlavor.stringFlavor)){
					String initString =(String) t.getTransferData(DataFlavor.stringFlavor);
					System.out.println("String dropped: "+initString);
					String[] lines = initString.split(System.getProperty("line.separator"));
					for (String s : lines){
						System.out.println("String '"+s+"'");
						if (s.endsWith(".jar")){
							PluginLoader.installPlugin(new URL(s), Configuration.getPluginDirectory());	        			
						} else {
							DataSourceHelper.load(model,new Locator(s));
						}
					}
					return true;
				} else if (flavor.equals(DataFlavor.javaFileListFlavor)) {
					System.out.println("importData: FileListFlavor");

					List<File> l = (List<File>)t.getTransferData(DataFlavor.javaFileListFlavor);
					
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
					if (l.size() != 0){
						return true;						
					} else {
						System.out.println("FileList was empty... (trying next flavor)");
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