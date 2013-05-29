/**
 * %HEADER%
 */
package net.sf.genomeview.gui.menu.navigation;

import java.awt.event.ActionEvent;
import java.util.ArrayList;

import javax.swing.JOptionPane;
import javax.swing.KeyStroke;


import net.sf.genomeview.data.Model;
import net.sf.genomeview.gui.MessageManager;
import net.sf.genomeview.gui.menu.AbstractModelAction;
import net.sf.genomeview.gui.viztracks.Track;

/**
 * 
 * @author Thomas Abeel
 *
 */
public class GotoTrack extends AbstractModelAction {


    
	private static final long serialVersionUID = -6221594184950780381L;

	public GotoTrack(Model model) {
        super(MessageManager.getString("navigationmenu.goto_track"), model);
        super.putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke("control T"));
    }

    @Override
    public void actionPerformed(ActionEvent arg0) {
        String input = JOptionPane.showInputDialog(MessageManager.getString("navigationmenu.provide_trackname"));
        if (input != null&&input.trim().length()>0) {
        	
        		input=input.toLowerCase();
        		ArrayList<Track>hits=new ArrayList<Track>();
        		for(Track t:model.getTrackList()){
        			if(t.getDataKey().toString().toLowerCase().contains(input)||t.config().displayName().toLowerCase().contains(input))
        				hits.add(t);
        		
        			
        		}
        		if(hits.size()>0)
        			model.getGUIManager().getEvidenceLabel().scroll2track(hits.get(0));
        		
        		
        
            
        }

    }

}
