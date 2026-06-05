package net.sf.genomeview.gui.dialog;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.logging.Level;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.SwingConstants;
import javax.swing.filechooser.FileFilter;

import net.sf.genomeview.core.Icons;
import net.sf.genomeview.data.DataSourceHelper;
import net.sf.genomeview.data.Model;
import net.sf.jannot.source.Locator;

public class OpenFileButton extends JButton {

	private static final long serialVersionUID = 869265859335849511L;
	private final String[] exts = new String[] { "fasta", "fa", "fas", "embl",
			"fna", "gtf", "gff", "gff3", "maln", "syn", "wig", "mfa", "bed",
			"mapview", "bam", "maf", "snp", "tbl", "gb", "gbk", "pileup", "con",
			"peaks", "tdf", "bw", "bigwig" };
	private final Model m;

	public OpenFileButton(final Model gvModel) {
		super(gvModel.getMessageMgr().getString("opendialog.local_files"),
				Icons.get("Hard Disk_48x48.png"));
		this.m = gvModel;
		setVerticalTextPosition(SwingConstants.BOTTOM);
		setHorizontalTextPosition(SwingConstants.CENTER);
		addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {

				JFileChooser chooser = new JFileChooser(
						gvModel.getConfiguration().getFile("lastDirectory"));
				chooser.resetChoosableFileFilters();
				for (final String ext : exts) {
					chooser.addChoosableFileFilter(new FileFilter() {

						@Override
						public boolean accept(File f) {
							if (f.isDirectory()) {
								return true;
							}

							if (f.getName().toLowerCase().endsWith(ext)
									|| f.getName().toLowerCase()
											.endsWith(ext + ".gz")
									|| f.getName().toLowerCase()
											.endsWith(ext + ".bgz")) {
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
						if (f.isDirectory()) {
							return true;
						}
						for (String ext : exts) {

							if (f.getName().toLowerCase().endsWith(ext)
									|| f.getName().toLowerCase()
											.endsWith(ext + ".gz")
									|| f.getName().toLowerCase()
											.endsWith(ext + ".bgz")) {
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
				int returnVal = chooser.showOpenDialog(
						gvModel.getGUIManager().getMainWindow());
				if (returnVal == JFileChooser.APPROVE_OPTION) {
					File[] files = chooser.getSelectedFiles();
					// DataSource[] out = new DataSource[files.length];
					try {
						for (int i = 0; i < files.length; i++) {
							new DataSourceHelper(gvModel).load(new Locator(
									files[i].toString(), gvModel.getLog()));

						}
						gvModel.getConfiguration().set("lastDirectory",
								files[0].getParentFile());
						// load(out);
					} catch (IOException | URISyntaxException e1) {
						m.getLog().log(Level.WARNING, m.getMessageMgr()
								.getString("editfeature.notes_failed_warn"),
								e1);
					}
				}

			}
		});

	}

}
