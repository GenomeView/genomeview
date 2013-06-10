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
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;

import net.sf.genomeview.core.Configuration;
import net.sf.genomeview.data.Model;
import net.sf.genomeview.gui.MessageManager;
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

	class PileupConfigPanel extends GridBagPanel {
		/**
		 * 
		 */
		private static final long serialVersionUID = 2077613517228432752L;
		private Model model;

		public PileupConfigPanel(Model model) {
			this.model = model;

			this.add(ConfigBox.booleanInstance(model, "pileup:crossTrackScaling", MessageManager.getString("configdialog.cross_connect_track_scalling")), gc);
			gc.gridy++;

			this.add(new JLabel(MessageManager.getString("configdialog.default_track_config")), gc);
			gc.gridy++;
			this.add(ConfigBox.booleanInstance(model, "pileup:dynamicRange", MessageManager.getString("configdialog.tracks_dynamic_range")), gc);
			gc.gridy++;
			this.add(ConfigBox.booleanInstance(model, "pileup:logScale", MessageManager.getString("configdialog.tracks_log_scale")), gc);
			gc.gridy++;
			this.add(ConfigBox.doubleInstance("pileup:maxPile", MessageManager.getString("configdialog.max_height_pileup_track")), gc);
			gc.gridy++;
			this.add(ConfigBox.integerInstance("pileup:switchBarLine", MessageManager.getString("configdialog.switch_bar_to_line")), gc);

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
			this.add(new TitledComponent(MessageManager.getString("configdialog.aminoacids"), aa), BorderLayout.NORTH);
			this.add(new TitledComponent(MessageManager.getString("configdialog.nucleotids"), nt), BorderLayout.CENTER);
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

			// this.add(ConfigBox.booleanInstance("shortread:logScaling",
			// "Use logarithmic scaling"), gc);
			// gc.gridy++;
			//
			// this.add(ConfigBox.doubleInstance("shortread:bottomValue",
			// "Bottom value"), gc);
			// gc.gridy++;
			//
			// this.add(ConfigBox.doubleInstance("shortread:topValue",
			// "Top value, use negative value for unlimited"), gc);
			// gc.gridy++;

			this.add(ConfigBox.integerInstance("shortread:graphLineHeight", MessageManager.getString("configdialog.coverage_graph_height")), gc);
			gc.gridy++;

			this.add(ConfigBox.integerInstance("shortread:snpTrackHeight", MessageManager.getString("configdialog.snp_track_height")), gc);
			gc.gridy++;

			this.add(ConfigBox.integerInstance("shortread:snpTrackMinimumCoverage", MessageManager.getString("configdialog.snp_min_coverage")), gc);
			gc.gridy++;

			this.add(ConfigBox.integerInstance("shortread:maxReads", MessageManager.getString("configdialog.max_number_displayed_reads")), gc);
			gc.gridy++;
			this.add(ConfigBox.integerInstance("shortread:maxRegion", MessageManager.getString("configdialog.max_range_nucleotides_reads")), gc);
			gc.gridy++;
			this.add(ConfigBox.integerInstance("shortread:maxStack", MessageManager.getString("configdialog.max_depth_stacked_reads")), gc);
			gc.gridy++;
			this.add(ConfigBox.booleanInstance(model, "shortread:enablepairing", MessageManager.getString("configdialog.draw_connected_paired_reads")), gc);
			gc.gridy++;
			this.add(ConfigBox.integerInstance("shortread:maximumCache", MessageManager.getString("configdialog.max_reads_cache")), gc);
			gc.gridy++;
			this.add(ConfigBox.integerInstance("shortread:maximumPairing", MessageManager.getString("configdialog.max_distance_paired_reads")), gc);
			gc.gridy++;
			this.add(ConfigBox.integerInstance("shortread:maximumPairing", MessageManager.getString("configdialog.max_distance_paired_reads")), gc);
			gc.gridy++;
			this.add(ConfigBox.colorInstance(model, "shortread:forwardColor", MessageManager.getString("configdialog.color_reads_mapping_forward_strands")), gc);
			gc.gridy++;
			this.add(ConfigBox.colorInstance(model, "shortread:reverseColor", MessageManager.getString("configdialog.color_reads_mapping_reverse_strands")), gc);
			gc.gridy++;
			this.add(
					ConfigBox.colorInstance(model, "shortread:forwardAntiColor",
							MessageManager.getString("configdialog.color_reads_mapping_forward_strands_anti")), gc);
			gc.gridy++;
			this.add(
					ConfigBox.colorInstance(model, "shortread:reverseAntiColor",
							MessageManager.getString("configdialog.color_reads_mapping_reverse_strands_anti")), gc);
			gc.gridy++;
			this.add(ConfigBox.colorInstance(model, "shortread:pairingColor", MessageManager.getString("configdialog.color_between_paired_reads")), gc);

		}
	}

	// /**
	// * Options for the AnnotationView
	// *
	// * @author Thomas Abeel
	// *
	// */
	// static class AnnotationConfigPanel extends GridBagPanel {
	//
	// /**
	// *
	// */
	// private static final long serialVersionUID = -628553625113038258L;
	//
	// public AnnotationConfigPanel(Model model) {
	// // this.add(ConfigBox.integerInstance("evidenceLineHeight",
	// "Height of a track in pixels"), gc);
	// // gc.gridy++;
	// // this.add(ConfigBox.booleanInstance(model,"showTrackName",
	// "Show evidence track names"), gc);
	// // gc.gridy++;
	// this.add(ConfigBox.booleanInstance(model,"useColorQualifierTag",
	// "Use the /color qualifier tag"), gc);
	// gc.gridy++;
	// // this.add(ConfigBox.stringInstance("visibleTypes",
	// "Visible types on start-up"), gc);
	// // gc.gridy++;
	// //
	// this.add(ConfigBox.integerInstance("annotationview:maximumNoVisibleFeatures",
	// "Maximum number of features of each type to display on the AnnotationView"),
	// gc);
	// }
	// }

	class StructureConfigPanel extends GridBagPanel {
		/**
         * 
         */
		private static final long serialVersionUID = 7503579007314777946L;

		public StructureConfigPanel(Model model) {
			this.add(ConfigBox.booleanInstance(model, "general:onlyMethionineAsStart", MessageManager.getString("configdialog.show_only_methionine_as_start")),
					gc);
			gc.gridy++;
			this.add(ConfigBox.booleanInstance(model, "track:showStructure", MessageManager.getString("configdialog.show_structure_track_on_startup")), gc);
			gc.gridy++;
			this.add(ConfigBox.integerInstance("geneStructureLineHeight", MessageManager.getString("configdialog.track_height_pixels")), gc);
			gc.gridy++;
			this.add(ConfigBox.booleanInstance(model, "colorStartCodons", MessageManager.getString("configdialog.color_start_codons")), gc);
			gc.gridy++;
			this.add(ConfigBox.booleanInstance(model, "colorStopCodons", MessageManager.getString("configdialog.color_stop_codons")), gc);
			gc.gridy++;
			this.add(ConfigBox.booleanInstance(model, "showNucleotideColor", MessageManager.getString("configdialog.color_nucleotides")), gc);
			gc.gridy++;
			this.add(ConfigBox.booleanInstance(model, "showSpliceSiteColor", MessageManager.getString("configdialog.color_splice_sites")), gc);
			gc.gridy++;

			this.add(ConfigBox.integerInstance("structureview:maximumNoVisibleFeatures", MessageManager.getString("configdialog.max_features")), gc);

		}
	}

	static class FeatureTrackConfigPanel extends GridBagPanel {
		/**
         * 
         */
		private static final long serialVersionUID = 7503579007314777946L;

		public FeatureTrackConfigPanel(Model model) {
			gc.gridwidth = 2;
			gc.weightx = 1;
			gc.weighty = 0;
			this.add(ConfigBox.booleanInstance(model, "track:forceFeatureLabels", MessageManager.getString("configdialog.force_label_features")), gc);
			gc.gridy++;
			gc.weighty = 1;
			Container typeContainer = new Container();
			typeContainer.setLayout(new GridLayout(0, 4));

			for (Type type : Type.values()) {
				typeContainer.add(ConfigBox.colorInstance(model, "TYPE_" + type, type.toString()));

			}

			this.add(new TitledComponent(MessageManager.getString("configdialog.features_types"), typeContainer), gc);

		}
	}

	class ComparativePanel extends GridBagPanel {

		/**
		 * 
		 */
		private static final long serialVersionUID = 511805592898297604L;

		public ComparativePanel(final Model model) {
			gc.gridwidth = 2;
			gc.weightx = 1;
			this.add(ConfigBox.booleanInstance(model, "maf:enableAnnotation", MessageManager.getString("configdialog.enable_comparative_annotations")), gc);
			gc.gridy++;
			this.add(ConfigBox.stringInstance("maf:annotationType", MessageManager.getString("configdialog.annotation_comparative")), gc);
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
			this.add(ConfigBox.integerInstance("general:zoomout", MessageManager.getString("configdialog.max_zoom_out")), gc);
			gc.gridy++;
			this.add(ConfigBox.booleanInstance(model,"general:monitorConnection", MessageManager.getString("configdialog.monitor_connection")), gc);
			gc.gridy++;
			this.add(ConfigBox.booleanInstance(model,"dualscreen", MessageManager.getString("configdialog.enable_dual_screen")), gc);
			gc.gridy++;
			this.add(ConfigBox.booleanInstance(model,"general:singleInstance", MessageManager.getString("configdialog.allow_one_instance")), gc);
			gc.gridy++;
			this.add(ConfigBox.booleanInstance(model,"geneticCodeSelection", MessageManager.getString("configdialog.enable_genetic_code_selection")), gc);
			gc.gridy++;
			this.add(ConfigBox.integerInstance("general:exportMagnifyFactor", MessageManager.getString("configdialog.resultion_increase_export_images")), gc);
			gc.gridy++;
			
			this.add(ConfigBox.dropDownInstance(Configuration.getStringSet("resource:lang:available").toArray(new String[0]),"lang:current", MessageManager.getString("configdialog.select_language")), gc);
			
			
			
			gc.gridy++;
			gc.gridwidth = 1;
		
			
			
			this.add(ConfigBox.booleanInstance(model,"general:disableURLCaching", MessageManager.getString("configdialog.disable_url_caching")), gc);
			gc.weightx=0;
			gc.gridx++;
			this.add(new HelpButton(model.getGUIManager().getParent(), MessageManager.getString("configdialog.disable_url_caching_to_save")),gc);
			gc.gridwidth = 2;
			gc.weightx=1;
			gc.gridx=0;
			gc.gridy++;
			// this.add(ConfigBox.booleanInstance("logToFile",
			// "Log console output to a file (Requires restart)"), gc);
			JButton resetButton = new JButton(MessageManager.getString("configdialog.reset_configuration"));
			resetButton.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent e) {
					Configuration.reset(model);

				}

			});

			this.add(resetButton, gc);
			JButton resetCache = new JButton(MessageManager.getString("configdialog.empty_cache"));
			resetCache.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent e) {
					File dir = new File(Configuration.getDirectory(), "cache");
					;
					for (File f : dir.listFiles()) {
						System.out.println("Marking for deletion:" + f);
						f.deleteOnExit();
					}
					JOptionPane.showMessageDialog(model.getGUIManager().getParent(), MessageManager.getString("configdialog.clear_cache_info"), MessageManager.getString("configdialog.cache_cleared"), JOptionPane.INFORMATION_MESSAGE);

				}

			});
			gc.gridy++;
			// gc.gridy++;
			this.add(resetCache, gc);

		}
	}

	public ConfigurationDialog(Model model) {
		super(model.getGUIManager().getParent(), MessageManager.getString("configddialog.configuration_panel"));
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
		JPanel structure = new StructureConfigPanel(model);
		// JPanel evidence = new AnnotationConfigPanel(model);
		JPanel colors = new AANucleotideColorsConfigPanel(model);
		JPanel miscPanel = new MiscellaneousPanel(model);

		jtp.add(MessageManager.getString("configdialog.structure_view_tab"), structure);
		// jtp.add("Evidence view", evidence);
		jtp.add(MessageManager.getString("configdialog.nucleotide_color_tab"), colors);
		jtp.add(MessageManager.getString("configdialog.feature_track_tab"), new FeatureTrackConfigPanel(model));

		jtp.add(MessageManager.getString("configdialog.short_reads_tab"), new ShortReadConfigPanel(model));
		jtp.add(MessageManager.getString("configdialog.pileup_tracks_tab"), new PileupConfigPanel(model));

		jtp.add(MessageManager.getString("configdialog.compartive_track_tab"), new ComparativePanel(model));

		jtp.add(MessageManager.getString("configdialog.miscellaneous_tab"), miscPanel);

		add(jtp, BorderLayout.CENTER);

		JButton ok = new JButton(MessageManager.getString("button.ok"));
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
		StaticUtils.center(model.getGUIManager().getParent(), this);

	}
}
