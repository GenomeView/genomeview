/**
 * %HEADER%
 */
package net.sf.genomeview.gui;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JOptionPane;

import net.sf.genomeview.data.Model;
import net.sf.genomeview.gui.task.ReadWorker;
import net.sf.jannot.source.DataSource;
import net.sf.jannot.source.DataSourceFactory;
import net.sf.jannot.utils.URIFactory;
import be.abeel.io.LineIterator;
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

	private static void loadSession(Model model, InputStream is) {
		LineIterator it = new LineIterator(is);

		try {
			String key = it.next();
			if (!key.startsWith("##GenomeView session")) {
				JOptionPane.showMessageDialog(model.getGUIManager().getParent(),
						"The selected file is not a GenomeView session");
			} else {
				//it.next();
				model.clearEntries();
				for (String line : it) {
					char c = line.charAt(0);
					line = line.substring(2);
					DataSource ds = null;
					switch (c) {
					case 'U':
						ds = DataSourceFactory.createURL(URIFactory.url(line));
						break;
					case 'F':
						ds = DataSourceFactory.createFile(new File(line));
						break;
					default:
						// Do nothing
						log.info("Could not load session line: " + line);
						break;

					}
					final ReadWorker rw = new ReadWorker(ds, model);
					rw.execute();
				}
			}
		} catch (Exception ex) {
			CrashHandler.crash(Level.SEVERE, "Could not load session", ex);
		}
		it.close();

	}

}
