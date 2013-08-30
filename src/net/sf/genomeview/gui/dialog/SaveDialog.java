/**
 * %HEADER%
 */
package net.sf.genomeview.gui.dialog;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

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
import javax.swing.JTextField;

import net.miginfocom.swing.MigLayout;
import net.sf.genomeview.core.Configuration;
import net.sf.genomeview.data.ClientHttpUpload;
import net.sf.genomeview.data.Model;
import net.sf.genomeview.gui.MessageManager;
import net.sf.genomeview.gui.StaticUtils;
import net.sf.genomeview.gui.components.JEditorPaneLabel;
import net.sf.jannot.Entry;
import net.sf.jannot.exception.SaveFailedException;
import net.sf.jannot.parser.EMBLParser;
import net.sf.jannot.parser.GFF3Parser;
import net.sf.jannot.parser.Parser;

import org.apache.commons.io.FileExistsException;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import be.abeel.io.ExtensionManager;
import be.abeel.net.URIFactory;

/**
 * 
 * @author Thomas Abeel
 * 
 */
public class SaveDialog extends JDialog {

	private static final Logger log = LoggerFactory.getLogger(SaveDialog.class.getCanonicalName());

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

	private class MultiSelectionArray<T> extends Container {

		private static final long serialVersionUID = -5487911457275295620L;

		private class TCheckBox extends JCheckBox {
			
			private static final long serialVersionUID = -5606344815979542381L;
			
			private T data;

			public TCheckBox(T e) {
				super(e.toString());
				this.data = e;
			}
		}

		private final ArrayList<TCheckBox> dss = new ArrayList<TCheckBox>();

		private MultiSelectionArray(Iterable<T> arr, boolean enabledFlag) {

			setLayout(new GridLayout(0, 1));
			for (T t : arr) {
				TCheckBox dsb = new TCheckBox(t);
				dsb.setEnabled(enabledFlag);
				dsb.setSelected(true);

				dss.add(dsb);
				add(dsb);

			}
		}

		private Collection<T> selectedItems() {
			ArrayList<T> out = new ArrayList<T>();
			for (TCheckBox item : dss) {
				if (item.isSelected())
					out.add(item.data);
			}
			return out;
		}
		
		public void selectAllItems(boolean select){
			for (TCheckBox item : dss){
				item.setSelected(select);
			}
		}
	}

	private void addSeparator(String text) {

		add(new JLabel(text), "gapbottom 1, span, split 2, aligny center");
		add(new JSeparator(), "gapleft rel, growx");
	}

	public SaveDialog(final Model model) {
		super(model.getGUIManager().getParent(), MessageManager.getString("savedialog.title"), true);
		setLayout(new MigLayout("wrap 2"));

		/*
		 * Save location
		 */
		addSeparator(MessageManager.getString("savedialog.location_to_save_to"));
		final JTextField locationField = new JTextField();
		add(locationField, "growx");

		JButton browseButton = new JButton(MessageManager.getString("savedialog.browse"));
		browseButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				String f = file(model);
				if (f != null)
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
		addSeparator(MessageManager.getString("savedialog.file_format_options"));
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
		
		Parser p = (Parser)parserList.getSelectedItem();
		if (p instanceof GFF3Parser){
			includeSequence.setEnabled(false);					
		}
		parserList.addActionListener(new ActionListener() {	
			@Override
			public void actionPerformed(ActionEvent e) {
				Parser p = (Parser)parserList.getSelectedItem();
				if (p instanceof GFF3Parser){
					includeSequence.setEnabled(false);					
				} else {
					includeSequence.setEnabled(true);					
				}
			}
		});

		/* Entries list */
		addSeparator(MessageManager.getString("savedialog.select_entries_to_save"));
		boolean entrySelectionEnabledFlag = Configuration.getBoolean("save:enableEntrySelection");
		final MultiSelectionArray<Entry> entriesList = new MultiSelectionArray<Entry>(model.entries(),entrySelectionEnabledFlag);
		add(new JScrollPane(entriesList), "growx,growy,span 1 2");

		JButton selectAllEntries = new JButton(MessageManager.getString("savedialog.select_all_entries"));
		add(selectAllEntries);

		
		selectAllEntries.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e) {
				entriesList.selectAllItems(true);				
			}
			
		});
		
		JButton selectNoneEntries = new JButton(MessageManager.getString("savedialog.deselect_all_entries"));
		add(selectNoneEntries);
		selectNoneEntries.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				entriesList.selectAllItems(false);
				
			}
		});
	
		entriesList.setEnabled(entrySelectionEnabledFlag);
		selectAllEntries.setEnabled(entrySelectionEnabledFlag);
		selectNoneEntries.setEnabled(entrySelectionEnabledFlag);
		/*
		 * Type selection
		 */
		addSeparator(MessageManager.getString("savedialog.annotation_types"));
		boolean typeSelectionEnabledFlag = Configuration.getBoolean("save:enableTypeSelection");
		final MultiSelectionArray<net.sf.jannot.Type> typesList = new MultiSelectionArray<net.sf.jannot.Type>(Arrays.asList(net.sf.jannot.Type.values()),typeSelectionEnabledFlag);
		add(new JScrollPane(typesList), "growx,growy,span 1 2");

		JButton selectAllTypes = new JButton(MessageManager.getString("savedialog.select_all_types"));
		add(selectAllTypes);

		JButton selectNoneTypes = new JButton(MessageManager.getString("savedialog.deselect_all_types"));
		add(selectNoneTypes);

		
		typesList.setEnabled(typeSelectionEnabledFlag);
		selectAllTypes.setEnabled(typeSelectionEnabledFlag);
		selectNoneTypes.setEnabled(typeSelectionEnabledFlag);
		
		
		selectAllTypes.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				typesList.selectAllItems(true);
				
			}
		});
		selectNoneTypes.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				typesList.selectAllItems(false);
				
			}
		});
		
		/*
		 * Actions
		 */
		addSeparator("");
		JButton save = new JButton(MessageManager.getString("button.save"));
		JButton close = new JButton(MessageManager.getString("button.cancel"));

		add(save, "center");
		add(close, "center");

		save.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				model.messageModel().setStatusBarMessage(MessageManager.getString("savedialog.saving_data"));
				EventQueue.invokeLater(new Runnable() {
					@Override
					public void run() {
						try {

							Collection<net.sf.jannot.Type> selectedTypes = Arrays.asList(net.sf.jannot.Type.values());
							if (typesList.selectedItems().size() > 0)
								selectedTypes = typesList.selectedItems();

							Parser parser = (Parser) parserList.getSelectedItem();
							if (parser instanceof EMBLParser) {
								((EMBLParser) parser).storeSequence = false;
								if (enableIncludeSequenceFlag && includeSequence.isSelected())
									((EMBLParser) parser).storeSequence = true;

							}
							File tmp = File.createTempFile("GV_", ".save");
							tmp.deleteOnExit();

							FileOutputStream fos = new FileOutputStream(tmp);

							for (Entry e : entriesList.selectedItems()) {
								System.out.println(selectedTypes);
								parser.write(fos, e, selectedTypes.toArray(new net.sf.jannot.Type[0]));
							}
							fos.close();
							setVisible(false);
							String location = locationField.getText().trim();
							if (location.startsWith("http://") || location.startsWith("https://")) {
								try {
									URL url = URIFactory.url(location);
									System.out.println(url.getProtocol() + "://" + url.getHost() + ":" + url.getPort() + url.getPath());
									url = URIFactory.url(url.getProtocol() + "://" + url.getHost() + ":" + url.getPort() + url.getPath());

									log.info("File size and location: " + tmp.length() + "\t" + tmp.getCanonicalPath());

									String reply = ClientHttpUpload.upload(tmp, url);

									if (reply.equals("")) {
										throw new SaveFailedException(MessageManager.getString("savedialog.empty_reply_server"));
									}

									if (reply.toLowerCase().contains("error")) {
										showServerMessage(reply);
										throw new SaveFailedException(MessageManager.getString("savedialog.empty_reply_server"));

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

								boolean tryToSave = true;
								while (tryToSave){
									try {
										FileUtils.moveFile(tmp, out);
										JOptionPane.showMessageDialog(model.getGUIManager().getParent(), MessageManager.getString("savedialog.save_succeeded"));
										tryToSave = false;
									} catch (FileExistsException fee){
										int answer = JOptionPane.showOptionDialog(model.getGUIManager().getParent(),
												MessageManager.getString("savedialog.file_exists"),
												MessageManager.getString("savedialog.file_exists_title"),
												JOptionPane.YES_NO_OPTION, 
												JOptionPane.QUESTION_MESSAGE,
												null, 
												null, null);
										if (answer==JOptionPane.YES_OPTION){
											out.delete();
										} else {
											tryToSave = false;
										}
									} catch (IOException e) {
										JOptionPane.showMessageDialog(model.getGUIManager().getParent(), MessageManager.getString("savedialog.save_failed"));
										tryToSave = false;
										e.printStackTrace();
									}
								}
							}

						} catch (Exception ex) {
							ex.printStackTrace();
						} finally {
							model.messageModel().setStatusBarMessage(null);
						}
						// h.dispose();
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
						diag.getContentPane().add(new JButton(new AbstractAction(MessageManager.getString("button.ok")) {

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
