package net.sf.genomeview.gui.explorer;

import java.awt.Component;

import net.sf.genomeview.data.Model;
import net.sf.genomeview.gui.explorer.NCBIPanel.NCBIRepository;

import be.abeel.gui.GridBagPanel;
/**
 * 
 * @author Thomas Abeel
 *
 */
public class NCBIPanel extends GridBagPanel {

	private Model model;
	private NCBIRepository repos;

	class NCBIRepository{

		public NCBIRepository(String string) {
			// TODO Auto-generated constructor stub
		}
		
	}
	
	public NCBIPanel(Model model, String string) {
		this.model=model;
		this.repos=new NCBIRepository(string);
		
	}

}
