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
import java.util.logging.Logger;

import javax.swing.JOptionPane;

import net.sf.genomeview.core.Configuration;
import net.sf.genomeview.gui.CrashHandler;
import net.sf.genomeview.gui.external.ExternalHelper;
import net.sf.jannot.Location;
import net.sf.jannot.source.DataSource;
import net.sf.jannot.source.Locator;
import be.abeel.io.LineIterator;
import be.abeel.net.URIFactory;

/**
 * 
 * @author Thomas Abeel
 * 
 */
public class Session {

	public static void loadSession(Model model, String in) throws IOException {
		if (in.startsWith("http://") || in.startsWith("https://")) {
			try {
				loadSession(model, URIFactory.url(in));
			} catch (MalformedURLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (URISyntaxException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else {
			loadSession(model, new File(in));
		}
	}

	public static void loadSession(Model model, File selectedFile) throws FileNotFoundException {
		loadSession(model, new FileInputStream(selectedFile));

	}

	public static void loadSession(Model model, URL url) throws IOException {
		loadSession(model, url.openStream());
	}

	private static Logger log = Logger.getLogger(Session.class.getCanonicalName());

	enum SessionInstruction {
		CONFIG, DATA, OPTION, LOCATION, C, U, F;
	}

	private static void loadSession(final Model model, final InputStream is) {
		model.messageModel().setStatusBarMessage("Preparing to load session, retrieving session file.");

		new Thread(new Runnable() {

			@Override
			public void run() {
				LineIterator it = new LineIterator(is, false, true);

				try {
					String key = it.next();
					if (!key.startsWith("##GenomeView session")) {
						JOptionPane.showMessageDialog(model.getGUIManager().getParent(), "The selected file is not a GenomeView session");
					} else {

						model.clearEntries();
						for (String line : it) {
							if (line.startsWith("#") || line.isEmpty())
								continue;
							char firstchar=line.toUpperCase().charAt(0);
							
							String[] arr = line.split("[: \t]", 2);

							model.messageModel().setStatusBarMessage("Loading session, current file: " + line + "...");
							SessionInstruction si = null;
							try {
								si = SessionInstruction.valueOf(arr[0].toUpperCase());
							} catch (Exception e) {
								log.log(Level.WARNING,"Could not parse: " + arr[0] + "\n Unknown instruction.\nCould not load session line: " + line,e);
							}
							if (si != null) {
								switch (si) {
								case U:
								case F:
								case DATA:
									try {
										DataSourceHelper.load(model, new Locator(arr[1]));
									} catch (RuntimeException re) {
										log.log(Level.SEVERE, "Something went wrong while loading line: " + line
												+ "\n\tfrom the session file.\n\tTo recover GenomeView skipped this file.", re);
									}
									break;
								case C:
								case CONFIG:
									Configuration.loadExtra(URIFactory.url(arr[1]).openStream());
									break;
								case OPTION:
									String[] ap = arr[1].split("=", 2);
									Configuration.set(ap[0], ap[1]);
									break;
								case LOCATION:
									ExternalHelper.setPosition(arr[1], model);

								}
							}

						}
					}
				} catch (Exception ex) {
					CrashHandler.crash(Level.SEVERE, "Could not load session", ex);
				}
				it.close();
				model.messageModel().setStatusBarMessage(null);

			}
		}).start();

	}

	public static void save(File f, Model model) throws IOException {
		PrintWriter out = new PrintWriter(f);
		log.info("Saving session for:" + model.loadedSources());

		out.println("##GenomeView session       ##");
		out.println("##Do not remove header lines##");
		for (DataSource ds : model.loadedSources()) {
			Locator l = ds.getLocator();
			out.println("DATA:" + l);
		}
		String e = model.vlm.getSelectedEntry().getID();
		Location l = model.vlm.getAnnotationLocationVisible();
		out.println("LOCATION:" + e + ":" + l.start + ":" + l.end);

		out.close();

	}

}
