/**
 * %HEADER%
 */
package net.sf.genomeview.data;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.logging.Level;

import javax.swing.JOptionPane;

import be.abeel.io.LineIterator;
import be.abeel.net.URIFactory;
import net.sf.genomeview.core.Configuration;
import net.sf.genomeview.core.MessageManager;
import net.sf.genomeview.gui.dialog.TryAgainHandler;
import net.sf.jannot.Location;
import net.sf.jannot.source.DataSource;
import net.sf.jannot.source.Locator;

/**
 * 
 * @author Thomas Abeel
 * 
 *         FIXME another singleton- apparent utility class.
 */
public class Session {

	/**
	 * 
	 * @param model
	 * @param in
	 * @return
	 * @throws IOException
	 */
	public static Thread loadSession(Model model, String in)
			throws IOException {
		model.getLog().log(Level.INFO, "Loading session from String: " + in);
		if (in.startsWith("http://") || in.startsWith("https://")) {
			try {
				return loadSession(model, URIFactory.url(in));
			} catch (MalformedURLException | URISyntaxException e) {
				model.getLog().log(Level.WARNING,
						"Failed to load from URL " + in, e);
				return null;
			}
		} else {
			return loadSession(model, new File(in));
		}
	}

	public static Thread loadSession(Model model, File selectedFile)
			throws FileNotFoundException {
		model.getLog().log(Level.INFO,
				"Loading session from File: " + selectedFile);
		return loadSession(model, new FileInputStream(selectedFile));

	}

	public static Thread loadSession(Model model, URL url) throws IOException {
		model.getLog().log(Level.INFO, "Loading session from URL: " + url);
		return loadSession(model, url.openStream());
	}

	enum SessionInstruction {
		PREFIX, CONFIG, DATA, OPTION, LOCATION, ALIAS, C, U, F, EXTRA;
	}

	/**
	 * Asynchronous loading of a session file
	 * 
	 * @param model model to load the session into
	 * @param is    inputstream that contains the session
	 * @return thread loading the session
	 */
	private static Thread loadSession(final Model model, final InputStream is) {
		final MessageManager mm = model.getMessageMgr();
		model.messageModel().setStatusBarMessage(
				mm.getString("session.preparing_load_session"));
		final Configuration config = model.getConfiguration();

		Thread t = new Thread(new Runnable() {

			@Override
			public void run() {
				LineIterator it = new LineIterator(is, false, true);

				try {
					String key = it.next();
					String lcKey = key.toLowerCase();
					if (!(lcKey.contains("genomeview")
							&& lcKey.contains("session"))) {
						JOptionPane.showMessageDialog(
								model.getGUIManager().getMainWindow(),
								mm.getString(
										"session.not_genome_view_session"));
					} else {

						model.clearEntries();
						String prefix = "";
						for (String line : it) {
							try {
								if (line.trim().startsWith("#")
										|| line.trim().isEmpty()) {
									continue;
								}
								char firstchar = line.toUpperCase().charAt(0);

								String[] arr = line.split("[: \t]", 2);

								model.messageModel()
										.setStatusBarMessage(mm.formatMessage(
												"session.loading_session_current_file_line",
												new Object[] { line }));
								SessionInstruction si = null;
								try {
									si = SessionInstruction
											.valueOf(arr[0].toUpperCase());
								} catch (Exception e) {
									model.getLog().log(Level.WARNING,
											"Could not parse: " + arr[0]
													+ "\n Unknown instruction.\nCould not load session line: "
													+ line,
											e);
								}

								if (si != null) {
									try {
										switch (si) {
										case PREFIX:
											if (arr.length == 1) {
												prefix = "";
											} else {
												prefix = arr[1].trim();
											}
											break;
										case EXTRA:
											model.getExtraSessionFiles()
													.addElement(arr[1]);
											model.getLog().log(Level.WARNING,
													"Extra file: " + arr[1]);
											break;
										case U:
										case F:
										case DATA:
											final Locator loc = new Locator(
													prefix + arr[1].trim(),
													model.getLog());
											try {
												new DataSourceHelper(model)
														.load(loc);
											} catch (RuntimeException re) {
												TryAgainHandler.ask(model,
														"Something went wrong while loading line: "
																+ line
																+ " from the session file.\n\tTo recover GenomeView skipped this file.",
														new Runnable() {
															@Override
															public void run() {
																try {
																	new DataSourceHelper(
																			model)
																			.load(loc);
																} catch (Exception e) {
																	throw new RuntimeException(
																			e);
																}
															}
														});
												model.getLog().log(
														Level.WARNING,
														"Something went wrong while loading line: "
																+ line
																+ " from the session file.\n\tAsked the user to try again.",
														re);
											}
											break;
										case C:
										case CONFIG:
											config.loadExtra(new Locator(
													prefix + arr[1].trim(),
													model.getLog()).stream());
											// Configuration.loadExtra(URIFactory.url(arr[1]).openStream());
											break;
										case OPTION:
											String[] ap = arr[1].trim()
													.split("=", 2);
											config.set(ap[0].trim(),
													ap[1].trim());
											break;
										case ALIAS:
											String[] al = arr[1].trim()
													.split("=", 2);
											model.getGlobal().getNameService()
													.addSynonym(al[1].trim(),
															al[0].trim());
											break;
										case LOCATION:
											model.setPosition(arr[1].trim());

										}
									} catch (Exception e) {
										model.getLog().log(Level.WARNING,
												"Problem while executing this instruction: "
														+ line
														+ ". Skipping this line and continuing.",
												e);

									}
								}
							} catch (Exception e) {
								model.getLog().log(Level.WARNING,
										"Problem while parsing this line: "
												+ line
												+ "\nSkipping this line and continuing.",
										e);

							}

						}
					}
				} catch (Exception ex) {
					model.getLog().log(Level.WARNING,
							mm.getString("crashhandler.couldnt_load_session"),
							ex);
				}
				it.close();
				model.messageModel().setStatusBarMessage(null);

			}
		});
		t.start();
		return t;

	}

	public static void save(File f, Model model) throws IOException {
		PrintWriter out = new PrintWriter(f);
		model.getLog().log(Level.INFO,
				"Saving session for:" + model.loadedSources());

		out.println("##GenomeView session       ##");
		out.println("##Do not remove header lines##");
		for (DataSource ds : model.loadedSources()) {
			Locator l = ds.getLocator();
			out.println("DATA:" + l);
		}
		for (String key : model.getConfiguration().keySet()) {
			out.println(
					"OPTION:" + key + "=" + model.getConfiguration().get(key));

		}

		String e = model.vlm.getVisibleEntry().getID();
		Location l = model.vlm.getAnnotationLocationVisible();
		out.println("LOCATION:" + e + ":" + l.start + ":" + l.end);

		out.close();

	}

}
