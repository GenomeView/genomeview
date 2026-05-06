/**
 * %HEADER%
 */
package net.sf.genomeview.core;

import java.awt.Color;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.logging.Level;
import java.util.zip.GZIPInputStream;

import be.abeel.io.GZIPPrintWriter;
import be.abeel.io.LineIterator;
import net.sf.genomeview.data.Model;
import net.sf.jannot.DataKey;
import net.sf.jannot.Global;
import net.sf.jannot.Type;

/**
 * Low level access to the configuration.
 * 
 * @author Thomas Abeel
 * 
 */
public class Configuration {
	private final Global global;

	private final File confDir;

	/* Map with resource configuration */
	private final HashMap<String, String> resourceMap = new HashMap<String, String>();

	/* Map with default genomeview configuration */
	private final HashMap<String, String> defaultMap = new HashMap<String, String>();

	/* Map with user configuration */
	private final HashMap<String, String> localMap = new HashMap<String, String>();

	/* Map with extra configuration */
	private final HashMap<String, String> extraMap = new HashMap<String, String>();

	private final Properties gvProperties = new Properties();

	private File configFile;

	/**
	 * @param global the {@link Global} constants from jannot
	 * @throws IllegalStateException if the Configuration can not be created or
	 *                               read. This is a nasty RuntimeException that
	 *                               we don't catch anywhere, assuming
	 *                               GenomeView will die instantly at startup if
	 *                               this happens.
	 */
	public Configuration(Global global) {
		this.global = global;
		this.confDir = findOurDirectory();
		global.getLog().log(Level.INFO, "User config: " + confDir);

		try {
			load();
			/*
			 * Make sure personal conf is also available for next start. This is
			 * mainly important for the first time you launch GV
			 */
			save();
		} catch (IOException e) {
			throw new IllegalStateException("Failed to load configuration", e);
		}

	}

	/**
	 * 
	 * @return our directory, or null if we can't find or make it.
	 */
	private File findOurDirectory() {
		String s = System.getProperty("user.home");
		File dir = new File(s + "/.genomeview");
		if (!dir.exists()) {
			if (!dir.mkdir()) {
				global.getLog().log(Level.WARNING,
						"Could not create configuration in user directory: "
								+ dir + ", let's try run folder.");
				dir = new File(".genomeview");
				if (!dir.exists()) {
					if (!dir.mkdir()) {
						global.getLog().log(Level.WARNING,
								"Could not create configuration in runtime directory directory: "
										+ dir);
						dir = null;
					}
				}
			}
		}
		return dir;
	}

	/**
	 * @param key the key to search for
	 * @return the value of key, contained in {@link #resourceMap},
	 *         {@link #extraMap}, {@link #localMap} or {@link #defaultMap} (in
	 *         this order), or null if none of these contains the key
	 */
	public String get(String key) {
		if (resourceMap.containsKey(key)) {
			return resourceMap.get(key);
		} else if (extraMap.containsKey(key)) {
			return extraMap.get(key);
		} else if (localMap.containsKey(key)) {
			return localMap.get(key);

		} else if (defaultMap.containsKey(key)) {
			localMap.put(key, defaultMap.get(key));
			return defaultMap.get(key);
		} else {
			return null;
		}

	}

	public char[] getNucleotides() {
		return new char[] { 'a', 't', 'g', 'c', 'A', 'T', 'G', 'C', 'n', 'N' };
	}

	public char[] getAminoAcids() {
		return new char[] { 'M', '*', 'X', 'Y', 'W', 'V', 'U', 'T', 'S', 'R',
				'Q', 'P', 'N', 'L', 'K', 'I', 'H', 'G', 'F', 'E', 'D', 'C',
				'A' };

	}

	public Set<String> keySet() {
		Set<String> tmp = new HashSet<String>();
		tmp.addAll(extraMap.keySet());
		tmp.addAll(localMap.keySet());
		tmp.addAll(defaultMap.keySet());
		return tmp;
	}

	/**
	 * load various versious of configuration.
	 * <ol>
	 * <li>In classpath in /genomeview.properties
	 * <li>In classpath in /conf/default.conf
	 * <li>In classpath in /conf/resources.conf
	 * 
	 * @throws IOException if problem occurs
	 */
	private void load() throws IOException {
		// InputStream is = null;
		try (InputStream is = Configuration.class
				.getResourceAsStream("/conf/genomeview.properties")) {
			gvProperties.load(is);
		} catch (Exception e1) {
			global.getLog().log(Level.WARNING,
					"genomeview.properties file could not be loaded!", e1);
		}

		/* loading default configuration from the jar */

		global.getLog().log(Level.INFO, "Loading default configuration...");
		LineIterator it;

		try (InputStream is = Configuration.class
				.getResourceAsStream("/conf/default.conf")) {
			it = new LineIterator(is, true, true);
			for (String line : it) {
				String key = line.substring(0, line.indexOf('='));
				String value = line.substring(line.indexOf('=') + 1);
				defaultMap.put(key.trim(), value.trim());

			}
			it.close();
		} catch (Exception e) {
			throw new IOException("Could not find default configuration file.",
					e);
		}

		try (InputStream is = Configuration.class
				.getResourceAsStream("/conf/resources.conf")) {
			it = new LineIterator(is, true, true);
			for (String line : it) {
				String key = line.substring(0, line.indexOf('='));
				String value = line.substring(line.indexOf('=') + 1);
				resourceMap.put(key.trim(), value.trim());
			}
			it.close();

			updateSynonyms();
		} catch (Exception e) {
			throw new IOException(
					"Could not find resources configuration file.", e);
		}

		/* look for local configuration and load it if present */
		// logger.info("Configuration directory: " + confDir);

		configFile = new File(confDir, "personal.conf.gz");
		if (!configFile.exists()) {
			if (!configFile.createNewFile()) {
				throw new IOException(
						"Cannot create your personal configuration file. GenomeView seems to have no write access to you home directory!");
			}
		} else if (configFile.length() == 0) {
			// Empty config file, don't load it.
			throw new IOException(
					"Failed to load Config file, the file is empty!");
		} else {
			try {
				it = new LineIterator(
						new GZIPInputStream(new FileInputStream(configFile)));
				it.setSkipBlanks(true);
				it.setSkipComments(true);
				for (String line : it) {
					if (line.indexOf('=') > 0) {
						String key = line.substring(0, line.indexOf('='));
						String value = line.substring(line.indexOf('=') + 1);
						localMap.put(key.trim(), value.trim());
					} else {
						throw new IOException(
								"Invalid line in configuration file! '" + line
										+ "'");
					}

				}
				it.close();
			} catch (Exception e) {
				throw new IOException("Failed loading the config file.", e);
			}
		}
		updateSynonyms();

	}

	/**
	 * The resources.conf file has strings pointing to synonym files. Read them
	 * 
	 * @throws IOException
	 */
	private void updateSynonyms() throws IOException {
		/**
		 * Default synonyms
		 */
		String deflt = get("synonyms.default");

		if (deflt != null && !deflt.isEmpty()) {
			for (String s : deflt.split(",")) {
				try (InputStream is = Configuration.class
						.getResourceAsStream(s)) {
					global.getNameService().addSynonyms(is);
				} catch (Exception e) {
					throw new IOException(
							"Failed to load default synonyms for: " + s, e);
				}

			}
		}

		/**
		 * User configured additional synonyms
		 */
		String add = get("synonyms.additional");
		if (add != null && !add.isEmpty()) {
			for (String s : add.split(",")) {

				try (InputStream is = Configuration.class
						.getResourceAsStream(s)) {
					global.getNameService().addSynonyms(is);
				} catch (Exception e) {
					throw new IOException(
							"Failed to load default synonyms for: " + s, e);
				}
			}

		}
	}

	/**
	 * Save all configuration back to the respective files.
	 * 
	 * @throws IOException
	 */
	public void save() throws IOException {
		// logger.info("Saving config");

		GZIPPrintWriter out = new GZIPPrintWriter(configFile);
		out.println("time=" + System.currentTimeMillis());
		for (String s : localMap.keySet()) {
			out.println(s + "=" + localMap.get(s));
		}
		out.close();

	}

	public File getDirectory() {
		return confDir;
	}

	public Color getColor(Type t) {
		return getColor("TYPE_" + t);
	}

	public Color getColor(String string) {
		String tmp = get(string);
		if (tmp == null) {
			tmp = "GRAY";
		}
		return Colors.decodeColor(get(string));
	}

	public int getInt(String string) {
		String s = get(string);
		if (s == null) {
			return 0;
		}
		return Integer.parseInt(s);
	}

	public boolean getBoolean(String string) {
		return Boolean.parseBoolean(get(string));
	}

	public Color getNucleotideColor(char nt) {

		return getColor("N_" + nt);
	}

	public Color getAminoAcidColor(char aa) {
		return getColor("AA_" + aa);
	}

	public void set(String string, boolean b) {
		set(string, "" + b);
	}

	public void set(String key, File value) {
		set(key, value.toString());
	}

	public void set(String key, String value) {
		if (extraMap.containsKey(key)) {
			extraMap.put(key, value);
		} else {
			localMap.put(key, value);
		}

	}

	public List<String> getStringList(String key) {
		String tmp = get(key);
		List<String> out = new ArrayList<String>();
		for (String s : tmp.split(",")) {
			out.add(s.trim());
		}
		return out;
	}

	public Set<String> getStringSet(String key) {
		String tmp = get(key);
		Set<String> out = new HashSet<String>();
		for (String s : tmp.split(",")) {
			out.add(s.trim());
		}
		return out;
	}

	public Set<Type> getTypeSet(String string) {
		String tmp = get(string);
		Set<Type> out = new HashSet<Type>();
		for (String s : tmp.split(",")) {
			out.add(Type.get(s.trim()));
		}
		return out;
	}

	public String version() {
		return gvProperties.getProperty("version", "developer version");
	}

	public void loadExtra(InputStream ios) throws IOException {
		// logger.info("Loading extra config...");
		LineIterator it = new LineIterator(ios);
		it.setSkipBlanks(true);
		it.setSkipComments(true);
		for (String line : it) {

			try {
				if (line.trim().length() > 0) {
					String key = line.substring(0, line.indexOf('='));
					String value = line.substring(line.indexOf('=') + 1);
					extraMap.put(key.trim(), value.trim());
				}
			} catch (Exception e) {
				throw new IOException("Failed to parse line: " + line, e);
			}
		}
		it.close();
		updateSynonyms();

	}

	public void set(String key, int value) {
		set(key, "" + value);

	}

	public void setColor(String key, Color newColor) {
		set(key, Colors.encode(newColor));
	}

	public void setColor(Type type, Color newColor) {
		setColor("TYPE_" + type, newColor);

	}

	public void reset(Model model) {
		if (!configFile.delete()) {
			model.getLog().log(Level.WARNING, "Could not reset configuration!");
		}

		localMap.clear();
		extraMap.clear();
		defaultMap.clear();
		try {
			load();
		} catch (IOException e) {
			model.getLog().log(Level.WARNING,
					"IOException while loading configuration", e);
		}
		model.refresh();
	}

	public File getFile(String key) {
		String val = get(key);
		if (val != null) {
			return new File(val);
		} else {
			return null;
		}
	}

	public double getDouble(String string, double defaultValue) {
		String s = get(string);
		if (s == null) {
			return defaultValue;
		} else {
			return Double.parseDouble(get(string));
		}
	}

	public double getDouble(String string) {
		return Double.parseDouble(get(string));
	}

	/**
	 * some data keys have a track:weight:[datakey] value. Higher weight means
	 * further down the visual track list.
	 * 
	 * @param dk the {@link DataKey}
	 * @return the weight of dk.
	 */
	public int getWeight(DataKey dk) {
		if (get("track:weight:" + dk) == null) {
			return 1000;
		}
		return getInt("track:weight:" + dk);

	}

	public void setWeight(DataKey dk, int weight) {
		set("track:weight:" + dk, weight);

	}

	public void setVisible(DataKey dk, boolean b) {
		set("track:visible:" + dk, b);
	}

	public boolean getVisible(DataKey dk) {
		if (get("track:visible:" + dk) == null) {
			set("track:visible:" + dk, true);
			return true;
		}
		return getBoolean("track:visible:" + dk);

	}

	public void unset(String string) {
		extraMap.remove(string);
		localMap.remove(string);

	}
}
