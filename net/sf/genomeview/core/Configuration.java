/**
 * %HEADER%
 */
package net.sf.genomeview.core;

import java.awt.Color;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;
import java.util.logging.Logger;
import java.util.zip.GZIPInputStream;

import net.sf.genomeview.data.Model;
import net.sf.jannot.Type;
import be.abeel.io.GZIPPrintWriter;
import be.abeel.io.LineIterator;

/**
 * Low level access to the configuration.
 * 
 * @author Thomas Abeel
 * 
 */
public class Configuration {

	private static File confDir;

	public static char[] getNucleotides() {
		return new char[] { 'a', 't', 'g', 'c', 'A', 'T', 'G', 'C', 'n', 'N' };
	}

	public static char[] getAminoAcids() {
		return new char[] { 'M', '*', 'X', 'Y', 'W', 'V', 'U', 'T', 'S', 'R', 'Q', 'P', 'N', 'L', 'K', 'I', 'H', 'G', 'F', 'E', 'D', 'C', 'A' };

	}

	static {
		String s = System.getProperty("user.home");
		confDir = new File(s + "/.genomeview");
		if (!confDir.exists()) {
			confDir.mkdir();

		}
		System.out.println("User config: " + confDir);

	}

	private static Logger logger = Logger.getLogger(Configuration.class.getCanonicalName());

	/* Map with default genomeview configuration */
	private static HashMap<String, String> defaultMap = new HashMap<String, String>();

	/* Map with user configuration */
	private static HashMap<String, String> localMap = new HashMap<String, String>();

	/* Map with extra configuration */
	private static HashMap<String, String> extraMap = new HashMap<String, String>();

	private static Properties dbConnection = new Properties();

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
			// TODO Auto-generated catch block
			e.printStackTrace();
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

	private static File dbConnectionFile;

	private static File configFile;

	private static void load() throws IOException {
		try {
			gvProperties.load(Configuration.class.getResourceAsStream("/genomeview.properties"));
		} catch (Exception e1) {
			logger.warning("genomeview.properties file could not be loaded! GenomeView assumes your are a developer and know why you can ignore this.");

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

		dbConnectionFile = new File(confDir, "dbconnection.properties");
		if (!dbConnectionFile.exists()) {
			createDbConnection();
		}
		dbConnection.load(new FileInputStream(dbConnectionFile));

		configFile = new File(confDir, "personal.conf.gz");
		if (!configFile.exists()) {
			configFile.createNewFile();
		} else {
			it = new LineIterator(new GZIPInputStream(new FileInputStream(configFile)));
			it.setSkipBlanks(true);
			it.setSkipComments(true);
			for (String line : it) {
				//FIXME this should be fixed in AJT
				if (line.length() > 0) {
					String key = line.substring(0, line.indexOf('='));
					String value = line.substring(line.indexOf('=') + 1);
					localMap.put(key.trim(), value.trim());
				}
			}
			it.close();
		}

	}

	private static void createDbConnection() throws FileNotFoundException, IOException {
		dbConnection.put("host", "jdbc:mysql://psbsql01:3306/db_thabe_genomeview");
		dbConnection.put("user", "genomeview");
		dbConnection.put("pass", "qjW23DPYDzMs3NYa");
		dbConnection.store(new FileOutputStream(dbConnectionFile), "initial configuration");
	}

	/**
	 * Save all configuration back to the respective files.
	 * 
	 * @throws IOException
	 */
	public static void save() throws IOException {
		logger.info("Saving config");

		dbConnection.store(new FileOutputStream(dbConnectionFile), "");

		GZIPPrintWriter out = new GZIPPrintWriter(configFile);
		for (String s : localMap.keySet()) {
			out.println(s + "=" + localMap.get(s));
		}
		out.close();

	}

	public static File getDirectory() {
		return confDir;
	}

	/**
	 * Returns the directory where the modules reside.
	 * 
	 * @return
	 */
	public static File getModuleDirectory() {
		File modules = new File(confDir, "modules");
		if (!modules.exists()) {
			modules.mkdir();

		}

		return modules;

	}

	public static String getDBHost() {
		return dbConnection.getProperty("host");
	}

	public static String getDBUser() {
		return dbConnection.getProperty("user");
	}

	public static String getDBPass() {
		return dbConnection.getProperty("pass");
	}

	public static Properties getDbConnection() {
		return dbConnection;
	}

	public static Color getColor(Type t) {
		return getColor("TYPE_" + t);
	}

	public static Color getColor(String string) {
		String tmp = get(string);
		if (tmp == null)
			tmp = "GRAY";
		return ColorFactory.decodeColor(get(string));
	}

	public static int getInt(String string) {
		return Integer.parseInt(get(string));
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
		localMap.put(key, value.toString());
	}

	public static void set(String string, String value) {
		localMap.put(string, value);

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
			out.add(Type.valueOf(s.trim()));
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
			String key = line.substring(0, line.indexOf('='));
			String value = line.substring(line.indexOf('=') + 1);
			extraMap.put(key.trim(), value.trim());
		}
		it.close();

	}

	public static File getPluginDirectory() {
		File modules = new File(confDir, "plugin");
		if (!modules.exists()) {
			modules.mkdir();

		}

		return modules;
	}

	public static void set(String key, int value) {
		set(key, "" + value);

	}

	public static void setColor(String key, Color newColor) {
		set(key, ColorFactory.encode(newColor));
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
			// TODO Auto-generated catch block
			e.printStackTrace();
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
}
