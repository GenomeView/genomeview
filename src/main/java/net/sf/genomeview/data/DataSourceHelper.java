/**
 * %HEADER%
 */
package net.sf.genomeview.data;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.util.concurrent.ExecutionException;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.ProgressMonitorInputStream;
import javax.swing.filechooser.FileFilter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import be.abeel.gui.MemoryWidget;
import be.abeel.io.ExtensionManager;
import htsjdk.samtools.seekablestream.SeekableFileStream;
import htsjdk.samtools.seekablestream.SeekableStream;
import net.sf.genomeview.core.Configuration;
import net.sf.genomeview.gui.MessageManager;
import net.sf.genomeview.gui.components.JOptionPaneX;
import net.sf.jannot.ConvertWig2TDF;
import net.sf.jannot.exception.ReadFailedException;
import net.sf.jannot.mafix.MafixFactory;
import net.sf.jannot.parser.Parser;
import net.sf.jannot.source.AbstractStreamDataSource;
import net.sf.jannot.source.DataSource;
import net.sf.jannot.source.DataSourceFactory;
import net.sf.jannot.source.IndexManager;
import net.sf.jannot.source.Locator;

/**
 * 
 * @author Thomas Abeel
 */
public class DataSourceHelper {

	// shortcuts to often used constants, to make code readable
	private final static int warn = JOptionPane.WARNING_MESSAGE;
	private final static int error = JOptionPane.ERROR_MESSAGE;

	private static Logger log = LoggerFactory
			.getLogger(DataSourceHelper.class.getCanonicalName());

	public static void load(Model model, Locator data)
			throws URISyntaxException, IOException, ReadFailedException {
		load(model, data, false);
	}

	public static void load(final Model model, Locator data, boolean wait)
			throws URISyntaxException, IOException, ReadFailedException {

		/*
		 * Check whether data locator is session, if so, load as session and
		 * skip the rest
		 */
		if (data.getName().endsWith(".gvs")) {
			Session.loadSession(model, data.toString());
			return;
		}

		Locator index = null;

		data.stripIndex();

		if (!data.exists()) {
			JOptionPane.showMessageDialog(model.getGUIManager().getMainWindow(),
					MessageManager.formatMessage(
							"datasourcehelper.data_missing_warn",
							new Object[] { data.getName() }),
					MessageManager.getString("datasourcehelper.data_missing"),
					error);
			return;
		}

		if (!data.isWebservice())
			index = IndexManager.getIndex(data);

		/* Check for stale index */
		if (index != null && index.lastModified() < data.lastModified()) {
			if (IndexManager.canBuildIndex(data)) {
				problem(model, "index_outdated_warn_message",
						"index_outdated_warn_title", warn);
				index(model, data);

				return;

			} else {
				problem(model, "index_outdated_error_message",
						"index_outdated_error_title", warn, data.getName());
			}

			return;

		}

		if (data.requiresIndex() && index == null) {
			if (IndexManager.canBuildIndex(data)) {
				int res = yesno(model, "index_missing_warn", "index_required");
				if (res == JOptionPane.YES_OPTION) {
					index(model, data);
				}

				return;

			} else {
				problem(model, "couldnt_locate_index", "index_missing", error,
						data.getName());
			}

			return;
		}

		if (data.isWig()) {
			int res = JOptionPane.showConfirmDialog(
					model.getGUIManager().getMainWindow(),
					MessageManager.getString(
							"datasourcehelper.wig_not_recommended_warn"),
					MessageManager
							.getString("datasourcehelper.wig_not_recommended"),
					JOptionPane.YES_NO_OPTION);
			if (res == JOptionPane.YES_OPTION) {
				convertWig2TDF(model, data);
				return;
			}
		}

		if (index == null && data.supportsIndex()
				&& data.length() > 5 * 1024 * 1024) {
			if (IndexManager.canBuildIndex(data)) {
				int res = yesno(model, "create_index", "index_missing",
						data.getName());
				if (res == JOptionPane.YES_OPTION) {
					index(model, data);
					return;
				}

			}

			if (data.isMaf() && !data.isBlockCompressed()) {
				int res = yesno(model, "preprocessing_warn",
						"preprocessing_available", data.getName());
				if (res == JOptionPane.YES_OPTION) {
					mafprocess(model, data);
					return;
				}
			} else {
				/* It will silently try to load files up to 40 Mb */
				if (data.length() > 40 * 1024 * 1024) {

					boolean ok = JOptionPaneX.showOkCancelDialog(
							model.getGUIManager().getMainWindow(),
							MessageManager.formatMessage(
									"datasourcehelper.load_big_file_no_index",
									new Object[] { data.getName() }),
							MessageManager.getString(
									"datasourcehelper.index_missing"),
							JOptionPane.WARNING_MESSAGE);
					if (!ok)
						return;
				}
			}

		} else if (index == null && data.length() > 50000000
				&& !(data.isTDF() || data.isBigWig())) {
			problem(model, "large_file_warn", "large_file",
					JOptionPane.ERROR_MESSAGE);
		}
		DataSource ds = DataSourceFactory.create(data, index);
		if (ds instanceof AbstractStreamDataSource) {
			AbstractStreamDataSource asd = ((AbstractStreamDataSource) ds);
			if (asd.getParser() == null) {
				Parser tmp = offerParserChoice(model, data);
				if (tmp != null) {
					asd.setParser(tmp);
				} else
					return;
			}
			asd.setIos(new ProgressMonitorInputStream(
					model.getGUIManager().getMainWindow(),
					MessageManager.getString("datasourcehelper.reading_file"),
					new BufferedInputStream(asd.getIos(), 512 * 1024)));

		}
		if (MemoryWidget.getAvailable() > 0 && index == null && !data.isTDF()
				&& !data.isBigWig()
				&& data.length() > (0.75 * MemoryWidget.getAvailable())) {
			System.out.println("Available mem: " + MemoryWidget.getAvailable());
			problem(model, "memory_warn", "not_enough_memory", error,
					data.getName());
			return;
		}

		final ReadWorker rw = new ReadWorker(ds, model);
		rw.execute();
		if (wait)
			try {
				rw.get();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ExecutionException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

	}

	/**
	 * Report problem to user. He has no choice but to click on OK button
	 * 
	 * @param model      the Model
	 * @param messageref a string for MessageManager.getString for the main
	 *                   message of the dialog
	 * @param titleref   a string for MessageManager#getString for the dialog
	 *                   title
	 * @param optinos    The {@link #warn}, {@link #error} etc
	 * @param params     the additional args to format the message.
	 */
	private static void problem(Model model, String messageref, String titleref,
			int options, String... params) {
		JOptionPane.showMessageDialog(model.getGUIManager().getMainWindow(),
				MessageManager.formatMessage("datasourcehelper." + messageref,
						params),
				MessageManager.getString("datasourcehelper." + titleref),
				options);
	}

	/**
	 * as problem, but now showing yes/no option .
	 * 
	 * @return YES_OPTION or NO_OPTION
	 */
	private static int yesno(Model model, String messageref, String titleref,
			String... params) {
		return JOptionPane.showConfirmDialog(
				model.getGUIManager().getMainWindow(),
				MessageManager.formatMessage("datasourcehelper." + messageref,
						params),
				MessageManager.getString("datasourcehelper." + titleref),
				JOptionPane.YES_NO_OPTION);
	}

	private static void convertWig2TDF(final Model model, final Locator data) {
		JFileChooser chooser = new JFileChooser(
				Configuration.getFile("lastDirectory"));
		chooser.resetChoosableFileFilters();

		chooser.addChoosableFileFilter(new FileFilter() {

			@Override
			public boolean accept(File f) {
				if (f.isDirectory())
					return true;

				if (f.getName().toLowerCase().endsWith("tdf")) {
					return true;
				}

				return false;
			}

			@Override
			public String getDescription() {
				return "TDF files";
			}

		});
		chooser.setMultiSelectionEnabled(false);
		int returnVal = chooser
				.showSaveDialog(model.getGUIManager().getMainWindow());
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			final File files = chooser.getSelectedFile();
			// DataSource[] out = new DataSource[files.length];

			GenomeViewScheduler.submit(new Task() {

				@Override
				public void run() {
					try {
						Configuration.set("lastDirectory",
								files.getParentFile());
						File extFile = ExtensionManager.extension(files, "tdf");
						ConvertWig2TDF.convertWig2TDF(data, extFile);
						Locator mafdata = new Locator(extFile.toString());
						log.info("Load newly create tdf file as: " + mafdata);
						load(model, mafdata);
					} catch (Exception e) {
						model.daemonException(e);
					}
				}

			});

		}

	}

	private static void mafprocess(final Model model, final Locator data) {
		GenomeViewScheduler.submit(new Task() {

			@Override
			public void run() {
				try {
					JFileChooser chooser = new JFileChooser(
							Configuration.getFile("lastDirectory"));
					chooser.resetChoosableFileFilters();

					chooser.addChoosableFileFilter(new FileFilter() {

						@Override
						public boolean accept(File f) {
							if (f.isDirectory())
								return true;

							if (f.getName().toLowerCase().endsWith("maf")
									|| f.getName().toLowerCase()
											.endsWith("maf.gz")
									|| f.getName().toLowerCase()
											.endsWith("maf.bgz")) {
								return true;
							}

							return false;
						}

						@Override
						public String getDescription() {
							return MessageManager.getString(
									"datasourcehelper.multiple_alignment_files");
						}

					});

					chooser.setMultiSelectionEnabled(false);
					int returnVal = chooser.showSaveDialog(
							model.getGUIManager().getMainWindow());
					if (returnVal == JFileChooser.APPROVE_OPTION) {
						File files = chooser.getSelectedFile();
						// DataSource[] out = new DataSource[files.length];
						try {
							Configuration.set("lastDirectory",
									files.getParentFile());
							File file = ExtensionManager.extension(files,
									"maf.bgz");

							ProgressMonitorInputStream pmis = new ProgressMonitorInputStream(
									model.getGUIManager().getMainWindow(),
									MessageManager.getString(
											"datasourcehelper.compressing_maf_file"),
									data.stream());
							pmis.getProgressMonitor()
									.setMaximum((int) data.length());
							MafixFactory.generateBlockZippedFile(pmis, file);

							SeekableStream is = new SeekableFileStream(file);
							SeekableProgressStream spmis = new SeekableProgressStream(
									model.getGUIManager().getMainWindow(),
									MessageManager.getString(
											"datasourcehelper.indexing_maf_file"),
									is);
							spmis.getProgressMonitor()
									.setMaximum((int) file.length());
							MafixFactory.generateIndex(spmis,
									new File(file + ".mfi"));
							Locator mafdata = new Locator(file.toString());
							log.info("Load newly create mafix as: " + mafdata);
							load(model, mafdata);

							// load(out);
						} catch (IOException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						} catch (URISyntaxException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						}
					}
				} catch (Exception e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
		});

	}

	/**
	 * Will try to create an index an reload the file
	 * 
	 * @param model
	 * @param prep
	 */
	private static void index(final Model model, final Locator prep) {

		// final Locator prep = data;
		GenomeViewScheduler.submit(new Task() {

			@Override
			public void run() {
				try {
					if (IndexManager.createIndex(prep))
						load(model, prep);
				} catch (MalformedURLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (URISyntaxException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (ReadFailedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			}

		});

	}

	private static Parser offerParserChoice(Model model, Locator l) {
		Parser p = (Parser) JOptionPane.showInputDialog(
				model.getGUIManager().getMainWindow(),
				MessageManager
						.getString("datasourcehelper.couldnt_detect_file"),
				MessageManager.getString("datasourcehelper.parser_detection"),
				JOptionPane.QUESTION_MESSAGE, null, Parser.parsers(l),
				Parser.parsers(l)[0]);
		return p;
	}
}
