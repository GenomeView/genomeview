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
import java.util.List;
import java.util.logging.Level;

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

import org.apache.commons.io.FileExistsException;
import org.apache.commons.io.FileUtils;

import be.abeel.io.ExtensionManager;
import be.abeel.net.URIFactory;
import net.miginfocom.swing.MigLayout;
import net.sf.genomeview.core.Configuration;
import net.sf.genomeview.core.MessageManager;
import net.sf.genomeview.data.ClientHttpUpload;
import net.sf.genomeview.data.Model;
import net.sf.genomeview.gui.StaticUtils;
import net.sf.genomeview.gui.components.JEditorPaneLabel;
import net.sf.jannot.Entry;
import net.sf.jannot.exception.SaveFailedException;
import net.sf.jannot.parser.EMBLParser;
import net.sf.jannot.parser.GFF3Parser;
import net.sf.jannot.parser.Parser;

/**
 * 
 * @author Thomas Abeel
 * 
 */
@SuppressWarnings("serial")
public class SaveDialog extends JDialog {

	private String file(Model model) {
		JFileChooser chooser = new JFileChooser(
				model.getConfiguration().getFile("lastDirectory"));
		int returnVal = chooser
				.showSaveDialog(model.getGUIManager().getMainWindow());
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			File files = chooser.getSelectedFile();
			return files.toString();
		} else {
			return null;
		}
	}

	private void addSeparator(String text) {

		add(new JLabel(text), "gapbottom 1, span, split 2, aligny center");
		add(new JSeparator(), "gapleft rel, growx");
	}

	public SaveDialog(final Model model) {
		super(model.getGUIManager().getMainWindow(),
				model.getMessageMgr().getString("savedialog.title"), true);
		final MessageManager mm = model.getMessageMgr();
		Configuration config = model.getConfiguration();
		setLayout(new MigLayout("wrap 2"));

		/*
		 * Save location
		 */
		addSeparator(mm.getString("savedialog.location_to_save_to"));
		final JTextField locationField = new JTextField();
		add(locationField, "growx");

		JButton browseButton = new JButton(mm.getString("savedialog.browse"));
		browseButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				String f = file(model);
				if (f != null) {
					locationField.setText(f);
				}

			}
		});

		add(browseButton);

		/*
		 * Handle default location
		 */

		String defaultLocation = config.get("save:defaultLocation");
		if (!defaultLocation.equals("null")) {
			locationField.setText(defaultLocation);
			locationField.setEditable(false);
			locationField.setEnabled(false);
			browseButton.setEnabled(false);
		}

		/*
		 * Parser selection
		 */
		addSeparator(mm.getString("savedialog.file_format_options"));
		String defaultParserName = config.get("save:defaultParser");
		// Parser defaultParser = Configuration.getParser("save:defaultParser");
		String[] arr = new String[] { "GFF3", "EMBL" };

		final JComboBox<String> parserList = new JComboBox(arr);
		if (defaultParserName != null) {
			parserList.setSelectedItem(defaultParserName);
			parserList.setEnabled(false);
		}
		add(parserList);

		/*
		 * Include sequence
		 */
		final boolean enableIncludeSequenceFlag = config
				.getBoolean("save:enableIncludeSequence");
		final JCheckBox includeSequence = new JCheckBox("Include sequence");
		includeSequence.setEnabled(enableIncludeSequenceFlag);
		add(includeSequence);

		final Parser p = "GFF3".equals(parserList.getSelectedItem())
				? new GFF3Parser(model.getGlobal())
				: new EMBLParser(model.getGlobal());
		if (p instanceof GFF3Parser) {
			includeSequence.setEnabled(false);
		}
		parserList.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				Parser p = (Parser) parserList.getSelectedItem();
				if (p instanceof GFF3Parser) {
					includeSequence.setEnabled(false);
				} else {
					includeSequence.setEnabled(true);
				}
			}
		});

		/* Entries list */
		addSeparator(mm.getString("savedialog.select_entries_to_save"));
		boolean entrySelectionEnabledFlag = config
				.getBoolean("save:enableEntrySelection");
		final MultiSelectionArray<Entry> entriesList = new MultiSelectionArray<Entry>(
				model.entries(), entrySelectionEnabledFlag);
		add(new JScrollPane(entriesList), "growx,growy,span 1 2");

		JButton selectAllEntries = new JButton(
				mm.getString("savedialog.select_all_entries"));
		add(selectAllEntries);

		selectAllEntries.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				entriesList.selectAllItems(true);
			}

		});

		JButton selectNoneEntries = new JButton(
				mm.getString("savedialog.deselect_all_entries"));
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
		addSeparator(mm.getString("savedialog.annotation_types"));
		boolean typeSelectionEnabledFlag = config
				.getBoolean("save:enableTypeSelection");
		final MultiSelectionArray<net.sf.jannot.Type> typesList = new MultiSelectionArray<net.sf.jannot.Type>(
				model.getGlobal().typeFactory().values(),
				typeSelectionEnabledFlag);
		add(new JScrollPane(typesList), "growx,growy,span 1 2");

		JButton selectAllTypes = new JButton(
				mm.getString("savedialog.select_all_types"));
		add(selectAllTypes);

		JButton selectNoneTypes = new JButton(
				mm.getString("savedialog.deselect_all_types"));
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
		JButton save = new JButton(mm.getString("button.save"));
		JButton close = new JButton(mm.getString("button.cancel"));

		add(save, "center");
		add(close, "center");

		save.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				model.messageModel().setStatusBarMessage(
						mm.getString("savedialog.saving_data"));
				EventQueue.invokeLater(new Runnable() {
					@Override
					public void run() {
						try {

							List<net.sf.jannot.Type> selectedTypes = model
									.getGlobal().typeFactory().values();
							if (typesList.selectedItems().size() > 0) {
								selectedTypes = typesList.selectedItems();
							}

							Parser parser = (Parser) parserList
									.getSelectedItem();
							if (parser instanceof EMBLParser) {
								((EMBLParser) parser).storeSequence = false;
								if (enableIncludeSequenceFlag
										&& includeSequence.isSelected()) {
									((EMBLParser) parser).storeSequence = true;
								}

							}

							File tmp = File.createTempFile("GV_", ".save");
							tmp.deleteOnExit();

							FileOutputStream fos = new FileOutputStream(tmp);

							for (Entry e : entriesList.selectedItems()) {
								// System.out.println(selectedTypes);
								parser.write(fos, e, selectedTypes);
							}
							fos.close();
							setVisible(false);
							String location = locationField.getText().trim();
							if (location.startsWith("http://")
									|| location.startsWith("https://")) {
								try {
									URL url = URIFactory.url(location);
									model.getLog().log(Level.INFO,
											url.getProtocol() + "://"
													+ url.getHost() + ":"
													+ url.getPort()
													+ url.getPath());
									url = URIFactory.url(url.getProtocol()
											+ "://" + url.getHost() + ":"
											+ url.getPort() + url.getPath());

									model.getLog().log(Level.INFO,
											"File size and location: "
													+ tmp.length() + "\t"
													+ tmp.getCanonicalPath());

									String reply = ClientHttpUpload.upload(tmp,
											url);

									if (reply.equals("")) {
										showServerMessage(mm.getString(
												"savedialog.empty_reply_server"));
//										throw new SaveFailedException();
									} else if (reply.toLowerCase()
											.contains("error")) {
										showServerMessage(reply);
//										throw new SaveFailedException(MessageManager.getString("savedialog.save_failed"));

									} else {
										showServerMessage(reply);
									}

								} catch (IOException ex) {
									throw new SaveFailedException("IOException",
											ex);
								}
							} else {
								if ((location == null)
										|| (location.trim().length() == 0)) {
									JOptionPane.showMessageDialog(
											model.getGUIManager()
													.getMainWindow(),
											mm.getString(
													"savedialog.provided_path_emtpy"),
											mm.getString(
													"savedialog.save_failed"),
											JOptionPane.ERROR_MESSAGE);
								} else {
									File out = new File(location);
									if (parser instanceof GFF3Parser) {
										out = ExtensionManager.extension(out,
												"gff");
									}

									if (parser instanceof EMBLParser) {
										out = ExtensionManager.extension(out,
												"embl");
									}

									boolean tryToSave = true;
									while (tryToSave) {
										try {
											FileUtils.moveFile(tmp, out);
											JOptionPane.showMessageDialog(
													model.getGUIManager()
															.getMainWindow(),
													mm.getString(
															"savedialog.save_succeeded"));
											tryToSave = false;
										} catch (FileExistsException fee) {
											model.getLog().log(Level.WARNING,
													mm.getString(
															"savedialog.file_exists"));
											int answer = JOptionPane
													.showOptionDialog(model
															.getGUIManager()
															.getMainWindow(),
															mm.getString(
																	"savedialog.file_exists"),
															mm.getString(
																	"savedialog.file_exists_title"),
															JOptionPane.YES_NO_OPTION,
															JOptionPane.QUESTION_MESSAGE,
															null, null, null);
											if (answer == JOptionPane.YES_OPTION) {
												out.delete();
											} else {
												tryToSave = false;
											}
										} catch (IOException e) {
											model.getLog().log(Level.SEVERE,
													mm.getString(
															"savedialog.save_failed"),
													e);
											// FIXME remove popup
											JOptionPane.showMessageDialog(
													model.getGUIManager()
															.getMainWindow(),
													mm.getString(
															"savedialog.save_failed"));
											tryToSave = false;
										}
									}
								}
							}
						} catch (Exception ex) {
							model.getLog().log(Level.SEVERE, "Save failed", ex);
						} finally {
							model.messageModel().setStatusBarMessage(null);
						}
						// h.dispose();
					}

					private void showServerMessage(String reply) {
						final JDialog diag = new JDialog(
								model.getGUIManager().getMainWindow());
						JEditorPaneLabel txt = new JEditorPaneLabel(
								model.getGlobals());
						txt.setEditable(false);
						txt.setText(reply);
						txt.setPreferredSize(new Dimension(300, 200));
						diag.setTitle("Server reply");
						diag.getContentPane().setLayout(new BorderLayout());
						diag.getContentPane().add(new JScrollPane(txt),
								BorderLayout.CENTER);
						diag.getContentPane().add(new JButton(
								new AbstractAction(mm.getString("button.ok")) {

									@Override
									public void actionPerformed(ActionEvent e) {
										diag.dispose();
									}

								}), BorderLayout.SOUTH);
						diag.pack();
						StaticUtils.center(
								model.getGUIManager().getMainWindow(), diag);
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
		StaticUtils.center(model.getGUIManager().getMainWindow(), this);
		setVisible(true);
	}

}

@SuppressWarnings("serial")
class MultiSelectionArray<T> extends Container {

	private final ArrayList<TCheckBox> dss = new ArrayList<TCheckBox>();

	private class TCheckBox extends JCheckBox {
		private T data;

		public TCheckBox(T e) {
			super(e.toString());
			this.data = e;
		}
	}

	protected MultiSelectionArray(Iterable<T> arr, boolean enabledFlag) {

		setLayout(new GridLayout(0, 1));
		for (T t : arr) {
			TCheckBox dsb = new TCheckBox(t);
			dsb.setEnabled(enabledFlag);
			dsb.setSelected(true);

			dss.add(dsb);
			add(dsb);

		}
	}

	protected List<T> selectedItems() {
		ArrayList<T> out = new ArrayList<T>();
		for (TCheckBox item : dss) {
			if (item.isSelected()) {
				out.add(item.data);
			}
		}
		return out;
	}

	public void selectAllItems(boolean select) {
		for (TCheckBox item : dss) {
			item.setSelected(select);
		}
	}
}