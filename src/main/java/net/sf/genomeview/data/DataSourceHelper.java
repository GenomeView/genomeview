/**
 * %HEADER%
 */
package net.sf.genomeview.data;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.logging.Level;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.ProgressMonitorInputStream;
import javax.swing.filechooser.FileFilter;

import be.abeel.gui.MemoryWidget;
import be.abeel.io.ExtensionManager;
import htsjdk.samtools.seekablestream.SeekableFileStream;
import htsjdk.samtools.seekablestream.SeekableStream;
import net.sf.genomeview.core.MessageManager;
import net.sf.genomeview.gui.components.JOptionPaneX;
import net.sf.jannot.ConvertWig2TDF;
import net.sf.jannot.exception.ReadFailedException;
import net.sf.jannot.mafix.MafixFactory;
import net.sf.jannot.parser.Parser;
import net.sf.jannot.parser.ParserFactory;
import net.sf.jannot.source.AbstractStreamDataSource;
import net.sf.jannot.source.DataSource;
import net.sf.jannot.source.IndexManager;
import net.sf.jannot.source.Locator;
import tudelft.utilities.logging.Reporter;

/**
 * 
 * @author Thomas Abeel
 */
public class DataSourceHelper {

	private final Model model;

	/**
	 * 
	 * @param model the {@link Model}
	 */
	public DataSourceHelper(Model model) {
		this.model = model;

	}

	/**
	 * FIXME What does this do?
	 * 
	 * @param data the {@link Locator} for the data to load
	 * @throws URISyntaxException
	 * @throws IOException
	 * @throws ReadFailedException FIXME this can't throw, ?
	 */
	public void load(Locator data)
			throws URISyntaxException, IOException, ReadFailedException {
		load(data, false);
	}

	/**
	 * FIXME What does this do, it does not return anything? Side effect? FIXME
	 * this shouldn't throw? FIXME this method is way too long
	 * 
	 * @param data the {@link Locator} for the data to load
	 * @param wait if true, load waits for reader to complete.
	 * 
	 */
	public void load(Locator data, boolean wait)
			throws URISyntaxException, IOException, ReadFailedException {

		final MessageManager mm = model.getGlobals().getMessageManager();
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
			String msg = mm.formatMessage("datasourcehelper.data_missing_warn",
					new Object[] { data.getName() });
			model.getLog().log(Level.WARNING, msg);
			return;
		}

		if (!data.isWebservice()) {
			index = IndexManager.getIndex(data, model.getLog());
		}

		/* Check for stale index */
		if (index != null && index.lastModified() < data.lastModified()) {
			if (IndexManager.canBuildIndex(data)) {
				String msg = mm.formatMessage("index_outdated_warn_message",
						new Object[] { data.getName() });

				model.getLog().log(Level.WARNING, msg);
				// problem(model, "index_outdated_warn_message",
				// "index_outdated_warn_title", warn);
				index(data);

				return;

			} else {
				problem("index_outdated_error_message",
						"index_outdated_error_title", data.getName());
			}

			return;

		}

		if (data.requiresIndex() && index == null) {
			if (IndexManager.canBuildIndex(data)) {
				int res = yesno("index_missing_warn", "index_required");
				if (res == JOptionPane.YES_OPTION) {
					index(data);
				}

				return;

			} else {
				problem("couldnt_locate_index", "index_missing",
						data.getName());
			}

			return;
		}

		if (data.isWig()) {
			// FIXME
			int res = yesno("wig_not_recommended_warn", "wig_not_recommended");
			if (res == JOptionPane.YES_OPTION) {
				convertWig2TDF(data, model.getLog());
				return;
			}
		}

		if (index == null && data.supportsIndex()
				&& data.length() > 5 * 1024 * 1024) {
			if (IndexManager.canBuildIndex(data)) {
				int res = yesno("create_index", "index_missing",
						data.getName());
				if (res == JOptionPane.YES_OPTION) {
					index(data);
					return;
				}

			}

			if (data.isMaf() && !data.isBlockCompressed()) {
				int res = yesno("preprocessing_warn", "preprocessing_available",
						data.getName());
				if (res == JOptionPane.YES_OPTION) {
					mafprocess(data);
					return;
				}
			} else {
				/* It will silently try to load files up to 40 Mb */
				if (data.length() > 40 * 1024 * 1024) {

					boolean ok = JOptionPaneX.showOkCancelDialog(
							model.getGUIManager().getMainWindow(),
							mm.formatMessage(
									"datasourcehelper.load_big_file_no_index",
									new Object[] { data.getName() }),
							mm.getString("datasourcehelper.index_missing"),
							JOptionPane.WARNING_MESSAGE, model.getGlobals());
					if (!ok) {
						return;
					}
				}
			}

		} else if (index == null && data.length() > 50000000
				&& !(data.isTDF() || data.isBigWig())) {
			problem("large_file_warn", "large_file");
		}
		DataSource ds = model.getGlobal().getSourceFactory().create(data, index,
				model.getGlobal());
		if (ds instanceof AbstractStreamDataSource) {
			AbstractStreamDataSource asd = ((AbstractStreamDataSource) ds);
			if (asd.getParser() == null) {
				Parser tmp = offerParserChoice(data);
				if (tmp != null) {
					asd.setParser(tmp);
				} else {
					return;
				}
			}
			asd.setIos(new ProgressMonitorInputStream(
					model.getGUIManager().getMainWindow(),
					mm.getString("datasourcehelper.reading_file"),
					new BufferedInputStream(asd.getIos(), 512 * 1024)));

		}
		if (MemoryWidget.getAvailable() > 0 && index == null && !data.isTDF()
				&& !data.isBigWig()
				&& data.length() > (0.75 * MemoryWidget.getAvailable())) {
			System.out.println("Available mem: " + MemoryWidget.getAvailable());
			problem("memory_warn", "not_enough_memory", data.getName());
			return;
		}

		final ReadWorker rw = new ReadWorker(ds, model);
		rw.execute();
		if (wait) {
			try {
				rw.get();
			} catch (Exception e) {
				model.getLog().log(Level.WARNING,
						"datasource read worker problem", e);
			}
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
	private void problem(String messageref, String titleref, String... params) {
		final MessageManager mm = model.getMessageMgr();
		model.getLog().log(Level.WARNING,
				mm.formatMessage("datasourcehelper." + messageref, params));
	}

	/**
	 * as problem, but now showing yes/no option .
	 * 
	 * @param messageref the message ID to use in the message manager. Prefixed
	 *                   with "datasourcehelper.".
	 * @param titleref   the title ID, again used with messagemanager and
	 *                   prefixed with "datasourcehelper."
	 * @param params     the params to use with formatting the messageref
	 * @return YES_OPTION or NO_OPTION
	 */
	private int yesno(String messageref, String titleref, String... params) {
		final MessageManager mm = model.getMessageMgr();
		return JOptionPane.showConfirmDialog(
				model.getGUIManager().getMainWindow(),
				mm.formatMessage("datasourcehelper." + messageref, params),
				mm.getString("datasourcehelper." + titleref),
				JOptionPane.YES_NO_OPTION);
	}

	private void convertWig2TDF(final Locator data, final Reporter log) {
		JFileChooser chooser = new JFileChooser(
				model.getConfiguration().getFile("lastDirectory"));
		chooser.resetChoosableFileFilters();

		chooser.addChoosableFileFilter(new FileFilter() {

			@Override
			public boolean accept(File f) {
				if (f.isDirectory()) {
					return true;
				}

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
						model.getConfiguration().set("lastDirectory",
								files.getParentFile());
						File extFile = ExtensionManager.extension(files, "tdf");
						ConvertWig2TDF.convertWig2TDF(data, extFile, log);
						Locator mafdata = new Locator(extFile.toString(), log);
						log.log(Level.INFO,
								"Load newly create tdf file as: " + mafdata);
						load(mafdata);
					} catch (Exception e) {
						model.getLog().log(Level.WARNING, "failed to load tdf",
								e);
					}
				}

			});

		}

	}

	private void mafprocess(final Locator data) {
		GenomeViewScheduler.submit(new Task() {

			@Override
			public void run() {
				final MessageManager mm = model.getGlobals()
						.getMessageManager();
				try {
					JFileChooser chooser = new JFileChooser(
							model.getConfiguration().getFile("lastDirectory"));
					chooser.resetChoosableFileFilters();

					chooser.addChoosableFileFilter(new FileFilter() {

						@Override
						public boolean accept(File f) {
							if (f.isDirectory()) {
								return true;
							}

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
							return mm.getString(
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
							model.getConfiguration().set("lastDirectory",
									files.getParentFile());
							File file = ExtensionManager.extension(files,
									"maf.bgz");

							ProgressMonitorInputStream pmis = new ProgressMonitorInputStream(
									model.getGUIManager().getMainWindow(),
									mm.getString(
											"datasourcehelper.compressing_maf_file"),
									data.stream());
							pmis.getProgressMonitor()
									.setMaximum((int) data.length());
							MafixFactory.generateBlockZippedFile(pmis, file);

							SeekableStream is = new SeekableFileStream(file);
							SeekableProgressStream spmis = new SeekableProgressStream(
									model.getGUIManager().getMainWindow(),
									mm.getString(
											"datasourcehelper.indexing_maf_file"),
									is);
							spmis.getProgressMonitor()
									.setMaximum((int) file.length());
							MafixFactory.generateIndex(spmis,
									new File(file + ".mfi"));
							Locator mafdata = new Locator(file.toString(),
									model.getLog());
							model.getLog().log(Level.INFO,
									"Load newly create mafix as: " + mafdata);
							load(mafdata);

							// load(out);
						} catch (IOException | URISyntaxException e1) {
							model.getLog().log(Level.WARNING,
									"Failed to load " + files, e1);
						}
					}
				} catch (Exception e1) {
					model.getLog().log(Level.WARNING, "MAF loader failed", e1);
				}
			}
		});

	}

	/**
	 * Will try to create an index an reload the file
	 * 
	 * @param model
	 * @param prep
	 * @param log   the logger to use for the indexing
	 */
	private void index(final Locator prep) {

		// final Locator prep = data;
		GenomeViewScheduler.submit(new Task() {

			@Override
			public void run() {
				try {
					if (IndexManager.createIndex(prep, model.getLog())) {
						load(prep);
					}
				} catch (Throwable e) {
					// catch ALL errors, otherwise they end up in threadpool
					// which will dump error to stderr
					model.getLog().log(Level.SEVERE,
							"can not create index for " + prep, e);
				}
			}

		});

	}

	/**
	 * Gives the option to select a parser from a list.
	 * 
	 * @param model
	 * @param l
	 * @return a parser as selected.
	 */
	private Parser offerParserChoice(Locator l) {
		final MessageManager mm = model.getMessageMgr();
		Parser[] list = ParserFactory.parsers(l, model.getGlobal());
		Parser p = (Parser) JOptionPane.showInputDialog(
				model.getGUIManager().getMainWindow(),
				mm.getString("datasourcehelper.couldnt_detect_file"),
				mm.getString("datasourcehelper.parser_detection"),
				JOptionPane.QUESTION_MESSAGE, null, list, list[0]);
		return p;
	}
}
