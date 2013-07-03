/**
 * %HEADER%
 */
package net.sf.genomeview.gui.explorer;

import java.awt.Color;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
import be.abeel.net.URIFactory;

/**
 * 
 * @author Thomas Abeel
 * 
 */
class GenomeExplorer extends JDialog {

	private static final long serialVersionUID = -7057835080241255157L;
	private Model model;
	private JTabbedPane tabs;

	GenomeExplorer(final Model model) {
		super(model.getGUIManager().getParent(), "GenomeView :: " + Configuration.version() + " - Genome Explorer",
				ModalityType.APPLICATION_MODAL);
		setIconImage(Icons.MINILOGO);
		this.model = model;

		this.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
		this.addWindowListener(new WindowAdapter() {

			@Override
			public void windowClosing(WindowEvent e) {
				model.getGUIManager().getGenomeExplorer().setVisible(false);
			}

		});

		tabs = new JTabbedPane();

		HashMap<String, ArrayList<Genome>> list = new HashMap<String, ArrayList<Genome>>();

		LineIterator it = new LineIterator(GenomeExplorer.class.getResourceAsStream("/conf/repositories.txt"), true,
				true);
		for (String line : it)
			try {
				parse(list, new URL(line).openStream());
			} catch (Exception e) {
				log.error( "Could not load instances from " + line, e);
			}
		it.close();
		parse(list, GenomeExplorer.class.getResourceAsStream("/conf/plazainstances.txt"));

		String externalRepo = Configuration.get("external:repository");
		if (!externalRepo.equalsIgnoreCase("null")) {
			try {
				parse(list, new URL(externalRepo).openStream());
			} catch (Exception e) {
				log.error( "Something went wrong while loading the external repository: " + externalRepo, e);
			}
		}

		System.out.println("instances: " + list);

		String common = "<br><br>You can load any of the instances by clicking on the image of one of the organisms in the list to the right.<br><br>At the top of this dialog, there are tabs for more genomes.<br><br>You can immediately start working with your data by clicking the 'Work with my data' button above.</html>";

		tabs.addTab("Tutorial genomes", new GenomesPanel(model,
				"<html>Welcome to the GenomeView Genome Explorer. This is your portal to preloaded GenomeView instances."
						+ common, list.get("demo")));

		tabs.addTab(
				"Microbial genomes",
				new GenomesPanel(
						model,
						"<html>Welcome to the GenomeView Genome Explorer. This is your portal to preloaded GenomeView instances for microbial genomes."
								+ common, list.get("microbial")));

		tabs.addTab(
				"Plant genomes",
				new GenomesPanel(
						model,
						"<html>Welcome to the GenomeView plant genomes section. This is your portal to the GenomeView plant genomes.<br><br>These genomes are made available in collaboration with the <a href='http://bioinformatics.psb.ugent.be/plaza/'>PLAZA platform</a> (<a href='http://bioinformatics.psb.ugent.be/plaza/credits/credits'>credits</a>)."
								+ common, list.get("plant")));

		tabs.addTab("Fungal genomes", new GenomesPanel(model,
				"<html>Welcome to the GenomeView fungal genomes section. This is your portal to the GenomeView fungal genomes."
						+ common, list.get("fungi")));

		tabs.addTab("Insect genomes", new GenomesPanel(model,
				"<html>Welcome to the GenomeView insect genomes section. This is your portal to the GenomeView animal genomes."
						+ common, list.get("insect")));

		tabs.addTab("Mammalian genomes", new GenomesPanel(model,
				"<html>Welcome to the GenomeView mammalian genomes section. This is your portal to the GenomeView animal genomes."
						+ common, list.get("animal")));

		tabs.addTab("NCBI Bacterial", new NCBIPanel(model));

		tabs.addTab(
				"Archived genomes",
				new GenomesPanel(
						model,
						"<html>Welcome to the GenomeView archived genomes section. This section contains all previous releases of genomes in the main sections that have been archived."
								+ common, list.get("archived")));

		if (!externalRepo.equalsIgnoreCase("null")) {
			try {
				Set<String> set = Configuration.getStringSet("external:repository:labels");

				for (String s : set) {
					String[] arr = s.split(":");
					String description = Configuration.get("external:repository:description:" + arr[0]);
					tabs.addTab(arr[1], new GenomesPanel(model, "<html>"
							+ (description == null ? common : description + "</html>"), list.get(arr[0])));
				}
				tabs.setSelectedIndex(tabs.getTabCount() - 1);
			} catch (Exception e) {
				log.error( "Something went wrong while loading the external repository: " + externalRepo, e);

			}
		}

		Border emptyBorder = BorderFactory.createEmptyBorder(15, 15, 15, 15);
		Border colorBorder = BorderFactory.createLineBorder(Color.BLACK);
		tabs.setBorder(BorderFactory.createCompoundBorder(colorBorder, emptyBorder));
		tabs.setBackground(Color.WHITE);

		setContentPane(tabs);
		setBackground(Color.WHITE);

		pack();
		StaticUtils.center(model.getGUIManager().getParent(), this);
	}

	private Logger log = LoggerFactory.getLogger(GenomeExplorer.class.getCanonicalName());

	private void parse(HashMap<String, ArrayList<Genome>> list, InputStream resourceAsStream) {
		LineIterator it = new LineIterator(resourceAsStream, true, true);
		for (String line : it) {
			String[] arr = line.split("\t");
			if (!list.containsKey(arr[0])) {
				list.put(arr[0], new ArrayList<Genome>());

			}
			try {
				list.get(arr[0]).add(new Genome(model, arr[1], instanceImage(arr[2]), arr[3], URIFactory.url(arr[4])));
			} catch (Exception e) {
				log.error( "Could not parse instance line: " + line, e);
			}

		}
		it.close();

	}

	private Icon instanceImage(String path) {
		URL url = Genome.class.getResource("/images/instances/" + path);
		if (path.startsWith("http"))
			try {
				url = new URL(path);
			} catch (MalformedURLException e) {
				log.error( "Failed to load instance image", e);
			}

		if (url == null)
			url = Genome.class.getResource("/images/instances/nopicture.png");
		return new ImageIcon(url);
	}

	void scollToTop() {
		for (int i = 0; i < tabs.getTabCount(); i++) {
			((ScrollToTop) tabs.getComponent(i)).scrollToTop();
		}

	}

}
