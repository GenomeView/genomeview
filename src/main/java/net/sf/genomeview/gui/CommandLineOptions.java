package net.sf.genomeview.gui;

import java.io.FileInputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;

import be.abeel.jargs.AutoHelpCmdLineParser;
import be.abeel.net.URIFactory;
import jargs.gnu.CmdLineParser.IllegalOptionValueException;
import jargs.gnu.CmdLineParser.Option;
import jargs.gnu.CmdLineParser.UnknownOptionException;
import net.sf.genomeview.core.Configuration;

/**
 * 
 * @author Thomas Abeel
 * 
 */

/**
 * Parser for the commandline options. FIXME make this normal class
 */
class CommandLineOptions {

	private final Option idO;
	private final Option sessionO;
	private final Option positionO;
	private final Option configurationO;
	private final Option fileO;
	private final Option urlO;

	private boolean goodParse; // default false initially
	private final AutoHelpCmdLineParser parser;

	/**
	 * parse command line options
	 * 
	 * @param args the original command line args
	 * @param log  the logger to log issues to
	 * @throws UnknownOptionException      if parser fails
	 * @throws IllegalOptionValueException if parser fails
	 * @throws URISyntaxException          if loading extra fails
	 * @throws IOException                 if loading extra fails
	 * @throws MalformedURLException       if loading extra fails
	 */
	public CommandLineOptions(String[] args)
			throws IllegalOptionValueException, UnknownOptionException,
			MalformedURLException, IOException, URISyntaxException {

		/* Initialize the command line options */
		parser = new AutoHelpCmdLineParser();
		urlO = parser.addHelp(parser.addStringOption("url"),
				"Start GenomeView with data loaded from the URL");

		fileO = parser.addHelp(parser.addStringOption("file"),
				"Start GenomeView with data loaded from a file.");

		configurationO = parser.addHelp(parser.addStringOption("config"),
				"Provide additional configuration to load.");

		positionO = parser.addHelp(parser.addStringOption("position"),
				"Provide the initial region that should be visible.");

		sessionO = parser.addHelp(parser.addStringOption("session"),
				"Provide a session file that contains all the files that have to be loaded.");

		idO = parser.addHelp(parser.addStringOption("id"),
				"Instance ID for this GenomeView instance, useful to control multiple GVs at once.");

		parser.parse(args);
		goodParse = true; // slightly hacky, if parse fails this remains false

		if (parser.checkHelp()) {
			System.exit(0);
		}

		/* Load the additional configuration */
		String config = (String) parser.getOptionValue(configurationO);
		if (config != null) {
			if (config.startsWith("http") || config.startsWith("ftp")) {
				Configuration.instance()
						.loadExtra(URIFactory.url(config).openStream());
			} else {
				Configuration.instance().loadExtra(new FileInputStream(config));
			}
		}

	}

	public boolean goodParse() {
		return goodParse;
	}

	public String position() {
		return (String) parser.getOptionValue(positionO);
	}

	public String file() {
		return (String) parser.getOptionValue(fileO);
	}

	public String url() {
		return (String) parser.getOptionValue(urlO);
	}

	public String session() {
		return (String) parser.getOptionValue(sessionO);
	}

	public String[] remaining() {
		return parser.getRemainingArgs();
	}

	public String id() {
		return (String) parser.getOptionValue(idO);
	}

}
