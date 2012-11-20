/**
 * %HEADER%
 */
package net.sf.genomeview.core;

import java.awt.Color;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.GZIPInputStream;

import net.sf.genomeview.data.Model;
import net.sf.genomeview.gui.CrashHandler;
import net.sf.jannot.DataKey;
import net.sf.jannot.Type;
import net.sf.jannot.parser.EMBLParser;
import net.sf.jannot.parser.GFF3Parser;
import net.sf.jannot.parser.Parser;
import be.abeel.io.GZIPPrintWriter;
import be.abeel.io.LineIterator;

/**
 * Low level access to the configuration.
 * 
 * @author Thomas Abeel
 * 
 */
public class Configuration {

	public static final Color green = new Color(0, 128, 0);

	private static File confDir;

	public static char[] getNucleotides() {
		return new char[] { 'a', 't', 'g', 'c', 'A', 'T', 'G', 'C', 'n', 'N' };
	}

	public static char[] getAminoAcids() {
		return new char[] { 'M', '*', 'X', 'Y', 'W', 'V', 'U', 'T', 'S', 'R', 'Q', 'P', 'N', 'L', 'K', 'I', 'H', 'G',
				'F', 'E', 'D', 'C', 'A' };

	}

	private static Logger logger = Logger.getLogger(Configuration.class.getCanonicalName());

	static {
		String s = System.getProperty("user.home");
		confDir = new File(s + "/.genomeview");
		if (!confDir.exists()) {
			if (!confDir.mkdir())
				logger.warning("Could not create configuration directory: " + confDir);

		}
		logger.info("User config: " + confDir);

	}

	/* Map with default genomeview configuration */
	private static HashMap<String, String> defaultMap = new HashMap<String, String>();

	/* Map with user configuration */
	private static HashMap<String, String> localMap = new HashMap<String, String>();

	/* Map with extra configuration */
	private static HashMap<String, String> extraMap = new HashMap<String, String>();

	private static Properties gvProperties = new Properties();

	static {
		try {
			load();
			/*
			 * Make sure personal conf is also available for next start. This is
			 * mainly important for the first time you launch GV
			 */
			save();
		} catch (IOException e) {
			CrashHandler.crash(Level.SEVERE, "IOException while loading configuration", e);

		}
	}

	public static String get(String key) {
		if (extraMap.containsKey(key)) {
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

	private static File configFile;

	private static void load() throws IOException {
		InputStream is = null;
		try {
			is = Configuration.class.getResourceAsStream("/genomeview.properties");
			gvProperties.load(is);
		} catch (Exception e1) {
			logger.warning("genomeview.properties file could not be loaded! GenomeView assumes your are a developer and know why you can ignore this.");

		} finally {
			if (is != null)
				is.close();
		}

		/* loading default configuration from the jar */

		logger.info("Loading default configuration...");
		LineIterator it;

		it = new LineIterator(Configuration.class.getResourceAsStream("/conf/default.conf"));
		it.setSkipBlanks(true);
		it.setSkipComments(true);
		for (String line : it) {
			String key = line.substring(0, line.indexOf('='));
			String value = line.substring(line.indexOf('=') + 1);
			defaultMap.put(key.trim(), value.trim());
		}
		it.close();

		/* look for local configuration and load it if present */
		logger.info("Configuration directory: " + confDir);

		configFile = new File(confDir, "personal.conf.gz");
		if (!configFile.exists()) {
			if (!configFile.createNewFile()) {
				logger.warning("Cannot create your personal configuration file sure GenomeView has write access to you home directory!");
			}
		} else if (configFile.length() == 0) {
			// Empty config file, don't load it.
			logger.warning("Config file has size zero!");
		} else {
			try {
				it = new LineIterator(new GZIPInputStream(new FileInputStream(configFile)));
				it.setSkipBlanks(true);
				it.setSkipComments(true);
				for (String line : it) {
					if (line.indexOf('=') > 0) {
						String key = line.substring(0, line.indexOf('='));
						String value = line.substring(line.indexOf('=') + 1);
						localMap.put(key.trim(), value.trim());
					} else {
						logger.warning("Invalid line in configuration file! '" + line + "'");
					}

				}
				it.close();
			} catch (Exception e) {
				logger.log(Level.SEVERE, "Something went horribly wrong while loading the configuration.", e);
			}
		}

	}

	/**
	 * Save all configuration back to the respective files.
	 * 
	 * @throws IOException
	 */
	public static void save() throws IOException {
		logger.info("Saving config");

		GZIPPrintWriter out = new GZIPPrintWriter(configFile);
		out.println("time=" + System.currentTimeMillis());
		for (String s : localMap.keySet()) {
			out.println(s + "=" + localMap.get(s));
		}
		out.close();

	}

	public static File getDirectory() {
		return confDir;
	}

	public static Color getColor(Type t) {
		return getColor("TYPE_" + t);
	}

	public static Color getColor(String string) {
		String tmp = get(string);
		if (tmp == null)
			tmp = "GRAY";
		return Colors.decodeColor(get(string));
	}

	public static int getInt(String string) {
		String s = get(string);
		if (s == null)
			return 0;
		return Integer.parseInt(s);
	}

	public static boolean getBoolean(String string) {
		return Boolean.parseBoolean(get(string));
	}

	public static Color getNucleotideColor(char nt) {

		return getColor("N_" + nt);
	}

	public static Color getAminoAcidColor(char aa) {
		return getColor("AA_" + aa);
	}

	public static void set(String string, boolean b) {
		set(string, "" + b);
	}

	public static void set(String key, File value) {
		set(key, value.toString());
	}

	public static void set(String key, String value) {
		if (extraMap.containsKey(key))
			extraMap.put(key, value);
		else
			localMap.put(key, value);

	}

	public static Set<String> getStringSet(String key) {
		String tmp = get(key);
		Set<String> out = new HashSet<String>();
		for (String s : tmp.split(",")) {
			out.add(s.trim());
		}
		return out;
	}

	public static Set<Type> getTypeSet(String string) {
		String tmp = get(string);
		Set<Type> out = new HashSet<Type>();
		for (String s : tmp.split(",")) {
			out.add(Type.get(s.trim()));
		}
		return out;
	}

	public static String version() {
		return gvProperties.getProperty("version", "developer version");
	}

	public static void loadExtra(InputStream ios) throws IOException {
		logger.info("Loading extra config...");
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
				logger.log(Level.SEVERE, "Failed to parse line: " + line, e);
			}
		}
		it.close();

	}

	public static File getPluginDirectory() {
		File modules = new File(confDir, "plugin");
		if (!modules.exists()) {
			if (!modules.mkdir())
				logger.warning("Cannot create plugin directory, make sure GenomeView has write access to you home directory!");
		}

		return modules;
	}

	public static void set(String key, int value) {
		set(key, "" + value);

	}

	public static void setColor(String key, Color newColor) {
		set(key, Colors.encode(newColor));
	}

	public static void setColor(Type type, Color newColor) {
		setColor("TYPE_" + type, newColor);

	}

	public static void reset(Model model) {
		if (!configFile.delete()) {
			System.err.println("Could not reset configuration!");
		}
		localMap.clear();
		extraMap.clear();
		defaultMap.clear();
		try {
			load();
		} catch (IOException e) {
			CrashHandler.crash(Level.SEVERE, "IOException while loading configuration", e);
		}
		model.refresh();
	}

	public static File getFile(String key) {
		String val = get(key);
		if (val != null)
			return new File(val);
		else
			return null;
	}

	public static double getDouble(String string, double defaultValue) {
		String s = get(string);
		if (s == null)
			return defaultValue;
		else
			return Double.parseDouble(get(string));
	}

	public static double getDouble(String string) {
		return Double.parseDouble(get(string));
	}

	public static int getWeight(DataKey dk) {
		if (get("track:weight:" + dk) == null)
			return 1000;
		return getInt("track:weight:" + dk);

	}

	public static void setWeight(DataKey dk, int weight) {
		set("track:weight:" + dk, weight);

	}

	public static void setVisible(DataKey dk, boolean b) {
		set("track:visible:" + dk, b);
	}

	public static boolean getVisible(DataKey dk) {
		if (get("track:visible:" + dk) == null){
			set("track:visible:" + dk,true);
			return true;
		}
		return getBoolean("track:visible:" + dk);

	}

	public static Parser getParser(String string) {
		String pKey = Configuration.get(string);
		if (pKey.equals("EMBL")) {
			return Parser.EMBL;
		}
		if (pKey.equals("GFF")) {
			return Parser.GFF3;
		}

		return null;
	}

	public static void unset(String string) {
		extraMap.remove(string);
		localMap.remove(string);

	}
}
