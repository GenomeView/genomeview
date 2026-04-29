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
import java.util.logging.Level;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;

import be.abeel.gui.GridBagPanel;
import be.abeel.gui.TitledComponent;
import net.sf.genomeview.core.Configuration;
import net.sf.genomeview.core.MessageManager;
import net.sf.genomeview.data.Model;
import net.sf.genomeview.data.NotificationTypes;
import net.sf.genomeview.gui.StaticUtils;
import net.sf.genomeview.gui.dialog.HelpButton;

@SuppressWarnings("serial")
public class ConfigurationDialog extends JDialog {

	private static ConfigurationDialog dialog = null;

	public static void showConfigurationDialog(Model model) {
		if (dialog == null) {
			dialog = new ConfigurationDialog(model);
		}
		dialog.setVisible(true);
	}

	class PileupConfigPanel extends GridBagPanel {
		private Model model;

		public PileupConfigPanel(Model model) {
			this.model = model;
			final MessageManager mm = model.getMessageMgr();

			this.add(ConfigBox.booleanInstance(model,
					"pileup:crossTrackScaling",
					mm.getString("configdialog.cross_connect_track_scalling")),
					gc);
			gc.gridy++;

			this.add(
					new JLabel(
							mm.getString("configdialog.default_track_config")),
					gc);
			gc.gridy++;
			this.add(
					ConfigBox.booleanInstance(model, "pileup:dynamicRange",
							mm.getString("configdialog.tracks_dynamic_range")),
					gc);
			gc.gridy++;
			this.add(ConfigBox.booleanInstance(model, "pileup:logScale",
					mm.getString("configdialog.tracks_log_scale")), gc);
			gc.gridy++;
			this.add(ConfigBox.doubleInstance("pileup:maxPile",
					mm.getString("configdialog.max_height_pileup_track"),
					model), gc);
			gc.gridy++;
			this.add(ConfigBox.integerInstance("pileup:switchBarLine",
					mm.getString("configdialog.switch_bar_to_line"), model),
					gc);

		}
	}

	static class AANucleotideColorsConfigPanel extends GridBagPanel {

		public AANucleotideColorsConfigPanel(Model model) {
			final MessageManager mm = model.getMessageMgr();
			setLayout(new BorderLayout());
			Container aa = new Container();
			aa.setLayout(new GridLayout(0, 8));
			for (char c : model.getConfiguration().getAminoAcids()) {
				aa.add(ConfigBox.colorInstance(model, "AA_" + c, "" + c));

			}
			Container nt = new Container();
			nt.setLayout(new GridLayout(0, 8));
			for (char c : model.getConfiguration().getNucleotides()) {
				nt.add(ConfigBox.colorInstance(model, "N_" + c, "" + c));
			}
			this.add(
					new TitledComponent(mm.getString("configdialog.aminoacids"),
							aa),
					BorderLayout.NORTH);
			this.add(
					new TitledComponent(mm.getString("configdialog.nucleotids"),
							nt),
					BorderLayout.CENTER);
		}

	}

	/**
	 * Options for Short reads
	 * 
	 * @author Thomas Abeel
	 * 
	 */
	@SuppressWarnings("serial")
	static class ShortReadConfigPanel extends GridBagPanel {

		public ShortReadConfigPanel(Model model) {
			final MessageManager mm = model.getMessageMgr();
			this.add(ConfigBox.integerInstance("shortread:graphLineHeight",
					mm.getString("configdialog.coverage_graph_height"), model),
					gc);
			gc.gridy++;

			this.add(ConfigBox.integerInstance("shortread:snpTrackHeight",
					mm.getString("configdialog.snp_track_height"), model), gc);
			gc.gridy++;

			this.add(ConfigBox.integerInstance(
					"shortread:snpTrackMinimumCoverage",
					mm.getString("configdialog.snp_min_coverage"), model), gc);
			gc.gridy++;

			this.add(ConfigBox.integerInstance("shortread:maxReads",
					mm.getString("configdialog.max_number_displayed_reads"),
					model), gc);
			gc.gridy++;
			this.add(ConfigBox.integerInstance("shortread:maxRegion",
					mm.getString("configdialog.max_range_nucleotides_reads"),
					model), gc);
			gc.gridy++;
			this.add(ConfigBox.integerInstance("shortread:maxStack",
					mm.getString("configdialog.max_depth_stacked_reads"),
					model), gc);
			gc.gridy++;
			this.add(ConfigBox.booleanInstance(model, "shortread:enablepairing",
					mm.getString("configdialog.draw_connected_paired_reads")),
					gc);
			gc.gridy++;
			this.add(ConfigBox.integerInstance("shortread:maximumCache",
					mm.getString("configdialog.max_reads_cache"), model), gc);
			gc.gridy++;
			this.add(ConfigBox.integerInstance("shortread:maximumPairing",
					mm.getString("configdialog.max_distance_paired_reads"),
					model), gc);
			gc.gridy++;
			this.add(ConfigBox.integerInstance("shortread:maximumPairing",
					mm.getString("configdialog.max_distance_paired_reads"),
					model), gc);
			gc.gridy++;
			this.add(ConfigBox.colorInstance(model, "shortread:forwardColor",
					mm.getString(
							"configdialog.color_reads_mapping_forward_strands")),
					gc);
			gc.gridy++;
			this.add(ConfigBox.colorInstance(model, "shortread:reverseColor",
					mm.getString(
							"configdialog.color_reads_mapping_reverse_strands")),
					gc);
			gc.gridy++;
			this.add(ConfigBox.colorInstance(model,
					"shortread:forwardAntiColor",
					mm.getString(
							"configdialog.color_reads_mapping_forward_strands_anti")),
					gc);
			gc.gridy++;
			this.add(ConfigBox.colorInstance(model,
					"shortread:reverseAntiColor",
					mm.getString(
							"configdialog.color_reads_mapping_reverse_strands_anti")),
					gc);
			gc.gridy++;
			this.add(
					ConfigBox.colorInstance(model, "shortread:pairingColor",
							mm.getString(
									"configdialog.color_between_paired_reads")),
					gc);
			gc.gridy++;
			this.add(ConfigBox.colorInstance(model,
					"shortread:mateDifferentChromosome",
					"Mate different chromosome"), gc);
			gc.gridy++;
			this.add(ConfigBox.colorInstance(model,
					"shortread:missingMateColor", "Missing mate"), gc);
			gc.gridy++;
			this.add(ConfigBox.colorInstance(model, "shortread:splicingColor",
					"Splicing color"), gc);
			gc.gridy++;
		}
	}

	class StructureConfigPanel extends GridBagPanel {

		public StructureConfigPanel(Model model) {
			final MessageManager mm = model.getMessageMgr();

			this.add(ConfigBox.booleanInstance(model,
					"general:onlyMethionineAsStart",
					mm.getString("configdialog.show_only_methionine_as_start")),
					gc);
			gc.gridy++;
			this.add(ConfigBox.booleanInstance(model, "track:showStructure",
					mm.getString(
							"configdialog.show_structure_track_on_startup")),
					gc);
			gc.gridy++;
			this.add(ConfigBox.integerInstance("geneStructureLineHeight",
					mm.getString("configdialog.track_height_pixels"), model),
					gc);
			gc.gridy++;
			this.add(
					ConfigBox.booleanInstance(model, "colorStartCodons",
							mm.getString("configdialog.color_start_codons")),
					gc);
			gc.gridy++;
			this.add(
					ConfigBox.booleanInstance(model, "colorStopCodons",
							mm.getString("configdialog.color_stop_codons")),
					gc);
			gc.gridy++;
			this.add(
					ConfigBox.booleanInstance(model, "showNucleotideColor",
							mm.getString("configdialog.color_nucleotides")),
					gc);
			gc.gridy++;
			this.add(
					ConfigBox.booleanInstance(model, "showSpliceSiteColor",
							mm.getString("configdialog.color_splice_sites")),
					gc);
			gc.gridy++;

			this.add(
					ConfigBox.integerInstance(
							"structureview:maximumNoVisibleFeatures",
							mm.getString("configdialog.max_features"), model),
					gc);

		}
	}

	static class FeatureTrackConfigPanel extends GridBagPanel {

		public FeatureTrackConfigPanel(Model model) {
			final MessageManager mm = model.getMessageMgr();
			gc.gridwidth = 2;
			gc.weightx = 1;
			gc.weighty = 0;
			this.add(
					ConfigBox.booleanInstance(model, "track:forceFeatureLabels",
							mm.getString("configdialog.force_label_features")),
					gc);
			gc.gridy++;
			gc.weighty = 1;
			Container typeContainer = new Container();
			typeContainer.setLayout(new GridLayout(0, 4));

			for (Type type : Type.values()) {
				typeContainer.add(ConfigBox.colorInstance(model, "TYPE_" + type,
						type.toString()));

			}

			this.add(new TitledComponent(
					mm.getString("configdialog.features_types"), typeContainer),
					gc);

		}
	}

	class ComparativePanel extends GridBagPanel {

		public ComparativePanel(final Model model) {
			final MessageManager mm = model.getMessageMgr();

			gc.gridwidth = 2;
			gc.weightx = 1;
			this.add(ConfigBox.booleanInstance(model, "maf:enableAnnotation",
					mm.getString(
							"configdialog.enable_comparative_annotations")),
					gc);
			gc.gridy++;
			this.add(
					ConfigBox.stringInstance(model, "maf:annotationType",
							mm.getString(
									"configdialog.annotation_comparative")),
					gc);
			gc.gridy++;
			this.add(ConfigBox.integerInstance("maf:maximumVisibleRange",
					mm.getString("configdialog.maximum_visible_range"), model),
					gc);
			gc.gridy++;
			this.add(ConfigBox.booleanInstance(model, "maf:extendedNames",
					mm.getString("configdialog.extended_names")), gc);
		}
	}

	class MiscellaneousPanel extends GridBagPanel {

// FIXME clean this up
		public MiscellaneousPanel(final Model model) {
			final MessageManager mm = model.getMessageMgr();
			final Configuration config = model.getConfiguration();
			gc.gridwidth = 2;
			gc.weightx = 1;
			this.add(
					ConfigBox.integerInstance("general:zoomout",
							mm.getString("configdialog.max_zoom_out"), model),
					gc);
			gc.gridy++;
			this.add(
					ConfigBox.booleanInstance(model,
							"general:monitorConnection",
							mm.getString("configdialog.monitor_connection")),
					gc);
			gc.gridy++;
			this.add(
					ConfigBox.booleanInstance(model, "dualscreen",
							mm.getString("configdialog.enable_dual_screen")),
					gc);
			gc.gridy++;
			this.add(
					ConfigBox.booleanInstance(model, "general:singleInstance",
							mm.getString("configdialog.allow_one_instance")),
					gc);
			gc.gridy++;
			this.add(ConfigBox.booleanInstance(model, "geneticCodeSelection",
					mm.getString("configdialog.enable_genetic_code_selection")),
					gc);
			gc.gridy++;
			this.add(ConfigBox.integerInstance("general:exportMagnifyFactor",
					mm.getString(
							"configdialog.resultion_increase_export_images"),
					model), gc);
			gc.gridy++;

			this.add(ConfigBox.dropDownInstance(model,
					config.getStringSet("resource:lang:available")
							.toArray(new String[0]),
					"lang:current",
					mm.getString("configdialog.select_language")), gc);

			gc.gridy++;
			gc.gridwidth = 1;

			this.add(
					ConfigBox.booleanInstance(model,
							"general:disableURLCaching",
							mm.getString("configdialog.disable_url_caching")),
					gc);
			gc.weightx = 0;
			gc.gridx++;
			this.add(new HelpButton(model.getGUIManager().getMainWindow(),
					mm.getString("configdialog.disable_url_caching_to_save")),
					gc);
			gc.gridwidth = 2;
			gc.weightx = 1;
			gc.gridx = 0;
			gc.gridy++;
			// this.add(ConfigBox.booleanInstance("logToFile",
			// "Log console output to a file (Requires restart)"), gc);
			JButton resetButton = new JButton(
					mm.getString("configdialog.reset_configuration"));
			resetButton.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent e) {
					config.reset(model);

				}

			});

			this.add(resetButton, gc);
			JButton resetCache = new JButton(
					mm.getString("configdialog.empty_cache"));
			resetCache.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent e) {
					File dir = new File(config.getDirectory(), "cache");
					;
					for (File f : dir.listFiles()) {
						model.getLog().log(Level.INFO,
								"Marking for deletion:" + f);
						f.deleteOnExit();
					}
					JOptionPane.showMessageDialog(
							model.getGUIManager().getMainWindow(),
							mm.getString("configdialog.clear_cache_info"),
							mm.getString("configdialog.cache_cleared"),
							JOptionPane.INFORMATION_MESSAGE);

				}

			});
			gc.gridy++;
			this.add(resetCache, gc);

		}
	}

	public ConfigurationDialog(final Model model) {
		super(model.getGUIManager().getMainWindow(), model.getMessageMgr()
				.getString("configddialog.configuration_panel"));
		final MessageManager mm = model.getMessageMgr();

		setModal(true);
		setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		addWindowListener(new WindowAdapter() {

			@Override
			public void windowClosing(WindowEvent e) {
				try {
					model.getConfiguration().save();
					model.refresh(NotificationTypes.CONFIGURATION_CHANGE);
				} catch (IOException e1) {
					model.getLog().log(Level.WARNING, "window close issue", e1);
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

		jtp.add(mm.getString("configdialog.structure_view_tab"), structure);
		// jtp.add("Evidence view", evidence);
		jtp.add(mm.getString("configdialog.nucleotide_color_tab"), colors);
		jtp.add(mm.getString("configdialog.feature_track_tab"),
				new FeatureTrackConfigPanel(model));

		jtp.add(mm.getString("configdialog.short_reads_tab"),
				new ShortReadConfigPanel(model));
		jtp.add(mm.getString("configdialog.pileup_tracks_tab"),
				new PileupConfigPanel(model));

		jtp.add(mm.getString("configdialog.compartive_track_tab"),
				new ComparativePanel(model));

		jtp.add(mm.getString("configdialog.miscellaneous_tab"), miscPanel);

		add(jtp, BorderLayout.CENTER);

		JButton ok = new JButton(mm.getString("button.ok"));
		ok.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				StaticUtils.run(() -> {
					model.getConfiguration().save();
					model.refresh(NotificationTypes.CONFIGURATION_CHANGE);
				}, model.getLog(), Level.WARNING, "config change issue");

				setVisible(false);

			}

		});
		add(ok, BorderLayout.SOUTH);
		pack();
		StaticUtils.center(model.getGUIManager().getMainWindow(), this);

	}
}
