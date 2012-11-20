/**
 * %HEADER%
 */
package net.sf.genomeview.gui.dialog;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.logging.Logger;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import net.sf.genomeview.core.Configuration;
import net.sf.genomeview.data.ClientHttpUpload;
import net.sf.genomeview.data.Model;
import net.sf.genomeview.gui.StaticUtils;
import net.sf.genomeview.gui.components.JEditorPaneLabel;
import net.sf.jannot.Entry;
import net.sf.jannot.Type;
import net.sf.jannot.exception.SaveFailedException;
import net.sf.jannot.parser.EMBLParser;
import net.sf.jannot.parser.GFF3Parser;
import net.sf.jannot.parser.Parser;
import be.abeel.io.ExtensionManager;
import be.abeel.io.LineIterator;
import be.abeel.net.URIFactory;

/**
 * 
 * @author Thomas Abeel
 * 
 */
public class SaveDialog extends JDialog {

	private static final Logger log = Logger.getLogger(SaveDialog.class.getCanonicalName());

	private static final long serialVersionUID = -5209291628487502687L;

	// private class DataSourceCheckbox extends JCheckBox {
	//
	// private static final long serialVersionUID = -208816638301437642L;
	//
	// private Entry data;
	//
	// public DataSourceCheckbox(Entry e) {
	// super(e.toString());
	// this.data = e;
	// }
	//
	// }

	class MultiSelectionArray<T> extends Container {

		class TCheckBox extends JCheckBox {
			private T data;

			public TCheckBox(T e) {
				super(e.toString());
				this.data = e;
			}
		}

		private final ArrayList<TCheckBox> dss = new ArrayList<TCheckBox>();

		public MultiSelectionArray(Iterable<T> arr) {

			setLayout(new GridLayout(0, 1));
			for (T t : arr) {

				// int count = 0;
				TCheckBox dsb = new TCheckBox(t);
				dsb.setSelected(true);

				dss.add(dsb);
				add(dsb);

			}
		}

		@SuppressWarnings("unchecked")
		public T[] selectedItems() {
			ArrayList<T>out=new ArrayList<T>();
			for(TCheckBox item:dss){
				if(item.isSelected())
					out.add(item.data);
			}
			return (T[]) out.toArray();
		}
	}

	public SaveDialog(final Model model) {
		super(model.getGUIManager().getParent(), "Save dialog", true);
		setLayout(new GridBagLayout());
		final JDialog _self = this;
		GridBagConstraints gc = new GridBagConstraints();
		gc.insets = new Insets(3, 3, 3, 3);
		gc.gridwidth = 1;
		gc.gridheight = 1;
		gc.gridx = 0;
		gc.gridy = 0;
		gc.weightx = 1;
		gc.weighty=0;
		gc.fill = GridBagConstraints.BOTH;
		/*
		 * Save location
		 */
		
		
		JTextField locationField = new JTextField();
		add(locationField, gc);

		JButton browseButton = new JButton("Browse...");
		gc.gridx++;
		gc.weightx = 0;
		add(browseButton, gc);

		/*
		 * Handle default location
		 */
		String defaultLocation = Configuration.get("save:defaultLocation");
		if (!defaultLocation.equals("null")) {
			locationField.setText(defaultLocation);
			locationField.setEditable(false);
			locationField.setEnabled(false);
			browseButton.setEnabled(false);
		}

		/*
		 * Parser selection
		 */
		Parser defaultParser = Configuration.getParser("save:defaultParser");
		Parser[] arr = new Parser[] { new GFF3Parser(), new EMBLParser() };
		

		final JComboBox parserList = new JComboBox(arr);
		if (defaultParser != null) {
			parserList.setSelectedItem(defaultParser);
			parserList.setEnabled(false);
		}
		gc.gridy++;
		gc.gridx = 0;
		gc.weightx = 1;

		add(parserList, gc);

		/*
		 * Include sequence
		 */
		boolean includeSequenceFlag = Configuration.getBoolean("save:includeSequence");
		JCheckBox includeSequence = new JCheckBox("Include sequence");
		includeSequence.setSelected(includeSequenceFlag);
		gc.gridx++;
		add(includeSequence, gc);

		/* Entries list */
		// Container entriesList = new Container();
		final MultiSelectionArray<Entry> entriesList = new MultiSelectionArray<Entry>(model.entries());

		gc.weightx = 1;
		gc.gridx = 0;
		gc.gridy++;
		gc.gridheight = 3;

		add(new JScrollPane(entriesList), gc);

		

		JButton selectAllEntries = new JButton("Select all entries");
		gc.gridx++;
		gc.weightx = 0;
		gc.gridheight = 1;
		add(selectAllEntries, gc);

		JButton selectNoneEntries = new JButton("Deselect all entries");
		gc.gridy++;
		add(selectNoneEntries, gc);
		/*
		 * Type selection
		 */
		final MultiSelectionArray<Type> typesList = new MultiSelectionArray<Type>(Arrays.asList(Type.values()));

		gc.gridx = 0;
		gc.gridy+=2;
		gc.gridheight = 3;
		gc.weightx = 1;
		add(new JScrollPane(typesList), gc);

		JButton selectAllTypes = new JButton("Select all types");
		gc.gridx++;
		
		gc.gridheight = 1;
		gc.weightx = 0;
		add(selectAllTypes, gc);

		JButton selectNoneTypes = new JButton("Deselect all types");
		gc.gridy++;
		add(selectNoneTypes, gc);

		gc.gridx = 0;
		gc.gridy++;
		JButton save = new JButton("Save");
		JButton close = new JButton("Cancel");

		gc.gridwidth = 1;
		gc.gridy++;
		add(save, gc);
		gc.gridx++;
		add(close, gc);

		save.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				final Hider h = new Hider(model, "Saving data...");

				EventQueue.invokeLater(new Runnable() {
					@Override
					public void run() {
						try {
							/* Default save locations? */
							String defaultLocation = Configuration.get("save:defaultLocation");
							net.sf.jannot.Type[] selectedTypes = net.sf.jannot.Type.values();
							if (defaultLocation.equals("null")) {
								selectedTypes = typesList.selectedItems();

								if (selectedTypes.length > 0)
									defaultLocation = file();

							}
							// if (defaultLocation == null ||
							// selectedTypes.length == 0) {
							// h.dispose();
							// return;
							// }
							// Parser parser =
							// Configuration.getParser("save:defaultParser");
							// if (parser == null) {
							//
							// Parser[] arr = new Parser[] { new GFF3Parser(),
							// new EMBLParser() };
							// parser = (Parser)
							// JOptionPane.showInputDialog(model.getGUIManager().getParent(),
							// "Select an output format", "Output format",
							// JOptionPane.QUESTION_MESSAGE, null,
							// arr, arr[0]);
							//
							// }
							//
							// if (parser == null) {
							// h.dispose();
							// return;
							// }

							Parser parser = (Parser) parserList.getSelectedItem();
							if (parser instanceof EMBLParser)
								((EMBLParser) parser).storeSequence = false;
							File tmp = File.createTempFile("GV_", ".save");
							tmp.deleteOnExit();

							FileOutputStream fos = new FileOutputStream(tmp);

							for (Entry e: entriesList.selectedItems()){
//							for (DataSourceCheckbox dsb : dss) {
//								if (dsb.isSelected()) {
									parser.write(fos, e, selectedTypes);

//								}
							}
							fos.close();
							setVisible(false);
							if (defaultLocation.startsWith("http://") || defaultLocation.startsWith("https://")) {
								try {
									URL url = URIFactory.url(defaultLocation);
									System.out.println(url.getProtocol() + "://" + url.getHost() + ":" + url.getPort() + url.getPath());
									url = URIFactory.url(url.getProtocol() + "://" + url.getHost() + ":" + url.getPort() + url.getPath());

									// LineIterator it = new LineIterator(tmp);
									// System.out.println("-------");
									// System.out.println("Uploaded file:");
									// for (String line : it)
									// System.out.println(line);
									// System.out.println("---EOF---");
									// it.close();
									log.info("File size and location: " + tmp.length() + "\t" + tmp.getCanonicalPath());

									String reply = ClientHttpUpload.upload(tmp, url);
									log.info("SERVER REPLY: " + reply);
									// TODO add more checks on the reply.
									h.dispose();
									if (reply.equals("")) {
										throw new SaveFailedException("Empty reply from server");
									}

									if (reply.toLowerCase().contains("error")) {
										showServerMessage(reply);
										throw new SaveFailedException("Error reply from server");

									}
									showServerMessage(reply);
									// JOptionPane.showMessageDialog(model.getGUIManager().getParent(),
									// reply);

								} catch (IOException ex) {

									ex.printStackTrace();
									throw new SaveFailedException("IOException");
								}
							} else {
								File out = new File(defaultLocation);
								if (parser instanceof GFF3Parser)
									out = ExtensionManager.extension(out, "gff");

								if (parser instanceof EMBLParser)
									out = ExtensionManager.extension(out, "embl");

								boolean succes = tmp.renameTo(out);
								h.dispose();
								if (!succes) {
									JOptionPane.showMessageDialog(model.getGUIManager().getParent(), "Save failed!");
								} else {
									JOptionPane.showMessageDialog(model.getGUIManager().getParent(), "Save succeeded!");
								}
							}

						} catch (Exception ex) {
							ex.printStackTrace();
						}
						h.dispose();
					}

					private void showServerMessage(String reply) {
						final JDialog diag = new JDialog(model.getGUIManager().getParent());
						JEditorPaneLabel txt = new JEditorPaneLabel();
						txt.setEditable(false);
						txt.setText(reply);
						txt.setPreferredSize(new Dimension(300, 200));
						diag.setTitle("Server reply");
						diag.getContentPane().setLayout(new BorderLayout());
						diag.getContentPane().add(new JScrollPane(txt), BorderLayout.CENTER);
						diag.getContentPane().add(new JButton(new AbstractAction("OK") {

							@Override
							public void actionPerformed(ActionEvent e) {
								diag.dispose();
							}

						}), BorderLayout.SOUTH);
						diag.pack();
						StaticUtils.center(model.getGUIManager().getParent(), diag);
						diag.setVisible(true);

					}

				});
			}

			private String file() {
				JFileChooser chooser = new JFileChooser(Configuration.getFile("lastDirectory"));
				int returnVal = chooser.showSaveDialog(model.getGUIManager().getParent());
				if (returnVal == JFileChooser.APPROVE_OPTION) {
					File files = chooser.getSelectedFile();
					return files.toString();
				} else {
					return null;
				}
			}

		});

		close.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				setVisible(false);

			}

		});

		pack();
		StaticUtils.center(model.getGUIManager().getParent(), this);
		setVisible(true);
	}

	// public static void display(Model model, boolean useDefault) {
	// new SaveDialog(model, useDefault);
	//
	// }
	private class TypeCheckbox extends JCheckBox {

		private static final long serialVersionUID = -208816638301437642L;

		private Type data;

		public TypeCheckbox(Type e) {
			super(e.toString());
			this.data = e;
		}

	}
}

/*
 * class TypeSelection {
 * 
 * public Type[] ask(JDialog owner) { final JDialog diag = new JDialog(owner,
 * "Select types to save", true); Container content = diag.getContentPane();
 * 
 * content.setLayout(new GridBagLayout());
 * 
 * GridBagConstraints gc = new GridBagConstraints(); gc.insets = new Insets(3,
 * 3, 3, 3); gc.gridwidth = 2; gc.gridheight = 1; gc.gridx = 0; gc.gridy = 0;
 * gc.weightx = 1; gc.weighty = 1; gc.fill = GridBagConstraints.BOTH;
 * 
 * content.add(new JLabel("Select sources to save"), gc);
 * 
 * gc.gridy++; Container cp = new Container(); cp.setLayout(new GridLayout(0,
 * 1)); for (Type t : Type.values()) {
 * 
 * // int count = 0; TypeCheckbox dsb = new TypeCheckbox(t);
 * dsb.setSelected(true);
 * 
 * dss.add(dsb); cp.add(dsb);
 * 
 * } content.add(new JScrollPane(cp), gc); gc.gridy++; JButton save = new
 * JButton(new AbstractAction("Save") {
 * 
 * @Override public void actionPerformed(ActionEvent e) {
 * diag.setVisible(false);
 * 
 * }
 * 
 * }); JButton cancel = new JButton(new AbstractAction("Cancel") {
 * 
 * @Override public void actionPerformed(ActionEvent e) { dss.clear();
 * diag.setVisible(false);
 * 
 * }
 * 
 * });
 * 
 * gc.gridwidth = 1; gc.gridy++; content.add(save, gc);
 * 
 * gc.gridx++; content.add(cancel, gc);
 * 
 * diag.pack(); StaticUtils.center(owner, diag); diag.setVisible(true);
 * diag.dispose();
 * 
 * ArrayList<Type> sTypes = new ArrayList<Type>(); for (TypeCheckbox tc : dss) {
 * if (tc.isSelected()) sTypes.add(tc.data); } return sTypes.toArray(new
 * Type[0]); }
 * 
 * }
 */
