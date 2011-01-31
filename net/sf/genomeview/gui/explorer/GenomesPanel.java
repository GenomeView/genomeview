/**
 * %HEADER%
 */
package net.sf.genomeview.gui.explorer;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JScrollPane;

import net.sf.genomeview.core.Icons;
import net.sf.genomeview.data.Model;
import net.sf.genomeview.gui.dialog.OpenDialog;
import be.abeel.gui.GridBagPanel;

/**
 * 
 * @author Thomas Abeel
 * 
 */
class GenomesPanel extends GridBagPanel {

	private static final long serialVersionUID = 2022250811407852659L;

	public GenomesPanel(final Model model,String msg, ArrayList<Genome> list) {
			Toolkit toolkit = Toolkit.getDefaultToolkit();
			Dimension dim = toolkit.getScreenSize();
			setPreferredSize(new Dimension((int) (dim.width * 0.8), 480));
			setBackground(Color.WHITE);
			add(new JLabel(Icons.LOGO), gc);
			gc.gridy++;
			gc.weightx = 0;
			
			JButton myData=new JButton("Work with my data",Icons.LARGEOPEN);
			myData.setBackground(Color.WHITE);
			add(myData, gc);
			myData.addActionListener(new ActionListener() {
				
				@Override
				public void actionPerformed(ActionEvent e) {
					model.getGUIManager().getGenomeExplorer().bg.setVisible(false);
					new OpenDialog(model.getGUIManager().getGenomeExplorer().bg,model);
					
				}
			});
			
			
			
			gc.gridy++;
			gc.weighty = 1;
			JLabel msgLabel=new JLabel(msg);
			msgLabel.setVerticalAlignment(JLabel.NORTH);
			msgLabel.setPreferredSize(new Dimension(Icons.LOGO.getIconWidth(),50));
			add(msgLabel, gc);
			

			
			
			gc.weighty = 0;
			gc.gridy++;
			JButton box=new JButton("Dismiss this dialog");
			box.setBackground(Color.WHITE);
			box.addActionListener(new ActionListener() {
				
				@Override
				public void actionPerformed(ActionEvent e) {
					model.getGUIManager().getGenomeExplorer().setVisible(false);
					
				}
			});
			add(box, gc);

			/*------------------*/
			gc.gridy = 0;
			gc.gridx++;
			gc.gridheight = 10;
			gc.weightx = 1;
			gc.weighty = 1;
			GridBagPanel buttons = new GridBagPanel();
			buttons.setBackground(Color.WHITE);
			
			buttons.gc.fill = GridBagConstraints.HORIZONTAL;
			buttons.gc.weightx = 1;
			buttons.gc.weighty = 0;
			buttons.gc.anchor = GridBagConstraints.NORTHWEST;
			if (list != null) {
				for (Genome g : list) {
					buttons.add(g, buttons.gc);
					buttons.gc.gridy++;
				}
			}
			buttons.gc.weighty = 1;
			JLabel space=new JLabel();
			space.setBackground(Color.WHITE);
			buttons.add(space, buttons.gc);

			JScrollPane jp = new JScrollPane(buttons);
			jp.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
			add(jp, gc);
			
			gc.gridx=0;
			gc.gridy=20;
			gc.gridwidth=2;
			gc.weightx=1;
			gc.weighty=0;
			JLabel requestNewGenome=new JLabel("If your genome of interest is not in the list, ");
			requestNewGenome.setBackground(Color.WHITE);


		}
}
