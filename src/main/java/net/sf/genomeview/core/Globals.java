package net.sf.genomeview.core;

import java.io.IOException;

import net.sf.jannot.DistributingReporter;
import net.sf.jannot.Global;
import net.sf.jannot.exception.ReadFailedException;
import tudelft.utilities.logging.Reporter;

/**
 * Contains all global stuff like logger, configuration. Similar to
 * {@link Global} but now for GenomeView as a whole
 */
public class Globals {
	private final Global global;
	private final Configuration configuration;
	private final MessageManager messageManager;

	public Globals() throws IOException, ReadFailedException {
		global = new Global();
		configuration = new Configuration(global);
		messageManager = new MessageManager(configuration.get("lang.current"));
	}

	/**
	 * Convenience method
	 * 
	 * @return the {@link Reporter}
	 */
	public DistributingReporter getLog() {
		return global.getLog();
	}

	public Configuration getConfiguration() {
		return configuration;
	}

	public MessageManager getMessageManager() {
		return messageManager;
	}

	/**
	 * 
	 * @return {@link Global}. the jannot sub-part of Globals
	 */
	public Global getGlobal() {
		return global;
	}
}
