/**
 * %HEADER%
 */
package net.sf.genomeview.gui.explorer;

import java.awt.Color;
import java.awt.EventQueue;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Observable;
import java.util.Observer;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JDialog;
import javax.swing.JTabbedPane;
import javax.swing.border.Border;

import net.sf.genomeview.core.Configuration;
import net.sf.genomeview.core.Icons;
import net.sf.genomeview.data.Model;
import net.sf.genomeview.gui.StaticUtils;
import be.abeel.io.LineIterator;

/**
 * 
 * @author Thomas Abeel
 * 
 */
public class GenomeExplorer extends JDialog {

	
	private static final long serialVersionUID = -7057835080241255157L;
	private Model model;

	GenomeExplorer(final Model model) {
		super(model.getGUIManager().getParent(), "GenomeView :: " + Configuration.version()+" - Genome Explorer",ModalityType.APPLICATION_MODAL);
		setIconImage(Icons.MINILOGO);
		this.model = model;
	
		//super.setUndecorated(true);
		setResizable(false);
		this.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
		this.addWindowListener(new WindowAdapter() {

			/* (non-Javadoc)
			 * @see java.awt.event.WindowAdapter#windowClosing(java.awt.event.WindowEvent)
			 */
			@Override
			public void windowClosing(WindowEvent e) {
				model.getGUIManager().getGenomeExplorer().setVisible(false);
			}
			
		});
		
		JTabbedPane tabs = new JTabbedPane();

		HashMap<String, ArrayList<Genome>> list = parse(GenomeExplorer.class.getResourceAsStream("/conf/instances.txt"));

		String common="<br><br>You can load any of the instances by clicking on the image of the organisms in the list to the right.<br><br>At the top of this dialog, there are tabs for more genomes.<br><br>You can immediately start working with your data by clicking the 'Work with my data' button above.</html>";
		tabs.addTab(
				"Tutorial genomes",
				new GenomesPanel(model,
						"<html>Welcome to the GenomeView Genome Explorer. This is your portal to preloaded GenomeView instances."+common,
						list.get("demo")));
		tabs.addTab("Plant genomes", new GenomesPanel(model,"<html>Welcome to the GenomeView plant genomes section. This is your portal to the GenomeView plant genomes.<br><br>These genomes are made available in collaboration with the Plaza platform."+common, list.get("plant")));
		tabs.addTab("Animal genomes", new GenomesPanel(model,"<html>Welcome to the GenomeView animal genomes section. This is your portal to the GenomeView animal genomes."+common, list.get("animal")));

		Border emptyBorder = BorderFactory.createEmptyBorder(15, 15, 15, 15);
		Border colorBorder = BorderFactory.createLineBorder(Color.BLACK);
		tabs.setBorder(BorderFactory.createCompoundBorder(colorBorder, emptyBorder));
		tabs.setBackground(Color.WHITE);

		setContentPane(tabs);
		setBackground(Color.WHITE);

		pack();
		
		StaticUtils.center(this);
		// setVisible(true);
	}

	private Logger log = Logger.getLogger(GenomeExplorer.class.getCanonicalName());

	private HashMap<String, ArrayList<Genome>> parse(InputStream resourceAsStream) {
		LineIterator it = new LineIterator(resourceAsStream);
		HashMap<String, ArrayList<Genome>> out = new HashMap<String, ArrayList<Genome>>();
		for (String line : it) {
			String[] arr = line.split("\t");
			if (!out.containsKey(arr[0])) {
				out.put(arr[0], new ArrayList<Genome>());

			}
			try {
				out.get(arr[0]).add(new Genome(model, arr[1], instanceImage(arr[2]), arr[3], new URL(arr[4])));
			} catch (Exception e) {
				log.log(Level.SEVERE, "Could not parse instance line: " + line, e);
			}

		}
		it.close();
		return out;
	}

	private Icon instanceImage(String path) {
		return new ImageIcon(Genome.class.getResource("/images/instances/" + path));
	}

	

}
