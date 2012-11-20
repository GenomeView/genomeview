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
import java.util.Collection;
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
import javax.swing.JSeparator;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import net.miginfocom.swing.MigLayout;
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

	private String file(Model model) {
		JFileChooser chooser = new JFileChooser(Configuration.getFile("lastDirectory"));
		int returnVal = chooser.showSaveDialog(model.getGUIManager().getParent());
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			File files = chooser.getSelectedFile();
			return files.toString();
		} else {
			return null;
		}
	}

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
				TCheckBox dsb = new TCheckBox(t);
				dsb.setSelected(true);

				dss.add(dsb);
				add(dsb);

			}
		}

		public Collection<T> selectedItems() {
			ArrayList<T> out = new ArrayList<T>();
			for (TCheckBox item : dss) {
				if (item.isSelected())
					out.add(item.data);
			}
			return out;
		}
	}

	private void addSeparator(String text) {

		add(new JLabel(text), "gapbottom 1, span, split 2, aligny center");
		add(new JSeparator(), "gapleft rel, growx");
	}

	public SaveDialog(final Model model) {
		super(model.getGUIManager().getParent(), "Save dialog", true);
		setLayout(new MigLayout("wrap 2"));

		/*
		 * Save location
		 */
		addSeparator("Location to save to");
		final JTextField locationField = new JTextField();
		add(locationField, "growx");

		JButton browseButton = new JButton("Browse...");
		browseButton.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				String f=file(model);
				if(f!=null)
					locationField.setText(f);
				
			}
		});
		
		add(browseButton);

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
		addSeparator("File format options");
		Parser defaultParser = Configuration.getParser("save:defaultParser");
		Parser[] arr = new Parser[] { Parser.GFF3, Parser.EMBL };

		final JComboBox parserList = new JComboBox(arr);
		if (defaultParser != null) {
			parserList.setSelectedItem(defaultParser);
			parserList.setEnabled(false);
		}
		add(parserList);

		/*
		 * Include sequence
		 */
		final boolean enableIncludeSequenceFlag = Configuration.getBoolean("save:enableIncludeSequence");
		final JCheckBox includeSequence = new JCheckBox("Include sequence");
		includeSequence.setEnabled(enableIncludeSequenceFlag);
		add(includeSequence);

		/* Entries list */
		addSeparator("Select entries to save");
		final MultiSelectionArray<Entry> entriesList = new MultiSelectionArray<Entry>(model.entries());
		add(new JScrollPane(entriesList), "growx,growy,span 1 2");

		JButton selectAllEntries = new JButton("Select all entries");
		add(selectAllEntries);

		JButton selectNoneEntries = new JButton("Deselect all entries");
		add(selectNoneEntries);
		/*
		 * Type selection
		 */
		addSeparator("Annotation types to save");
		final MultiSelectionArray<Type> typesList = new MultiSelectionArray<Type>(Arrays.asList(Type.values()));
		add(new JScrollPane(typesList), "growx,growy,span 1 2");

		JButton selectAllTypes = new JButton("Select all types");
		add(selectAllTypes);

		JButton selectNoneTypes = new JButton("Deselect all types");
		add(selectNoneTypes);

		/*
		 * Actions
		 */
		addSeparator("");
		JButton save = new JButton("Save");
		JButton close = new JButton("Cancel");

		add(save, "center");
		add(close, "center");

		save.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				model.messageModel().setStatusBarMessage("Saving data, you will be notified when complete...");
				EventQueue.invokeLater(new Runnable() {
					@Override
					public void run() {
						try {
						
							Collection<Type> selectedTypes = Arrays.asList(Type.values());
							if(typesList.selectedItems().size()>0)
								selectedTypes = typesList.selectedItems();
							
							Parser parser = (Parser) parserList.getSelectedItem();
							if (parser instanceof EMBLParser){
								((EMBLParser) parser).storeSequence = false;
								if(enableIncludeSequenceFlag && includeSequence.isSelected())
									((EMBLParser) parser).storeSequence = true;
								
							}
							File tmp = File.createTempFile("GV_", ".save");
							tmp.deleteOnExit();

							FileOutputStream fos = new FileOutputStream(tmp);

							for (Entry e : entriesList.selectedItems()) {
								parser.write(fos, e, selectedTypes.toArray(new Type[0]));
							}
							fos.close();
							setVisible(false);
							String location=locationField.getText().trim();
							if (location.startsWith("http://") || location.startsWith("https://")) {
								try {
									URL url = URIFactory.url(location);
									System.out.println(url.getProtocol() + "://" + url.getHost() + ":" + url.getPort() + url.getPath());
									url = URIFactory.url(url.getProtocol() + "://" + url.getHost() + ":" + url.getPort() + url.getPath());

									log.info("File size and location: " + tmp.length() + "\t" + tmp.getCanonicalPath());

									String reply = ClientHttpUpload.upload(tmp, url);
								
									if (reply.equals("")) {
										throw new SaveFailedException("Empty reply from server");
									}

									if (reply.toLowerCase().contains("error")) {
										showServerMessage(reply);
										throw new SaveFailedException("Error reply from server");

									}
									showServerMessage(reply);

								} catch (IOException ex) {

									ex.printStackTrace();
									throw new SaveFailedException("IOException");
								}
							} else {
								File out = new File(location);
								if (parser instanceof GFF3Parser)
									out = ExtensionManager.extension(out, "gff");

								if (parser instanceof EMBLParser)
									out = ExtensionManager.extension(out, "embl");

								boolean succes = tmp.renameTo(out);
								if (!succes) {
									JOptionPane.showMessageDialog(model.getGUIManager().getParent(), "Save failed!");
								} else {
									JOptionPane.showMessageDialog(model.getGUIManager().getParent(), "Save succeeded!");
								}
							}

						} catch (Exception ex) {
							ex.printStackTrace();
						}
//						h.dispose();
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

}
