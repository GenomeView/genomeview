/**
 * %HEADER%
 */
package net.sf.genomeview.gui;

import java.awt.Frame;

import net.sf.genomeview.gui.explorer.DataExplorerManager;
import net.sf.genomeview.gui.information.InformationFrame;
import net.sf.genomeview.gui.viztracks.GeneEvidenceLabel;

/**
 * Manages GUI components that are accessible.
 * 
 * @author Thomas Abeel
 * @author thpar
 * 
 */
public class GUIManager {

	/**
	 * The main window of the GUI belonging to this model.
	 */
	private Frame parent;
	private GeneEvidenceLabel gel = null;
	private StatusBar statusBar;
	private DataExplorerManager genomeExplorerManager;
	private InformationFrame infoFrame;

	public void registerMainWindow(Frame parentFrame) {
		this.parent = parentFrame;

	}

	/**
	 * 
	 * @return application main window
	 */
	public Frame getMainWindow() {
		return parent;
	}

	public StatusBar getStatusBar() {
		return statusBar;
	}

	public void registerEvidenceLabel(GeneEvidenceLabel gel) {
		this.gel = gel;
	}

	public GeneEvidenceLabel getEvidenceLabel() {
		return gel;
	}

	public void registerStatusBar(StatusBar statusBar) {
		this.statusBar = statusBar;

	}

	public void registerGenomeExplorer(
			DataExplorerManager genomeExplorerManager) {
		this.genomeExplorerManager = genomeExplorerManager;

	}

	public DataExplorerManager getGenomeExplorer() {
		return genomeExplorerManager;
	}

	public void registerInformationFrame(InformationFrame infoFrame) {
		this.infoFrame = infoFrame;
	}

	public InformationFrame getInformationFrame() {
		return infoFrame;
	}

}
