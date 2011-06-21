/**
 * %HEADER%
 */
package net.sf.genomeview.data;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.util.concurrent.ExecutionException;

import javax.swing.JOptionPane;
import javax.swing.ProgressMonitorInputStream;

import net.sf.jannot.exception.ReadFailedException;
import net.sf.jannot.parser.Parser;
import net.sf.jannot.source.AbstractStreamDataSource;
import net.sf.jannot.source.DataSource;
import net.sf.jannot.source.DataSourceFactory;
import net.sf.jannot.source.IndexManager;
import net.sf.jannot.source.Locator;

/**
 * 
 * @author Thomas Abeel
 * 
 */
public class DataSourceHelper {

	public static void load(Model model, Locator data) throws URISyntaxException, IOException, ReadFailedException {
		load(model, data, false);
	}

	public static void load(final Model model, Locator data, boolean wait) throws URISyntaxException, IOException,
			ReadFailedException {
		Locator index = null;

		data.stripIndex();

		if (!data.exists()) {
			JOptionPane.showMessageDialog(null, "Data file is missing " + data + "\nSkipping this file...",
					"Data missing", JOptionPane.ERROR_MESSAGE);
			return;
		}

		if (!data.isWebservice())
			index = IndexManager.getIndex(data);

		if (data.requiresIndex() && index == null) {
			if (IndexManager.canBuildIndex(data)) {
				int res = JOptionPane.showConfirmDialog(model.getGUIManager().getParent(),
						"Index is required and missing. Do you want to create the index?", "Index required",
						JOptionPane.YES_NO_OPTION);
				if (res == JOptionPane.YES_OPTION) {
					index(model, data);
				}
				return;

			} else {
				JOptionPane.showMessageDialog(null, "Could not locate index for " + data + "\nSkipping this file...",
						"Index missing", JOptionPane.ERROR_MESSAGE);

			}

			return;
		}

		DataSource ds = DataSourceFactory.create(data, index);

		if (ds instanceof AbstractStreamDataSource) {
			AbstractStreamDataSource asd = ((AbstractStreamDataSource) ds);
			if (asd.getParser() == null) {
				Parser tmp = offerParserChoice(model);
				if (tmp != null) {
					asd.setParser(tmp);
				} else
					return;
			}
			asd.setIos(new ProgressMonitorInputStream(model.getGUIManager().getParent(), "Reading file", asd.getIos()));
			
		}

		if (index == null && data.supportsIndex() && data.length() > 5 * 1024 * 1024) {

			if (IndexManager.canBuildIndex(data)) {
				int res = JOptionPane.showConfirmDialog(model.getGUIManager().getParent(),
						"Performance would benefit from indexing this file.\n" + data
								+ "\nDo you want to create the index?", "Index missing", JOptionPane.YES_NO_OPTION);
				if (res == JOptionPane.YES_OPTION) {
					index(model, data);
				}
				return;

			} else {
				JOptionPane.showMessageDialog(null, "File is rather large and has no index.\n " + data
						+ "\nTo improve performance you may want to build an index.", "Index missing",
						JOptionPane.WARNING_MESSAGE);

			}

		} else if (index == null && data.length() > 50000000) {
			JOptionPane
					.showMessageDialog(
							null,
							"Large file:\n"
									+ data
									+ "It may take a while to load this file.\nIf GenomeView becomes unresponsive, please increase the amount of memory.",
							"Large file!", JOptionPane.WARNING_MESSAGE);
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

	private static Parser offerParserChoice(Model model) {
		Parser p = (Parser) JOptionPane.showInputDialog(model.getGUIManager().getParent(),
				"Could not detect file type, please select the correct parser manually.", "Parser detection",
				JOptionPane.QUESTION_MESSAGE, null, Parser.parsers, Parser.parsers[0]);
		return p;
	}
}
