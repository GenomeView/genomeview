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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.JOptionPane;

import net.sf.genomeview.core.Configuration;
import net.sf.genomeview.gui.CrashHandler;
import net.sf.genomeview.gui.MessageManager;
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

	private static Logger log = LoggerFactory.getLogger(Session.class.getCanonicalName());

	enum SessionInstruction {
		PREFIX, CONFIG, DATA, OPTION, LOCATION, C, U, F;
	}

	private static void loadSession(final Model model, final InputStream is) {
		model.messageModel().setStatusBarMessage(MessageManager.getString("session.preparing_load_session"));

		new Thread(new Runnable() {

			@Override
			public void run() {
				LineIterator it = new LineIterator(is, false, true);

				try {
					String key = it.next();
					String lcKey = key.toLowerCase();
					if (!(lcKey.contains("genomeview") && lcKey.contains("session"))) {
						JOptionPane.showMessageDialog(model.getGUIManager().getParent(), MessageManager.getString("session.not_genome_view_session"));
					} else {

						model.clearEntries();
						String prefix = "";
						for (String line : it) {
							if (line.startsWith("#") || line.isEmpty())
								continue;
							char firstchar = line.toUpperCase().charAt(0);

							String[] arr = line.split("[: \t]", 2);
							
							model.messageModel().setStatusBarMessage(MessageManager.formatMessage("session.loading_session_current_file_line", new Object[]{arr[1]}));
							SessionInstruction si = null;
							try {
								si = SessionInstruction.valueOf(arr[0].toUpperCase());
							} catch (Exception e) {
								log.warn("Could not parse: " + arr[0] + "\n Unknown instruction.\nCould not load session line: " + line, e);
							}

							if (si != null) {
								try {
									switch (si) {
									case PREFIX:
										prefix = arr[1];
										break;
									case U:
									case F:
									case DATA:
										try {
											DataSourceHelper.load(model, new Locator(prefix + arr[1]));
										} catch (RuntimeException re) {
											log.error("Something went wrong while loading line: " + line
													+ "\n\tfrom the session file.\n\tTo recover GenomeView skipped this file.", re);
										}
										break;
									case C:
									case CONFIG:
										Configuration.loadExtra(new Locator(prefix + arr[1]).stream());
										// Configuration.loadExtra(URIFactory.url(arr[1]).openStream());
										break;
									case OPTION:
										String[] ap = arr[1].split("=", 2);
										Configuration.set(ap[0], ap[1]);
										break;
									case LOCATION:
										ExternalHelper.setPosition(arr[1], model);

									}
								} catch (Exception e) {
									log.warn("Exception while executing this instruction: " + line + "\n Skipping this line and continuing.", e);
								}
							}

						}
					}
				} catch (Exception ex) {
					CrashHandler.crash(MessageManager.getString("crashhandler.couldnt_load_session"), ex);
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
