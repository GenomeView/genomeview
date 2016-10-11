package net.sf.genomeview.gui;

import jargs.gnu.CmdLineParser.IllegalOptionValueException;
import jargs.gnu.CmdLineParser.Option;
import jargs.gnu.CmdLineParser.UnknownOptionException;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.sf.genomeview.core.Configuration;

import be.abeel.jargs.AutoHelpCmdLineParser;
import be.abeel.net.URIFactory;

/**
 * 
 * @author Thomas Abeel
 * 
 */

class CommandLineOptions {

	private static Option idO;
	private static Option sessionO;
	private static Option positionO;
	private static Option configurationO;
	private static Option fileO;
	private static Option urlO;
	private static Logger logger=LoggerFactory.getLogger(CommandLineOptions.class.getCanonicalName());
	private static boolean goodParse;
	private static AutoHelpCmdLineParser parser;

	static void init(String[] args) {

		/* Initialize the command line options */
		parser = new AutoHelpCmdLineParser();
		urlO = parser.addHelp(parser.addStringOption("url"), "Start GenomeView with data loaded from the URL");

		fileO = parser.addHelp(parser.addStringOption("file"), "Start GenomeView with data loaded from a file.");

		configurationO = parser.addHelp(parser.addStringOption("config"),
				"Provide additional configuration to load.");

		positionO = parser.addHelp(parser.addStringOption("position"),
				"Provide the initial region that should be visible.");

		sessionO = parser.addHelp(parser.addStringOption("session"),
				"Provide a session file that contains all the files that have to be loaded.");

		idO = parser.addHelp(parser.addStringOption("id"),
				"Instance ID for this GenomeView instance, useful to control multiple GVs at once.");

		goodParse = parse(parser, args);

		if (parser.checkHelp()) {
			System.exit(0);
		}
		
		/* Load the additional configuration */
		String config=(String)parser.getOptionValue(configurationO);
		if (config != null) {
			try {
				if (config.startsWith("http") || config.startsWith("ftp")) {
					Configuration.loadExtra(URIFactory.url(config).openStream());
				} else {
					Configuration.loadExtra(new FileInputStream(config));
				}
			} catch (FileNotFoundException e) {
				logger.error( "loading extra configuration", e);
			} catch (MalformedURLException e) {
				logger.error( "loading extra configuration", e);
			} catch (IOException e) {
				logger.error( "loading extra configuration", e);
			} catch (URISyntaxException e) {
				logger.error( "loading extra configuration", e);
			}
		}
		
		
	}

	private static boolean parse(AutoHelpCmdLineParser parser, String[] args) {
		try {
			parser.parse(args);
			return true;
		} catch (IllegalOptionValueException e) {
			logger.error( e.getMessage(), e);
			CrashHandler.showErrorMessage(MessageManager.getString("commandlineoptions.parsing_command_line_error") + " " + e.getMessage()
					+ "\n\n" + MessageManager.getString("commandlineoptions.will_continue_without_args"), e);
		} catch (UnknownOptionException e) {
			logger.error( e.getMessage(), e);
			CrashHandler.showErrorMessage(MessageManager.getString("commandlineoptions.parsing_command_line_error") + " " + e.getMessage()
					+ "\n\n" + MessageManager.getString("commandlineoptions.will_continue_without_args"), e);
		}
		return false;

	}

	public static boolean goodParse() {
		return goodParse;
	}

	public static String position() {
		return (String) parser.getOptionValue(positionO);
	}

	public static String file() {
		return (String) parser.getOptionValue(fileO);
	}

	public static String url() {
		return (String) parser.getOptionValue(urlO);
	}

	public static String session() {
		return (String) parser.getOptionValue(sessionO);
	}

	public static String[] remaining() {
		return parser.getRemainingArgs();
	}

	public static String id() {
		return (String) parser.getOptionValue(idO);
	}


}
