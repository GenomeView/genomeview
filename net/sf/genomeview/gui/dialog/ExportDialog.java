/**
 * %HEADER%
 */
package net.sf.genomeview.gui.dialog;

import java.awt.Container;
import java.awt.EventQueue;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.ProgressMonitorInputStream;
import javax.swing.filechooser.FileFilter;

import net.sf.genomeview.core.Configuration;
import net.sf.genomeview.data.Model;
import net.sf.genomeview.gui.StaticUtils;
import net.sf.jannot.source.DataSource;
import net.sf.jannot.source.IndexManager;
import net.sf.jannot.source.Locator;

/**
 * 
 * @author Thomas Abeel
 * 
 */
public class ExportDialog extends JDialog {

	private static final long serialVersionUID = -5209291628487502687L;

	private class DataSourceCheckbox extends JCheckBox {

		private static final long serialVersionUID = -3767564402477672638L;

		private Locator data;

		private Locator idx;

		private Model m;

		private DataSourceCheckbox(Model m, Locator data, Locator idx) {
			super(data.toString());
			this.m = m;
			this.data = data;
			this.idx = idx;
		}

		public Thread[] export(File location) {
			Thread t=save(data, location);
			Thread s=save(idx, location);
			return new Thread[]{t,s};

		}

		private Thread save(Locator loc, File location) {
			if (loc == null)
				return null;
			try {
				System.out.println("Loc: "+loc+", "+location);
				System.out.println(loc.getName());
				File out = new File(location, loc.getName());
				InputStream r=null;
				if(loc.isURL())
					r=loc.url().openStream();
				else
					r=new FileInputStream(loc.file());
				
				InputStream in = new ProgressMonitorInputStream(m.getGUIManager().getParent(), "Downloading file "
						+ loc.getName(), r);
				FileOutputStream fos = new FileOutputStream(out);
				return copy(in, fos);

			} catch (MalformedURLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (URISyntaxException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return null;

		}

		private Thread copy(final InputStream in, final OutputStream out) {
			Thread t = new Thread(new Runnable() {

				@Override
				public void run() {

					// Read bytes and write to destination until eof
					try {
						byte[] buf = new byte[16 *1024* 1024];
						int len = 0;
						while ((len = in.read(buf)) >= 0) {
							out.write(buf, 0, len);
							out.flush();
						}
						out.close();
						in.close();
					} catch (Exception e) {
						log.log(Level.SEVERE, "Error while copying file", e);
					}
					
				}

			});
			t.setDaemon(true);
			t.start();
			return t;

		}

	}

	private static final Logger log = Logger.getLogger(ExportDialog.class.getCanonicalName());

	private ExportDialog(final Model model, final boolean useDefault) {
		super(model.getGUIManager().getParent(), "Export dialog", true);
		final ExportDialog _self=this;
		setLayout(new GridBagLayout());

		GridBagConstraints gc = new GridBagConstraints();
		gc.insets = new Insets(3, 3, 3, 3);
		gc.gridwidth = 2;
		gc.gridheight = 1;
		gc.gridx = 0;
		gc.gridy = 0;
		gc.weightx = 1;
		gc.weighty = 1;
		gc.fill = GridBagConstraints.BOTH;

		final ArrayList<DataSourceCheckbox> dss = new ArrayList<DataSourceCheckbox>();
		add(new JLabel("Select sources to save"), gc);

		gc.gridy++;
		Container cp = new Container();
		cp.setLayout(new GridLayout(0, 1));
		for (DataSource ds : model.loadedSources()) {
			Locator data = ds.getLocator();
			if (data.isWebservice())
				continue;
			Locator idx = null;

			if (ds.isIndexed()) {
				idx = IndexManager.getIndex(data);
			}

			DataSourceCheckbox dsb = new DataSourceCheckbox(model, data, idx);
			dsb.setSelected(true);

			dss.add(dsb);
			cp.add(dsb);

		}
		add(new JScrollPane(cp), gc);
		gc.gridy++;
		JButton save = new JButton("Save");
		JButton close = new JButton("Close");

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

							File location = file();

							if (location == null){
								h.dispose();
								return;
							}

							final ArrayList<Thread>monitor=new ArrayList<Thread>();
							for (DataSourceCheckbox dsb : dss) {
								if (dsb.isSelected()) {
									Thread[]a=dsb.export(location);
									for(int i=0;i<a.length;i++)
										if(a[i]!=null)
										monitor.add(a[i]);

								}
							}
							Thread moni=new Thread(new Runnable(){

								@Override
								public void run() {
									for(Thread t:monitor){
										try {
											t.join();
										} catch (InterruptedException e) {
											// TODO Auto-generated catch block
											e.printStackTrace();
										}
									}
									
									
									JOptionPane.showMessageDialog(model.getGUIManager().getParent(), "Export completed");
									
								}
								
							});
							moni.setDaemon(true);
							moni.start();
							JOptionPane.showMessageDialog(model.getGUIManager().getParent(), "Export started...\nYou will be notified when all files have been copied!");
						
						} catch (Exception ex) {
							log.log(Level.SEVERE, "Save failed", ex);
							JOptionPane.showMessageDialog(model.getGUIManager().getParent(), "Save failed!");
						}
						h.dispose();
						_self.dispose();
					}

//					private void showServerMessage(String reply) {
//						final JDialog diag = new JDialog(model.getGUIManager().getParent());
//						JTextArea txt = new JTextArea(10, 20);
//						txt.setEditable(false);
//						txt.setText(reply);
//						diag.setTitle("Server reply");
//						diag.getContentPane().setLayout(new BorderLayout());
//						diag.getContentPane().add(txt, BorderLayout.CENTER);
//						diag.getContentPane().add(new JButton(new AbstractAction("OK") {
//
//							@Override
//							public void actionPerformed(ActionEvent e) {
//								diag.dispose();
//							}
//
//						}), BorderLayout.SOUTH);
//						diag.pack();
//						StaticUtils.center(diag);
//						diag.setVisible(true);
//
//					}

				});
			}

			private File file() {
				JFileChooser chooser = new JFileChooser(Configuration.getFile("lastDirectory"));
				chooser.addChoosableFileFilter(new FileFilter() {

					@Override
					public String getDescription() {
						return "Select directory to store output";
					}

					@Override
					public boolean accept(File f) {
						return f.isDirectory();
					}
				});
				chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
				int returnVal = chooser.showSaveDialog(model.getGUIManager().getParent());
				if (returnVal == JFileChooser.APPROVE_OPTION) {
					File files = chooser.getSelectedFile();
					return files;
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
		StaticUtils.center(model.getGUIManager().getParent(),this);
		setVisible(true);
	}

	public static void display(Model model, boolean useDefault) {
		new ExportDialog(model, useDefault);

	}

}
