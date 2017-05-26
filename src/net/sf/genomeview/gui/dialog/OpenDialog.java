/**
 * %HEADER%
 */
package net.sf.genomeview.gui.dialog;

import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.util.Arrays;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.SwingConstants;
import javax.swing.filechooser.FileFilter;

import be.abeel.gui.GridBagPanel;
import net.sf.genomeview.core.Configuration;
import net.sf.genomeview.core.Icons;
import net.sf.genomeview.data.DataSourceHelper;
import net.sf.genomeview.data.Model;
import net.sf.genomeview.data.NotificationTypes;
import net.sf.genomeview.gui.MessageManager;
import net.sf.genomeview.gui.StaticUtils;
import net.sf.jannot.exception.ReadFailedException;
import net.sf.jannot.source.Locator;

/**
 * 
 * @author Thomas Abeel
 * 
 */
public class OpenDialog extends JDialog {

	private static final long serialVersionUID = -9176452114031190911L;


	private final String[] exts = new String[] { "fasta", "fa", "fas", "embl", "fna", "gtf", "gff", "gff3", "maln",
			"syn", "wig", "mfa", "bed", "mapview", "bam", "maf", "snp", "tbl", "gb", "gbk", "pileup", "con", "peaks",
			"tdf","bw","bigwig" };


	public OpenDialog(Window parent, final Model model) {
		super(parent, "Load data", ModalityType.APPLICATION_MODAL);
		this.setIconImage(Icons.MINILOGO);

		setResizable(false);
		addWindowListener(new WindowAdapter() {

			@Override
			public void windowClosing(WindowEvent e) {
				super.windowClosing(e);
				model.refresh(NotificationTypes.DIALOGCHANGE);
			}

		});
		final OpenDialog _self = this;
		// EventQueue.invokeLater(new Runnable() {
		//
		// @Override
		// public void run() {
		GridBagPanel gp = new GridBagPanel();
		_self.setContentPane(gp);
		JButton file = new JButton(MessageManager.getString("opendialog.local_files"), Icons.get("Hard Disk_48x48.png"));
		configButton(file);
		gp.add(file, gp.gc);
		gp.gc.gridx++;
		JButton url = new JButton(MessageManager.getString("opendialog.url"), Icons.get("Globe_48x48.png"));
		configButton(url);
		gp.add(url, gp.gc);
		gp.gc.gridx++;
		JButton genomespace = new JButton(MessageManager.getString("opendialog.genomespace"), Icons.get("genomespace.png"));
		configButton(genomespace);
		gp.add(genomespace, gp.gc);
		
//		JButton das = new JButton("DAS(exp)", Icons.get("das.png"));
//		configButton(das);
//		gp.add(das, gp.gc);

		/** Add logic to buttons */
		file.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				_self.dispose();
				JFileChooser chooser = new JFileChooser(Configuration.getFile("lastDirectory"));
				chooser.resetChoosableFileFilters();
				for (final String ext : exts) {
					chooser.addChoosableFileFilter(new FileFilter() {

						@Override
						public boolean accept(File f) {
							if (f.isDirectory())
								return true;

							if (f.getName().toLowerCase().endsWith(ext)
									|| f.getName().toLowerCase().endsWith(ext + ".gz")
									|| f.getName().toLowerCase().endsWith(ext + ".bgz")) {
								return true;
							}

							return false;
						}

						@Override
						public String getDescription() {
							return ext;
						}

					});
				}
				chooser.addChoosableFileFilter(new FileFilter() {

					@Override
					public boolean accept(File f) {
						if (f.isDirectory())
							return true;
						for (String ext : exts) {

							if (f.getName().toLowerCase().endsWith(ext)
									|| f.getName().toLowerCase().endsWith(ext + ".gz")
									|| f.getName().toLowerCase().endsWith(ext + ".bgz")) {
								return true;
							}
						}
						return false;
					}

					@Override
					public String getDescription() {
						return Arrays.toString(exts);
					}

				});

				chooser.setMultiSelectionEnabled(true);
				int returnVal = chooser.showOpenDialog(model.getGUIManager().getParent());
				if (returnVal == JFileChooser.APPROVE_OPTION) {
					File[] files = chooser.getSelectedFiles();
//					DataSource[] out = new DataSource[files.length];
					try {
						for (int i = 0; i < files.length; i++) {
						DataSourceHelper.load(model,new Locator(files[i].toString()));

						}
						Configuration.set("lastDirectory", files[0].getParentFile());
//						load(out);
					} catch (IOException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}  catch (URISyntaxException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					} catch (ReadFailedException e2) {
						// TODO Auto-generated catch block
						e2.printStackTrace();
					}
				}

			}
		});

		url.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				_self.dispose();
				try {
					String input = JOptionPane.showInputDialog(model.getGUIManager().getParent(),
							"Give the URL of the data");
					if (input != null && input.trim().length() > 0) {
						
						DataSourceHelper.load(model,new Locator(input.trim()));
					}

				}  catch (MalformedURLException e2) {
					// TODO Auto-generated catch block
					e2.printStackTrace();
				} catch (IOException e2) {
					// TODO Auto-generated catch block
					e2.printStackTrace();
				} catch (URISyntaxException e3) {
					// TODO Auto-generated catch block
					e3.printStackTrace();
				} catch (ReadFailedException e4) {
					// TODO Auto-generated catch block
					e4.printStackTrace();
				}

			}
		});

		
		


		pack();
		StaticUtils.center(model.getGUIManager().getParent(),_self);
		setVisible(true);

		

	}

	private void configButton(JButton button) {
		button.setVerticalTextPosition(SwingConstants.BOTTOM);
		button.setHorizontalTextPosition(SwingConstants.CENTER);

	}

	

}
