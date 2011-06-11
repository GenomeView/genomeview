/**
 * %HEADER%
 */
package net.sf.genomeview.gui.config;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;

import net.sf.genomeview.core.Configuration;
import net.sf.genomeview.data.Model;
import net.sf.genomeview.gui.StaticUtils;
import net.sf.genomeview.gui.dialog.HelpButton;
import net.sf.jannot.Type;
import be.abeel.gui.GridBagPanel;
import be.abeel.gui.TitledComponent;

public class ConfigurationDialog extends JDialog {

	private static final long serialVersionUID = 3226397962717512578L;

	private static ConfigurationDialog dialog = null;

	public static void showConfigurationDialog(Model model) {
		if (dialog == null)
			dialog = new ConfigurationDialog(model);
		dialog.setVisible(true);
	}

	

	

	class PileupConfigPanel extends GridBagPanel{
		/**
		 * 
		 */
		private static final long serialVersionUID = 2077613517228432752L;
		private Model model;

		public PileupConfigPanel(Model model) {
			this.model=model;
			
			this.add(ConfigBox.booleanInstance("pileup:dynamicRange", "Should all tracks use dynamic range?"), gc);
			gc.gridy++;
			this.add(ConfigBox.booleanInstance("pileup:crossTrackScaling", "Cross connect the track scaling."), gc);
			gc.gridy++;
			
			
			this.add(ConfigBox.booleanInstance("pileup:logScale", "Should all tracks be log scaled?"), gc);
			gc.gridy++;
			this.add(ConfigBox.doubleInstance("pileup:maxPile", "Maximum height of the pileup track"), gc);
			gc.gridy++;
			this.add(ConfigBox.integerInstance("pileup:switchBarLine", "Switch from bar chart to line graph at N nt"), gc);
			
		}
	}

	
	static class AANucleotideColorsConfigPanel extends GridBagPanel {

		private static final long serialVersionUID = -2574897453334264771L;

		public AANucleotideColorsConfigPanel(Model model) {
			setLayout(new BorderLayout());
			Container aa = new Container();
			aa.setLayout(new GridLayout(0, 8));
			for (char c : Configuration.getAminoAcids()) {
				aa.add(ConfigBox.colorInstance(model, "AA_" + c, "" + c));

			}
			Container nt = new Container();
			nt.setLayout(new GridLayout(0, 8));
			for (char c : Configuration.getNucleotides()) {
				nt.add(ConfigBox.colorInstance(model, "N_" + c, "" + c));
			}
			this.add(new TitledComponent("Amino acids", aa), BorderLayout.NORTH);
			this.add(new TitledComponent("Nucleotides", nt), BorderLayout.CENTER);
		}

	}

	/**
	 * Options for Short reads
	 * 
	 * @author Thomas Abeel
	 * 
	 */
	static class ShortReadConfigPanel extends GridBagPanel {

		/**
         * 
         */
		private static final long serialVersionUID = -628553625113038258L;

		public ShortReadConfigPanel(Model model) {
			
//			this.add(ConfigBox.booleanInstance("shortread:logScaling", "Use logarithmic scaling"), gc);
//			gc.gridy++;
//			
//			this.add(ConfigBox.doubleInstance("shortread:bottomValue", "Bottom value"), gc);
//			gc.gridy++;
//			
//			this.add(ConfigBox.doubleInstance("shortread:topValue", "Top value, use negative value for unlimited"), gc);
//			gc.gridy++;
			
			this.add(ConfigBox.integerInstance("shortread:graphLineHeight", "Height of the coverage graph in pixels"), gc);
			gc.gridy++;
			
			this.add(ConfigBox.integerInstance("shortread:snpTrackHeight", "Height SNP track in pixels"), gc);
			gc.gridy++;
			
			this.add(ConfigBox.integerInstance("shortread:snpTrackMinimumCoverage", "Minimum coverage for SNPs to be shown"), gc);
			gc.gridy++;
			
			this.add(ConfigBox.integerInstance("shortread:maxReads", "Maximum number of displayed reads"), gc);
			gc.gridy++;
			this.add(ConfigBox.integerInstance("shortread:maxRegion", "Maximum range in nucleotides to display individual reads"), gc);
			gc.gridy++;
			this.add(ConfigBox.integerInstance("shortread:maxStack", "<html>Maximum display depth of stacked reads, <br/>deeper stacked reads will not be shown individually, <br/>but are included in the pile-up view</html>"), gc);
			gc.gridy++;
			this.add(ConfigBox.booleanInstance("shortread:enablepairing", "Draw a connection between paired reads"), gc);
			gc.gridy++;
			this.add(ConfigBox.integerInstance("shortread:maximumCache", "Maximum number of reads to cache"), gc);
			gc.gridy++;
			this.add(ConfigBox.integerInstance("shortread:maximumPairing", "Maximum distance between paired reads"), gc);
			gc.gridy++;
			this.add(ConfigBox.integerInstance("shortread:maximumPairing", "Maximum distance between paired reads"), gc);
			gc.gridy++;
			this.add(ConfigBox.colorInstance(model, "shortread:forwardColor", "Color of the forward reads"), gc);
			gc.gridy++;
			this.add(ConfigBox.colorInstance(model, "shortread:reverseColor", "Color of the reverse reads"), gc);
			gc.gridy++;
			this.add(ConfigBox.colorInstance(model, "shortread:pairingColor", "Color of the line between paired reads"), gc);

		}
	}

	/**
	 * Options for the AnnotationView
	 * 
	 * @author Thomas Abeel
	 * 
	 */
	static class AnnotationConfigPanel extends GridBagPanel {

		/**
         * 
         */
		private static final long serialVersionUID = -628553625113038258L;

		public AnnotationConfigPanel() {
			this.add(ConfigBox.integerInstance("evidenceLineHeight", "Height of a track in pixels"), gc);
			gc.gridy++;
			this.add(ConfigBox.booleanInstance("showTrackName", "Show evidence track names"), gc);
			gc.gridy++;
			this.add(ConfigBox.booleanInstance("useColorQualifierTag", "Use the /color qualifier tag"), gc);
			gc.gridy++;
			this.add(ConfigBox.stringInstance("visibleTypes", "Visible types on start-up"), gc);
			gc.gridy++;
			this.add(ConfigBox.integerInstance("annotationview:maximumNoVisibleFeatures", "Maximum number of features of each type to display on the AnnotationView"), gc);
		}
	}

	class StructureConfigPanel extends GridBagPanel {
		/**
         * 
         */
		private static final long serialVersionUID = 7503579007314777946L;

		public StructureConfigPanel() {
			this.add(ConfigBox.booleanInstance("general:onlyMethionineAsStart","Show only Methionine as start codon"),gc);
			gc.gridy++;
			this.add(ConfigBox.booleanInstance("track:showStructure", "Show structure track on start-up"), gc);
			gc.gridy++;
			this.add(ConfigBox.integerInstance("geneStructureLineHeight", "Height of a track in pixels"), gc);
			gc.gridy++;
			this.add(ConfigBox.booleanInstance("colorStartCodons", "Color start codons"), gc);
			gc.gridy++;
			this.add(ConfigBox.booleanInstance("colorStopCodons", "Color stop codons"), gc);
			gc.gridy++;
			this.add(ConfigBox.booleanInstance("showNucleotideColor", "Color nucleotides"), gc);
			gc.gridy++;
			this.add(ConfigBox.booleanInstance("showSpliceSiteColor", "Color splice sites (overrides nucleotide coloring)"), gc);
			gc.gridy++;

			this.add(ConfigBox.stringInstance("visibleTypesStructure", "Visible types on start-up"), gc);
			gc.gridy++;
			this.add(ConfigBox.stringInstance("geneStructures", "Types to be considered gene models"), gc);
			gc.gridy++;
			this.add(ConfigBox.integerInstance("structureview:maximumNoVisibleFeatures", "Maximum number of features to display on the structure view"), gc);

		}
	}

	static class FeatureTrackConfigPanel extends GridBagPanel {
		/**
         * 
         */
		private static final long serialVersionUID = 7503579007314777946L;

		public FeatureTrackConfigPanel(Model model) {
			gc.gridwidth = 2;
			gc.weightx=1;
			gc.weighty=0;
			this.add(ConfigBox.booleanInstance("track:forceFeatureLabels", "Force labels on features"), gc);
			gc.gridy++;
			gc.weighty=1;
			Container typeContainer = new Container();
			typeContainer.setLayout(new GridLayout(0, 4));

			for (Type type : Type.values()) {
				typeContainer.add(ConfigBox.colorInstance(model, "TYPE_" + type, type.toString()));

			}

			this.add(new TitledComponent("Feature types", typeContainer), gc);

		}
	}

	class MiscellaneousPanel extends GridBagPanel {

		/**
		 * 
		 */
		private static final long serialVersionUID = 511805592898297604L;

		public MiscellaneousPanel(final Model model) {
			gc.gridwidth = 2;
			gc.weightx=1;
			this.add(ConfigBox.integerInstance("general:zoomout", "Maximum zoom-out size"), gc);
			gc.gridy++;
			this.add(ConfigBox.booleanInstance("dualscreen", "Enable dual-screen mode? (Requires restart)"), gc);
			gc.gridy++;
			this.add(ConfigBox.booleanInstance("general:singleInstance", "Allow only one instance of GenomeView"), gc);
			gc.gridy++;
			this.add(ConfigBox.booleanInstance("geneticCodeSelection", "Enable genetic code selection? (Requires restart)"), gc);
			gc.gridy++;
			this.add(ConfigBox.integerInstance("general:exportMagnifyFactor", "Resolution increase factor for export images"), gc);
			
			
			
			gc.gridy++;
			gc.gridwidth = 1;
		
			
			
			this.add(ConfigBox.booleanInstance("general:disableURLCaching", "Disable URL caching?"), gc);
			gc.weightx=0;
			gc.gridx++;
			this.add(new HelpButton(model.getGUIManager().getParent(), "URL caching needs to be disabled to be able to save to URLs."),gc);
			gc.gridwidth = 2;
			gc.weightx=1;
			gc.gridx=0;
			gc.gridy++;
			// this.add(ConfigBox.booleanInstance("logToFile",
			// "Log console output to a file (Requires restart)"), gc);
			JButton resetButton = new JButton("Reset configuration");
			resetButton.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent e) {
					Configuration.reset(model);

				}

			});

			this.add(resetButton, gc);
			JButton resetCache = new JButton("Empty cache");
			resetCache.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent e) {
					File dir = new File(Configuration.getDirectory(), "cache");
					;
					for (File f : dir.listFiles()) {
						System.out.println("Marking for deletion:" + f);
						f.deleteOnExit();
					}
					JOptionPane.showMessageDialog(model.getGUIManager().getParent(), "<html>Cached files have been marked for deletion, they will be removed when you close GenomeView</html>", "Cache cleared!", JOptionPane.INFORMATION_MESSAGE);

				}

			});
			gc.gridy++;
			// gc.gridy++;
			this.add(resetCache, gc);

		}
	}

	public ConfigurationDialog(Model model) {
		super(model.getGUIManager().getParent(), "Configuration panel");
		setModal(true);
		setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		addWindowListener(new WindowAdapter() {

			@Override
			public void windowClosing(WindowEvent e) {
				try {
					Configuration.save();
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				setVisible(false);
				super.windowClosing(e);

			}

		});
		setLayout(new BorderLayout());
		JTabbedPane jtp = new JTabbedPane();
		// JPanel colorPanel = new ConfigureColorPanel();
		JPanel structure = new StructureConfigPanel();
		JPanel evidence = new AnnotationConfigPanel();
		JPanel colors = new AANucleotideColorsConfigPanel(model);
		JPanel miscPanel = new MiscellaneousPanel(model);
		

		jtp.add("Structure view", structure);
		jtp.add("Evidence view", evidence);
		jtp.add("AA&nucleotide colors", colors);
		jtp.add("Feature track", new FeatureTrackConfigPanel(model));

		jtp.add("Short reads", new ShortReadConfigPanel(model));
		jtp.add("Pile up tracks", new PileupConfigPanel(model));

		jtp.add("Miscellaneous", miscPanel);

		add(jtp, BorderLayout.CENTER);

		JButton ok = new JButton("OK");
		ok.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					Configuration.save();
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				setVisible(false);

			}

		});
		add(ok, BorderLayout.SOUTH);
		pack();
		StaticUtils.center(this);

	}
}
