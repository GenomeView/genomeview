/**
 * %HEADER%
 */
package net.sf.genomeview.gui.dialog;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JColorChooser;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;

import net.sf.genomeview.core.ColorIcon;
import net.sf.genomeview.core.Configuration;
import net.sf.genomeview.data.Model;
import net.sf.jannot.Type;
import be.abeel.gui.GridBagPanel;
import be.abeel.gui.JIntegerField;
import be.abeel.gui.TitledComponent;

public class ConfigurationDialog extends JDialog {

	private static final long serialVersionUID = 3226397962717512578L;

	private static ConfigurationDialog dialog = null;

	public static void showConfigurationDialog(Model model) {
		if (dialog == null)
			dialog = new ConfigurationDialog(model);
		dialog.setVisible(true);
	}

	static class IntegerConfig extends Container {

		/**
         * 
         */
		private static final long serialVersionUID = -1460525692768168573L;

		private JLabel label = new JLabel();

		private JIntegerField valueField = new JIntegerField("");

		public IntegerConfig(final String key, final String title) {
			valueField.setText(Configuration.get(key));
			label.setText(title);
			setLayout(new BorderLayout());
			add(label, BorderLayout.WEST);
			add(valueField, BorderLayout.CENTER);
			valueField.addKeyListener(new KeyAdapter() {

				@Override
				public void keyReleased(KeyEvent e) {
					super.keyReleased(e);
					Configuration.set(key, valueField.getValue());

				}

			});

		}

	}

	static class StringConfig extends Container {

		/**
         * 
         */
		private static final long serialVersionUID = -1460525692768168573L;

		private JLabel label = new JLabel();

		private JTextField valueField = new JTextField("");

		public StringConfig(final String key, final String title) {
			valueField.setText(Configuration.get(key));
			label.setText(title);
			setLayout(new BorderLayout());
			add(label, BorderLayout.WEST);
			add(valueField, BorderLayout.CENTER);
			valueField.addKeyListener(new KeyAdapter() {

				@Override
				public void keyReleased(KeyEvent e) {
					super.keyReleased(e);
					Configuration.set(key, valueField.getText());

				}

			});

		}

	}

	static class BooleanConfig extends JCheckBox {

		private static final long serialVersionUID = 9081788377933556296L;

		public BooleanConfig(final String key, final String title) {
			super(title);
			this.setSelected(Configuration.getBoolean(key));

			this.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					Configuration.set(key, isSelected());

				}

			});
		}
	}

	static class ColorLabel extends JLabel {

		/**
		 * 
		 */
		private static final long serialVersionUID = -7614916778081300913L;

		ColorLabel(final Model model, final String configKey) {
			super(new ColorIcon(Configuration.getColor(configKey), 16));
			this.addMouseListener(new MouseAdapter() {
				public void mouseClicked(MouseEvent e) {
					Color newColor = JColorChooser.showDialog(
							model.getParent(), "Choose track color",
							Configuration.getColor(configKey));

					if (newColor != null) {
						Configuration.setColor(configKey, newColor);
						setIcon(new ColorIcon(
								Configuration.getColor(configKey), 16));
					}
				}
			});

		}
	}

	static class AANucleotideColorsConfigPanel extends GridBagPanel {

		private static final long serialVersionUID = -2574897453334264771L;

		public AANucleotideColorsConfigPanel(Model model) {
			setLayout(new BorderLayout());
			Container aa = new Container();
			aa.setLayout(new GridLayout(0, 8));
			for (char c : Configuration.getAminoAcids()) {
				aa.add(new JLabel("" + c));
				aa.add(new ColorLabel(model, "AA_" + c));

			}
			Container nt = new Container();
			nt.setLayout(new GridLayout(0, 8));
			for (char c : Configuration.getNucleotides()) {
				nt.add(new JLabel("" + c));
				nt.add(new ColorLabel(model, "N_" + c));
			}
			this
					.add(new TitledComponent("Amino acids", aa),
							BorderLayout.NORTH);
			this.add(new TitledComponent("Nucleotides", nt),
					BorderLayout.CENTER);
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

		public ShortReadConfigPanel() {
			this.add(new IntegerConfig("shortread:maxReads",
					"Maximum number of displayed reads"), gc);
			gc.gridy++;
			this.add(new IntegerConfig("shortread:maxRegion",
					"Maximum range in nucleotides to display individual reads"), gc);
			gc.gridy++;
			this.add(new IntegerConfig("shortread:maxStack",
					"<html>Maximum display depth of stacked reads, <br/>deeper stacked reads will not be shown individually, <br/>but are included in the pile-up view</html>"), gc);
			gc.gridy++;
			this.add(new BooleanConfig("shortread:enablepairing",
			"Draw a connection between paired reads"), gc);
	
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
			this.add(new IntegerConfig("evidenceLineHeight",
					"Height of a track in pixels"), gc);
			gc.gridy++;
			this.add(new BooleanConfig("showTrackName",
					"Show evidence track names"), gc);
			gc.gridy++;
			this.add(new BooleanConfig("useColorQualifierTag",
					"Use the /color qualifier tag"), gc);
			gc.gridy++;
			this.add(new StringConfig("visibleTypes",
					"Visible types on start-up"), gc);
			gc.gridy++;
			this
					.add(
							new IntegerConfig(
									"annotationview:maximumNoVisibleFeatures",
									"Maximum number of features of each type to display on the AnnotationView"),
							gc);
		}
	}

	class StructureConfigPanel extends GridBagPanel {
		/**
         * 
         */
		private static final long serialVersionUID = 7503579007314777946L;

		public StructureConfigPanel() {
			this.add(new IntegerConfig("geneStructureLineHeight",
					"Height of a track in pixels"), gc);
			gc.gridy++;
			this
					.add(new BooleanConfig("colorStartCodons",
							"Color start codons"), gc);
			gc.gridy++;
			this.add(new BooleanConfig("colorStopCodons", "Color stop codons"),
					gc);
			gc.gridy++;
			this.add(new BooleanConfig("showNucleotideColor",
					"Color nucleotides"), gc);
			gc.gridy++;
			this.add(new BooleanConfig("showSpliceSiteColor",
					"Color splice sites (overrides nucleotide coloring)"), gc);
			gc.gridy++;

			this.add(new StringConfig("visibleTypesStructure",
					"Visible types on start-up"), gc);
			gc.gridy++;
			this.add(new StringConfig("geneStructures",
					"Types to be considered gene models"), gc);
			gc.gridy++;
			this
					.add(
							new IntegerConfig(
									"structureview:maximumNoVisibleFeatures",
									"Maximum number of features to display on the structure view"),
							gc);

		}
	}

	static class FeatureTrackConfigPanel extends GridBagPanel {
		/**
         * 
         */
		private static final long serialVersionUID = 7503579007314777946L;

		public FeatureTrackConfigPanel(Model model) {
			setLayout(new BorderLayout());
			Container typeContainer = new Container();
			typeContainer.setLayout(new GridLayout(0, 4));

			for (Type type : Type.values()) {

				typeContainer.add(new JLabel("" + type));
				typeContainer.add(new ColorLabel(model, "TYPE_" + type));

			}

			this.add(new TitledComponent("Feature types", typeContainer),
					BorderLayout.CENTER);

		}
	}

	class MiscellaneousPanel extends GridBagPanel {

		/**
		 * 
		 */
		private static final long serialVersionUID = 511805592898297604L;

		public MiscellaneousPanel(final Model model) {

			this.add(new BooleanConfig("dualscreen",
					"Enable dual-screen mode? (Requires restart)"), gc);
			gc.gridy++;
			this.add(new BooleanConfig("geneticCodeSelection",
					"Enable genetic code selection? (Requires restart"), gc);
			gc.gridy++;
			// this.add(new BooleanConfig("logToFile",
			// "Log console output to a file (Requires restart)"), gc);
			JButton resetButton = new JButton("Reset configuration");
			resetButton.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent e) {
					Configuration.reset(model);

				}

			});
			// gc.gridy++;
			this.add(resetButton, gc);

		}
	}

	public ConfigurationDialog(Model model) {
		super((JFrame) model.getParent(), "Configuration panel");
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
		setLocationRelativeTo(model.getParent());
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

		jtp.add("Short reads",new ShortReadConfigPanel());
		
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

	}
}
