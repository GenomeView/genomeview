/**
 * %HEADER%
 */
package net.sf.genomeview.gui.dialog;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.table.AbstractTableModel;

import net.sf.genomeview.data.Model;
import net.sf.genomeview.gui.StaticUtils;
import net.sf.genomeview.gui.components.TypeCombo;
import net.sf.jannot.AminoAcidMapping;
import net.sf.jannot.Entry;
import net.sf.jannot.Feature;
import net.sf.jannot.Location;
import net.sf.jannot.Qualifier;
import net.sf.jannot.Strand;
import net.sf.jannot.Type;
import net.sf.jannot.utils.SequenceTools;
import be.abeel.gui.GridBagPanel;
import be.abeel.gui.TitledComponent;

import com.eaio.stringsearch.BoyerMooreHorspoolRaita;
import com.eaio.stringsearch.ShiftOrMismatches;

public class SearchDialog extends JDialog {

	/**
     * 
     */
	private static final long serialVersionUID = 6844861145552724990L;

	private static SearchDialog dialog = null;

	private SearchDialog(Model model) {

		super(model.getParent(), "Search");
		setContentPane(new SearchDialogContent(model));
		pack();
		StaticUtils.right(this, model.getParent());

	}

	public static void showDialog(Model model) {
		if (dialog == null)
			dialog = new SearchDialog(model);
		dialog.setVisible(true);
		focusField.requestFocusInWindow();
	}

	private static JTextArea focusField;

	class SearchDialogContent extends JPanel {

		/**
         * 
         */
		private static final long serialVersionUID = 431569354571751771L;

		public SearchDialogContent(Model model) {

			JTabbedPane pane = new JTabbedPane();
			setLayout(new BorderLayout());
			add(pane, BorderLayout.CENTER);
			pane.add("Sequence search", new SearchSequencePane(model));
			pane.add("Keyword search", new SearchKeywordPane(model));
			pane.add("Overlap search", new FeatureOverlapSearchPane(model));

		}

	}

	class SearchResultModel extends AbstractTableModel {
		class StrandedLocation {
			public StrandedLocation(Strand s, Location l) {
				this.l = l;
				this.s = s;
			}

			Location l;

			Strand s;
		}

		/**
         * 
         */
		private static final long serialVersionUID = 5974192617901951628L;

		private ArrayList<StrandedLocation> locations = new ArrayList<StrandedLocation>();

		@Override
		public Class<?> getColumnClass(int columnIndex) {
			return String.class;
		}

		@Override
		public int getColumnCount() {
			return 3;
		}

		@Override
		public String getColumnName(int columnIndex) {
			switch (columnIndex) {
			case 0:
				return "Strand";
			case 1:
				return "Start";
			case 2:
				return "End";
			}
			return null;
		}

		@Override
		public int getRowCount() {
			return locations.size();
		}

		@Override
		public Object getValueAt(int rowIndex, int columnIndex) {
			switch (columnIndex) {
			case 0:
				return locations.get(rowIndex).s;
			case 1:
				return locations.get(rowIndex).l.start();
			case 2:
				return locations.get(rowIndex).l.end();
			}
			return null;
		}

		@Override
		public boolean isCellEditable(int rowIndex, int columnIndex) {
			return false;
		}

		/**
		 * Search this particular sequence and populate the model with the
		 * results.
		 * 
		 * @param model
		 * @param mismatch
		 * @param text
		 */
		public void search(final Model model, String pattern, final int mismatch, final SequenceType type) {
			locations.clear();
			model.clearHighlights();
			final byte[] bytePattern = pattern.toUpperCase().getBytes();

			new Thread(new Runnable() {

				@Override
				public void run() {
					try {
						switch (type) {
						case Nucleotide:
							performNucleotideSearch();
							break;
						case AminoAcid:
							performAminoAcidSearch();
						}
					} catch (IllegalArgumentException ie) {
						JOptionPane.showMessageDialog(model.getParent(), "<html>Mismatch search has some limitations:<br/>" + "<table><tr><td>Editing distance</td><td>maximum pattern length</td></tr><tr><td>"
								+ "1</td><td>15</td></tr><tr><td>" + "2-3</td><td>10</td></tr><tr><td>" + "4-5</td><td>7</td></tr></table></html>", "Too many mismatches", JOptionPane.WARNING_MESSAGE);
					}

				}

				private void performAminoAcidSearch() {
					byte[] byteSequence = model.getSelectedEntry().sequence.getSequence().toUpperCase().getBytes();
					byte[] translation = translate(byteSequence, 0);
					forwardSearch(translation, 0);
					translation = translate(byteSequence, 1);
					forwardSearch(translation, 1);
					translation = translate(byteSequence, 2);
					forwardSearch(translation, 2);

					reverseArray(byteSequence);
					for (int i = 0; i < byteSequence.length; i++) {
						byteSequence[i] = (byte) SequenceTools.complement((char) byteSequence[i]);
					}

					translation = translate(byteSequence, 0);
					reverseSearch(translation, 0);
					translation = translate(byteSequence, 1);
					reverseSearch(translation, 1);
					translation = translate(byteSequence, 2);
					reverseSearch(translation, 2);
				}

				private byte[] translate(byte[] seq, int offset) {
					byte[] out = new byte[seq.length / 3];
					for (int i = 0; i < out.length && 3 * i + offset + 2 < seq.length; i++) {
						AminoAcidMapping aamap = model.getAAMapping(model.getSelectedEntry());
						out[i] = (byte) aamap.get("" + (char) seq[3 * i + offset] + (char) seq[3 * i + offset + 1] + (char) seq[3 * i + offset + 2]);
						// System.out.println((char)out[i]);
					}
					return out;
				}

				private void forwardSearch(byte[] byteSequence, int offset) {

					BoyerMooreHorspoolRaita bm = new BoyerMooreHorspoolRaita();

					ShiftOrMismatches som = new ShiftOrMismatches();

					/* Search forward strand */
					int[] lastPos = { 0, 0 };
					do {
						if (mismatch == 0)
							lastPos[0] = bm.searchBytes(byteSequence, lastPos[0], byteSequence.length, bytePattern);
						else
							lastPos = som.searchBytes(byteSequence, lastPos[0], byteSequence.length, bytePattern, mismatch);

						if (lastPos[0] >= 0) {

							lastPos[0]++;
							Location l = null;
							switch (type) {
							case Nucleotide:
								l = new Location(lastPos[0], lastPos[0] + bytePattern.length);
								break;
							case AminoAcid:
								l = new Location(lastPos[0] * 3 + offset - 2, lastPos[0] * 3 + bytePattern.length * 3 + offset - 2);
								break;
							}

							locations.add(new StrandedLocation(Strand.FORWARD, l));
							model.addHighlight(l, Color.cyan, Strand.FORWARD);
							fireTableDataChanged();

						}

					} while (lastPos[0] >= 0);
				}

				private void performNucleotideSearch() {
					byte[] byteSequence = model.getSelectedEntry().sequence.getSequence().toUpperCase().getBytes();
					forwardSearch(byteSequence, 0);
					reverseArray(byteSequence);
					for (int i = 0; i < byteSequence.length; i++) {
						byteSequence[i] = (byte) SequenceTools.complement((char) byteSequence[i]);
					}
					reverseSearch(byteSequence, 0);

				}

				private void reverseArray(byte[] array) {
					if (array == null) {
						return;
					}
					int i = 0;
					int j = array.length - 1;
					byte tmp;
					while (j > i) {
						tmp = array[j];
						array[j] = array[i];
						array[i] = tmp;
						j--;
						i++;
					}
				}

				private void reverseSearch(byte[] byteSequence, int offset) {
					BoyerMooreHorspoolRaita bm = new BoyerMooreHorspoolRaita();

					ShiftOrMismatches som = new ShiftOrMismatches();
					/* Search reverse strand */
					int lastPos[] = { 0, 0 };
					do {
						if (mismatch == 0)
							lastPos[0] = bm.searchBytes(byteSequence, lastPos[0], byteSequence.length, bytePattern);
						else
							lastPos = som.searchBytes(byteSequence, lastPos[0], byteSequence.length, bytePattern, mismatch);

						if (lastPos[0] >= 0) {
							Location l = null;
							switch (type) {
							case Nucleotide:
								l = new Location(byteSequence.length - lastPos[0] + 1, byteSequence.length - (lastPos[0] + bytePattern.length) + 1);
								break;
							case AminoAcid:
								l = new Location(byteSequence.length * 3 - lastPos[0] * 3 + 2 - offset, byteSequence.length * 3 - (lastPos[0] * 3 + bytePattern.length * 3) + 2 - offset);
								break;

							}

							locations.add(new StrandedLocation(Strand.REVERSE, l));
							model.addHighlight(l, Color.magenta, Strand.REVERSE);
							lastPos[0]++;
							fireTableDataChanged();

						}

					} while (lastPos[0] >= 0);

				}

			}).start();
		}

		public Location getLocation(int index) {
			return locations.get(index).l;
		}

		public void clear() {
			locations.clear();
			fireTableDataChanged();

		}
	}

	enum SequenceType {
		Nucleotide, AminoAcid;
		@Override
		public String toString() {
			switch (this) {
			case Nucleotide:
				return "Nucleotide";
			case AminoAcid:
				return "Amino acid";
			}
			return null;
		}
	}

	class FeatureOverlapSearchPane extends GridBagPanel {
		public FeatureOverlapSearchPane(final Model model) {
			gc.weightx=1;
			gc.weighty=0;
			gc.fill=GridBagConstraints.BOTH;
			final JTextArea seq = new JTextArea(7, 30);
			focusField = seq;
			final TypeCombo sourceType = new TypeCombo(model);
			final TypeCombo targetType = new TypeCombo(model);
			final OverlapSearchResultModel srm = new OverlapSearchResultModel(model);
			final JTable results = new JTable(srm);
			results.addMouseListener(new MouseAdapter() {
				@Override
				public void mouseClicked(MouseEvent e) {
					int row = results.getSelectedRow();
					Feature f = srm.getFeature(row);
					model.setLocationSelection(f);
					double border = 0.05 * (f.end() - f.start());
					model.setAnnotationLocationVisible(new Location((int) (f.start() - border), (int) (f.end() + border)));

				}
			});
			JButton searchButton = new JButton("Search");
			searchButton.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent e) {
					srm.search(sourceType.getTerm(), targetType.getTerm());

				}

			});
			JButton clearButton = new JButton("Clear results");
			clearButton.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent e) {
					srm.clear();

				}

			});
			gc.gridwidth = 2;
			add(new TitledComponent("Overlap between type ", sourceType), gc);
			gc.gridy++;
			add(new TitledComponent("and type", targetType), gc);
			gc.gridy++;
			gc.gridwidth = 1;
			add(searchButton, gc);
			gc.gridx++;
			add(clearButton, gc);
			gc.gridx = 0;
			gc.gridwidth = 2;

			gc.gridy++;
			gc.weighty=1;
			add(new JScrollPane(results), gc);

		}
	}

	class SearchSequencePane extends GridBagPanel {

		/**
         * 
         */
		private static final long serialVersionUID = -3270709193426284702L;

		public SearchSequencePane(final Model model) {
			gc.fill=GridBagConstraints.BOTH;
			final JTextArea seq = new JTextArea(7, 30);
			focusField = seq;
			final JComboBox mismatch = new JComboBox();
			mismatch.addItem(0);
			mismatch.addItem(1);
			mismatch.addItem(2);
			mismatch.addItem(3);
			mismatch.addItem(4);
			mismatch.addItem(5);
			mismatch.setSelectedItem(0);
			JButton search = new JButton("Search");
			JButton clearSearch = new JButton("Clear results");

			final SearchResultModel srm = new SearchResultModel();
			final JTable results = new JTable(srm);
			results.addMouseListener(new MouseAdapter() {
				public void mouseClicked(MouseEvent e) {
					Location l = srm.getLocation(results.getSelectedRow());
					model.center((l.start() + l.end()) / 2);
				}
			});
			final JComboBox type = new JComboBox(SequenceType.values());

			search.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent e) {

					srm.search(model, seq.getText().trim(), mismatch.getSelectedIndex(), (SequenceType) type.getSelectedItem());

				}

			});

			clearSearch.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent e) {
					srm.clear();
					model.clearHighlights();

				}

			});
			/* Query sequence box */
			gc.weightx = 1;
			gc.gridwidth = 4;
			gc.weighty = 0;
			add(new TitledComponent("Query sequence", new JScrollPane(seq)), gc);

			gc.gridwidth = 1;
			gc.gridy++;
			add(type, gc);
			gc.gridx++;
			gc.weightx = 1;
			add(new TitledComponent("Mismatches allowed", mismatch), gc);
			gc.weightx = 0;
			gc.gridx++;
			add(search, gc);
			gc.gridx++;
			add(clearSearch, gc);

			/* Result table */
			gc.gridx = 0;
			gc.gridwidth = 4;
			gc.gridy++;
			gc.weighty = 1;
			gc.weightx = 1;
			add(new TitledComponent("Result locations", new JScrollPane(results)), gc);

		}

	}

	class KeywordSearchResultModel extends AbstractTableModel {

		private static final long serialVersionUID = -6980331160054705283L;
		
		private ArrayList<Feature> features = new ArrayList<Feature>();
		private ArrayList<Entry> entries = new ArrayList<Entry>();

		private Set<Feature> featuresSet = new HashSet<Feature>();

		private Model model;

		public KeywordSearchResultModel(Model model) {
			this.model = model;
		}

		private String[]columns=new String[]{"Entry","Feature"};
		@Override
		public String getColumnName(int col) {
			
			return columns[col];

		}

		@Override
		public int getColumnCount() {
			return 2;
		}

		@Override
		public int getRowCount() {
			return features.size();
		}

		@Override
		public Object getValueAt(int row, int col) {
			switch(col){
			case 0:
				return entries.get(row);
			case 1:
				return features.get(row);
			}
			return null;
			
		}
		
		public Entry getEntry(int row){
			return entries.get(row);
			
		}

		public void search(String text) {
			String lowerCaseText = text.toLowerCase();
			clear();
			for (Entry e : model.entries()) {
				for (Type t : Type.values()) {
					for (Feature f : e.annotation.getByType(t)) {
						if (!featuresSet.contains(f)) {
							for (String key : f.getQualifiersKeys()) {
								for (Qualifier q : f.qualifier(key)) {
									if (q.getKey().toLowerCase().contains(lowerCaseText) || q.getValue().toLowerCase().contains(lowerCaseText)) {
										if(!featuresSet.contains(f)){
											features.add(f);
											entries.add(e);
											featuresSet.add(f);
										}
									}

								}
							}
						}
					}
				}
			}
			
			fireTableDataChanged();

		}

		public Feature getFeature(int row) {
			return features.get(row);
		}

		public void clear() {
			features.clear();
			entries.clear();
			featuresSet.clear();
			fireTableDataChanged();
		}
	}

	class OverlapSearchResultModel extends AbstractTableModel {

		private static final long serialVersionUID = 5850270831525342543L;

		private ArrayList<Feature> features = new ArrayList<Feature>();

		private Set<Feature> featuresSet = new HashSet<Feature>();

		private Model model;

		public OverlapSearchResultModel(Model model) {
			this.model = model;
		}

		public void search(Type source, Type target) {
			for (Feature f : model.getSelectedEntry().annotation.getByType(source)) {
				for (Feature g : model.getSelectedEntry().annotation.getByType(target)) {
					if(f.overlaps(g)){
						features.add(f);
						break;
					}
					
				}
				
			}
			fireTableDataChanged();

		}

		@Override
		public String getColumnName(int col) {
			return "Feature";

		}

		@Override
		public int getColumnCount() {
			return 1;
		}

		@Override
		public int getRowCount() {
			return features.size();
		}

		@Override
		public Object getValueAt(int row, int col) {
			return features.get(row);
		}

//		public void search(String text) {
//			String lowerCaseText = text.toLowerCase();
//			clear();
//			for (Entry e : model.entries()) {
//				for (Type t : Type.values()) {
//					for (Feature f : e.annotation.getByType(t)) {
//						if (!featuresSet.contains(f)) {
//							for (String key : f.getQualifiersKeys()) {
//								for (Qualifier q : f.qualifier(key)) {
//									if (q.getKey().toLowerCase().contains(lowerCaseText) || q.getValue().toLowerCase().contains(lowerCaseText)) {
//										features.add(f);
//										featuresSet.add(f);
//									}
//
//								}
//							}
//						}
//					}
//				}
//			}
//			fireTableDataChanged();
//
//		}

		public Feature getFeature(int row) {
			return features.get(row);
		}

		public void clear() {
			features.clear();
			featuresSet.clear();
			fireTableDataChanged();
		}
	}

	class SearchKeywordPane extends GridBagPanel {

		private static final long serialVersionUID = -7531967816569386730L;

		public SearchKeywordPane(final Model model) {
			gc.weightx=1;
			gc.weighty=0;
			gc.fill=GridBagConstraints.BOTH;
			final JTextArea text = new JTextArea(5, 30);
			JButton searchButton = new JButton("Search");
			JButton clearButton = new JButton("Clear result");
			final KeywordSearchResultModel srm = new KeywordSearchResultModel(model);
			final JTable resultTable = new JTable(srm);
			resultTable.addMouseListener(new MouseAdapter() {
				@Override
				public void mouseClicked(MouseEvent e) {
					int row = resultTable.getSelectedRow();
					Feature f = srm.getFeature(row);
					Entry entry=srm.getEntry(row);
					if(model.getSelectedEntry()!=entry)
						model.setSelectedEntry(entry);
					model.setLocationSelection(f);
					double border = 0.05 * (f.end() - f.start());
					model.setAnnotationLocationVisible(new Location((int) (f.start() - border), (int) (f.end() + border)),true);

				}
			});
			searchButton.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent e) {
					srm.search(text.getText().trim());

				}

			});

			clearButton.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent e) {
					srm.clear();

				}

			});
			gc.gridwidth = 2;
			add(new TitledComponent("Keyword", text), gc);
			gc.gridy++;
			gc.gridwidth = 1;
			add(searchButton, gc);
			gc.gridx++;
			add(clearButton, gc);
			gc.gridx = 0;
			gc.gridwidth = 2;

			gc.gridy++;
			gc.weighty=1;
			add(new JScrollPane(resultTable), gc);

		}

	}

}
