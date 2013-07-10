package net.sf.genomeview.plugin;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;
import java.io.File;
import java.net.URL;

import net.sf.genomeview.core.Configuration;
import net.sf.genomeview.data.Model;

public class PluginDropListener implements DropTargetListener {
	private static DataFlavor urlFlavor;
	
	public PluginDropListener() {
		try { 
			urlFlavor = 
			new DataFlavor ("application/x-java-url; class=java.net.URL"); 
		} catch (ClassNotFoundException cnfe) { 
			cnfe.printStackTrace( ); 
		}
	}

	@Override
	public void dragEnter(DropTargetDragEvent dtde) {
		// TODO Auto-generated method stub

	}

	@Override
	public void dragOver(DropTargetDragEvent dtde) {
		// TODO Auto-generated method stub

	}

	@Override
	public void dropActionChanged(DropTargetDragEvent dtde) {
		// TODO Auto-generated method stub

	}

	@Override
	public void dragExit(DropTargetEvent dte) {
		// TODO Auto-generated method stub

	}

	@Override
	public void drop(DropTargetDropEvent event) {
		// Accept copy drops
        event.acceptDrop(DnDConstants.ACTION_COPY);

        // Get the transfer which can provide the dropped item data
        Transferable transferable = event.getTransferable();

        boolean gotData = false;
        try {
        	if (transferable.isDataFlavorSupported(urlFlavor)){
        		URL url = (URL) transferable.getTransferData (urlFlavor);
        		System.out.println("URL: "+url);
        		PluginLoader.installPlugin(url, Configuration.getPluginDirectory());
        		gotData = true;
        	} else if (transferable.isDataFlavorSupported(DataFlavor.stringFlavor)){
        		String s =(String) transferable.getTransferData (DataFlavor.stringFlavor);
        		System.out.println("URL String: "+s);
        		PluginLoader.installPlugin(new URL(s), Configuration.getPluginDirectory());
        		gotData = true;
        	}
        } catch (Exception e) {
        	e.printStackTrace();
        } finally{            	
        	event.dropComplete(gotData);
        }
	}

}
